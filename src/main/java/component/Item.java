package component;

import component.ModelItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;

public class Item extends javax.swing.JPanel {

    private Color fillOriginal = new Color(245, 245, 245); // abu-abu terang
    private Color fillHover = new Color(220, 220, 220); // abu-abu hover
    private Color fillSelected = new Color(255, 193, 7); // gold
    private Color currentFill = fillOriginal;

    public ModelItem getData() {
        return data;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    private boolean selected;

    public Item() {
        initComponents();
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!selected) {
                    currentFill = fillHover;
                    repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!selected) {
                    currentFill = fillOriginal;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setSelected(true); // biar panel kelihatan dipilih
                currentFill = fillSelected;
                repaint();

                // Kirim sinyal ke listener (untuk deselect item lain)
                firePropertyChange("itemSelected", false, true);
            }
        });
    }

    private ModelItem data;

    public void setData(ModelItem data) {
        this.data = data;
        pic.setImage(data.getGambar());
        lbItemName.setText(data.getNama());
        lbKode.setText(data.getKode());
        lbStok.setText(String.valueOf(data.getStok()));
        DecimalFormat df = new DecimalFormat("Rp #,##0.00");
        lbPrice.setText(df.format(data.getHarga()));
    }

    @Override
    public void paint(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(currentFill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        if (selected) {
            g2.setColor(fillSelected);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }
        g2.dispose();
        super.paint(grphcs);
    }

    public void deselect() {
        selected = false;
        currentFill = fillOriginal;
        repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbItemName = new javax.swing.JLabel();
        lbKode = new javax.swing.JLabel();
        pic = new component.PictureBox();
        lbPrice = new javax.swing.JLabel();
        lbStok = new javax.swing.JLabel();
        lbStok1 = new javax.swing.JLabel();

        lbItemName.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lbItemName.setText("Item Name");

        lbKode.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lbKode.setForeground(new java.awt.Color(178, 178, 178));
        lbKode.setText("AB010123");

        pic.setImage(new javax.swing.ImageIcon(getClass().getResource("/icon/img1.png"))); // NOI18N

        lbPrice.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lbPrice.setText("Rp. 000.000.00");

        lbStok.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lbStok.setForeground(new java.awt.Color(76, 76, 76));
        lbStok.setText("000");

        lbStok1.setFont(new java.awt.Font("sansserif", 0, 10)); // NOI18N
        lbStok1.setForeground(new java.awt.Color(76, 76, 76));
        lbStok1.setText("Stok :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pic, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbPrice))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lbItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lbKode, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(10, 10, 10))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbStok1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbStok)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lbItemName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbKode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pic, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbStok1)
                    .addComponent(lbStok))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbPrice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lbItemName;
    private javax.swing.JLabel lbKode;
    private javax.swing.JLabel lbPrice;
    private javax.swing.JLabel lbStok;
    private javax.swing.JLabel lbStok1;
    private component.PictureBox pic;
    // End of variables declaration//GEN-END:variables
}
