package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.models.Legume;
import fr.tanchou.menudlasemaine.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LegumeDAO {

    // Ajoute un légume avec son nom et son poids, et initialise l'historique d'utilisation
    public void ajouterLegume(Legume legume) {
        String sql = "INSERT INTO Legume (nom_legume, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legume.getLegumeNom());
            pstmt.setInt(2, legume.getPoids());
            pstmt.executeUpdate();

            initialiserHistorique(legume.getLegumeNom()); // Initialise la date d'utilisation pour ce légume
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère un légume par son nom, incluant le poids et la dernière utilisation
    public Legume getLegumeByName(String nomLegume) {
        String sql = "SELECT * FROM Legume WHERE nom_legume = ?";
        Legume legume = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLegume);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomLegume);
                legume = new Legume(nomLegume, poids, derniereUtilisation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legume;
    }

    // Récupère tous les légumes avec leurs poids et dates de dernière utilisation
    public List<Legume> getAllLegumes() {
        List<Legume> legumes = new ArrayList<>();
        String sql = "SELECT * FROM Legume";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nomLegume = rs.getString("nom_legume");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(nomLegume);
                legumes.add(new Legume(nomLegume, poids, derniereUtilisation));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return legumes;
    }

    // Supprime un légume par son nom
    public void deleteLegume(String nomLegume) {
        String sql = "DELETE FROM Legume WHERE nom_legume = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLegume);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère la dernière date d'utilisation d'un légume depuis ProduitLastUse
    private LocalDate getDerniereUtilisation(String nomLegume) {
        String sql = "SELECT date_last_use FROM ProduitLastUse WHERE nom_produit = ?";
        LocalDate lastUsed = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLegume);
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

    // Initialise l'historique d'utilisation pour un nouveau légume
    private void initialiserHistorique(String nomLegume) {
        String sql = "INSERT INTO ProduitLastUse (nom_produit, date_last_use) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLegume);
            pstmt.setDate(2, Date.valueOf(LocalDate.now())); // Initialisé avec la date actuelle
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
