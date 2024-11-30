package fr.tanchou.menudlasemaine.dao.produit;

import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlatCompletDAO {

    // Méthode pour ajouter un plat complet avec la gestion du poids et de la dernière utilisation
    public void ajouterPlatComplet(PlatComplet platComplet) {
        String sql = "INSERT INTO plat_complet (nom_plat_complet, poids) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, platComplet.getNomPlat());
            pstmt.setInt(2, platComplet.getPoids());
            pstmt.executeUpdate();

            // Initialise l'enregistrement dans ProduitLastUse après insertion
            if (pstmt.getGeneratedKeys().next()) {
                initialiserHistorique(platComplet.getNomPlat(), "PlatComplet");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour initialiser l'enregistrement de la dernière utilisation dans ProduitLastUse
    private void initialiserHistorique(String nomProduit, String typeProduit) {
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        produitLastUseDAO.upsertProduitLastUse(nomProduit, typeProduit);
    }

    // Méthode pour récupérer un plat complet par son nom
    public static PlatComplet getPlatCompletByName(String platCompletName) {
        String sql = "SELECT * FROM plat_complet WHERE nom_plat_complet = ?";
        PlatComplet platComplet = null;
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platCompletName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(platCompletName);
                platComplet = new PlatComplet(platCompletName, poids, derniereUtilisation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platComplet;
    }

    // Méthode pour récupérer tous les plats complets
    public static List<Produits> getAllPlatsComplets() {
        List<Produits> platsComplets = new ArrayList<>();

        String sql = "SELECT * FROM plat_complet";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {

                String name = rs.getString("nom_plat_complet");
                int poids = rs.getInt("poids");
                LocalDate derniereUtilisation = getDerniereUtilisation(name);

                platsComplets.add(new PlatComplet(name, poids, derniereUtilisation));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platsComplets;
    }

    // Méthode pour obtenir la dernière utilisation d'un produit via ProduitLastUse
    private static LocalDate getDerniereUtilisation(String nomProduit) {
        ProduitLastUseDAO produitLastUseDAO = new ProduitLastUseDAO();
        Optional<Date> lastUseDate = produitLastUseDAO.getLastUseDate(nomProduit, TypeProduit.PLAT_COMPLET);
        return lastUseDate.map(Date::toLocalDate).orElse(null);
    }
}
