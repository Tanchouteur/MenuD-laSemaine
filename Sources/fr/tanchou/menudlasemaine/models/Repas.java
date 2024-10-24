package fr.tanchou.menudlasemaine.models;

public class Repas {
    private final Entree entree;
    private final Plat plat;

    public Repas( Entree entree, Plat plat) {
        this.entree = entree;
        this.plat = plat;
    }

    public Entree getEntree() {
        return entree;
    }

    public Plat getPlat() {
        return plat;
    }
}
