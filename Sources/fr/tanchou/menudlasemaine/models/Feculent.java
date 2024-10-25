package fr.tanchou.menudlasemaine.models;

import java.time.LocalDate;

public class Feculent {
    private String feculentName;
    private int poids;
    private LocalDate lastUsed;

    public Feculent(String feculentName, int poids, LocalDate lastUsed) {
        this.feculentName = feculentName;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    public String getFeculentName() {
        return feculentName;
    }

    public void setFeculentName(String feculentName) {
        this.feculentName = feculentName;
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
