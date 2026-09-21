# Architecture technique

## Vue d’ensemble

```text
Navigateur
   │ HTTPS / JSON
   ▼
Next.js 16 (App Router, React 19)
   ├── pages et composants client
   ├── routes API
   ├── services métier transactionnels
   └── moteur de génération pur
            │ Prisma 7
            ▼
       PostgreSQL 17
```

L’application est compilée en mode Next.js standalone dans une image Docker.
En production, Coolify construit le `Dockerfile`, exécute les migrations Prisma
au démarrage puis lance `server.js`. PostgreSQL est une ressource Coolify
séparée et persistante.

## Responsabilités

- `src/app/` : pages serveur et frontières HTTP.
- `src/components/` : interactions et présentation mobile-first.
- `src/services/` : accès Prisma, transactions et règles persistantes.
- `src/engine/` : génération, filtrage, score et hasard déterministe sans base.
- `src/domain/` : validation des instantanés de repas.
- `src/lib/` : authentification, dates, validation et client Prisma.
- `prisma/` : schéma, migrations et données de démonstration locales.

Les composants n’accèdent jamais directement à PostgreSQL. Une route API
valide l’entrée puis appelle un service. Le moteur reçoit des objets simples et
reste testable sans infrastructure.

## Flux de génération

1. Le service charge recettes, ingrédients autorisés et historique confirmé.
2. Le moteur filtre par saison, moment, incompatibilité et repas déjà affectés.
3. Il choisit entre recette et assiette, puis pondère les assiettes complètes.
4. Le service sauvegarde un instantané du repas dans chaque créneau.
5. Le menu, sa version et la liste de courses sont mis à jour en transaction.

## Cohérence et concurrence

Chaque semaine porte une version entière. Les modifications d’un repas réclament
la version connue par le navigateur et incrémentent cette version dans la même
transaction. Un second appareil travaillant sur une version ancienne reçoit une
erreur et doit recharger la page.

## Authentification

Lorsque `FAMILY_PASSWORD` et `AUTH_SECRET` existent, le proxy exige un cookie
dérivé des deux valeurs. `/api/health`, l’authentification et le calendrier font
exception. L’export JSON suit la protection familiale normale et désactive le
cache HTTP.

L’import accepte uniquement le format versionné
`menu-de-la-semaine-enrichment`. Une prévisualisation valide les références et
annonce les créations et mises à jour. L’application s’exécute ensuite dans une
transaction PostgreSQL et ne modifie ni les semaines ni leurs instantanés.

## Observabilité

`/api/health` vérifie réellement PostgreSQL. Les journaux de démarrage indiquent
les migrations appliquées. Il n’existe actuellement ni métriques applicatives
personnalisées ni traçage distribué.
