# Menu de la semaine

Application web familiale mobile-first pour générer quatorze repas variés, les adapter simplement et obtenir automatiquement la liste de courses.

## Fonctions disponibles

- génération déterministe avec saisons, moments, historique, variété et incompatibilités ;
- remplacement par trois idées expliquées, verrouillage et régénération partielle ;
- repas libres, restes, repas à l’extérieur et nombre de convives par repas ;
- confirmation, historique et correction manuelle d’une semaine confirmée, favoris et réapplication ;
- liste de courses visible seulement après confirmation, regroupée par rayon et ajustée aux portions ;
- création, modification et archivage des ingrédients et recettes ;
- entrées à fréquence réglable, recettes à accompagner et variantes liées par le refroidissement ;
- réglages du foyer et associations d’aliments à éviter ;
- export iCalendar et protection facultative par mot de passe familial ;
- export JSON du catalogue et des menus, puis import prévisualisé d’un enrichissement ciblé ;
- import idempotent de l’ancienne base MariaDB, avec dry-run ;
- interface française accessible et pensée d’abord pour le téléphone.

## Documentation

- [Produit et périmètre](docs/PRODUCT.md)
- [Architecture technique](docs/ARCHITECTURE.md)
- [Décisions d’architecture](docs/DECISIONS.md)
- [Modèle de données](docs/DATA_MODEL.md)
- [Exploitation et maintenance](docs/OPERATIONS.md)
- [Tests et prévention des régressions](TEST.md)
- [Déploiement Coolify](docs/DEPLOIEMENT_COOLIFY.md)
- [Plan d’implémentation V2 historique](docs/implementation_plan_menu.md)

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
npm run check:full
npm run build
npm run db:validate
```

`npm run check` exécute la boucle rapide. `npm run check:full` ajoute la
couverture, les tests d’intégration sur PostgreSQL 17, la validation Prisma et le
build de production. Voir [`TEST.md`](TEST.md) pour les garde-fous de base et le
périmètre détaillé.

## Structure

```text
prisma/          schéma, migrations et données de départ
scripts/         import contrôlé de MariaDB
src/app/         pages et API Next.js
src/components/  interface mobile-first
src/engine/      moteur métier pur et tests
src/services/    transactions et accès métier persistants
```

Les semaines confirmées seules influencent l’historique. Les repas sauvegardent un instantané du plat, de ses accompagnements et de l’entrée : modifier ensuite le catalogue ne change pas les anciens repas. Une correction manuelle explicite d’une semaine confirmée recalcule ses courses et son calendrier.
