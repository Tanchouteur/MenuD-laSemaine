package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Viande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViandeDAO {

    public void ajouterViande(Viande viande) {
        String sql = "INSERT INTO Viande (nom_viande) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, viande.getNomViande());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Viande getViandeByName(String nomViande) {
        String sql = "SELECT * FROM Viande WHERE nom_viande = ?";
        Viande viande = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                viande = new Viande(rs.getString("nom_viande"));
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
                viandes.add(new Viande(rs.getString("nom_viande")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return viandes;
    }

    public void updateViande(Viande viande, String newNomViande) {
        String sql = "UPDATE Viande SET nom_viande = ? WHERE nom_viande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, viande.getNomViande());
            pstmt.setString(2, newNomViande);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteViande(String nomViande) {
        String sql = "DELETE FROM Viande WHERE nom_viande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
