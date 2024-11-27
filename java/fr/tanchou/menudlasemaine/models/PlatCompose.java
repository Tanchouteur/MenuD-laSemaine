package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.time.LocalDate;

public class PlatCompose extends Plat {
    private Viande viande;
    private Accompagnement accompagnement;

    public PlatCompose(int poids, Viande viande, Accompagnement accompagnement) {
        String nom = viande.getNom();
        if (accompagnement != null){
            nom = nom + " " + accompagnement.getNomAccompagnement();
        }
        super(nom ,poids, viande.getLastUsed(),TypePlat.COMPOSE);

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
        return (viande != null ? viande.getNom() : "") +
                (accompagnement != null ? " + " + accompagnement.getNomAccompagnement() : "");
    }

    @Override
    public String toString() {
        return "PlatCompose{ " +
                "nomPlat='" + getNomPlat() + '\'' +
                " , poids=" + getPoids() +
                ", viande=" + (viande != null ? viande.getNom() : "Aucune") +
                ", accompagnement=" + (accompagnement != null ? accompagnement.getNomAccompagnement() : "Aucun") +
                '}';
    }

    @Override
    public String getNom() {
        return viande.getNom() + accompagnement.getNomAccompagnement();
    }
}
