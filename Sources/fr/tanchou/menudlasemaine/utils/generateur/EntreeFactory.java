package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntreeFactory {
    public static Entree getRandomEntree(MomentJournee momentJournee) {
        Random random = new Random();

        EntreeDAO entreeDAO = new EntreeDAO();
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(produitLastUseDAO);

        Map<Entree, Integer> lastUseEntreeWeights = lastUseWeightManager.calculateWeights(Entree.class, TypeProduit.ENTREE);
        List<Entree> entrees = entreeDAO.getAllEntrees();

        if (entrees.isEmpty()) {
            System.out.println("Aucune entrée trouvée");
            return null; // Ou gérer le cas comme souhaité
        }

        Entree selectedEntree = selectBasedOnWeights(entrees, lastUseEntreeWeights, random);

        if (selectedEntree != null) {
            produitLastUseDAO.updateLastUseDate(selectedEntree.getNomEntree()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedEntree;
    }

    private static <T> T selectBasedOnWeights(List<T> items, Map<T, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Le poids total est zéro ou négatif. Aucun élément ne peut être sélectionné.");
            return null;
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
        return null;
    }
}
