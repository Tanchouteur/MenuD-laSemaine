package fr.tanchou.menudlasemaine.models;

import java.util.Date;

public class Entree {
    private String nomEntree;
    private int poids;
    private Date lastUsed;

    public Entree(String nomEntree, int poids, Date lastUsed) {
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

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
