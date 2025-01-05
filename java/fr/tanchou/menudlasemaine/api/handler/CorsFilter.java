package fr.tanchou.menudlasemaine.api.handler;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsFilter extends Filter {

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Ajouter les en-têtes CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "https://menu.tanchou.fr");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Si la méthode est OPTIONS, retourne une réponse vide avec le code 204
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Continuer la chaîne de filtres
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Filtre pour gérer les en-têtes CORS.";
    }
}