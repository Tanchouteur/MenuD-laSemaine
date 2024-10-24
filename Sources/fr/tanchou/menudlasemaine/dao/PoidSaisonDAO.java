package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.PoidSaison;
import fr.tanchou.menudlasemaine.enums.Saison;

import java.sql.*;

public class PoidSaisonDAO {

    // Ajouter un Poids de Saison
    public void ajouterPoidSaison(PoidSaison poidSaison) {
        String sql = "INSERT INTO PoidsSaison (plat_id, saison, poids) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poidSaison.getPlat().getPlatId());
            pstmt.setString(2, poidSaison.getSaison().name());
            pstmt.setInt(3, poidSaison.getPoid());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    poidSaison.setPoidId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du poids de saison : " + e.getMessage());
        }
    }

    public PoidSaison getPoidSaisonById(int poidId) {
        String sql = "SELECT * FROM PoidsSaison WHERE poids_id = ?";
        PoidSaison poidSaison = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int platId = rs.getInt("plat_id");
                Plat plat;

                // Utiliser une instance de PlatCompletDAO ou PlatComposeDAO pour récupérer le plat
                if (rs.getString("type_plat").equals("complet")) {
                    PlatCompletDAO platCompletDAO = new PlatCompletDAO();
                    plat = platCompletDAO.getPlatCompletById(platId);
                } else {
                    PlatComposeDAO platComposeDAO = new PlatComposeDAO();
                    plat = platComposeDAO.getPlatComposeById(platId);
                }

                Saison saison = Saison.valueOf(rs.getString("saison"));
                poidSaison = new PoidSaison(poidId, plat, saison, rs.getInt("poids"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du poids de saison : " + e.getMessage());
        }
        return poidSaison;
    }

    public void updatePoidSaison(PoidSaison poidSaison) {
        String sql = "UPDATE PoidsSaison SET plat_id = ?, saison = ?, poids = ? WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidSaison.getPlat().getPlatId());
            pstmt.setString(2, poidSaison.getSaison().name());
            pstmt.setInt(3, poidSaison.getPoid());
            pstmt.setInt(4, poidSaison.getPoidId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePoidSaison(int poidId) {
        String sql = "DELETE FROM PoidsSaison WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
