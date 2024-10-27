package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViandeDAO {

    // Méthode pour ajouter une viande avec son nom et poids, et initialiser l'historique d'utilisation
    public void ajouterViande(Viande viande) {
        String sql = "INSERT INTO Viande (nom_viande, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, viande.getViandeNom());
            pstmt.setInt(2, viande.getPoids());
            pstmt.executeUpdate();

            initialiserHistorique(viande.getViandeNom()); // Initialise la date d'utilisation pour cette viande
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère une viande par son nom, incluant le poids et la dernière utilisation
    public static Viande getViandeByName(String nomViande) {
        String sql = "SELECT * FROM Viande WHERE nom_viande = ?";
        Viande viande = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomViande);
                viande = new Viande(nomViande, poids, derniereUtilisation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return viande;
    }

    // Récupère toutes les viandes avec leurs poids et dates de dernière utilisation
    public static List<Viande> getAllViandes() {
        List<Viande> viandes = new ArrayList<>();
        String sql = "SELECT * FROM Viande";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nomViande = rs.getString("nom_viande");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomViande);
                viandes.add(new Viande(nomViande, poids, derniereUtilisation));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return viandes;
    }

    // Supprime une viande par son nom
    public void deleteViande(String nomViande) {
        String sql = "DELETE FROM Viande WHERE nom_viande = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère la dernière date d'utilisation d'une viande depuis ProduitLastUse
    private static LocalDate getDerniereUtilisation(String nomViande) {
        String sql = "SELECT date_last_use FROM ProduitLastUse WHERE nom_produit = ?";
        LocalDate lastUsed = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
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

    // Initialisation de l'historique d'utilisation pour une nouvelle viande
    private void initialiserHistorique(String nomViande) {
        String sql = "INSERT INTO ProduitLastUse (nom_produit, date_last_use) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomViande);
            pstmt.setDate(2, Date.valueOf(LocalDate.now())); // Initialisé avec la date actuelle
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}