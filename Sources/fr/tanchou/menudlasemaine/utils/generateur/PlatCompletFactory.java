package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.PlatCompletDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;

import java.util.List;
import java.util.Random;

public class PlatCompletFactory {

    public static PlatComplet getRandomPlatComplet(MomentJournee momentJournee) {
        List<PlatComplet> platsComplets = new PlatCompletDAO().getAllPlatsComplets();

        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }

        Random random = new Random();
        int randomIndex = random.nextInt(platsComplets.size());

        return platsComplets.get(randomIndex);
    }
}
