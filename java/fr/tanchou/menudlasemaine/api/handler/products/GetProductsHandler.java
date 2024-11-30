package fr.tanchou.menudlasemaine.api.handler.products;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.Factory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class GetProductsHandler implements HttpHandler {
    private final Factory factory;

    public GetProductsHandler(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("GetProduct : " + exchange.getRequestURI());
        String requestURI = exchange.getRequestURI().toString();

        // Extraire le type de produit de l'URL (par exemple : /products/entree)
        String productType = requestURI.split("/")[2];
        System.out.println("productType : " + productType);
        // Récupérer les produits du type demandé

        TypeProduit type = TypeProduit.valueOf(productType.toUpperCase());
        String response = getProductsByType(type);

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

    private String getProductsByType(TypeProduit type) {
        // Récupérer la liste de produits du DAO
        List<Produits> produits = factory.getWeightManager().getProduitsByType(type);

        // Utiliser un StringBuilder pour construire le JSON
        StringBuilder jsonBuilder = new StringBuilder();

        // Début du tableau JSON
        jsonBuilder.append("[");

        // Parcourir la liste des produits
        for (int i = 0; i < produits.size(); i++) {
            Produits produit = produits.get(i);

            // Ajouter les propriétés du produit dans l'objet JSON
            jsonBuilder.append("{")
                    .append("\"nom\": \"").append(produit.getNomProduit()).append("\", ")
                    .append("\"poids\": \"").append(produit.getPoidsArbitraire()).append("\", ")
                    .append("\"last_use\": \"").append(produit.getLastUsed()).append("\"")
                    .append("}");

            // Ajouter une virgule sauf pour le dernier élément
            if (i < produits.size() - 1) {
                jsonBuilder.append(", ");
            }
        }

        // Fin du tableau JSON
        jsonBuilder.append("]");

        return jsonBuilder.toString();
    }

}
