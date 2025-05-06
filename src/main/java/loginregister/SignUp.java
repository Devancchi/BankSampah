package loginregister;

import com.formdev.flatlaf.FlatClientProperties;
import login_register.component.ButtonLink;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;

import javax.swing.*;

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

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold;");
        add(lbUsername);

        JTextField txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukan Username");
        add(txtEmail);

        JLabel lbPassword = new JLabel("Create a password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold;");
        add(lbPassword, "gapy 10 n");

        JTextField txtPassword = new JTextField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukan Password");
        add(txtPassword);

        JLabel lbNote = new JLabel("");
        lbNote.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:-1;" +
                "foreground:$Label.disabledForeground;");
        add(lbNote);

        JButton cmdSignUp = new JButton("Sign up") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        cmdSignUp.putClientProperty(FlatClientProperties.STYLE, "" +
                "foreground:#FFFFFF;");
        add(cmdSignUp);

        add(new JSeparator(), "gapy 15 15");

        add(new JLabel("Sudah Punya Akun ?"), "split 2, gapx push n");

        ButtonLink cmdBackLogin = new ButtonLink("Login");
        add(cmdBackLogin, "gapx n push");

        // event
        cmdBackLogin.addActionListener(actionEvent -> {
            ModalDialog.popModel(Login.ID);
        });
    }
}
