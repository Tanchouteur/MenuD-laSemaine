package fr.tanchou.menudlasemaine.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://129.151.251.156:3306/menudelasemaine";
    private static final String USER = "louis";
    private static final String PASSWORD = "2112";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
