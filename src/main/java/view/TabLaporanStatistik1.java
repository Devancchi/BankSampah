package view;

import java.awt.Color;
import java.awt.Cursor;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;
import main.DBconnect;

public class TabLaporanStatistik1 extends javax.swing.JPanel {
    
    public TabLaporanStatistik1() {
         initComponents();
         loadDashboardData();
        loadData("");
    }
    
       private void loadData(String filterJenis) {
     DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Membuat semua sel tidak bisa diedit
        }
    };
    model.setColumnIdentifiers(new String[]{
        "ID", "Nama Admin", "Nama", "Nama Barang", "Kategori Sampah",
        "Total Berat Sampah", "Harga", "Jenis Transaksi", "Riwayat"
    });

    String baseQuery =
        "SELECT id, nama_admin, nama_nasabah, nama_barang, kategori, total_berat, harga, jenis, riwayat FROM (" +
        "   SELECT " +
        "       lp.id_laporan_pemasukan AS id, " +
        "       u.nama_user AS nama_admin, " +
        "       COALESCE(n.nama_nasabah, '-') AS nama_nasabah, " +
        "       db.nama_barang, " +
        "       '-' AS kategori, " +
        "       '-' AS total_berat, " +
        "       db.harga, " +
        "       'Pemasukan' AS jenis, " +
        "       lp.riwayat AS riwayat " +
        "   FROM laporan_pemasukan lp " +
        "   JOIN login u ON lp.id_user = u.id_user " +
        "   LEFT JOIN data_barang db ON lp.id_barang = db.id_barang " +
        "   LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah " +

        "   UNION ALL " +

        "   SELECT " +
        "       lpl.id_laporan_pengeluaran AS id, " +
        "       u.nama_user AS nama_admin, " +
        "       n.nama_nasabah AS nama_nasabah, " +
        "       '-' AS nama_barang, " +
        "       s.kategori_sampah, " +
        "       s.berat_sampah, " +
        "       s.harga, " +
        "       'Pengeluaran' AS jenis, " +
        "       lpl.riwayat AS riwayat " +
        "   FROM laporan_pengeluaran lpl " +
        "   JOIN login u ON lpl.id_user = u.id_user " +
        "   JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran " +
        "   JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah" +
        ") AS combined ";

    // Tambah WHERE jika ada filter
    if (filterJenis != null && !filterJenis.isEmpty()) {
        baseQuery += "WHERE jenis = ? ";
    }

    baseQuery += "ORDER BY riwayat DESC";

    try (Connection conn = DBconnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(baseQuery)) {

        if (filterJenis != null && !filterJenis.isEmpty()) {
            pst.setString(1, filterJenis);
        }

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String harga = rs.getString("harga");
                if (!harga.equals("-")) {
                    try {
                        double nominal = Double.parseDouble(harga);
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        harga = formatRupiah.format(nominal);
                    } catch (NumberFormatException e) {
                        // Do nothing
                    }
                }

                model.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("nama_admin"),
                    rs.getString("nama_nasabah"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getString("total_berat"),
                    harga,
                    rs.getString("jenis"),
                    rs.getString("riwayat")
                });
            }
        }

        tb_laporan.setModel(model);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
    }
}

    private void loadDashboardData() {
    try {
        Connection conn = DBconnect.getConnection();
        Statement stmt = conn.createStatement();

        // Ambil total pemasukan
        double totalpemasukan = 0;
        ResultSet rspemasukan = stmt.executeQuery("SELECT SUM(harga) FROM data_barang");
        if (rspemasukan.next()) {
            totalpemasukan = rspemasukan.getDouble(1);
            String formatted = String.format("Rp %,.2f", totalpemasukan)
                                  .replace(',', 'X')   // sementara ubah koma jadi X
                                  .replace('.', ',')   // titik jadi koma
                                  .replace('X', '.');  // X (yang tadi koma) jadi titik
            lb_pemasukan.setText(formatted);
        }

        // Ambil total pengeluaran
        double totalpengeluaran = 0;
        ResultSet rspengeluaran = stmt.executeQuery("SELECT SUM(harga) FROM setor_sampah");
        if (rspengeluaran.next()) {
            totalpengeluaran = rspengeluaran.getDouble(1);
            String formatted = String.format("Rp %,.2f", totalpengeluaran)
                                  .replace(',', 'X')
                                  .replace('.', ',')
                                  .replace('X', '.');
            lb_pengeluaran.setText(formatted);
        }

        // Hitung total akhir (pemasukan - pengeluaran)
        double totalakhir = totalpemasukan - totalpengeluaran;
        String formattedTotal = String.format("Rp %,.2f", totalakhir)
                                 .replace(',', 'X')
                                 .replace('.', ',')
                                 .replace('X', '.');
        lb_total.setText(formattedTotal);

        conn.close();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data dashboard: " + e.getMessage());
    }
}

            private Timer animationTimer;
          private Color startColor;
          private Color endColor;
          private int animationStep = 0;
          private final int totalSteps = 10;

          private void animateClick(Color from, Color to) {
              if (animationTimer != null && animationTimer.isRunning()) {
                  animationTimer.stop();
              }
              startColor = from;
              endColor = to;
              animationStep = 0;

              animationTimer = new Timer(15, e -> {
                  float ratio = (float) animationStep / totalSteps;
                  int red = (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()));
                  int green = (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()));
                  int blue = (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()));
                  Color currentColor = new Color(red, green, blue);
                  card1.setFillColor(currentColor);
                  card1.repaint();

                  animationStep++;
                  if (animationStep > totalSteps) {
                      ((Timer) e.getSource()).stop();
                  }
              });
              animationTimer.start();
          }  
          
          private Timer animationTimer2;
        private Color startColor2;
        private Color endColor2;
        private int animationStep2 = 0;
        private final int totalSteps2 = 10;

        private void animateClickCard2(Color from, Color to) {
            if (animationTimer2 != null && animationTimer2.isRunning()) {
                animationTimer2.stop();
            }
            startColor2 = from;
            endColor2 = to;
            animationStep2 = 0;

            animationTimer2 = new Timer(15, e -> {
                float ratio = (float) animationStep2 / totalSteps2;
                int red = (int) (startColor2.getRed() + ratio * (endColor2.getRed() - startColor2.getRed()));
                int green = (int) (startColor2.getGreen() + ratio * (endColor2.getGreen() - startColor2.getGreen()));
                int blue = (int) (startColor2.getBlue() + ratio * (endColor2.getBlue() - startColor2.getBlue()));
                Color currentColor = new Color(red, green, blue);
                card2.setFillColor(currentColor);
                card2.repaint();

        animationStep2++;
        if (animationStep2 > totalSteps2) {
            ((Timer) e.getSource()).stop();
        }
    });
    animationTimer2.start();
}
    
        private Timer animationTimer3;
        private Color startColor3;
        private Color endColor3;
        private int animationStep3 = 0;
        private final int totalSteps3 = 10;

        private void animateClickCard3(Color from, Color to) {
            if (animationTimer3 != null && animationTimer3.isRunning()) {
                animationTimer3.stop();
            }
            startColor3 = from;
            endColor3 = to;
            animationStep3 = 0;

            animationTimer3 = new Timer(15, e -> {
                float ratio = (float) animationStep3 / totalSteps3;
                int red = (int) (startColor3.getRed() + ratio * (endColor3.getRed() - startColor3.getRed()));
                int green = (int) (startColor3.getGreen() + ratio * (endColor3.getGreen() - startColor3.getGreen()));
                int blue = (int) (startColor3.getBlue() + ratio * (endColor3.getBlue() - startColor3.getBlue()));
                Color currentColor = new Color(red, green, blue);
                card3.setFillColor(currentColor);
                card3.repaint();

                animationStep3++;
                if (animationStep3 > totalSteps3) {
                    ((Timer) e.getSource()).stop();
                }
            });
            animationTimer3.start();
        }


            private void showPanel() {
                panelMain.removeAll();
                panelMain.add(new TabLaporanStatistik1());
                panelMain.repaint();
                panelMain.revalidate();
            }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateChooser1 = new datechooser.Main.DateChooser();
        dateBetween1 = new datechooser.Main.DateBetween();
        defaultDateChooserRender1 = new datechooser.render.DefaultDateChooserRender();
        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        ShadowUtama = new component.ShadowPanel();
        shadowPanel1 = new component.ShadowPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        card1 = new component.Card();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lb_pemasukan = new javax.swing.JLabel();
        card2 = new component.Card();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lb_pengeluaran = new javax.swing.JLabel();
        card3 = new component.Card();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        lb_total = new javax.swing.JLabel();
        card4 = new component.Card();
        ShadowSearch = new component.ShadowPanel();
        jLabel7 = new javax.swing.JLabel();
        txt_search = new swing.TextField();
        ShadowSearch1 = new component.ShadowPanel();
        t_date = new javax.swing.JTextField();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        dateChooser1.setDateChooserRender(defaultDateChooserRender1);
        dateChooser1.setDateSelectable(null);
        dateChooser1.setDateSelectionMode(datechooser.Main.DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateChooser1.setTextField(t_date);

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(255, 255, 255));
        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(248, 248, 248));

        jButton2.setText("First Page");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText("<");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton4.setText(">");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton3.setText("Last Page");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowPanel1Layout = new javax.swing.GroupLayout(shadowPanel1);
        shadowPanel1.setLayout(shadowPanel1Layout);
        shadowPanel1Layout.setHorizontalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        shadowPanel1Layout.setVerticalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama Admin", "Nama Nasabah", "Nama Barang", "Harga", "Riwayat"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tb_laporan);

        card1.setFillColor(new java.awt.Color(255, 255, 255));
        card1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                card1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card1MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                card1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                card1MouseReleased(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_totalPemasukan.png"))); // NOI18N

        jLabel10.setBackground(new java.awt.Color(0, 0, 0));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Total Pemasukan");

        lb_pemasukan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_pemasukan.setText("1.000");

        javax.swing.GroupLayout card1Layout = new javax.swing.GroupLayout(card1);
        card1.setLayout(card1Layout);
        card1Layout.setHorizontalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(lb_pemasukan))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        card1Layout.setVerticalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(card1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lb_pemasukan))
                    .addComponent(jLabel5))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        card2.setFillColor(new java.awt.Color(239, 100, 100));
        card2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                card2MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card2MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                card2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                card2MouseReleased(evt);
            }
        });

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_total_sampah.png"))); // NOI18N

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("Total Pengeluaran");

        lb_pengeluaran.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_pengeluaran.setText("1.000");

        javax.swing.GroupLayout card2Layout = new javax.swing.GroupLayout(card2);
        card2.setLayout(card2Layout);
        card2Layout.setHorizontalGroup(
            card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(lb_pengeluaran))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        card2Layout.setVerticalGroup(
            card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(card2Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lb_pengeluaran))
                    .addComponent(jLabel15))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        card3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                card3MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card3MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card3MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                card3MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                card3MouseReleased(evt);
            }
        });

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_total_tabungan.png"))); // NOI18N

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("Jumlah Transaksi");

        lb_total.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_total.setText("Rp 1.000");

        javax.swing.GroupLayout card3Layout = new javax.swing.GroupLayout(card3);
        card3.setLayout(card3Layout);
        card3Layout.setHorizontalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(lb_total))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        card3Layout.setVerticalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(card3Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lb_total))
                    .addComponent(jLabel18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        card4.setFillColor(new java.awt.Color(255, 255, 255));
        card4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                card4MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card4MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card4MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                card4MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                card4MouseReleased(evt);
            }
        });

        ShadowSearch.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch.setPreferredSize(new java.awt.Dimension(259, 43));

        jLabel7.setBackground(new java.awt.Color(204, 204, 204));
        jLabel7.setForeground(new java.awt.Color(204, 204, 204));
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_search.png"))); // NOI18N

        txt_search.setBorder(null);
        txt_search.setForeground(new java.awt.Color(0, 0, 0));
        txt_search.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_search.setHint("  Filter Laporan");
        txt_search.setSelectionColor(new java.awt.Color(255, 255, 255));
        txt_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_searchActionPerformed(evt);
            }
        });
        txt_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_searchKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout ShadowSearchLayout = new javax.swing.GroupLayout(ShadowSearch);
        ShadowSearch.setLayout(ShadowSearchLayout);
        ShadowSearchLayout.setHorizontalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel7)
                .addContainerGap(434, Short.MAX_VALUE))
            .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ShadowSearchLayout.createSequentialGroup()
                    .addGap(42, 42, 42)
                    .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(20, Short.MAX_VALUE)))
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ShadowSearchLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        pilihtanggal.setText("...");
        pilihtanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pilihtanggalActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(204, 204, 204));
        jLabel8.setForeground(new java.awt.Color(204, 204, 204));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_search.png"))); // NOI18N

        javax.swing.GroupLayout ShadowSearch1Layout = new javax.swing.GroupLayout(ShadowSearch1);
        ShadowSearch1.setLayout(ShadowSearch1Layout);
        ShadowSearch1Layout.setHorizontalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel8)
                .addContainerGap(434, Short.MAX_VALUE))
            .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ShadowSearch1Layout.createSequentialGroup()
                    .addGap(50, 50, 50)
                    .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(t_date, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(50, Short.MAX_VALUE)))
        );
        ShadowSearch1Layout.setVerticalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ShadowSearch1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(t_date, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout card4Layout = new javax.swing.GroupLayout(card4);
        card4.setLayout(card4Layout);
        card4Layout.setHorizontalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(356, Short.MAX_VALUE))
        );
        card4Layout.setVerticalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtamaLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(shadowPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ShadowUtamaLayout.createSequentialGroup()
                            .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(41, 41, 41)
                            .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(43, 43, 43)
                            .addComponent(card3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(card3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(card2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(113, 113, 113)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(shadowPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelView.add(ShadowUtama, "card2");

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void card1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseClicked
        loadData("Pemasukan");
    }//GEN-LAST:event_card1MouseClicked

    private void card2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseClicked
        loadData("Pengeluaran");
    }//GEN-LAST:event_card2MouseClicked

    private void card3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseClicked
        loadData("");
    }//GEN-LAST:event_card3MouseClicked

    private void card1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseEntered
        card1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card1.setFillColor(new Color(129, 199, 132)); // Warna hover (lebih terang)
        card1.repaint(); // Pastikan komponen direfresh
    }//GEN-LAST:event_card1MouseEntered

    private void card1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseExited
        card1.setCursor(Cursor.getDefaultCursor());
        card1.setFillColor(new Color(76, 175, 80)); // Warna normal
        card1.repaint();
    }//GEN-LAST:event_card1MouseExited

    private void card2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseEntered
        card2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card2.setFillColor(new Color(255, 138, 138)); // Warna hover (lebih terang)
        card2.repaint();
    }//GEN-LAST:event_card2MouseEntered

    private void card2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseExited
        card2.setCursor(Cursor.getDefaultCursor());
        card2.setFillColor(new Color(239, 100, 100)); // Warna normal (hijau)
        card2.repaint();
    }//GEN-LAST:event_card2MouseExited

    private void card3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseEntered
       card3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card3.setFillColor(new Color(255, 255, 150)); // Warna hover (lebih terang)
        card3.repaint();
    }//GEN-LAST:event_card3MouseEntered

    private void card3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseExited
        card3.setCursor(Cursor.getDefaultCursor());
        card3.setFillColor(new Color(255, 254, 84)); // Normal: kuning utama
        card3.repaint();
        
    }//GEN-LAST:event_card3MouseExited

    private void card1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MousePressed
         animateClick(new Color(129, 199, 132), new Color(76, 175, 80));
    
        loadData("Pemasukan");
    }//GEN-LAST:event_card1MousePressed

    private void card2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MousePressed
   
    animateClickCard2(new Color(255, 138, 138), new Color(211, 47, 47));
    loadData("Pengeluaran"); // atau aksi lain sesuai card2
    }//GEN-LAST:event_card2MousePressed

    private void card3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MousePressed
        animateClickCard3(new Color(255, 255, 150), new Color(255, 235, 59));
        loadData("");
    }//GEN-LAST:event_card3MousePressed

    private void card1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseReleased
        animateClick(new Color(76, 175, 80), new Color(129, 199, 132));
    }//GEN-LAST:event_card1MouseReleased

    private void card2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseReleased
        animateClickCard2(new Color(211, 47, 47), new Color(255, 138, 138));
    }//GEN-LAST:event_card2MouseReleased

    private void card3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseReleased
        animateClickCard3(new Color(255, 235, 59), new Color(255, 255, 150));
    }//GEN-LAST:event_card3MouseReleased

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pilihtanggalActionPerformed
       dateChooser1.showPopup();
    }//GEN-LAST:event_pilihtanggalActionPerformed

    private void card4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card4MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_card4MouseClicked

    private void card4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card4MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_card4MouseEntered

    private void card4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card4MouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_card4MouseExited

    private void card4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card4MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_card4MousePressed

    private void card4MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card4MouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_card4MouseReleased

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
       
    }//GEN-LAST:event_txt_searchKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private component.ShadowPanel ShadowUtama;
    private component.Card card1;
    private component.Card card2;
    private component.Card card3;
    private component.Card card4;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_pemasukan;
    private javax.swing.JLabel lb_pengeluaran;
    private javax.swing.JLabel lb_total;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.ShadowPanel shadowPanel1;
    private javax.swing.JTextField t_date;
    private component.Table tb_laporan;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}


