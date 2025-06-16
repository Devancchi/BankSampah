package view;

import component.ExcelExporter;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import main.DBconnect;
import notification.toast.Notifications;

public class TabLaporan_jual_sampah extends javax.swing.JPanel {
    private final Connection conn = DBconnect.getConnection();
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 20;
    private int totalPages;
    private int totalData;

    public TabLaporan_jual_sampah() {
        initComponents();
        txt_date.setText("");
        loadData("");
        setupPagination();
    }

    private void setupPagination() {
        cbx_data2.addActionListener(e -> {
            dataPerHalaman = Integer.parseInt(cbx_data2.getSelectedItem().toString());
            halamanSaatIni = 1;
            loadData("");
        });

        btn_first2.addActionListener(e -> {
            halamanSaatIni = 1;
            loadData("");
        });

        btn_before2.addActionListener(e -> {
            if (halamanSaatIni > 1) {
                halamanSaatIni--;
                loadData("");
            }
        });

        btn_next2.addActionListener(e -> {
            if (halamanSaatIni < totalPages) {
                halamanSaatIni++;
                loadData("");
            }
        });

        btn_last2.addActionListener(e -> {
            halamanSaatIni = totalPages;
            loadData("");
        });
    }

    private void calculateTotalPage() {
        totalData = getTotalData();
        totalPages = (int) Math.ceil((double) totalData / dataPerHalaman);
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        lb_halaman2.setText(String.valueOf("Page " + halamanSaatIni + " Dari Total " + totalData + " Data"));
    }

