package fr.tanchou.menudlasemaine.menu;

import fr.tanchou.menudlasemaine.enums.MomentJournee;

public class Repas {
    private final Produits entree;
    private final Plat plat;
    private final MomentJournee momentJournee;

    public Repas(Produits entree, Plat plat, MomentJournee momentJournee) {
        this.entree = entree;
        this.plat = plat;
        this.momentJournee = momentJournee;
    }

    public Produits getEntree() {
        return entree;
    }

    public Plat getPlat() {
        return plat;
    }

    public MomentJournee getMomentJournee() {
        return momentJournee;
    }

    public String getNomRepas() {
        return entree.getNomProduit() + " + " + plat.getNomPlat();
    }

    @Override
    public String toString() {
        return "Repas { Entrée" + entree + " - Plat " + plat + " - Moment de la journée " + momentJournee + " }";
    }
}
