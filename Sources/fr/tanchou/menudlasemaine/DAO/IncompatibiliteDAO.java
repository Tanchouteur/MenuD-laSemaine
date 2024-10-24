package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Incompatibilite;
import fr.tanchou.menudlasemaine.models.Feculent;
import fr.tanchou.menudlasemaine.models.Legume;
import fr.tanchou.menudlasemaine.models.Viande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncompatibiliteDAO {

    public List<Incompatibilite> getAllIncompatibilites() {
        List<Incompatibilite> incompatibilites = new ArrayList<>();
        String sql = "SELECT * FROM Incompatibilite";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Viande viande = null;
                Legume legume = null;
                Feculent feculent = null;

                // Récupérer les objets Viande, Legume et Feculent si ils ne sont pas NULL
                if (rs.getInt("viande_id") != 0) {
                    viande = new Viande(rs.getInt("viande_id"), "NomViande"); // Remplace "NomViande" par la récupération réelle du nom
                }
                if (rs.getInt("legume_id") != 0) {
                    legume = new Legume(rs.getInt("legume_id"), "NomLegume"); // Remplace "NomLegume" par la récupération réelle du nom
                }
                if (rs.getInt("feculent_id") != 0) {
                    feculent = new Feculent(rs.getInt("feculent_id"), "NomFeculent"); // Remplace "NomFeculent" par la récupération réelle du nom
                }

                Incompatibilite incompatibilite = new Incompatibilite(
                        rs.getInt("incompatibilite_id"),
                        viande,
                        legume,
                        feculent
                );
                incompatibilites.add(incompatibilite);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return incompatibilites;
    }

    public Incompatibilite getIncompatibiliteById(int incompatibiliteId) {
        String sql = "SELECT * FROM Incompatibilite WHERE incompatibilite_id = ?";
        Incompatibilite incompatibilite = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, incompatibiliteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Viande viande = null;
                Legume legume = null;
                Feculent feculent = null;

                // Récupérer les objets Viande, Legume et Feculent si ils ne sont pas NULL
                if (rs.getInt("viande_id") != 0) {
                    viande = new Viande(rs.getInt("viande_id"), "NomViande"); // Remplace "NomViande" par la récupération réelle du nom
                }
                if (rs.getInt("legume_id") != 0) {
                    legume = new Legume(rs.getInt("legume_id"), "NomLegume"); // Remplace "NomLegume" par la récupération réelle du nom
                }
                if (rs.getInt("feculent_id") != 0) {
                    feculent = new Feculent(rs.getInt("feculent_id"), "NomFeculent"); // Remplace "NomFeculent" par la récupération réelle du nom
                }

                incompatibilite = new Incompatibilite(
                        rs.getInt("incompatibilite_id"),
                        viande,
                        legume,
                        feculent
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return incompatibilite;
    }
}
