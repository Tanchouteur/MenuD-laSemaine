package fr.tanchou.menudlasemaine.menu;

public class PlatComplet extends Plat {
    private final Produits plat;

    public PlatComplet(Produits plat) {
        this.plat = plat;
    }

    public Produits getPlat() {
        return plat;
    }

    @Override
    public String getNomPlat() {
        return plat.getNomProduit();
    }
}
