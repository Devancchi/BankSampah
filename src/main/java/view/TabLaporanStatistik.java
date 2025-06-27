package view;

import component.ExcelExporter;
import component.LoggerUtil;
import component.UserSession;
import grafik.main.ModelChart;
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
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
import notification.toast.Notifications;
import main.ModelData;
import java.text.DecimalFormat;

public class TabLaporanStatistik extends javax.swing.JPanel {
    private final Connection conn = DBconnect.getConnection();
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 20;
    private int totalPages;
    private int totalData;
    private UserSession users;

    public TabLaporanStatistik(UserSession user) {
        this.users = user;
        initComponents();
        loadDashboardData();
        txt_date.setText("");

        chart.setTitle("Chart Data");

        chart.addLegend("Cost", Color.decode("#7b4397"), Color.decode("#dc2430"));
        chart.addLegend("Gross Profit", Color.decode("#e65c00"), Color.decode("#F9D423"));
        chart.addLegend("Profit", Color.decode("#00C853"), Color.decode("#2E7D32"));

        setdata();
        setupPagination();

        // Initially load all transactions without filtering
        loadData(""); // KEEP THIS - it loads the correct table model initially
    }

    private void setdata() {
        try {
            List<ModelData> lists = new ArrayList<>();
            DBconnect.getInstance().getConnection();
            String sql = "SELECT Month, SUM(Cost) AS Cost, SUM(GrossProfit) AS GrossProfit, SUM(GrossProfit) - SUM(Cost) AS Profit FROM (" +
                "SELECT strftime('%m-%Y', t.tanggal) AS Month, SUM(t.total_harga) AS GrossProfit, 0 AS Cost FROM laporan_pemasukan lpm JOIN transaksi t ON lpm.id_transaksi = t.id_transaksi GROUP BY strftime('%m-%Y', t.tanggal) " +
                "UNION ALL " +
                "SELECT strftime('%m-%Y', js.tanggal) AS Month, SUM(js.harga) AS GrossProfit, 0 AS Cost FROM laporan_pemasukan lpm JOIN jual_sampah js ON lpm.id_jual_sampah = js.id_jual_sampah GROUP BY strftime('%m-%Y', js.tanggal) " +
                "UNION ALL " +
                "SELECT strftime('%m-%Y', lp.riwayat) AS Month, 0 AS GrossProfit, SUM(ss.harga) AS Cost FROM laporan_pengeluaran lp JOIN setor_sampah ss ON lp.id_setoran = ss.id_setoran GROUP BY strftime('%m-%Y', lp.riwayat) " +
            ") AS data GROUP BY Month ORDER BY substr(Month, 4, 4) || '-' || substr(Month, 1, 2) DESC LIMIT 12;";
            PreparedStatement p = DBconnect.getInstance().getConnection().prepareStatement(sql);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                String month = r.getString("Month");
                double cost = r.getDouble("Cost");
                double grossprofit = r.getDouble("GrossProfit");
                double profit = r.getDouble("Profit");
                lists.add(new ModelData(month, cost, grossprofit, profit));
            }
            p.close();
            r.close();

            // Fix: Check if we have enough data points to render the chart
            if (lists.size() >= 4) {
                // We have enough data points, proceed normally
                for (int i = lists.size() - 1; i >= 0; i--) {
                    ModelData d = lists.get(i);
                    chart.addData(new ModelChart(d.getMonth(),
                            new double[] { d.getCost(), d.getGrossProfit(), d.getProfit() }));
                }
                chart.start();
            } else if (lists.size() > 0) {
                // We have some data but not enough for spline rendering
                // Add dummy data points to make at least 4 points
                for (int i = lists.size() - 1; i >= 0; i--) {
                    ModelData d = lists.get(i);
                    chart.addData(new ModelChart(d.getMonth(),
                            new double[] { d.getCost(), d.getGrossProfit(), d.getProfit() }));
                }

                // Add extra dummy data points with zero values
                for (int i = 0; i < (4 - lists.size()); i++) {
                    chart.addData(new ModelChart("Dummy " + i, new double[] { 0, 0, 0 }));
                }
                chart.start();
            } else {
                // No data at all - add 4 dummy points
                for (int i = 0; i < 4; i++) {
                    chart.addData(new ModelChart("No Data " + i, new double[] { 0, 0, 0 }));
                }
                chart.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading chart data: " + e.getMessage());
        }
    }

    private void loadData(String filterJenis) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Update columns for unified transaction view
        model.setColumnIdentifiers(new String[] {
                "No", "Nama Admin", "Nama Nasabah", "Transaksi", "Detail", "Nominal", "Status", "Waktu"
        });

        String baseQuery = """
                SELECT
                    nama_admin,
                    nama_nasabah,
                    jenis_transaksi,
                    detail,
                    nominal,
                    status,
                    waktu_transaksi
                FROM (
                    -- Transaksi penjualan barang (Pemasukan)
                    SELECT
                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                        COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                        'Penjualan Barang' AS jenis_transaksi,
                        tr.nama_barang AS detail,
                        tr.total_harga AS nominal,
                        'Pemasukan' AS status,
                        tr.tanggal AS waktu_transaksi
                    FROM laporan_pemasukan lp
                    LEFT JOIN login u ON lp.id_user = u.id_user
                    LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                    LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                    WHERE lp.id_transaksi IS NOT NULL

                    UNION ALL

                    -- Penjualan sampah (Pemasukan)
                    SELECT
                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                        '-' AS nama_nasabah,
                        'Penjualan Sampah' AS jenis_transaksi,
                        kate.nama_kategori AS detail,
                        js.harga AS nominal,
                        'Pemasukan' AS status,
                        js.tanggal AS waktu_transaksi
                    FROM laporan_pemasukan lp
                    LEFT JOIN login u ON lp.id_user = u.id_user
                    LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                    JOIN sampah sa ON js.id_sampah = sa.id_sampah
                    JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                    WHERE lp.id_jual_sampah IS NOT NULL

                    UNION ALL

                    -- Setoran sampah (Pengeluaran)
                    SELECT
                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                        COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                        'Setoran Sampah' AS jenis_transaksi,
                        kate.nama_kategori AS detail,
                        s.harga AS nominal,
                        'Pengeluaran' AS status,
                        s.tanggal AS waktu_transaksi
                    FROM laporan_pengeluaran lpl
                    LEFT JOIN login u ON lpl.id_user = u.id_user
                    JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                    LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                    JOIN sampah sa ON s.id_sampah = sa.id_sampah
                    JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                    WHERE lpl.id_setoran IS NOT NULL

                    UNION ALL

                    -- Penarikan saldo (Pengeluaran dari perspektif bank)
                    SELECT
                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                        COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                        'Penarikan Saldo' AS jenis_transaksi,
                        'Tarik Tunai' AS detail,
                        ps.jumlah_penarikan AS nominal,
                        'Pengeluaran' AS status,
                        ps.tanggal_penarikan AS waktu_transaksi
                    FROM penarikan_saldo ps
                    LEFT JOIN login u ON ps.id_user = u.id_user
                    LEFT JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                ) AS combined_data
                """;

