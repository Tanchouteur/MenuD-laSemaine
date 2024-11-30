package fr.tanchou.menudlasemaine.dao.weight;

import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PoidsSaisonDAO {

    // Méthode pour ajouter ou mettre à jour le poids d'un produit en fonction de la saison
    public static void saveOrUpdateWeight(String nomProduit, Saison saison, int poids) {
        String sql = "REPLACE INTO PoidsSaison (nom_produit, saison, poid) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nomProduit);
            statement.setString(2, saison.name());
            statement.setInt(3, poids);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer le poids d'un produit pour une saison donnée
    public static Integer getWeightBySeason(String nomProduit, Saison saison) {
        String sql = "SELECT poid FROM PoidsSaison WHERE nom_produit = ? AND saison = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nomProduit);
            statement.setString(2, saison.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("poid");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Map<T, Integer> getAllWeightByTypeAndSeason(TypeProduit typeProduit, Saison saison) {
        Map<T, Integer> weights = new HashMap<>();

        String sql = "SELECT nom_produit, poid FROM PoidsSaison WHERE type_produit = ? AND saison = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, typeProduit.name());
            statement.setString(2, saison.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String nomProduit = resultSet.getString("nom_produit");
                    int poids = resultSet.getInt("poid");

                    T produit = ProduitDAO.getProduitByName(nomProduit, typeProduit);
                    if (produit != null) {
                        weights.put(produit, poids);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weights;
    }

}
