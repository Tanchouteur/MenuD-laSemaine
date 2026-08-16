# Menu de la semaine

Application web familiale mobile-first pour générer quatorze repas variés, les adapter simplement et obtenir automatiquement la liste de courses.

## Fonctions disponibles

- génération déterministe avec saisons, moments, historique, variété et incompatibilités ;
- remplacement par trois idées expliquées, verrouillage et régénération partielle ;
- repas libres, restes, repas à l’extérieur et nombre de convives par repas ;
- confirmation, historique immuable, favoris et réapplication d’une semaine ;
- liste de courses persistante, regroupée par rayon et ajustée aux portions ;
- création, modification et archivage des ingrédients et recettes ;
- réglages du foyer et associations d’aliments à éviter ;
- export iCalendar et protection facultative par mot de passe familial ;
- import idempotent de l’ancienne base MariaDB, avec dry-run ;
- interface française accessible et pensée d’abord pour le téléphone.

Le cahier d’architecture et les règles métier se trouvent dans [implementation_plan_menu.md](docs/implementation_plan_menu.md).

Le déploiement de production pas à pas est documenté dans [docs/DEPLOIEMENT_COOLIFY.md](docs/DEPLOIEMENT_COOLIFY.md).

## Démarrage local

Prérequis : Node.js 24, npm et Docker Desktop.

```bash
cp .env.example .env
docker compose up -d postgres
npm install
npm run db:deploy
npm run db:seed
npm run dev
```

Ouvrir <http://localhost:3000>. Le seed peut être relancé sans créer de doublons.

## Déploiement Docker complet

Renseigner au minimum `FAMILY_PASSWORD` et une longue valeur aléatoire `AUTH_SECRET` dans `.env`, puis :

```bash
docker compose up -d --build
docker compose exec app npm run db:seed
```

Le conteneur applicatif applique les migrations avant de démarrer. Le service `backup` produit chaque jour une sauvegarde PostgreSQL au format custom dans le volume `menu_postgres_backups` et conserve sept jours. L’endpoint `/api/health` contrôle l’accès à PostgreSQL.

## Import de l’ancien projet

Configurer `LEGACY_DATABASE_URL` dans `.env`. L’import commence toujours par un rapport sans écriture :

```bash
npm run db:import-legacy
```

Après vérification du rapport :

```bash
npm run db:import-legacy -- --write
```

L’import est idempotent. `LEGACY_MENU_WEEK`, s’il contient un lundi au format `AAAA-MM-JJ`, recopie aussi l’ancien menu courant comme choix libres dans un brouillon. Les portions, rayons et ingrédients des anciens plats complets restent à compléter dans l’interface.

## Qualité

```bash
npm run check
npm run build
npm run db:validate
```

`npm run check` exécute ESLint, TypeScript strict et les tests Vitest.

## Structure

```text
prisma/          schéma, migrations et données de départ
scripts/         import contrôlé de MariaDB
src/app/         pages et API Next.js
src/components/  interface mobile-first
src/engine/      moteur métier pur et tests
src/services/    transactions et accès métier persistants
```

Les semaines confirmées seules influencent l’historique. Les repas sauvegardent un instantané afin qu’une modification ultérieure d’une recette ne change jamais une ancienne semaine ou sa liste de courses.
