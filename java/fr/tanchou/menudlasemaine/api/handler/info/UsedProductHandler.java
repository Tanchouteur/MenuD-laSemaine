package fr.tanchou.menudlasemaine.api.handler.info;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.MenuDAO;

import java.io.IOException;
import java.io.OutputStream;

public class UsedProductHandler implements HttpHandler {

    private final MenuDAO menuDAO;

    public UsedProductHandler(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            System.out.println("Received GET /info/getProductUsed + param = " + exchange.getRequestURI().getQuery());

            String response = getProductUsedXML();

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/XML"); // Définit le type de contenu XML
            byte[] responseBytes = response.getBytes(); // Convertit la réponse en octets
            exchange.sendResponseHeaders(200, responseBytes.length); // Envoie les en-têtes avec le code et la taille de la réponse

            try (OutputStream os = exchange.getResponseBody()) { // Utilise try-with-resources pour gérer le flux
                //System.out.println("Sending response");

                // Envoi de la réponse en petits blocs
                int blockSize = 1024; // Taille du bloc à envoyer
                for (int i = 0; i < responseBytes.length; i += blockSize) {
                    int length = Math.min(blockSize, responseBytes.length - i);
                    os.write(responseBytes, i, length);
                }

                //System.out.println("Response sent");
            }

            exchange.sendResponseHeaders(405, -1); // Méthode non autorisée si ce n'est pas GET

    }

    private String getProductUsedXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<products>");
        menuDAO.getMenu().getProductsUsed().forEach(product -> {
            xml.append("<product>");
            xml.append("<name>").append(product.getNomProduit()).append("</name>");
            xml.append("</product>");
        });
        xml.append("</products>");
        return xml.toString();
    }
}
