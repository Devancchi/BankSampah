package loginregister;

import com.formdev.flatlaf.FlatClientProperties;
import login_register.component.ButtonLink;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import main.DBconnect;

public class SignUp extends JPanel {

    public SignUp() {
        setLayout(new MigLayout("insets n 20 n 20,fillx,wrap,width 380", "[fill]"));

        JTextArea text = new JTextArea("Bersama Bank Sampah Sahabat Ibu \nkelola sampah rumah tangga secara efektif, hemat, dan penuh manfaat.");
        text.setEditable(false);
        text.setFocusable(false);
        text.putClientProperty(FlatClientProperties.STYLE, "" +
                "border:0,0,0,0;" +
                "background:null;");
        add(text);

        add(new JSeparator(), "gapy 15 15");

        JLabel lbEmail = new JLabel("Email");
        lbEmail.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbEmail);

        JTextField txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan Email");
        add(txtEmail);

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbUsername);

        JTextField txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan Username");
        add(txtUsername);

        JLabel lbPassword = new JLabel("Password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbPassword, "gapy 10 n");

        JTextField txtPassword = new JTextField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan Password");
        add(txtPassword);

        JLabel lbNote = new JLabel("");
        lbNote.putClientProperty(FlatClientProperties.STYLE, "font:-1;foreground:$Label.disabledForeground;");
        add(lbNote);

        JButton cmdSignUp = new JButton("Sign up") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        cmdSignUp.putClientProperty(FlatClientProperties.STYLE, "foreground:#FFFFFF;");
        add(cmdSignUp);

        add(new JSeparator(), "gapy 15 15");
        add(new JLabel("Sudah punya akun?"), "split 2, gapx push n");

        ButtonLink cmdBackLogin = new ButtonLink("Login");
        add(cmdBackLogin, "gapx n push");

        // Aksi kembali ke login
        cmdBackLogin.addActionListener(actionEvent -> {
            ModalDialog.popModel(Login.ID);
        });

        // Aksi sign up
        cmdSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtEmail.getText().trim();
                String username = txtUsername.getText().trim();
                String password = txtPassword.getText().trim();

                if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    lbNote.setText("Semua field wajib diisi.");
                    return;
                }

                // Validasi email wajib mengandung '@' dan diakhiri dengan '.com'
                if (!email.matches("^[^@\\s]+@[^@\\s]+\\.com$")) {
                    lbNote.setText("Format email tidak valid. Gunakan format seperti: user@example.com");
                    return;
                }

                try (Connection conn = DBconnect.getConnection()) {
                    String sql = "INSERT INTO login (email, nama, password, level) VALUES (?, ?, ?, ?)";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, email);
                    pst.setString(2, username);
                    pst.setString(3, md5(password));
                    pst.setString(4, "Admin"); // otomatis jadi Admin

                    int inserted = pst.executeUpdate();
                    if (inserted > 0) {
                        JOptionPane.showMessageDialog(null, "Registrasi berhasil! Silakan login.");
                        ModalDialog.popModel(Login.ID);
                    } else {
                        lbNote.setText("Registrasi gagal. Coba lagi.");
                    }

                } catch (Exception ex) {
                    lbNote.setText("Gagal koneksi ke database!");
                    ex.printStackTrace();
                }
            }
        });
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
