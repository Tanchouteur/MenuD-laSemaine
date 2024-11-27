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
    public Map<Produits, Integer> getMomentWeights(TypeProduit TypeProduit, String momentJournee) {
        Map<Produits, Integer> momentWeights = new HashMap<>();

        // Récupère les produits du type spécifié avec leur dernière date d'utilisation
        List<Produits> produitsList = ProduitDAO.getAllProduits(TypeProduit);

        // Parcourir les produits et récupérer leur poids selon le moment de la journée
        for (Produits produit : produitsList) {
            String nomProduit = produit.getNom(); // Récupère le nom du produit
            int poidsMoment = PoidsMomentJourneeDAO.getPoids(nomProduit, momentJournee); // Récupérer le poids du moment

            // Si le poids est différent de 0, on l'ajoute à la map
            if (poidsMoment != 0) {
                momentWeights.put(produit, poidsMoment);
            }
        }

        return momentWeights;
    }
}
