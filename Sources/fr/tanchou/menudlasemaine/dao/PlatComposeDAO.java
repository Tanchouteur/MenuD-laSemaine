package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.PlatCompose;
import fr.tanchou.menudlasemaine.models.Viande;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.enums.TypePlat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatComposeDAO {

    public void ajouterPlatCompose(PlatCompose platCompose) {
        String sql = "INSERT INTO Plat (type_plat, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, platCompose.getTypePlat().name());
            pstmt.setFloat(2, platCompose.getPoids());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int platId = generatedKeys.getInt(1);
                    ajouterPlatComposeDetails(platId, platCompose);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ajouterPlatComposeDetails(int platId, PlatCompose platCompose) {
        String sql = "INSERT INTO PlatCompose (plat_id, viande_id, accompagnement_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            pstmt.setInt(2, platCompose.getViande().getViandeId());
            pstmt.setInt(3, platCompose.getAccompagnement().getAccompagnementId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlatCompose getPlatComposeById(int platId) {
        String sql = "SELECT p.*, pc.viande_id, pc.accompagnement_id FROM Plat p JOIN PlatCompose pc ON p.plat_id = pc.plat_id WHERE p.plat_id = ?";
        PlatCompose platCompose = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                TypePlat typePlat = TypePlat.valueOf(rs.getString("type_plat"));
                float poids = rs.getFloat("poids");
                Viande viande = new ViandeDAO().getViandeById(rs.getInt("viande_id"));
                Accompagnement accompagnement = new AccompagnementDAO().getAccompagnementById(rs.getInt("accompagnement_id"));
                platCompose = new PlatCompose(platId, typePlat, poids, viande, accompagnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platCompose;
    }

    public List<PlatCompose> getAllPlatsComposes() {
        List<PlatCompose> platsComposes = new ArrayList<>();
        String sql = "SELECT p.*, pc.viande_id, pc.accompagnement_id FROM Plat p JOIN PlatCompose pc ON p.plat_id = pc.plat_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int platId = rs.getInt("plat_id");
                TypePlat typePlat = TypePlat.valueOf(rs.getString("type_plat"));
                float poids = rs.getFloat("poids");
                Viande viande = new ViandeDAO().getViandeById(rs.getInt("viande_id"));
                Accompagnement accompagnement = new AccompagnementDAO().getAccompagnementById(rs.getInt("accompagnement_id"));
                platsComposes.add(new PlatCompose(platId, typePlat, poids, viande, accompagnement));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platsComposes;
    }

    public void updatePlatCompose(PlatCompose platCompose) {
        String sql = "UPDATE Plat SET type_plat = ?, poids = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platCompose.getTypePlat().name());
            pstmt.setFloat(2, platCompose.getPoids());
            pstmt.setInt(3, platCompose.getPlatId());
            pstmt.executeUpdate();

            updatePlatComposeDetails(platCompose);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlatComposeDetails(PlatCompose platCompose) {
        String sql = "UPDATE PlatCompose SET viande_id = ?, accompagnement_id = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platCompose.getViande().getViandeId());
            pstmt.setInt(2, platCompose.getAccompagnement().getAccompagnementId());
            pstmt.setInt(3, platCompose.getPlatId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlatCompose(int platId) {
        String sql = "DELETE FROM PlatCompose WHERE plat_id = ?";
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
