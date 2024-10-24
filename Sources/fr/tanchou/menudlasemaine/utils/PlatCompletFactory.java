package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.PlatCompletDAO;
import fr.tanchou.menudlasemaine.models.PlatComplet;

import java.util.List;
import java.util.Random;

public class PlatCompletFactory extends PlatFactory {

    public static PlatComplet getRandomPlatComplet() {
        List<PlatComplet> platsComplets = new PlatCompletDAO().getAllPlatsComplets();

        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }

        Random random = new Random();
        int randomIndex = random.nextInt(platsComplets.size());

        return platsComplets.get(randomIndex);
    }
}
