package fr.tanchou.menudlasemaine.menu;

public class Repas {
    private final Produits entree;
    private final Plat plat;

    public Repas(Produits entree, Plat plat) {
        this.entree = entree;
        this.plat = plat;
    }

    public Produits getEntree() {
        return entree;
    }

    public Plat getPlat() {
        return plat;
    }

    public String getNomRepas() {
        return entree.getNomProduit() + "," + plat.getNomPlat();
    }

    @Override
    public String toString() {
        return "Repas { Entrée" + entree + " - Plat " + plat + " }";
    }
}
