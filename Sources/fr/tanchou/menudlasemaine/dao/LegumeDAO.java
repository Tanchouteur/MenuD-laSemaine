package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Legume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LegumeDAO {

    // Méthode pour ajouter un nouveau légume
    public void ajouterLegume(Legume legume) {
        String sql = "INSERT INTO Legume (nom_legume) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, legume.getLegumeNom());
            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    legume.setLegumeId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer un légume par ID
    public Legume getLegumeById(int legumeId) {
        String sql = "SELECT * FROM Legume WHERE legume_id = ?";
        Legume legume = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, legumeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                legume = new Legume(
                        rs.getInt("legume_id"),
                        rs.getString("nom_legume")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legume;
    }

    // Méthode pour récupérer tous les légumes
    public List<Legume> getAllLegumes() {
        List<Legume> legumes = new ArrayList<>();
        String sql = "SELECT * FROM Legume";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                legumes.add(new Legume(
                        rs.getInt("legume_id"),
                        rs.getString("nom_legume")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legumes;
    }

    // Méthode pour mettre à jour un légume
    public void updateLegume(Legume legume) {
        String sql = "UPDATE Legume SET nom_legume = ? WHERE legume_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legume.getLegumeNom());
            pstmt.setInt(2, legume.getLegumeId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour supprimer un légume
    public void deleteLegume(int legumeId) {
        String sql = "DELETE FROM Legume WHERE legume_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, legumeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
