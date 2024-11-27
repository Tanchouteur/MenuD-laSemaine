package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Produits;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LastUseWeightManager extends WeightManager {

    @Override
    public Map<Produits, Integer> calculateWeights(TypeProduit typeProduit) {
        Map<Produits, LocalDate> lastUseDates = ProduitLastUseDAO.getLastUseDatesForType(typeProduit);
        Map<Produits, Integer> weights = new HashMap<>();

        for (Map.Entry<Produits, LocalDate> entry : lastUseDates.entrySet()) {
            Produits produit = entry.getKey();
            LocalDate lastUseDate = entry.getValue();

            int poids = computeWeight(lastUseDate, typeProduit);

            weights.put(produit, poids);
        }
        return weights;
    }

    private int computeWeight(LocalDate lastUseDate, TypeProduit typeProduit) {
        if (lastUseDate == null) {
            return Integer.MAX_VALUE; // Poids par défaut si jamais utilisé
        }

        long daysSinceLastUse = ChronoUnit.DAYS.between(lastUseDate, LocalDate.now());

        // Ajustement du poids en fonction du type de produit
        if (daysSinceLastUse < ProduitLimite.getViandePoidsLimite(typeProduit)) { // Limite de jours pour poids faible
            return ProduitLimite.getPoidsFaible(typeProduit); // Utilise la méthode pour obtenir le poids faible
        }

        return (int) Math.min(daysSinceLastUse - 3, Integer.MAX_VALUE); // Limiter pour éviter un poids trop élevé
    }


}


