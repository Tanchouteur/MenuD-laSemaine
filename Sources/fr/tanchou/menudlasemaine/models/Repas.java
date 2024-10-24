package fr.tanchou.menudlasemaine.models;

public class Repas {
    private int repasId;
    private Entree entree;
    private Plat plat;

    public Repas(int repasId, Entree entree, Plat plat) {
        this.repasId = repasId;
        this.entree = entree;
        this.plat = plat;
    }

    public int getRepasId() {
        return repasId;
    }

    public void setRepasId(int repasId) {
        this.repasId = repasId;
    }

    public Entree getEntree() {
        return entree;
    }

    public void setEntree(Entree entree) {
        this.entree = entree;
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }
}
