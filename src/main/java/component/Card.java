/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 *
 * @author devan
 */
public class Card extends javax.swing.JPanel {

    private Color fillColor;
    private Color borderColor;
    private int borderRadius;
    private int strokeWidth;

    public Card() {
        fillColor = new Color(255, 254, 84);       // Default warna kuning
        borderColor = new Color(230, 230, 230);    // Optional: border tipis
        borderRadius = 20;
        strokeWidth = 0; // 0 = no stroke
        setOpaque(false);
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderRadius(int borderRadius) {
        this.borderRadius = borderRadius;
        repaint();
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        repaint();
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = strokeWidth;
        int y = strokeWidth;
        int width = getWidth() - strokeWidth * 2;
        int height = getHeight() - strokeWidth * 2;

        // Fill background
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, borderRadius, borderRadius);

        // Optional: border
        if (strokeWidth > 0) {
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width, height, borderRadius, borderRadius);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
