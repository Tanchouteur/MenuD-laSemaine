package fr.tanchou.menudlasemaine.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class NotFoundHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "404 - Page non trouvée !";

        // Définir le code de réponse 404
        exchange.sendResponseHeaders(404, response.length());

        // Ecrire le corps de la réponse
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
