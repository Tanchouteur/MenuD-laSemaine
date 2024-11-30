package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.Map;
import java.util.Random;

public class ViandeFactory {

    public static Viande getRandomViande(MomentJournee momentJournee, MomentSemaine momentSemaine, LastUseWeightManager lastUseWeightManager) {
        Random random = new Random();

        ManuelWeightManager manuelWeightManager = new ManuelWeightManager();

        // Calcul des poids
        Map<Produits, Integer> manuelViandeWeights = manuelWeightManager.calculateWeights(TypeProduit.VIANDE);
        Map<Produits, Integer> lastUseViandeWeights = lastUseWeightManager.calculateWeights(TypeProduit.VIANDE);
        Map<Produits, Integer> combienedViandeWeights = WeightManager.combineWeights(lastUseViandeWeights, manuelViandeWeights);
        Map<Produits, Integer> multipliedViandeWeightsMoment = WeightManager.multiplyWeights(combienedViandeWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.VIANDE, momentJournee, momentSemaine));
        Map<Produits, Integer> multipliedViandeWeightsSaisons = WeightManager.multiplyWeights(multipliedViandeWeightsMoment, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.VIANDE, momentJournee, momentSemaine));

        // Sélectionner la viande en fonction des poids
        Produits selectedViande = WeightManager.selectBasedOnWeights(multipliedViandeWeightsSaisons, random, lastUseWeightManager.getDejaChoisis());

        return (Viande) selectedViande;
    }
}
