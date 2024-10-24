package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.PlatComplet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatCompletDAO {

    public void ajouterPlatComplet(PlatComplet platComplet) {
        String sql = "{CALL AjouterPlatComplet(?, ?)}"; // Utilisation de la procédure
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            // Remplir les paramètres de la procédure
            cstmt.setFloat(1, platComplet.getPoids());
            cstmt.setString(2, platComplet.getNomPlat()); // Supposons que tu as une méthode getNomPlat() dans PlatComplet

            // Exécuter la procédure
            cstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlatComplet getPlatCompletById(int platId) {
        String sql = "SELECT p.*, pc.nom_plat FROM Plat p JOIN PlatComplet pc ON p.plat_id = pc.plat_id WHERE p.plat_id = ?";
        PlatComplet platComplet = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                float poids = rs.getFloat("poids");
                String nomPlat = rs.getString("nom_plat");
                platComplet = new PlatComplet(poids, nomPlat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platComplet;
    }

    public List<PlatComplet> getAllPlatsComplets() {
        List<PlatComplet> platsComplets = new ArrayList<>();
        String sql = "SELECT p.*, pc.nom_plat FROM Plat p JOIN PlatComplet pc ON p.plat_id = pc.plat_id WHERE p.type_plat = 'complet'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                float poids = rs.getFloat("poids");
                String nomPlat = rs.getString("nom_plat");
                platsComplets.add(new PlatComplet(poids, nomPlat));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platsComplets;
    }

    public void updatePlatComplet(PlatComplet platComplet, int platId) {
        String sql = "UPDATE Plat SET type_plat = ?, poids = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platComplet.getTypePlat().name());
            pstmt.setFloat(2, platComplet.getPoids());
            pstmt.setInt(3, platId);
            pstmt.executeUpdate();

            updatePlatCompletDetails(platComplet, platId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlatCompletDetails(PlatComplet platComplet, int platId) {
        String sql = "UPDATE PlatComplet SET nom_plat = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platComplet.getNomPlat());
            pstmt.setInt(2, platId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlatComplet(int platId) {
        String sql = "DELETE FROM PlatComplet WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        deletePlat(platId);
    }

    private void deletePlat(int platId) {
        String sql = "DELETE FROM Plat WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
