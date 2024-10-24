package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Feculent;
import fr.tanchou.menudlasemaine.models.Legume;

public class AccompagnementGenerator {
    public static Accompagnement generateAccompagnement(Legume legume, Feculent feculent) {
        return new Accompagnement(0, legume, feculent); // Utilise le modèle existant
    }
}
