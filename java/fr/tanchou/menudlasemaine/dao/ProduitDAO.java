package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

public class ProduitDAO {
    private Map<TypeProduit, LinkedList<Produits>> produitListMap;
    private boolean isUpTodate = false;
    public ProduitDAO() {
        this.produitListMap = this.getAllProduits();
        isUpTodate = true;
    }

    public LinkedList<Produits> getProduitsByType(TypeProduit typeProduit) {
        if (isUpTodate) {
            return produitListMap.get(typeProduit);
        }else {
            this.produitListMap = this.getAllProduits();
            isUpTodate = true;
            return produitListMap.get(typeProduit);
        }
    }

    // Méthode pour récupérer tous les produits
    public Map<TypeProduit, LinkedList<Produits>> getAllProduits() {

        Map<TypeProduit, LinkedList<Produits>> produitsMap = new HashMap<>();
        String query = "SELECT * FROM Produits " +
                "LEFT JOIN PoidsMoment ON Produits.id = PoidsMoment.idProduit " +
                "LEFT JOIN PoidsSaison ON Produits.id = PoidsSaison.idProduit";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Récupération des informations de la ligne courante
                int id = resultSet.getInt("id");

                String nomProduit = resultSet.getString("nomProduit");

                TypeProduit typeProduit = TypeProduit.valueOf(resultSet.getString("typeProduit").toUpperCase());

                int poidsArbitraire = resultSet.getInt("poidsArbitraire");

                Date lastUsed = resultSet.getDate("dateLastUsed");
                LocalDate dateLastUsed = null;
                if (lastUsed != null) {
                    dateLastUsed = lastUsed.toLocalDate();
                }

                int[] poidsMoment = new int[]{
                        resultSet.getInt("poidsMidiSemaine"),
                        resultSet.getInt("poidsSoirSemaine"),
                        resultSet.getInt("poidsMidiWeekend"),
                        resultSet.getInt("poidsSoirWeekend")};

                int[] poidsSaison = new int[]{
                        resultSet.getInt("poidsPrintemps"),
                        resultSet.getInt("poidsEte"),
                        resultSet.getInt("poidsAutomne"),
                        resultSet.getInt("poidsHiver")
                };


                // Création de l'objet Produit
                Produits produit = new Produits(id, nomProduit, poidsArbitraire, dateLastUsed, typeProduit, poidsMoment, poidsSaison);

                // Ajout du produit dans la liste correspondante dans la map
                produitsMap.computeIfAbsent(typeProduit, k -> new LinkedList<>()).add(produit);
            }
        } catch (Exception e) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println("Probleme dans le try de connexion a la bd getAllProduits(); " + e.getMessage());
            System.out.println("---------------------------------------------------------------------");
        }

        return produitsMap;
    }

    // Méthode pour récupérer un produit par son nom
    public Produits getProduitByName(String nomProduit) {
        Produits produit = null;
        String query = "SELECT * FROM Produits " +
                "LEFT JOIN PoidsMoment ON Produits.id = PoidsMoment.idProduit " +
                "LEFT JOIN PoidsSaison ON Produits.id = PoidsSaison.idProduit " +
                "WHERE nomProduit = ?";

        try (Connection connection = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, nomProduit);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Récupération des informations de la ligne courante
                    int id = resultSet.getInt("id");
                    TypeProduit typeProduit = TypeProduit.valueOf(resultSet.getString("typeProduit").toUpperCase());

                    int poidsArbitraire = resultSet.getInt("poidsArbitraire");

                    Date lastUsed = resultSet.getDate("dateLastUsed");
                    LocalDate dateLastUsed = null;
                    if (lastUsed != null) {
                        dateLastUsed = lastUsed.toLocalDate();
                    }


                    int[] poidsMoment = new int[]{
                            resultSet.getInt("poidsMidiSemaine"),
                            resultSet.getInt("poidsSoirSemaine"),
                            resultSet.getInt("poidsMidiWeekend"),
                            resultSet.getInt("poidsSoirWeekend")
                    };

                    int[] poidsSaison = new int[]{
                            resultSet.getInt("poidsPrintemps"),
                            resultSet.getInt("poidsEte"),
                            resultSet.getInt("poidsAutomne"),
                            resultSet.getInt("poidsHiver")
                    };

                    // Création de l'objet Produit
                    produit = new Produits(id, nomProduit, poidsArbitraire, dateLastUsed, typeProduit, poidsMoment, poidsSaison);
                }
            }
        } catch (Exception e) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println("Probleme de connexion a la bd getProduitByName(); " + e.getMessage());
            System.out.println("---------------------------------------------------------------------");
        }

        return produit;
    }

    // Méthode pour ajouter un produit avec la procedure stockée
    public void addProduit(Produits produit) {
        String query = "{CALL ajouter_produit(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"; // Utilisation de paramètres pour éviter les erreurs

        try {
            // Obtenir la connexion
            Connection connection = DatabaseConnection.getDataSource().getConnection();

            // Préparer la requête
            PreparedStatement statement = connection.prepareStatement(query);

            // Remplir les paramètres
            statement.setString(1, produit.getNomProduit());
            statement.setString(2, produit.getType().toString());
            statement.setInt(3, produit.getPoidsArbitraire());
            statement.setInt(4, produit.getPoidsMoment()[0]);
            statement.setInt(5, produit.getPoidsMoment()[1]);
            statement.setInt(6, produit.getPoidsMoment()[2]);
            statement.setInt(7, produit.getPoidsMoment()[3]);
            statement.setInt(8, produit.getPoidsSaison()[0]);
            statement.setInt(9, produit.getPoidsSaison()[1]);
            statement.setInt(10, produit.getPoidsSaison()[2]);
            statement.setInt(11, produit.getPoidsSaison()[3]);

            // Exécuter la procédure stockée
            statement.executeUpdate();
            System.out.println("Produit ajouté avec succès.");
            this.isUpTodate = false;

        } catch (Exception e) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println("Probleme dans le try de addProduit(); " + e.getMessage());
            System.out.println("---------------------------------------------------------------------");
        }
    }

    // Méthode pour supprimer un produit avec la procedure stockée
    public void deleteProduit(String nomProduit) {
        String query = "{CALL supprimer_produit(" + nomProduit + ")}";
        try {
            Connection connection = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            this.isUpTodate = false;
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd deleteProduit(); " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour un produit avec la procedure stockée
    public void updateProduit(String nomProduit, TypeProduit typeProduit, int poidsArbitraire, int[] poidsMoment, int[] poidsSaison) {
        String query = "{CALL updateProduit(" + nomProduit + ", " + typeProduit + ", " + poidsArbitraire + ", " + String.valueOf(poidsMoment[0]) + ", " + String.valueOf(poidsMoment[1]) + ", " + String.valueOf(poidsMoment[2]) + ", " + String.valueOf(poidsMoment[3]) + ", " + poidsSaison[0] + ", " + poidsSaison[1] + ", " + poidsSaison[2] + ", " + poidsSaison[3] + ")}";
        query = query.replace("[", "").replace("]", "");
        query = query.replace(" ", "");
        try {
            Connection connection = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            this.isUpTodate = false;
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd updateProduit(); " + e.getMessage());
        }
    }
}