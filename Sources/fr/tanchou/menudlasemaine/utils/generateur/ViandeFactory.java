package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ViandeFactory {
    public static Viande getRandomViande(MomentJournee momentJournee) {
        Random random = new Random();

        // Instanciation des DAO
        ViandeDAO viandeDAO = new ViandeDAO();
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(produitLastUseDAO);

        // Calcul des poids pour les viandes
        Map<Viande, Integer> lastUseViandeWeights = lastUseWeightManager.calculateWeights(Viande.class, TypeProduit.VIANDE);
        List<Viande> viandes = viandeDAO.getAllViandes();

        if (viandes.isEmpty()) {
            System.out.println("Aucune viande trouvée");
            return null; // Ou gérer le cas comme souhaité
        }

        // Sélectionner la viande en fonction des poids
        Viande selectedViande = WeightManager.selectBasedOnWeights(viandes, lastUseViandeWeights, random);

        // Mettre à jour la date de dernière utilisation pour la viande sélectionnée
        if (selectedViande != null) {
            produitLastUseDAO.updateLastUseDate(selectedViande.getViandeNom()); // Assurez-vous d'avoir une méthode pour obtenir le nom
        }

        return selectedViande;
    }
}
