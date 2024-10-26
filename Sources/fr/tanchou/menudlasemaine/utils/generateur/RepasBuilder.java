package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Repas;

import java.util.Random;

public class RepasBuilder {
    public static Repas buildRepa(MomentJournee momentJournee, MomentSemaine momentSemaine) {

        Plat plat = PlatFactory.getRandomPlat(momentJournee, momentSemaine);

        Random random = new Random();
        int probaEntry = random.nextInt(100);
        Entree entree;

        if (probaEntry > 70){
            entree = EntreeFactory.getRandomEntree(momentJournee);
        }else {
            entree = null;
        }

        return new Repas(entree, plat, momentJournee);
    }
}
