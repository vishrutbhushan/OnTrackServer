package com.project.onTrackServer.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JdbcManager {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static JdbcManager instance;
    private static Connection connection;

    private JdbcManager() {
        try {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new RuntimeException("application.properties not found in classpath");
                }
                props.load(input);
            }
            URL = props.getProperty("spring.datasource.url");
            USER = props.getProperty("spring.datasource.username");
            PASSWORD = props.getProperty("spring.datasource.password");
            if (URL == null || USER == null || PASSWORD == null) {
                throw new RuntimeException("Database properties not set in application.properties");
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize JDBC connection.", e);
        }
    }

    public static synchronized JdbcManager getInstance() {
        if (instance == null) {
            instance = new JdbcManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
