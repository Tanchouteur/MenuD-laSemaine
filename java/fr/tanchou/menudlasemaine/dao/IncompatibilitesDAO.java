package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;

/**
 * Data Access Object (DAO) for managing product incompatibilities and retrieving compatible products from the database.
 * This class provides methods for adding and removing incompatibilities between products and fetching compatible products.
 */
public class IncompatibilitesDAO {

    /**
     * Adds an incompatibility between two products.
     *
     * @param idProduit1 The ID of the first product.
     * @param idProduit2 The ID of the second product.
     */
    public static void ajouterIncompatibilite(int idProduit1, int idProduit2) {
        String insertSQL = "INSERT INTO Incompatibilite (idProduit1, idProduit2) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, idProduit1);
            pstmt.setInt(2, idProduit2);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding incompatibility: " + e.getMessage());
        }
    }

    /**
     * Removes an incompatibility between two products.
     *
     * @param idProduit1 The ID of the first product.
     * @param idProduit2 The ID of the second product.
     */
    public static void retirerIncompatibilite(int idProduit1, int idProduit2) {
        String deleteSQL = "DELETE FROM Incompatibilite WHERE idProduit1 = ? AND idProduit2 = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, idProduit1);
            pstmt.setInt(2, idProduit2);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing incompatibility: " + e.getMessage());
        }
    }

    /**
     * Retrieves the list of products compatible with a given product based on its type.
     * Incompatible products are excluded from the list.
     *
     * @param idProduit The ID of the product for which compatible products are sought.
     * @param typeProduitToCheck The type of product to check for compatibility (e.g., meat, vegetable, etc.).
     * @return A list of compatible products.
     */
    public static LinkedList<Produits> getProduitsCompatibles(int idProduit, TypeProduit typeProduitToCheck) {
        LinkedList<Produits> produitsCompatibles = new LinkedList<>();

        String selectSQL = "SELECT p.id, p.nomProduit, p.typeProduit, p.poidsArbitraire, p.dateLastUsed, " +
                "pm.poidsMidiSemaine, pm.poidsSoirSemaine, pm.poidsMidiWeekend, pm.poidsSoirWeekend, " +
                "ps.poidsPrintemps, ps.poidsEte, ps.poidsAutomne, ps.poidsHiver " +
                "FROM Produits p " +
                "JOIN PoidsMoment pm ON p.id = pm.idProduit " +
                "JOIN PoidsSaison ps ON p.id = ps.idProduit " +
                "WHERE p.typeProduit = ? " +
                "AND p.id NOT IN (" +
                "SELECT idProduit2 FROM Incompatibilite WHERE idProduit1 = ? " +
                "UNION " +
                "SELECT idProduit1 FROM Incompatibilite WHERE idProduit2 = ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, typeProduitToCheck.toString());
            pstmt.setInt(2, idProduit);
            pstmt.setInt(3, idProduit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nomProduit = rs.getString("nomProduit");
                    TypeProduit typeProduit = TypeProduit.valueOf(rs.getString("typeProduit").toUpperCase());
                    int poidsArbitraire = rs.getInt("poidsArbitraire");

                    LocalDate dateLastUsed = null;
                    Date lastUsed = rs.getDate("dateLastUsed");
                    if (lastUsed != null) {
                        dateLastUsed = lastUsed.toLocalDate();
                    }

                    int[] poidsMoment = new int[] {
                            rs.getInt("poidsMidiSemaine"),
                            rs.getInt("poidsSoirSemaine"),
                            rs.getInt("poidsMidiWeekend"),
                            rs.getInt("poidsSoirWeekend")
                    };

                    int[] poidsSaison = new int[] {
                            rs.getInt("poidsPrintemps"),
                            rs.getInt("poidsEte"),
                            rs.getInt("poidsAutomne"),
                            rs.getInt("poidsHiver")
                    };

                    Produits produit = new Produits(id, nomProduit, poidsArbitraire, dateLastUsed, typeProduit, poidsMoment, poidsSaison);
                    produitsCompatibles.add(produit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving compatible products: " + e.getMessage());
        }

        return produitsCompatibles;
    }
}