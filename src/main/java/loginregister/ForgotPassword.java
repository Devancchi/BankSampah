package loginregister;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import login_register.component.ButtonLink;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import main.DBconnect;

import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ForgotPassword extends JPanel {

    public ForgotPassword() {
    setLayout(new MigLayout("insets 20,fillx,wrap 1", "[fill]"));
    add(new JLabel("Masukkan Username dan Password Baru"));
    
    JTextField txtUsername = new JTextField();
    txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contoh: krisna123");
    add(txtUsername);
    
    JPasswordField txtNewPassword = new JPasswordField();
    txtNewPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password Baru");
    add(txtNewPassword);
    
    JPasswordField txtConfirmPassword = new JPasswordField();
    txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Konfirmasi Password Baru");
    add(txtConfirmPassword);
    
    JLabel lbNote = new JLabel("");
    lbNote.putClientProperty(FlatClientProperties.STYLE, "font:-1; foreground:#d9534f;");
    add(lbNote);
    
    JButton cmdSubmit = new JButton("Ganti Password");
    add(cmdSubmit, "gapy 10");
    
    ButtonLink cmdBackLogin = new ButtonLink("Kembali ke Login");
    add(cmdBackLogin, "al center");
    
    cmdSubmit.addActionListener(e -> {
        String username = txtUsername.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        
        // Reset warna label ke merah default
        lbNote.setForeground(new Color(217, 83, 79)); // Warna merah default
        
        if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            lbNote.setText("Semua field harus diisi.");
            return;
        }
        
        // Validasi konfirmasi password
        if (!newPassword.equals(confirmPassword)) {
            lbNote.setText("Password dan konfirmasi password tidak cocok.");
            return;
        }
        
        try (Connection conn = DBconnect.getConnection()) {
            String sql = "SELECT * FROM login WHERE nama_user = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, username);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    String level = rs.getString("level");
                    // Hanya owner yang tidak bisa mengganti password
                    if ("owner".equalsIgnoreCase(level)) {
                        lbNote.setText("Password untuk owner tidak dapat diubah.");
                        return;
                    }
                    
                    String hashedPassword = hashPassword(newPassword);
                    String updateSql = "UPDATE login SET password = ? WHERE nama_user = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, hashedPassword);
                        updateStmt.setString(2, username);
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            lbNote.setForeground(new Color(0, 128, 0)); // Hijau untuk sukses
                            lbNote.setText("Password berhasil diubah.");
                            
                            // Clear semua field setelah berhasil
                            txtUsername.setText("");
                            txtNewPassword.setText("");
                            txtConfirmPassword.setText("");
                            
                            // Optional: Kembali ke login setelah beberapa detik
                            Timer timer = new Timer(2000, evt -> ModalDialog.popModel(Login.ID));
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            lbNote.setText("Terjadi kesalahan saat mengubah password.");
                        }
                    }
                } else {
                    lbNote.setText("Username tidak ditemukan.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lbNote.setText("Terjadi kesalahan koneksi.");
        }
    });
    
    cmdBackLogin.addActionListener(e -> ModalDialog.popModel(Login.ID)); // Kembali ke login
}

// Fungsi untuk melakukan hashing MD5 pada password
private String hashPassword(String password) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(password.getBytes());
    byte[] hashedBytes = md.digest();
    
    // Mengonversi byte array ke hexadecimal string
    StringBuilder sb = new StringBuilder();
    for (byte b : hashedBytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
}
