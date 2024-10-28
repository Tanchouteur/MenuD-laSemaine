package fr.tanchou.menudlasemaine;

import com.sun.net.httpserver.HttpServer; // Vérifie que c'est bien cette importation
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.models.Repas;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;
import fr.tanchou.menudlasemaine.web.ChangeMenuHandler;
import fr.tanchou.menudlasemaine.web.GetMenuHandler;
import fr.tanchou.menudlasemaine.web.IndexHtmlHandler;
import fr.tanchou.menudlasemaine.web.NotFoundHandler;

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

        HttpServer server = HttpServer.create(new InetSocketAddress("192.168.1.59", 8090), 0);


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
