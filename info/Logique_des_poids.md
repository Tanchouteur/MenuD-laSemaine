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

#### Moment de la journée

