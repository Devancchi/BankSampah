package loginregister;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import login_register.component.ButtonLink;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import main.DBconnect;
import main.Dashboard;

public class Login extends JPanel {

    public static final String ID = "login_id";
    
    public Login() {
        setLayout(new MigLayout("insets n 20 n 20,fillx,wrap,width 380", "[fill]"));
        
        JTextArea text = new JTextArea("Kelola sampah rumah tangga jadi lebih mudah.\n" +
            "Masuk dan lanjutkan langkah bijak untuk lingkungan bersama Bank Sampah Sahabat Ibu.");
        text.setEditable(false);
        text.setFocusable(false);
        text.putClientProperty(FlatClientProperties.STYLE, "" +
                "border:0,0,0,0;" +
                "background:null;");
        add(text);

        add(new JSeparator(), "gapy 15 15");

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold;");
        add(lbUsername);

        JTextField txtUser = new JTextField();
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukan Username Anda");
        add(txtUser);

        JLabel lbPassword = new JLabel("Password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold;");
        add(lbPassword, "gapy 10 n");

        JPasswordField txtPassword = new JPasswordField();
        installRevealButton(txtPassword);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukan Password Anda");
        add(txtPassword);

        ButtonLink cmdForgotPassword = new ButtonLink("Forgot password ?");
        add(cmdForgotPassword, "gapx push n");

        JLabel lbNote = new JLabel("");
        lbNote.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:-1;" +
                "foreground:$Label.disabledForeground;");
        add(lbNote);

        JButton cmdLogin = new JButton("Login") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "foreground:#FFFFFF;");
        add(cmdLogin);

        add(new JSeparator(), "gapy 15 15");

        add(new JLabel("Belum Punya Akun? "), "split 2,gapx push n");
        ButtonLink cmdSignUp = new ButtonLink("Sign up");
        add(cmdSignUp, "gapx n push");

        // Event Handlers
        cmdSignUp.addActionListener(actionEvent -> {
            String icon = "icon/signup.svg";
            ModalDialog.pushModal(new CustomModalBorder(new SignUp(), "Sign up", icon), ID);
        });

        cmdForgotPassword.addActionListener(actionEvent -> {
            String icon = "icon/forgot_password.svg";
            ModalDialog.pushModal(new CustomModalBorder(new ForgotPassword(), "Forgot password", icon), ID);
        });

        cmdLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                lbNote.setText("Username atau password tidak boleh kosong.");
                return;
            }

            try (Connection con = DBconnect.getConnection()) {
                // Query untuk memeriksa username yang ada di database
                String sql = "SELECT * FROM login WHERE nama = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, username);
                ResultSet rs = pst.executeQuery();

                // Jika username ditemukan
                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password");

                    // Bandingkan password yang di-hash
                    if (storedPasswordHash.equals(md5(password))) {
                        // Login berhasil
                        SwingUtilities.getWindowAncestor(this).dispose();  // Tutup form login

                        // Buka form Dashboard atau halaman berikutnya
                        new Dashboard().setVisible(true);
                    } else {
                        lbNote.setText("Username atau password salah.");
                    }
                } else {
                    lbNote.setText("Username atau password salah.");
                }
            } catch (Exception ex) {
                lbNote.setText("Terjadi kesalahan koneksi.");
                ex.printStackTrace();
            }
        });
    }

    // Fungsi untuk meng-hash password menggunakan MD5
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

    // Fungsi untuk menambahkan tombol reveal (tampil/sembunyikan password)
    private void installRevealButton(JPasswordField txt) {
        FlatSVGIcon iconEye = new FlatSVGIcon("icon/eye.svg", 0.3f);
        FlatSVGIcon iconHide = new FlatSVGIcon("icon/hide.svg", 0.3f);

        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:0,0,0,5;");
        JButton button = new JButton(iconEye);

        button.addActionListener(new ActionListener() {
            private char defaultEchoChart = txt.getEchoChar();
            private boolean show;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                show = !show;
                if (show) {
                    button.setIcon(iconHide);
                    txt.setEchoChar((char) 0);
                } else {
                    button.setIcon(iconEye);
                    txt.setEchoChar(defaultEchoChart);
                }
            }
        });
        toolBar.add(button);
        txt.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
    }
}
