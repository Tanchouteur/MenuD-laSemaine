# Fichier ou je pose mes idées

## Logique des poids

### Les éléments qui vont affecter les poids dans les choix sont les suivants:
Premierement la valeurs arbitraire mis par moi même pour chaque élément. qui définira notre appréciation du produit.

Ensuite, La derniere date a laquel le produit a été selectionné. Plus le produit a été selectionné récemment, moins il a de chance d'etre selectionner. (pour éviter d'avoir 2 fois le meme produit d'affiler)

Le moment de lajournée, Midi ou Soir. pour limiter beaucoup de plat

Le moment de la semaine, pour limiter les plat trop long a faire en semaine

la saisons, pour etre en accord avec les produits de saisons

et endfin le nombre de personne au repas


### Détail de mes idées
#### Valeurs arbitraire
ba sa c'est simple, dans les tables des produit il y a un attribus "poids" qui est un entier. il suffit de le remplirs

#### Derniere date de selection
J'ai fait une table ProduitLastUsed qui représente la derniere date a laquel le produit a été selectionné. Il suffit de faire une requete pour avoir la date la plus récente

on additionne le poids de chaque produit avec un le poid généré par la derniere utilisation qui diminue en fonction de la date
Mais a voir si c'est pas mieux de multiplier plutot que d'additionner

#### Moment de la journée et de la semaine



```sql
                     (nom du produit, type    , poid produit , poid midi, poid soir , poid semaine  , poid week-end);
CALL ajouter_produit('Harricot-vert', 'Legume', 50           , 30       , 100       , 50            , 100          );
```

donc par exemple si on veux qu'un plat n'apparaisse pas le midi en semaine mais que le midi en weekend il puisse sortir alors il faut mettre le poid du midi en semaine a 0

pour rajouter un produit dans sa table il suffit de faire un appel a la procedure ajouter_produit

Le poids calculer pour la selection baser sur le poids est désormais stocker dans l'atrtibut poids des chaque produit apres qu'il ais été séléctionné