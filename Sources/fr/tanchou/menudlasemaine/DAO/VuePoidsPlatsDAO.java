package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.VuePoidsPlats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VuePoidsPlatsDAO {

    public List<VuePoidsPlats> getAllPoidsPlats() {
        List<VuePoidsPlats> poidsPlats = new ArrayList<>();
        String sql = "SELECT * FROM VuePoidsPlats";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                VuePoidsPlats poidsPlat = new VuePoidsPlats(
                        rs.getInt("plat_id"),
                        rs.getString("nom_plat"),
                        rs.getInt("poids_famille"),
                        rs.getInt("poids_saison"),
                        rs.getInt("poids_moment_journee"),
                        rs.getInt("poids_moment_semaine")
                );
                poidsPlats.add(poidsPlat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return poidsPlats;
    }

    public VuePoidsPlats getPoidsPlatById(int platId) {
        String sql = "SELECT * FROM VuePoidsPlats WHERE plat_id = ?";
        VuePoidsPlats poidsPlat = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                poidsPlat = new VuePoidsPlats(
                        rs.getInt("plat_id"),
                        rs.getString("nom_plat"),
                        rs.getInt("poids_famille"),
                        rs.getInt("poids_saison"),
                        rs.getInt("poids_moment_journee"),
                        rs.getInt("poids_moment_semaine")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return poidsPlat;
    }
}
