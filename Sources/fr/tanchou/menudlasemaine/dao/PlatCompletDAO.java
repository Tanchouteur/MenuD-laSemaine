package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.PlatComplet;
import fr.tanchou.menudlasemaine.enums.TypePlat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatCompletDAO {

    public void ajouterPlatComplet(PlatComplet platComplet) {
        String sql = "INSERT INTO Plat (type_plat, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, platComplet.getTypePlat().name());
            pstmt.setFloat(2, platComplet.getPoids());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int platId = generatedKeys.getInt(1);
                    ajouterPlatCompletDetails(platId, platComplet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ajouterPlatCompletDetails(int platId, PlatComplet platComplet) {
        String sql = "INSERT INTO PlatComplet (plat_id, nom_plat) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            pstmt.setString(2, platComplet.getNomPlat());
            pstmt.executeUpdate();
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
                TypePlat typePlat = TypePlat.valueOf(rs.getString("type_plat"));
                float poids = rs.getFloat("poids");
                String nomPlat = rs.getString("nom_plat");
                platComplet = new PlatComplet(platId, typePlat, poids, nomPlat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platComplet;
    }

    public List<PlatComplet> getAllPlatsComplets() {
        List<PlatComplet> platsComplets = new ArrayList<>();
        String sql = "SELECT p.*, pc.nom_plat FROM Plat p JOIN PlatComplet pc ON p.plat_id = pc.plat_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int platId = rs.getInt("plat_id");
                TypePlat typePlat = TypePlat.valueOf(rs.getString("type_plat"));
                float poids = rs.getFloat("poids");
                String nomPlat = rs.getString("nom_plat");
                platsComplets.add(new PlatComplet(platId, typePlat, poids, nomPlat));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platsComplets;
    }

    public void updatePlatComplet(PlatComplet platComplet) {
        String sql = "UPDATE Plat SET type_plat = ?, poids = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platComplet.getTypePlat().name());
            pstmt.setFloat(2, platComplet.getPoids());
            pstmt.setInt(3, platComplet.getPlatId());
            pstmt.executeUpdate();

            updatePlatCompletDetails(platComplet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlatCompletDetails(PlatComplet platComplet) {
        String sql = "UPDATE PlatComplet SET nom_plat = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platComplet.getNomPlat());
            pstmt.setInt(2, platComplet.getPlatId());
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
