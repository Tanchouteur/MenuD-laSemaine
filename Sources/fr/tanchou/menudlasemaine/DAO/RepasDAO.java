package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Repas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepasDAO {

    public void ajouterRepas(Repas repas) {
        String sql = "INSERT INTO Repas (entree_id, plat_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, repas.getEntree().getEntreeId());
            pstmt.setInt(2, repas.getPlat().getPlatId());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    repas.setRepasId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Repas getRepasById(int repasId) {
        String sql = "SELECT * FROM Repas WHERE repas_id = ?";
        Repas repas = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                repas = new Repas(
                        rs.getInt("repas_id"),
                        new EntreeDAO().getEntreeById(rs.getInt("entree_id")),
                        new PlatDAO().getPlatById(rs.getInt("plat_id"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return repas;
    }

    public List<Repas> getAllRepas() {
        List<Repas> repasList = new ArrayList<>();
        String sql = "SELECT * FROM Repas";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                repasList.add(new Repas(
                        rs.getInt("repas_id"),
                        new EntreeDAO().getEntreeById(rs.getInt("entree_id")),
                        new PlatDAO().getPlatById(rs.getInt("plat_id"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return repasList;
    }

    public void updateRepas(Repas repas) {
        String sql = "UPDATE Repas SET entree_id = ?, plat_id = ? WHERE repas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repas.getEntree().getEntreeId());
            pstmt.setInt(2, repas.getPlat().getPlatId());
            pstmt.setInt(3, repas.getRepasId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRepas(int repasId) {
        String sql = "DELETE FROM Repas WHERE repas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
