/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

/**
 *
 * @author devan
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.DBconnect;

public class LoggerUtil {

    private static Connection conn;

    // Setter untuk mengatur koneksi
    public static void setConnection(Connection connection) {
        conn = connection;
    }

    // Method untuk memasukkan log aktivitas
    public static void insert(String admin, String aktivitas) {
        if (conn == null) {
            return;
        }

        try {
            String sql = "INSERT INTO log_aktivitas (admin, aktivitas) VALUES (?, ?)";
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, admin);
                st.setString(2, aktivitas);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.getLogger(LoggerUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
