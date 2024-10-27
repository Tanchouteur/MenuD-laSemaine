package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.produit.Viande;

public class PlatCompose extends Plat {
    private Viande viande;
    private Accompagnement accompagnement;

    public PlatCompose(int poids, Viande viande, Accompagnement accompagnement) {
        super(poids, TypePlat.COMPOSE);
        this.viande = viande;
        this.accompagnement = accompagnement;
    }

    public Viande getViande() {
        return viande;
    }

    public void setViande(Viande viande) {
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
        return (viande != null ? viande.getViandeNom() : "") +
                (accompagnement != null ? " + " + accompagnement.getNomAccompagnement() : "");
    }

    @Override
    public String toString() {
        return "PlatCompose{ " +
                "nomPlat='" + getNomPlat() + '\'' +
                " , poids=" + getPoids() +
                ", viande=" + (viande != null ? viande.getViandeNom() : "Aucune") +
                ", accompagnement=" + (accompagnement != null ? accompagnement.getNomAccompagnement() : "Aucun") +
                '}';
    }
}
