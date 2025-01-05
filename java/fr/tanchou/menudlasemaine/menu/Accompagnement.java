package fr.tanchou.menudlasemaine.menu;

public class Accompagnement {
    private final Produits legume;
    private final Produits feculent;

    public Accompagnement(Produits legume, Produits feculent) {
        this.legume = legume;
        this.feculent = feculent;
    }

    public Produits getLegume() {
        return legume;
    }

    public Produits getFeculent() {
        return feculent;
    }

    public String getNomAccompagnement() {
        String nomAccompagnement = "";
        if (legume != null) {
            nomAccompagnement += legume.getNomProduit() + ",";
        }
        if (feculent != null) {
            nomAccompagnement += feculent.getNomProduit();
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
}
