package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.models.PlatCompose;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.util.Random;

public class RepasBuilder {
    public static Repas buildRepa(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison, LastUseWeightManager lastUseWeightManager) {

        Plat plat = PlatFactory.getRandomPlat(momentJournee, momentSemaine, saison, lastUseWeightManager);

        Random random = new Random();
        int probaEntry = random.nextInt(100);
        Entree entree;

        if (probaEntry > 70){
            entree = EntreeFactory.getRandomEntree(momentJournee, momentSemaine, saison, lastUseWeightManager);
            lastUseWeightManager.addProduit(entree.getNom());
        }else {
            entree = null;
        }

        if (plat instanceof PlatComplet platComplet){
            lastUseWeightManager.addProduit(platComplet.getNom());

        }else if (plat instanceof PlatCompose platCompose){
            lastUseWeightManager.addProduit(platCompose.getViande().getViandeNom());
            lastUseWeightManager.addProduit(platCompose.getAccompagnement().getNomAccompagnement());
        }

        return new Repas(entree, plat, momentJournee);
    }
}
