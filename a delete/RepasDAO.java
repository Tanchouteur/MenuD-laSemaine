package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.models.*;
import fr.tanchou.menudlasemaine.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepasDAO {

    public void ajouterRepas(Repas repas) {
        String sql = "INSERT INTO Repas (entree_id, plat_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, repas.getEntree().getEntreeId());
            pstmt.setInt(2, repas.getPlat().getPlatId());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    repas.setRepasId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Repas getRepasById(int repasId) {
        String sql = "SELECT * FROM Repas WHERE repas_id = ?";
        Repas repas = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Entree entree = new EntreeDAO().getEntreeById(rs.getInt("entree_id"));
                Plat plat = getPlatById(rs.getInt("plat_id"));
                repas = new Repas(rs.getInt("repas_id"), entree, plat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return repas;
    }

    public List<Repas> getAllRepas() {
        List<Repas> repasList = new ArrayList<>();
        String sql = "SELECT * FROM Repas";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Entree entree = new EntreeDAO().getEntreeById(rs.getInt("entree_id"));
                Plat plat = getPlatById(rs.getInt("plat_id"));
                repasList.add(new Repas(rs.getInt("repas_id"), entree, plat));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return repasList;
    }

    public void updateRepas(Repas repas) {
        String sql = "UPDATE Repas SET entree_id = ?, plat_id = ? WHERE repas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repas.getEntree().getEntreeId());
            pstmt.setInt(2, repas.getPlat().getPlatId());
            pstmt.setInt(3, repas.getRepasId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRepas(int repasId) {
        String sql = "DELETE FROM Repas WHERE repas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Plat getPlatById(int platId) {
        Plat plat = null;

        // Récupérer le type de plat
        String sql = "SELECT type_plat FROM Plat WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String typePlat = rs.getString("type_plat");
                if ("complet".equals(typePlat)) {
                    plat = getPlatCompletById(platId);
                } else if ("composé".equals(typePlat)) {
                    plat = getPlatComposeById(platId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plat;
    }

    private PlatComplet getPlatCompletById(int platId) {
        PlatComplet platComplet = null;
        String sql = "SELECT * FROM PlatComplet WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // On doit aussi récupérer le type de plat et le poids depuis la table Plat
                Plat plat = getPlatById(platId); // On récupère le plat qui contient le type et le poids
                platComplet = new PlatComplet(plat.getPlatId(), plat.getTypePlat(), plat.getPoids(), rs.getString("nom_plat"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platComplet;
    }

    private PlatCompose getPlatComposeById(int platId) {
        PlatCompose platCompose = null;
        String sql = "SELECT * FROM PlatCompose WHERE plat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, platId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // On doit aussi récupérer le type de plat et le poids depuis la table Plat
                Plat plat = getPlatById(platId); // On récupère le plat qui contient le type et le poids
                Viande viande = new ViandeDAO().getViandeById(rs.getInt("viande_id"));
                Accompagnement accompagnement = new AccompagnementDAO().getAccompagnementById(rs.getInt("accompagnement_id"));
                platCompose = new PlatCompose(plat.getPlatId(), plat.getPoids(), viande, accompagnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platCompose;
    }

}
