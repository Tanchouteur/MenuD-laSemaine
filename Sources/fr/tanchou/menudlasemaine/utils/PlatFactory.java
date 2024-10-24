package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Viande;

public class PlatFactory {
    public static Plat createPlat(Viande viande, Accompagnement accompagnement, boolean toutEnUn, String nomPlat) {
        return new Plat(0, viande, accompagnement, toutEnUn, nomPlat);
    }
}
