package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

public class ProduitDAO {

    // Méthode pour récupérer tous les produits
    public static Map<TypeProduit, LinkedList<Produits>> getAllProduits() {
        // La map pour stocker les listes de produits par type de produit
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
                TypeProduit typeProduit = TypeProduit.valueOf(resultSet.getString("typeProduit"));
                int poidsArbitraire = resultSet.getInt("poidsArbitraire");
                LocalDate lastUsed = resultSet.getDate("lastUsed").toLocalDate();
                int[][] poidsMoment = new int[][]{
                        { resultSet.getInt("poidsMidi"), resultSet.getInt("poidsSoir")},
                        { resultSet.getInt("poidsMidi"), resultSet.getInt("poidsSoir")}
                };

                int[] poidsSaison = new int[]{
                        resultSet.getInt("poidsHiver"),
                        resultSet.getInt("poidsPrintemps"),
                        resultSet.getInt("poidsEte"),
                        resultSet.getInt("poidsAutomne")
                };


                // Création de l'objet Produit
                Produits produit = new Produits(id, nomProduit, poidsArbitraire, lastUsed, typeProduit, poidsMoment, poidsSaison);

                // Ajout du produit dans la liste correspondante dans la map
                produitsMap.computeIfAbsent(typeProduit, k -> new LinkedList<>()).add(produit);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'exception selon tes besoins
        }

        return produitsMap;
    }
}