package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Entree;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Repas;

public class RepasBuilder {
    private Entree entree;
    private Plat plat;

    public RepasBuilder setEntree(Entree entree) {
        this.entree = entree;
        return this;
    }

    public RepasBuilder setPlat(Plat plat) {
        this.plat = plat;
        return this;
    }

    public Repas build() {
        return new Repas(0, entree, plat);
    }
}
