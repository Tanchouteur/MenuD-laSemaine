package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.PlatCompletDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsSaisonDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Legume;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Random;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlatCompletFactory {

    public static PlatComplet getRandomPlatComplet(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison) {
        Random random = new Random();

        // Instanciation des DAO
        PlatCompletDAO platCompletDAO = new PlatCompletDAO();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager();
        ManuelWeightManager manuelWeightManager = new ManuelWeightManager();

        // Calcul des poids pour les plats complets
        Map<PlatComplet, Integer> lastUsePlatCompletWeights = lastUseWeightManager.calculateWeights(PlatComplet.class, TypeProduit.PLAT_COMPLET);
        Map<PlatComplet, Integer> manuelPlatCompletWeights = manuelWeightManager.calculateWeights(PlatComplet.class, TypeProduit.PLAT_COMPLET);
        Map<PlatComplet, Integer> combinedPlatCompletWeights = WeightManager.combineWeights(lastUsePlatCompletWeights, manuelPlatCompletWeights);
        Map<PlatComplet, Integer> multipliedPlatCompletWeightsMoment = WeightManager.multiplyWeights(combinedPlatCompletWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.PLAT_COMPLET, momentJournee, momentSemaine));
        Map<PlatComplet, Integer> multipliedPlatCompletWeightsSaisons = WeightManager.multiplyWeights(multipliedPlatCompletWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.PLAT_COMPLET, saison));

        // Récupérer tous les plats complets
        List<PlatComplet> platsComplets = platCompletDAO.getAllPlatsComplets();

        if (platsComplets.isEmpty()) {
            throw new IllegalStateException("La liste de plats complets est vide.");
        }

        // Sélectionner le plat complet en fonction des poids
        PlatComplet selectedPlatComplet = WeightManager.selectBasedOnWeights(platsComplets, multipliedPlatCompletWeightsSaisons, random);

        // Mettre à jour la date de dernière utilisation pour le plat complet sélectionné
        if (selectedPlatComplet != null) {
            ProduitLastUseDAO.updateLastUseDate(selectedPlatComplet.getNomPlat()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedPlatComplet;
    }
}

