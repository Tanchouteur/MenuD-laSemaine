package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.PlatCompletDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsSaisonDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;
import fr.tanchou.menudlasemaine.models.produit.Produits;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Random;

import java.util.Map;

public class PlatCompletFactory {

    public static PlatComplet getRandomPlatComplet(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison, LastUseWeightManager lastUseWeightManager) {
        Random random = new Random();

        // Instanciation des DAO
        PlatCompletDAO platCompletDAO = new PlatCompletDAO();
        ManuelWeightManager manuelWeightManager = new ManuelWeightManager();

        // Calcul des poids pour les plats complets
        Map<Produits, Integer> lastUsePlatCompletWeights = lastUseWeightManager.calculateWeights(TypeProduit.PLAT_COMPLET);
        Map<Produits, Integer> manuelPlatCompletWeights = manuelWeightManager.calculateWeights(TypeProduit.PLAT_COMPLET);
        Map<Produits, Integer> combinedPlatCompletWeights = WeightManager.combineWeights(lastUsePlatCompletWeights, manuelPlatCompletWeights);
        Map<Produits, Integer> multipliedPlatCompletWeightsMoment = WeightManager.multiplyWeights(combinedPlatCompletWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.PLAT_COMPLET, momentJournee, momentSemaine));
        Map<Produits, Integer> multipliedPlatCompletWeightsSaisons = WeightManager.multiplyWeights(multipliedPlatCompletWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.PLAT_COMPLET, saison));

        // Sélectionner le plat complet en fonction des poids
        Produits selectedPlatComplet = WeightManager.selectBasedOnWeights(multipliedPlatCompletWeightsSaisons, random);

        return (PlatComplet) selectedPlatComplet;
    }
}

