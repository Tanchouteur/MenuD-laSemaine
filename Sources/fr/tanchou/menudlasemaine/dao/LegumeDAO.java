package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.models.Feculent;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                        rs.getString("nom_legume")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legumes;
    }

    public Legume getLegumeByName(String legumeName) {
        String sql = "SELECT * FROM Legume WHERE nom_legume = ?";
        Legume legume = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legumeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                legume = new Legume(
                        rs.getString("nom_feculent")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legume;
    }

    // Méthode pour mettre à jour un légume
    public void updateLegume(Legume legume, String newLegumeName) {
        String sql = "UPDATE Legume SET nom_legume = ? WHERE nom_legume = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legume.getLegumeNom());
            pstmt.setString(2, newLegumeName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour supprimer un légume
    public void deleteLegume(String legumeNom) {
        String sql = "DELETE FROM Legume WHERE nom_legume = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legumeNom);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
