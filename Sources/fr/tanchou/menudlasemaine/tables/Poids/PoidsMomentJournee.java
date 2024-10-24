package fr.tanchou.menudlasemaine.tables.Poids;

import fr.tanchou.menudlasemaine.tables.Menu.Plat;

public class PoidsMomentJournee {
    private int poidsId;
    private Plat plat;
    private MomentJournee momentJournee; //enum "midi" ou "soir"
    private int poids;

    public PoidsMomentJournee(int poidsId, Plat plat, MomentJournee momentJournee, int poids) {
        this.poidsId = poidsId;
        this.plat = plat;
        this.momentJournee = momentJournee;
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

    public MomentJournee getMomentJournee() {
        return momentJournee;
    }

    public void setMomentJournee(MomentJournee momentJournee) {
        this.momentJournee = momentJournee;
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }
}
