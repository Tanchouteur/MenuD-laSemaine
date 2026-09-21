# Stratégie de tests

## Commandes de référence

```bash
npm run check
npm run db:validate
npm run build
docker build -t menu-de-la-semaine:check .
```

`npm run check` enchaîne ESLint, TypeScript strict et Vitest. Le build Next.js
vérifie aussi les frontières serveur/client et toutes les routes de production.

## Tests automatisés

Le dossier `src/engine` concentre les tests unitaires du moteur pur :

- saisons et moments de la semaine ;
- refroidissement des repas déjà consommés ;
- incompatibilités et variété ;
- tirage reproductible avec une graine ;
- génération complète et catalogue insuffisant ;
- préservation des repas verrouillés ;
- alternatives distinctes ;
- préférence souple pour les assiettes complètes.

Les services Prisma nécessitent une base PostgreSQL. Avant une migration, lancer
`prisma migrate deploy` sur une base temporaire ou une restauration récente et
vérifier que les anciennes données restent lisibles.

## Parcours manuels avant production

1. Connexion avec mot de passe correct et incorrect.
2. Sélection initiale des ingrédients composables.
3. Génération d’une semaine et affichage des avertissements.
4. Choix d’une recette, d’une composition et d’une idée libre.
5. Annulation exacte d’une modification et conflit entre deux onglets.
6. Confirmation, liste de courses et persistance des coches.
7. Création d’un ingrédient depuis une recette.
8. Export JSON et vérification de l’absence de secrets.
9. Contrôle mobile des cases, panneaux et libellés longs.

## Critères de livraison

Une modification est livrable quand les commandes de référence passent, la
migration a été testée lorsqu’elle existe, le parcours concerné a été vérifié et
les documents d’architecture ou de décision ont été mis à jour si le contrat du
système change.
