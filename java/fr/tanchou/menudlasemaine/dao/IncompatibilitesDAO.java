package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IncompatibilitesDAO {

    // Méthode pour ajouter une incompatibilité entre deux produits
    public static void ajouterIncompatibilite(int idProduit1, int idProduit2) {
        String insertSQL = "INSERT INTO Incompatibilite (idProduit1, idProduit2) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, idProduit1);
            pstmt.setInt(2, idProduit2);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'incompatibilité : " + e.getMessage());
        }
    }

    // Méthode pour retirer une incompatibilité entre deux produits
    public static void retirerIncompatibilite(int idProduit1, int idProduit2) {
        String deleteSQL = "DELETE FROM Incompatibilite WHERE idProduit1 = ? AND idProduit2 = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, idProduit1);
            pstmt.setInt(2, idProduit2);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'incompatibilité : " + e.getMessage());
        }
    }

    // Méthode pour obtenir la liste des produits compatibles avec un produit donné
    public static LinkedList<Produits> getProduitsCompatibles(int idProduit, TypeProduit typeProduitToCheck) {
        LinkedList<Produits> produitsCompatibles = new LinkedList<>();
        String selectSQL = "SELECT p.* FROM Produits p WHERE p.typeProduit = ? AND p.id NOT IN " +
                "(SELECT idProduit2 FROM Incompatibilite WHERE idProduit1 = ? " +
                "UNION SELECT idProduit1 FROM Incompatibilite WHERE idProduit2 = ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, String.valueOf(typeProduitToCheck));  // Ajout du filtre sur le type de produit
            pstmt.setInt(2, idProduit);
            pstmt.setInt(3, idProduit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nomProduit = rs.getString("nomProduit");
                TypeProduit typeProduit = TypeProduit.valueOf(rs.getString("typeProduit"));
                int poidsArbitraire = rs.getInt("poidsArbitraire");
                LocalDate lastUsed = rs.getDate("lastUsed").toLocalDate();
                int[] poidsMoment = new int[]{
                        rs.getInt("poidsMidiSemaine"),
                        rs.getInt("poidsSoirSemaine"),
                        rs.getInt("poidsMidiWeekend"),
                        rs.getInt("poidsSoirWeekend")};

                int[] poidsSaison = new int[]{
                        rs.getInt("poidsPrintemps"),
                        rs.getInt("poidsEte"),
                        rs.getInt("poidsAutomne"),
                        rs.getInt("poidsHiver")
                };


                // Création de l'objet Produit
                Produits produit = new Produits(id, nomProduit, poidsArbitraire, lastUsed, typeProduit, poidsMoment, poidsSaison);
                produitsCompatibles.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits compatibles : " + e.getMessage());
        }
        return produitsCompatibles;
    }
}
