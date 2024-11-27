package fr.tanchou.menudlasemaine.dao.weight;

import fr.tanchou.menudlasemaine.dao.produit.*;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.*;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProduitLastUseDAO {

    public static void updateLastUseDate(String nomProduit) {
        String sql = """
            UPDATE ProduitLastUse 
            SET date_last_use = CURRENT_DATE 
            WHERE nom_produit = ?
            """;

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void upsertProduitLastUse(String nomProduit, String typeProduit) {
        String sql = """
                INSERT INTO ProduitLastUse (nom_produit, type_produit, date_last_use) 
                VALUES (?, ?, CURRENT_DATE) 
                ON DUPLICATE KEY UPDATE date_last_use = CURRENT_DATE
                """;

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, typeProduit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtenir la dernière date d'utilisation d'un produit spécifique
    public static Optional<Date> getLastUseDate(String nomProduit, TypeProduit typeProduit) {
        String sql = "SELECT date_last_use FROM ProduitLastUse WHERE nom_produit = ? AND type_produit = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, String.valueOf(typeProduit));
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
    public static void deleteProduitLastUse(String nomProduit, String typeProduit) {
        String sql = "DELETE FROM ProduitLastUse WHERE nom_produit = ? AND type_produit = ?";
        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomProduit);
            pstmt.setString(2, typeProduit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
    * Récupère toutes les dates de dernière utilisation pour chaque produit.
    *
    * @return une Map contenant les noms des produits et leurs dates de dernière utilisation.
    */
    public static  Map<Produits, LocalDate> getLastUseDatesForType(TypeProduit typeProduit) {
        Map<Produits, LocalDate> lastUseDates = new HashMap<>();
        String sql = "";

        // Préparer la requête SQL selon le type de produit
        switch (typeProduit) {
            case VIANDE:
                sql = "SELECT pl.nom_produit, v.poids, pl.date_last_use " +
                        "FROM ProduitLastUse pl JOIN Viande v ON pl.nom_produit = v.nom_viande " +
                        "WHERE pl.type_produit = ?";
                break;
            case FECULENT:
                sql = "SELECT pl.nom_produit, f.poids, pl.date_last_use " +
                        "FROM ProduitLastUse pl JOIN Feculent f ON pl.nom_produit = f.nom_feculent " +
                        "WHERE pl.type_produit = ?";
                break;
            case LEGUME:
                sql = "SELECT pl.nom_produit, l.poids, pl.date_last_use " +
                        "FROM ProduitLastUse pl JOIN Legume l ON pl.nom_produit = l.nom_legume " +
                        "WHERE pl.type_produit = ?";
                break;
            case ENTREE:
                sql = "SELECT pl.nom_produit, e.poids, pl.date_last_use " +
                        "FROM ProduitLastUse pl JOIN Entree e ON pl.nom_produit = e.nom_entree " +
                        "WHERE pl.type_produit = ?";
                break;
            case PLAT_COMPLET:
                sql = "SELECT pl.nom_produit, pc.poids, pl.date_last_use " +
                        "FROM ProduitLastUse pl JOIN PlatComplet pc ON pl.nom_produit = pc.nom_plat " +
                        "WHERE pl.type_produit = ?";
                break;
        }

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, typeProduit.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nomProduit = rs.getString("nom_produit");
                int poids = rs.getInt("poids");
                LocalDate lastUseDate = rs.getDate("date_last_use") != null ? rs.getDate("date_last_use").toLocalDate() : null;

                switch (typeProduit) {
                    case VIANDE:
                        lastUseDates.put(new Viande(nomProduit,poids,lastUseDate), lastUseDate);
                        break;
                    case FECULENT:
                        lastUseDates.put(new Feculent(nomProduit,poids,lastUseDate), lastUseDate);
                        break;
                    case LEGUME:
                        lastUseDates.put(new Legume(nomProduit,poids,lastUseDate), lastUseDate);
                        break;
                    case ENTREE:
                        lastUseDates.put(new Entree(nomProduit,poids,lastUseDate), lastUseDate);
                        break;
                    case PLAT_COMPLET:
                        lastUseDates.put(new PlatComplet(nomProduit,poids,lastUseDate), lastUseDate);
                        break;
                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastUseDates;
    }

}