package dev.ltocca.loanranger.ORM;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static ConnectionManager instance; // singleton: beware of multiple dao connections
    private Connection connection;
    private String url = "jdbc:postgresql://localhost:5432/loanranger_db";
    private String username = "admin";
    private String password = "password";

    private ConnectionManager() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException ex) {
            System.out.println("Database Connection Creation Failed : " + ex.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static ConnectionManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionManager();
        } else if (instance.getConnection().isClosed()) {
            instance = new ConnectionManager();
        }
        return instance;
    }
}