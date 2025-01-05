package fr.tanchou.menudlasemaine.api;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import fr.tanchou.menudlasemaine.api.handler.*;
import fr.tanchou.menudlasemaine.api.handler.menu.ChangeMenuHandler;
import fr.tanchou.menudlasemaine.api.handler.menu.ChangeRepasHandler;
import fr.tanchou.menudlasemaine.api.handler.menu.GetMenuHandler;
import fr.tanchou.menudlasemaine.api.handler.products.AddProductHandler;
import fr.tanchou.menudlasemaine.api.handler.products.DeleteProductHandler;
import fr.tanchou.menudlasemaine.api.handler.products.GetProductsHandler;
import fr.tanchou.menudlasemaine.utils.Factory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * Main class that starts the HTTPS server for handling API requests related to menu and product management.
 * It initializes the server with SSL context for secure communication and sets up various API endpoints.
 */
public class ApiMain {

    /**
     * The entry point of the application that sets up and starts the HTTPS server.
     *
     * @param args Command-line arguments (not used).
     * @throws Exception If there is an error while initializing the SSL context or starting the server.
     */
    public static void main(String[] args) throws Exception {
        // Factory instance for dependency injection
        Factory factory = new Factory();

        // Scanner for reading user input
        Scanner scanner = new Scanner(System.in);

        // Path to the keystore containing the SSL certificate
        String keystorePath = "/home/ubuntu/MenuSemaine/certificates/keystore.p12";

        // Prompt user for the keystore password
        System.out.print("Keystore password: \n >");
        String keystorePassword = scanner.nextLine();

        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

        // Initialize the KeyManagerFactory to handle the certificate
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        // Create SSLContext using the initialized KeyManagerFactory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

        // Create the HTTPS server
        HttpsServer server = HttpsServer.create(new InetSocketAddress("0.0.0.0", 8090), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLEngine engine = sslContext.createSSLEngine();
                engine.setUseClientMode(false);
                params.setSSLParameters(engine.getSSLParameters());
            }
        });

        // Set up contexts for various routes
        server.createContext("/menu/getMenu", new GetMenuHandler(factory.getWeightManager().getMenuDAO()))
                .getFilters().add(new CorsFilter());
        server.createContext("/menu/changeMenu", new ChangeMenuHandler(factory))
                .getFilters().add(new CorsFilter());
        server.createContext("/menu/repas/change", new ChangeRepasHandler(factory))
                .getFilters().add(new CorsFilter());

        server.createContext("/products/get", new GetProductsHandler(factory))
                .getFilters().add(new CorsFilter());
        server.createContext("/products/add", new AddProductHandler(factory))
                .getFilters().add(new CorsFilter());
        server.createContext("/products/delete", new DeleteProductHandler(factory))
                .getFilters().add(new CorsFilter());
        /*server.createContext("/products/update", new UpdateProductHandler(factory))
              .getFilters().add(new CorsFilter());*/

        // Handle undefined routes
        server.createContext("/", new NotFoundHandler());

        // Start the server
        server.start();
        System.out.println("Server started in HTTPS on port 8090");
    }
}