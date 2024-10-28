package fr.tanchou.menudlasemaine;

import com.sun.net.httpserver.HttpServer; // Vérifie que c'est bien cette importation
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;

import java.io.File;
import java.io.FileInputStream;
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
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8090), 0);


        // Définir le contexte pour obtenir le menu
        server.createContext("/menu/getMenu", new GetMenuHandler());

        // Définir le contexte pour changer le menu
        server.createContext("/menu/changeMenu", new ChangeMenuHandler());

        server.createContext("/menu", new IndexHtmlHandler());

        server.start();
        System.out.println("Serveur démarré sur le port 8090");
    }

    static class GetMenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                System.out.println("Received GET /menu/getMenu + param = " + exchange.getRequestURI().getQuery());
                String response = getMenu();

                //System.out.println("Response: " + response);

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

    static class ChangeMenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Received " + exchange.getRequestURI() + " + param = " + exchange.getRequestURI().getQuery());
                // Logique pour changer le menu - Ici, tu pourrais appeler une méthode pour traiter le menu entrant
                String response = changeMenu();

                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Méthode non autorisée si ce n'est pas POST
            }
        }

        // Méthode pour changer le menu
        public String changeMenu() {

            Menu newMenu = MenuService.buildMenu();
            MenuDAO.updateMenu(newMenu.getRepasParJour());

            return "Menu a été changé avec succès";
        }
    }

    static class IndexHtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
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
}
