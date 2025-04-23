package loginregister;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.*;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.option.BorderOption;
import raven.modal.option.Option;
import com.formdev.flatlaf.extras.FlatSVGIcon;


import javax.swing.*;
import java.awt.*;

public class loginregister extends JFrame {

    public loginregister() {
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1366, 768));
        setBackgroundImage();
        setLocationRelativeTo(null);
        setLayout(new MigLayout("al center center"));
        
        

        // style modal border
        ModalDialog.getDefaultOption()
                .setOpacity(0f)
                .getBorderOption()
                .setShadow(BorderOption.Shadow.MEDIUM);

        JButton button = new JButton("Show");

        button.addActionListener(actionEvent -> {
            showLogin();
        });
        add(button);

        showLogin();
    }
    
     private void setBackgroundImage() {
        FlatSVGIcon backgroundSVG = new FlatSVGIcon("icon/BGlogin.svg");
    JLabel backgroundLabel = new JLabel(backgroundSVG);
    backgroundLabel.setLayout(new BorderLayout());
    setContentPane(backgroundLabel);
    }


    private void showLogin() {
        Option option = ModalDialog.createOption()
                .setCloseOnPressedEscape(false)
                .setBackgroundClickType(Option.BackgroundClickType.BLOCK)
                .setAnimationEnabled(false)
                .setOpacity(0.2f);
        String icon = "icon/account.svg";
        ModalDialog.showModal(this, new CustomModalBorder(new Login(), "Login", icon), option, Login.ID);
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("login.themes");
        FlatMacLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        EventQueue.invokeLater(() -> new loginregister().setVisible(true));
    }
}
