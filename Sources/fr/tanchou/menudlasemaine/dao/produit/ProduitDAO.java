package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.models.produit.*;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    // Méthode pour récupérer tous les produits d'un type donné avec leur dernière date d'utilisation
    public static <T> List<T> getAllProduits(Class<T> produitClass) {
        List<T> produits = new ArrayList<>();
        String query = "SELECT p.*, plu.last_used FROM " + produitClass.getSimpleName() + " p " +
                "LEFT JOIN ProduitLastUse plu ON p.nom_legume = plu.nom_produit " +
                "WHERE plu.type_produit = ?"; // Assurez-vous que le nom de la colonne est correct

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, produitClass.getSimpleName()); // Type de produit

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    T produit = createProduitFromResultSet(resultSet, produitClass);
                    produits.add(produit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }

        return produits;
    }

    // Méthode pour récupérer les poids d'un produit pour un moment de la journée
    public static int getPoids(String nomProduit, String momentJournee) {
        int poids = 0;
        String query = "SELECT poids FROM PoidsMomentJournee WHERE nom_produit = ? AND moment_journee = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, nomProduit);
            statement.setString(2, momentJournee);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                poids = resultSet.getInt("poids");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }

        return poids;
    }

    // Méthode pour créer un produit à partir d'un ResultSet
    private static <T> T createProduitFromResultSet(ResultSet resultSet, Class<T> produitClass) {
        try {
            LocalDate lastUsed = resultSet.getDate("last_used").toLocalDate(); // Récupérer la date de dernière utilisation
            if (produitClass == Legume.class) {
                return produitClass.cast(new Legume(resultSet.getString("nom_legume"), resultSet.getInt("poids"), lastUsed));
            } else if (produitClass == Viande.class) {
                return produitClass.cast(new Viande(resultSet.getString("nom_viande"), resultSet.getInt("poids"), lastUsed));
            } else if (produitClass == Feculent.class) {
                return produitClass.cast(new Feculent(resultSet.getString("nom_feculent"), resultSet.getInt("poids"), lastUsed));
            } else if (produitClass == PlatComplet.class) {
                return produitClass.cast(new PlatComplet(resultSet.getString("nom_plat_complet"), resultSet.getInt("poids"), lastUsed));
            } else if (produitClass == Entree.class) {
                return produitClass.cast(new Entree(resultSet.getString("nom_entree"), resultSet.getInt("poids"), lastUsed));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }
        return null; // Si aucun type ne correspond
    }
}