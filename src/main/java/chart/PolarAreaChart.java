package chart;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class PolarAreaChart extends javax.swing.JComponent {

    private final List<ModelPolarAreaChart> list = new ArrayList<>();
    private double maxValues;
    private double totalValues;
    private final int PADDING_BOTTON = 50;
    private final Animator animator;
    private float animate;

    // Add these member variables to keep track of item counts
    private int panel1Count = 0;
    private int panel2Count = 0;
    private int panel3Count = 0;

    public PolarAreaChart() {
        initComponents();

        // Set up chart panel in circle panel
        circle.setLayout(new java.awt.BorderLayout());
        ChartPanel chartPanel = new ChartPanel();
        chartPanel.setOpaque(false);
        circle.add(chartPanel);

        setBackground(Color.WHITE);
        setForeground(Color.WHITE);

        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                animate = fraction;
                circle.repaint(); // Repaint circle panel instead
            }
        };
        animator = new Animator(200, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
    }

    @Override
    public void paint(Graphics grphcs) {
        if (isOpaque()) {
            grphcs.setColor(getBackground());
            grphcs.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paint(grphcs);
    }

    // Create a new inner class for the chart panel
    private class ChartPanel extends javax.swing.JPanel {
        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);

            // Get dimensions
            int width = getWidth();
            int height = getHeight();
            int space = 5;
            int size = Math.min(width, height) - space;

            if (width <= 0)
                width = 1;
            if (height <= 0)
                height = 1;

            // Create buffered image for smoother rendering
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw chart
            if (!list.isEmpty()) {
                DecimalFormat df = new DecimalFormat("#,##0.##");
                double startAngle = 90;
                for (ModelPolarAreaChart data : list) {
                    g2.setColor(data.getColor());
                    double angle = valuesToAngle(data.getValues());
                    double rs = valuesToRealSize(data.getValues(), size) * animate;
                    Shape s = createChartShape(startAngle, angle, rs, width, height);
                    g2.fill(s);
                    g2.setComposite(AlphaComposite.Clear);
                    g2.setStroke(new BasicStroke(3f));
                    g2.draw(s);
                    g2.setComposite(AlphaComposite.SrcOver);
                    startAngle += angle;
                    drawChartValues(g2, df.format(data.getValues()), startAngle - angle / 2, rs / 4, width, height);
                }
            } else {
                g2.setColor(new Color(200, 200, 200));
                int x = (width - size) / 2;
                int y = (height - size) / 2;
                g2.drawOval(x, y, size, size);
            }

            g2.dispose();
            grphcs.drawImage(img, 0, 0, null);
        }

        // Helper methods for chart drawing
        private Shape createChartShape(double start, double end, double values, int width, int height) {
            double x = (width - values) / 2;
            double y = (height - values) / 2;
            return new Arc2D.Double(x, y, values, values, start, end, Arc2D.PIE);
        }

        private void drawChartValues(Graphics2D g2, String values, double angle, double rs, int width, int height) {
            int centerx = width / 2;
            int centerY = height / 2;
            Point p = getChartLocation(angle, rs);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics ft = g2.getFontMetrics();
            Rectangle2D r2 = ft.getStringBounds(values, g2);
            double x = (centerx + p.x) - (r2.getWidth() / 2);
            double y = (centerY - p.y) + (ft.getAscent() / 2);
            g2.drawString(values, (int) x, (int) y);
        }

        private Point getChartLocation(double angle, double rs) {
            double x = Math.cos(Math.toRadians(angle)) * rs;
            double y = Math.sin(Math.toRadians(angle)) * rs;
            return new Point((int) x, (int) y);
        }
    }

    private void createChart(Graphics grphcs) {
        int width = getWidth();
        int height = getHeight() - PADDING_BOTTON;
        int space = 5;
        int size = Math.min(width, height) - space;
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!list.isEmpty()) {
            DecimalFormat df = new DecimalFormat("#,##0.##");
            double startAngle = 90;
            for (ModelPolarAreaChart data : list) {
                g2.setColor(data.getColor());
                double angle = valuesToAngle(data.getValues());
                double rs = valuesToRealSize(data.getValues(), size) * animate;
                Shape s = createShape(startAngle, angle, rs);
                g2.fill(s);
                g2.setComposite(AlphaComposite.Clear);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(s);
                g2.setComposite(AlphaComposite.SrcOver);
                startAngle += angle;
                drawValues(g2, df.format(data.getValues()), startAngle - angle / 2, rs / 4);
            }

        } else {
            g2.setColor(new Color(200, 200, 200));
            g2.drawOval(x, y, size, size);
        }
        g2.dispose();
        grphcs.drawImage(img, 0, 0, null);
    }

    private void drawValues(Graphics2D g2, String values, double angle, double rs) {
        int centerx = getWidth() / 2;
        int centerY = (getHeight() - PADDING_BOTTON) / 2;
        Point p = getLocation(angle, rs);
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics ft = g2.getFontMetrics();
        Rectangle2D r2 = ft.getStringBounds(values, g2);
        double x = (centerx + p.x) - (r2.getWidth() / 2);
        double y = (centerY - p.y) + (ft.getAscent() / 2);
        g2.drawString(values, (int) x, (int) y);
    }

    private Shape createShape(double start, double end, double values) {
        int width = getWidth();
        int height = getHeight() - PADDING_BOTTON;
        double x = (width - values) / 2;
        double y = (height - values) / 2;
        Shape shape = new Arc2D.Double(x, y, values, values, start, end, Arc2D.PIE);
        return shape;
    }

    private double valuesToRealSize(double values, int size) {
        double n = values * 100 / maxValues;
        return n * size / 100;
    }

    private double valuesToAngle(double values) {
        double n = values * 100 / totalValues;
        return n * 360 / 100;
    }

    private Point getLocation(double angle, double rs) {
        double x = Math.cos(Math.toRadians(angle)) * rs;
        double y = Math.sin(Math.toRadians(angle)) * rs;
        return new Point((int) x, (int) y);
    }

    private void calculateValues(ModelPolarAreaChart data) {
        maxValues = Math.max(maxValues, data.getValues());
        totalValues = 0;
        for (ModelPolarAreaChart l : list) {
            totalValues += l.getValues();
        }
    }

    public void addItem(ModelPolarAreaChart data) {
        list.add(data);
        calculateValues(data);
        repaint();

        PolarAreaLabel label = new PolarAreaLabel(data.getName(), data.getValues());
        label.setBackground(data.getColor());

        // Distribute items across the three panels, 3 items per panel maximum
        if (panel1Count < 3) {
            panel1.add(label);
            panel1Count++;
        } else if (panel2Count < 3) {
            panel2.add(label);
            panel2Count++;
        } else if (panel3Count < 3) {
            panel3.add(label);
            panel3Count++;
        } else {
            // If all panels are full, you can either:
            // Option 1: Start over with panel1 (wrap around)
            panel1.add(label);
            panel1Count++;
            // Option 2: Show a warning or handle differently
            // System.out.println("Warning: More than 9 items in chart");
        }

        // Make sure to repaint and revalidate all panels
        panel1.repaint();
        panel1.revalidate();
        panel2.repaint();
        panel2.revalidate();
        panel3.repaint();
        panel3.revalidate();
    }

    public void start() {
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    // Update the clear method to reset item counts
    public void clear() {
        animate = 0;
        list.clear();

        // Reset item counts
        panel1Count = 0;
        panel2Count = 0;
        panel3Count = 0;

        // Clear all panels
        panel1.removeAll();
        panel2.removeAll();
        panel3.removeAll();

        // Repaint and revalidate
        panel1.repaint();
        panel1.revalidate();
        panel2.repaint();
        panel2.revalidate();
        panel3.repaint();
        panel3.revalidate();

        repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new javax.swing.JPanel();
        panel2 = new javax.swing.JPanel();
        panel3 = new javax.swing.JPanel();
        circle = new javax.swing.JPanel();

        panel1.setOpaque(false);
        panel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        panel2.setOpaque(false);
        panel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        panel3.setOpaque(false);
        panel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        circle.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout circleLayout = new javax.swing.GroupLayout(circle);
        circle.setLayout(circleLayout);
        circleLayout.setHorizontalGroup(
            circleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        circleLayout.setVerticalGroup(
            circleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 156, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(circle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .addComponent(panel3, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(circle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel circle;
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel panel2;
    private javax.swing.JPanel panel3;
    // End of variables declaration//GEN-END:variables
}
