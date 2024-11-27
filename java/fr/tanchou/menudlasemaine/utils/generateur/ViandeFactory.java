package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Produits;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
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
        Produits selectedViande = WeightManager.selectBasedOnWeights(multipliedViandeWeightsSaisons, random);

        return (Viande) selectedViande;
    }
}
