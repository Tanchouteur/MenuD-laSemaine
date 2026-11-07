# Qualification MCP — 7 novembre 2026

## Qualification synthétique initiale

Qualification sur worktree dédié, avec PostgreSQL 17 et Node 24 dans le moteur
Docker TEST, via `task-compose -p menu-mcp-qualification`. Source montée au même
chemin absolu ; volume de base dédié ; aucune lecture ni écriture de base
familiale, aucun jeton réel créé, aucun déploiement de production.

Commande : `npm run check:full` dans le conteneur Node partageant le réseau
PostgreSQL. `TEST_DATABASE_URL` cible `127.0.0.1` et une base nommée `menu_mcp_test`.
Les identifiants de cette base sont purement synthétiques.

| Vérification | Résultat |
| --- | --- |
| ESLint | succès |
| TypeScript | succès |
| Tests unitaires | 66 réussis |
| Tests d’intégration PostgreSQL | 42 réussis, dont 21 MCP |
| Couverture configurée du dépôt | lignes 92,61 %, branches 86,63 % ; seuils respectés |
| Validation Prisma et migration | schéma valide ; migration MCP appliquée sur base synthétique |
| Build Next.js 16.4.5 | succès, `/api/mcp` inclus |
| Transport SDK | initialize, tools/list avec neuf schémas, tools/call et révocation réussis |
| Redémarrage | mutation rejouée dans un nouveau processus Node ; résultat identique, un seul ingrédient et un seul reçu |
| Atomicité | échec SQL après un premier repas écrit : aucune semaine neuve résiduelle ; semaine existante et sa version intégralement préservées |
| Identité opérateur | fichier de jeton 0600, valeur absente de stdout, révocation vérifiée |

La mesure de couverture est celle du périmètre déjà configuré (moteur, domaine,
bibliothèques), pas un pourcentage de couverture du MCP. Les garanties MCP sont
exercées dans la suite d’intégration PostgreSQL.

Le conteneur Node de qualification signale une détection OpenSSL imparfaite par
Prisma ; migrations et validation aboutissent. Cela ne constitue pas un test de
l’image Alpine finale ni du reverse proxy de production.

## Audit des dépendances

L’audit initial remontait 24 alertes, dont une critique. Next.js, son outillage
ESLint et Zod ont été mis à jour respectivement vers 16.4.5 et 4.6.7 dans la PR.
Après ces mises à jour, `npm audit --omit=dev` remonte encore **7 alertes hautes**
sur des dépendances préexistantes : Prisma/outillage et transitives, MariaDB,
`fast-uri` et `source-map-js`. L’audit complet remonte 21 alertes (10 hautes,
11 modérées), aucune critique. La PR ne prétend pas assainir l’ensemble des
dépendances historiques. Prévoir leur mise à jour séparément et ne pas confondre
la réussite des tests fonctionnels avec un audit de sécurité exhaustif.

## Limites et raccordement

La qualification initiale ci-dessus n’avait pas déployé le service. Louis a
ensuite demandé de relancer les tests et de déployer : les résultats réels
ci-dessous remplacent cet ancien état. Restent le raccordement de l’assistant,
WhatsApp et l’analyse d’image ; les écritures demeurent fermées.
Les clients imposant OAuth ont besoin d’un adaptateur ; les clients acceptant
un Bearer explicite peuvent utiliser directement ce transport MCP.


## Déploiement et qualification réelle — 7 novembre 2026

- PR nº 6 fusionnée ; commit fonctionnel `2a2d21b4911ea59a8f757e55c668441fa92b6dcb`.
- `npm run check:full` relancé : **108 tests réussis**, lint, TypeScript,
  validation Prisma et build Next.js 16.4.5 réussis.
- Déploiement Coolify `yetc35jpogfj3yyb7blhqbpe` terminé, nouveau conteneur sain,
  migration additive MCP appliquée au démarrage, ancien conteneur remplacé.
- URL réelle : `https://menus.tanchou.fr/api/mcp` ; URL publique configurée et
  `MCP_ALLOW_WRITES=false` vérifiés dans le conteneur actif.
- Sauvegarde PostgreSQL préalable au déploiement, archive non vide et catalogue
  vérifié par `pg_restore --list`. Une restauration complète n’a pas été testée.
- Site contrôlé par Playwright : page de connexion publique et ses ressources
  chargées, aucune erreur console après la fin du remplacement des conteneurs.
  Une première navigation pendant le remplacement avait rencontré un ancien
  chunk 404 ; ce défaut n’est pas reproduit après la bascule.
- API publique : sans jeton et avec cookie seul → **401** ; origine étrangère
  → **403** ; initialize → **200** ; tools/list → **neuf outils** ; recherche et
  consultation du catalogue réussies. Listage de semaines et consultation d’une
  semaine future absente vérifiés sans afficher de données familiales.
- Aperçu de 14 repas fictifs sur une semaine future : succès, puis semaine
  toujours absente. Aucune application d’import réelle effectuée.
- Création avec accès `read` → **FORBIDDEN**, sans reçu ni effet métier.
- Jeton temporaire de qualification révoqué : nouvel appel → **401**, fichier
  temporaire effacé. Accès permanent distinct limité à `read`, testé et conservé
  en fichier privé 0600 hors Git ; valeur jamais affichée.
- Empreintes privées des **neuf tables métier identiques avant/après** : semaines,
  créneaux, ingrédients, recettes, liens recette/ingrédients, courses,
  incompatibilités, rayons et réglages. Seules la migration et les identités
  machine ont ajouté leurs données techniques.

Cloudflare a rejeté le User-Agent Python par défaut avec le code 1010. Les
contrôles ont ensuite utilisé l’identifiant descriptif
`MenuMCP-Qualification/1.0`, sans modifier les règles Cloudflare. Ce point est
consigné dans la connexion client ; les protections globales restent en place.

Le parcours WhatsApp → analyse photo → application réelle reste à qualifier
avec le futur assistant avant activation des écritures. La disponibilité du MCP
ne prouve pas cette intégration.
