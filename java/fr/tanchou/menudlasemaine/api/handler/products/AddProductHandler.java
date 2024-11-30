package fr.tanchou.menudlasemaine.api.handler.products;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;
import fr.tanchou.menudlasemaine.utils.Factory;

import java.io.*;

public class AddProductHandler implements HttpHandler {
    private final Factory factory;

    public AddProductHandler(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("AddProduct : " + exchange.getRequestURI());
        String requestURI = exchange.getRequestURI().toString();

        // Extraire le type de produit de l'URL (par exemple : /products/add/entree)
        String productType = requestURI.split("/")[2];
        System.out.println("productType : " + productType);

        TypeProduit type = TypeProduit.valueOf(productType.toUpperCase());

        // Lire le corps de la requête POST
        InputStream inputStream = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBody = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        reader.close();

        // Utiliser Gson pour parser le JSON en un objet Produits
        Gson gson = new Gson();
        Produits newProduits = gson.fromJson(requestBody.toString(), Produits.class);

        // Si besoin, vous pouvez configurer les poids de la requête de cette manière
        // newProduits.setPoidsMoment(new int[]{...});
        // newProduits.setPoidsSaison(new int[]{...});

        // Ajouter le produit (exemple de fonction de traitement)
        String response = addProduct(newProduits);

        System.out.println("response : " + response);

        // Définir le type de contenu comme JSON
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] responseBytes = response.getBytes();

        // Envoyer les en-têtes de réponse avec le code HTTP 200
        exchange.sendResponseHeaders(200, responseBytes.length);

        // Envoyer la réponse JSON dans le corps de la réponse
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private String addProduct(Produits produits) {
        // Ajouter le produit à la base de données
        ProduitDAO.addProduit(produits);

        // Retourner le produit ajouté
        return new Gson().toJson(produits);
    }
}
