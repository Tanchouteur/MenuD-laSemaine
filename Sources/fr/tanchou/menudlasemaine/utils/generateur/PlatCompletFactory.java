package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.PlatCompletDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.util.List;
import java.util.Random;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlatCompletFactory {

    public static PlatComplet getRandomPlatComplet(MomentJournee momentJournee) {
        Random random = new Random();

        // Instanciation des DAO
        PlatCompletDAO platCompletDAO = new PlatCompletDAO();
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(produitLastUseDAO);

        // Calcul des poids pour les plats complets
        Map<PlatComplet, Integer> lastUsePlatCompletWeights = lastUseWeightManager.calculateWeights(PlatComplet.class, TypeProduit.PLAT_COMPLET);
        List<PlatComplet> platsComplets = platCompletDAO.getAllPlatsComplets();

        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }

        // Sélectionner le plat complet en fonction des poids
        PlatComplet selectedPlatComplet = selectBasedOnWeights(platsComplets, lastUsePlatCompletWeights, random);

        // Mettre à jour la date de dernière utilisation pour le plat complet sélectionné
        if (selectedPlatComplet != null) {
            produitLastUseDAO.updateLastUseDate(selectedPlatComplet.getNomPlat()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedPlatComplet;
    }

    private static <T> T selectBasedOnWeights(List<T> items, Map<T, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected.");
            return null; // Ou gérer ce cas d'une autre manière
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
        return null; // Si aucun élément n'est sélectionné
    }
}

