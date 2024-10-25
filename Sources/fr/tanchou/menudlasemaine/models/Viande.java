package fr.tanchou.menudlasemaine.models;

import java.time.LocalDate;

public class Viande {
    private String nom;
    private int poids;
    private LocalDate lastUsed;

    public Viande(String nom, int poids, LocalDate lastUsed) {
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

    public LocalDate getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDate lastUsed) {
        this.lastUsed = lastUsed;
    }
}
