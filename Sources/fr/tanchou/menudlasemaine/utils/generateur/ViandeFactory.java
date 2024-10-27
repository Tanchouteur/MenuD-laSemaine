package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ViandeFactory {

    public static Viande getRandomViande(MomentJournee momentJournee, MomentSemaine momentSemaine) {
        Random random = new Random();

        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager();
        ManuelWeightManager manuelWeightManager = new ManuelWeightManager();

        // Calcul des poids
        Map<Viande, Integer> manuelViandeWeights = manuelWeightManager.calculateWeights(Viande.class, TypeProduit.VIANDE);
        Map<Viande, Integer> lastUseViandeWeights = lastUseWeightManager.calculateWeights(Viande.class, TypeProduit.VIANDE);
        Map<Viande, Integer> combienedViandeWeights = WeightManager.combineWeights(lastUseViandeWeights, manuelViandeWeights);
        Map<Viande, Integer> multipliedViandeWeightsMoment = WeightManager.multiplyWeights(combienedViandeWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.VIANDE, momentJournee, momentSemaine));
        Map<Viande, Integer> multipliedViandeWeightsSaisons = WeightManager.multiplyWeights(multipliedViandeWeightsMoment, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.VIANDE, momentJournee, momentSemaine));

        // Récupérer toutes les viandes
        List<Viande> viandes = ViandeDAO.getAllViandes();

        if (viandes.isEmpty()) {
            System.out.println("Aucune viande trouvée");
            return null;
        }

        // Sélectionner la viande en fonction des poids
        Viande selectedViande = WeightManager.selectBasedOnWeights(viandes, multipliedViandeWeightsSaisons, random);

        // Mettre à jour la date de dernière utilisation pour la viande sélectionnée
        if (selectedViande != null) {
            ProduitLastUseDAO.updateLastUseDate(selectedViande.getViandeNom()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedViande;
    }
}
