package com.dbsl.shop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class OracleConnectionManager {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:FREE";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "6184";

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException exception) {
            System.err.println("Oracle JDBC driver not found. Add ojdbc jar to the classpath.");
        }
    }

    private OracleConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
