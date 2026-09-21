# Décisions d’architecture

Ce journal décrit les décisions encore actives. Une décision remplacée reste
dans le fichier avec son nouveau statut afin de préserver le raisonnement.

## ADR-001 — Next.js et PostgreSQL

**Statut : accepté.** La V2 utilise Next.js/TypeScript et PostgreSQL avec Prisma.
Ce choix rassemble interface et API, permet des transactions explicites et
remplace l’ancien moteur Java difficile à tester.

## ADR-002 — Moteur fonctionnel et hasard déterministe

**Statut : accepté.** Le moteur de génération ne dépend pas de Prisma et reçoit
une graine. Une génération peut ainsi être reproduite en test et les accès aux
données restent dans les services.

## ADR-003 — Instantanés immuables des repas

**Statut : accepté.** Un créneau conserve le nom et les ingrédients du repas au
moment de son choix. Modifier ou archiver ensuite une recette ne change donc ni
l’historique ni les anciennes courses.

## ADR-004 — Historique fondé sur les semaines confirmées

**Statut : accepté.** Les brouillons ne représentent pas une consommation.
Seules les semaines confirmées influencent le délai avant une nouvelle
proposition.

## ADR-005 — Opt-in pour les assiettes automatiques

**Statut : accepté le 21 septembre 2026.** Un ingrédient peut appartenir à une
recette sans devenir un repas potentiel. `useInComposedMeals` est désactivé par
défaut et choisi explicitement par la famille.

## ADR-006 — Préférence souple pour les féculents

**Statut : accepté le 21 septembre 2026.** Parmi les catégories disponibles,
les poids des assiettes sont 80 % protéine-féculent-légume, 10 %
protéine-féculent et 10 % protéine-légume. Ce sont des poids de tirage et non des
quotas hebdomadaires.

## ADR-007 — Le choix manuel est prioritaire

**Statut : accepté.** Une recette ou composition choisie par la famille est
conservée par défaut. Les règles automatiques produisent un avertissement mais
ne bloquent pas ce choix.

## ADR-008 — Export JSON avant import contrôlé

**Statut : accepté le 21 septembre 2026.** L’échange de catalogue passe par un
fichier JSON téléchargé depuis l’application. Il évite l’exposition de
PostgreSQL et la dépendance au terminal WebSocket de Coolify. L’import
d’enrichissement propose un aperçu avant écriture, vérifie toutes les références
et applique uniquement les ingrédients et recettes dans une transaction.

## ADR-009 — Déploiement progressif

**Statut : accepté.** Les migrations de production sont additives. Une
sauvegarde précède le déploiement. Le rollback applicatif conserve les colonnes
ajoutées lorsqu’une ancienne version peut les ignorer.
