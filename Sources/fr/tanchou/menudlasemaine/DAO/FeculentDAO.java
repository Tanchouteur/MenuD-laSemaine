package fr.tanchou.menudlasemaine.DAO;

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

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feculent.setFeculentId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Feculent getFeculentById(int feculentId) {
        String sql = "SELECT * FROM Feculent WHERE feculent_id = ?";
        Feculent feculent = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, feculentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                feculent = new Feculent(
                        rs.getInt("feculent_id"),
                        rs.getString("nom_feculent")
                );
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
                        rs.getInt("feculent_id"),
                        rs.getString("nom_feculent")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculents;
    }

    public void updateFeculent(Feculent feculent) {
        String sql = "UPDATE Feculent SET nom_feculent = ? WHERE feculent_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculent.getFeculentName());
            pstmt.setInt(2, feculent.getFeculentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFeculent(int feculentId) {
        String sql = "DELETE FROM Feculent WHERE feculent_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, feculentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
