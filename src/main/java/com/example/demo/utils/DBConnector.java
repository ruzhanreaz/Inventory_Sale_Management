package com.example.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static volatile DBConnector instance;
    private static final String URL = "jdbc:sqlite:pos.db";

    private DBConnector() {
    }

    public static DBConnector getInstance() {
        DBConnector result = instance;
        if (result == null) {
            synchronized (DBConnector.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DBConnector();
                }
            }
        }
        return result;
    }

    public static Connection getConnection() {
        System.out.println("[DBConnector] Connecting to DB at: " + new java.io.File("pos.db").getAbsolutePath());
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database.");
        }
    }
}
