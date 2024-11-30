package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.util.*;

public class ManuelWeightManager extends WeightManager {

    public ManuelWeightManager() {
        super();
    }

    @Override
    public Map<Produits, Integer> calculateWeights(TypeProduit typeProduit) {
        Map<Produits, Integer> weights = new HashMap<>();

        // Récupérer tous les produits du type spécifié
        List<Produits> produits = getAllProduits(typeProduit); // Récupération des produits

        for (Produits produit : produits) {

            int poids = produit.getPoids(); // Récupérer le poids de chaque produit
            weights.put(produit, poids);

        }

        return weights;
    }

    // Méthode pour récupérer tous les produits en fonction de leur type

    private List<Produits> getAllProduits(TypeProduit typeProduit) {
        if (typeProduit == TypeProduit.VIANDE) {
            return ViandeDAO.getAllViandes();
        } else if (typeProduit == TypeProduit.LEGUME) {
            return LegumeDAO.getAllLegumes();
        } else if (typeProduit == TypeProduit.FECULENT) {
            return FeculentDAO.getAllFeculents();
        } else if (typeProduit == TypeProduit.PLAT_COMPLET) {
            return PlatCompletDAO.getAllPlatsComplets();
        } else if (typeProduit == TypeProduit.ENTREE) {
            return EntreeDAO.getAllEntrees();
        }
        return new ArrayList<>();
    }
}