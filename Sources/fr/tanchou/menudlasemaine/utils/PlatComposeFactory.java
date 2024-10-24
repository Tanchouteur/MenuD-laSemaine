package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Viande;

public class PlatComposeFactory extends PlatFactory {
    private final Viande viande;
    private final Accompagnement accompagnement;
    private final float poids;

    public PlatComposeFactory(Viande viande, Accompagnement accompagnement, float poids) {
        this.viande = viande;
        this.accompagnement = accompagnement;
        this.poids = poids;
    }

    public Plat createPlat() {
        return createPlatCompose(viande, accompagnement, poids);
    }
}
