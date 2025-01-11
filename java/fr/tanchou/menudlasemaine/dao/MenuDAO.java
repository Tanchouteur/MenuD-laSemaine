package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.menu.*;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cette classe gère l'accès aux données du menu de la semaine.
 * Elle permet de récupérer, insérer, mettre à jour et afficher le menu.
 */
public class MenuDAO {
    private Menu menu;
    private boolean isMenuUpTodate = false;
    private final ProduitDAO produitDAO;

    /**
     * Constructeur de MenuDAO.
     *
     * @param produitDAO Une instance de ProduitDAO pour récupérer les informations des produits.
     */
    public MenuDAO(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
        this.menu = this.getMenu();
    }

    /**
     * Récupère le menu de la semaine. Si le menu n'est pas à jour, il est rechargé depuis la base de données.
     *
     * @return Le menu de la semaine.
     */
    public Menu getMenu() {
        if (!isMenuUpTodate) {
            menu = getMenuObject();
            isMenuUpTodate = true;
        }
        return menu;
    }

    /**
     * Insère un nouveau menu dans la base de données.
     *
     * @param listRepas Un tableau 2D représentant les repas de la semaine.
     */
    public void insertMenu(Repas[][] listRepas) {

        String insertSQL = "INSERT INTO Menu (jour, moment, entree, plat) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            semaine(listRepas, pstmt);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du menu. " + e.getMessage());
        }
    }

    /**
     * Insère les repas dans la base de données pour chaque jour et moment de la semaine.
     *
     * @param listRepas Un tableau 2D représentant les repas de la semaine.
     * @param pstmt     L'objet PreparedStatement utilisé pour insérer les données.
     * @throws SQLException Si une erreur SQL se produit.
     */
    private void semaine(Repas[][] listRepas, PreparedStatement pstmt) throws SQLException {
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] moments = {"midi", "soir"};
        for (int i = 0; i < listRepas.length; i++) {
            for (int j = 0; j < listRepas[i].length; j++) {
                pstmt.setString(1, joursSemaine[i]);
                pstmt.setString(2, moments[j]); // ou "Soir" selon la position
                pstmt.setString(3, listRepas[i][j].entree() != null ? listRepas[i][j].entree().getNomProduit() : "Aucune");
                pstmt.setString(4, listRepas[i][j].plat() != null ? listRepas[i][j].plat().getNomPlat() : "Aucun");
                pstmt.executeUpdate();
            }
        }
        isMenuUpTodate = false;
    }

    /**
     * Met à jour le menu de la semaine dans la base de données.
     * Supprime les anciens repas et insère les nouveaux.
     *
     * @param listRepas Un tableau 2D représentant les nouveaux repas de la semaine.
     */
    public void updateMenu(Repas[][] listRepas) {
        // SQL pour supprimer les anciens enregistrements
        String deleteSQL = "DELETE FROM Menu";
        String insertSQL = "INSERT INTO Menu (jour, moment, entree, plat) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement deletePstmt = conn.prepareStatement(deleteSQL);
             PreparedStatement insertPstmt = conn.prepareStatement(insertSQL)) {

            // Supprimer tous les anciens enregistrements
            deletePstmt.executeUpdate();

            semaine(listRepas, insertPstmt);
        } catch (SQLException e) {
            System.err.println("------------------------ Menu - DAO ---------------------------------");
            System.err.println("Erreur lors de la mise à jour du menu. " + e.getMessage());
            System.err.println("---------------------------------------------------------------------");
        }
    }

    /**
     * Récupère le menu sous forme de chaîne de caractères.
     *
     * @return Le menu de la semaine sous forme de chaîne de caractères.
     */
    public String getMenuAsString() {
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

    /**
     * Récupère l'objet Menu représentant les repas de la semaine à partir de la base de données.
     *
     * @return Un objet Menu représentant le menu de la semaine.
     */
    private Menu getMenuObject() {
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

                try {
                    MomentJournee.valueOf(momentStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Moment invalide: " + momentStr);
                    continue; // Passer à la ligne suivante si le moment est invalide
                }

                // Récupérer l'objet Produits pour l'entrée
                Produits entreeProduit = (entree != null && !entree.equalsIgnoreCase("Aucune")) ? produitDAO.getProduitByName(entree) : null;

                // Récupérer l'objet Produits pour le plat
                Produits platCompletProduits = (plat != null && !plat.isEmpty()) ? produitDAO.getProduitByName(plat) : null;

                Accompagnement accompagnement = null;
                Repas repas;

                if (platCompletProduits == null) { // Si c'est un plat composé
                    if (plat != null && plat.contains(",")) {
                        String[] produitsList = plat.split(",");

                        if (produitsList.length < 2) {
                            System.err.println("Erreur de format pour le plat composé: " + plat);
                            continue; // Passer à la ligne suivante si le format du plat est incorrect
                        }

                        Produits viande = produitDAO.getProduitByName(produitsList[0]);
                        Produits legume = produitDAO.getProduitByName(produitsList[1]);
                        Produits feculent = (produitsList.length > 2) ? produitDAO.getProduitByName(produitsList[2]) : null;

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
            System.err.println("------------------------ Menu - DAO ---------------------------------");
            System.err.println("Erreur lors de la récupération du menu. " + e.getMessage());
            System.err.println("---------------------------------------------------------------------");
        }

        return null;
    }

    /**
     * Met à jour un repas spécifique dans la base de données.
     *
     * @param repasToUpdate L'objet Repas à mettre à jour.
     * @param jour          Le jour du repas à mettre à jour.
     * @param moment        Le moment de la journée du repas à mettre à jour (midi ou soir).
     */
    public void updateRepas(Repas repasToUpdate, String jour, MomentJournee moment) {

        // SQL pour mettre à jour un repas spécifique dans la base de données
        String updateSQL = "UPDATE Menu SET entree = ?, plat = ? WHERE jour = ? AND moment = ?";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement updatePstmt = conn.prepareStatement(updateSQL)) {

            // Définir les paramètres de la requête SQL
            updatePstmt.setString(1, repasToUpdate.entree() != null ? repasToUpdate.entree().getNomProduit() : "Aucune");
            updatePstmt.setString(2, repasToUpdate.plat() != null ? repasToUpdate.plat().getNomPlat() : "Aucun");
            updatePstmt.setString(3, jour); // Le jour (ex : "Lundi")
            updatePstmt.setString(4, moment.toString()); // Le moment ("midi" ou "soir")

            // Exécuter la mise à jour
            int rowsAffected = updatePstmt.executeUpdate();

            // Afficher un message pour indiquer si la mise à jour a réussi
            if (rowsAffected > 0) {
                System.out.println("Le repas a été mis à jour avec succès pour " + jour + " - " + moment);
                isMenuUpTodate = false;
            } else {
                System.out.println("Aucun repas n'a été trouvé pour " + jour + " - " + moment);
            }
        } catch (SQLException e) {
            System.err.println("------------------------ Menu - DAO ---------------------------------");
            System.err.println("Erreur lors de la mise à jour du repas. " + e.getMessage());
            System.err.println("---------------------------------------------------------------------");
        }
    }
}
