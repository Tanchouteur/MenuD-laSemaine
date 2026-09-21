# Proposition d'évolution : plusieurs foyers sans comptes individuels

> Statut : note de conception, aucune implémentation réalisée.
>
> But : conserver une base de réflexion exploitable le jour où l'application
> devra gérer plusieurs familles sur `menus.tanchou.fr`.

## Conclusion rapide

Ce serait un chantier **de taille moyenne**, pas une réécriture complète.

Le moteur de génération, les composants de repas, les instantanés et une bonne
partie de l'interface peuvent rester tels quels. En revanche, l'application a
été conçue autour d'un foyer unique : les semaines, le catalogue, les réglages,
le mot de passe et le calendrier sont actuellement globaux. Ajouter seulement
un sélecteur à l'écran provoquerait des mélanges de données.

La bonne évolution consiste à introduire un `Household` (`Foyer`) comme
propriétaire de toutes les données, puis à obliger chaque lecture et écriture à
s'effectuer dans le foyer actif.

Estimation très indicative pour une version propre, migrée et testée :
**8 à 14 jours de développement concentré**. Un prototype pourrait sembler
fonctionner en 3 à 5 jours, mais il manquerait vraisemblablement les protections
contre les accès croisés, la migration sûre et les tests d'isolation.

## Besoin exprimé

Une même installation doit pouvoir héberger plusieurs foyers, par exemple :

- `Couple` pour les semaines passées à deux ;
- `Tanchou` pour un premier domicile familial ;
- `Béthisie` pour un second domicile familial.

Sur un appareil autorisé, une personne peut déverrouiller plusieurs foyers et
passer facilement de l'un à l'autre. Les autres membres n'ont pas besoin de
compte personnel : ils utilisent un code partagé ou un lien d'invitation du
foyer concerné.

Le mot « foyer » est préférable dans le modèle et l'interface : il désigne le
contexte dans lequel on prépare les menus, sans supposer une structure familiale
particulière.

## Ce que fait l'application actuellement

L'état actuel est explicitement mono-foyer :

- `WeeklyPlan.startDate` est unique dans toute la base. Deux foyers ne peuvent
  donc pas avoir chacun un menu pour le même lundi.
- `Ingredient.name`, `Recipe.name` et `Aisle.name` sont uniques globalement.
- `AppSettings` est un singleton dont l'identifiant est toujours `default`.
- les services Prisma ne reçoivent aucun contexte de foyer et chargent toutes
  les données disponibles ;
- l'historique utilisé par la génération mélange toutes les semaines
  confirmées ;
- `FAMILY_PASSWORD` protège toute l'installation avec un seul mot de passe ;
- `CALENDAR_TOKEN` donne accès à un flux unique qui rassemble toutes les
  semaines confirmées ;
- le nom iCalendar est fixé à « Menus de la famille ».

Le moteur de génération pur n'est pas réellement le problème. Une fois qu'on
lui fournit le catalogue et l'historique du bon foyer, ses règles peuvent
rester inchangées.

## Expérience proposée

### Premier accès sur un appareil

1. L'utilisateur arrive sur une page « Choisir un foyer ».
2. Il choisit un foyer puis saisit son code partagé, ou ouvre un lien
   d'invitation temporaire.
3. Le navigateur mémorise l'autorisation de ce foyer.
4. L'utilisateur arrive sur la semaine courante de ce foyer.

Un membre de `Tanchou` peut ainsi ne voir que `Tanchou`. Le navigateur de Louis
peut avoir déverrouillé `Couple`, `Tanchou` et `Béthisie` et afficher les trois.

### Navigation quotidienne

- Le foyer actif reste toujours visible dans l'en-tête, par exemple
  `⌂ Couple ▾`.
- Le sélecteur permet de changer de foyer sans se reconnecter lorsque celui-ci
  a déjà été déverrouillé sur l'appareil.
- Les liens contiennent le foyer : `/f/couple`, `/f/couple/courses`,
  `/f/tanchou/recettes`, etc.
- La navigation basse conserve le préfixe du foyer actif.
- Le changement de foyer recharge toutes les données ; aucun état de formulaire
  d'un foyer ne doit être réutilisé dans un autre.

