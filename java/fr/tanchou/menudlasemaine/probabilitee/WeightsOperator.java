package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.IncompatibilitesDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for handling operations related to product weights and compatibility.
 * <p>
 * This class provides static methods to compute product weights based on various criteria,
 * select compatible products, and handle incompatibilities between products.
 * </p>
 */
public class WeightsOperator {

    /**
     * Selects a product from the list based on their weights.
     *
     * @param produitsList The list of products to choose from.
     * @param moment       The moment of the day (e.g., lunch, dinner, etc.).
     * @param saison       The season of the year (e.g., spring, summer, etc.).
     * @return The selected product, or {@code null} if no product is selected.
     */
    public static Produits selectBasedOnWeights(List<Produits> produitsList, int moment, int saison) {


        int totalWeight = 0;
        for (Produits produit : produitsList) {
            // Calculer le poids de dernier utilisation
            computeLastUsedWeight(produit);

            // Calculer le poids final
            calculateWeight(produit, moment, saison);

            totalWeight += produit.getPoidsFinal();
        }

        if (totalWeight <= 0) {
            System.err.println("Total weight is zero or negative. No item can be selected. SelectBasedOnWeights() " + produitsList);
            return null;
        }

        Random random = new Random();
        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (Produits produit : produitsList) {

            // On avance le poids cumulé
            cumulativeWeight += produit.getPoidsFinal();

            // Si le poids cumulé dépasse le poids aléatoire, on retourne l'élément
            if (cumulativeWeight > randomWeight) {
                return produit;
            }
        }

        // Par sécurité, on retourne null si rien n'est trouvé
        System.err.println("Aucun élément sélectionné. Cumulative weight: " + cumulativeWeight);
        return null;
    }

    /**
     * Selects a compatible product based on the provided base product and desired type.
     *
     * @param produitOnBaseTheIncopatibilitie The base product to check incompatibilities against.
     * @param typeProduitWeWish               The type of product desired (e.g., vegetable, starch).
     * @param moment                          The moment of the day (e.g., lunch, dinner, etc.).
     * @param saison                          The season of the year (e.g., spring, summer, etc.).
     * @return A compatible product, or {@code null} if none is found.
     */
    public static Produits selectCompatibleProduct(Produits produitOnBaseTheIncopatibilitie, TypeProduit typeProduitWeWish, int moment, int saison){
        // On récupère la liste des produits compatibles
        LinkedList<Produits> compatibleProducts = IncompatibilitesDAO.getProduitsCompatibles(produitOnBaseTheIncopatibilitie.getId(), typeProduitWeWish);

        // On sélectionne un produit compatible
        return selectBasedOnWeights(compatibleProducts, moment, saison);
    }

    /**
     * Computes the final weight of a product based on moment, season, and other criteria.
     *
     * @param produits The product whose weight is to be calculated.
     * @param moment   The moment of the day (e.g., lunch, dinner, etc.).
     * @param saison   The season of the year (e.g., spring, summer, etc.).
     */
    public static void calculateWeight(Produits produits, int moment, int saison) {
        // Momment 0 = midiSemaine, 1 = soirSemaine, 2 = midiWeekend, 3 = soirWeekend
        // Saison 0 = printemps, 1 = été, 2 = automne, 3 = hiver

        // Vérification des cas éliminatoires
        if (produits.getPoidsLastUsed() == 0 || produits.getPoidsMoment()[moment] == 0 || produits.getPoidsSaison()[saison] == 0) {
            produits.setPoidsFinal(0); // Produit exclu
            return;
        }

        // Combinaison multiplicative
        produits.setPoidsFinal(produits.getPoidsMoment()[moment] *
                produits.getPoidsSaison()[saison] *
                produits.getPoidsArbitraire() *
                produits.getPoidsLastUsed());
    }

    /**
     * Computes the "last used" weight for a product based on its usage history.
     * <p>
     * Products used recently will have lower weights, while those unused for a long time
     * will have higher weights.
     * </p>
     *
     * @param produit The product for which the last used weight is to be calculated.
     */
    public static void computeLastUsedWeight(Produits produit) {

        // Récupérer la date de dernière utilisation
        LocalDate lastUsed = produit.getLastUsed();
        LocalDate today = LocalDate.now();

        if (lastUsed == null) {
            produit.setPoidsLastUsed(10);
            return;
        }

        // Calculer la différence en jours
        long daysSinceLastUsed = ChronoUnit.DAYS.between(lastUsed, today);

        // Si utilisé récemment (moins de 4 jours), poids = 0
        if (daysSinceLastUsed < 4) {
            produit.setPoidsLastUsed(0);
            return;
        }

        // Si utilisé il y a plus de 3 semaines, poids = 10
        if (daysSinceLastUsed > 21) {
            produit.setPoidsLastUsed(10);
            return;
        }

        // Calculer le poids proportionnel (entre 4 et 21 jours)
        // Echelle linéaire : (daysSinceLastUsed - 4) / (21 - 4) * 10
        double normalizedWeight = ((double) (daysSinceLastUsed - 4)) / (21 - 4) * 10;

        // Retourner l'entier arrondi
        produit.setPoidsLastUsed((int) Math.round(normalizedWeight));
    }
}
