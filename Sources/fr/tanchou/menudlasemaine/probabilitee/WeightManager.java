package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class WeightManager {
    // Calcule les poids pour tous les produits d'un type donné
    public abstract <T> Map<T, Integer> calculateWeights(Class<T> produitClass, TypeProduit typeProduit);

    // Méthode statique pour sélectionner un élément basé sur des poids
    public static <T> T selectBasedOnWeights(List<T> items, Map<T, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected.");
            return null;
        }

        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (T item : items) {
            cumulativeWeight += weights.getOrDefault(item, 0);
            if (cumulativeWeight > randomWeight) {
                return item;
            }
        }

        System.err.println("Aucun élément sélectionné " + cumulativeWeight);
        return null;
    }
}
