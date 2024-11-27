package fr.tanchou.menudlasemaine.dao;
import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.models.produit.Entree;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MenuDAO {
    public static void insertMenu(Repas[][] repasParJour) {

        String insertSQL = "INSERT INTO Menu (jour, moment, entree, plat) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            String[] moments = {"midi", "soir"};
            for (int i = 0; i < repasParJour.length; i++) {
                for (int j = 0; j < repasParJour[i].length; j++) {
                    pstmt.setString(1, joursSemaine[i]);
                    pstmt.setString(2, moments[j]); // ou "Soir" selon la position
                    pstmt.setString(3, repasParJour[i][j].getEntree() != null ? repasParJour[i][j].getEntree().getNom() : "Aucune");
                    pstmt.setString(4, repasParJour[i][j].getPlat() != null ? repasParJour[i][j].getPlat().getNomPlat() : "Aucun");
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMenu(Repas[][] repasParJour) {
        // SQL pour supprimer les anciens enregistrements
        String deleteSQL = "DELETE FROM Menu";
        String insertSQL = "INSERT INTO Menu (jour, moment, entree, plat) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement deletePstmt = conn.prepareStatement(deleteSQL);
             PreparedStatement insertPstmt = conn.prepareStatement(insertSQL)) {

            // Supprimer tous les anciens enregistrements
            deletePstmt.executeUpdate();

            String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            String[] moments = {"midi", "soir"};

            for (int i = 0; i < repasParJour.length; i++) {
                for (int j = 0; j < repasParJour[i].length; j++) {
                    insertPstmt.setString(1, joursSemaine[i]);
                    insertPstmt.setString(2, moments[j]); // "Midi" ou "Soir" selon la position
                    insertPstmt.setString(3, repasParJour[i][j].getEntree() != null ? repasParJour[i][j].getEntree().getNom() : "Aucune");
                    insertPstmt.setString(4, repasParJour[i][j].getPlat() != null ? repasParJour[i][j].getPlat().getNomPlat() : "Aucun");
                    insertPstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //methode pour recuperer le menu
    public static String getMenu() {
        StringBuilder menuBuilder = new StringBuilder();
        String selectSQL = "SELECT jour, moment, entree, plat FROM Menu ORDER BY FIELD(jour, 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi', 'dimanche'), FIELD(moment, 'midi', 'soir')";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            String currentJour = "";
            while (rs.next()) {
                String jour = rs.getString("jour");
                String moment = rs.getString("moment");
                String entree = rs.getString("entree");
                String plat = rs.getString("plat");

                // Ajouter le nom du jour si on passe à un jour différent
                if (!jour.equals(currentJour)) {
                    if (!currentJour.isEmpty()) {
                        menuBuilder.append("\n"); // Saut de ligne entre chaque jour
                    }
                    menuBuilder.append(jour.substring(0, 1).toUpperCase() + jour.substring(1)).append(":\n");
                    currentJour = jour;
                }

                // Concaténer le moment, l'entrée et le plat pour chaque repas
                menuBuilder.append("  - ").append(moment).append(": ");
                if (entree != null && !entree.equals("Aucune")) {
                    menuBuilder.append(entree).append(" - ");
                }
                menuBuilder.append(plat).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return menuBuilder.toString();
    }

}
