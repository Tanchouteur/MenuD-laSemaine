package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public class PlatCompose extends Plat {
    private Viande viande;
    private Accompagnement accompagnement;
    private final String nomPlat;

    // Constructeur
    public PlatCompose(float poids, Viande viande, Accompagnement accompagnement) {
        super(poids, TypePlat.COMPOSE);
        this.viande = viande;
        this.accompagnement = accompagnement;
        this.nomPlat = getNomPlat();
    }

    // Getters et setters
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
        return (viande != null ? viande.getNomViande() : "") +
                (accompagnement != null ? " + " + accompagnement.getNomAccompagnement() : "");
    }

    @Override
    public String toString() {
        return "PlatCompose{ " +
                "nomPlat='" + nomPlat + '\'' +
                " , poids=" + getPoids() +
                ", viande=" + (viande != null ? viande.getNomViande() : "Aucune") +
                ", accompagnement=" + (accompagnement != null ? accompagnement.getNomAccompagnement() : "Aucun") +
                '}';
    }
}
