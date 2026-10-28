package com.devflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hands out a NEW database connection every time getConnection() is called.
 * The caller closes it (try-with-resources does this automatically).
 *
 * Only the settings (url, user, password) are static and loaded once. Connections are
 * never shared between requests because a shared connection is not thread-safe.
 *
 * To move to a connection pool later (e.g. HikariCP), only the body of getConnection()
 * changes to "return dataSource.getConnection();". Nothing else in the project changes.
 */
public final class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found. Copy db.properties.example to db.properties.");
            }
            props.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not initialise database settings", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
