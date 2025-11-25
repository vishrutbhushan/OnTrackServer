package com.project.onTrackServer.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcManager {
    private static String URL = "jdbc:mysql://localhost:3306/ontrack_schema";
    private static String USER = "dbuser";
    private static String PASSWORD = "gaT3@ssPa";

    private static JdbcManager instance;
    private static Connection connection;

    private JdbcManager() {

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static JdbcManager getInstance() {
        if (instance == null) {
            instance = new JdbcManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
