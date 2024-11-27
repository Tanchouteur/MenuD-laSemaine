package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseTempDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.produit.Entree;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LastUseWeightManager extends WeightManager {

    private final LinkedList<String> dejaChoisis;


    public LastUseWeightManager() {
        dejaChoisis = new LinkedList<>();
    }

    @Override
    public <T> Map<T, Integer> calculateWeights(Class<T> produitClass, TypeProduit typeProduit) {
        Map<T, LocalDate> lastUseDates = ProduitLastUseTempDAO.getLastUseDatesForType(produitClass, typeProduit);
        Map<T, Integer> weights = new HashMap<>();

        for (Map.Entry<T, LocalDate> entry : lastUseDates.entrySet()) {
            T produit = entry.getKey();
            LocalDate lastUseDate = entry.getValue();

            int poids;
            if (isProduitDejaChoisi(produit)) {
                poids = 0; // Poids nul pour les produits déjà choisis
            } else {
                poids = computeWeight(lastUseDate, typeProduit);
            }

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

    /**
     * Vérifie si un produit a déjà été choisi pour un autre repas.
     *
     * @param produit Le produit à vérifier.
     * @param <T>     Le type du produit.
     * @return true si le produit a déjà été choisi, sinon false.
     */
    private <T> boolean isProduitDejaChoisi(T produit) {
        String nomProduit = getNomProduit(produit);

        return dejaChoisis.contains(nomProduit);
    }

    /**
     * Récupère le nom d'un produit en utilisant la méthode `getNom`.
     * Assure que le produit a une méthode `getNom`.
     *
     * @param produit Le produit dont le nom est à récupérer.
     * @param <T>     Le type du produit.
     * @return Le nom du produit sous forme de chaîne.
     */
    private <T> String getNomProduit(T produit) {
        try {
            return (String) produit.getClass().getMethod("getNom").invoke(produit);
        } catch (Exception e) {
            throw new IllegalArgumentException("Le produit doit avoir une méthode getNom()", e);
        }
    }

    // Méthodes pour ajouter les noms des produits déjà choisis
    public void addProduit(String selectedproduitName) {
        dejaChoisis.add(selectedproduitName);
    }
}


