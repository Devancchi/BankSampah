package loginregister;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.option.BorderOption;
import raven.modal.option.Option;

import javax.swing.*;
import java.awt.*;

public class loginregister extends JFrame {

    private JPanel contentPanel;

    public loginregister() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
        setLayout(new MigLayout("al center center"));

        setContentPane(new GradientPanel());

        // Setting untuk modal dialog
        ModalDialog.getDefaultOption()
                .setOpacity(0f)
                .getBorderOption()
                .setShadow(BorderOption.Shadow.MEDIUM);

        showLogin();  // tampilkan login saat awal
    }

    private void showLogin() {
        Option option = ModalDialog.createOption()
                .setCloseOnPressedEscape(false)
                .setBackgroundClickType(Option.BackgroundClickType.BLOCK)
                .setAnimationEnabled(false)
                .setOpacity(0.2f);

        // Menggunakan path yang benar untuk icon sahabat.svg
        // Coba beberapa kemungkinan path
        String icon = "icon/account.svg";
        
        // Gunakan CustomModalBorder dengan path icon
        ModalDialog.showModal(this, new CustomModalBorder(new Login(), "Login", icon), option, Login.ID);
    }
   
    public static void main(String[] args) {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatMacLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        EventQueue.invokeLater(() -> new loginregister().setVisible(true));
    }
}