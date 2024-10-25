package fr.tanchou.menudlasemaine.models;

import java.util.Date;

public class Legume {
    private String legumeNom;
    private int poids;
    private Date lastUsed;

    public Legume(String legumeNom, int poids, Date lastUsed) {
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

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
