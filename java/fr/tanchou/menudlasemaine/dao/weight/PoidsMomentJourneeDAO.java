package fr.tanchou.menudlasemaine.dao.weight;

import fr.tanchou.menudlasemaine.dao.produit.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PoidsMomentJourneeDAO {

    // Méthode pour récupérer le poids d'un produit selon le moment de la journée
    public static int getPoids(String nomProduit, String momentJournee) {
        int poids = 0;
        String sql = "SELECT poids FROM PoidsMomentJournee WHERE nom_produit = ? AND moment_journee = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, nomProduit);
            preparedStatement.setString(2, momentJournee);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                poids = resultSet.getInt("poids");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return poids;
    }

    public static <T> Map<T, Integer> getAllWeightByTypeAndMoment(TypeProduit typeProduit, MomentJournee momentJournee, MomentSemaine momentSemaine) {
        Map<T, Integer> weights = new HashMap<>();

        String sql = "SELECT nom_produit, poids FROM PoidsMomentJournee WHERE type_produit = ? AND moment_journee = ? AND moment_semaine = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, typeProduit.name());
            statement.setString(2, momentJournee.name());
            statement.setString(3, momentSemaine.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String nomProduit = resultSet.getString("nom_produit");
                    int poids = resultSet.getInt("poids");

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


    public static <T> Map<T, Integer> getAllWeightByTypeAndMomentSemaine(TypeProduit typeProduit, MomentSemaine momentSemaine) {
        Map<T, Integer> weights = new HashMap<>();

        String sql = "SELECT nom_produit, poids, moment_journee FROM PoidsMomentJournee WHERE type_produit = ? AND moment_semaine = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Définir les paramètres de la requête
            statement.setString(1, typeProduit.name());
            statement.setString(2, String.valueOf(momentSemaine));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String nomProduit = resultSet.getString("nom_produit");
                    int poids = resultSet.getInt("poids");

                    // Récupérer le produit par son nom
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
