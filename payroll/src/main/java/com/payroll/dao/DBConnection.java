package com.payroll.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Reads db.properties once and hands out JDBC connections. */
public final class DBConnection {
    private static final Properties PROPS = loadProperties();
    private static String lastError = "";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }

    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(3);
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public static String getLastError() {
        return lastError;
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found in src/main/resources");
            }
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
        return p;
    }
}
