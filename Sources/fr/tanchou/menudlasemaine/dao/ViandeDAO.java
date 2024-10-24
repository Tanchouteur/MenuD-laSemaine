package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Viande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ViandeDAO {

    public void ajouterViande(Viande viande) {
        String sql = "INSERT INTO Viande (nom_viande) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, viande.getNomViande());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    viande.setViandeId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Viande getViandeById(int viandeId) {
        String sql = "SELECT * FROM Viande WHERE viande_id = ?";
        Viande viande = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, viandeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                viande = new Viande(
                        rs.getInt("viande_id"),
                        rs.getString("nom_viande")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return viande;
    }

    public List<Viande> getAllViandes() {
        List<Viande> viandes = new ArrayList<>();
        String sql = "SELECT * FROM Viande";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                viandes.add(new Viande(
                        rs.getInt("viande_id"),
                        rs.getString("nom_viande")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return viandes;
    }

    public void updateViande(Viande viande) {
        String sql = "UPDATE Viande SET nom_viande = ? WHERE viande_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, viande.getNomViande());
            pstmt.setInt(2, viande.getViandeId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteViande(int viandeId) {
        String sql = "DELETE FROM Viande WHERE viande_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, viandeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Viande getRandomViande() {
        Random random = new Random();
        List<Viande> viandes = getAllViandes();
        return viandes.get(random.nextInt(viandes.size()));
    }
}