Des URL explicites sont un peu plus longues qu'un simple cookie `foyerActif`,
mais elles évitent qu'un onglet ouvert sur `Couple` se mette soudain à modifier
`Tanchou` après un changement effectué dans un autre onglet.

### Création et administration

La création, la modification et la suppression d'un foyer sont réservées à un
**code administrateur de l'installation**, distinct des codes partagés des
foyers. Cela permet à Louis de gérer les trois foyers sans donner ce pouvoir à
toutes les personnes qui utilisent l'application.

Une page protégée, par exemple `/administration/foyers`, permettrait à
l'administrateur de :

- créer un foyer et choisir son catalogue initial ;
- modifier son nom, son slug et ses réglages par défaut ;
- définir ou modifier individuellement le code d'accès partagé de chaque foyer ;
- révoquer les sessions déjà autorisées sur ce foyer ;
- créer, révoquer ou renouveler ses liens calendrier ;
- copier son catalogue vers un autre foyer ;
- archiver un foyer pour le masquer sans perdre ses données ;
- supprimer définitivement un foyer et toutes ses données.

Le code administrateur ne doit pas être confondu avec un « super foyer ». Il
ouvre uniquement une session d'administration temporaire. L'interface doit
encore demander quel foyer consulter lorsqu'on revient aux menus.

Lors de la création, trois choix sont utiles :

1. partir d'un catalogue vide ;
2. partir d'un catalogue modèle livré par l'application ;
3. copier le catalogue et les réglages d'un foyer existant, sans copier ses
   semaines ni sa liste de courses.

La troisième option réduirait beaucoup la saisie initiale tout en permettant à
chaque foyer de faire ensuite évoluer ses goûts séparément.

## Modèle d'accès sans comptes personnels

L'absence de comptes individuels est compatible avec ce besoin. Il faut
cependant distinguer trois notions :

### Le foyer

Un foyer possède au minimum un nom, un identifiant stable, un slug d'URL, un
code d'accès haché, un état actif/archivé et ses dates de création et de mise à
jour.

```text
Household
  id
  name
  slug
  accessCodeHash
  status
  createdAt
  updatedAt
```

Le code doit être haché avec un algorithme prévu pour les mots de passe. Il ne
doit jamais être stocké en clair ni placé directement dans le cookie.

### La session de navigateur

Après validation d'un code, le serveur crée une session opaque. Cette session
peut avoir plusieurs autorisations de foyer et mémoriser le foyer actif.

```text
BrowserSession
  id
  tokenHash
  activeHouseholdId
  expiresAt

HouseholdGrant
  sessionId
  householdId
  grantedAt
```

Le cookie ne contient qu'un jeton aléatoire. La base permet de révoquer une
session ou l'accès à un foyer sans changer les autres.

Conséquence assumée : l'application reconnaît un **appareil autorisé**, pas une
personne. Sur un nouveau téléphone, Louis devra déverrouiller de nouveau ses
trois foyers. Sans comptes individuels, il n'y aura ni attribution précise des
modifications, ni rôles par personne, ni récupération personnelle du code.

### Le code d'administration

Un secret distinct, configuré au déploiement, protège la gestion des foyers.
Idéalement, l'environnement contient son empreinte, par exemple
`HOUSEHOLD_ADMIN_PASSWORD_HASH`, plutôt que le code en clair. Il ne doit pas
servir à l'usage quotidien et ne donne pas automatiquement accès aux flux
calendrier.

Après saisie correcte, le serveur crée une session d'administration séparée,
opaque, `HttpOnly`, `Secure` en production et courte, par exemple 30 minutes.
Le code ne doit pas être renvoyé à chaque requête ni stocké dans le navigateur.

Toutes les routes de gestion (`create`, `update`, `archive`, `delete`, rotation
des accès) vérifient cette session côté serveur. Masquer les boutons dans
l'interface ne constitue pas une autorisation suffisante.

La suppression définitive mérite des protections supplémentaires :

1. proposer l'archivage comme action normale et réversible ;
2. afficher le nombre de semaines, recettes et éléments qui seront supprimés ;
3. demander de retaper le nom exact du foyer ;
4. refuser de supprimer le dernier foyer actif ;
5. recommander ou proposer un export juste avant la suppression ;
6. invalider ses sessions et ses flux calendrier après suppression.

