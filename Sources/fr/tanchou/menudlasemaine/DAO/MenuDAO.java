package fr.tanchou.menudlasemaine.DAO;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.models.Repas;

import java.sql.*;

public class MenuDAO {

    public void ajouterMenu(Menu menu) {
        String sql = "INSERT INTO Menu (lundi_midi_repas_id, lundi_soir_repas_id, mardi_midi_repas_id, mardi_soir_repas_id, " +
                "mercredi_midi_repas_id, mercredi_soir_repas_id, jeudi_midi_repas_id, jeudi_soir_repas_id, " +
                "vendredi_midi_repas_id, vendredi_soir_repas_id, samedi_midi_repas_id, samedi_soir_repas_id, " +
                "dimanche_midi_repas_id, dimanche_soir_repas_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, menu.getRepasParJour().get("Lundi")[0].getRepasId());
            pstmt.setInt(2, menu.getRepasParJour().get("Lundi")[1].getRepasId());
            pstmt.setInt(3, menu.getRepasParJour().get("Mardi")[0].getRepasId());
            pstmt.setInt(4, menu.getRepasParJour().get("Mardi")[1].getRepasId());
            pstmt.setInt(5, menu.getRepasParJour().get("Mercredi")[0].getRepasId());
            pstmt.setInt(6, menu.getRepasParJour().get("Mercredi")[1].getRepasId());
            pstmt.setInt(7, menu.getRepasParJour().get("Jeudi")[0].getRepasId());
            pstmt.setInt(8, menu.getRepasParJour().get("Jeudi")[1].getRepasId());
            pstmt.setInt(9, menu.getRepasParJour().get("Vendredi")[0].getRepasId());
            pstmt.setInt(10, menu.getRepasParJour().get("Vendredi")[1].getRepasId());
            pstmt.setInt(11, menu.getRepasParJour().get("Samedi")[0].getRepasId());
            pstmt.setInt(12, menu.getRepasParJour().get("Samedi")[1].getRepasId());
            pstmt.setInt(13, menu.getRepasParJour().get("Dimanche")[0].getRepasId());
            pstmt.setInt(14, menu.getRepasParJour().get("Dimanche")[1].getRepasId());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menu.setMenuId(generatedKeys.getInt(1)); // Mettre à jour l'ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Menu getMenuById(int menuId) {
        String sql = "SELECT * FROM Menu WHERE menu_id = ?";
        Menu menu = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                menu = new Menu(menuId);
                // Récupérer les repas pour chaque jour
                String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
                for (String jour : jours) {
                    Repas[] repasDuJour = new Repas[2];
                    repasDuJour[0] = new RepasDAO().getRepasById(rs.getInt(jour.toLowerCase() + "_midi_repas_id"));
                    repasDuJour[1] = new RepasDAO().getRepasById(rs.getInt(jour.toLowerCase() + "_soir_repas_id"));
                    menu.getRepasParJour().put(jour, repasDuJour);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }

    public void updateMenu(Menu menu) {
        String sql = "UPDATE Menu SET lundi_midi_repas_id = ?, lundi_soir_repas_id = ?, mardi_midi_repas_id = ?, " +
                "mardi_soir_repas_id = ?, mercredi_midi_repas_id = ?, mercredi_soir_repas_id = ?, " +
                "jeudi_midi_repas_id = ?, jeudi_soir_repas_id = ?, vendredi_midi_repas_id = ?, " +
                "vendredi_soir_repas_id = ?, samedi_midi_repas_id = ?, samedi_soir_repas_id = ?, " +
                "dimanche_midi_repas_id = ?, dimanche_soir_repas_id = ? WHERE menu_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menu.getRepasParJour().get("Lundi")[0].getRepasId());
            pstmt.setInt(2, menu.getRepasParJour().get("Lundi")[1].getRepasId());
            pstmt.setInt(3, menu.getRepasParJour().get("Mardi")[0].getRepasId());
            pstmt.setInt(4, menu.getRepasParJour().get("Mardi")[1].getRepasId());
            pstmt.setInt(5, menu.getRepasParJour().get("Mercredi")[0].getRepasId());
            pstmt.setInt(6, menu.getRepasParJour().get("Mercredi")[1].getRepasId());
            pstmt.setInt(7, menu.getRepasParJour().get("Jeudi")[0].getRepasId());
            pstmt.setInt(8, menu.getRepasParJour().get("Jeudi")[1].getRepasId());
            pstmt.setInt(9, menu.getRepasParJour().get("Vendredi")[0].getRepasId());
            pstmt.setInt(10, menu.getRepasParJour().get("Vendredi")[1].getRepasId());
            pstmt.setInt(11, menu.getRepasParJour().get("Samedi")[0].getRepasId());
            pstmt.setInt(12, menu.getRepasParJour().get("Samedi")[1].getRepasId());
            pstmt.setInt(13, menu.getRepasParJour().get("Dimanche")[0].getRepasId());
            pstmt.setInt(14, menu.getRepasParJour().get("Dimanche")[1].getRepasId());
            pstmt.setInt(15, menu.getMenuId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMenu(int menuId) {
        String sql = "DELETE FROM Menu WHERE menu_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
