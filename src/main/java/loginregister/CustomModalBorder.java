package loginregister;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.Modal;

import javax.swing.*;
import java.awt.*;

public class CustomModalBorder extends Modal {

    private final Component component;
    private final String title;
    private final String iconPath;

    public CustomModalBorder(Component component, String title, String iconPath) {
        this.component = component;
        this.title = title;
        this.iconPath = iconPath;
    }

    @Override
    public void installComponent() {
        setLayout(new MigLayout("wrap,fillx", "[fill]"));
        add(createHeader());
        add(component);
    }

    protected Component createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets n 20 n 20,gapx 10"));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null;");
        panel.add(createIcon());
        panel.add(createTitle());
        return panel;
    }

    protected Component createTitle() {
        JLabel label = new JLabel(title);
        label.putClientProperty(FlatClientProperties.STYLE, "font:bold +5;");
        return label;
    }

    protected JLabel createIcon() {
        // pastikan path icon SVG benar, misal icon/sahabat.svg di resources
        FlatSVGIcon svgIcon = new FlatSVGIcon(iconPath, 0.5f)
                .setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("Component.accentColor")));
        
        JLabel label = new JLabel(svgIcon);
        label.putClientProperty(FlatClientProperties.STYLE, "" +
                "border:10,10,10,10,fade($Component.accentColor,50%),,999;" +
                "background:fade($Component.accentColor,10%);");
        return label;
    }
}
