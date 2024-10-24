package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Accompagnement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccompagnementDAO {

    public void insertAccompagnement(Accompagnement accompagnement) {
        String sql = "INSERT INTO Accompagnement (legume_id, feculent_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, accompagnement.getLegume().getLegumeId());
            pstmt.setInt(2, accompagnement.getFeculent().getFeculentId());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    accompagnement.setAccompagnementId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Accompagnement getAccompagnementById(int accompagnementId) {
        String sql = "SELECT * FROM Accompagnement WHERE accompagnement_id = ?";
        Accompagnement accompagnement = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accompagnementId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                accompagnement = new Accompagnement(
                        rs.getInt("accompagnement_id"),
                        new LegumeDAO().getLegumeById(rs.getInt("legume_id")),
                        new FeculentDAO().getFeculentById(rs.getInt("feculent_id"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accompagnement;
    }

    public List<Accompagnement> getAllAccompagnements() {
        List<Accompagnement> accompagnements = new ArrayList<>();
        String sql = "SELECT * FROM Accompagnement";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accompagnements.add(new Accompagnement(
                        rs.getInt("accompagnement_id"),
                        new LegumeDAO().getLegumeById(rs.getInt("legume_id")),
                        new FeculentDAO().getFeculentById(rs.getInt("feculent_id"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accompagnements;
    }

    public void updateAccompagnement(Accompagnement accompagnement) {
        String sql = "UPDATE Accompagnement SET legume_id = ?, feculent_id = ? WHERE accompagnement_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accompagnement.getLegume().getLegumeId());
            pstmt.setInt(2, accompagnement.getFeculent().getFeculentId());
            pstmt.setInt(3, accompagnement.getAccompagnementId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAccompagnement(int accompagnementId) {
        String sql = "DELETE FROM Accompagnement WHERE accompagnement_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accompagnementId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
