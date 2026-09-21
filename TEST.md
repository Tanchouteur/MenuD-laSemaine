# Tests et prévention des régressions

Ce document est la référence unique pour tester **Menu de la semaine**. Il décrit
ce qui est automatisé, ce qui dépend de PostgreSQL et les contrôles à effectuer
avant un déploiement Coolify.

## Réalité de production reproduite

Le déploiement documenté dans Nexus et dans `docs/DEPLOIEMENT_COOLIFY.md` utilise :

- une application Coolify `menu-app`, construite avec le `Dockerfile` ;
- une ressource séparée `menu-postgres` sous PostgreSQL 17 ;
- une URL PostgreSQL interne, jamais un PostgreSQL embarqué dans l’application ;
- `prisma migrate deploy` avant le démarrage de Next.js ;
- `/api/health` comme contrôle réel de la connexion à PostgreSQL ;
- une base persistante qui ne doit jamais être recréée pendant un déploiement.

La suite d’intégration reproduit ce contrat : elle lance `postgres:17-alpine`,
applique les vraies migrations avec `prisma migrate deploy`, exécute les tests,
puis supprime le conteneur et son stockage éphémère.

Elle ne contacte jamais Coolify et ne modifie jamais `menu-postgres`.

## Commandes

### Boucle rapide

```bash
npm test
```

Exécute les tests unitaires et les tests de frontières HTTP sans Docker.

```bash
npm run test:coverage
```

Ajoute la couverture V8 et échoue sous les seuils suivants :

| Mesure | Seuil minimal |
|---|---:|
| Lignes | 85 % |
| Instructions | 85 % |
| Fonctions | 85 % |
| Branches | 75 % |

Le rapport HTML est généré dans `coverage/` et n’est pas versionné.

### Intégration PostgreSQL 17

Docker doit être démarré :

```bash
npm run test:integration
```

Par défaut, le script crée un conteneur PostgreSQL jetable sur un port local
aléatoire. Il peut utiliser une base déjà disponible avec :

```bash
TEST_DATABASE_URL='postgresql://...@127.0.0.1:5432/menu_test?schema=public' \
  npm run test:integration
```

Le garde-fou refuse l’exécution si :

- l’hôte n’est ni `localhost` ni `127.0.0.1` ;
- le nom de la base ne contient pas `test`.

Il est donc volontairement impossible de lancer cette suite sur l’URL interne
Coolify ou sur une base distante.

### Validation complète avant livraison

```bash
npm run check:full
```

Cette commande enchaîne :

1. ESLint ;
2. TypeScript strict ;
3. tests unitaires avec couverture ;
4. tests d’intégration PostgreSQL 17 ;
5. validation du schéma Prisma ;
6. build Next.js de production.

`npm run check` reste la boucle locale rapide sans Docker.

## Périmètre automatisé

### Tests unitaires

- saisons, moments de repas et calcul des semaines selon le fuseau ;
- refroidissement des repas déjà consommés ;
- incompatibilités, répétitions, variété et contraintes dures ;
- tirage pondéré, reproductibilité par graine et alternatives distinctes ;
- génération complète, catalogue insuffisant et créneaux verrouillés ;
- validation des ingrédients, recettes, réglages, dates et convives ;
- parsing et conversion des instantanés de repas ;
- erreurs HTTP et lecture de JSON ;
- jeton familial, connexion, déconnexion et protection par proxy.

### Tests d’intégration sur les vraies migrations

- application de toutes les migrations sur PostgreSQL 17 ;
- healthcheck SQL et réponses JSON des routes principales ;
- création idempotente d’une semaine et de ses quatorze créneaux ;
- propagation des valeurs par défaut semaine/week-end ;
- confirmation, réouverture et interdiction de modifier une semaine confirmée ;
- verrouillage optimiste et conflit entre deux écritures concurrentes ;
- validation et persistance du catalogue ;
- normalisation des incompatibilités ;
- sélection des ingrédients autorisés dans les assiettes automatiques ;
- prévisualisation et application idempotente d’un enrichissement JSON ;
- génération et persistance d’une semaine complète ;
- création des instantanés et recalcul de la liste de courses ;
- agrégation par ingrédient/unité et nombre de convives ;
- conservation des coches et des ajouts manuels lors d’un recalcul ;
- restauration exacte d’un état de créneau ;
- contraintes SQL transversales du modèle Prisma.

## Organisation

| Fichier | Responsabilité |
|---|---|
| `src/engine/engine.test.ts` | moteur déterministe et règles de sélection |
| `src/lib/core.test.ts` | validation, HTTP, auth et semaines |
| `src/domain/meal-snapshot.test.ts` | contrat des instantanés persistés |
| `src/app/api/auth/auth.test.ts` | routes et proxy d’authentification |
| `src/app/api/calendar/calendar.test.ts` | protection et format iCalendar |
| `src/services/services.integration.test.ts` | services, routes et PostgreSQL réel |
| `scripts/run-integration-tests.mjs` | cycle de vie sécurisé du PostgreSQL jetable |

Les fichiers `*.integration.test.ts` sont exclus de `npm test` et ne sont
chargés que par `vitest.integration.config.ts`.

## Règles pour ajouter un test

1. Tester le comportement observable, pas l’implémentation interne.
2. Préférer un test unitaire pour une règle pure et un test PostgreSQL pour une
   transaction, une contrainte, une migration ou une concurrence.
3. Donner aux scénarios des données explicites et indépendantes de l’heure du
   poste ; utiliser des dates ISO fixes et une graine fixe.
4. Nettoyer la base entre les tests. La suite d’intégration s’exécute sans
   parallélisme afin de garder cette isolation déterministe.
5. Toute correction de bug doit ajouter un test qui échoue avant la correction.
6. Toute migration doit être validée depuis une base vide par la suite
   d’intégration et, avant production, sur une restauration récente si elle
   transforme des données existantes.
7. Ne jamais remplacer les tests PostgreSQL par des mocks pour les opérations
   de version, les transactions ou les listes de courses.

## Contrôle avant déploiement Coolify

Avant un déploiement qui modifie le schéma ou les données :

1. exécuter `npm run check:full` ;
2. déclencher et vérifier une sauvegarde de `menu-postgres` dans Coolify ;
3. ne jamais exécuter `prisma migrate dev` en production ;
4. suivre le journal de `prisma migrate deploy` au démarrage ;
5. vérifier `/api/health` ;
6. vérifier une ancienne semaine, une nouvelle génération et la liste de
   courses ;
7. ne jamais supprimer ni recréer la ressource PostgreSQL pour mettre à jour
   l’application.

## Parcours encore manuels

Les tests actuels couvrent fortement le domaine, les transactions et les
frontières HTTP, mais ils ne remplacent pas encore un navigateur réel. Avant une
version affectant l’interface, vérifier au minimum :

1. connexion correcte et incorrecte sur mobile ;
2. assistant de sélection des ingrédients composables ;
3. génération, alternatives, choix manuel et annulation ;
4. conflit visible entre deux onglets ;
5. confirmation et persistance des coches de courses ;
6. mise en page mobile, clavier et libellés longs ;
7. abonnement calendrier.

Une future suite Playwright pourra automatiser ces parcours. Elle devra démarrer
l’application et PostgreSQL de manière isolée, sans viser le domaine de
production.
