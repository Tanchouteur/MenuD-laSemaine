package fr.tanchou.menudlasemaine.menu;

public class PlatCompose extends Plat {
    private Produits viande;
    private Accompagnement accompagnement;

    public PlatCompose(Produits viande, Accompagnement accompagnement) {
        this.viande = viande;
        this.accompagnement = accompagnement;
    }

    public Produits getViande() {
        return viande;
    }

    public void setViande(Produits viande) {
        this.viande = viande;
    }

    public Accompagnement getAccompagnement() {
        return accompagnement;
    }

    public void setAccompagnement(Accompagnement accompagnement) {
        this.accompagnement = accompagnement;
    }

    @Override
    public String getNomPlat() {
        return (viande != null ? viande.getNomProduit() : "") +
                (accompagnement != null ? "," + accompagnement.getNomAccompagnement() : "");
    }

    @Override
    public String toString() {
        return getNomPlat();
    }
}
