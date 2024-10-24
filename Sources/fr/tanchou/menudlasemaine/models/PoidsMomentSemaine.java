package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.MomentSemaine;

public class PoidsMomentSemaine {
    private int poidsId;
    private Plat plat;
    private MomentSemaine momentSemaine; //enum "lundi" à "dimanche"
    private int poids;

    public PoidsMomentSemaine(int poidsId, Plat plat, MomentSemaine momentSemaine, int poids) {
        this.poidsId = poidsId;
        this.plat = plat;
        this.momentSemaine = momentSemaine;
        this.poids = poids;
    }

    public int getPoidsId() {
        return poidsId;
    }

    public void setPoidsId(int poidsId) {
        this.poidsId = poidsId;
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }

    public MomentSemaine getMomentSemaine() {
        return momentSemaine;
    }

    public void setMomentSemaine(MomentSemaine momentSemaine) {
        this.momentSemaine = momentSemaine;
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }
}
