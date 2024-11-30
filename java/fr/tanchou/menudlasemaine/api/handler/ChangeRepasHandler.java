package fr.tanchou.menudlasemaine.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.menu.Repas;
import fr.tanchou.menudlasemaine.utils.Factory;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ChangeRepasHandler implements HttpHandler {
    private final Factory factory;

    public ChangeRepasHandler(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            //System.out.println("Received " + exchange.getRequestURI() + " + param = " + exchange.getRequestURI().getQuery());
            // /menu/repas/change?jours=*&moment=*

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

            // Définition du type de contenu (utilisez "application/xml" si la réponse est en XML)
            exchange.getResponseHeaders().set("Content-Type", "application/xml");

            // Calcul de la longueur de la réponse pour la réponse HTTP
            byte[] responseBytes = response.getBytes();
            exchange.sendResponseHeaders(200, responseBytes.length);

            // Envoi de la réponse
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            //System.out.println("Response : " + response);

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
        MomentSemaine momentSemaine = getMomentSemaine(jour);

        Month moisCourant = LocalDate.now().getMonth();
        int saisonInt = Saison.getSaisonIndexByMois(moisCourant.getValue());

        Repas repas = factory.buildRepas(factory.getMomentInt(moment, momentSemaine), saisonInt);

        MenuDAO.updateRepas(repas, jour, moment);

        StringBuilder xmlBuilder = new StringBuilder();

        xmlBuilder.append("<jour nom=\"").append(jour).append("\">");

        // Ajouter les éléments moment, entree, et plat
        xmlBuilder.append("<").append(moment).append(">")
                .append("<entree>").append(repas.getEntree() != null ? repas.getEntree() : "").append("</entree>")
                .append("<plat>").append(repas.getPlat().toString()).append("</plat>")
                .append("</").append(moment).append(">");


        xmlBuilder.append("</jour>");


        return xmlBuilder.toString();
    }

    private MomentSemaine getMomentSemaine(String jours) {
        return switch (jours.toLowerCase()) {
            case "lundi", "mardi", "mercredi", "jeudi", "vendredi" -> MomentSemaine.SEMAINE;
            case "samedi", "dimanche" -> MomentSemaine.WEEKEND;
            default -> throw new IllegalArgumentException("Jour invalide: " + jours);
        };
    }
}
