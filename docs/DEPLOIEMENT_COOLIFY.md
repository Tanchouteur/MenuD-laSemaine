# Déployer Menu de la semaine sur Coolify

Ce guide décrit le déploiement recommandé : une base PostgreSQL gérée comme ressource Coolify et l’application construite avec le `Dockerfile` du dépôt. Cette séparation rend les sauvegardes, les restaurations et les mises à jour plus simples que le déploiement du `docker-compose.yml` local complet.

## 1. Ce qu’il faut préparer

Avant de commencer, vérifiez que vous avez :

- un serveur Coolify fonctionnel ;
- ce projet dans un dépôt GitHub, GitLab ou un autre dépôt Git accessible à Coolify ;
- un nom de domaine, par exemple `menus.mondomaine.fr` ;
- accès à la zone DNS de ce domaine ;
- un mot de passe familial que vous pourrez communiquer aux membres du foyer.

Le serveur doit laisser entrer le trafic HTTP et HTTPS. Le domaine doit avoir un enregistrement DNS `A` pointant vers l’adresse IPv4 du serveur. Ajoutez aussi un enregistrement `AAAA` seulement si le serveur est correctement configuré en IPv6.

Attendez que le DNS réponde avant de demander le certificat HTTPS. Selon le fournisseur DNS, cela peut prendre quelques minutes ou davantage.

## 2. Mettre le projet dans Git

Coolify construit l’application depuis un dépôt. Vérifiez localement que tout est enregistré :

```bash
git status
git add .
git commit -m "Refonte Menu de la semaine V2"
git push origin main
```

Si le dépôt est privé sur GitHub, la méthode recommandée est l’application GitHub de Coolify :

1. ouvrez **Sources** dans Coolify ;
2. cliquez sur **Add** puis créez une GitHub App ;
3. autorisez uniquement le dépôt de Menu de la semaine ;
4. revenez dans votre projet Coolify.

Un dépôt public peut être ajouté directement avec son URL. Un dépôt privé d’un autre fournisseur peut utiliser une deploy key SSH.

