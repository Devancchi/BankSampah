package chart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class PolarAreaLabel extends JLabel {

    private String valueText;

    public PolarAreaLabel(String name, double value) {
        setText(name + ": " + (int) value); // Gabungkan nama dan angka dalam satu string
        setBorder(new EmptyBorder(3, 25, 3, 3));
        setFont(getFont().deriveFont(0, 13));
        setForeground(new Color(100, 100, 100));
        valueText = name + ": " + (int) value; // Simpan teks dengan angka
    }

    // Adjust the circle size and text positioning
    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        int size = 12; // Smaller circle (was probably using getHeight() - 10)
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = (getHeight() - size) / 2;
        g2.setColor(getBackground());
        g2.fillOval(5, y, size, size); // Position circle at fixed x=5
        g2.dispose();
    }
}
