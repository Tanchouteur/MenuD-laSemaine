package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;

public class Entree extends Produits {
    public Entree(String nom, int poids, LocalDate lastUsed) {
        super(nom, poids, lastUsed, TypeProduit.ENTREE);
    }
}
