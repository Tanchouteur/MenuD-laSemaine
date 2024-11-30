package fr.tanchou.menudlasemaine.dao;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.menu.*;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuDAO {
    public static void insertMenu(Repas[][] listRepas) {

        String insertSQL = "INSERT INTO Menu (jour, moment, entree, plat) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            String[] moments = {"midi", "soir"};
            for (int i = 0; i < listRepas.length; i++) {
                for (int j = 0; j < listRepas[i].length; j++) {
                    pstmt.setString(1, joursSemaine[i]);
                    pstmt.setString(2, moments[j]); // ou "Soir" selon la position
                    pstmt.setString(3, listRepas[i][j].getEntree() != null ? listRepas[i][j].getEntree().getNomProduit() : "Aucune");
                    pstmt.setString(4, listRepas[i][j].getPlat() != null ? listRepas[i][j].getPlat().getNomPlat() : "Aucun");
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMenu(Repas[][] listRepas) {
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

            for (int i = 0; i < listRepas.length; i++) {
                for (int j = 0; j < listRepas[i].length; j++) {
                    insertPstmt.setString(1, joursSemaine[i]);
                    insertPstmt.setString(2, moments[j]); // "Midi" ou "Soir" selon la position
                    insertPstmt.setString(3, listRepas[i][j].getEntree() != null ? listRepas[i][j].getEntree().getNomProduit() : "Aucune");
                    insertPstmt.setString(4, listRepas[i][j].getPlat() != null ? listRepas[i][j].getPlat().getNomPlat() : "Aucun");
                    insertPstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //methode pour recuperer le menu
    public static String getMenuAsString() {
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
            System.err.println("Erreur lors de la récupération du menu sous forme de chaine de character." + e.getMessage());
        }

        return menuBuilder.toString();
    }

    //methode pour recuperer le menu
    public static Menu getMenu() {
        Repas[][] listRepas = new Repas[7][2];
        String selectSQL = "SELECT jour, moment, entree, plat FROM Menu ORDER BY FIELD(jour, 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi', 'dimanche'), FIELD(moment, 'midi', 'soir')";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            int i = 0;
            int j = 0;
            while (rs.next()) {
                String jour = rs.getString("jour");
                String momentStr = rs.getString("moment");
                String entree = rs.getString("entree");
                String plat = rs.getString("plat");

                System.out.println("Traitement du jour: " + jour);
                System.out.println("Moment: " + momentStr);
                System.out.println("Entrée: " + entree);
                System.out.println("Plat: " + plat);

                MomentJournee moment;
                try {
                    moment = MomentJournee.valueOf(momentStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Moment invalide: " + momentStr);
                    continue; // Passer à la ligne suivante si le moment est invalide
                }

                // Récupérer l'objet Produits pour l'entrée
                Produits entreeProduit = (entree != null && !entree.equalsIgnoreCase("Aucune")) ? ProduitDAO.getProduitByName(entree) : null;

                // Récupérer l'objet Produits pour le plat
                Produits platCompletProduits = (plat != null && !plat.isEmpty()) ? ProduitDAO.getProduitByName(plat) : null;

                Accompagnement accompagnement = null;
                Repas repas;

                if (platCompletProduits == null) { // Si c'est un plat composé
                    if (plat != null && plat.contains(",")) {
                        String[] produitsList = plat.split(",");

                        if (produitsList.length < 2) {
                            System.err.println("Erreur de format pour le plat composé: " + plat);
                            continue; // Passer à la ligne suivante si le format du plat est incorrect
                        }

                        Produits viande = ProduitDAO.getProduitByName(produitsList[0]);
                        Produits legume = ProduitDAO.getProduitByName(produitsList[1]);
                        Produits feculent = (produitsList.length > 2) ? ProduitDAO.getProduitByName(produitsList[2]) : null;

                        accompagnement = new Accompagnement(legume, feculent);
                        PlatCompose platCompose = new PlatCompose(viande, accompagnement);
                        repas = new Repas(entreeProduit, platCompose);
                    } else {
                        System.err.println("Erreur de format pour le plat: " + plat);
                        continue; // Passer à la ligne suivante si le format du plat est incorrect
                    }
                } else {
                    // Cas où c'est un plat complet
                    PlatComplet platComplet = new PlatComplet(platCompletProduits);
                    repas = new Repas(entreeProduit, platComplet);
                }

                listRepas[i][j] = repas;

                j++;
                if (j == 2) {
                    j = 0;
                    i++;
                }
            }

            return new Menu(listRepas);

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du menu. " + e.getMessage());
        }

        return null;
    }


    public static void updateRepas(Repas repasToUpdate, String jour, MomentJournee moment) {
        // SQL pour mettre à jour un repas spécifique dans la base de données
        String updateSQL = "UPDATE Menu SET entree = ?, plat = ? WHERE jour = ? AND moment = ?";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement updatePstmt = conn.prepareStatement(updateSQL)) {

            // Définir les paramètres de la requête SQL
            updatePstmt.setString(1, repasToUpdate.getEntree() != null ? repasToUpdate.getEntree().getNomProduit() : "Aucune");
            updatePstmt.setString(2, repasToUpdate.getPlat() != null ? repasToUpdate.getPlat().getNomPlat() : "Aucun");
            updatePstmt.setString(3, jour); // Le jour (ex : "Lundi")
            updatePstmt.setString(4, moment.toString()); // Le moment ("midi" ou "soir")

            // Exécuter la mise à jour
            int rowsAffected = updatePstmt.executeUpdate();

            // Afficher un message pour indiquer si la mise à jour a réussi
            if (rowsAffected > 0) {
                //System.out.println("Le repas a été mis à jour avec succès pour " + jour + " - " + moment);
            } else {
                System.out.println("Aucun repas n'a été trouvé pour " + jour + " - " + moment);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du repas. " + e.getMessage());
        }
    }


}
