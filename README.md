# MenuD-laSemaine
Projet-Personnel

## [Base de données](info/README.md)

## Ajouter un produit : 
```sql
PROCEDURE ajouter_produit(
     IN p_nom_produit VARCHAR(255),
     IN p_type_produit ENUM('Legume', 'Viande', 'Feculent', 'Entree', 'plat_complet'),

     IN p_poids INT,

     IN p_poids_midi_semaine INT,
     IN p_poids_soir_semaine INT,
     IN p_poids_midi_weekend INT,
     IN p_poids_soir_weekend INT,

     IN p_poids_printemps INT,
     IN p_poids_ete INT,
     IN p_poids_automne INT,
     IN p_poids_hiver INT
 );


CALL ajouter_produit('Harricot-vert', 'legume', 20, 0, 50, 50, 40 , 0, 0, 0, 100);
```