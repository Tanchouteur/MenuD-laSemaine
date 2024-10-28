package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.models.Plat;

import java.util.Random;

public class PlatFactory {
    public static Plat getRandomPlat(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison) {
        Random random = new Random();
        int probaPlatComplet = random.nextInt(100);
        Plat plat = null;

        int seuil = 50;

        if (probaPlatComplet > seuil){
            plat = PlatCompletFactory.getRandomPlatComplet(momentJournee, momentSemaine, saison);
        }else {
            plat = PlatComposeFactory.getRandomPlatCompose(momentJournee, momentSemaine, saison);
        }

        return plat;
    }
}
