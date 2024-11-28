package fr.tanchou.menudlasemaine.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;
import fr.tanchou.menudlasemaine.utils.generateur.RepasBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ChangeRepasHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Received " + exchange.getRequestURI() + " + param = " + exchange.getRequestURI().getQuery());
            // // /menu/repas/change?jours=*&moment=*

            // Décomposition de la requete
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

            String jour = params.get("jour");
            String moment = params.get("moment");

            if (jour == null){
                System.out.println("Jour non renseigné");
            }else if (moment == null){
                System.out.println("Moment non renseigné");
            }

            // Logique pour changer un repas
            String response = changeRepas(jour, MomentJournee.valueOf(moment.toUpperCase()));

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Autorise toutes les origines
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS"); // Autorise les méthodes GET et OPTIONS
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type"); // Autorise les en-têtes nécessaires

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
            // Réponse pour les requêtes OPTIONS (pré-vol)
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1); // Pas de contenu pour les OPTIONS
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    // Méthode pour changer le menu
    public String changeRepas(String jour, MomentJournee moment) {

        Repas repas = RepasBuilder.buildRepa(moment, getMomentSemaine(jour), Saison.getSaisonByMois(LocalDate.EPOCH.getMonthValue()), new LastUseWeightManager());
        MenuDAO.updateRepas(repas, jour, moment);

        return "succes";
    }

    private MomentSemaine getMomentSemaine(String jours) {
        return switch (jours.toLowerCase()) {
            case "lundi", "mardi", "mercredi", "jeudi", "vendredi" -> MomentSemaine.SEMAINE;
            case "samedi", "dimanche" -> MomentSemaine.WEEKEND;
            default -> throw new IllegalArgumentException("Jour invalide: " + jours);
        };
    }
}
