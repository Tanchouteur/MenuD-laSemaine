package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.produit.Poidsable;

public abstract class Plat implements Poidsable {
    private final TypePlat typePlat; // Utilisation de l'enum
    private int poids;

    public Plat(int poids, TypePlat typePlat) {
        this.poids = poids;
        this.typePlat = typePlat;
    }

    public TypePlat getTypePlat() {
        return typePlat;
    }

    @Override
    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }

    public abstract String getNomPlat(); // Méthode abstraite pour obtenir le nom du plat

    @Override
    public String toString() {
        return "Plat{" +
                ", typePlat=" + typePlat.getLabel() +
                ", poids=" + poids +
                '}';
    }
}
