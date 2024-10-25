package fr.tanchou.menudlasemaine.models;

import java.time.LocalDate;

public class Entree {
    private String nomEntree;
    private int poids;
    private LocalDate lastUsed;

    public Entree(String nomEntree, int poids, LocalDate lastUsed) {
        this.nomEntree = nomEntree;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    public String getNomEntree() {
        return nomEntree;
    }

    public void setNomEntree(String nomEntree) {
        this.nomEntree = nomEntree;
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