    private int getTotalData() {
        try {
            String query = """
                SELECT COUNT(*) as total FROM (
                    SELECT 
                        u.nama_user AS nama_admin,
                        kate.nama_kategori AS nama_sampah,
                        js.berat_sampah AS berat_sampah,                
                        js.harga AS harga,
                        js.tanggal AS riwayat
                    FROM laporan_pemasukan lp
                    JOIN login u ON lp.id_user = u.id_user
                    JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                    JOIN sampah s ON js.id_sampah = s.id_sampah
                    JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                    WHERE lp.id_jual_sampah IS NOT NULL
                ) AS combined
            """;
            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Method untuk mengupdate label total pendapatan dan transaksi

    private void updatePemasukanLabels() {
        try (Connection conn = DBconnect.getConnection()) {
            // Query untuk total pendapatan (sum harga dari tabel jual_sampah)
            String queryPendapatan = "SELECT SUM(harga) as total_pendapatan FROM jual_sampah WHERE harga != '-'";

            // Query untuk total transaksi (count semua data dari tabel jual_sampah)
            String queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM jual_sampah";

            // Mengambil total pendapatan
            try (PreparedStatement pstPendapatan = conn.prepareStatement(queryPendapatan); ResultSet rsPendapatan = pstPendapatan.executeQuery()) {

                if (rsPendapatan.next()) {
                    double totalPendapatan = rsPendapatan.getDouble("total_pendapatan");

                    // Format ke rupiah
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                    String pendapatanFormatted = formatRupiah.format(totalPendapatan);

                    lbl_total_pendapatan.setText(pendapatanFormatted);
                }
            }

            // Mengambil total transaksi
            try (PreparedStatement pstTotalTransaksi = conn.prepareStatement(queryTotalTransaksi); ResultSet rsTotalTransaksi = pstTotalTransaksi.executeQuery()) {

                if (rsTotalTransaksi.next()) {
                    int totalTransaksi = rsTotalTransaksi.getInt("total_transaksi");
                    lbl_transaksi.setText(String.valueOf(totalTransaksi) + " Transaksi");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            // Set default values jika terjadi error
            lbl_total_pendapatan.setText("Rp 0");
            lbl_transaksi.setText("0 Transaksi");
        }
    }

// Method loadData yang sudah dimodifikasi untuk memanggil updatePemasukanLabels()
    private void loadData(String filterJenis) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{
            "No", "Nama Admin", "Nama Sampah", "Berat Sampah", "Harga", "Riwayat"
        });

        String baseQuery = """
            SELECT 
                nama_admin, 
                nama_sampah, 
                berat_sampah,
                harga, 
                riwayat
            FROM (
                SELECT 
                    u.nama_user AS nama_admin,
                    kate.nama_kategori AS nama_sampah,
                    js.berat_sampah AS berat_sampah,                
                    js.harga AS harga,
                    js.tanggal AS riwayat
                FROM laporan_pemasukan lp
                JOIN login u ON lp.id_user = u.id_user
                JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                JOIN sampah s ON js.id_sampah = s.id_sampah
                JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                WHERE lp.id_jual_sampah IS NOT NULL
            ) AS combined
        """;
        baseQuery += " ORDER BY riwayat DESC LIMIT ? OFFSET ?";

        try (PreparedStatement pst = conn.prepareStatement(baseQuery)) {
            pst.setInt(1, dataPerHalaman);
            pst.setInt(2, (halamanSaatIni - 1) * dataPerHalaman);

            try (ResultSet rs = pst.executeQuery()) {
                int no = (halamanSaatIni - 1) * dataPerHalaman + 1;
                while (rs.next()) {
                    String harga = rs.getString("harga");
                    if (!harga.equals("-")) {
                        try {
                            double nominal = Double.parseDouble(harga);
                            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                            harga = formatRupiah.format(nominal);
                        } catch (NumberFormatException e) {
                            // Biarkan harga tetap apa adanya jika gagal format
                        }
                    }
                    model.addRow(new Object[]{
                        no++,
                        rs.getString("nama_admin"),
                        rs.getString("nama_sampah"),
                        rs.getString("berat_sampah"),
                        harga,
                        rs.getString("riwayat")
                    });
                }
            }
            tb_laporan.setModel(model);
            calculateTotalPage();
            updatePemasukanLabels();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
        }
    }

// Method tambahan untuk update labels berdasarkan filter (jika diperlukan untuk search function)
    private void updatePemasukanLabelsWithFilter(String kataKunci, String filter, String tanggalMulai, String tanggalAkhir, boolean isRange, boolean isSingleDate) {
        try (Connection conn = DBconnect.getConnection()) {

            // Base query untuk mengambil data yang sudah difilter dari jual_sampah
            StringBuilder baseQuery = new StringBuilder();
            baseQuery.append("""
            SELECT harga FROM (
                SELECT 
                    u.nama_user AS nama_admin,
                    kate.nama_kategori AS nama_sampah,
                    js.harga AS harga,
                    js.tanggal AS riwayat
                FROM laporan_pemasukan lp
                JOIN login u ON lp.id_user = u.id_user
                JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                JOIN sampah s ON js.id_sampah = s.id_sampah
                JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                WHERE lp.id_jual_sampah IS NOT NULL
            ) AS combined
        """);

            boolean whereAdded = false;

            // Tambahkan filter kata kunci jika ada
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        baseQuery.append(" WHERE (nama_admin LIKE ? OR nama_sampah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        baseQuery.append(" WHERE nama_admin LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Sampah":
                        baseQuery.append(" WHERE nama_sampah LIKE ? ");
                        whereAdded = true;
                        break;
                }
            }

            // Tambahkan filter tanggal jika ada
            if (isRange) {
                baseQuery.append(whereAdded ? "AND " : "WHERE ");
                baseQuery.append("DATE(riwayat) BETWEEN ? AND ? ");
            } else if (isSingleDate) {
                baseQuery.append(whereAdded ? "AND " : "WHERE ");
                baseQuery.append("DATE(riwayat) = ? ");
            }

            // Query untuk total pendapatan
            String queryPendapatan = "SELECT SUM(CASE WHEN harga != '-' THEN CAST(harga AS DECIMAL(10,2)) ELSE 0 END) as total_pendapatan FROM (" + baseQuery.toString() + ") AS filtered_data";

            // Query untuk total transaksi
            String queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM (" + baseQuery.toString() + ") AS filtered_data";

            // Mengambil total pendapatan
            try (PreparedStatement pstPendapatan = conn.prepareStatement(queryPendapatan)) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pstPendapatan.setString(paramIndex++, searchPattern);
                            pstPendapatan.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pstPendapatan.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal
                if (isRange) {
                    pstPendapatan.setString(paramIndex++, tanggalMulai);
                    pstPendapatan.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstPendapatan.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsPendapatan = pstPendapatan.executeQuery()) {
                    if (rsPendapatan.next()) {
                        double totalPendapatan = rsPendapatan.getDouble("total_pendapatan");
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                        String pendapatanFormatted = formatRupiah.format(totalPendapatan);
                        lbl_total_pendapatan.setText(pendapatanFormatted);
                    }
                }
            }

            // Mengambil total transaksi
            try (PreparedStatement pstTotalTransaksi = conn.prepareStatement(queryTotalTransaksi)) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pstTotalTransaksi.setString(paramIndex++, searchPattern);
                            pstTotalTransaksi.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pstTotalTransaksi.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal
                if (isRange) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                    pstTotalTransaksi.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsTotalTransaksi = pstTotalTransaksi.executeQuery()) {
                    if (rsTotalTransaksi.next()) {
                        int totalTransaksi = rsTotalTransaksi.getInt("total_transaksi");
                        lbl_transaksi.setText(String.valueOf(totalTransaksi) + " Transaksi");
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            lbl_total_pendapatan.setText("Rp 0");
            lbl_transaksi.setText("0 Transaksi");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateChooser1 = new datechooser.Main.DateChooser();
        dateBetween1 = new datechooser.Main.DateBetween();
        defaultDateChooserRender1 = new datechooser.render.DefaultDateChooserRender();
        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        shadowFilter = new component.ShadowPanel();
        ShadowSearch = new component.ShadowPanel();
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txt_date = new swing.TextField();
        jButton1 = new javax.swing.JButton();
        btn_cancel = new component.Jbutton();
        shadowTable = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        panelGradient1 = new grafik.panel.PanelGradient();
        jLabel5 = new javax.swing.JLabel();
        lbl_total_pendapatan = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lbl_transaksi = new javax.swing.JLabel();
        panelBawah2 = new component.ShadowPanel();
        lb_halaman2 = new javax.swing.JLabel();
        btn_before2 = new javax.swing.JButton();
        cbx_data2 = new javax.swing.JComboBox<>();
        btn_next2 = new javax.swing.JButton();
        btn_last2 = new javax.swing.JButton();
        btn_first2 = new javax.swing.JButton();
        btn_Export2 = new component.Jbutton();

        dateChooser1.setDateChooserRender(defaultDateChooserRender1);
        dateChooser1.setDateSelectable(null);
        dateChooser1.setDateSelectionMode(datechooser.Main.DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateChooser1.setTextField(txt_date);

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(250, 250, 250));

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

        box_pilih.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nama Admin", "Nama Sampah" }));
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
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(box_pilih)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(txt_date, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                .addContainerGap())
        );
        ShadowSearch1Layout.setVerticalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ShadowSearch1Layout.createSequentialGroup()
                        .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txt_date, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btn_cancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btn_cancel.setText("Kembali");
        btn_cancel.setFillClick(new java.awt.Color(200, 125, 0));
        btn_cancel.setFillOriginal(new java.awt.Color(243, 156, 18));
        btn_cancel.setFillOver(new java.awt.Color(230, 145, 10));
        btn_cancel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowFilterLayout = new javax.swing.GroupLayout(shadowFilter);
        shadowFilter.setLayout(shadowFilterLayout);
        shadowFilterLayout.setHorizontalGroup(
            shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowFilterLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowFilterLayout.setVerticalGroup(
            shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowFilterLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(ShadowSearch1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                        .addComponent(ShadowSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
                    .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
        );

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Nama Admin", "Nama Sampah", "Berat Sampah", "Harga", "Riwayat"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tb_laporan);

        panelGradient1.setBackground(new java.awt.Color(0, 153, 153));
        panelGradient1.setColorGradient(new java.awt.Color(0, 204, 204));

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Total Pendapatan Jual Sampah");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        lbl_total_pendapatan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbl_total_pendapatan.setForeground(new java.awt.Color(255, 255, 255));
        lbl_total_pendapatan.setText("0");

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/total_transaksi.png"))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Total Transaksi");

        lbl_transaksi.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbl_transaksi.setForeground(new java.awt.Color(255, 255, 255));
        lbl_transaksi.setText("0");

        javax.swing.GroupLayout panelGradient1Layout = new javax.swing.GroupLayout(panelGradient1);
        panelGradient1.setLayout(panelGradient1Layout);
        panelGradient1Layout.setHorizontalGroup(
            panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                            .addComponent(lbl_total_pendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelGradient1Layout.setVerticalGroup(
            panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_total_pendapatan)
                .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        lb_halaman2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lb_halaman2.setText("hal");

        btn_before2.setText("<");

        cbx_data2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "20", "40", "60", "80" }));

        btn_next2.setText(">");

        btn_last2.setText("Last Page");

        btn_first2.setText("First Page");

        btn_Export2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_Export2.setText("Export To Excel");
        btn_Export2.setFillClick(new java.awt.Color(55, 130, 60));
        btn_Export2.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_Export2.setFillOver(new java.awt.Color(69, 160, 75));
        btn_Export2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_Export2.setRoundedCorner(40);
        btn_Export2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Export2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBawah2Layout = new javax.swing.GroupLayout(panelBawah2);
        panelBawah2.setLayout(panelBawah2Layout);
        panelBawah2Layout.setHorizontalGroup(
            panelBawah2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah2Layout.createSequentialGroup()
                .addComponent(btn_Export2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_halaman2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_first2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_before2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbx_data2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_next2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_last2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        panelBawah2Layout.setVerticalGroup(
            panelBawah2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBawah2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Export2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb_halaman2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_first2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_before2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbx_data2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_next2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_last2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout shadowTableLayout = new javax.swing.GroupLayout(shadowTable);
        shadowTable.setLayout(shadowTableLayout);
        shadowTableLayout.setHorizontalGroup(
            shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTableLayout.createSequentialGroup()
                .addGroup(shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
                    .addComponent(panelBawah2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowTableLayout.setVerticalGroup(
            shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 753, Short.MAX_VALUE)
                    .addGroup(shadowTableLayout.createSequentialGroup()
                        .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBawah2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shadowTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(shadowTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txt_date.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pilihtanggalActionPerformed
        dateChooser1.showPopup();
    }//GEN-LAST:event_pilihtanggalActionPerformed

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_pilihActionPerformed
        searchByKeywordAndDate();
    }//GEN-LAST:event_box_pilihActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_searchKeyTyped

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cancelActionPerformed
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void btn_Export2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Export2ActionPerformed
        try {
            loadData("");

            DefaultTableModel model = (DefaultTableModel) tb_laporan.getModel();

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file Excel");
            chooser.setSelectedFile(new File("data_export.xls")); // Default filename

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

                // Ensure proper file extension
                String fileName = fileToSave.getName().toLowerCase();
                if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
                }

                // Check if file already exists
                if (fileToSave.exists()) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "File sudah ada. Apakah Anda ingin menimpanya?",
                            "Konfirmasi",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Export to Excel
                try {
                    ExcelExporter.exportTableModelToExcel(model, fileToSave);

                    // Show success message
                    JOptionPane.showMessageDialog(this,
                            "Export berhasil!\nFile disimpan di: " + fileToSave.getAbsolutePath(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mengekspor file: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btn_Export2ActionPerformed

    private void txt_dateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_dateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_dateActionPerformed

    private void txt_datePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txt_datePropertyChange
        searchByKeywordAndDate();        // TODO add your handling code here:
    }//GEN-LAST:event_txt_datePropertyChange

    private void txt_dateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_dateKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_dateKeyTyped

   private void searchByKeywordAndDate() {
        String kataKunci = txt_search.getText().trim();
        String tanggalRange = txt_date.getText().trim();
        String filter = box_pilih.getSelectedItem().toString();
        
        String tanggalMulai = "";
        String tanggalAkhir = "";
        boolean isRange = false;
        boolean isSingleDate = false;

        // Handle input tanggal manual (jika ada)
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

        model.setColumnIdentifiers(new String[]{
            "No", "Nama Admin", "Nama Sampah", "Berat Sampah", "Harga", "Riwayat"
        });

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                SELECT 
                    nama_admin, 
                    nama_sampah, 
                    berat_sampah,
                    harga, 
                    riwayat
                FROM (
                    SELECT 
                        u.nama_user AS nama_admin,
                        kate.nama_kategori AS nama_sampah,
                        js.berat_sampah AS berat_sampah,                
                        js.harga AS harga,
                        js.tanggal AS riwayat
                    FROM laporan_pemasukan lp
                    JOIN login u ON lp.id_user = u.id_user
                    JOIN jual_sampah js ON lp.id_jual_sampah = js.id_jual_sampah
                    JOIN sampah s ON js.id_sampah = s.id_sampah
                    JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                    WHERE lp.id_jual_sampah IS NOT NULL
                ) AS combined
            """);

            boolean whereAdded = false;

            // Filter berdasarkan kata kunci dengan pencarian yang lebih spesifik
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append(" WHERE (LOWER(nama_admin) LIKE LOWER(?) OR LOWER(nama_sampah) LIKE LOWER(?)) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        sql.append(" WHERE LOWER(nama_admin) LIKE LOWER(?) ");
                        whereAdded = true;
                        break;
                    case "Nama Sampah":
                        sql.append(" WHERE LOWER(nama_sampah) LIKE LOWER(?) ");
                        whereAdded = true;
                        break;
                }
            }

            // Filter berdasarkan input tanggal manual
            if (isRange) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(riwayat) BETWEEN ? AND ? ");
                whereAdded = true;
            } else if (isSingleDate) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(riwayat) = ? ");
                whereAdded = true;
            }

            sql.append(" ORDER BY riwayat DESC LIMIT ? OFFSET ?");

            try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci dengan pencarian yang lebih akurat
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
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

                st.setInt(paramIndex++, dataPerHalaman);
                st.setInt(paramIndex, (halamanSaatIni - 1) * dataPerHalaman);

                try (ResultSet rs = st.executeQuery()) {
                    int no = (halamanSaatIni - 1) * dataPerHalaman + 1;
                    while (rs.next()) {
                        String harga = rs.getString("harga");
                        if (harga != null && !harga.equals("-")) {
                            try {
                                double nominal = Double.parseDouble(harga);
                                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                                harga = formatRupiah.format(nominal);
                            } catch (NumberFormatException e) {
                                // Biarkan harga tetap
                            }
                        }

                        model.addRow(new Object[]{
                            no++,
                            rs.getString("nama_admin"),
                            rs.getString("nama_sampah"),
                            rs.getString("berat_sampah"),
                            harga,
                            rs.getString("riwayat")
                        });
                    }
                }
            }

            tb_laporan.setModel(model);
            calculateTotalPage();
            updatePemasukanLabelsWithFilter(kataKunci, filter, tanggalMulai, tanggalAkhir, isRange, isSingleDate);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private javax.swing.JComboBox<String> box_pilih;
    private component.Jbutton btn_Export2;
    private javax.swing.JButton btn_before2;
    private component.Jbutton btn_cancel;
    private javax.swing.JButton btn_first2;
    private javax.swing.JButton btn_last2;
    private javax.swing.JButton btn_next2;
    private javax.swing.JComboBox<String> cbx_data2;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_halaman2;
    private javax.swing.JLabel lbl_total_pendapatan;
    private javax.swing.JLabel lbl_transaksi;
    private component.ShadowPanel panelBawah2;
    private grafik.panel.PanelGradient panelGradient1;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.ShadowPanel shadowFilter;
    private component.ShadowPanel shadowTable;
    private component.Table tb_laporan;
    private swing.TextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}
