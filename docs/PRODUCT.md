# Produit — Menu de la semaine

## Objectif

L’application aide un foyer unique à préparer quatorze repas, remplacer les
propositions qui ne conviennent pas et obtenir une liste de courses calculée à
partir du nombre de convives. L’expérience de référence est le téléphone.

## Utilisateurs et accès

- Un seul foyer partage les mêmes ingrédients, recettes, menus et réglages.
- Un mot de passe familial facultatif protège toutes les pages et API, sauf le
  contrôle de santé et le calendrier muni de son jeton.
- L’application n’est pas un service multi-utilisateur et ne gère pas de rôles.

## Parcours principaux

1. Maintenir le catalogue d’ingrédients et de recettes.
2. Choisir les ingrédients utilisables dans les assiettes automatiques.
3. Générer une semaine puis conserver, remplacer ou saisir chaque repas.
4. Confirmer la semaine afin qu’elle alimente l’historique et les courses.
5. Cocher les courses, consulter l’historique et réutiliser une semaine.
6. Exporter les données en JSON pour les relire ou préparer un enrichissement.

Sur la page Semaine, le changement de semaine et les raccourcis vers les jours
restent visibles au défilement. L’onglet Courses reprend la semaine sélectionnée
et indique clairement si elle attend encore une confirmation.

## Règles visibles

- Un ingrédient est un produit acheté ; une recette décrit une préparation et peut être un plat, une entrée ou un accompagnement.
- Un plat préparé peut être complet ou demander un féculent et/ou un légume choisi pour ce repas.
- « Variante de » relie des recettes pour partager le refroidissement, sans interdire deux variantes dans la même semaine.
- Une entrée est facultative ; la cible générée est réglable de 0 à 9 par semaine (3 par défaut), jamais le midi en semaine.
- Une assiette automatique contient une protéine et au moins un accompagnement.
- Les ingrédients de recette ne sont pas automatiquement des bases de repas.
- Un choix manuel prime sur saison, moment, répétition et incompatibilité ; les
  écarts sont signalés et le choix est conservé.
- Seules les semaines confirmées comptent comme repas consommés.
- Les restes et repas extérieurs n’ajoutent pas de courses.
- Les brouillons et les semaines rouvertes ne montrent pas de liste de courses ; elle redevient accessible après confirmation.
- Une semaine confirmée accepte les corrections manuelles ; seule sa génération automatique est bloquée.

## Hors périmètre actuel

- comptes individuels et permissions par membre du foyer ;
- inventaire du placard et déduction du stock ;
- calcul nutritionnel ou médical ;
- synchronisation bidirectionnelle avec un calendrier ;
- modification automatique des données à partir d’un export JSON.

## Critères de qualité

L’interface doit rester compréhensible sans vocabulaire technique, fonctionner
au clavier, conserver les saisies en cas d’erreur et rendre les opérations
importantes réversibles. Une mise à jour ne doit jamais réécrire les anciennes
semaines ni nécessiter de réinitialiser PostgreSQL.
