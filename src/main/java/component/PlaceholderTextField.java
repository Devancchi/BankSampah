/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

/**
 *
 * @author devan
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Icon;
import javax.swing.JTextField;

public class PlaceholderTextField extends JTextField implements FocusListener {

    private String placeholder = "";
    private boolean showingPlaceholder = true;
    private Icon icon;

    public PlaceholderTextField() {
        super();
        addFocusListener(this);
        setFont(new Font("Poppins", Font.PLAIN, 13));
    }

    public void setPlaceholder(String text) {
        this.placeholder = text;
        repaint();
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        repaint();
    }

    public Icon getIcon() {
        return this.icon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (icon != null) {
            int iconY = (this.getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g, 5, iconY);
        }

        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            Font prev = g.getFont();
            g.setFont(getFont().deriveFont(Font.ITALIC));
            int x = icon != null ? icon.getIconWidth() + 10 : 5;
            g.drawString(placeholder, x, getHeight() / 2 + getFont().getSize() / 2 - 2);
            g.setFont(prev);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        showingPlaceholder = false;
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        showingPlaceholder = true;
        repaint();
    }
}