        // Add filter if specified
        if (filterJenis != null && !filterJenis.isEmpty()) {
            baseQuery += " WHERE status = ? ";
        }

        // Always sort by datetime in descending order (newest first)
        baseQuery += " ORDER BY waktu_transaksi DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBconnect.getConnection();
                PreparedStatement pst = conn.prepareStatement(baseQuery)) {

            int paramIndex = 1;
            if (filterJenis != null && !filterJenis.isEmpty()) {
                pst.setString(paramIndex++, filterJenis);
            }

            // Add pagination parameters
            int startIndex = (halamanSaatIni - 1) * dataPerHalaman;
            pst.setInt(paramIndex++, dataPerHalaman);
            pst.setInt(paramIndex, startIndex);

            try (ResultSet rs = pst.executeQuery()) {
                int no = startIndex + 1;
                while (rs.next()) {
                    String nominal = rs.getString("nominal");
                    if (nominal != null && !nominal.equals("-")) {
                        try {
                            double amount = Double.parseDouble(nominal);
                            NumberFormat formatRupiah = NumberFormat
                                    .getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                            formatRupiah.setMaximumFractionDigits(0);
                            formatRupiah.setMinimumFractionDigits(0);
                            nominal = formatRupiah.format(amount);
                        } catch (NumberFormatException e) {
                            // Keep original value if formatting fails
                        }
                    }

                    model.addRow(new Object[] {
                            no++,
                            rs.getString("nama_admin"),
                            rs.getString("nama_nasabah"),
                            rs.getString("jenis_transaksi"),
                            rs.getString("detail"),
                            nominal,
                            rs.getString("status"),
                            rs.getString("waktu_transaksi")
                    });
                }
            }

            tb_laporan.setModel(model);
            updatePaginationInfo();
            calculateTotalPage();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculateTotalPage() {
        try {
            String countQuery = """
                        SELECT COUNT(*) as total FROM (
                        -- Transaksi penjualan barang (Pemasukan)
                        SELECT tr.tanggal FROM laporan_pemasukan lp
                        JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                        WHERE lp.id_transaksi IS NOT NULL

                        UNION ALL

                        -- Penjualan sampah (Pemasukan)
                        SELECT js.tanggal FROM laporan_pemasukan lp
                        JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                        WHERE lp.id_jual_sampah IS NOT NULL

                        UNION ALL

                        -- Setoran sampah (Pengeluaran)
                        SELECT s.tanggal FROM laporan_pengeluaran lpl
                        JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                        WHERE lpl.id_setoran IS NOT NULL

                        UNION ALL

                        -- Penarikan saldo (Pengeluaran)
                        SELECT ps.tanggal_penarikan FROM penarikan_saldo ps
                    ) AS all_transactions
                    """;

            try (Connection conn = DBconnect.getConnection();
                    PreparedStatement pst = conn.prepareStatement(countQuery);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalData = rs.getInt("total");
                    totalPages = (int) Math.ceil((double) totalData / dataPerHalaman);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghitung total halaman: " + e.getMessage());
        }
    }

    private void updatePaginationInfo() {
        calculateTotalPage();
        lb_halaman1.setText("Page " + halamanSaatIni + " Dari Total " + totalData + " Data");
    }

    private void setupPagination() {
        btn_first1.addActionListener(e -> {
            halamanSaatIni = 1;
            loadData("");
        });

        btn_before1.addActionListener(e -> {
            if (halamanSaatIni > 1) {
                halamanSaatIni--;
                loadData("");
            }
        });

        btn_next1.addActionListener(e -> {
            if (halamanSaatIni < totalPages) {
                halamanSaatIni++;
                loadData("");
            }
        });

        btn_last1.addActionListener(e -> {
            halamanSaatIni = totalPages;
            loadData("");
        });

        cbx_data1.addActionListener(e -> {
            dataPerHalaman = Integer.parseInt(cbx_data1.getSelectedItem().toString());
            halamanSaatIni = 1;
            calculateTotalPage();
            loadData("");
        });
    }

    private void loadDashboardData() {
        try {
            Connection conn = DBconnect.getConnection();
            Statement stmt = conn.createStatement();

            // Ambil total pemasukan
            double totalpemasukan = 0;
            ResultSet rspemasukan = stmt.executeQuery(
                    "SELECT COALESCE(SUM(tr.total_harga), 0) + COALESCE(SUM(sp.harga), 0) FROM laporan_pemasukan lp LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi LEFT JOIN jual_sampah sp ON lp.id_jual_sampah = sp.id_jual_sampah ORDER BY riwayat DESC;");
            if (rspemasukan.next()) {
                totalpemasukan = rspemasukan.getDouble(1);
                DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
                String formatted = formatRupiah.format(totalpemasukan);
                lb_pemasukan.setText(formatted);
            }

            // Ambil total pengeluaran
            double totalpengeluaran = 0;
            ResultSet rspengeluaran = stmt.executeQuery("SELECT SUM(harga) FROM setor_sampah ORDER BY tanggal DESC");
            if (rspengeluaran.next()) {
                totalpengeluaran = rspengeluaran.getDouble(1);
                DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
                String formatted = formatRupiah.format(totalpengeluaran);
                lb_pengeluaran.setText(formatted);
            }

            // Hitung total akhir (pemasukan - pengeluaran)
            double totalakhir = totalpemasukan - totalpengeluaran;
            DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
            String formattedTotal = formatRupiah.format(totalakhir);
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
            cardPemasukan.setFillColor(currentColor);
            cardPemasukan.repaint();

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
            cardPengeluaran.setFillColor(currentColor);
            cardPengeluaran.repaint();

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
            cardTransaksi.setFillColor(currentColor);
            cardTransaksi.repaint();

            animationStep3++;
            if (animationStep3 > totalSteps3) {
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer3.start();
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik(users));
        panelMain.repaint();
        panelMain.revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateChooser1 = new datechooser.Main.DateChooser();
        dateBetween1 = new datechooser.Main.DateBetween();
        defaultDateChooserRender1 = new datechooser.render.DefaultDateChooserRender();
        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        panelCard = new component.ShadowPanel();
        cardPengeluaran = new component.Card();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lb_pengeluaran = new javax.swing.JLabel();
        cardTransaksi = new component.Card();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        lb_total = new javax.swing.JLabel();
        cardPemasukan = new component.Card();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lb_pemasukan = new javax.swing.JLabel();
        panelTable = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        panelBawah1 = new component.ShadowPanel();
        lb_halaman1 = new javax.swing.JLabel();
        btn_before1 = new javax.swing.JButton();
        cbx_data1 = new javax.swing.JComboBox<>();
        btn_next1 = new javax.swing.JButton();
        btn_last1 = new javax.swing.JButton();
        btn_first1 = new javax.swing.JButton();
        btn_Export = new component.Jbutton();
        ShadowSearch = new component.ShadowPanel();
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txt_date = new swing.TextField();
        btnReset = new javax.swing.JButton();
        panelCurve = new component.ShadowPanel();
        btn_detail_pemasukan = new ripple.button.Button();
        btn_laporan_transaksi = new ripple.button.Button();
        btn_laporan_jual_sampah = new ripple.button.Button();
        jLabel1 = new javax.swing.JLabel();
        panelCurve1 = new component.ShadowPanel();
        chart = new grafik.main.CurveLineChart();
        btn_laporan_saldo = new ripple.button.Button();
        jLabel14 = new javax.swing.JLabel();

        dateChooser1.setDateChooserRender(defaultDateChooserRender1);
        dateChooser1.setDateSelectable(null);
        dateChooser1.setDateSelectionMode(datechooser.Main.DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateChooser1.setTextField(txt_date);

        setBackground(new java.awt.Color(24, 58, 51));
        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(253, 253, 253));

        cardPengeluaran.setFillColor(new java.awt.Color(255, 236, 238));
        cardPengeluaran.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardPengeluaranMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cardPengeluaranMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardPengeluaranMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cardPengeluaranMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cardPengeluaranMouseReleased(evt);
            }
        });

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_total_sampah.png"))); // NOI18N

        jLabel16.setFont(jLabel16.getFont().deriveFont(jLabel16.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel16.setText("Total Pengeluaran");

        lb_pengeluaran.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_pengeluaran.setForeground(new java.awt.Color(206, 79, 91));
        lb_pengeluaran.setText("1.000");

        javax.swing.GroupLayout cardPengeluaranLayout = new javax.swing.GroupLayout(cardPengeluaran);
        cardPengeluaran.setLayout(cardPengeluaranLayout);
        cardPengeluaranLayout.setHorizontalGroup(
            cardPengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPengeluaranLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardPengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lb_pengeluaran, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cardPengeluaranLayout.setVerticalGroup(
            cardPengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPengeluaranLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_pengeluaran)
                .addContainerGap(35, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPengeluaranLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        cardTransaksi.setFillColor(new java.awt.Color(254, 244, 208));
        cardTransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardTransaksiMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cardTransaksiMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardTransaksiMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cardTransaksiMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cardTransaksiMouseReleased(evt);
            }
        });

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Jumlah.png"))); // NOI18N

        jLabel19.setFont(jLabel19.getFont().deriveFont(jLabel19.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel19.setText("Total Profit");

        lb_total.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_total.setForeground(new java.awt.Color(251, 215, 63));
        lb_total.setText("Rp 1.000");

        javax.swing.GroupLayout cardTransaksiLayout = new javax.swing.GroupLayout(cardTransaksi);
        cardTransaksi.setLayout(cardTransaksiLayout);
        cardTransaksiLayout.setHorizontalGroup(
            cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTransaksiLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lb_total, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cardTransaksiLayout.setVerticalGroup(
            cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTransaksiLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_total)
                .addContainerGap(36, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardTransaksiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        cardPemasukan.setBackground(new java.awt.Color(234, 250, 247));
        cardPemasukan.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        cardPemasukan.setFillColor(new java.awt.Color(214, 255, 247));
        cardPemasukan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardPemasukanMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cardPemasukanMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardPemasukanMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cardPemasukanMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cardPemasukanMouseReleased(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_totalPemasukan.png"))); // NOI18N

        jLabel10.setBackground(new java.awt.Color(0, 0, 0));
        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel10.setText("Total Pemasukan");

        lb_pemasukan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_pemasukan.setForeground(new java.awt.Color(28, 205, 174));
        lb_pemasukan.setText("1.000");

        javax.swing.GroupLayout cardPemasukanLayout = new javax.swing.GroupLayout(cardPemasukan);
        cardPemasukan.setLayout(cardPemasukanLayout);
        cardPemasukanLayout.setHorizontalGroup(
            cardPemasukanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPemasukanLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardPemasukanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lb_pemasukan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cardPemasukanLayout.setVerticalGroup(
            cardPemasukanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPemasukanLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_pemasukan)
                .addContainerGap(36, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPemasukanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelCardLayout = new javax.swing.GroupLayout(panelCard);
        panelCard.setLayout(panelCardLayout);
        panelCardLayout.setHorizontalGroup(
            panelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(cardPemasukan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(42, 42, 42)
                .addComponent(cardPengeluaran, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(42, 42, 42)
                .addComponent(cardTransaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelCardLayout.setVerticalGroup(
            panelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardPemasukan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardPengeluaran, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardTransaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tb_laporan);

        lb_halaman1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lb_halaman1.setText("hal");

        btn_before1.setText("<");

        cbx_data1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "20", "40", "60", "80" }));

        btn_next1.setText(">");

        btn_last1.setText("Last Page");

        btn_first1.setText("First Page");

        btn_Export.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_Export.setText("Export To Excel");
        btn_Export.setFillClick(new java.awt.Color(55, 130, 60));
        btn_Export.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_Export.setFillOver(new java.awt.Color(69, 160, 75));
        btn_Export.setRoundedCorner(40);
        btn_Export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBawah1Layout = new javax.swing.GroupLayout(panelBawah1);
        panelBawah1.setLayout(panelBawah1Layout);
        panelBawah1Layout.setHorizontalGroup(
            panelBawah1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah1Layout.createSequentialGroup()
                .addComponent(btn_Export, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_halaman1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_first1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_before1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbx_data1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_next1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_last1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        panelBawah1Layout.setVerticalGroup(
            panelBawah1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBawah1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Export, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb_halaman1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_first1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_before1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbx_data1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_next1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_last1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ShadowSearch.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch.setPreferredSize(new java.awt.Dimension(259, 43));

        txt_search.setBorder(null);
        txt_search.setForeground(new java.awt.Color(0, 0, 0));
        txt_search.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_search.setHint("Cari Nama");
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

        box_pilih.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nama Admin", "Nama Nasabah", "Nama Barang/Sampah" }));
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
                .addGap(0, 0, 0)
                .addComponent(box_pilih, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(box_pilih))
                .addContainerGap())
        );

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        pilihtanggal.setText("...");
        pilihtanggal.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pilihtanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pilihtanggalActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(204, 204, 204));
        jLabel8.setForeground(new java.awt.Color(204, 204, 204));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Calendar.png"))); // NOI18N

        txt_date.setBorder(null);
        txt_date.setForeground(new java.awt.Color(0, 0, 0));
        txt_date.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_date.setHint("Tanggal");
        txt_date.setSelectionColor(new java.awt.Color(255, 255, 255));
        txt_date.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_dateActionPerformed(evt);
            }
        });
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

        javax.swing.GroupLayout ShadowSearch1Layout = new javax.swing.GroupLayout(ShadowSearch1);
        ShadowSearch1.setLayout(ShadowSearch1Layout);
        ShadowSearch1Layout.setHorizontalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_date, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        ShadowSearch1Layout.setVerticalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pilihtanggal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txt_date, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btn_detail_pemasukan.setBackground(new java.awt.Color(0, 204, 204));
        btn_detail_pemasukan.setForeground(new java.awt.Color(255, 255, 255));
        btn_detail_pemasukan.setText("Laporan Setor Sampah");
        btn_detail_pemasukan.setFont(btn_detail_pemasukan.getFont().deriveFont(btn_detail_pemasukan.getFont().getStyle() | java.awt.Font.BOLD));
        btn_detail_pemasukan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_detail_pemasukanActionPerformed(evt);
            }
        });

        btn_laporan_transaksi.setBackground(new java.awt.Color(0, 204, 204));
        btn_laporan_transaksi.setForeground(new java.awt.Color(255, 255, 255));
        btn_laporan_transaksi.setText("Laporan Transaksi");
        btn_laporan_transaksi.setFont(btn_laporan_transaksi.getFont().deriveFont(btn_laporan_transaksi.getFont().getStyle() | java.awt.Font.BOLD));
        btn_laporan_transaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_laporan_transaksiActionPerformed(evt);
            }
        });

        btn_laporan_jual_sampah.setBackground(new java.awt.Color(0, 204, 204));
        btn_laporan_jual_sampah.setForeground(new java.awt.Color(255, 255, 255));
        btn_laporan_jual_sampah.setText("Laporan Jual Sampah");
        btn_laporan_jual_sampah.setFont(btn_laporan_jual_sampah.getFont().deriveFont(btn_laporan_jual_sampah.getFont().getStyle() | java.awt.Font.BOLD));
        btn_laporan_jual_sampah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_laporan_jual_sampahActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Halaman Laporan");

        chart.setBackground(new java.awt.Color(0, 0, 0));
        chart.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chart.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout panelCurve1Layout = new javax.swing.GroupLayout(panelCurve1);
        panelCurve1.setLayout(panelCurve1Layout);
        panelCurve1Layout.setHorizontalGroup(
            panelCurve1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelCurve1Layout.setVerticalGroup(
            panelCurve1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurve1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(chart, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        btn_laporan_saldo.setBackground(new java.awt.Color(0, 204, 204));
        btn_laporan_saldo.setForeground(new java.awt.Color(255, 255, 255));
        btn_laporan_saldo.setText("Laporan Saldo");
        btn_laporan_saldo.setFont(btn_laporan_saldo.getFont().deriveFont(btn_laporan_saldo.getFont().getStyle() | java.awt.Font.BOLD));
        btn_laporan_saldo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_laporan_saldoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCurveLayout = new javax.swing.GroupLayout(panelCurve);
        panelCurve.setLayout(panelCurveLayout);
        panelCurveLayout.setHorizontalGroup(
            panelCurveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurveLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelCurveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_laporan_jual_sampah, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_detail_pemasukan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addComponent(btn_laporan_transaksi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_laporan_saldo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addComponent(panelCurve1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelCurveLayout.setVerticalGroup(
            panelCurveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurveLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_laporan_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_laporan_jual_sampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_detail_pemasukan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_laporan_saldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelCurve1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel14.setFont(jLabel14.getFont().deriveFont(jLabel14.getFont().getStyle() | java.awt.Font.BOLD, jLabel14.getFont().getSize()+10));
        jLabel14.setText("Riwayat");
        jLabel14.setPreferredSize(new java.awt.Dimension(145, 30));

        javax.swing.GroupLayout panelTableLayout = new javax.swing.GroupLayout(panelTable);
        panelTable.setLayout(panelTableLayout);
        panelTableLayout.setHorizontalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTableLayout.createSequentialGroup()
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBawah1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTableLayout.createSequentialGroup()
                        .addComponent(ShadowSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ShadowSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCurve, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelTableLayout.setVerticalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTableLayout.createSequentialGroup()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTableLayout.createSequentialGroup()
                        .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ShadowSearch1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(ShadowSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(btnReset, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                    .addComponent(panelCurve, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBawah1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panelCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(panelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void cardPemasukanMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPemasukanMouseClicked
        loadData("Pemasukan");
        Notifications.getInstance().show(Notifications.Type.INFO, "Menampilkan transaksi pemasukan");
    }// GEN-LAST:event_cardPemasukanMouseClicked

    private void cardPemasukanMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPemasukanMouseEntered
        cardPemasukan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardPemasukan.setFillColor(new Color(178, 242, 233)); // Warna hover (lebih terang)
        cardPemasukan.repaint(); // Pastikan komponen direfresh
    }// GEN-LAST:event_cardPemasukanMouseEntered

    private void cardPemasukanMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPemasukanMouseExited
        cardPemasukan.setCursor(Cursor.getDefaultCursor());
        cardPemasukan.setFillColor(new Color(214, 255, 247)); // Warna normal
        cardPemasukan.repaint();
    }// GEN-LAST:event_cardPemasukanMouseExited

    private void cardPemasukanMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPemasukanMousePressed
        animateClick(new Color(137, 227, 214), new Color(214, 255, 247));

        loadData("Pemasukan");
    }// GEN-LAST:event_cardPemasukanMousePressed

    private void cardPemasukanMouseReleased(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPemasukanMouseReleased
        animateClick(new Color(214, 255, 247), new Color(137, 227, 214));
    }// GEN-LAST:event_cardPemasukanMouseReleased

    private void cardPengeluaranMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPengeluaranMouseClicked
        loadData("Pengeluaran");
        Notifications.getInstance().show(Notifications.Type.INFO, "Menampilkan transaksi pengeluaran");
    }// GEN-LAST:event_cardPengeluaranMouseClicked

    private void cardPengeluaranMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPengeluaranMouseEntered
        cardPengeluaran.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardPengeluaran.setFillColor(new Color(255, 222, 228)); // Warna hover (lebih terang)
        cardPengeluaran.repaint();
    }// GEN-LAST:event_cardPengeluaranMouseEntered

    private void cardPengeluaranMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPengeluaranMouseExited
        cardPengeluaran.setCursor(Cursor.getDefaultCursor());
        cardPengeluaran.setFillColor(new Color(255, 236, 238)); // Warna normal (hijau)
        cardPengeluaran.repaint();
    }// GEN-LAST:event_cardPengeluaranMouseExited

    private void cardPengeluaranMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPengeluaranMousePressed

        animateClickCard2(new Color(255, 200, 210), new Color(243, 180, 195));
        loadData("Pengeluaran"); // atau aksi lain sesuai card2
    }// GEN-LAST:event_cardPengeluaranMousePressed

    private void cardPengeluaranMouseReleased(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardPengeluaranMouseReleased
        animateClickCard2(new Color(243, 180, 195), new Color(255, 200, 210));
    }// GEN-LAST:event_cardPengeluaranMouseReleased

    private void cardTransaksiMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardTransaksiMouseClicked
        // Reset any filters that might be applied
        txt_search.setText("");
        txt_date.setText("");
        box_pilih.setSelectedItem("Default");
        halamanSaatIni = 1;

        // Load data with empty filter to show all transactions
        loadData("");

        // Provide user feedback
        Notifications.getInstance().show(Notifications.Type.INFO, "Menampilkan semua transaksi");
    }// GEN-LAST:event_cardTransaksiMouseClicked

    private void cardTransaksiMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardTransaksiMouseEntered
        cardTransaksi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardTransaksi.setFillColor(new Color(250, 234, 180)); // Warna hover (lebih terang)
        cardTransaksi.repaint();
    }// GEN-LAST:event_cardTransaksiMouseEntered

    private void cardTransaksiMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardTransaksiMouseExited
        cardTransaksi.setCursor(Cursor.getDefaultCursor());
        cardTransaksi.setFillColor(new Color(254, 244, 208)); // Normal: kuning utama
        cardTransaksi.repaint();

    }// GEN-LAST:event_cardTransaksiMouseExited

    private void cardTransaksiMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardTransaksiMousePressed
        animateClickCard3(new Color(240, 215, 130), new Color(224, 198, 105));
        loadData("");
    }// GEN-LAST:event_cardTransaksiMousePressed

    private void cardTransaksiMouseReleased(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cardTransaksiMouseReleased
        animateClickCard3(new Color(224, 198, 105), new Color(240, 215, 130));
    }// GEN-LAST:event_cardTransaksiMouseReleased

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }// GEN-LAST:event_txt_searchKeyTyped

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_box_pilihActionPerformed
        searchByKeywordAndDate();
    }// GEN-LAST:event_box_pilihActionPerformed

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_pilihtanggalActionPerformed
        dateChooser1.showPopup();
    }// GEN-LAST:event_pilihtanggalActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnResetActionPerformed
        txt_date.setText("");
    }// GEN-LAST:event_btnResetActionPerformed

    private void btn_detail_pemasukanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_detail_pemasukanActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_setor_sampah(users));
        panelMain.repaint();
        panelMain.revalidate();
        notification.toast.Notifications.getInstance().show(Notifications.Type.INFO, "Beralih Halaman Transaksi");
    }// GEN-LAST:event_btn_detail_pemasukanActionPerformed

    private void btn_laporan_transaksiActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_laporan_transaksiActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_transaksi(users));
        panelMain.repaint();
        panelMain.revalidate();
        notification.toast.Notifications.getInstance().show(Notifications.Type.INFO, "Beralih Halaman Transaksi");
    }// GEN-LAST:event_btn_laporan_transaksiActionPerformed

    private void btn_laporan_jual_sampahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_laporan_jual_sampahActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_jual_sampah(users));
        panelMain.repaint();
        panelMain.revalidate();
        notification.toast.Notifications.getInstance().show(Notifications.Type.INFO, "Beralih Halaman Jual Sampah");
    }// GEN-LAST:event_btn_laporan_jual_sampahActionPerformed

    private void btn_ExportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_Export1ActionPerformed
        try {
            // Create export model with appropriate columns
            DefaultTableModel exportModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Use the same column headers as the table
            exportModel.setColumnIdentifiers(new String[] {
                    "No", "Nama Admin", "Nama Nasabah", "Transaksi", "Detail", "Nominal", "Status", "Waktu"
            });

            // Get current filter values
            String kataKunci = txt_search.getText().trim();
            String tanggalRange = txt_date.getText().trim();
            String filterType = box_pilih.getSelectedItem().toString();

            // Parse date range if present
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

            // Build SQL query with filters
            try (Connection conn = DBconnect.getConnection()) {
                StringBuilder sql = new StringBuilder();
                sql.append("""
                        SELECT
                            nama_admin,
                            nama_nasabah,
                            jenis_transaksi,
                            detail,
                            nominal,
                            status,
                            waktu_transaksi
                        FROM (
                            -- Transaksi penjualan barang (Pemasukan)
                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                'Penjualan Barang' AS jenis_transaksi,
                                tr.nama_barang AS detail,
                                tr.total_harga AS nominal,
                                'Pemasukan' AS status,
                                tr.tanggal AS waktu_transaksi
                            FROM laporan_pemasukan lp
                            LEFT JOIN login u ON lp.id_user = u.id_user
                            LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                            LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                            WHERE lp.id_transaksi IS NOT NULL

                            UNION ALL

                            -- Penjualan sampah (Pemasukan)
                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                '-' AS nama_nasabah,
                                'Penjualan Sampah' AS jenis_transaksi,
                                kate.nama_kategori AS detail,
                                js.harga AS nominal,
                                'Pemasukan' AS status,
                                js.tanggal AS waktu_transaksi
                            FROM laporan_pemasukan lp
                            LEFT JOIN login u ON lp.id_user = u.id_user
                            LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                            JOIN sampah sa ON js.id_sampah = sa.id_sampah
                            JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                            WHERE lp.id_jual_sampah IS NOT NULL

                            UNION ALL

                            -- Setoran sampah (Pengeluaran)
                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                'Setoran Sampah' AS jenis_transaksi,
                                kate.nama_kategori AS detail,
                                s.harga AS nominal,
                                'Pengeluaran' AS status,
                                s.tanggal AS waktu_transaksi
                            FROM laporan_pengeluaran lpl
                            LEFT JOIN login u ON lpl.id_user = u.id_user
                            JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                            LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                            JOIN sampah sa ON s.id_sampah = sa.id_sampah
                            JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                            WHERE lpl.id_setoran IS NOT NULL

                            UNION ALL

                            -- Penarikan saldo (Pengeluaran dari perspektif bank)
                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                'Penarikan Saldo' AS jenis_transaksi,
                                'Tarik Tunai' AS detail,
                                ps.jumlah_penarikan AS nominal,
                                'Pengeluaran' AS status,
                                ps.tanggal_penarikan AS waktu_transaksi
                            FROM penarikan_saldo ps
                            LEFT JOIN login u ON ps.id_user = u.id_user
                            LEFT JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                        ) AS combined_data
                        """);

                boolean whereAdded = false;

                // Apply keyword filter
                if (!kataKunci.isEmpty()) {
                    switch (filterType) {
                        case "Default":
                            sql.append("WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ? OR detail LIKE ?) ");
                            whereAdded = true;
                            break;
                        case "Nama Admin":
                            sql.append("WHERE (nama_admin LIKE ?) ");
                            whereAdded = true;
                            break;
                        case "Nama Nasabah":
                            sql.append("WHERE (nama_nasabah LIKE ?) ");
                            whereAdded = true;
                            break;
                        case "Nama Barang/Sampah":
                            sql.append("WHERE (detail LIKE ?) ");
                            whereAdded = true;
                            break;
                    }
                }

                // Apply date filter
                if (isRange) {
                    sql.append(whereAdded ? "AND " : "WHERE ");
                    // Use the same approach as in searchByKeywordAndDate
                    sql.append("DATE(waktu_transaksi) BETWEEN ? AND ? ");
                    whereAdded = true;
                } else if (isSingleDate) {
                    System.out.println("Export filtering by date: " + tanggalMulai); // Debug log
                    sql.append(whereAdded ? "AND " : "WHERE ");
                    // Use the same approach as in searchByKeywordAndDate
                    sql.append("DATE(waktu_transaksi) = ? ");
                    whereAdded = true;
                }

                // Always sort by datetime in descending order (newest first)
                sql.append(" ORDER BY waktu_transaksi DESC");

                try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
                    int paramIndex = 1;

                    // Set search parameters
                    if (!kataKunci.isEmpty()) {
                        String searchPattern = "%" + kataKunci + "%";
                        switch (filterType) {
                            case "Default":
                                pst.setString(paramIndex++, searchPattern);
                                pst.setString(paramIndex++, searchPattern);
                                pst.setString(paramIndex++, searchPattern);
                                break;
                            default:
                                pst.setString(paramIndex++, searchPattern);
                                break;
                        }
                    }

                    // Set date parameters
                    if (isRange) {
                        pst.setString(paramIndex++, tanggalMulai);
                        pst.setString(paramIndex++, tanggalAkhir);
                    } else if (isSingleDate) {
                        pst.setString(paramIndex++, tanggalMulai);
                    }

                    try (ResultSet rs = pst.executeQuery()) {
                        int no = 1;
                        while (rs.next()) {
                            String nominal = rs.getString("nominal");
                            if (nominal != null && !nominal.equals("-")) {
                                try {
                                    double amount = Double.parseDouble(nominal);
                                    NumberFormat formatRupiah = NumberFormat
                                            .getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                                    formatRupiah.setMaximumFractionDigits(0);
                                    formatRupiah.setMinimumFractionDigits(0);
                                    nominal = formatRupiah.format(amount);
                                } catch (NumberFormatException e) {
                                    // Keep original value if formatting fails
                                }
                            }

                            exportModel.addRow(new Object[] {
                                    no++,
                                    rs.getString("nama_admin"),
                                    rs.getString("nama_nasabah"),
                                    rs.getString("jenis_transaksi"),
                                    rs.getString("detail"),
                                    nominal,
                                    rs.getString("status"),
                                    rs.getString("waktu_transaksi")
                            });
                        }
                    }
                }
            }

            // Check if no data to export
            if (exportModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Choose save location
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file Excel");
            chooser.setSelectedFile(new File("laporan_transaksi.xls")); // Default name

            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls")
                            || f.getName().toLowerCase().endsWith(".xlsx");
                }

                @Override
                public String getDescription() {
                    return "Excel Files (*.xls, *.xlsx)";
                }
            });

            int option = chooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File fileToSave = chooser.getSelectedFile();

                // Add extension if not present
                String fileName = fileToSave.getName().toLowerCase();
                if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
                }

                // Confirm overwrite if file exists
                if (fileToSave.exists()) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "File sudah ada. Apakah Anda ingin menimpanya?",
                            "Konfirmasi",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Export the data
                try {
                    ExcelExporter.exportTableModelToExcel(exportModel, fileToSave);

                    // Log export activity
                    String exportDetails = "Laporan Statistik";
                    if (!kataKunci.isEmpty()) {
                        exportDetails += " dengan filter kata kunci: " + kataKunci;
                    }
                    if (isSingleDate || isRange) {
                        exportDetails += " untuk tanggal: " + txt_date.getText();
                    }
                    LoggerUtil.insert(users.getId(), "Export " + exportDetails);

                    JOptionPane.showMessageDialog(this,
                            "Export berhasil!\nFile disimpan di: " + fileToSave.getAbsolutePath(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, "Export berhasil");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mengekspor file: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    Notifications.getInstance().show(Notifications.Type.WARNING, "Gagal mengekspor file");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            Notifications.getInstance().show(Notifications.Type.ERROR, "Terjadi kesalahan");
            e.printStackTrace();
        }
    }// GEN-LAST:event_btn_Export1ActionPerformed

    private void getFilteredDataForExport(DefaultTableModel model) {
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

        try (Connection conn = DBconnect.getConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                    SELECT
                        nama_admin,
                        nama_nasabah,
                        jenis_transaksi,
                        detail,
                        nominal,
                        status,
                        waktu_transaksi
                    FROM (
                        -- Transaksi penjualan barang (Pemasukan)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Penjualan Barang' AS jenis_transaksi,
                            tr.nama_barang AS detail,
                            tr.total_harga AS nominal,
                            'Pemasukan' AS status,
                            tr.tanggal AS waktu_transaksi
                        FROM laporan_pemasukan lp
                        LEFT JOIN login u ON lp.id_user = u.id_user
                        LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                        LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                        WHERE lp.id_transaksi IS NOT NULL

                        UNION ALL

                        -- Penjualan sampah (Pemasukan)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            '-' AS nama_nasabah,
                            'Penjualan Sampah' AS jenis_transaksi,
                            kate.nama_kategori AS detail,
                            js.harga AS nominal,
                            'Pemasukan' AS status,
                            js.tanggal AS waktu_transaksi
                        FROM laporan_pemasukan lp
                        LEFT JOIN login u ON lp.id_user = u.id_user
                        LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                        JOIN sampah sa ON js.id_sampah = sa.id_sampah
                        JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                        WHERE lp.id_jual_sampah IS NOT NULL

                        UNION ALL

                        -- Setoran sampah (Pengeluaran)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Setoran Sampah' AS jenis_transaksi,
                            kate.nama_kategori AS detail,
                            s.harga AS nominal,
                            'Pengeluaran' AS status,
                            s.tanggal AS waktu_transaksi
                        FROM laporan_pengeluaran lpl
                        LEFT JOIN login u ON lpl.id_user = u.id_user
                        JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                        LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                        JOIN sampah sa ON s.id_sampah = sa.id_sampah
                        JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                        WHERE lpl.id_setoran IS NOT NULL

                        UNION ALL

                        -- Penarikan saldo (Pengeluaran dari perspektif bank)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Penarikan Saldo' AS jenis_transaksi,
                            'Tarik Tunai' AS detail,
                            ps.jumlah_penarikan AS nominal,
                            'Pengeluaran' AS status,
                            ps.tanggal_penarikan AS waktu_transaksi
                        FROM penarikan_saldo ps
                        LEFT JOIN login u ON ps.id_user = u.id_user
                        LEFT JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                    ) AS combined_data
                    """);

            boolean whereAdded = false;

            // Apply filters
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append("WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ? OR detail LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        sql.append("WHERE (nama_admin LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Nasabah":
                        sql.append("WHERE (nama_nasabah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Barang/Sampah":
                        sql.append("WHERE (detail LIKE ?) ");
                        whereAdded = true;
                        break;
                }
            }

            // Add date filter
            if (isRange) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(waktu_transaksi) BETWEEN STR_TO_DATE(?, '%d-%m-%Y') AND STR_TO_DATE(?, '%d-%m-%Y') ");
                whereAdded = true;
            } else if (isSingleDate) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(waktu_transaksi) = STR_TO_DATE(?, '%d-%m-%Y') ");
                whereAdded = true;
            }

            // Always sort by datetime in descending order (newest first)
            sql.append(" ORDER BY waktu_transaksi DESC");

            try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;

                // Set search parameters
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pst.setString(paramIndex++, searchPattern);
                            pst.setString(paramIndex++, searchPattern);
                            pst.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pst.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set date parameters
                if (isRange) {
                    pst.setString(paramIndex++, tanggalMulai);
                    pst.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pst.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rs = pst.executeQuery()) {
                    int no = 1;
                    while (rs.next()) {
                        String nominal = rs.getString("nominal");
                        if (nominal != null && !nominal.equals("-")) {
                            try {
                                double amount = Double.parseDouble(nominal);
                                NumberFormat formatRupiah = NumberFormat
                                        .getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                                formatRupiah.setMaximumFractionDigits(0);
                                formatRupiah.setMinimumFractionDigits(0);
                                nominal = formatRupiah.format(amount);
                            } catch (NumberFormatException e) {
                                // Keep original value if formatting fails
                            }
                        }

                        model.addRow(new Object[] {
                                no++,
                                rs.getString("nama_admin"),
                                rs.getString("nama_nasabah"),
                                rs.getString("jenis_transaksi"),
                                rs.getString("detail"),
                                nominal,
                                rs.getString("status"),
                                rs.getString("waktu_transaksi")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(TabLaporanStatistik.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error mengambil data untuk export: " + e.getMessage());
        }
    }

    private void txt_dateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_dateActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_dateActionPerformed

    private void txt_datePropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_txt_datePropertyChange
        searchByKeywordAndDate(); // TODO add your handling code here:
    }// GEN-LAST:event_txt_datePropertyChange

    private void txt_dateKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_dateKeyTyped
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_dateKeyTyped

    private void btn_laporan_saldoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_laporan_saldoActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_tarik_saldo(users));
        panelMain.repaint();
        panelMain.revalidate();
        notification.toast.Notifications.getInstance().show(Notifications.Type.INFO, "Beralih Halaman Saldo");
    }// GEN-LAST:event_btn_laporan_saldoActionPerformed

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

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Use the SAME column structure as in loadData() method
        model.setColumnIdentifiers(new String[] {
                "No", "Nama Admin", "Nama Nasabah", "Transaksi", "Detail", "Nominal", "Status", "Waktu"
        });

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                    SELECT
                        nama_admin,
                        nama_nasabah,
                        jenis_transaksi,
                        detail,
                        nominal,
                        status,
                        waktu_transaksi
                    FROM (
                        -- Transaksi penjualan barang (Pemasukan)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Penjualan Barang' AS jenis_transaksi,
                            tr.nama_barang AS detail,
                            tr.total_harga AS nominal,
                            'Pemasukan' AS status,
                            tr.tanggal AS waktu_transaksi
                        FROM laporan_pemasukan lp
                        LEFT JOIN login u ON lp.id_user = u.id_user
                        LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                        LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                        WHERE lp.id_transaksi IS NOT NULL

                        UNION ALL

                        -- Penjualan sampah (Pemasukan)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            '-' AS nama_nasabah,
                            'Penjualan Sampah' AS jenis_transaksi,
                            kate.nama_kategori AS detail,
                            js.harga AS nominal,
                            'Pemasukan' AS status,
                            js.tanggal AS waktu_transaksi
                        FROM laporan_pemasukan lp
                        LEFT JOIN login u ON lp.id_user = u.id_user
                        LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                        JOIN sampah sa ON js.id_sampah = sa.id_sampah
                        JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                        WHERE lp.id_jual_sampah IS NOT NULL

                        UNION ALL

                        -- Setoran sampah (Pengeluaran)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Setoran Sampah' AS jenis_transaksi,
                            kate.nama_kategori AS detail,
                            s.harga AS nominal,
                            'Pengeluaran' AS status,
                            s.tanggal AS waktu_transaksi
                        FROM laporan_pengeluaran lpl
                        LEFT JOIN login u ON lpl.id_user = u.id_user
                        JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                        LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                        JOIN sampah sa ON s.id_sampah = sa.id_sampah
                        JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                        WHERE lpl.id_setoran IS NOT NULL

                        UNION ALL

                        -- Penarikan saldo (Pengeluaran dari perspektif bank)
                        SELECT
                            COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                            COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                            'Penarikan Saldo' AS jenis_transaksi,
                            'Tarik Tunai' AS detail,
                            ps.jumlah_penarikan AS nominal,
                            'Pengeluaran' AS status,
                            ps.tanggal_penarikan AS waktu_transaksi
                        FROM penarikan_saldo ps
                        LEFT JOIN login u ON ps.id_user = u.id_user
                        LEFT JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                    ) AS combined_data
                    """);

            boolean whereAdded = false;

            // Filter berdasarkan kata kunci
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append(" WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ? OR detail LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        sql.append(" WHERE nama_admin LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Nasabah":
                        sql.append(" WHERE nama_nasabah LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Barang/Sampah":
                        sql.append(" WHERE detail LIKE ? ");
                        whereAdded = true;
                        break;
                }
            }

            // Filter berdasarkan tanggal - Key fix here!
            if (isRange) {
                sql.append(whereAdded ? " AND " : " WHERE ");
                // Use direct date comparison without STR_TO_DATE function
                sql.append("DATE(waktu_transaksi) BETWEEN ? AND ? ");
                whereAdded = true;
            } else if (isSingleDate) {
                System.out.println("Filtering by date: " + tanggalMulai);
                sql.append(whereAdded ? " AND " : " WHERE ");
                // Use direct date comparison without STR_TO_DATE function
                sql.append("DATE(waktu_transaksi) = ? ");
                whereAdded = true;
            }

            sql.append(" ORDER BY waktu_transaksi DESC LIMIT ? OFFSET ?");

            try (Connection conn = DBconnect.getConnection();
                    PreparedStatement st = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            st.setString(paramIndex++, searchPattern);
                            st.setString(paramIndex++, searchPattern);
                            st.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            st.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal
                if (isRange) {
                    st.setString(paramIndex++, tanggalMulai);
                    st.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    st.setString(paramIndex++, tanggalMulai);
                }

                // Add pagination parameters
                int startIndex = (halamanSaatIni - 1) * dataPerHalaman;
                st.setInt(paramIndex++, dataPerHalaman);
                st.setInt(paramIndex, startIndex);

                try (ResultSet rs = st.executeQuery()) {
                    int no = startIndex + 1;
                    while (rs.next()) {
                        String nominal = rs.getString("nominal");
                        if (nominal != null && !nominal.equals("-")) {
                            try {
                                double amount = Double.parseDouble(nominal);
                                NumberFormat formatRupiah = NumberFormat
                                        .getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                                formatRupiah.setMaximumFractionDigits(0);
                                formatRupiah.setMinimumFractionDigits(0);
                                nominal = formatRupiah.format(amount);
                            } catch (NumberFormatException e) {
                                // Keep original value if formatting fails
                            }
                        }

                        model.addRow(new Object[] {
                                no++,
                                rs.getString("nama_admin"),
                                rs.getString("nama_nasabah"),
                                rs.getString("jenis_transaksi"),
                                rs.getString("detail"),
                                nominal,
                                rs.getString("status"),
                                rs.getString("waktu_transaksi")
                        });
                    }
                }
                tb_laporan.setModel(model);
                calculateTotalPage();
                updatePaginationInfo();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DefaultTableModel getFilteredTableModel() {
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

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Use the SAME column structure as in loadData() method
        model.setColumnIdentifiers(new String[] {
                "No", "Nama Admin", "Nama Nasabah", "Transaksi", "Detail", "Nominal", "Status", "Waktu"
        });

        try (Connection conn = DBconnect.getConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                       SELECT
                                    nama_admin,
                                    nama_nasabah,
                                    nama_barang_sampah,
                                    jenis_transaksi,
                                    harga,
                                    riwayat
                                FROM (
                                    SELECT
                                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                        COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                        tr.nama_barang AS nama_barang_sampah,
                                        'Pemasukan' AS jenis_transaksi,
                                        tr.harga AS harga,
                                        lp.riwayat AS riwayat
                                    FROM laporan_pemasukan lp
                                    LEFT JOIN login u ON lp.id_user = u.id_user
                                    LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                                    LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                                    WHERE lp.id_transaksi IS NOT NULL

                                    UNION ALL

                                    SELECT
                                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                        '-' AS nama_nasabah,
                                        kate.nama_kategori AS nama_barang_sampah,
                                        'Pemasukan' AS jenis_transaksi,
                                        js.harga AS harga,
                                        lp.riwayat AS riwayat
                                    FROM laporan_pemasukan lp
                                    LEFT JOIN login u ON lp.id_user = u.id_user
                                    LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                                    JOIN sampah sa ON js.id_sampah = sa.id_sampah
                                    JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                                    WHERE lp.id_jual_sampah IS NOT NULL

                                    UNION ALL

                                    SELECT
                                        COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                        COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                        kate.nama_kategori AS nama_barang_sampah,
                                        'Pengeluaran' AS jenis_transaksi,
                                        s.harga AS harga,
                                        lpl.riwayat AS riwayat
                                    FROM laporan_pengeluaran lpl
                                    LEFT JOIN login u ON lpl.id_user = u.id_user
                                    JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                                    LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                                    JOIN sampah sa ON s.id_sampah = sa.id_sampah
                                    JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                                    WHERE lpl.id_setoran IS NOT NULL
                                ) AS combined
                    """);

            boolean whereAdded = false;

            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append("WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ? OR nama_barang_sampah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        sql.append("WHERE (nama_admin LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Nasabah":
                        sql.append("WHERE (nama_nasabah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Barang/Sampah":
                        sql.append("WHERE (nama_barang_sampah LIKE ?) ");
                        whereAdded = true;
                        break;
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

            try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            st.setString(paramIndex++, searchPattern);
                            st.setString(paramIndex++, searchPattern);
                            st.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            st.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                if (isRange) {
                    st.setString(paramIndex++, tanggalMulai);
                    st.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    st.setString(paramIndex++, tanggalMulai);
                }

                ResultSet rs = st.executeQuery();
                int no = 1;
                while (rs.next()) {
                    String harga = rs.getString("harga");
                    if (harga != null && !harga.equals("-")) {
                        try {
                            double nominal = Double.parseDouble(harga);
                            DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
                            harga = formatRupiah.format(nominal);
                        } catch (NumberFormatException e) {
                            // abaikan
                        }
                    }

                    Object[] rowData = {
                            no++,
                            rs.getString("nama_admin"),
                            rs.getString("nama_nasabah"),
                            rs.getString("nama_barang_sampah"),
                            harga,
                            rs.getString("jenis_transaksi"),
                            rs.getString("riwayat")
                    };
                    model.addRow(rowData);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    private int getTotalData() {
        int totalData = 0;
        try {
            String countQuery = """
                        SELECT COUNT(*) as total FROM (
                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                tr.nama_barang AS nama_barang_sampah,
                                'Pemasukan' AS jenis_transaksi,
                                tr.harga AS harga,
                                lp.riwayat AS riwayat
                            FROM laporan_pemasukan lp
                            LEFT JOIN login u ON lp.id_user = u.id_user
                            LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                            LEFT JOIN manajemen_nasabah n ON lp.id_nasabah = n.id_nasabah
                            WHERE lp.id_transaksi IS NOT NULL

                            UNION ALL

                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                '-' AS nama_nasabah,
                                kate.nama_kategori AS nama_barang_sampah,
                                'Pemasukan' AS jenis_transaksi,
                                js.harga AS harga,
                                lp.riwayat AS riwayat
                            FROM laporan_pemasukan lp
                            LEFT JOIN login u ON lp.id_user = u.id_user
                            LEFT JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                            JOIN sampah sa ON js.id_sampah = sa.id_sampah
                            JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                            WHERE lp.id_jual_sampah IS NOT NULL

                            UNION ALL

                            SELECT
                                COALESCE(u.nama_user, '[user dihapus]') AS nama_admin,
                                COALESCE(n.nama_nasabah, '[nasabah dihapus]') AS nama_nasabah,
                                kate.nama_kategori AS nama_barang_sampah,
                                'Pengeluaran' AS jenis_transaksi,
                                s.harga AS harga,
                                lpl.riwayat AS riwayat
                            FROM laporan_pengeluaran lpl
                            LEFT JOIN login u ON lpl.id_user = u.id_user
                            JOIN setor_sampah s ON lpl.id_setoran = s.id_setoran
                            LEFT JOIN manajemen_nasabah n ON s.id_nasabah = n.id_nasabah
                            JOIN sampah sa ON s.id_sampah = sa.id_sampah
                            JOIN kategori_sampah kate ON sa.id_kategori = kate.id_kategori
                            WHERE lpl.id_setoran IS NOT NULL
                        ) AS combined
                    """;

            try (Connection conn = DBconnect.getConnection();
                    PreparedStatement pst = conn.prepareStatement(countQuery);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalData = rs.getInt("total");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghitung total data: " + e.getMessage());
        }
        return totalData;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private javax.swing.JComboBox<String> box_pilih;
    private javax.swing.JButton btnReset;
    private component.Jbutton btn_Export;
    private javax.swing.JButton btn_before1;
    private ripple.button.Button btn_detail_pemasukan;
    private javax.swing.JButton btn_first1;
    private ripple.button.Button btn_laporan_jual_sampah;
    private ripple.button.Button btn_laporan_saldo;
    private ripple.button.Button btn_laporan_transaksi;
    private javax.swing.JButton btn_last1;
    private javax.swing.JButton btn_next1;
    private component.Card cardPemasukan;
    private component.Card cardPengeluaran;
    private component.Card cardTransaksi;
    private javax.swing.JComboBox<String> cbx_data1;
    private grafik.main.CurveLineChart chart;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_halaman1;
    private javax.swing.JLabel lb_pemasukan;
    private javax.swing.JLabel lb_pengeluaran;
    private javax.swing.JLabel lb_total;
    private component.ShadowPanel panelBawah1;
    private component.ShadowPanel panelCard;
    private component.ShadowPanel panelCurve;
    private component.ShadowPanel panelCurve1;
    private javax.swing.JPanel panelMain;
    private component.ShadowPanel panelTable;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.Table tb_laporan;
    private swing.TextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}
