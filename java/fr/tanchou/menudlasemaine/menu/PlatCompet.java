package fr.tanchou.menudlasemaine.menu;

public class PlatCompet extends Plat {
    private final Produits plat;

    public PlatCompet(Produits plat) {
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
