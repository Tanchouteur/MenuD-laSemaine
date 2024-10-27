package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.models.produit.Entree;

public class Repas {
    private final Entree entree;
    private final Plat plat;
    private final MomentJournee momentJournee;
    private final int poids;

    public Repas(Entree entree, Plat plat, MomentJournee momentJournee) {
        int poidsEntree = 0;
        if (entree != null) {
            poidsEntree = entree.getPoids();
        }

        this.poids = poidsEntree + plat.getPoids();
        this.entree = entree;
        this.plat = plat;
        this.momentJournee = momentJournee;
    }

    public Entree getEntree() {
        return entree;
    }

    public Plat getPlat() {
        return plat;
    }

    public MomentJournee getMomentJournee() {
        return momentJournee;
    }

    @Override
    public String toString() {
        return "Repas { Entrée" + entree + " - Plat " + plat + " - Moment de la journée " + momentJournee + " }";
    }
}
