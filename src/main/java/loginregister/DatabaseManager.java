package loginregister;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_FOLDER = "db";
    private static final String DB_NAME = DB_FOLDER + File.separator + "bank_sampah_sahabat_ibu.db";
    private static final String TEMPLATE_DB = "/bank_sampah_sahabat_ibu.db";
    private static DatabaseManager instance;
    private String databasePath;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            File dbDir = new File(DB_FOLDER);
            if (!dbDir.exists()) {
                dbDir.mkdirs(); // Buat folder /db jika belum ada
            }

            File userDB = new File(DB_NAME);

            // Jika database user belum ada, extract template dari resource
            if (!userDB.exists()) {
                System.out.println("Initializing database from template...");
                extractTemplateDatabase();
            }

            this.databasePath = "jdbc:sqlite:" + DB_NAME;
            System.out.println("Database initialized at: " + userDB.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            this.databasePath = "jdbc:sqlite:" + DB_NAME;
        }
    }

    private void extractTemplateDatabase() throws IOException {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_DB)) {
            if (templateStream == null) {
                throw new IOException("Template database not found in resources at: " + TEMPLATE_DB);
            }
            try (FileOutputStream fos = new FileOutputStream(DB_NAME)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = templateStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                System.out.println("Database template extracted successfully");
            }
        }
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databasePath);
    }

    // Method untuk test koneksi database
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // Method untuk reset database ke template (optional)
    public void resetToTemplate() throws IOException {
        File userDB = new File(DB_NAME);
        if (userDB.exists()) {
            userDB.delete();
        }
        extractTemplateDatabase();
        System.out.println("Database reset to template");
    }

    // Method untuk backup database
    public void backupDatabase(String backupPath) throws IOException {
        File sourceDB = new File(DB_NAME);
        File backupDB = new File(backupPath);

        if (sourceDB.exists()) {
            Files.copy(sourceDB.toPath(), backupDB.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database backed up to: " + backupPath);
        } else {
            throw new IOException("Source database not found");
        }
    }
}