# MCP pour l’assistant personnel

Le serveur expose le catalogue et l’import de semaines par **Streamable HTTP**,
avec le SDK TypeScript officiel `@modelcontextprotocol/server` 2.4.0.
Il reçoit des données structurées, pas des photos. L’assistant est responsable de
la lecture de la photo WhatsApp, de la date de semaine et des clarifications.
Aucun fournisseur IA, compte WhatsApp ou modèle n’est intégré au serveur.

- [Outils, contrats et exemples synthétiques](MCP-CONTRACTS.md)
- [Permissions, idempotence, erreurs et révocation](MCP-SECURITY.md)
- [Résultats et limites de qualification](MCP-QUALIFICATION.md)
- [Schémas JSON d’entrée et de sortie](MCP.schemas.json), générés par `npm run mcp:schemas`.

## Connexion

| Paramètre | Valeur |
| --- | --- |
| URL déployée | `https://menus.tanchou.fr/api/mcp` |
| Transport | Streamable HTTP, réponses JSON, sans session persistante ni SSE GET |
| Méthode | `POST` pour initialize, notifications et appels JSON-RPC MCP |
| Authentification | `Authorization: Bearer <jeton-machine>` |
| Content-Type | `application/json` |
| Accept | `application/json, text/event-stream` |
| Version | négociée par le SDK à initialize ; qualification sur `2025-11-25` |
| Cache | `no-store` |
| Corps maximal | 256 Kio |

Le client doit permettre un en-tête Bearer configuré explicitement.
Cloudflare a refusé la signature par défaut `Python-urllib` (1010) lors de la
qualification ; un User-Agent explicite et descriptif `MenuMCP-Qualification/1.0`
a permis les appels réels. Configurer un identifiant propre au client, sans
imiter un navigateur ni désactiver les protections globales. Un 403 Cloudflare
ne doit pas être confondu avec une erreur d’authentification MCP. Les clients
qui imposent une découverte OAuth interactive nécessiteront un adaptateur OAuth
ultérieur ; aucun faux service OAuth n’est annoncé. Ne pas placer le jeton dans
une URL, une photo, un prompt ou un journal.

Exemple de configuration **indicative** pour un client acceptant ces clés :

```json
{
  "mcpServers": {
    "menus": {
      "url": "https://menus.tanchou.fr/api/mcp",
      "headers": { "Authorization": "Bearer ${MENU_MCP_TOKEN}" }
    }
  }
}
```

La substitution de variable dépend du client ; configurer son coffre de secrets.
Le serveur ne lit pas `MENU_MCP_TOKEN` : il vérifie l’empreinte du Bearer en base.
Un cookie familial ne donne aucun droit MCP. Inversement, le jeton MCP ne donne
pas accès aux autres routes de l’application.

## État déployé et administration

Le serveur est **déployé et qualifié en lecture/aperçu depuis le 7 novembre
2026**, après fusion de la [PR nº 6](https://github.com/Tanchouteur/MenuD-laSemaine/pull/6).
Les 108 tests ont été relancés avant déploiement. L’accès machine lecture seule
est préparé dans le stockage opérateur privé ; aucune valeur de jeton n’est
publiée. `MCP_ALLOW_WRITES=false` demeure actif. Le futur assistant n’est pas
encore raccordé et aucun menu familial n’a été modifié pour tester.

La qualification publique confirme initialize, les neuf outils, les lectures,
l’aperçu sans création, les refus d’écriture et la révocation. Les neuf tables
métier ont des empreintes identiques avant/après. Voir le
[rapport de qualification](MCP-QUALIFICATION.md).

Pour reproduire ou administrer cette configuration :

1. Appliquer les migrations normalement (`npm run db:deploy`, déjà exécuté au
   démarrage de l’image). Aucun menu n’est modifié par la migration MCP.
2. Configurer `MCP_PUBLIC_URL=https://menus.tanchou.fr/api/mcp` et conserver
   `MCP_ALLOW_WRITES=false` pendant la qualification réelle.
3. Dans l’environnement opérateur disposant de la base voulue, créer un accès
   lecture seule :

   ```sh
   npm run mcp:credential -- create read /chemin/prive/menu-mcp-token
   ```

   Le répertoire doit être privé et accessible à l’utilisateur applicatif.
   Le fichier doit être inexistant ; le script l’écrit en mode `0600` et refuse
   l’écrasement. Seul l’identifiant de révocation est affiché, jamais le jeton.
   L’image embarque ce script ; utiliser un chemin privé monté/persistant si
   l’on exécute la commande dans un conteneur. Transmettre le fichier au coffre
   de l’assistant par un canal privé, sans copier sa valeur dans une conversation.
4. Vérifier initialize, tools/list, les lectures et les aperçus sans application.
   Si un reverse proxy est utilisé, préserver le `Host` public configuré. Ne pas
   ajouter d’origines arbitraires : une origine fournie doit être exactement
   celle de `MCP_PUBLIC_URL`. Sans configuration valide, l’endpoint reste fermé.
5. Après validation explicite de l’intégration, créer un accès `read,write` et
   activer `MCP_ALLOW_WRITES=true`. Révoquer les jetons remplacés.

Aucune suppression, édition générale du catalogue,
modification de droits ou commande d’administration n’est exposée en outil MCP.
Par défaut, un import laisse la semaine en brouillon. Depuis le 7 novembre 2026, `import.confirmWeek=true`, présent dès l’aperçu, confirme la semaine complète dans la même transaction que l’import et son reçu. Aucun outil de confirmation isolée ni de déconfirmation n’est ajouté.

## Qualification reproductible

`npm run check:full` couvre lint, TypeScript, tests unitaires avec couverture,
tests PostgreSQL 17, validation Prisma et build Next.js. Le lanceur refuse les
bases d’intégration dont le nom ne contient pas `test` ou dont l’hôte n’est pas
`localhost`/`127.0.0.1`. Ne jamais utiliser une copie de menus familiaux.

Sur le runner Happier, lancer PostgreSQL et le conteneur Node de vérification
avec `task-compose`, un nom de projet unique et un volume dédié. Le conteneur
Node peut partager le réseau du service PostgreSQL pour que `TEST_DATABASE_URL`
utilise `127.0.0.1:5432`. Monter le worktree au même chemin absolu. Nettoyer
uniquement ce projet avec `task-compose ... down --volumes --remove-orphans`.

La suite `src/mcp/mcp.integration.test.ts` exerce les vraies transactions et le
transport SDK : authentification, droits, révocation, mode lecture, validation,
création de recette, correspondances, concurrence, réessai de la mutation depuis
un autre processus, versions, protection des semaines, deux verrous, aperçu
périmé et rollback sur une contrainte SQL injectée après un premier repas écrit.
Voir aussi [le protocole général de tests](../TEST.md).

## Raccordement restant

L’assistant devra stocker durablement ses `operationKey`, appeler les outils
avec des références de catalogue, montrer les ambiguïtés et demander les
informations absentes. Il devra convertir les dates locales en lundi ISO,
présenter l’aperçu puis appliquer les mêmes données, et utiliser `get_operation`
après une réponse perdue. Le branchement WhatsApp et l’analyse d’image sont
extérieurs à ce dépôt. L’accès réel restera en lecture/simulation jusqu’à la fin
de sa qualification.

Référence SDK : [dépôt officiel](https://github.com/modelcontextprotocol/typescript-sdk).
Les API ont été vérifiées dans les déclarations du paquet épinglé ; les routes
suivent la documentation Next.js livrée avec la version du dépôt.
