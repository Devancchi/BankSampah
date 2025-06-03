
package component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {

    private static Connection conn;

    public static void setConnection(Connection connection) {
        conn = connection;
    }

    public static void insert(int idUser, String aktivitas) {
        if (conn == null) {
            return;
        }

        try {
            String sql = "INSERT INTO log_aktivitas (id_user, aktivitas, tanggal) VALUES (?, ?, NOW())";
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, idUser);
                st.setString(2, aktivitas);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.getLogger(LoggerUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
