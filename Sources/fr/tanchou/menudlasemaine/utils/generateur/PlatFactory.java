package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.models.Plat;

import java.util.Random;

public class PlatFactory {
    public static Plat getRandomPlat(MomentJournee momentJournee, MomentSemaine momentSemaine) {
        Random random = new Random();
        int probaPlatComplet = random.nextInt(100);
        Plat plat = null;

        if (probaPlatComplet > 70){
            plat = PlatCompletFactory.getRandomPlatComplet(momentJournee);
        }else {
            plat = PlatComposeFactory.getRandomPlatCompose(momentJournee, momentSemaine);
        }

        return plat;
    }
}
