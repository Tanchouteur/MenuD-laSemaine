package fr.tanchou.menudlasemaine.api.handler.products;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.utils.Factory;

import java.io.*;

public class DeleteProductHandler  implements HttpHandler {
    private final Factory factory;

    public DeleteProductHandler(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("DelProduct : " + exchange.getRequestURI());
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

        String response;

        response = deleteProduct(requestBody.toString());


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

    private String deleteProduct(String produit) {
        if (factory.getWeightManager().getProduitDAO().deleteProduit(produit)){
            return "Erreur lors de la suppression du produit";
        }
        return "Produit supprimé";
    }
}