Changer le code administrateur n'a aucun effet sur les codes des foyers. À
l'inverse, changer le code d'un foyer peut révoquer ses sessions existantes sans
affecter les autres foyers ni la session d'administration.

### Modification du code d'accès d'un foyer

Depuis `/administration/foyers`, l'administrateur choisit le foyer concerné,
par exemple `Tanchou`, puis saisit et confirme son nouveau code. L'opération ne
modifie que ce foyer : les codes de `Couple` et `Béthisie` restent valides.

Le comportement recommandé est le suivant :

- le nouveau code remplace immédiatement l'ancien et est stocké uniquement sous
  forme hachée ;
- l'ancien code ne permet plus de déverrouiller le foyer ;
- l'administrateur choisit s'il conserve les appareils déjà autorisés ou s'il
  les déconnecte tous ;
- en cas de doute sur une fuite du code, l'option « déconnecter tous les
  appareils » est cochée par défaut ;
- les abonnements calendrier restent valides, car leurs jetons sont indépendants
  du code d'accès du foyer ; ils peuvent être révoqués séparément ;
- l'opération demande une nouvelle validation de la session administrateur afin
  qu'une personne trouvant brièvement un appareil déverrouillé ne puisse pas
  changer les accès familiaux.

Il n'est pas nécessaire de connaître l'ancien code du foyer : l'autorisation
vient du code administrateur. L'interface ne doit jamais afficher le code
actuel, puisqu'il n'est pas conservé en clair.

## Isolation des données

Pour la première version, chaque foyer devrait avoir son propre catalogue. Les
goûts, portions, recettes actives, incompatibilités et habitudes de convives
peuvent différer entre les trois lieux. Un catalogue global partagé rendrait
une modification dans `Couple` visible dans les deux familles, ce qui serait
surprenant.

Les modèles racines à rattacher directement à `Household` sont :

| Modèle | Nouvelle règle d'appartenance |
|---|---|
| `WeeklyPlan` | une semaine appartient à un foyer |
| `Ingredient` | un ingrédient et ses préférences appartiennent à un foyer |
| `Recipe` | une recette appartient à un foyer |
| `Aisle` | un rayon appartient à un foyer |
| `Incompatibility` | une règle appartient à un foyer |
| `AppSettings` | exactement une ligne de réglages par foyer |

`MealSlot`, `RecipeIngredient` et `ShoppingListEntry` héritent naturellement du
foyer de leur parent. Pour les opérations reçues avec un simple identifiant
(`slotId`, `entryId`, etc.), le service doit néanmoins vérifier l'appartenance
en rejoignant le parent. Connaître ou deviner l'identifiant d'un objet d'un
autre foyer ne doit jamais suffire à le lire ou à le modifier.

Les contraintes globales deviennent des contraintes composées :

```text
WeeklyPlan: unique(householdId, startDate)
Ingredient: unique(householdId, name)
Recipe:     unique(householdId, name)
Aisle:      unique(householdId, name)
Settings:   unique(householdId)
```

La règle importante dans le code serait : **aucun appel métier ne travaille
sans `householdId` explicite**. Exemple :

```ts
ensureWeeklyPlan(householdId, startDate)
listRecipes(householdId)
generatePersistedWeek(householdId, startDate, seed, version)
```

Le contexte de foyer est résolu côté serveur à partir de l'URL et de la
session. Il ne faut jamais faire confiance à un `householdId` envoyé seul par
le navigateur.

## Calendrier

Le calendrier demande une attention particulière, car il est volontairement
accessible sans le cookie de connexion afin qu'Apple Calendrier, Google Agenda
ou Outlook puissent le télécharger en arrière-plan.

### Proposition

Chaque foyer possède un ou plusieurs abonnements indépendants :

```text
CalendarFeed
  id
  householdId
  tokenHash
  label
  createdAt
  lastUsedAt
  revokedAt
```

Exemple d'URL :

```text
https://menus.tanchou.fr/api/calendars/<feed-id>.ics?token=<secret>
```

Le flux doit :

