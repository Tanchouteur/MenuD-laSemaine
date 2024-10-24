package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public abstract class Plat {
    private final TypePlat typePlat; // Utilisation de l'enum
    private int poids;
    private final String nomPlat;

    public Plat(int poids, TypePlat typePlat) {
        this.poids = poids;
        this.typePlat = typePlat;
        this.nomPlat = getNomPlat();
    }

    // Getters et setters

    public TypePlat getTypePlat() {
        return typePlat;
    }

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
