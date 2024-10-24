package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Viande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatDAO {

    public void ajouterPlat(Plat plat) {
        String sql = "INSERT INTO Plat (viande_id, accompagnement_id, tout_en_un, nom_plat) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, plat.getViande().getViandeId());
            pstmt.setInt(2, plat.getAccompagnement() != null ? plat.getAccompagnement().getAccompagnementId() : null);
            pstmt.setBoolean(3, plat.isToutEnUn());
            pstmt.setString(4, plat.getNomPlat());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    plat.setPlatId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Plat getPlatById(int platId) {
        String sql = "SELECT * FROM Plat WHERE plat_id = ?";
        Plat plat = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Viande viande = new ViandeDAO().getViandeById(rs.getInt("viande_id"));
                Accompagnement accompagnement = new AccompagnementDAO().getAccompagnementById(rs.getInt("accompagnement_id"));
                plat = new Plat(
                        rs.getInt("plat_id"),
                        viande,
                        accompagnement,
                        rs.getBoolean("tout_en_un"),
                        rs.getString("nom_plat")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plat;
    }

    public List<Plat> getAllPlats() {
        List<Plat> plats = new ArrayList<>();
        String sql = "SELECT * FROM Plat";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Viande viande = new ViandeDAO().getViandeById(rs.getInt("viande_id"));
                Accompagnement accompagnement = new AccompagnementDAO().getAccompagnementById(rs.getInt("accompagnement_id"));
                plats.add(new Plat(
                        rs.getInt("plat_id"),
                        viande,
                        accompagnement,
                        rs.getBoolean("tout_en_un"),
                        rs.getString("nom_plat")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plats;
    }

    public void updatePlat(Plat plat) {
        String sql = "UPDATE Plat SET viande_id = ?, accompagnement_id = ?, tout_en_un = ?, nom_plat = ? WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plat.getViande().getViandeId());
            pstmt.setInt(2, plat.getAccompagnement() != null ? plat.getAccompagnement().getAccompagnementId() : null);
            pstmt.setBoolean(3, plat.isToutEnUn());
            pstmt.setString(4, plat.getNomPlat());
            pstmt.setInt(5, plat.getPlatId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlat(int platId) {
        String sql = "DELETE FROM Plat WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

