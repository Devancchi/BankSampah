
package loginregister;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;

public class GradientPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Titik pusat gradasi
        Point2D center = new Point2D.Float(w * 0.4f, h * 0.6f);
        float radius = Math.max(w, h);

        // Warna-warna gradasi mirip gambar
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(36, 82, 72),     // #F9FD50 kuning terang
            new Color(168, 222, 211),    // putih kekuningan
            new Color(31, 179, 148)     // hijau pastel terang
        };

        RadialGradientPaint paint = new RadialGradientPaint(center, radius, dist, colors, CycleMethod.NO_CYCLE);
        g2.setPaint(paint);
        g2.fillRect(0, 0, w, h);
    }
}

