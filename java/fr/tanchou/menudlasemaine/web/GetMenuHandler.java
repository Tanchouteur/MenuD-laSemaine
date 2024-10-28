package fr.tanchou.menudlasemaine.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetMenuHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            System.out.println("Received GET /menu/getMenu + param = " + exchange.getRequestURI().getQuery());

            String response = getMenu();

            exchange.getResponseHeaders().set("Content-Type", "application/XML"); // Définit le type de contenu XML
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Méthode non autorisée si ce n'est pas GET
        }
    }

    public String getMenu() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<menuSemaine>");

        String selectSQL = "SELECT jour, moment, entree, plat FROM Menu ORDER BY FIELD(jour, 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi', 'dimanche'), FIELD(moment, 'midi', 'soir')";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            String currentJour = "";
            while (rs.next()) {
                String jour = rs.getString("jour");
                String moment = rs.getString("moment");
                String entree = rs.getString("entree");
                String plat = rs.getString("plat");

                // Ajouter un nouvel élément jour si on passe à un jour différent
                if (!jour.equals(currentJour)) {
                    if (!currentJour.isEmpty()) {
                        xmlBuilder.append("</jour>"); // Fermer l'élément précédent
                    }
                    xmlBuilder.append("<jour nom=\"").append(jour).append("\">");
                    currentJour = jour;
                }

                // Ajouter les éléments moment, entree, et plat
                xmlBuilder.append("<").append(moment).append(">")
                        .append("<entree>").append(entree != null && !entree.equals("Aucune") ? entree : "").append("</entree>")
                        .append("<plat>").append(plat).append("</plat>")
                        .append("</").append(moment).append(">");
            }

            // Fermer le dernier élément jour
            if (!currentJour.isEmpty()) {
                xmlBuilder.append("</jour>");
            }

            xmlBuilder.append("</menuSemaine>");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return xmlBuilder.toString();
    }
}