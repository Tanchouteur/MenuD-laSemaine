package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.PlatCompose;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlatComposeFactory {
    public static Plat getRandomPlatCompose(MomentJournee momentJournee) {
        Random random = new Random();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(new ProduitLastUseDAO());

        Map<Viande, Integer> lastUseViandeWeights = lastUseWeightManager.calculateWeights(Viande.class, TypeProduit.VIANDE);
        List<Viande> viandes = ViandeDAO.getAllViandes();

        Viande selectedViande = selectBasedOnWeights(viandes, lastUseViandeWeights, random);

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement(momentJournee);

        if (selectedViande != null) {
            ProduitLastUseDAO.updateLastUseDate(selectedViande.getViandeNom());
        }

        return new PlatCompose(1, selectedViande, randomAccompagnement);
    }

    private static <T> T selectBasedOnWeights(List<T> items, Map<T, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected.");
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
