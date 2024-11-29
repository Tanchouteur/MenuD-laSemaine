package fr.tanchou.menudlasemaine;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.security.KeyStore;
import java.security.SecureRandom;

import fr.tanchou.menudlasemaine.web.*;
import fr.tanchou.menudlasemaine.web.products.GetProductsHandler;

public class SimpleHttpsServer {

    public static void main(String[] args) throws Exception {
        // Chemin vers le fichier keystore contenant le certificat SSL
        String keystorePath = "/home/ubuntu/MenuSemaine/certificates/keystore.p12"; // Remplace par le chemin absolu ou relatif de ton keystore
        String keystorePassword = "2112"; // Remplace par le mot de passe de ton keystore

        // Charger le keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

        // Initialiser le KeyManagerFactory pour gérer le certificat
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        // Créer le contexte SSL
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

        // Créer un serveur HTTPS
        HttpsServer server = HttpsServer.create(new InetSocketAddress("0.0.0.0", 8090), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLEngine engine = sslContext.createSSLEngine();
                engine.setUseClientMode(false);
                params.setSSLParameters(engine.getSSLParameters());
            }
        });

        // Définir les contextes pour les différentes routes
        server.createContext("/menu/getMenu", new GetMenuHandler()).getFilters().add(new CorsFilter());
        server.createContext("/menu/changeMenu", new ChangeMenuHandler()).getFilters().add(new CorsFilter());
        server.createContext("/menu/repas/change", new ChangeRepasHandler()).getFilters().add(new CorsFilter());
        server.createContext("/products", new GetProductsHandler()).getFilters().add(new CorsFilter());
        server.createContext("/", new NotFoundHandler());

        // Démarrer le serveur
        server.start();
        System.out.println("Serveur démarré en HTTPS sur le port 8090");
    }


}
