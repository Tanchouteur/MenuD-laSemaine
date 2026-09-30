# Exploitation et maintenance

## Production

La production comprend deux ressources Coolify sur le même réseau Docker :

- `Menu de la Semaine`, construite avec le `Dockerfile` du dépôt ;
- `menu-postgres`, base PostgreSQL persistante non destinée à Internet.

Les variables nécessaires sont `DATABASE_URL`, `FAMILY_PASSWORD`, `AUTH_SECRET`
et `CALENDAR_TOKEN`. Elles ne doivent jamais être copiées dans un export, un
commit ou une capture publique.

## Déploiement normal

1. Exécuter localement `npm run check`, `npm run db:validate` et `npm run build`.
2. Déclencher une sauvegarde PostgreSQL et attendre sa réussite.
3. Pousser le commit sur la branche suivie par Coolify.
4. Suivre la construction et `prisma migrate deploy` dans les journaux.
5. Contrôler `/api/health`, une semaine historique, une recette et les courses.

Ne jamais exécuter `prisma migrate dev`, le seed ou l’import historique en
production pendant une mise à jour.

## Sauvegardes

Coolify n’affiche une exécution immédiate qu’après création d’une planification.
Créer au minimum une sauvegarde locale quotidienne et conserver sept
exécutions. Une copie externe protège contre la perte complète du serveur.

## Export fonctionnel

La page **Plus > Historique** permet de télécharger le catalogue JSON. Cet
export est une copie de travail lisible, pas un remplacement d’un dump
PostgreSQL. Il sert à auditer et enrichir les données sans terminal Coolify.

Un fichier au format `menu-de-la-semaine-enrichment` peut être sélectionné dans
la même section. L’interface affiche d’abord le nombre d’ingrédients et de
recettes à créer ou mettre à jour. Le bouton d’application apparaît seulement
après cette validation. L’écriture est atomique et les menus existants ne sont
pas modifiés.

Depuis le format d’enrichissement version 2, l’aperçu indique aussi les
ingrédients à retirer des propositions et les quantités ou portions encore à
vérifier. Tant qu’une valeur manque, le bouton d’application reste désactivé.
Le fichier prêt à importer issu du catalogue joint est décrit dans
`Catalogue/CONVERSION_REPAS_2026-09-25.md`.

Une recette peut déclarer `previousName` pour être renommée en conservant son
identifiant et ses références. Les métadonnées facultatives permettent aussi de
mettre à jour son style, ses durées, ses saisons et ses moments de proposition.

## Diagnostic rapide

### Calendrier des menus : libre et sans notifications

Le flux `/api/calendar` conserve les créneaux de 12 h à 13 h 30 et de 19 h à
20 h 30 (Europe/Paris). Chaque repas est informatif : `TRANSP:TRANSPARENT`
laisse la disponibilité libre, et aucun `VALARM` n’est exporté. Les identifiants
des événements restent identiques pour mettre à jour l’abonnement existant.

L’absence de rappel dans le flux ne force pas les préférences d’alerte du
client. Pour rendre uniquement les menus silencieux dans Apple Calendrier :

- **iPhone** : Calendrier → Calendriers → ⓘ à côté de « Menus de la famille »
  → désactiver **Alertes** → OK.
- **Mac** : sélectionner « Menus de la famille » → Édition → Lire les
  informations → cocher **Ignorer les alertes** → OK.

Vérifier le réglage sur les appareils utilisés. Après déploiement, le statut
libre sera récupéré à la prochaine actualisation de l’abonnement ; le flux
suggère une heure, mais le client choisit la fréquence effective. Les événements
importés comme copie ne sont pas mis à jour par l’abonnement.

Sources consultées le 30 septembre 2026 :
[Apple iPhone](https://support.apple.com/fr-fr/guide/iphone/iph3d1110d4/ios),
[Apple Mac](https://support.apple.com/fr-fr/guide/calendar/icl1022/mac),
[RFC 5545, §3.8.2.7](https://datatracker.ietf.org/doc/html/rfc5545#section-3.8.2.7).

### Application et base de données

- `/api/health` non sain : vérifier PostgreSQL et `DATABASE_URL`.
- erreur 502 : vérifier le port interne 3000 et les journaux du conteneur.
- échec de migration : conserver les logs, ne pas supprimer la base, tester la
  correction sur une restauration temporaire.
- terminal Coolify vide : la connexion WebSocket du terminal est probablement
  interrompue par le proxy ou le tunnel ; utiliser les fonctions applicatives
  ou SSH pour le diagnostic, sans exposer PostgreSQL.

## Rollback

Revenir à une image ou un commit antérieur plutôt que réinitialiser Git ou la
base. Les migrations additives restent en place si l’ancienne application
ignore leurs colonnes. Une restauration PostgreSQL est réservée aux corruptions
ou migrations destructrices et doit être précédée d’une sauvegarde de l’état
courant.

Le guide détaillé de Coolify se trouve dans `DEPLOIEMENT_COOLIFY.md`.
