# Conversion proposée du catalogue du 25 septembre 2026

Source : export `menu-catalogue-2026-09-25.json` fourni par Louis. Le fichier **`enrichissement-repas-2026-09-25.json` est prêt pour l'import**, mais n'a pas été appliqué. L'ouvrir dans **Plus → Historique → Importer un enrichissement** affiche les créations et modifications avant application. Ce fichier est un enrichissement ciblé, pas un export complet de la base.

## Sens des éléments

- Un ingrédient représente un produit acheté. `Tenders Lidl`, `Tournedos`, `Pâtes`, `Riz`, `Semoule` et `Haricots verts` restent des ingrédients.
- Une recette représente une préparation. `Omelette au jambon`, `Poulet à la crème moutarde` et `Pdt. Sauté` sont proposés comme recettes ; leurs anciens ingrédients sont archivés uniquement lors de l'application validée.
- `Asperges` et `Cœurs de palmiers` sont des entrées simples. `Tomates et carottes râpées` est une recette d'entrée.
- `Poulet à la crème moutarde` est lié comme variante de `Poulet curry coco et riz` pour partager le refroidissement. Le lien n'interdit pas les deux recettes dans la même semaine.

## Portions usuelles retenues

Les quantités préremplies (8 œufs, 600 g de poulet, 20 cl de crème, 1 kg de pommes de terre pour 4 personnes) proviennent des portions de l'export. Pour les données absentes, nous avons retenu des **estimations pour un adulte**, modifiables dans la bibliothèque après import :

| Élément | Portion retenue |
| --- | --- |
| Jambon grillé de l'omelette | 1 tranche/personne |
| Moutarde | 10 g/personne |
| Huile d'olive des pommes de terre sautées | 1 cl/personne |
| Tomates et carottes râpées | 100 g de tomate + 80 g de carotte/personne |
| Asperges | 150 g/personne |
| Cœurs de palmiers | 100 g/personne |
| Tenders Lidl | 150 g/personne |
| Semoule sèche | 80 g/personne |
| Brocolis | 150 g/personne |

La portion de tenders est en grammes car la taille des pièces varie selon le paquet. Ces estimations servent à calculer les courses ; elles ne prétendent pas décrire les portions réellement mangées auparavant.

Les semaines déjà enregistrées conservent leurs instantanés. Une correction manuelle d'une semaine confirmée met à jour sa liste de courses, sans régénérer les autres repas.
