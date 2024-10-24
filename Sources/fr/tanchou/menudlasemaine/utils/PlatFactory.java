package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.*;

public abstract class PlatFactory {

    public abstract Plat createPlat();

    public static PlatComplet createPlatComplet(String nomPlat, float poids) {
        return new PlatComplet(0, TypePlat.COMPLET, poids, nomPlat);
    }

    public static PlatCompose createPlatCompose(Viande viande, Accompagnement accompagnement, float poids) {
        // Crée et retourne un PlatCompose
        return new PlatCompose(0, TypePlat.COMPOSE, poids, viande, accompagnement);
    }
}
