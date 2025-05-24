package view;

import grafik.main.ModelChart;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.DBconnect;
import main.ModelData;
import javax.swing.Timer;




public class TabLaporanStatistik extends javax.swing.JPanel {
    
    
    private final Connection conn = DBconnect.getConnection();
    
    
public TabLaporanStatistik() {
        
         initComponents();
         
         
         
         loadDashboardData();
        loadData("");
       
        
        
        chart.setTitle("Chart Data");

        // Harus ADA 3 legend sesuai urutan data
        chart.addLegend("Amount", Color.decode("#7b4397"), Color.decode("#dc2430"));
        chart.addLegend("Cost", Color.decode("#e65c00"), Color.decode("#F9D423"));
        chart.addLegend("Profit", Color.decode("#00C853"), Color.decode("#2E7D32"));

        setdata();
    }

    public void addEventTablaporantransaksi(ActionListener event) {
        pindahhalaman1.addActionListener(event);
    }

    
   private void setdata() {
    List<ModelData> lists = new ArrayList<>();
    String sql = "SELECT DATE_FORMAT(t.tanggal, '%M') AS 'Month', " +
                 "SUM(t.total_harga) AS Amount, " +
                 "SUM(s.harga) AS Cost, " +
                 "SUM(t.total_harga) - SUM(s.harga) AS Profit " +
                 "FROM laporan_pemasukan lplpm " +
                 "JOIN transaksi t ON lplpm.id_transaksi = t.id_transaksi " +
                 "JOIN jual_sampah s ON lplpm.id_jual_sampah = s.id_jual_sampah " +
                 "GROUP BY DATE_FORMAT(t.tanggal,'%m%Y') " +
                 "ORDER BY t.tanggal DESC " +
                 "LIMIT 7;";

    try (
        Connection conn = DBconnect.getInstance().getConnection();
        PreparedStatement p = conn.prepareStatement(sql);
        ResultSet r = p.executeQuery()
    ) {
        while (r.next()) {
            String month = r.getString("Month");
            double amount = r.getDouble("Amount");
            double cost = r.getDouble("Cost");
            double profit = r.getDouble("Profit");
            lists.add(new ModelData(month, amount, cost, profit));
        }

        // Tambahkan data ke chart (dibalik urutannya agar data terbaru di kanan)
        for (int i = lists.size() - 1; i >= 0; i--) {
            ModelData d = lists.get(i);
            chart.addData(new ModelChart(d.getMonth(), new double[]{d.getAmount(), d.getCost(), d.getProfit()}));
        }

        chart.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

       private void loadData(String filterJenis) {
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    model.setColumnIdentifiers(new String[]{
        "ID", "No", "Nama Admin", "Nama Nasabah", "Nama Barang/Sampah", "Jenis Transaksi",
        "Harga", "Riwayat"
    });

    String baseQuery =
        "SELECT id, nama_admin, nama_nasabah, nama_barang_sampah, jenis_transaksi, harga, riwayat FROM (" +
        "   SELECT " +
        "       lp.id_laporan_pemasukan AS id, " +
        "       u.nama_user AS nama_admin, " +
        "       COALESCE(n.nama_nasabah, '-') AS nama_nasabah, " +
        "       db.nama_barang AS nama_barang_sampah, " +
        "       'Pemasukan' AS jenis_transaksi, " +
        "       db.harga AS harga, " +
        "       lp.riwayat AS riwayat " +
        "   FROM laporan_pemasukan lp " +
        "   JOIN login u ON lp.id_user = u.id_user " +
        "   LEFT JOIN data_barang db ON lp.id_barang = db.id_barang " +
        "   LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah " +
        "   WHERE lp.id_barang IS NOT NULL " +

        "   UNION ALL " +

        "   SELECT " +
        "       lp.id_laporan_pemasukan AS id, " +
        "       u.nama_user AS nama_admin, " +
        "       '-' AS nama_nasabah, " +
        "       kate.nama_kategori AS nama_barang_sampah, " +
        "       'Pemasukan' AS jenis_transaksi, " +
        "       js.harga AS harga, " +
        "       lp.riwayat AS riwayat " +
        "   FROM laporan_pemasukan lp " +
        "   JOIN login u ON lp.id_user = u.id_user " +
        "   LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah " +
        "   JOIN sampah sa ON js.id_sampah = sa.id_sampah " +
        "   JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori " +
        "   WHERE lp.id_jual_sampah IS NOT NULL " +

        "   UNION ALL " +

        "   SELECT " +
        "       lpl.id_laporan_pengeluaran AS id, " +
        "       u.nama_user AS nama_admin, " +
        "       n.nama_nasabah AS nama_nasabah, " +
        "       kate.nama_kategori AS nama_barang_sampah, " +
        "       'Pengeluaran' AS jenis_transaksi, " +
        "       s.harga AS harga, " +
        "       lpl.riwayat AS riwayat " +
        "   FROM laporan_pengeluaran lpl " +
        "   JOIN login u ON lpl.id_user = u.id_user " +
        "   JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran " +
        "   JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah " +
        "   JOIN sampah sa ON s.id_sampah = sa.id_sampah " +
        "   JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori " +
        ") AS combined ";

    if (filterJenis != null && !filterJenis.isEmpty()) {
        baseQuery += "WHERE jenis_transaksi = ? ";
    }

    baseQuery += "ORDER BY riwayat DESC";

    try (Connection conn = DBconnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(baseQuery)) {

        if (filterJenis != null && !filterJenis.isEmpty()) {
            pst.setString(1, filterJenis);
        }

        try (ResultSet rs = pst.executeQuery()) {
            int no = 1;
            while (rs.next()) {
                String harga = rs.getString("harga");
                if (harga != null && !harga.equals("-")) {
                    try {
                        double nominal = Double.parseDouble(harga);
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        harga = formatRupiah.format(nominal);
                    } catch (NumberFormatException e) {
                        // skip
                    }
                }

                model.addRow(new Object[]{
                    rs.getString("id"), // hidden column
                    no++,
                    rs.getString("nama_admin"),
                    rs.getString("nama_nasabah"),
                    rs.getString("nama_barang_sampah"),
                    rs.getString("jenis_transaksi"),
                    harga,
                    rs.getString("riwayat")
                });
            }
        }

        tb_laporan.setModel(model);

        // Hide ID column
        if (tb_laporan.getColumnModel().getColumnCount() > 0) {
            tb_laporan.getColumnModel().getColumn(0).setMinWidth(0);
            tb_laporan.getColumnModel().getColumn(0).setMaxWidth(0);
            tb_laporan.getColumnModel().getColumn(0).setWidth(0);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
    }
}

    private void loadDashboardData() {
    String queryPemasukan = "SELECT SUM(sa.harga)+SUM(t.total_harga) FROM laporan_pemasukan lpl\n" +
"JOIN jual_sampah sa ON lpl.id_jual_sampah=sa.id_jual_sampah\n" +
"JOIN transaksi t ON lpl.id_transaksi=t.id_transaksi";
    String queryPengeluaran = "SELECT SUM(harga) FROM setor_sampah";

    try (Connection conn = DBconnect.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rspemasukan = stmt.executeQuery(queryPemasukan)) {

        double totalpemasukan = 0;
        if (rspemasukan.next()) {
            totalpemasukan = rspemasukan.getDouble(1);
        }

        double totalpengeluaran = 0;
        try (ResultSet rspengeluaran = stmt.executeQuery(queryPengeluaran)) {
            if (rspengeluaran.next()) {
                totalpengeluaran = rspengeluaran.getDouble(1);
            }
        }

        // Format dan tampilkan ke label
        lb_pemasukan.setText(formatRupiah(totalpemasukan));
        lb_pengeluaran.setText(formatRupiah(totalpengeluaran));
        lb_total.setText(formatRupiah(totalpemasukan - totalpengeluaran));

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data dashboard: " + e.getMessage());
    }
}

private String formatRupiah(double amount) {
    String formatted = String.format("Rp %,.2f", amount)
        .replace(',', 'X')   // sementara ubah koma jadi X
        .replace('.', ',')   // titik jadi koma
        .replace('X', '.');  // X (yang tadinya koma) jadi titik
    return formatted;
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
        
 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateChooser1 = new datechooser.Main.DateChooser();
        dateBetween1 = new datechooser.Main.DateBetween();
        defaultDateChooserRender1 = new datechooser.render.DefaultDateChooserRender();
        panelMain = new component.PanelSlide();
        panelView = new javax.swing.JPanel();
        ShadowUtama = new component.ShadowPanel();
        scrollPaneWin111 = new table.scroll.win11.ScrollPaneWin11();
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
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        txt_date = new javax.swing.JTextField();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        card5 = new component.Card();
        chart = new grafik.main.CurveLineChart();
        card6 = new component.Card();
        pindahhalaman1 = new javax.swing.JButton();

        dateChooser1.setDateChooserRender(defaultDateChooserRender1);
        dateChooser1.setDateSelectable(null);
        dateChooser1.setDateSelectionMode(datechooser.Main.DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateChooser1.setTextField(txt_date);
        dateChooser1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dateChooser1FocusGained(evt);
            }
        });
        dateChooser1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                dateChooser1KeyTyped(evt);
            }
        });

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(255, 255, 255));
        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(248, 248, 248));

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Nama Admin", "Nama Nasabah", "Nama Barang/Sampah", "Jenis Transaksi", "Harga", "Riwayat"
            }
        ));
        scrollPaneWin111.setViewportView(tb_laporan);

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
                .addContainerGap(239, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(211, Short.MAX_VALUE))
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
                .addContainerGap(11, Short.MAX_VALUE))
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
                .addContainerGap(110, Short.MAX_VALUE))
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

        txt_search.setBorder(null);
        txt_search.setForeground(new java.awt.Color(0, 0, 0));
        txt_search.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_search.setHint("  Cari Nama");
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

        box_pilih.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nama Admin", "Nama Nasabah" }));
        box_pilih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_pilihActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ShadowSearchLayout = new javax.swing.GroupLayout(ShadowSearch);
        ShadowSearch.setLayout(ShadowSearchLayout);
        ShadowSearchLayout.setHorizontalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(box_pilih, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(box_pilih))
                .addContainerGap())
        );

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        txt_date.setBackground(new java.awt.Color(230, 245, 241));
        txt_date.setBorder(null);
        txt_date.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txt_datePropertyChange(evt);
            }
        });
        txt_date.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_dateKeyTyped(evt);
            }
        });

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
                .addContainerGap(473, Short.MAX_VALUE))
            .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ShadowSearch1Layout.createSequentialGroup()
                    .addGap(50, 50, 50)
                    .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txt_date, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(19, Short.MAX_VALUE)))
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
                        .addComponent(txt_date, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout card4Layout = new javax.swing.GroupLayout(card4);
        card4.setLayout(card4Layout);
        card4Layout.setHorizontalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(ShadowSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9))
        );
        card4Layout.setVerticalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ShadowSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                    .addComponent(ShadowSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
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
                .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
        card5Layout.setVerticalGroup(
            card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chart, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        card6.setFillColor(new java.awt.Color(0, 204, 204));

        javax.swing.GroupLayout card6Layout = new javax.swing.GroupLayout(card6);
        card6.setLayout(card6Layout);
        card6Layout.setHorizontalGroup(
            card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        card6Layout.setVerticalGroup(
            card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 265, Short.MAX_VALUE)
        );

        pindahhalaman1.setText("Halaman Laporan Transaksi");
        pindahhalaman1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pindahhalaman1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                                .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(44, 44, 44)
                                .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(scrollPaneWin111, javax.swing.GroupLayout.PREFERRED_SIZE, 852, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                                .addComponent(pindahhalaman1)
                                .addGap(40, 40, 40)))
                        .addGap(18, 18, 18)
                        .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(card3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(card5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(card6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addComponent(pindahhalaman1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollPaneWin111, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addComponent(card5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(card6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        panelView.add(ShadowUtama, "card2");

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1194, Short.MAX_VALUE)
            .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(panelView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 944, Short.MAX_VALUE)
            .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(panelView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        add(panelMain, "card3");
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

    private void dateChooser1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dateChooser1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_dateChooser1FocusGained

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txt_date.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_searchKeyTyped

    private void txt_dateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_dateKeyTyped
       
    }//GEN-LAST:event_txt_dateKeyTyped

    private void dateChooser1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dateChooser1KeyTyped

    }//GEN-LAST:event_dateChooser1KeyTyped

    private void txt_datePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txt_datePropertyChange
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_datePropertyChange

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_pilihActionPerformed
       searchByKeywordAndDate();
    }//GEN-LAST:event_box_pilihActionPerformed

    private void pindahhalaman1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pindahhalaman1ActionPerformed

    }//GEN-LAST:event_pindahhalaman1ActionPerformed
    
  
    
    
  private void searchByKeywordAndDate() {
    String kataKunci = txt_search.getText().trim();
    String tanggalRange = txt_date.getText().trim();
    String filter = box_pilih.getSelectedItem().toString();

    String tanggalMulai = "";
    String tanggalAkhir = "";
    boolean isRange = false;
    boolean isSingleDate = false;

    if (!tanggalRange.isEmpty()) {
        if (tanggalRange.contains("dari")) {
            String[] parts = tanggalRange.split("dari");
            if (parts.length == 2) {
                tanggalMulai = parts[0].trim();
                tanggalAkhir = parts[1].trim();
                isRange = true;
            }
        } else {
            tanggalMulai = tanggalRange;
            isSingleDate = true;
        }
    }

    DefaultTableModel model = (DefaultTableModel) tb_laporan.getModel();
    model.setRowCount(0);

    Connection conn = null;
    PreparedStatement st = null;
    ResultSet rs = null;

    try {
        conn = DBconnect.getConnection();  // Panggil koneksi di sini

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, nama_admin, nama_nasabah, nama_barang_sampah, jenis_transaksi, harga, riwayat FROM (")
            .append(" SELECT lp.id_laporan_pemasukan AS id, u.nama_user AS nama_admin, COALESCE(n.nama_nasabah, '-') AS nama_nasabah, db.nama_barang AS nama_barang_sampah, 'Pemasukan' AS jenis_transaksi, db.harga AS harga, lp.riwayat AS riwayat FROM laporan_pemasukan lp JOIN login u ON lp.id_user = u.id_user LEFT JOIN data_barang db ON lp.id_barang = db.id_barang LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah WHERE lp.id_barang IS NOT NULL")
            .append(" UNION ALL ")
            .append(" SELECT lp.id_laporan_pemasukan AS id, u.nama_user AS nama_admin, '-' AS nama_nasabah, kate.nama_kategori AS nama_barang_sampah, 'Pemasukan' AS jenis_transaksi, js.harga AS harga, lp.riwayat AS riwayat FROM laporan_pemasukan lp JOIN login u ON lp.id_user = u.id_user LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah JOIN sampah sa ON js.id_sampah = sa.id_sampah JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori WHERE lp.id_jual_sampah IS NOT NULL")
            .append(" UNION ALL ")
            .append(" SELECT lpl.id_laporan_pengeluaran AS id, u.nama_user AS nama_admin, n.nama_nasabah AS nama_nasabah, kate.nama_kategori AS nama_barang_sampah, 'Pengeluaran' AS jenis_transaksi, s.harga AS harga, lpl.riwayat AS riwayat FROM laporan_pengeluaran lpl JOIN login u ON lpl.id_user = u.id_user JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah JOIN sampah sa ON s.id_sampah = sa.id_sampah JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori ")
            .append(") AS combined ");

        boolean whereAdded = false;

        if (!kataKunci.isEmpty()) {
            if (filter.equals("Default")) {
                sql.append("WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ?) ");
                whereAdded = true;
            } else if (filter.equals("Nama Admin")) {
                sql.append("WHERE (nama_admin LIKE ?) ");
                whereAdded = true;
            } else if (filter.equals("Nama Nasabah")) {
                sql.append("WHERE (nama_nasabah LIKE ?) ");
                whereAdded = true;
            }
        }

        if (isRange) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("riwayat BETWEEN ? AND ? ");
        } else if (isSingleDate) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("riwayat = ? ");
        }

        sql.append("ORDER BY riwayat DESC");

        st = conn.prepareStatement(sql.toString());
        int paramIndex = 1;

        if (!kataKunci.isEmpty()) {
            String searchPattern = "%" + kataKunci + "%";
            if (filter.equals("Default")) {
                st.setString(paramIndex++, searchPattern);
                st.setString(paramIndex++, searchPattern);
            } else {
                st.setString(paramIndex++, searchPattern);
            }
        }

        if (isRange) {
            st.setString(paramIndex++, tanggalMulai);
            st.setString(paramIndex++, tanggalAkhir);
        } else if (isSingleDate) {
            st.setString(paramIndex++, tanggalMulai);
        }

        rs = st.executeQuery();
        int no = 1;

        while (rs.next()) {
            String harga = rs.getString("harga");
            if (harga != null && !harga.equals("-")) {
                try {
                    double nominal = Double.parseDouble(harga);
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    harga = formatRupiah.format(nominal);
                } catch (NumberFormatException e) {
                    // abaikan
                }
            }

            Object[] rowData = {
                rs.getString("id"),
                no++,
                rs.getString("nama_admin"),
                rs.getString("nama_nasabah"),
                rs.getString("nama_barang_sampah"),
                rs.getString("jenis_transaksi"),
                harga,
                rs.getString("riwayat")
            };
            model.addRow(rowData);
        }

        tb_laporan.clearSelection();

    } catch (SQLException e) {
        Logger.getLogger(TabManajemenNasabah.class.getName()).log(Level.SEVERE, null, e);
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (st != null) st.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}







    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private component.ShadowPanel ShadowUtama;
    private javax.swing.JComboBox<String> box_pilih;
    private component.Card card1;
    private component.Card card2;
    private component.Card card3;
    private component.Card card4;
    private component.Card card5;
    private component.Card card6;
    private grafik.main.CurveLineChart chart;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lb_pemasukan;
    private javax.swing.JLabel lb_pengeluaran;
    private javax.swing.JLabel lb_total;
    private component.PanelSlide panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private javax.swing.JButton pindahhalaman1;
    private table.scroll.win11.ScrollPaneWin11 scrollPaneWin111;
    private component.Table tb_laporan;
    private javax.swing.JTextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}



