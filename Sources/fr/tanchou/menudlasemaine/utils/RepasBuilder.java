package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Entree;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Repas;

import java.util.Random;

public class RepasBuilder {
    public static Repas buildRepa() {

        Plat plat = PlatFactory.getRandomPlat();

        Random random = new Random();
        int probaEntry = random.nextInt(100);
        Entree entree = null;

        if (probaEntry > 70){
            entree = EntreeFactory.getRandomEntree();
        }else {
            entree = null;
        }

        return new Repas(entree, plat);
    }
}
