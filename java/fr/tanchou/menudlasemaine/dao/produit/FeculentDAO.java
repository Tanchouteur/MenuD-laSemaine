package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Feculent;
import fr.tanchou.menudlasemaine.models.produit.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeculentDAO {

    // Méthode pour ajouter un féculent et initialiser son historique dans ProduitLastUse
    public static void ajouterFeculent(Feculent feculent) {
        String sql = "INSERT INTO Feculent (nom_feculent, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, feculent.getNom());
            pstmt.setInt(2, feculent.getPoids());
            pstmt.executeUpdate();

            // Récupérer l'ID généré et initialiser l'historique de la dernière utilisation
            if (pstmt.getGeneratedKeys().next()) {
                initialiserHistorique(feculent.getNom(), "Feculent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour initialiser l'enregistrement de la dernière utilisation
    private static void initialiserHistorique(String nomProduit, String typeProduit) {
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        produitLastUseDAO.upsertProduitLastUse(nomProduit, typeProduit);
    }

    // Méthode pour récupérer un féculent par son nom
    public static Produits getFeculentByName(String feculentName) {
        String sql = "SELECT * FROM Feculent WHERE nom_feculent = ?";
        Produits feculent = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(feculentName);
                feculent = new Feculent(feculentName, poids, derniereUtilisation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculent;
    }

    // Méthode pour récupérer tous les féculents
    public static List<Produits> getAllFeculents() {
        List<Produits> feculents = new ArrayList<>();
        String sql = "SELECT * FROM Feculent";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("nom_feculent");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(name);
                feculents.add(new Feculent(name, poids, derniereUtilisation));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculents;
    }

    // Méthode pour obtenir la dernière utilisation d'un produit
    private static LocalDate getDerniereUtilisation(String nomProduit) {
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        Optional<Date> lastUseDate = produitLastUseDAO.getLastUseDate(nomProduit, TypeProduit.FECULENT);
        return lastUseDate.map(Date::toLocalDate).orElse(null);
    }
}
