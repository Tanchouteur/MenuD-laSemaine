package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.models.produit.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EntreeDAO {

    // Ajoute une nouvelle entrée avec son nom et poids, et initialise l'historique d'utilisation
    public void ajouterEntree(Entree entree) {
        String sql = "INSERT INTO entree (nom_entree, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entree.getNom());
            pstmt.setInt(2, entree.getPoids());
            pstmt.executeUpdate();

            initialiserHistorique(entree.getNom()); // Initialise la date d'utilisation pour cette entrée
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère une entrée par son nom avec son poids et dernière utilisation
    public static Entree getEntreeByName(String entreeName) {
        String sql = "SELECT * FROM entree WHERE nom_entree = ?";
        Entree entree = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entreeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nomEntree = rs.getString("nom_entree");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomEntree);
                entree = new Entree(nomEntree, poids, derniereUtilisation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entree;
    }

    // Récupère toutes les entrées avec leurs poids et dates d'utilisation
    public static List<Produits> getAllEntrees() {
        List<Produits> entrees = new ArrayList<>();
        String sql = "SELECT * FROM entree";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nomEntree = rs.getString("nom_entree");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomEntree);
                entrees.add(new Entree(nomEntree, poids, derniereUtilisation));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entrees;
    }

    // Supprime une entrée par son nom
    public void deleteEntree(String entreeName) {
        String sql = "DELETE FROM entree WHERE nom_entree = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entreeName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode privée pour récupérer la dernière utilisation d'une entrée à partir de la table ProduitLastUse
    private static LocalDate getDerniereUtilisation(String nomEntree) {
        String sql = "SELECT date_last_use FROM ProduitLastUse WHERE nom_produit = ?";
        LocalDate lastUsed = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomEntree);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Date date = rs.getDate("date_last_use");
                lastUsed = date.toLocalDate(); // Conversion de Date à LocalDate
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastUsed;
    }

    // Méthode pour initialiser l'historique de la date d'utilisation dans ProduitLastUse
    private void initialiserHistorique(String nomEntree) {
        String sql = "INSERT INTO ProduitLastUse (nom_produit, date_last_use) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomEntree);
            pstmt.setDate(2, Date.valueOf(LocalDate.now())); // Initialise avec la date actuelle
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
