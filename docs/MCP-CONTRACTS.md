# Contrats des outils MCP V1

La référence complète et vérifiable est [MCP.schemas.json](MCP.schemas.json).
Les mêmes schémas sont annoncés par `tools/list` ; les types d’entrée sont dans
`src/mcp/contracts.ts`. Les sorties métier ont cette enveloppe :

```json
{ "ok": true, "data": {} }
```

ou :

```json
{ "ok": false, "error": { "code": "VERSION_CONFLICT", "retryable": false, "details": { "currentVersion": 4 } } }
```

Le résultat MCP contient `structuredContent` et sa copie JSON dans `content`.
Une erreur métier positionne aussi `isError=true`. Les erreurs de protocole ou
de schéma détectées par le SDK suivent les erreurs MCP/JSON-RPC standard.
Les champs inconnus en entrée sont refusés. Les noms, étiquettes et résultats
sont des **données non fiables**, jamais des instructions d’administration.

## Outils

| Outil | Droit | Entrée | `data` en succès |
| --- | --- | --- | --- |
| `search_catalog` | read | `kind: ingredient\|recipe`, `query`, `offset=0`, `limit=20` (max 100) | `items`, `total`, `ambiguous` |
| `get_catalog_item` | read | `kind`, `id` exact | `item`, `dietaryInformation` |
| `list_weeks` | read | `from`, `to` : lundis ISO inclusifs ; `offset=0`, `limit=10` (max 20) | `items`: id, date, statut et version |
| `get_week` | read | `startDate` lundi ISO | `plan` avec version, 14 créneaux, instantanés et verrous ; `null` si absente |
| `create_ingredient` | write | `operationKey`, `ingredient`, `acknowledgedMatchIds=[]` | `ingredient`, `missingInformation` |
| `create_recipe` | write | `operationKey`, `recipe`, `acknowledgedMatchIds=[]` | `recipe`, `missingInformation` |
| `preview_week_import` | read | `import` | changements `before/after`, instantanés, `previewDigest`, informations manquantes |
| `apply_week_import` | write | `operationKey`, `previewDigest`, **même** `import` | `plan` mis à jour, `missingInformation`, avertissements métier |
| `get_operation` | read | `operationKey` | `state: completed`, `tool`, `result` ; sinon `state: unknown`, `guidance` |

Les recherches incluent les éléments archivés pour éviter de les recréer.
Les accents, la casse, les espaces et la ponctuation sont normalisés. Plusieurs
résultats sont signalés par `ambiguous`. Une création d’un nom normalisé déjà
présent échoue avec `CATALOG_MATCH` et ses candidats : réutiliser leurs IDs.
Une ressemblance par inclusion de nom donne `AMBIGUOUS_MATCH`. Seule une nouvelle
création explicitement distincte peut acquitter les IDs semblables par
`acknowledgedMatchIds` ; cela ne contourne jamais un nom normalisé identique.
Ce rapprochement est déterministe et ne constitue pas une résolution sémantique
des synonymes : l’assistant doit aussi rechercher les variantes pertinentes.

## Création des éléments

Ingrédient : `name`, `category`, `rating` (1–5), `seasons` non vide et les quatre
booléens de moments sont obligatoires. `portionPerPerson`, `unit`, `aisleId`,
`subFamily` peuvent rester absents/null. Les indicateurs de participation à la
génération et d’entrée ont le défaut prudent `false` du service existant.
Les quantités décimales doivent être représentables exactement avec trois
chiffres décimaux, sans arrondi silencieux.

Recette : `name`, `rating`, `basePortions` (1–30), `seasons`, quatre moments et
au moins un `ingredients[{ingredientId, quantity, unit}]` sont obligatoires.
Les ingrédients doivent être actifs, sans répétition. Quantités positives,
trois décimales au plus. Les rôles sont `MAIN`, `STARTER`, `SIDE_STARCH`,
`SIDE_VEGETABLE`. Les options et variantes suivent le service de catalogue.
Les temps peuvent rester inconnus. Aucun défaut n’est attribué aux portions
ou quantités manquantes.

