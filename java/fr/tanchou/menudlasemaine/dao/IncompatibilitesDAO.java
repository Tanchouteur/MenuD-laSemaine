package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
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

        // Requête SQL
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

        // Connexion à la base de données
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            // Paramètres de la requête
            pstmt.setString(1, typeProduitToCheck.toString());
            pstmt.setInt(2, idProduit);
            pstmt.setInt(3, idProduit);

            // Exécution de la requête
            try (ResultSet rs = pstmt.executeQuery()) {
                // Traitement des résultats
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nomProduit = rs.getString("nomProduit");
                    TypeProduit typeProduit = TypeProduit.valueOf(rs.getString("typeProduit").toUpperCase());
                    int poidsArbitraire = rs.getInt("poidsArbitraire");

                    // Conversion de la date
                    LocalDate dateLastUsed = null;
                    Date lastUsed = rs.getDate("dateLastUsed");
                    if (lastUsed != null) {
                        dateLastUsed = lastUsed.toLocalDate();
                    }

                    // Poids des moments de la journée
                    int[] poidsMoment = new int[]{
                            rs.getInt("poidsMidiSemaine"),
                            rs.getInt("poidsSoirSemaine"),
                            rs.getInt("poidsMidiWeekend"),
                            rs.getInt("poidsSoirWeekend")
                    };

                    // Poids des saisons
                    int[] poidsSaison = new int[]{
                            rs.getInt("poidsPrintemps"),
                            rs.getInt("poidsEte"),
                            rs.getInt("poidsAutomne"),
                            rs.getInt("poidsHiver")
                    };

                    // Création de l'objet Produit
                    Produits produit = new Produits(id, nomProduit, poidsArbitraire, dateLastUsed, typeProduit, poidsMoment, poidsSaison);
                    produitsCompatibles.add(produit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits compatibles : " + e.getMessage());
        }

        return produitsCompatibles;
    }

}
