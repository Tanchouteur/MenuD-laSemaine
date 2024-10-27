package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.produit.ProduitDAO;
import fr.tanchou.menudlasemaine.dao.weight.PoidsMomentJourneeDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MomentJourneeWeightManager {

    // Méthode pour récupérer les poids selon le moment de la journée
    public <T> Map<T, Integer> getMomentWeights(Class<T> produitClass, TypeProduit typeProduit, String momentJournee) {
        Map<T, Integer> momentWeights = new HashMap<>();

        // Récupère les produits du type spécifié avec leur dernière date d'utilisation
        List<T> produits = ProduitDAO.getAllProduits(produitClass);

        // Parcourir les produits et récupérer leur poids selon le moment de la journée
        for (T produit : produits) {
            String nomProduit = getNomProduit(produit); // Récupère le nom du produit
            int poidsMoment = PoidsMomentJourneeDAO.getPoids(nomProduit, momentJournee); // Récupérer le poids du moment

            // Si le poids est différent de 0, on l'ajoute à la map
            if (poidsMoment != 0) {
                momentWeights.put(produit, poidsMoment);
            }
        }

        return momentWeights;
    }

    // Méthode pour récupérer le nom du produit (implémentation à adapter selon ton modèle)
    private <T> String getNomProduit(T produit) {
        if (produit instanceof Legume) {
            return ((Legume) produit).getLegumeNom(); // Assurez-vous d'avoir une méthode getNomLegume()
        } else if (produit instanceof Viande) {
            return ((Viande) produit).getViandeNom(); // Assurez-vous d'avoir une méthode getNomViande()
        } else if (produit instanceof Feculent) {
            return ((Feculent) produit).getFeculentNom(); // Assurez-vous d'avoir une méthode getNomFeculent()
        } else if (produit instanceof PlatComplet) {
            return ((PlatComplet) produit).getNomPlat(); // Assurez-vous d'avoir une méthode getNomPlatComplet()
        } else if (produit instanceof Entree) {
            return ((Entree) produit).getNomEntree(); // Assurez-vous d'avoir une méthode getNomEntree()
        }
        return null; // Si aucun type ne correspond
    }
}
