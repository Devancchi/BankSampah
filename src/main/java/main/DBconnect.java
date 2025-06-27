package main;

import component.LoggerUtil;
import component.LoggerUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnect {

    // Ambil path database dari DatabaseManager agar selalu sinkron
    private static DBconnect instance;

    // SQLite JDBC connection properties
    private static final String PRAGMA_BUSY_TIMEOUT = "busy_timeout";
    private static final int BUSY_TIMEOUT_MS = 30000; // 30 seconds

    public static DBconnect getInstance() {
        if (instance == null) {
            instance = new DBconnect();
        }
        return instance;
    }

    /**
     * Get a database connection with improved settings for SQLite to handle busy
     * database issues
     * 
     * @return Connection - a fresh connection to the database
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC Driver

            // Ambil URL dari DatabaseManager
            String url = loginregister.DatabaseManager.getInstance().getDatabasePath();

            // Set SQLite connection properties
            java.util.Properties props = new java.util.Properties();
            props.setProperty(PRAGMA_BUSY_TIMEOUT, String.valueOf(BUSY_TIMEOUT_MS));

            // Get connection with improved properties
            connection = DriverManager.getConnection(url, props);

            // Configure connection for better concurrency handling
            if (connection != null) {
                try (java.sql.Statement stmt = connection.createStatement()) {
                    // SQLite pragma settings for better concurrency
                    stmt.execute("PRAGMA journal_mode=WAL"); // Write-Ahead Logging mode
                    stmt.execute("PRAGMA synchronous=NORMAL"); // Balance between durability and performance
                    stmt.execute("PRAGMA busy_timeout=" + BUSY_TIMEOUT_MS); // Wait when database is locked
                }

                // Set default connection timeout
                connection.setNetworkTimeout(null, 5000); // 5 seconds timeout

                // Configure LoggerUtil
                LoggerUtil.setConnection(connection);
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Koneksi ke database gagal: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Get a connection - exists for compatibility with existing code
     * 
     * @return Connection - a fresh connection to the database
     */
    public Connection getConn() {
        return getConnection();
    }

    /**
     * Safely close a database connection
     * 
     * @param conn - the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            closeConnection(conn);
        }
    }
}
