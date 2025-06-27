package main;

import component.LoggerUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnect {

    private static final String URL = "jdbc:sqlite:bank_sampah_sahabat_ibu.db";
    private static DBconnect instance;
    
    public static DBconnect getInstance() {
        if (instance == null) {
            instance = new DBconnect();
        }
        return instance;
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC Driver
            connection = DriverManager.getConnection(URL);
            LoggerUtil.setConnection(connection);  // Set koneksi untuk LoggerUtil
        } catch (ClassNotFoundException e) {
            System.out.println("Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Koneksi ke database gagal: " + e.getMessage());
        }
        return connection;
    }
    
    public Connection getConn() {
        return getConnection();
    }

    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
