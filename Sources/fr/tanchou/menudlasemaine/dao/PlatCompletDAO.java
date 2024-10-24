package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.PlatComplet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatCompletDAO {

    public void ajouterPlatComplet(PlatComplet platComplet) {
        String sql = "INSERT INTO PlatComplet (nom_plat,poids) VALUES ('? , ?')"; // Utilisation de la procédure
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            // Remplir les paramètres de la procédure
            cstmt.setInt(1, platComplet.getPoids());
            cstmt.setString(2, platComplet.getNomPlat()); // Supposons que tu as une méthode getNomPlat() dans PlatComplet

            // Exécuter la procédure
            cstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlatComplet> getAllPlatsComplets() {
        List<PlatComplet> platsComplets = new ArrayList<>();
        String sql = "SELECT pc.* FROM PlatComplet pc";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int poids = rs.getInt("poids");
                String nomPlat = rs.getString("nom_plat");
                platsComplets.add(new PlatComplet(poids, nomPlat));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platsComplets;
    }

    public void updatePlatComplet(PlatComplet platComplet, String nomPlat) {
        String sql = "UPDATE PlatComplet SET nom_plat = ?, poids = ? WHERE nom_plat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platComplet.getTypePlat().name());
            pstmt.setFloat(2, platComplet.getPoids());
            pstmt.setString(3, nomPlat);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlatComplet(String nomPlat) {
        String sql = "DELETE FROM PlatComplet WHERE nom_plat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomPlat);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
