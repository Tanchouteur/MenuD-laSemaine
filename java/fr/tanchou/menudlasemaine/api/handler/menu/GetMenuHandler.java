package fr.tanchou.menudlasemaine.api.handler.menu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.menu.Menu;
import fr.tanchou.menudlasemaine.menu.PlatComplet;
import fr.tanchou.menudlasemaine.menu.PlatCompose;
import fr.tanchou.menudlasemaine.menu.Repas;

import java.io.IOException;
import java.io.OutputStream;

public class GetMenuHandler implements HttpHandler {
    private final MenuDAO menuDAO;

    public GetMenuHandler(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            System.out.println("Received GET /menu/getMenu + param = " + exchange.getRequestURI().getQuery());

            String response = getMenuXml();

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
        } else {
            exchange.sendResponseHeaders(405, -1); // Méthode non autorisée si ce n'est pas GET
        }
    }


    public String getMenuXml() {
        StringBuilder xmlBuilder = new StringBuilder();

        // Récupérer le menu
        Menu menu = menuDAO.getMenu();

        // Début du document XML
        xmlBuilder.append("<menuSemaine>\n");

        // Parcourir les repas de la semaine
        for (int jour = 0; jour < menu.listRepas().length; jour++) {
            xmlBuilder.append("  <jour numero=\"").append(jour + 1).append("\">\n");

            for (int repasIndex = 0; repasIndex < menu.listRepas()[jour].length; repasIndex++) {
                Repas repas = menu.listRepas()[jour][repasIndex];

                xmlBuilder.append("    <repas>\n");

                // Ajouter l'entrée
                if (repas.entree() != null) {
                    xmlBuilder.append("      <entree>").append(repas.entree().getNomProduit()).append("</entree>\n");
                }else {
                    xmlBuilder.append("      <entree>").append("Aucune").append("</entree>\n");
                }

                // Ajouter le plat (gestion des types PlatCompose et PlatComplet)
                if (repas.plat() != null) {
                    if (repas.plat() instanceof PlatComplet) {
                        PlatComplet platComplet = (PlatComplet) repas.plat();
                        xmlBuilder.append("      <platComplet>").append(platComplet.getNomPlat()).append("</platComplet>\n");
                    } else if (repas.plat() instanceof PlatCompose) {
                        PlatCompose platCompose = (PlatCompose) repas.plat();
                        xmlBuilder.append("      <platCompose>\n");
                        if (platCompose.getViande() != null) {
                            xmlBuilder.append("        <viande>").append(platCompose.getViande().getNomProduit()).append("</viande>\n");
                        }
                        if (platCompose.getAccompagnement() != null) {
                            xmlBuilder.append("        <accompagnement>").append(platCompose.getAccompagnement().getNomAccompagnement()).append("</accompagnement>\n");
                        }
                        xmlBuilder.append("      </platCompose>\n");
                    }
                }

                xmlBuilder.append("    </repas>\n");
            }

            xmlBuilder.append("  </jour>\n");
        }

        // Fin du document XML
        xmlBuilder.append("</menuSemaine>\n");

        // Affichage pour debug
        //System.out.println("XML généré :\n" + xmlBuilder);

        return xmlBuilder.toString();
    }


}