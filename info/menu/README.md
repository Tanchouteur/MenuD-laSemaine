# Partie logique métier

## Introduction
Le systeme est a pour but de permettre de généré facilement un menu ou un repas en fonction de criteres :
- L'icompatibilité des aliments (Flagoellet et couscous par exemple)
- La saisonnalité des aliments (Racllette en été)
- Le moment de la journée (déjeuner, diner)
- Les préférences alimentaires
- Le moment de la semaine (weekend, semaine. Sa évite de devoir faire un repas trop long en semaine surtout le midi)
- La dernière fois que l'on a mangé un aliment (éviter de manger 2 fois la même chose dans la semaine)

## Fonctionnement
Le systeme est basé sur une liste d'aliments séparé en plusieurs catégories :
- Les entrées
- Les viandes
- Les légumes
- Les féculents
- Les plat complets


un menu est composé de 14 repas (7 midi et 7 soir) et est généré en fonction des critères cités plus haut.
Les repas contiennent une entrée et un plat.
Un plat est soit un plat complet (ex: pizza) soit une viande avec un accompagnement.
un accompagnement est composé d'un légume et d'un féculent. (pas obligatoire)


## Les classes
La structure est séparé en plusieurs type de classes :
- Les class qui gere le poids des alliments en fonction des critères
- la class qui Factory qui permet de généré les repas
- Les Enums qui permettent de définir les types de produits et autres constantes

## Les tests
A venir...

## Les améliorations
- Rajouter des critères pour le choix de l'aliment (prix, temps de préparation, nombre de personne)
- Rajouter des tests
