package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.incompatibilitedao.IncompatibilitesAccompagnementDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsSaisonDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Accompagnement;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.ManuelWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.Map;
import java.util.Random;

public class AccompagnementGenerator {

    public static Accompagnement generateAccompagnement(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison, LastUseWeightManager lastUseWeightManager) {
        Random random = new Random();

        // Calcul des poids
        ManuelWeightManager manuelWeightManager = new ManuelWeightManager();

        // Calcul des poids pour les légumes
        Map<Produits, Integer> manuelLegumeWeights = manuelWeightManager.calculateWeights(TypeProduit.LEGUME);
        Map<Produits, Integer> lastUseLegumeWeights = lastUseWeightManager.calculateWeights(TypeProduit.LEGUME);
        Map<Produits, Integer> combinedLegumeWeights = WeightManager.combineWeights(lastUseLegumeWeights, manuelLegumeWeights);
        Map<Produits, Integer> multipliedLegumeWeightsMoment = WeightManager.multiplyWeights(combinedLegumeWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.LEGUME, momentJournee, momentSemaine));

        Map<Produits, Integer> multipliedLegumeWeightsSaisons = WeightManager.multiplyWeights(multipliedLegumeWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.LEGUME ,saison));

        // Calcul des poids pour les féculents
        Map<Produits, Integer> manuelFeculentWeights = manuelWeightManager.calculateWeights(TypeProduit.FECULENT);
        Map<Produits, Integer> lastUseFeculentWeights = lastUseWeightManager.calculateWeights(TypeProduit.FECULENT);
        Map<Produits, Integer> combinedFeculentWeights = WeightManager.combineWeights(lastUseFeculentWeights, manuelFeculentWeights);
        Map<Produits, Integer> multipliedFeculentWeightsMoment = WeightManager.multiplyWeights(combinedFeculentWeights, PoidsMomentJourneeDAO.getAllWeightByTypeAndMoment(TypeProduit.FECULENT, momentJournee, momentSemaine));

        Map<Produits, Integer> multipliedFeculentWeightsSaisons = WeightManager.multiplyWeights(multipliedFeculentWeightsMoment, PoidsSaisonDAO.getAllWeightByTypeAndSeason(TypeProduit.FECULENT, saison));

        // Sélectionner le féculent
        Feculent selectedFeculent = (Feculent) WeightManager.selectBasedOnWeights(multipliedFeculentWeightsSaisons, random, lastUseWeightManager.getDejaChoisis());

        // Sélectionner le légume compatible avec le féculent sélectionné
        Legume selectedLegume = selectCompatibleLegume(selectedFeculent, multipliedLegumeWeightsSaisons, random);

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

    // Méthode pour sélectionner un légume compatible avec le féculent choisi
    private static Legume selectCompatibleLegume(Feculent feculent, Map<Produits, Integer> weights, Random random) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            System.err.println("Total weight for legumes is zero or negative. No item can be selected.");
            return null;
        }

        while (true) {
            int randomWeight = random.nextInt(totalWeight);
            int cumulativeWeight = 0;

            for (Map.Entry<Produits, Integer> entry : weights.entrySet()) {
                Produits legume = entry.getKey();
                int weight = entry.getValue();
                cumulativeWeight += weight;

                if (cumulativeWeight > randomWeight) {
                    // Vérifier la compatibilité
                    if (!IncompatibilitesAccompagnementDAO.areIncompatible(legume.getNom(), feculent.getNom())) {
                        // Retourner le légume compatible
                        //System.out.println("Légume sélectionné : " + legume + " avec un poids de " + weight);
                        return (Legume) legume;
                    }
                }
            }

            // Si aucun légume compatible n'est trouvé, on retourne null
            System.err.println("Aucun légume compatible trouvé avec les poids donnés.");
            return null;
        }
    }

}
