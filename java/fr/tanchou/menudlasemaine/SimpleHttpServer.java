package fr.tanchou.menudlasemaine;

import com.sun.net.httpserver.HttpServer;

import fr.tanchou.menudlasemaine.web.ChangeMenuHandler;
import fr.tanchou.menudlasemaine.web.GetMenuHandler;
import fr.tanchou.menudlasemaine.web.NotFoundHandler;

import java.io.IOException;

import java.net.InetSocketAddress;

public class SimpleHttpServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress("192.168.1.47", 8090), 0);

        // Définir le contexte pour obtenir le menu
        server.createContext("/menu/getMenu", new GetMenuHandler());

        // Définir le contexte pour changer le menu
        server.createContext("/menu/changeMenu", new ChangeMenuHandler());

        //server.createContext("/menu", new IndexHtmlHandler());

        server.createContext("/", new NotFoundHandler());

        server.start();
        System.out.println("Serveur démarré sur le port 8090");
    }
}
