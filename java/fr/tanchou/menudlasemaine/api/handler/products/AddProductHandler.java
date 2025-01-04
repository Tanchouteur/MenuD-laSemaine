package fr.tanchou.menudlasemaine.api.handler.products;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.api.gson.ProduitsAdapter;
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
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Produits.class, new ProduitsAdapter())
                .create();

        Produits newProduits = gson.fromJson(requestBody.toString(), Produits.class);
        String response;
        if (newProduits == null) {
            System.out.println("newProduits is null");
            return;
        }
        if (newProduits.getType() == null || newProduits.getNomProduit() == null) {
            response = "Le type et le nom du produit sont obligatoires";
        }else {
            response = addProduct(newProduits);
        }

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

    private String addProduct(Produits produit) {
        // Ajouter le produit à la base de données
        factory.getWeightManager().getProduitDAO().addProduit(produit);

        StringBuilder jsonBuilder = new StringBuilder();
        // Ajouter les propriétés du produit dans l'objet JSON
        jsonBuilder.append("{")
                .append("\"nom\": \"").append(produit.getNomProduit()).append("\", ")
                .append("\"poids_arbitraire\": \"").append(produit.getPoidsArbitraire()).append("\", ")

                .append("\"poids_midiSemaine\": \"").append(produit.getPoidsMoment()[0]).append("\", ")
                .append("\"poids_soirSemaine\": \"").append(produit.getPoidsMoment()[1]).append("\", ")
                .append("\"poids_midiWeekend\": \"").append(produit.getPoidsMoment()[2]).append("\", ")
                .append("\"poids_soirWeekend\": \"").append(produit.getPoidsMoment()[3]).append("\", ")

                .append("\"poids_printemps\": \"").append(produit.getPoidsSaison()[0]).append("\", ")
                .append("\"poids_ete\": \"").append(produit.getPoidsSaison()[1]).append("\", ")
                .append("\"poids_automne\": \"").append(produit.getPoidsSaison()[2]).append("\", ")
                .append("\"poids_hiver\": \"").append(produit.getPoidsSaison()[3]).append("\", ")

                .append("\"last_use\": \"").append(produit.getLastUsed()).append("\"")
                .append("}");

        return jsonBuilder.toString();

    }
}
