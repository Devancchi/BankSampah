package loginregister;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import main.AppInitializer;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.option.BorderOption;
import raven.modal.option.Option;

import javax.swing.*;
import java.awt.*;

public class loginregister extends JFrame {

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

        showLogin(); // tampilkan login saat awal
    }

    private void showLogin() {
        Option option = ModalDialog.createOption()
                .setCloseOnPressedEscape(false)
                .setBackgroundClickType(Option.BackgroundClickType.BLOCK)
                .setAnimationEnabled(false)
                .setOpacity(0.2f);

        // Gunakan path yang benar untuk icon admin/staff
        String icon = "icon/account.svg";

        // Gunakan CustomModalBorder dengan path icon, title "Admin Login"
        ModalDialog.showModal(this, new CustomModalBorder(new Login(), "Admin Login", icon), option, Login.ID);
    }

    public static void main(String[] args) {
        // Load Poppins font
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    loginregister.class.getResourceAsStream("/fonts/Poppins-Regular.ttf")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Poppins font for all Swing components
        Font poppinsFont = new Font("Poppins", Font.PLAIN, 13);
        UIManager.put("Label.font", poppinsFont);
        UIManager.put("Button.font", poppinsFont);
        UIManager.put("ToggleButton.font", poppinsFont);
        UIManager.put("RadioButton.font", poppinsFont);
        UIManager.put("CheckBox.font", poppinsFont);
        UIManager.put("ColorChooser.font", poppinsFont);
        UIManager.put("ComboBox.font", poppinsFont);
        UIManager.put("List.font", poppinsFont);
        UIManager.put("MenuBar.font", poppinsFont);
        UIManager.put("MenuItem.font", poppinsFont);
        UIManager.put("RadioButtonMenuItem.font", poppinsFont);
        UIManager.put("CheckBoxMenuItem.font", poppinsFont);
        UIManager.put("Menu.font", poppinsFont);
        UIManager.put("PopupMenu.font", poppinsFont);
        UIManager.put("OptionPane.font", poppinsFont);
        UIManager.put("Panel.font", poppinsFont);
        UIManager.put("ProgressBar.font", poppinsFont);
        UIManager.put("ScrollPane.font", poppinsFont);
        UIManager.put("Viewport.font", poppinsFont);
        UIManager.put("TabbedPane.font", poppinsFont);
        UIManager.put("Table.font", poppinsFont);
        UIManager.put("TableHeader.font", poppinsFont);
        UIManager.put("TextField.font", poppinsFont);
        UIManager.put("PasswordField.font", poppinsFont);
        UIManager.put("TextArea.font", poppinsFont);
        UIManager.put("TextPane.font", poppinsFont);
        UIManager.put("EditorPane.font", poppinsFont);
        UIManager.put("TitledBorder.font", poppinsFont);
        UIManager.put("ToolBar.font", poppinsFont);
        UIManager.put("ToolTip.font", poppinsFont);
        UIManager.put("Tree.font", poppinsFont);
        UIManager.put("defaultFont", poppinsFont);

        AppInitializer.setupLookAndFeel();
        java.awt.EventQueue.invokeLater(() -> {
            new loginregister().setVisible(true);
        });
    }
}