Documentation officielle : [intégration GitHub](https://coolify.io/docs/applications/ci-cd/github/overview) et [configuration de la GitHub App](https://coolify.io/docs/applications/ci-cd/github/setup-app).

## 3. Créer le projet Coolify

1. Dans Coolify, cliquez sur **Projects** puis **Add**.
2. Nommez le projet `Menu de la semaine`.
3. Ouvrez l’environnement `production`.
4. Utilisez le même serveur et la même destination Docker pour la base et l’application. Cela permet d’utiliser l’URL PostgreSQL interne sans exposer la base à Internet.

## 4. Créer PostgreSQL

Dans l’environnement `production` :

1. cliquez sur **New Resource** ;
2. choisissez **Database**, puis **PostgreSQL** ;
3. utilisez l’image PostgreSQL 17 proposée par Coolify ;
4. nommez la ressource `menu-postgres` ;
5. renseignez :
   - base : `menu_de_la_semaine` ;
   - utilisateur : `menu` ;
   - mot de passe : une longue valeur aléatoire ;
6. laissez **Publicly Accessible** désactivé ;
7. démarrez la base ;
8. copiez son **Internal URL**. Elle ressemble à :

```text
postgresql://menu:MOT_DE_PASSE@nom-interne:5432/menu_de_la_semaine
```

Ajoutez `?schema=public` à la fin si l’URL ne le contient pas déjà.

La base n’a pas besoin de domaine ni de port public. Coolify recommande l’URL interne lorsque l’application et PostgreSQL partagent le même réseau : [documentation des bases Coolify](https://coolify.io/docs/databases/).

## 5. Configurer les sauvegardes avant la première utilisation

Ouvrez la ressource PostgreSQL, puis la section **Backups** :

1. ajoutez une sauvegarde planifiée ;
2. utilisez la fréquence `0 2 * * *` pour une sauvegarde quotidienne à 2 h ;
3. indiquez `menu_de_la_semaine` dans les bases à sauvegarder ;
4. conservez au minimum sept sauvegardes locales ;
5. idéalement, ajoutez un stockage S3 compatible situé sur un autre serveur ;
6. activez la sauvegarde ;
7. lancez immédiatement **Backup now**.

Une sauvegarde située uniquement sur le même serveur ne protège pas d’une panne totale du serveur. Le stockage S3 externe est donc fortement recommandé.

Coolify utilise `pg_dump` au format custom pour PostgreSQL et documente aussi la restauration avec `pg_restore` : [sauvegardes Coolify](https://coolify.io/docs/databases/backups).

## 6. Créer l’application

Toujours dans l’environnement `production` :

1. cliquez sur **New Resource** ;
2. sélectionnez le dépôt public, la GitHub App ou la deploy key utilisée à l’étape 2 ;
3. choisissez le dépôt et la branche `main` ;
4. sélectionnez le build pack **Dockerfile** ;
5. utilisez `/` comme **Base Directory** ;
6. indiquez `/Dockerfile` comme chemin du Dockerfile si Coolify ne le détecte pas ;
7. configurez **Port Exposes** à `3000` ;
8. ne configurez aucun **Port Mapping** public ;
9. nommez la ressource `menu-app`.

Le dépôt contient déjà un Dockerfile multi-stage. Il compile Next.js en mode standalone, applique les migrations Prisma au démarrage et lance le serveur sur `0.0.0.0:3000`.

Documentation officielle : [build pack Dockerfile](https://next.coolify.io/docs/applications/build-packs/dockerfile) et [configuration des applications](https://coolify.io/docs/applications/index).

## 7. Ajouter les variables d’environnement

Dans **Environment Variables** de `menu-app`, ajoutez les variables suivantes comme variables d’exécution :

| Nom | Valeur |
|---|---|
| `DATABASE_URL` | L’Internal URL PostgreSQL de l’étape 4, terminée par `?schema=public` |
| `FAMILY_PASSWORD` | Le mot de passe partagé par la famille |
| `AUTH_SECRET` | Une valeur aléatoire longue et privée |
| `CALENDAR_TOKEN` | Une autre valeur aléatoire utilisée dans le lien calendrier |

Pour générer les deux secrets depuis macOS ou Linux :

```bash
openssl rand -hex 32
openssl rand -hex 32
```

Utilisez une sortie différente pour `AUTH_SECRET` et `CALENDAR_TOKEN`. Ne placez jamais ces valeurs dans Git, dans une capture d’écran ou dans un message public.

Points importants :

- `DATABASE_URL` doit utiliser l’hôte interne fourni par Coolify, pas `localhost` ;
- le mot de passe PostgreSQL présent dans l’URL doit être encodé si vous avez utilisé des caractères spéciaux réservés dans une URL ;
- l’application fonctionne sans `FAMILY_PASSWORD`, mais elle serait alors accessible à toute personne connaissant le domaine ;
- après la modification d’une variable, sauvegardez puis redéployez l’application.

Documentation officielle : [variables d’environnement Coolify](https://coolify.io/docs/knowledge-base/environment-variables).

## 8. Configurer le domaine et HTTPS

Dans les réglages généraux de `menu-app`, renseignez le domaine avec le port interne de l’application :

```text
https://menus.mondomaine.fr:3000
```

Le `:3000` indique à Coolify le port du conteneur. Les utilisateurs ouvriront normalement `https://menus.mondomaine.fr` sans saisir ce port : le proxy Coolify termine HTTPS et transmet la requête au port 3000 du conteneur.

Sauvegardez. Coolify configurera son proxy et demandera automatiquement le certificat TLS si le DNS pointe correctement vers le serveur.

Documentation officielle : [domaines et HTTPS](https://coolify.io/docs/knowledge-base/domains).

## 9. Configurer le contrôle de santé

Le Dockerfile contient déjà un contrôle de santé sur `/api/health`. Dans Coolify, vérifiez néanmoins :

- health checks activés ;
- chemin : `/api/health` ;
- code attendu : `200` ;
- port : `3000` ;
- délai initial d’au moins 30 secondes ;
- intervalle de 15 secondes ;
- cinq tentatives avant de déclarer l’application indisponible.

L’endpoint vérifie réellement l’accès à PostgreSQL. Une application qui démarre mais ne peut pas joindre la base restera donc non saine au lieu d’afficher des erreurs aux utilisateurs.

Documentation officielle : [health checks Coolify](https://coolify.io/docs/knowledge-base/health-checks).

## 10. Premier déploiement

Cliquez sur **Deploy** et suivez les logs. La première partie attendue ressemble à :

```text
Prisma schema loaded
Datasource "db": PostgreSQL
Applying migration ...
Next.js ... Ready
```

Le conteneur exécute automatiquement `prisma migrate deploy`. N’utilisez pas `prisma migrate dev` en production.

Si le déploiement échoue sur la connexion à PostgreSQL :

1. vérifiez que la base est démarrée ;
2. vérifiez que les deux ressources utilisent le même serveur et la même destination ;
3. recopiez l’Internal URL sans espace ;
4. vérifiez le mot de passe et `?schema=public` ;
5. ne remplacez jamais l’hôte interne par `localhost`.

## 11. Charger les données de départ

Cette étape est nécessaire une seule fois sur une base vide.

1. ouvrez `menu-app` dans Coolify ;
2. ouvrez **Terminal** ;
3. lancez :

```bash
npm run db:seed
```

La commande est idempotente : elle peut être relancée sans dupliquer les aliments. Elle crée les rayons, douze ingrédients et la recette d’exemple. Le saumon est configuré pour ne jamais être proposé au déjeuner en semaine.

Rechargez ensuite l’application. L’assistant de premier démarrage doit apparaître.

## 12. Vérifications après déploiement

Effectuez ce parcours avant de transmettre l’adresse à la famille :

1. ouvrez `https://menus.mondomaine.fr` dans une fenêtre privée ;
2. vérifiez que la page de connexion apparaît ;
3. essayez volontairement un mauvais mot de passe ;
4. connectez-vous avec le mot de passe familial ;
5. terminez l’assistant ;
6. préparez une semaine ;
7. vérifiez qu’aucun saumon n’apparaît le midi du lundi au vendredi ;
8. changez de semaine avec les deux flèches et vérifiez que les dates et repas changent immédiatement ;
9. confirmez une semaine, puis utilisez **Annuler la confirmation** ;
10. cochez un article de courses, rechargez la page et vérifiez qu’il reste coché ;
11. ouvrez `/api/health` et vérifiez la réponse `{"status":"ok"}` ;
12. téléchargez ou contrôlez la sauvegarde créée à l’étape 5.

## 13. Activer les déploiements automatiques

Avec une GitHub App, Coolify peut redéployer automatiquement après chaque push sur `main` :

1. ouvrez `menu-app` ;
2. allez dans **Advanced** ;
3. activez **Auto Deploy** ;
4. poussez une petite modification pour vérifier le webhook.

La base est persistante et n’est pas recréée lors d’un déploiement. Le nouveau conteneur applique uniquement les migrations manquantes avant de démarrer.

Documentation officielle : [GitHub Auto Deploy](https://coolify.io/docs/applications/ci-cd/github/auto-deploy).

## 14. Mettre l’application à jour manuellement

Avant chaque mise à jour importante :

1. déclenchez une sauvegarde PostgreSQL ;
2. poussez les changements sur `main` ;
3. cliquez sur **Redeploy** si Auto Deploy est désactivé ;
4. suivez les logs de migration ;
5. contrôlez `/api/health` puis les quatre écrans principaux.

Ne supprimez jamais la ressource PostgreSQL pour mettre à jour l’application.

## 15. Restaurer une sauvegarde

Une restauration remplace potentiellement les données actuelles. Faites-la pendant une période où personne n’utilise l’application :

1. arrêtez temporairement `menu-app` ;
2. créez une sauvegarde supplémentaire de l’état actuel ;
3. ouvrez le terminal PostgreSQL ou suivez l’action de restauration proposée par Coolify ;
4. restaurez le dump custom avec `pg_restore` ;
5. redémarrez l’application ;
6. vérifiez `/api/health`, une semaine historique et la liste de courses.

Commande PostgreSQL générique, à adapter aux identifiants Coolify :

```bash
pg_restore --verbose --clean --if-exists \
  --host HOTE --username menu --dbname menu_de_la_semaine sauvegarde.dmp
```

Testez la restauration sur une base temporaire au moins une fois avant de dépendre de l’application au quotidien.

## 16. Problèmes fréquents

### `No available server` ou erreur 404 du proxy

Le health check échoue. Consultez les logs de `menu-app`, puis vérifiez `DATABASE_URL`, PostgreSQL et `/api/health`.

### Erreur 502

Vérifiez **Port Exposes = 3000** et que le domaine contient `:3000` dans la configuration Coolify.

### Le certificat HTTPS n’est pas créé

Vérifiez l’enregistrement DNS `A`, l’absence d’un ancien enregistrement `AAAA` incorrect et l’ouverture des ports 80/443.

### Prisma ne trouve pas la base

L’hôte `localhost` désigne le conteneur de l’application, pas PostgreSQL. Utilisez exclusivement l’Internal URL Coolify.

### L’application redemande constamment le mot de passe

Vérifiez que `AUTH_SECRET` reste identique entre les redéploiements et que le domaine est toujours servi en HTTPS.

### Une ancienne version reste visible

Contrôlez que le dernier commit est bien déployé, puis utilisez **Redeploy**. Vérifiez les logs de build avant de vider le cache du navigateur.

### Une migration échoue

N’effacez pas la base. Conservez les logs, restaurez si nécessaire sur une base temporaire et corrigez la migration dans le dépôt avant un nouveau déploiement.
