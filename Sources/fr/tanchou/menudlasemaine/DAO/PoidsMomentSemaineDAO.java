package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.models.PoidsMomentSemaine;

import java.sql.*;

public class PoidsMomentSemaineDAO {

    public void ajouterPoidsMomentSemaine(PoidsMomentSemaine poidsMomentSemaine) {
        String sql = "INSERT INTO PoidsMomentSemaine (plat_id, semaine_weekend, poids) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poidsMomentSemaine.getPlat().getPlatId());
            pstmt.setString(2, poidsMomentSemaine.getMomentSemaine().name()); // Enum to String
            pstmt.setInt(3, poidsMomentSemaine.getPoids());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    poidsMomentSemaine.setPoidsId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PoidsMomentSemaine getPoidsMomentSemaineById(int poidsId) {
        String sql = "SELECT * FROM PoidsMomentSemaine WHERE poids_id = ?";
        PoidsMomentSemaine poidsMomentSemaine = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Plat plat = new PlatDAO().getPlatById(rs.getInt("plat_id"));
                MomentSemaine momentSemaine = MomentSemaine.valueOf(rs.getString("semaine_weekend")); // String to Enum
                poidsMomentSemaine = new PoidsMomentSemaine(poidsId, plat, momentSemaine, rs.getInt("poids"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return poidsMomentSemaine;
    }

    public void updatePoidsMomentSemaine(PoidsMomentSemaine poidsMomentSemaine) {
        String sql = "UPDATE PoidsMomentSemaine SET plat_id = ?, semaine_weekend = ?, poids = ? WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsMomentSemaine.getPlat().getPlatId());
            pstmt.setString(2, poidsMomentSemaine.getMomentSemaine().name()); // Enum to String
            pstmt.setInt(3, poidsMomentSemaine.getPoids());
            pstmt.setInt(4, poidsMomentSemaine.getPoidsId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePoidsMomentSemaine(int poidsId) {
        String sql = "DELETE FROM PoidsMomentSemaine WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
