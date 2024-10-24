package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.PlatCompletDAO;
import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.*;

public abstract class PlatFactory {

    public static PlatCompose createPlatCompose(Viande viande, Accompagnement accompagnement, float poids) {
        // Crée et retourne un PlatCompose
        return new PlatCompose(0, TypePlat.COMPOSE, poids, viande, accompagnement);
    }
}
