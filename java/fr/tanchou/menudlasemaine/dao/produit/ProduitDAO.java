package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
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
    public static List<Produits> getAllProduits(TypeProduit typeProduit) {
        List<Produits> produits = new ArrayList<>();
        String query = "SELECT p.*, plu.last_used FROM " + typeProduit.toString() + " p " +
                "LEFT JOIN ProduitLastUse plu ON p.nom_legume = plu.nom_produit " +
                "WHERE plu.type_produit = ?"; // Assurez-vous que le nom de la colonne est correct

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, typeProduit.toString()); // Type de produit

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Produits produit = createProduitFromResultSet(resultSet, typeProduit);
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

    public static List<Produits> getAllProduitByType(String typeProduit){ //TypeProduit est un enum
        List<Produits> produits = new ArrayList<>();
        String query = "SELECT p.*, plu.last_used FROM " + typeProduit + " p " +
                "LEFT JOIN ProduitLastUse plu ON p.nom_legume = plu.nom_produit " +
                "WHERE plu.type_produit = ?"; // Assurez-vous que le nom de la colonne est correct

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, typeProduit.toLowerCase()); // Type de produit

            switch (typeProduit){
                case "Legume":
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Produits produit = new Legume(resultSet.getString("nom_legume"), resultSet.getInt("poids"), resultSet.getDate("last_used").toLocalDate());
                            produits.add(produit);
                        }
                    }
                    break;
                case "VIANDE":
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Produits produit = new Viande(resultSet.getString("nom_viande"), resultSet.getInt("poids"), resultSet.getDate("last_used").toLocalDate());
                            produits.add(produit);
                        }
                    }
                    break;
                case "FECULENT":
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Produits produit = new Feculent(resultSet.getString("nom_feculent"), resultSet.getInt("poids"), resultSet.getDate("last_used").toLocalDate());
                            produits.add(produit);
                        }
                    }
                    break;
                case "PLAT_COMPLET":
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Produits produit = new PlatComplet(resultSet.getString("nom_plat_complet"), resultSet.getInt("poids"), resultSet.getDate("last_used").toLocalDate());
                            produits.add(produit);
                        }
                    }
                    break;
                case "Entree":
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Produits produit = new Entree(resultSet.getString("nom_entree"), resultSet.getInt("poids"), LocalDate.now());
                            produits.add(produit);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }

        return produits;
    }

    public static <T> T getProduitByName(String nom_produit, TypeProduit typeProduit){ //TypeProduit est un enum
        T produit = null;
        switch (typeProduit){
            case LEGUME:
                produit = (T) LegumeDAO.getLegumeByName(nom_produit);
                break;

            case VIANDE:
                produit = (T) ViandeDAO.getViandeByName(nom_produit);
                break;

            case FECULENT:
                produit = (T) FeculentDAO.getFeculentByName(nom_produit);
                break;

            case PLAT_COMPLET:
                produit = (T) PlatCompletDAO.getPlatCompletByName(nom_produit);
                break;

            case ENTREE:
                produit = (T) EntreeDAO.getEntreeByName(nom_produit);
                break;
        }

        return produit;
    }

    // Méthode pour créer un produit à partir d'un ResultSet
    private static Produits createProduitFromResultSet(ResultSet resultSet,TypeProduit typeProduit) {
        try {
            LocalDate lastUsed = resultSet.getDate("last_used").toLocalDate(); // Récupérer la date de dernière utilisation
            if (typeProduit == TypeProduit.LEGUME) {
                return new Legume(resultSet.getString("nom_legume"), resultSet.getInt("poids"), lastUsed);
            } else if (typeProduit == TypeProduit.VIANDE) {
                return new Viande(resultSet.getString("nom_viande"), resultSet.getInt("poids"), lastUsed);
            } else if (typeProduit == TypeProduit.FECULENT) {
                return new Feculent(resultSet.getString("nom_feculent"), resultSet.getInt("poids"), lastUsed);
            } else if (typeProduit == TypeProduit.PLAT_COMPLET) {
                return new PlatComplet(resultSet.getString("nom_plat_complet"), resultSet.getInt("poids"), lastUsed);
            } else if (typeProduit == TypeProduit.ENTREE) {
                return new Entree(resultSet.getString("nom_entree"), resultSet.getInt("poids"), lastUsed);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }
        return null; // Si aucun type ne correspond
    }
}