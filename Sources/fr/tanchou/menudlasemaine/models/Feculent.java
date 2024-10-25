package fr.tanchou.menudlasemaine.models;

import java.util.Date;

public class Feculent {
    private String feculentName;
    private int poids;
    private Date lastUsed;

    public Feculent(String feculentName, int poids, Date lastUsed) {
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

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
