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

    // Méthode pour récupérer tous les produits
    public static Map<TypeProduit, LinkedList<Produits>> getAllProduits() {

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
    public static Produits getProduitByName(String nomProduit) {
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
                    TypeProduit typeProduit = TypeProduit.valueOf(resultSet.getString("typeProduit"));
                    int poidsArbitraire = resultSet.getInt("poidsArbitraire");
                    LocalDate lastUsed = resultSet.getDate("lastUsed").toLocalDate();
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
                    produit = new Produits(id, nomProduit, poidsArbitraire, lastUsed, typeProduit, poidsMoment, poidsSaison);
                }
            }
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd getProduitByName(); " + e.getMessage());
        }

        return produit;
    }

    // Méthode pour ajouter un produit avec la procedure stockée
    public static void addProduit(String nomProduit, TypeProduit typeProduit, int poidsArbitraire, int[] poidsMoment, int[] poidsSaison) {
        String query = "{CALL addProduit(" + nomProduit + ", " + typeProduit + ", " + poidsArbitraire + ", " + String.valueOf(poidsMoment[0]) + ", " + String.valueOf(poidsMoment[1]) + ", " + String.valueOf(poidsMoment[2]) + ", " + String.valueOf(poidsMoment[3]) + ", " + poidsSaison[0] + ", " + poidsSaison[1] + ", " + poidsSaison[2] + ", " + poidsSaison[3] + ")}";
        query = query.replace("[", "").replace("]", "");
        query = query.replace(" ", "");
        try {
            Connection connection = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd addProduit(); " + e.getMessage());
        }
    }

    // Méthode pour supprimer un produit avec la procedure stockée
    public static void deleteProduit(String nomProduit) {
        String query = "{CALL supprimer_produit(" + nomProduit + ")}";
        try {
            Connection connection = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd deleteProduit(); " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour un produit avec la procedure stockée
    public static void updateProduit(String nomProduit, TypeProduit typeProduit, int poidsArbitraire, int[] poidsMoment, int[] poidsSaison) {
        String query = "{CALL updateProduit(" + nomProduit + ", " + typeProduit + ", " + poidsArbitraire + ", " + String.valueOf(poidsMoment[0]) + ", " + String.valueOf(poidsMoment[1]) + ", " + String.valueOf(poidsMoment[2]) + ", " + String.valueOf(poidsMoment[3]) + ", " + poidsSaison[0] + ", " + poidsSaison[1] + ", " + poidsSaison[2] + ", " + poidsSaison[3] + ")}";
        query = query.replace("[", "").replace("]", "");
        query = query.replace(" ", "");
        try {
            Connection connection = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Probleme de connexion a la bd updateProduit(); " + e.getMessage());
        }
    }
}