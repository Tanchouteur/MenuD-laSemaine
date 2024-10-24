package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Plat;

import java.util.Random;

public class PlatFactory {
    public static Plat getRandomPlat() {
        Random random = new Random();
        int probaPlatComplet = random.nextInt(100);
        Plat plat = null;

        if (probaPlatComplet > 70){
            plat = PlatCompletFactory.getRandomPlatComplet();
        }else {
            plat = PlatComposeFactory.getRandomPlatCompose();
        }

        return plat;
    }
}
