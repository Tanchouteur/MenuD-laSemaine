package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public abstract class Plat {
    private int platId;
    private TypePlat typePlat; // Utilisation de l'enum
    private float poids;

    // Constructeur
    public Plat(int platId, TypePlat typePlat, float poids) {
        this.platId = platId;
        this.typePlat = typePlat;
        this.poids = poids;
    }

    // Getters et setters
    public int getPlatId() {
        return platId;
    }

    public void setPlatId(int platId) {
        this.platId = platId;
    }

    public TypePlat getTypePlat() {
        return typePlat;
    }

    public void setTypePlat(TypePlat typePlat) {
        this.typePlat = typePlat;
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
                "platId=" + platId +
                ", typePlat=" + typePlat.getLabel() +
                ", poids=" + poids +
                '}';
    }
}
