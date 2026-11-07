# Qualification MCP — 7 novembre 2026

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

Endpoint réel non déployé par cette qualification. Restent la revue/merge,
le déploiement, la configuration de l’URL publique et d’un jeton lecture seule,
la validation réelle en lecture/aperçu, puis le raccordement WhatsApp et de
l’analyse d’image dans l’assistant. Les écritures restent désactivées par défaut.
Les clients imposant OAuth ont besoin d’un adaptateur ; les clients acceptant
un Bearer explicite peuvent utiliser directement ce transport MCP.
