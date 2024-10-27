package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.util.Map;

public class SomeWeightManager {

    private final LastUseWeightManager lastUsedWeightManager;
    private final ManuelWeightManager manuelWeightManager;

    public SomeWeightManager() {
        lastUsedWeightManager = new LastUseWeightManager();
        manuelWeightManager = new ManuelWeightManager();
    }

    public <T> Map<T, Integer> calculateCombinedWeights(Class<T> produitClass, TypeProduit typeProduit) {
        Map<T, Integer> lastUsedWeights = lastUsedWeightManager.calculateWeights(produitClass, typeProduit);
        Map<T, Integer> manuelWeights = manuelWeightManager.calculateWeights(produitClass, typeProduit);

        return WeightManager.combineWeights(lastUsedWeights, manuelWeights);
    }
}
