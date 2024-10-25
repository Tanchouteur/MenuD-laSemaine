package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.incompatibilitedao.IncompatibilitesAccompagnementDAO;
import fr.tanchou.menudlasemaine.dao.produit.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.produit.LegumeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.produit.Feculent;
import fr.tanchou.menudlasemaine.models.produit.Legume;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccompagnementGenerator {

    public static Accompagnement generateAccompagnement(MomentJournee momentJournee) {
        Random random = new Random();

        IncompatibilitesAccompagnementDAO incompatibilitesAccompagnementDAO = new IncompatibilitesAccompagnementDAO(); // DAO pour gérer les incompatibilités

        // Calcul des poids
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(new ProduitLastUseDAO());
        Map<Legume, Integer> lastUseLegumeWeights = lastUseWeightManager.calculateWeights(Legume.class, TypeProduit.LEGUME);
        Map<Feculent, Integer> lastUseFeculentWeights = lastUseWeightManager.calculateWeights(Feculent.class, TypeProduit.FECULENT);

        List<Legume> legumes = LegumeDAO.getAllLegumes();
        List<Feculent> feculents = FeculentDAO.getAllFeculents();

        // Sélectionner le féculent en fonction des poids
        Feculent selectedFeculent = WeightManager.selectBasedOnWeights(feculents, lastUseFeculentWeights, random);

        // Sélectionner le légume compatible avec le féculent sélectionné
        Legume selectedLegume = selectCompatibleLegume(legumes, selectedFeculent, lastUseLegumeWeights, incompatibilitesAccompagnementDAO, random);

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

        if (nouvelAccompagnement.getLegume() != null) {
            ProduitLastUseDAO.updateLastUseDate(nouvelAccompagnement.getLegume().getLegumeNom());
        }
        if (nouvelAccompagnement.getFeculent() != null) {
            ProduitLastUseDAO.updateLastUseDate(nouvelAccompagnement.getFeculent().getFeculentNom());
        }

        return nouvelAccompagnement;
    }

    // Méthode pour sélectionner un légume compatible avec le féculent choisi
    private static Legume selectCompatibleLegume(List<Legume> legumes, Feculent feculent, Map<Legume, Integer> weights, IncompatibilitesAccompagnementDAO incompatibiliteDAO, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight for legumes is zero or negative. No item can be selected.");
            return null;
        }

        while (true) {
            int randomWeight = random.nextInt(totalWeight);
            int cumulativeWeight = 0;

            for (Legume legume : legumes) {
                cumulativeWeight += weights.getOrDefault(legume, 0);
                if (cumulativeWeight > randomWeight) {
                    // Vérifier la compatibilité
                    if (!IncompatibilitesAccompagnementDAO.areIncompatible(legume.getLegumeNom(), feculent.getFeculentNom())) {
                        return legume; // Retourner le légume compatible
                    }
                }
            }
            System.err.println("Aucun légume compatible trouvé avec les poids donnés.");
            return null;
        }
    }
}
