package fr.tanchou.menudlasemaine.models;

import java.util.Date;

public class Viande {
    private String nom;
    private int poids;
    private Date lastUsed;

    public Viande(String nom, int poids, Date lastUsed) {
        this.nom = nom;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    public String getNomViande() {
        return nom;
    }

    public void setNomViande(String nom) {
        this.nom = nom;
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
