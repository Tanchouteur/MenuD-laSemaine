package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.Saison;

public class PoidSaison {
    private int poidId;
    private Plat plat;
    private Saison saison; //enum "HIVER", "PRINTEMPS", "ETE", "AUTOMNE"
    private int poid;

    public PoidSaison(int poidId, Plat plat, Saison saison, int poid) {
        this.poidId = poidId;
        this.plat = plat;
        this.saison = saison;
        this.poid = poid;
    }

    public int getPoidId() {
        return poidId;
    }

    public void setPoidId(int poidId) {
        this.poidId = poidId;
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }

    public Saison getSaison() {
        return saison;
    }

    public void setSaison(Saison saison) {
        this.saison = saison;
    }

    public int getPoid() {
        return poid;
    }

    public void setPoid(int poid) {
        this.poid = poid;
    }
}
