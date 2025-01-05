package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.menu.Produits;

public class Incompatibilites {
    private final Produits legume;
    private final Produits feculent;

    public Incompatibilites(Produits legume, Produits feculent) {
        this.legume = legume;
        this.feculent = feculent;
    }

    @Override
    public String toString() {
        return legume + " et " + feculent + " ne vont pas ensemble.";
    }
}
