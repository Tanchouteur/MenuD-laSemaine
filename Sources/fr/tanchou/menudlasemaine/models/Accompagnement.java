package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.models.produit.Feculent;
import fr.tanchou.menudlasemaine.models.produit.Legume;

public class Accompagnement {
    private Legume legume;
    private Feculent feculent;
    private int poids;

    public Accompagnement(Legume legume, Feculent feculent) {
        this.legume = legume;
        this.feculent = feculent;
        this.poids = legume.getPoids() + feculent.getPoids();
    }

    public Accompagnement(Feculent feculent) {
        this.legume = null;
        this.feculent = feculent;
        this.poids = feculent.getPoids();
    }
    public Accompagnement(Legume legume) {
        this.legume = legume;
        this.feculent = null;
        this.poids = legume.getPoids();
    }

    public Legume getLegume() {
        return legume;
    }

    public void setLegume(Legume legume) {
        this.legume = legume;
    }

    public Feculent getFeculent() {
        return feculent;
    }

    public void setFeculent(Feculent feculent) {
        this.feculent = feculent;
    }

    public String getNomAccompagnement() {
        String nomAccompagnement = "";
        if (legume != null) {
            nomAccompagnement += legume.getLegumeNom() + " ";
        }
        if (feculent != null) {
            nomAccompagnement += feculent.getFeculentNom();
        }
        return nomAccompagnement;
    }

    @Override
    public String toString() {
        return "Accompagnement{" +
                "legume=" + legume +
                ", feculent=" + feculent +
                '}';
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }
}
