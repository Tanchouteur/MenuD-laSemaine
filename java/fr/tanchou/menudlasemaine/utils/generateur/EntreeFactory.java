package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsSaisonDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.models.produit.Produits;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntreeFactory {

    public static Entree getRandomEntree(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison, LastUseWeightManager lastUseWeightManager) {
        Random random = new Random();

        ManuelWeightManager manuelEntreeWeightManager = new ManuelWeightManager();

        Map<Produits, Integer> lastUseEntreeWeights = lastUseWeightManager.calculateWeights(TypeProduit.ENTREE);
        Map<Produits, Integer> manuelEntreeWeight = manuelEntreeWeightManager.calculateWeights(TypeProduit.ENTREE);
        Map<Produits, Integer> combinedEntreeWeights = WeightManager.combineWeights(lastUseEntreeWeights, manuelEntreeWeight);
        Map<Produits, Integer> multipliedEntreeWeightsMoment = WeightManager.multiplyWeights(combinedEntreeWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.ENTREE, momentJournee, momentSemaine));
        Map<Produits, Integer> multipliedEntreeWeightsSaisons = WeightManager.multiplyWeights(multipliedEntreeWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.ENTREE, saison));

        Produits selectedEntree = WeightManager.selectBasedOnWeights(multipliedEntreeWeightsSaisons, random, lastUseWeightManager.getDejaChoisis());

        return (Entree) selectedEntree;
    }
}
