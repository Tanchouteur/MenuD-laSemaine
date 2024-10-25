package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.util.Map;

public abstract class WeightManager {
    // Calcule les poids pour tous les produits d'un type donné
    public abstract <T> Map<T, Integer> calculateWeights(Class<T> produitClass, TypeProduit typeProduit);
}
