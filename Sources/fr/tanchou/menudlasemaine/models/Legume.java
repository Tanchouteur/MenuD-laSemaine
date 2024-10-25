package fr.tanchou.menudlasemaine.models;

import java.time.LocalDate;

public class Legume {
    private String legumeNom;
    private int poids;
    private LocalDate lastUsed;

    public Legume(String legumeNom, int poids, LocalDate lastUsed) {
        this.legumeNom = legumeNom;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    public String getLegumeNom() {
        return legumeNom;
    }
    public void setLegumeNom(String legumeNom) {
        this.legumeNom = legumeNom;
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