- ne charger que les semaines confirmées du foyer associé ;
- s'appeler `Menus — Couple`, `Menus — Tanchou`, etc. ;
- inclure le foyer dans l'UID de chaque événement pour éviter les collisions ;
- conserver une URL stable lors d'un changement de code d'accès au foyer ;
- pouvoir être révoqué et recréé sans changer le code du foyer ;
- rester en lecture seule, comme aujourd'hui.

Chaque foyer affiche son propre bouton Apple Calendrier et sa propre URL à
copier. Il est possible de s'abonner aux trois calendriers et de leur attribuer
des couleurs différentes dans l'application d'agenda.

### Compatibilité avec l'abonnement actuel

Pendant la migration, l'ancienne route `/api/calendar?token=...` devrait
continuer à servir uniquement le foyer créé à partir des données historiques.
Cette compatibilité temporaire évite de casser immédiatement les abonnements
déjà installés. Les nouveaux foyers utilisent exclusivement les nouvelles URL.

Une redirection HTTP vers la nouvelle URL est moins sûre : tous les clients de
calendrier ne mémorisent pas ou ne suivent pas les redirections de la même
manière. Servir l'ancien flux directement pendant une période de transition est
plus robuste.

## Export, import et sauvegarde

Les exports doivent devenir explicites :

- export d'un foyer depuis son écran « Plus » ;
- nom de fichier incluant le slug du foyer ;
- identifiant et nom du foyer dans le format exporté ;
- aucune donnée d'un autre foyer ;
- aucun code, cookie ou jeton calendrier exporté.

L'import d'enrichissement s'applique uniquement au foyer actif. Une fonction
réservée à l'administrateur peut copier un catalogue entre deux foyers, mais
elle ne doit pas être confondue avec l'import quotidien.

Le format d'export devrait passer à une version 2. La lecture de la version 1
peut rester disponible en considérant qu'elle cible le foyer actif.

## Migration progressive recommandée

### Phase 0 — Décisions produit

Valider le vocabulaire, l'isolation du catalogue, les règles de création et la
durée des sessions. Cette phase ne modifie aucune donnée.

### Phase 1 — Préparer le schéma sans casser l'existant

1. Ajouter `Household`, les sessions et les flux calendrier.
2. Créer automatiquement un foyer initial, par exemple `Tanchou`.
3. Ajouter des `householdId` temporairement facultatifs aux modèles racines.
4. Rattacher toutes les données existantes au foyer initial.
5. Remplacer les contraintes uniques globales par les contraintes composées.
6. Rendre ensuite les appartenances obligatoires.

Cette migration doit préserver toutes les semaines, les instantanés, les listes
de courses et le catalogue existants.

### Phase 2 — Introduire un contexte de foyer côté serveur

Créer une seule fonction centrale qui :

1. lit le slug de l'URL ;
2. charge la session ;
3. vérifie l'autorisation du foyer ;
4. retourne un `HouseholdContext` aux pages, routes et services.

Tous les services doivent ensuite recevoir ce contexte. Les mises à jour par
identifiant sont les plus sensibles : repas, favoris, courses, recettes,
ingrédients, incompatibilités et restauration d'un choix.

### Phase 3 — Adapter l'interface

Ajouter la page de sélection, le sélecteur permanent, les URL préfixées, la
page protégée par le code administrateur, les garde-fous de suppression et la
création par copie de catalogue.

### Phase 4 — Séparer les calendriers et les exports

Créer les flux par foyer, maintenir temporairement le flux historique, puis
versionner l'export.

### Phase 5 — Vérification avant ouverture de nouveaux foyers

Créer deux foyers de test avec la même semaine et des recettes portant le même
nom. Vérifier systématiquement qu'aucune action dans l'un ne modifie ou ne
révèle l'autre.

## Tests indispensables

Les tests d'isolation sont plus importants que les tests du sélecteur visuel :

- deux foyers peuvent avoir une semaine commençant le même lundi ;
- deux foyers peuvent avoir une recette et un ingrédient de même nom ;
- la génération n'utilise que le catalogue, les incompatibilités et
  l'historique du foyer actif ;
