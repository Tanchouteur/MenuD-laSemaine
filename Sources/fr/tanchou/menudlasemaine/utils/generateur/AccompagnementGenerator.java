package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.produit.LegumeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.produit.Feculent;
import fr.tanchou.menudlasemaine.models.produit.Legume;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccompagnementGenerator {
    public static Accompagnement generateAccompagnement(MomentJournee momentJournee) {
        Random random = new Random();

        // Instanciation des DAO
        LegumeDAO legumeDAO = new LegumeDAO();
        FeculentDAO feculentDAO = new FeculentDAO();

        // Calcul des poids
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(new ProduitLastUseDAO());
        Map<Legume, Integer> lastUseLegumeWeights = lastUseWeightManager.calculateWeights(Legume.class, TypeProduit.LEGUME);
        Map<Feculent, Integer> lastUseFeculentWeights = lastUseWeightManager.calculateWeights(Feculent.class, TypeProduit.FECULENT);

        List<Legume> legumes = legumeDAO.getAllLegumes();
        List<Feculent> feculents = feculentDAO.getAllFeculents();

        // Sélectionner le légume en fonction des poids
        Legume selectedLegume = selectBasedOnWeights(legumes, lastUseLegumeWeights, random);
        Feculent selectedFeculent = selectBasedOnWeights(feculents, lastUseFeculentWeights, random);

        // Logique pour déterminer si l'accompagnement est vide
        int probaOneEmpty = random.nextInt(100);
        Accompagnement nouvelAccompagnement;

        if (probaOneEmpty > 70) {
            if (random.nextBoolean()) { // Légume vide
                nouvelAccompagnement = new Accompagnement(selectedFeculent);
            } else { // Féculent vide
                nouvelAccompagnement = new Accompagnement(selectedLegume);
            }
        } else {
            nouvelAccompagnement = new Accompagnement(selectedLegume, selectedFeculent);
        }

        return nouvelAccompagnement;
    }

    private static <T> T selectBasedOnWeights(List<T> items, Map<T, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected.");
            return null; // Ou gérer ce cas d'une autre manière
        }

        int randomWeight = random.nextInt(totalWeight);
        //System.out.println("Random Weight: " + randomWeight);

        int cumulativeWeight = 0;
        for (T item : items) {
            cumulativeWeight += weights.getOrDefault(item, 0);
            //System.out.println("Item: " + item.hashCode() + ", Cumulative Weight: " + cumulativeWeight);
            if (cumulativeWeight > randomWeight) {
                //System.out.println("Item non null : " + items.indexOf(item) + " : " + item.getClass());
                return item;
            }else if (item == null){
                //System.out.println("Item null : " + items.indexOf(item));
            }
        }
        System.err.println("aucun élément selectionner " + cumulativeWeight);
        return null; // Si aucun élément n'est sélectionné
    }

}