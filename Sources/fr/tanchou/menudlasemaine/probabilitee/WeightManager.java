package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.util.HashMap;
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

    // Méthode pour combiner les poids de deux sources
    public static <T> Map<T, Integer> combineWeights(Map<T, Integer> weights1, Map<T, Integer> weights2) {
        Map<T, Integer> combinedWeights = new HashMap<>();

        // Ajouter les poids de la première map
        for (Map.Entry<T, Integer> entry : weights1.entrySet()) {
            combinedWeights.put(entry.getKey(), entry.getValue());
        }

        // Combiner avec les poids de la seconde map
        for (Map.Entry<T, Integer> entry : weights2.entrySet()) {
            T produit = entry.getKey();
            int poids2 = entry.getValue();

            // Ajouter le poids si le produit existe déjà, sinon l'ajouter directement
            combinedWeights.merge(produit, poids2, Integer::sum);
        }

        return combinedWeights;
    }

    // méthode pour multiplier les poids par leurs facteurs
    public static <T> Map<T, Integer> multiplyWeights(Map<T, Integer> weights, Map<T, Integer> weightsFactor) {
        Map<T, Integer> multipliedWeights = new HashMap<>();

        // Ajouter les poids de la première map
        multipliedWeights.putAll(weights);

        // Combiner avec les poids de la seconde map
        for (Map.Entry<T, Integer> entry : weightsFactor.entrySet()) {
            T produit = entry.getKey();
            int weight = entry.getValue();

            // multiplier le poids avec le facteur correspondant de la liste
            multipliedWeights.merge(produit, weight, (w1, w2) -> w1 * w2);

        }

        return multipliedWeights;
    }
}

