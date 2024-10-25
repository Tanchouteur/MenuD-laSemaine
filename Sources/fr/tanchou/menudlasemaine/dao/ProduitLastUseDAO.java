package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Optional;

public class ProduitLastUseDAO {

    public void upsertProduitLastUse(String nomProduit, String typeProduit) {
        String sql = """
                INSERT INTO ProduitLastUse (nom_produit, type_produit, date_last_use) 
                VALUES (?, ?, CURRENT_DATE) 
                ON DUPLICATE KEY UPDATE date_last_use = CURRENT_DATE
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, typeProduit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtenir la dernière date d'utilisation d'un produit spécifique
    public Optional<Date> getLastUseDate(String nomProduit, String typeProduit) {
        String sql = "SELECT date_last_use FROM ProduitLastUse WHERE nom_produit = ? AND type_produit = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, typeProduit);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getDate("date_last_use"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Supprimer un produit de la table ProduitLastUse
    public void deleteProduitLastUse(String nomProduit, String typeProduit) {
        String sql = "DELETE FROM ProduitLastUse WHERE nom_produit = ? AND type_produit = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, typeProduit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}