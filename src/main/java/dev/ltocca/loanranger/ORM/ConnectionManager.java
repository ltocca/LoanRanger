package dev.ltocca.loanranger.ORM;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String url = "jdbc:postgresql://localhost:5432/loanranger_db";
    private static final String username = "admin";
    private static final String password = "password";

    private ConnectionManager() {

    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException ex) {
            System.out.println("Database Connection Creation Failed : " + ex.getMessage());
            throw new SQLException("PostgreSQL JDBC Driver not found.", ex);
        }
    }
}