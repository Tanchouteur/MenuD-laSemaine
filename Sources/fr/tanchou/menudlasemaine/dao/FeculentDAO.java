package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Feculent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeculentDAO {

    public void ajouterFeculent(Feculent feculent) {
        String sql = "INSERT INTO Feculent (nom_feculent) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, feculent.getFeculentName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Feculent getFeculentByName(String feculentName) {
        String sql = "SELECT * FROM Legume WHERE nom_legume = ?";
        Feculent feculent = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                feculent = new Feculent(rs.getString("nom_feculent"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculent;
    }

    public List<Feculent> getAllFeculents() {
        List<Feculent> feculents = new ArrayList<>();
        String sql = "SELECT * FROM Feculent";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                feculents.add(new Feculent(
                        rs.getString("nom_feculent")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculents;
    }

    /*public void updateFeculent(Feculent feculent) {
        String sql = "UPDATE Feculent SET nom_feculent = ? WHERE feculent_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculent.getFeculentName());
            pstmt.setInt(2, feculent.getFeculentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    public void deleteFeculent(String feculentName) {
        String sql = "DELETE FROM Feculent WHERE nom_feculent = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculentName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
