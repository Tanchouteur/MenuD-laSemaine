package fr.tanchou.menudlasemaine.utils.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Utility class to manage database connections using HikariCP.
 * This class provides a static method to access a pre-configured
 * {@link DataSource} for the "menu_de_la_semaine" database.
 *
 * <p>
 * The database is accessed via a MariaDB JDBC URL, and the connection pool
 * is managed by HikariCP, which ensures high performance and efficient use
 * of resources.
 * </p>
 *
 * <p><b>Important:</b> This class is initialized statically. The connection
 * details (such as JDBC URL, username, and password) are hard-coded and
 * should be secured for production environments.</p>
 */
public class DatabaseConnectionFakeGit {

    /**
     * The HikariCP connection pool configured for the "menu_de_la_semaine" database.
     */
    private static final HikariDataSource dataSource;

    // Static block to initialize the HikariDataSource
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://129.151.251.156:3306/menu_de_la_semaine");
        config.setUsername("user");
        config.setPassword("000");
        config.setMaximumPoolSize(6);
        dataSource = new HikariDataSource(config);
    }

    /**
     * Returns the configured {@link DataSource} for accessing the database.
     *
     * @return The {@link DataSource} instance.
     *
     * <p>This method provides a single access point to the connection pool
     * managed by HikariCP. It ensures thread-safe, high-performance database
     * connections.</p>
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
}