package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.PoidsFamille;

import java.sql.*;

public class PoidsFamilleDAO {

    public void ajouterPoidsFamille(PoidsFamille poidsFamille) {
        String sql = "INSERT INTO PoidsFamille (plat_id, membre_famille_id, poids) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poidsFamille.getPlat().getPlatId());
            pstmt.setInt(2, poidsFamille.getMembre());
            pstmt.setInt(3, poidsFamille.getPoids());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    poidsFamille.setPoidsFamilleId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PoidsFamille getPoidsFamilleById(int poidsFamilleId) {
        String sql = "SELECT * FROM PoidsFamille WHERE poids_id = ?";
        PoidsFamille poidsFamille = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsFamilleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Plat plat = new PlatDAO().getPlatById(rs.getInt("plat_id"));
                poidsFamille = new PoidsFamille(poidsFamilleId, plat, rs.getInt("membre"), rs.getInt("poids"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return poidsFamille;
    }

    public void updatePoidsFamille(PoidsFamille poidsFamille) {
        String sql = "UPDATE PoidsFamille SET plat_id = ?, membre_famille_id = ?, poids = ? WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsFamille.getPlat().getPlatId());
            pstmt.setInt(2, poidsFamille.getMembre());
            pstmt.setInt(3, poidsFamille.getPoids());
            pstmt.setInt(4, poidsFamille.getPoidsFamilleId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePoidsFamille(int poidsFamilleId) {
        String sql = "DELETE FROM PoidsFamille WHERE poids_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poidsFamilleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
