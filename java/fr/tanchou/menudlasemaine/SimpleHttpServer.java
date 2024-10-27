package fr.tanchou.menudlasemaine;

import com.sun.net.httpserver.HttpServer; // Vérifie que c'est bien cette importation
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class SimpleHttpServer { // Renommé ici

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
        server.createContext("/getMenu", new MenuHandler());
        server.start();
        System.out.println("Serveur démarré sur le port 8090");
        MenuService.createAndInsertMenu();
    }

    static class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {

                String response = getMenu();

                exchange.getResponseHeaders().set("Content-Type", "application/xml"); // Définit le type de contenu
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
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
}
