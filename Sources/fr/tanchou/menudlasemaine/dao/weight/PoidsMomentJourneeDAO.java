package fr.tanchou.menudlasemaine.dao.weight;

import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
