package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Produits;

import java.util.Map;

public class SomeWeightManager {

    private final LastUseWeightManager lastUsedWeightManager;
    private final ManuelWeightManager manuelWeightManager;

    public SomeWeightManager() {
        lastUsedWeightManager = new LastUseWeightManager();
        manuelWeightManager = new ManuelWeightManager();
    }

    public Map<Produits, Integer> calculateCombinedWeights(TypeProduit typeProduit) {
        Map<Produits, Integer> lastUsedWeights = lastUsedWeightManager.calculateWeights(typeProduit);
        Map<Produits, Integer> manuelWeights = manuelWeightManager.calculateWeights(typeProduit);

        return WeightManager.combineWeights(lastUsedWeights, manuelWeights);
    }
}
