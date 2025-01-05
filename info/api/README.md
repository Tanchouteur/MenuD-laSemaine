# API

## Introduction
L'API est une interface de programmation qui permet de communiquer avec un serveur. Elle permet de récupérer des données, de les envoyer, de les modifier, etc. L'API est un moyen de communication entre deux applications.

Pour l'API j'ai utilisé la librairie javax.net et com.sun.net.httpserver pour créer un serveur HTTPS.

J'ai utiliser les certificats ssl fournit avec mon nom de dommaine

Cette partie de l'application est pour moi la partie la plus interessante du programme.

## Fonctionnalités
Je crée des points d'entrée pour les différentes fonctionnalités de l'application. Les points d'entrée sont les suivants :*

#### Menu
- /menu/getMenu : permet de récupérer le menu
- /menu/changeMenu : permet de modifier le menu
- /menu/repas/change : permet de modifier un repas

#### Produits
- /products/get : permet de récupérer les produits
- /products/add : permet d'ajouter un produit
- /products/delete : permet de supprimer un produit
- /products/update : permet de modifier un produit

## Les futures améliorations
- Test JUNITS
- Ajout de sécurité (Pour l'instant il n'y a pas de sécurité)
