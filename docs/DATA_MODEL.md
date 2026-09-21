# Modèle de données

La source canonique est `prisma/schema.prisma`. Ce document explique le rôle des
tables et les invariants qui ne sont pas évidents à la lecture du schéma.

## Catalogue

### Ingredient

Un ingrédient possède une catégorie, une appréciation, des saisons et quatre
moments autorisés. La portion et l’unité servent aux courses. Le booléen
`useInComposedMeals` autorise son emploi par le générateur d’assiettes ; il n’a
aucun effet sur son emploi dans une recette.

### Recipe et RecipeIngredient

Une recette décrit un plat complet et ses portions de base. Chaque ligne relie
un ingrédient à une quantité et une unité. Un ingrédient ne peut apparaître
qu’une fois dans une recette.

### Aisle et Incompatibility

Les rayons ordonnent les courses. Une incompatibilité représente une paire
canonique d’ingrédients distincts et concerne uniquement les compositions
automatiques.

## Planification

### WeeklyPlan

Une semaine commence obligatoirement un lundi et porte un statut `DRAFT`,
`CONFIRMED` ou `ARCHIVED`. `version` protège contre les modifications
concurrentes. Une confirmation ajoute la date `confirmedAt`.

### MealSlot

Il existe exactement un déjeuner et un dîner par date dans une semaine. Le type
du créneau détermine les références autorisées :

| Type | Contenu persistant |
|---|---|
| `EMPTY` | aucun repas |
| `RECIPE` | recette, signature et instantané |
| `COMPOSED` | protéine, accompagnement(s), signature et instantané |
| `LEFTOVERS` | référence facultative vers un créneau précédent |
| `EATING_OUT` | libellé facultatif, aucune course |
| `CUSTOM` | libellé libre, aucune course automatique |

`mealSnapshot` contient une version, le nom affiché et les quantités par
personne. Il reste valable si le catalogue change.

## Courses

`ShoppingListEntry` agrège un ingrédient et une unité pour une semaine. Sa clé
stable permet de conserver l’état coché lors d’un recalcul. Les ajouts manuels
ont une clé aléatoire et ne sont jamais supprimés par le recalcul automatique.

## Réglages

`AppSettings` est un singleton d’identifiant `default`. Il contient les nombres
de convives habituels, le fuseau, l’état de l’accueil initial et celui de la
sélection des ingrédients composables.

## Export JSON

Le format `menu-de-la-semaine-catalog`, version 1, contient le catalogue complet
y compris les éléments archivés, les réglages non secrets, les incompatibilités
et les menus. Les mots de passe, jetons, URL PostgreSQL et cookies sont exclus.
