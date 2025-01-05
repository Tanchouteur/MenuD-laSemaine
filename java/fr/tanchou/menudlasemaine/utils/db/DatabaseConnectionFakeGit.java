package fr.tanchou.menudlasemaine.utils.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConnectionFakeGit {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://129.151.251.156:3306/menu_de_la_semaine");
        config.setUsername("user");
        config.setPassword("000");
        config.setMaximumPoolSize(6);
        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}