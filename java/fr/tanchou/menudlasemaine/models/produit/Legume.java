package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;
import java.util.Objects;

public class Legume extends Produits {

    public Legume(String nom, int poids, LocalDate lastUsed) {
        super(nom, poids, lastUsed, TypeProduit.LEGUME);
    }
}
