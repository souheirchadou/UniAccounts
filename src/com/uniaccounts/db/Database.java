package com.uniaccounts.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database access (SQLite through JDBC). One connection shared by the program;
 * the file uniaccounts.db is created in the folder where the program runs.
 */
public final class Database {

    private static final String URL = "jdbc:sqlite:uniaccounts.db";
    private static Connection connection;

    private Database() { }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                createTables(connection);
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error: " + e.getMessage()
                    + " (is lib\\sqlite-jdbc.jar on the classpath?)", e);
        }
    }

    private static void createTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "id TEXT PRIMARY KEY, "
                    + "full_name TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "phone TEXT, "
                    + "level TEXT NOT NULL, "
                    + "major TEXT, "
                    + "scholarship INTEGER NOT NULL DEFAULT 0, "
                    + "courses TEXT NOT NULL DEFAULT '')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS audit_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ts TEXT NOT NULL, "
                    + "action TEXT NOT NULL, "
                    + "details TEXT)");
        }
    }
}
