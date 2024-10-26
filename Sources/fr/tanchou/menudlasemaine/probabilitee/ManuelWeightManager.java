package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.produit.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.produit.LegumeDAO;
import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.produit.PlatCompletDAO;
import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManuelWeightManager extends WeightManager {

    @Override
    public <T> Map<T, Integer> calculateWeights(Class<T> produitClass, TypeProduit typeProduit) {
        Map<T, Integer> weights = new HashMap<>();

        // Récupérer tous les produits du type spécifié
        List<T> produits = getAllProduits(produitClass); // Récupération des produits

        for (T produit : produits) {
            if (produit instanceof Poidsable) { // Vérifiez que le produit est Poidsable
                int poids = ((Poidsable) produit).getPoids(); // Récupérer le poids de chaque produit
                weights.put(produit, poids);
            }
        }

        return weights;
    }

    // Méthode pour récupérer tous les produits en fonction de leur type
    @SuppressWarnings("unchecked") // Éviter les avertissements de compilation pour le cast
    private <T> List<T> getAllProduits(Class<T> produitClass) {
        if (produitClass == Viande.class) {
            return (List<T>) ViandeDAO.getAllViandes();
        } else if (produitClass == Legume.class) {
            return (List<T>) LegumeDAO.getAllLegumes();
        } else if (produitClass == Feculent.class) {
            return (List<T>) FeculentDAO.getAllFeculents();
        } else if (produitClass == PlatComplet.class) {
            return (List<T>) PlatCompletDAO.getAllPlatsComplets();
        } else if (produitClass == Entree.class) {
            return (List<T>) EntreeDAO.getAllEntrees();
        }
        return new ArrayList<>(); // Retourner une liste vide si aucun type ne correspond
    }
}