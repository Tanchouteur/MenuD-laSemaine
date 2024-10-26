package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntreeFactory {
    public static Entree getRandomEntree(MomentJournee momentJournee) {
        Random random = new Random();

        EntreeDAO entreeDAO = new EntreeDAO();
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();

        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(produitLastUseDAO);
        ManuelWeightManager manuelEntreeWeightManager = new ManuelWeightManager();

        Map<Entree, Integer> lastUseEntreeWeights = lastUseWeightManager.calculateWeights(Entree.class, TypeProduit.ENTREE);
        Map<Entree, Integer> manuelEntreeWeight = manuelEntreeWeightManager.calculateWeights(Entree.class, TypeProduit.ENTREE);

        List<Entree> entrees = entreeDAO.getAllEntrees();

        if (entrees.isEmpty()) {
            System.out.println("Aucune entrée trouvée");
            return null; // Ou gérer le cas comme souhaité
        }

        Entree selectedEntree = WeightManager.selectBasedOnWeights(entrees, WeightManager.combineWeights(manuelEntreeWeight, lastUseEntreeWeights), random);

        if (selectedEntree != null) {
            produitLastUseDAO.updateLastUseDate(selectedEntree.getNomEntree()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedEntree;
    }
}
