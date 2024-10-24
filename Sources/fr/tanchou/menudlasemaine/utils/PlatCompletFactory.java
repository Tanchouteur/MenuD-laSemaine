package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.PlatCompletDAO;
import fr.tanchou.menudlasemaine.models.PlatComplet;

import java.util.List;
import java.util.Random;

public class PlatCompletFactory extends PlatFactory {
    private final List<PlatComplet> platsComplets;

    public PlatCompletFactory(PlatCompletDAO platCompletDAO) {
        this.platsComplets = platCompletDAO.getAllPlatsComplets();
    }

    public PlatComplet getRandomPlatComplet() {
        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }
        Random random = new Random();
        int randomIndex = random.nextInt(platsComplets.size());
        return platsComplets.get(randomIndex);
    }
}
