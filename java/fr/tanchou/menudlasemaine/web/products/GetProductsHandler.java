package fr.tanchou.menudlasemaine.web.products;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.produit.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.produit.Produits;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class GetProductsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("GetProduct : " + exchange.getRequestURI());
        String requestURI = exchange.getRequestURI().toString();

        // Extraire le type de produit de l'URL (par exemple : /products/entree)
        String productType = requestURI.split("/")[2];
        System.out.println("productType : " + productType);
        // Récupérer les produits du type demandé

        String response = getProductsByType(productType);


        System.out.println("response : " + response);
        // Définir le type de contenu comme JSON
        exchange.getResponseHeaders().set("Content-Type", "application/json"); // Changer XML en JSON

        byte[] responseBytes = response.getBytes();

        // Envoyer les en-têtes de réponse avec le code HTTP 200
        exchange.sendResponseHeaders(200, responseBytes.length);

        // Envoyer la réponse JSON dans le corps de la réponse
        OutputStream os = exchange.getResponseBody();

        os.write(responseBytes); // Utiliser le tableau de bytes pour envoyer la réponse
        os.close();
    }


    private String getProductsByType(String type) {
        // Récupérer la liste de produits du DAO
        List<?> produits = ProduitDAO.getAllProduitByType(type);

        // Utiliser un StringBuilder pour construire le JSON
        StringBuilder jsonBuilder = new StringBuilder();

        // Début du tableau JSON
        jsonBuilder.append("[");

        // Parcours de la liste des produits
        for (int i = 0; i < produits.size(); i++) {
            Object produit = produits.get(i);

            // Commencer un objet JSON pour chaque produit
            jsonBuilder.append("{");

            // Gérer les différents types de produits, ici un exemple pour "Entree"
            if (produit instanceof Produits entree) {

                jsonBuilder.append("\"nom_entree\": \"")
                        .append(entree.getNom()).append("\", ")
                        .append("\"poids\": ").append(entree.getPoids()).append("\"");
            }

            // Fin de l'objet JSON
            jsonBuilder.append("}");

            // Si ce n'est pas le dernier élément de la liste, ajouter une virgule
            if (i < produits.size() - 1) {
                jsonBuilder.append(", ");
            }
        }

        // Fin du tableau JSON
        jsonBuilder.append("]");

        return jsonBuilder.toString();
    }
}
