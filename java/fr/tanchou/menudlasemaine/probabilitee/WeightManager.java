package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.*;

import java.util.*;

public abstract class WeightManager {


    // Calcule les poids pour tous les produits d'un type donné
    public abstract Map<Produits, Integer> calculateWeights(TypeProduit typeProduit);

    // Méthode statique pour sélectionner un élément basé sur des poids
    public static Produits selectBasedOnWeights(Map<Produits, Integer> weights, Random random, LinkedList<String> dejaChoisis) {

        // Filtrer les produits pour mettre le poids à 0 s'ils ont déjà été choisis
        Map<Produits, Integer> filteredWeights = new HashMap<>();
        for (Map.Entry<Produits, Integer> entry : weights.entrySet()) {

            Produits produit = entry.getKey();
            int poids = dejaChoisis.contains(produit.getNom()) ? 0 : entry.getValue();
            filteredWeights.put(produit, poids);

        }

        int totalWeight = filteredWeights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected. " + filteredWeights.keySet());
            return null;
        }

        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (Map.Entry<Produits, Integer> entry : filteredWeights.entrySet()) {
            int itemWeight = entry.getValue();

            // On avance le poids cumulé
            cumulativeWeight += itemWeight;

            // Si le poids cumulé dépasse le poids aléatoire, on retourne l'élément
            if (cumulativeWeight > randomWeight) {
                Produits item = entry.getKey();

                // Ajout du produit à la liste des produits déjà choisis
                //dejaChoisis.add(item.getNom());

                item.setPoids(itemWeight); // Met à jour le poids si nécessaire
                return item;
            }
        }

        // Par sécurité, on retourne null si rien n'est trouvé
        System.err.println("Aucun élément sélectionné. Cumulative weight: " + cumulativeWeight);
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

    public static <T> Map<T, Integer> multiplyWeights(Map<T, Integer> weights, Map<T, Integer> weightsFactor) {
        Map<T, Integer> multipliedWeights = new HashMap<>();

        // Itère sur chaque produit dans la map des poids
        for (Map.Entry<T, Integer> entry : weights.entrySet()) {
            T produit = entry.getKey();
            int weight = entry.getValue();

            // Récupère le facteur de poids depuis weightsFactor, ou utilise 100 par défaut (aucun changement si absent)
            int factor = weightsFactor.getOrDefault(produit, 100);

            // Multiplie le poids avec le facteur et divise par 100 pour ajuster
            int adjustedWeight = (int) (weight * (factor / 100.0));
            multipliedWeights.put(produit, adjustedWeight);
        }

        return multipliedWeights;
    }




}

