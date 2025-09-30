package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.ORM.ConnectionManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class AdminDatabaseController {
    private final Admin admin;

    public AdminDatabaseController(Admin admin) {
        this.admin = admin;
    }

    public void recreateSchemaAndAdmin() {
        System.out.println("Executing database reset...");
        try {
            executeSqlScript("sql/reset.sql");
            System.out.println("Database schema has been recreated successfully with a default admin.");
        } catch (Exception e) {
            System.err.println("Failed to reset the database: " + e.getMessage());
        }
    }

    public void generateDefaultDatabase() {
        System.out.println("Generating default database...");
        try {
            executeSqlScript("sql/default.sql");
            System.out.println("Default database generated successfully.");
        } catch (Exception e) {
            System.err.println("Failed to generate the default database: " + e.getMessage());
        }
    }

    private void executeSqlScript(String sqlPath) throws Exception {
        String scriptContent;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(sqlPath)) {
            if (is == null) {
                throw new RuntimeException("Cannot find script file: " + sqlPath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                scriptContent = reader.lines().collect(Collectors.joining("\n"));
            }
        }

        try (Connection conn = ConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(scriptContent);
        }
    }
}