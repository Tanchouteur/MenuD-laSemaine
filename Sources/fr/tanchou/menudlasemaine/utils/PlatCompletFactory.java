package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.PlatComplet;

import java.util.List;
import java.util.Random;

public class PlatCompletFactory extends PlatFactory {
    private final List<PlatComplet> platsComplets;

    public PlatCompletFactory(List<PlatComplet> platsComplets) {
        this.platsComplets = platsComplets;
    }

    @Override
    public PlatComplet createPlat() {
        return getRandomPlatComplet();
    }

    private PlatComplet getRandomPlatComplet() {
        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }
        Random random = new Random();
        int randomIndex = random.nextInt(platsComplets.size());
        return platsComplets.get(randomIndex);
    }
}
