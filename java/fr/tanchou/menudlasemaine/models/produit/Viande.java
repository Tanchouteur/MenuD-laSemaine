package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;

public class Viande extends Produits {

    public Viande(String nom, int poids, LocalDate lastUsed) {
        super(nom, poids, lastUsed, TypeProduit.VIANDE);
    }


}
