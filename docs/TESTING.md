# Stratégie de tests

La documentation de référence a été déplacée à la racine du dépôt :
[`../TEST.md`](../TEST.md).

Utiliser `npm run check` pour la boucle rapide et `npm run check:full` avant une
livraison. La validation complète démarre un PostgreSQL 17 éphémère, applique les
vraies migrations et n’accède jamais à la base Coolify.
