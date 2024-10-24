package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public abstract class Plat {
    private final TypePlat typePlat; // Utilisation de l'enum
    private float poids;
    private final String nomPlat;

    public Plat(float poids, TypePlat typePlat) {
        this.poids = poids;
        this.typePlat = typePlat;
        this.nomPlat = getNomPlat();
    }

    // Getters et setters

    public TypePlat getTypePlat() {
        return typePlat;
    }

    public float getPoids() {
        return poids;
    }

    public void setPoids(float poids) {
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
