package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.MomentJournee;

public class Repas {
    private final Entree entree;
    private final Plat plat;
    private final MomentJournee momentJournee;

    public Repas(Entree entree, Plat plat, MomentJournee momentJournee) {
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
