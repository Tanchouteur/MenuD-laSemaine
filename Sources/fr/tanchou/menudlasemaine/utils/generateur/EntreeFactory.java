package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsSaisonDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.models.produit.Legume;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntreeFactory {
    public static Entree getRandomEntree(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison) {
        Random random = new Random();

        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager();
        ManuelWeightManager manuelEntreeWeightManager = new ManuelWeightManager();

        Map<Entree, Integer> lastUseEntreeWeights = lastUseWeightManager.calculateWeights(Entree.class, TypeProduit.ENTREE);
        Map<Entree, Integer> manuelEntreeWeight = manuelEntreeWeightManager.calculateWeights(Entree.class, TypeProduit.ENTREE);
        Map<Entree, Integer> combinedEntreeWeights = WeightManager.combineWeights(lastUseEntreeWeights, manuelEntreeWeight);
        Map<Entree, Integer> multipliedEntreeWeightsMoment = WeightManager.multiplyWeights(combinedEntreeWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.ENTREE, momentJournee, momentSemaine));
        Map<Entree, Integer> multipliedEntreeWeightsSaisons = WeightManager.multiplyWeights(multipliedEntreeWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.ENTREE, saison));


        List<Entree> entrees = EntreeDAO.getAllEntrees();

        if (entrees.isEmpty()) {
            System.out.println("Aucune entrée trouvée");
            return null;
        }

        Entree selectedEntree = WeightManager.selectBasedOnWeights(entrees, multipliedEntreeWeightsSaisons, random);

        if (selectedEntree != null) {
            ProduitLastUseDAO.updateLastUseDate(selectedEntree.getNomEntree()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedEntree;
    }
}
