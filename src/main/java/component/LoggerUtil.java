
package component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.DBconnect;

public class LoggerUtil {

    private static Connection conn;

    /**
     * Sets the database connection for the logger.
     * Note: This is kept for backward compatibility, but the insert method
     * will now get a fresh connection if this one is closed or null.
     * 
     * @param connection The database connection to use for logging
     */
    public static void setConnection(Connection connection) {
        conn = connection;
    }

    /**
     * Insert a log entry into the database
     * 
     * @param idUser    The ID of the user performing the action
     * @param aktivitas Description of the activity being logged
     */
    public static void insert(int idUser, String aktivitas) {
        // Get a connection - either use the existing one or get a new one if necessary
        Connection logConn = null;
        boolean needToCloseConnection = false;

        try {
            // Check if the static connection is valid
            if (conn == null || conn.isClosed()) {
                // Get a fresh connection from our DBconnect class
                logConn = main.DBconnect.getConnection();
                needToCloseConnection = true; // Mark that we need to close this connection when done
            } else {
                logConn = conn; // Use the existing connection
            }

            // Skip logging if we still couldn't get a valid connection
            if (logConn == null) {
                System.err.println("Warning: Could not log activity - no database connection available");
                return;
            }

            // Use parameterized date value for SQLite compatibility instead of NOW()
            String sql = "INSERT INTO log_aktivitas (id_user, aktivitas, tanggal) VALUES (?, ?, ?)";
            try (PreparedStatement st = logConn.prepareStatement(sql)) {
                st.setInt(1, idUser);
                st.setString(2, aktivitas);
                // Use current timestamp in SQLite-compatible format
                st.setString(3, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                st.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.getLogger(LoggerUtil.class.getName()).log(Level.SEVERE,
                    "Error logging activity: " + aktivitas, e);
        } finally {
            // Only close the connection if we created a new one
            if (needToCloseConnection && logConn != null) {
                try {
                    if (!logConn.isClosed()) {
                        logConn.close();
                    }
                } catch (SQLException e) {
                    Logger.getLogger(LoggerUtil.class.getName()).log(Level.WARNING,
                            "Error closing logger connection", e);
                }
            }
        }
    }
}
