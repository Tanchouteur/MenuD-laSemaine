package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.VueMenuSemaine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VueMenuSemaineDAO {

    public List<VueMenuSemaine> getAllMenus() {
        List<VueMenuSemaine> menus = new ArrayList<>();
        String sql = "SELECT * FROM VueMenuSemaine"; // Assurez-vous que ce soit le nom correct de la vue

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                VueMenuSemaine menu = new VueMenuSemaine(
                        rs.getInt("menu_id"),
                        rs.getString("lundi_midi_entree"),
                        rs.getString("lundi_midi_plat"),
                        rs.getString("lundi_soir_entree"),
                        rs.getString("lundi_soir_plat"),
                        rs.getString("mardi_midi_entree"),
                        rs.getString("mardi_midi_plat"),
                        rs.getString("mardi_soir_entree"),
                        rs.getString("mardi_soir_plat"),
                        rs.getString("mercredi_midi_entree"),
                        rs.getString("mercredi_midi_plat"),
                        rs.getString("mercredi_soir_entree"),
                        rs.getString("mercredi_soir_plat"),
                        rs.getString("jeudi_midi_entree"),
                        rs.getString("jeudi_midi_plat"),
                        rs.getString("jeudi_soir_entree"),
                        rs.getString("jeudi_soir_plat"),
                        rs.getString("vendredi_midi_entree"),
                        rs.getString("vendredi_midi_plat"),
                        rs.getString("vendredi_soir_entree"),
                        rs.getString("vendredi_soir_plat"),
                        rs.getString("samedi_midi_entree"),
                        rs.getString("samedi_midi_plat"),
                        rs.getString("samedi_soir_entree"),
                        rs.getString("samedi_soir_plat"),
                        rs.getString("dimanche_midi_entree"),
                        rs.getString("dimanche_midi_plat"),
                        rs.getString("dimanche_soir_entree"),
                        rs.getString("dimanche_soir_plat")
                );
                menus.add(menu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menus;
    }

    public VueMenuSemaine getMenuById(int menuId) {
        String sql = "SELECT * FROM VueMenuSemaine WHERE menu_id = ?";
        VueMenuSemaine menu = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                menu = new VueMenuSemaine(
                        rs.getInt("menu_id"),
                        rs.getString("lundi_midi_entree"),
                        rs.getString("lundi_midi_plat"),
                        rs.getString("lundi_soir_entree"),
                        rs.getString("lundi_soir_plat"),
                        rs.getString("mardi_midi_entree"),
                        rs.getString("mardi_midi_plat"),
                        rs.getString("mardi_soir_entree"),
                        rs.getString("mardi_soir_plat"),
                        rs.getString("mercredi_midi_entree"),
                        rs.getString("mercredi_midi_plat"),
                        rs.getString("mercredi_soir_entree"),
                        rs.getString("mercredi_soir_plat"),
                        rs.getString("jeudi_midi_entree"),
                        rs.getString("jeudi_midi_plat"),
                        rs.getString("jeudi_soir_entree"),
                        rs.getString("jeudi_soir_plat"),
                        rs.getString("vendredi_midi_entree"),
                        rs.getString("vendredi_midi_plat"),
                        rs.getString("vendredi_soir_entree"),
                        rs.getString("vendredi_soir_plat"),
                        rs.getString("samedi_midi_entree"),
                        rs.getString("samedi_midi_plat"),
                        rs.getString("samedi_soir_entree"),
                        rs.getString("samedi_soir_plat"),
                        rs.getString("dimanche_midi_entree"),
                        rs.getString("dimanche_midi_plat"),
                        rs.getString("dimanche_soir_entree"),
                        rs.getString("dimanche_soir_plat")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }
}
