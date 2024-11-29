package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Poidsable;
import fr.tanchou.menudlasemaine.models.produit.Produits;

import java.time.LocalDate;

public abstract class Plat extends Produits {
    private final TypePlat typePlat; // Utilisation de l'enum

    public Plat(String nom, int poids, LocalDate lastUsed, TypePlat typePlat) {
        super(nom,poids,lastUsed, TypeProduit.PLAT_COMPLET);
        this.typePlat = typePlat;
    }


    public abstract String getNomPlat(); // Méthode abstraite pour obtenir le nom du plat

    @Override
    public String toString() {
        return getNomPlat();
    }
}