- une mise à jour avec un `slotId` d'un autre foyer est refusée ;
- une liste de courses ne peut pas être lue ou modifiée depuis un autre foyer ;
- les réglages de convives ne mettent à jour que les brouillons du bon foyer ;
- export et import restent limités au foyer actif ;
- un jeton calendrier ne retourne que son foyer ;
- un jeton révoqué est refusé ;
- un code de foyer ne permet pas d'appeler les routes d'administration ;
- une session administrateur expirée ne peut plus créer, modifier ou supprimer ;
- modifier le code d'un foyer invalide l'ancien sans changer celui des autres ;
- la révocation optionnelle des appareils ne concerne que le foyer modifié ;
- l'archivage conserve les données et la suppression les efface uniquement
  après la confirmation renforcée ;
- le dernier foyer actif ne peut pas être supprimé ;
- deux onglets ouverts sur deux URL de foyer différentes restent indépendants ;
- la migration rattache 100 % des anciennes lignes au foyer initial.

Un test d'intégration devrait également parcourir toutes les routes API avec un
identifiant valide appartenant volontairement à un autre foyer. C'est le moyen
le plus efficace de repérer une requête Prisma oubliée.

## Répartition indicative du chantier

| Bloc | Difficulté | Ordre de grandeur |
|---|---:|---:|
| Schéma et migration des données actuelles | élevée | 2–3 jours |
| Sessions partagées et autorisations par foyer | élevée | 2–3 jours |
| Filtrage de tous les services et routes | élevée mais répétitive | 2–4 jours |
| Sélecteur et navigation multi-foyers | moyenne | 1–2 jours |
| Calendriers, export et import | moyenne | 1–2 jours |
| Tests d'isolation et finition | élevée | 2–3 jours |

Ces blocs se recouvrent ; leur somme brute n'est donc pas l'estimation finale.
Le risque principal n'est pas la quantité d'interface, mais une requête oubliée
qui ferait apparaître ou modifier les données du mauvais foyer.

## Ce qui peut rester inchangé

- le moteur pur de génération et ses algorithmes ;
- la représentation des quatorze créneaux ;
- les instantanés de repas ;
- le calcul d'une liste de courses à partir d'une semaine ;
- les contrôles de concurrence par version ;
- le principe de confirmation et d'historique ;
- l'abonnement calendrier en lecture seule ;
- le déploiement unique Next.js + PostgreSQL sur Coolify.

Il n'est donc pas nécessaire de déployer trois applications ou trois bases de
données. Une seule application et une seule base, correctement partitionnée par
`householdId`, suffisent pour cette échelle familiale.

## Décisions à prendre avant une implémentation

Les recommandations sont indiquées entre parenthèses.

1. Les recettes et ingrédients doivent-ils diverger par foyer ?
   (**Oui**, avec une fonction de copie initiale.)
2. Les noms des foyers sont-ils visibles avant la saisie d'un code ?
   (**Oui** sur cette installation familiale privée ; sinon masquer les foyers
   non déverrouillés.)
3. Qui peut créer, renommer ou archiver un foyer ?
   (**Seulement le détenteur du code administrateur de l'installation.**)
4. Combien de temps un appareil reste-t-il autorisé ?
   (**90 jours glissants**, avec bouton pour verrouiller un foyer.)
5. Un foyer a-t-il un seul code partagé ou plusieurs liens d'invitation ?
   (**Un code partagé pour la première version**, puis invitations révocables si
   le besoin apparaît.)
6. Faut-il un agenda par foyer uniquement, ou aussi un flux agrégé personnel
   réunissant plusieurs foyers ?
   (**Un agenda par foyer d'abord.** Un flux agrégé suppose de représenter la
   sélection personnelle de Louis sans compte utilisateur.)
7. Quel nom donner au foyer historique créé pendant la migration ?
   (**À choisir explicitement avant le déploiement.**)

## Périmètre conseillé pour une première version

Inclure : foyers isolés, codes partagés, code et session administrateur,
création/modification/archivage/suppression protégées, plusieurs foyers mémorisés
par navigateur, sélecteur, catalogue copiable, calendrier par foyer, export par
foyer et migration des données existantes.

Reporter : comptes personnels, rôles fins, journal « qui a modifié quoi », flux
calendrier personnel agrégé, partage d'une recette vivante entre plusieurs
foyers et synchronisation bidirectionnelle avec un agenda.

Cette limite garde le produit fidèle à son usage familial simple tout en posant
des fondations qui n'obligeraient pas à tout recommencer plus tard.
