package fr.tanchou.menudlasemaine.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;

import java.io.IOException;
import java.io.OutputStream;

public class ChangeMenuHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Received " + exchange.getRequestURI() + " + param = " + exchange.getRequestURI().getQuery());

            // Logique pour changer le menu
            String response = changeMenu();

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    // Méthode pour changer le menu
    public String changeMenu() {

        Menu newMenu = MenuService.buildMenu();
        MenuDAO.updateMenu(newMenu.getRepasParJour());

        return "Menu a été changé avec succès";
    }
}