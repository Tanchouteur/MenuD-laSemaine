package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.IncompatibilitesDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Random;

public class WeightsOperator {

    // Méthode statique pour sélectionner un élément basé sur des poids
    public static Produits selectBasedOnWeights(LinkedList<Produits> produitsList, int moment, int saison) {

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

    // Méthode pour slelectionner un élément basé sur l'incompatibilité
    public static Produits selectCompatibleProduct(Produits produitOnBaseTheIncopatibilitie, TypeProduit typeProduitWeWish, int moment, int saison){
        // On récupère la liste des produits compatibles
        LinkedList<Produits> compatibleProducts = IncompatibilitesDAO.getProduitsCompatibles(produitOnBaseTheIncopatibilitie.getId(), typeProduitWeWish);

        // On sélectionne un produit compatible
        return selectBasedOnWeights(compatibleProducts, moment, saison);
    }

    // Méthode pour calculer le poids final d'un produit
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

    // Calcul du poids de dernier utilisation
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
