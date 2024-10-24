package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.models.PoidsMomentJournee;

import java.sql.*;

public class PoidsMomentJourneeDAO {

    public void ajouterPoidsMomentJournee(PoidsMomentJournee poidsMomentJournee) {
        String sql = "INSERT INTO PoidsMomentJournee (plat_id, moment, poids) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poidsMomentJournee.getPlat().getPlatId());
            pstmt.setString(2, poidsMomentJournee.getMomentJournee().name()); // Enum to String
            pstmt.setInt(3, poidsMomentJournee.getPoids());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    poidsMomentJournee.setPoidsId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PoidsMomentJournee getPoidsMomentJourneeById(int poidsId) {
        String sql = "SELECT * FROM PoidsMomentJournee WHERE poids_id = ?";
        PoidsMomentJournee poidsMomentJournee = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int platId = rs.getInt("plat_id");
                Plat plat;

                // Déterminer le type de plat
                String typePlat = rs.getString("type_plat"); // Assurez-vous que cette colonne existe
                if (typePlat.equals("complet")) {
                    PlatCompletDAO platCompletDAO = new PlatCompletDAO();
                    plat = platCompletDAO.getPlatCompletById(platId);
                } else {
                    PlatComposeDAO platComposeDAO = new PlatComposeDAO();
                    plat = platComposeDAO.getPlatComposeById(platId);
                }

                MomentJournee momentJournee = MomentJournee.valueOf(rs.getString("moment"));
                poidsMomentJournee = new PoidsMomentJournee(poidsId, plat, momentJournee, rs.getInt("poids"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return poidsMomentJournee;
    }


    public void updatePoidsMomentJournee(PoidsMomentJournee poidsMomentJournee) {
        String sql = "UPDATE PoidsMomentJournee SET plat_id = ?, moment = ?, poids = ? WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsMomentJournee.getPlat().getPlatId());
            pstmt.setString(2, poidsMomentJournee.getMomentJournee().name()); // Enum to String
            pstmt.setInt(3, poidsMomentJournee.getPoids());
            pstmt.setInt(4, poidsMomentJournee.getPoidsId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePoidsMomentJournee(int poidsId) {
        String sql = "DELETE FROM PoidsMomentJournee WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
