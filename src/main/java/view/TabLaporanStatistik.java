package view;

import grafik.main.ModelChart;
import java.awt.Color;
import java.awt.Cursor;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import main.DBconnect;
import main.ModelData;

public class TabLaporanStatistik extends javax.swing.JPanel {
    
    public TabLaporanStatistik() {
         initComponents();
         loadDashboardData();
        loadData("");
        
        chart.setTitle("Chart Data");
        chart.addLegend("Amount", Color.decode("#7b4397"), Color.decode("#dc2430"));
        //chart.addLegend("Cost", Color.decode("#e65c00"), Color.decode("#F9D423"));
        chart.addLegend("Profit", Color.decode("#0099F7"), Color.decode("#F11712"));
        test();
    }
    
    private void test() {
        chart.clear();
        chart.addData(new ModelChart("January", new double[]{500, 50, 100}));
        chart.addData(new ModelChart("February", new double[]{600, 300, 150}));
        chart.addData(new ModelChart("March", new double[]{200, 50, 900}));
        chart.addData(new ModelChart("April", new double[]{480, 700, 100}));
        chart.addData(new ModelChart("May", new double[]{350, 540, 500}));
        chart.addData(new ModelChart("June", new double[]{450, 800, 100}));
        chart.start();
    }
    
    private void setdata(){
        try {
            List<ModelData> lists = new ArrayList<>();
        DBconnect.getInstance().getConnection();
        String sql="SELECT DATE_FORMAT(tanggal,'%m') AS 'Month', SUM(total_jumlah) AS 'Amount', SUM(total_harga) AS 'Profit' FROM `transaksi` GROUP BY DATE_FORMAT(tanggal,'%m%Y') ORDER BY tanggal DESC LIMIT 7";
        PreparedStatement p = DBconnect.getInstance().getConn().prepareStatement(sql);
        ResultSet r = p.executeQuery();
            while (r.next()) {
                String month = r.getString("Month");
                double amount = r.getDouble("Amount");  
                double profit = r.getDouble("Profit");
                lists.add(new ModelData(month, amount, profit));
            }
            r.close();
            p.close();
            //  Add Data to chart
            for (int i = lists.size() - 1; i >= 0; i--) {
                ModelData d = lists.get(i);
                chart.addData(new ModelChart(d.getMonth(), new double[]{d.getAmount(), d.getProfit()}));
            }
            //  Start to show data with animation
            chart.start();
        } catch (Exception e) {
            e.printStackTrace();
        
    }
        
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
                panelMain.add(new TabLaporanStatistik());
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
        ShadowSearch1 = new component.ShadowPanel();
        t_date = new javax.swing.JTextField();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        card5 = new component.Card();
        chart = new grafik.main.CurveLineChart();

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
                .addContainerGap(146, Short.MAX_VALUE))
        );
        card1Layout.setVerticalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(card1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lb_pemasukan))
                    .addGroup(card1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel5)))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        card2.setFillColor(new java.awt.Color(255, 255, 255));
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
                .addContainerGap(159, Short.MAX_VALUE))
        );
        card2Layout.setVerticalGroup(
            card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel15)
                    .addGroup(card2Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lb_pengeluaran)
                        .addGap(6, 6, 6)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        card3.setFillColor(new java.awt.Color(255, 255, 255));
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

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Jumlah.png"))); // NOI18N

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
                .addContainerGap(163, Short.MAX_VALUE))
        );
        card3Layout.setVerticalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addGroup(card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(card3Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lb_total))
                    .addGroup(card3Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel18)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        card4.setFillColor(new java.awt.Color(255, 255, 255));

        ShadowSearch.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch.setPreferredSize(new java.awt.Dimension(259, 43));

        jLabel7.setBackground(new java.awt.Color(204, 204, 204));
        jLabel7.setForeground(new java.awt.Color(204, 204, 204));
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_search.png"))); // NOI18N

        javax.swing.GroupLayout ShadowSearchLayout = new javax.swing.GroupLayout(ShadowSearch);
        ShadowSearch.setLayout(ShadowSearchLayout);
        ShadowSearchLayout.setHorizontalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel7)
                .addContainerGap(434, Short.MAX_VALUE))
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        t_date.setBackground(new java.awt.Color(230, 245, 241));
        t_date.setBorder(null);

        pilihtanggal.setText("...");
        pilihtanggal.setBorder(null);
        pilihtanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pilihtanggalActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(204, 204, 204));
        jLabel8.setForeground(new java.awt.Color(204, 204, 204));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Calendar.png"))); // NOI18N

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
                    .addComponent(t_date, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(23, Short.MAX_VALUE)))
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
                .addContainerGap(175, Short.MAX_VALUE))
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

        card5.setBackground(new java.awt.Color(204, 204, 204));
        card5.setFillColor(new java.awt.Color(255, 255, 255));

        chart.setBackground(new java.awt.Color(0, 0, 0));
        chart.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chart.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout card5Layout = new javax.swing.GroupLayout(card5);
        card5.setLayout(card5Layout);
        card5Layout.setHorizontalGroup(
            card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        card5Layout.setVerticalGroup(
            card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chart, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addComponent(card4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtamaLayout.createSequentialGroup()
                        .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                                .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))
                        .addGap(66, 66, 66)
                        .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(card5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(27, 27, 27))))
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 593, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        panelView.add(ShadowUtama, "card2");

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void card1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseClicked
        loadData("Pemasukan");
    }//GEN-LAST:event_card1MouseClicked

    private void card1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseEntered
        card1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card1.setFillColor(new Color(240, 240, 240)); // Warna hover (lebih terang)
        card1.repaint(); // Pastikan komponen direfresh
    }//GEN-LAST:event_card1MouseEntered

    private void card1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseExited
        card1.setCursor(Cursor.getDefaultCursor());
        card1.setFillColor(new Color(255, 255, 255)); // Warna normal
        card1.repaint();
    }//GEN-LAST:event_card1MouseExited

    private void card1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MousePressed
        animateClick(new Color(224, 224, 224), new Color(158, 158, 158));

        loadData("Pemasukan");
    }//GEN-LAST:event_card1MousePressed

    private void card1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card1MouseReleased
        animateClick(new Color(158, 158, 158), new Color(224, 224, 224));
    }//GEN-LAST:event_card1MouseReleased

    private void card2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseClicked
        loadData("Pengeluaran");
    }//GEN-LAST:event_card2MouseClicked

    private void card2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseEntered
        card2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card2.setFillColor(new Color(240, 240, 240)); // Warna hover (lebih terang)
        card2.repaint();
    }//GEN-LAST:event_card2MouseEntered

    private void card2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseExited
        card2.setCursor(Cursor.getDefaultCursor());
        card2.setFillColor(new Color(255, 255, 255)); // Warna normal (hijau)
        card2.repaint();
    }//GEN-LAST:event_card2MouseExited

    private void card2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MousePressed

        animateClickCard2(new Color(224, 224, 224), new Color(158, 158, 158));
        loadData("Pengeluaran"); // atau aksi lain sesuai card2
    }//GEN-LAST:event_card2MousePressed

    private void card2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card2MouseReleased
        animateClickCard2(new Color(158, 158, 158), new Color(224, 224, 224));
    }//GEN-LAST:event_card2MouseReleased

    private void card3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseClicked
        loadData("");
    }//GEN-LAST:event_card3MouseClicked

    private void card3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseEntered
        card3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card3.setFillColor(new Color(240, 240, 240)); // Warna hover (lebih terang)
        card3.repaint();
    }//GEN-LAST:event_card3MouseEntered

    private void card3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseExited
        card3.setCursor(Cursor.getDefaultCursor());
        card3.setFillColor(new Color(255, 255, 255)); // Normal: kuning utama
        card3.repaint();

    }//GEN-LAST:event_card3MouseExited

    private void card3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MousePressed
        animateClickCard3(new Color(224, 224, 224), new Color(158, 158, 158));
        loadData("");
    }//GEN-LAST:event_card3MousePressed

    private void card3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_card3MouseReleased
        animateClickCard3(new Color(158, 158, 158), new Color(224, 224, 224));
    }//GEN-LAST:event_card3MouseReleased

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pilihtanggalActionPerformed
        dateChooser1.showPopup();
    }//GEN-LAST:event_pilihtanggalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private component.ShadowPanel ShadowUtama;
    private component.Card card1;
    private component.Card card2;
    private component.Card card3;
    private component.Card card4;
    private component.Card card5;
    private grafik.main.CurveLineChart chart;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
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
    private javax.swing.JTextField t_date;
    private component.Table tb_laporan;
    // End of variables declaration//GEN-END:variables
}


