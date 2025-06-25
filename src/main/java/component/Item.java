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
        setPreferredSize(new java.awt.Dimension(140, 210));

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
        DecimalFormat df = new DecimalFormat("'Rp '###,###");
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

        lbItemName.setFont(lbItemName.getFont().deriveFont(lbItemName.getFont().getStyle() | java.awt.Font.BOLD, lbItemName.getFont().getSize()+6));
        lbItemName.setText("Item Name");

        lbKode.setFont(lbKode.getFont().deriveFont(lbKode.getFont().getStyle() | java.awt.Font.BOLD, lbKode.getFont().getSize()+2));
        lbKode.setForeground(new java.awt.Color(178, 178, 178));
        lbKode.setText("AB010123");

        pic.setImage(new javax.swing.ImageIcon(getClass().getResource("/icon/img1.png"))); // NOI18N

        lbPrice.setFont(lbPrice.getFont().deriveFont(lbPrice.getFont().getStyle() | java.awt.Font.BOLD, lbPrice.getFont().getSize()+6));
        lbPrice.setText("Rp. 000.000.00");

        lbStok.setFont(lbStok.getFont().deriveFont(lbStok.getFont().getStyle() | java.awt.Font.BOLD));
        lbStok.setForeground(new java.awt.Color(76, 76, 76));
        lbStok.setText("000");

        lbStok1.setFont(lbStok1.getFont().deriveFont(lbStok1.getFont().getSize()-2f));
        lbStok1.setForeground(new java.awt.Color(76, 76, 76));
        lbStok1.setText("Stok :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbPrice)
                    .addComponent(pic, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lbItemName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbKode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lbStok1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lbStok)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lbItemName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbKode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pic, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbStok1)
                    .addComponent(lbStok))
                .addGap(11, 11, 11)
                .addComponent(lbPrice)
                .addGap(10, 10, 10))
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
