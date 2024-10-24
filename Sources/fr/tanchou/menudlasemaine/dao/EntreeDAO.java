package fr.tanchou.menudlasemaine.dao;
import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Entree;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntreeDAO {

    public void ajouterEntree(Entree entree) {
        String sql = "INSERT INTO Entree (nom_entree) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, entree.getNomEntree());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entree.setEntreeId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Entree getEntreeById(int entreeId) {
        String sql = "SELECT * FROM Entree WHERE entree_id = ?";
        Entree entree = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entreeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                entree = new Entree(
                        rs.getInt("entree_id"),
                        rs.getString("nom_entree")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entree;
    }

    public List<Entree> getAllEntrees() {
        List<Entree> entrees = new ArrayList<>();
        String sql = "SELECT * FROM Entree";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entrees.add(new Entree(
                        rs.getInt("entree_id"),
                        rs.getString("nom_entree")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entrees;
    }

    public void updateEntree(Entree entree) {
        String sql = "UPDATE Entree SET nom_entree = ? WHERE entree_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entree.getNomEntree());
            pstmt.setInt(2, entree.getEntreeId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEntree(int entreeId) {
        String sql = "DELETE FROM Entree WHERE entree_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entreeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