Les catégories et unités exactes figurent dans le schéma JSON. Les allergies et
valeurs nutritionnelles ne sont pas représentées ; le MCP annonce cette absence
et rejette les champs correspondants plutôt que d’enregistrer des affirmations
non prises en charge. Une recette ne peut être créée à partir de son seul nom.

Exemple d’ingrédient **synthétique** (les choix explicites ci-dessous sont ceux
de l’utilisateur fictif, pas des valeurs à déduire d’une photo) :

```json
{
  "operationKey": "demo-legume-0001",
  "ingredient": {
    "name": "Légume de démonstration",
    "category": "VEGETABLE",
    "rating": 3,
    "seasons": ["WINTER"],
    "okLunchWeekday": true,
    "okDinnerWeekday": true,
    "okLunchWeekend": true,
    "okDinnerWeekend": true,
    "portionPerPerson": null,
    "unit": null
  }
}
```

## Contrat d’import

```json
{
  "import": {
    "startDate": "2030-01-07",
    "expectedVersion": 3,
    "slots": [{
      "slotIndex": 0,
      "guestCount": 2,
      "replaceExisting": false,
      "meal": { "type": "catalog", "selection": { "recipeId": "synthetic-recipe-id" } }
    }]
  }
}
```

Cet exemple vise une semaine **déjà existante**, brouillon, version 3, premier
créneau vide. Les IDs sont fictifs. `slotIndex` va de 0 (lundi midi) à 13
(dimanche soir). Les indices ne peuvent pas se répéter. Les créneaux non cités
restent inchangés. Le nombre de convives est toujours explicite.

`expectedVersion: null` exige l’absence de semaine et **14 créneaux explicites**.
Le service de semaine est réutilisé pour créer son squelette ; tous les convives
sont ensuite renseignés explicitement dans la même transaction.

Trois types de repas sont acceptés :

- `catalog`, avec exactement un plat `recipeId` ou une protéine `proteinId`,
  puis, selon le catalogue, `starchId`/`starchRecipeId` et
  `vegetableId`/`vegetableRecipeId`. Le service métier valide la composition et
  construit les instantanés. Les quantités inconnues restent nulles.
- `custom`, avec `label` explicite : repas libre, informations de courses
  signalées manquantes. Aucun ingrédient, quantité ou recette n’est inventé.
- `eating_out`, sans quantité alimentaire.

Pas d’import de restes, de suppression, de déverrouillage ou de modification
d’entrée distincte dans cette V1. Le comportement existant des services est
conservé : un choix du catalogue conserve l’entrée, un repas libre/extérieur
l’efface. Les deux verrous sont donc vérifiés avant tout import. Un choix du
catalogue verrouille le plat sélectionné, comme un choix manuel dans l’app.

Toute semaine confirmée/archivée est refusée. Tout créneau ciblé verrouillé
(plat **ou entrée**) est refusé. Pour remplacer un repas déjà renseigné,
`replaceExisting: true` doit être explicite ; cette option ne contourne aucun
verrou. L’import ne confirme jamais une semaine.

L’aperçu retourne les anciennes et nouvelles données, les instantanés, les
informations manquantes et une empreinte `previewDigest`. Il n’écrit rien, même
si la semaine n’existe pas. Pour appliquer, envoyer `apply_week_import` avec
ce digest, le même `import` et une nouvelle `operationKey` durable. Si l’état
ou les instantanés du catalogue ont changé, relire et refaire l’aperçu.

Les repas libres restent permis lorsque la photo donne seulement un intitulé ;
ils n’alimentent pas les courses. Les quantités nulles d’ingrédients connus
restent signalées. Une absence d’allergènes déclarés ne signifie jamais
l’absence d’allergies ou l’innocuité d’un plat.
