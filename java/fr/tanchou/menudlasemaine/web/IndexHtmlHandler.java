package fr.tanchou.menudlasemaine.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IndexHtmlHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {

            System.out.println("Received GET /menu + param = " + exchange.getRequestURI().getQuery());

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            File file = new File("webapp/index.html");

            if (file.exists()) {
                // Lit le fichier HTML
                FileInputStream fis = new FileInputStream(file);
                byte[] fileBytes = fis.readAllBytes();
                fis.close();

                // Répond avec le fichier HTML
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, fileBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileBytes);
                os.close();
            } else {
                // Envoie une erreur 404 si le fichier n'existe pas
                String response = "404 (File Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
        }
    }
}