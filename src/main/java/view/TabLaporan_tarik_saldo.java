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
import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.Cursor;

public class TabLaporan_tarik_saldo extends javax.swing.JPanel {
    private final Connection conn = DBconnect.getConnection();
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 20;
    private int totalPages;
    private int totalData;

    // Variable to store current filter type - "Pemasukan", "Pengeluaran", or "" for
    // all
    private String currentFilterType = "";

    // Variable for filtering based on combobox selection
    private String filterJenis = null;

    public TabLaporan_tarik_saldo() {
        initComponents();
        txt_date.setText("");
        loadData("");
        setupPagination();
    }

    private void setupPagination() {
        cbx_data5.addActionListener(e -> {
            dataPerHalaman = Integer.parseInt(cbx_data5.getSelectedItem().toString());
            halamanSaatIni = 1;
            loadData("");
        });

        btn_first5.addActionListener(e -> {
            halamanSaatIni = 1;
            loadData("");
        });

        btn_before5.addActionListener(e -> {
            if (halamanSaatIni > 1) {
                halamanSaatIni--;
                loadData("");
            }
        });

        btn_next5.addActionListener(e -> {
            if (halamanSaatIni < totalPages) {
                halamanSaatIni++;
                loadData("");
            }
        });

        btn_last5.addActionListener(e -> {
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
        lb_halaman5.setText(String.valueOf("Page " + halamanSaatIni + " Dari Total " + totalData + " Data"));
    }

    private int getTotalData() {
        try {
            String query = """
                        SELECT COUNT(*) as total FROM (
                            -- Count setor sampah entries
                            SELECT
                                st.tanggal
                            FROM laporan_pengeluaran lpn
                            INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                            INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                            WHERE lpn.id_setoran IS NOT NULL

                            UNION ALL

                            -- Count withdrawal entries
                            SELECT
                                ps.tanggal_penarikan
                            FROM penarikan_saldo ps
                            INNER JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                        ) AS combined_data
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

    private void updateLabels() {
        try (Connection conn = DBconnect.getConnection()) {
            String querySaldoMasuk = "SELECT SUM(harga) as total_saldo_masuk FROM setor_sampah WHERE harga != '-'";
            String querySaldoKeluar = "SELECT SUM(jumlah_penarikan) as total_saldo_keluar FROM penarikan_saldo";
            String queryTotalTransaksi;

            // Modify total transaksi query based on filter
            if (filterJenis != null) {
                if (filterJenis.equals("Pemasukan")) {
                    queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM setor_sampah";
                } else if (filterJenis.equals("Pengeluaran")) {
                    queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM penarikan_saldo";
                } else {
                    queryTotalTransaksi = """
                            SELECT
                                (SELECT COUNT(*) FROM setor_sampah) +
                                (SELECT COUNT(*) FROM penarikan_saldo) as total_transaksi
                            """;
                }
            } else {
                queryTotalTransaksi = """
                        SELECT
                            (SELECT COUNT(*) FROM setor_sampah) +
                            (SELECT COUNT(*) FROM penarikan_saldo) as total_transaksi
                        """;
            }

            // Mengambil total saldo masuk
            double totalSaldoMasuk = 0;
            try (PreparedStatement pst = conn.prepareStatement(querySaldoMasuk);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalSaldoMasuk = rs.getDouble("total_saldo_masuk");
                }
            }

            // Mengambil total saldo keluar
            double totalSaldoKeluar = 0;
            try (PreparedStatement pst = conn.prepareStatement(querySaldoKeluar);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalSaldoKeluar = rs.getDouble("total_saldo_keluar");
                }
            }

            // Format ke rupiah
            DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
            String saldoMasukFormatted = formatRupiah.format(totalSaldoMasuk);
            String saldoKeluarFormatted = formatRupiah.format(totalSaldoKeluar);

            // Calculate the saldo based on filter
            double netSaldo;
            if (filterJenis != null) {
                if (filterJenis.equals("Pemasukan")) {
                    netSaldo = totalSaldoMasuk;
                } else if (filterJenis.equals("Pengeluaran")) {
                    netSaldo = totalSaldoKeluar;
                } else {
                    netSaldo = totalSaldoMasuk - totalSaldoKeluar;
                }
            } else {
                netSaldo = totalSaldoMasuk - totalSaldoKeluar;
            }

            String netSaldoFormatted = formatRupiah.format(netSaldo);

            // Update label dengan total saldo
            lbl_saldoNasabah.setText(netSaldoFormatted);

            // Update label judul saldo sesuai filter
            if (filterJenis != null) {
                if (filterJenis.equals("Pemasukan")) {
                    LabelTotalSaldo.setText("Total Saldo Masuk");
                } else if (filterJenis.equals("Pengeluaran")) {
                    LabelTotalSaldo.setText("Total Saldo Keluar");
                } else {
                    LabelTotalSaldo.setText("Total Saldo Nasabah");
                }
            } else {
                LabelTotalSaldo.setText("Total Saldo Nasabah");
            }

            // Mengambil total transaksi
            try (PreparedStatement pst = conn.prepareStatement(queryTotalTransaksi);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int totalTransaksi = rs.getInt("total_transaksi");
                    lbl_jumlahPenarikan.setText(String.valueOf(totalTransaksi) + " Transaksi");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            // Set default values jika terjadi error
            lbl_saldoNasabah.setText("Rp 0");
            lbl_jumlahPenarikan.setText("0 Transaksi");
            e.printStackTrace();
        }
    }

    private void loadData(String searchKeyword) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[] {
                "No", "Nama Admin", "Nama Nasabah", "Jenis Transaksi", "Deskripsi", "Saldo Masuk", "Saldo Keluar",
                "Tanggal"
        }); // Create a UNION query to combine both setor_sampah (saldo masuk) and
        // penarikan_saldo (saldo keluar) transactions
        String baseQuery = """
                    SELECT
                        admin_name,
                        nasabah_name,
                        transaction_type,
                        description,
                        saldo_masuk,
                        saldo_keluar,
                        transaction_date
                    FROM (
                        -- Setor sampah entries (Saldo Masuk)
                        SELECT
                            u.nama_user AS admin_name,
                            n.nama_nasabah AS nasabah_name,
                            'Setor Sampah' AS transaction_type,
                            CONCAT(kate.nama_kategori, ' (', st.berat_sampah, ' kg)') AS description,
                            st.harga AS saldo_masuk,
                            0 AS saldo_keluar,
                            st.tanggal AS transaction_date
                        FROM laporan_pengeluaran lpn
                        INNER JOIN login u ON lpn.id_user = u.id_user
                        INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                        INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                        INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                        INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                        WHERE lpn.id_setoran IS NOT NULL

                        UNION ALL

                        -- Penarikan saldo entries (Saldo Keluar)
                        SELECT
                            l.nama_user AS admin_name,
                            n.nama_nasabah AS nasabah_name,
                            'Tarik Tunai' AS transaction_type,
                            'Penarikan Saldo' AS description,
                            0 AS saldo_masuk,
                            ps.jumlah_penarikan AS saldo_keluar,
                            ps.tanggal_penarikan AS transaction_date
                        FROM penarikan_saldo ps
                        INNER JOIN login l ON ps.id_user = l.id_user
                        INNER JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                    ) AS combined_data
                """; // Add filter conditions if necessary
        boolean whereAdded = false;

        // First apply filter based on the combobox selection (Saldo Masuk/Keluar)
        if (filterJenis != null) {
            if (filterJenis.equals("Pemasukan")) {
                baseQuery += " WHERE transaction_type = 'Setor Sampah'";
                whereAdded = true;
            } else if (filterJenis.equals("Pengeluaran")) {
                baseQuery += " WHERE transaction_type = 'Tarik Tunai'";
                whereAdded = true;
            }
        }

        // Then apply additional search keyword if provided
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            if (whereAdded) {
                baseQuery += " AND (nasabah_name LIKE ? OR admin_name LIKE ? OR transaction_type LIKE ?)";
            } else {
                baseQuery += " WHERE (nasabah_name LIKE ? OR admin_name LIKE ? OR transaction_type LIKE ?)";
                whereAdded = true;
            }
        }

        baseQuery += " ORDER BY transaction_date DESC LIMIT ? OFFSET ?";

        try (PreparedStatement pst = conn.prepareStatement(baseQuery)) {
            int paramIndex = 1;

            // Add search parameters if we have a search keyword
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String searchPattern = "%" + searchKeyword + "%";
                pst.setString(paramIndex++, searchPattern);
                pst.setString(paramIndex++, searchPattern);
                pst.setString(paramIndex++, searchPattern);
            }

            pst.setInt(paramIndex++, dataPerHalaman);
            pst.setInt(paramIndex, (halamanSaatIni - 1) * dataPerHalaman);

            try (ResultSet rs = pst.executeQuery()) {
                int no = (halamanSaatIni - 1) * dataPerHalaman + 1;
                while (rs.next()) {
                    // Format saldo masuk
                    String saldoMasuk = formatToRupiah(rs.getDouble("saldo_masuk"));

                    // Format saldo keluar
                    String saldoKeluar = formatToRupiah(rs.getDouble("saldo_keluar"));

                    model.addRow(new Object[] {
                            no++,
                            rs.getString("admin_name"),
                            rs.getString("nasabah_name"),
                            rs.getString("transaction_type"),
                            rs.getString("description"),
                            saldoMasuk,
                            saldoKeluar,
                            rs.getString("transaction_date")
                    });
                }
            }
            tb_laporan.setModel(model);
            calculateTotalPage();
            updateLabels();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to format double values to Rupiah
    private String formatToRupiah(double value) {
        if (value == 0)
            return "-";
        DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
        return formatRupiah.format(value);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
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
        panelFilter = new component.ShadowPanel();
        ShadowSearch = new component.ShadowPanel();
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txt_date = new swing.TextField();
        jButton1 = new javax.swing.JButton();
        btn_cancel = new component.Jbutton();
        box_FilterMK = new javax.swing.JComboBox<>();
        panelTable = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        panelGradient1 = new grafik.panel.PanelGradient();
        lbl_saldoNasabah = new javax.swing.JLabel();
        LabelTotalSaldo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_jumlahPenarikan = new javax.swing.JLabel();
        lbl_totalJumlahPenarikan = new javax.swing.JLabel();
        panelBawah5 = new component.ShadowPanel();
        lb_halaman5 = new javax.swing.JLabel();
        btn_before5 = new javax.swing.JButton();
        cbx_data5 = new javax.swing.JComboBox<>();
        btn_next5 = new javax.swing.JButton();
        btn_last5 = new javax.swing.JButton();
        btn_first5 = new javax.swing.JButton();
        btn_Export5 = new component.Jbutton();

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

        box_pilih.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nama Admin", "Nama Nasabah", "Nama Sampah" }));
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
                .addComponent(box_pilih, 0, 81, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                    .addComponent(txt_date, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowSearch1Layout.createSequentialGroup()
                        .addGap(0, 1, Short.MAX_VALUE)
                        .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        box_FilterMK.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Saldo Masuk", "Saldo Keluar" }));
        box_FilterMK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_FilterMKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelFilterLayout = new javax.swing.GroupLayout(panelFilter);
        panelFilter.setLayout(panelFilterLayout);
        panelFilterLayout.setHorizontalGroup(
            panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFilterLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(box_FilterMK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelFilterLayout.setVerticalGroup(
            panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFilterLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(ShadowSearch1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                        .addComponent(ShadowSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                        .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(box_FilterMK)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(15, 15, 15))
        );

        panelFilterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {box_FilterMK, jButton1});

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Nama Admin", "Nama Nasabah", "Jenis Transaksi", "Deskripsi", "Saldo Masuk", "Saldo Keluar", "Tanggal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tb_laporan);

        panelGradient1.setBackground(new java.awt.Color(0, 153, 153));
        panelGradient1.setColorGradient(new java.awt.Color(0, 204, 204));
        panelGradient1.setPreferredSize(new java.awt.Dimension(295, 295));

        lbl_saldoNasabah.setBackground(new java.awt.Color(255, 255, 255));
        lbl_saldoNasabah.setFont(lbl_saldoNasabah.getFont().deriveFont(lbl_saldoNasabah.getFont().getStyle() | java.awt.Font.BOLD, lbl_saldoNasabah.getFont().getSize()+24));
        lbl_saldoNasabah.setForeground(new java.awt.Color(255, 255, 255));
        lbl_saldoNasabah.setText("0");

        LabelTotalSaldo.setBackground(new java.awt.Color(255, 255, 255));
        LabelTotalSaldo.setFont(LabelTotalSaldo.getFont().deriveFont(LabelTotalSaldo.getFont().getStyle() | java.awt.Font.BOLD, LabelTotalSaldo.getFont().getSize()+6));
        LabelTotalSaldo.setForeground(new java.awt.Color(255, 255, 255));
        LabelTotalSaldo.setText("Total Saldo Nasabah");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/total_item.png"))); // NOI18N
        jLabel2.setText("jLabel2");
        jLabel2.setPreferredSize(new java.awt.Dimension(52, 47));

        lbl_jumlahPenarikan.setBackground(new java.awt.Color(255, 255, 255));
        lbl_jumlahPenarikan.setFont(lbl_jumlahPenarikan.getFont().deriveFont(lbl_jumlahPenarikan.getFont().getStyle() | java.awt.Font.BOLD, lbl_jumlahPenarikan.getFont().getSize()+12));
        lbl_jumlahPenarikan.setForeground(new java.awt.Color(255, 255, 255));
        lbl_jumlahPenarikan.setText("0");

        lbl_totalJumlahPenarikan.setBackground(new java.awt.Color(255, 255, 255));
        lbl_totalJumlahPenarikan.setFont(lbl_totalJumlahPenarikan.getFont().deriveFont(lbl_totalJumlahPenarikan.getFont().getStyle() | java.awt.Font.BOLD));
        lbl_totalJumlahPenarikan.setForeground(new java.awt.Color(255, 255, 255));
        lbl_totalJumlahPenarikan.setText("Total Transaksi");

        javax.swing.GroupLayout panelGradient1Layout = new javax.swing.GroupLayout(panelGradient1);
        panelGradient1.setLayout(panelGradient1Layout);
        panelGradient1Layout.setHorizontalGroup(
            panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_totalJumlahPenarikan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbl_jumlahPenarikan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(lbl_saldoNasabah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LabelTotalSaldo, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelGradient1Layout.setVerticalGroup(
            panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(LabelTotalSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_saldoNasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120)
                .addGroup(panelGradient1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelGradient1Layout.createSequentialGroup()
                        .addComponent(lbl_totalJumlahPenarikan, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_jumlahPenarikan, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );

        lb_halaman5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lb_halaman5.setText("hal");

        btn_before5.setText("<");

        cbx_data5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "20", "40", "60", "80" }));

        btn_next5.setText(">");
        btn_next5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_next5ActionPerformed(evt);
            }
        });

        btn_last5.setText("Last Page");
        btn_last5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_last5ActionPerformed(evt);
            }
        });

        btn_first5.setText("First Page");

        btn_Export5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_Export5.setText("Export To Excel");
        btn_Export5.setFillClick(new java.awt.Color(55, 130, 60));
        btn_Export5.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_Export5.setFillOver(new java.awt.Color(69, 160, 75));
        btn_Export5.setRoundedCorner(40);
        btn_Export5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Export5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBawah5Layout = new javax.swing.GroupLayout(panelBawah5);
        panelBawah5.setLayout(panelBawah5Layout);
        panelBawah5Layout.setHorizontalGroup(
            panelBawah5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah5Layout.createSequentialGroup()
                .addComponent(btn_Export5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_halaman5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_first5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_before5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbx_data5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_next5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_last5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBawah5Layout.setVerticalGroup(
            panelBawah5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBawah5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Export5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb_halaman5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_first5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_before5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbx_data5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_next5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_last5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelTableLayout = new javax.swing.GroupLayout(panelTable);
        panelTable.setLayout(panelTableLayout);
        panelTableLayout.setHorizontalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTableLayout.createSequentialGroup()
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)
                    .addComponent(panelBawah5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelTableLayout.setVerticalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
                    .addGroup(panelTableLayout.createSequentialGroup()
                        .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBawah5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panelFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(panelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void box_FilterMKActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_box_FilterMKActionPerformed
        // Handle filter selection for Saldo Masuk and Saldo Keluar
        String selectedFilter = box_FilterMK.getSelectedItem().toString();
        if (selectedFilter.equals("Saldo Masuk")) {
            filterJenis = "Pemasukan";
        } else if (selectedFilter.equals("Saldo Keluar")) {
            filterJenis = "Pengeluaran";
        } else {
            filterJenis = null; // Reset filter to show all data
        }
        // Apply the filter and refresh the table
        searchByKeywordAndDate();
        // Update labels to reflect the current filter
        updateLabels();
    }// GEN-LAST:event_box_FilterMKActionPerformed

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }// GEN-LAST:event_txt_searchKeyTyped

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_box_pilihActionPerformed
        searchByKeywordAndDate();
    }// GEN-LAST:event_box_pilihActionPerformed // Date chooser popup is now handled
     // directly in the action listener

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        resetFilter();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_cancelActionPerformed
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
    }// GEN-LAST:event_btn_cancelActionPerformed

    private void btn_Export5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_Export5ActionPerformed
        try {
            loadData("");

            DefaultTableModel model = (DefaultTableModel) tb_laporan.getModel();

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
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
                            JOptionPane.YES_NO_OPTION);
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
    }// GEN-LAST:event_btn_Export5ActionPerformed

    private void btn_last5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_last5ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btn_last5ActionPerformed

    private void btn_next5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_next5ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btn_next5ActionPerformed

    private void txt_dateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_dateActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_dateActionPerformed

    private void txt_datePropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_txt_datePropertyChange
        searchByKeywordAndDate(); // TODO add your handling code here:
    }// GEN-LAST:event_txt_datePropertyChange

    private void txt_dateKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_dateKeyTyped
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_dateKeyTyped

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
        model.setColumnIdentifiers(new String[] {
                "No", "Nama Admin", "Nama Nasabah", "Jenis Transaksi", "Deskripsi", "Saldo Masuk", "Saldo Keluar",
                "Tanggal"
        });

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                    SELECT
                        admin_name,
                        nasabah_name,
                        transaction_type,
                        description,
                        saldo_masuk,
                        saldo_keluar,
                        transaction_date
                    FROM (
                        -- Setor sampah entries (Saldo Masuk)
                        SELECT
                            u.nama_user AS admin_name,
                            n.nama_nasabah AS nasabah_name,
                            'Setor Sampah' AS transaction_type,
                            CONCAT(kate.nama_kategori, ' (', st.berat_sampah, ' kg)') AS description,
                            st.harga AS saldo_masuk,
                            0 AS saldo_keluar,
                            st.tanggal AS transaction_date
                        FROM laporan_pengeluaran lpn
                        INNER JOIN login u ON lpn.id_user = u.id_user
                        INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                        INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                        INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                        INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                        WHERE lpn.id_setoran IS NOT NULL

                        UNION ALL

                        -- Penarikan saldo entries (Saldo Keluar)
                        SELECT
                            l.nama_user AS admin_name,
                            n.nama_nasabah AS nasabah_name,
                            'Tarik Tunai' AS transaction_type,
                            'Penarikan Saldo' AS description,
                            0 AS saldo_masuk,
                            ps.jumlah_penarikan AS saldo_keluar,
                            ps.tanggal_penarikan AS transaction_date
                        FROM penarikan_saldo ps
                        INNER JOIN login l ON ps.id_user = l.id_user
                        INNER JOIN manajemen_nasabah n ON ps.id_nasabah = n.id_nasabah
                    ) AS combined_data
                    """);

            boolean whereAdded = false;

            // First apply filter based on the combobox selection (Saldo Masuk/Keluar)
            if (filterJenis != null) {
                if (filterJenis.equals("Pemasukan")) {
                    sql.append(" WHERE transaction_type = 'Setor Sampah'");
                    whereAdded = true;
                } else if (filterJenis.equals("Pengeluaran")) {
                    sql.append(" WHERE transaction_type = 'Tarik Tunai'");
                    whereAdded = true;
                }
            }

            // Filter berdasarkan kata kunci
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        if (whereAdded) {
                            sql.append(" AND (admin_name LIKE ? OR nasabah_name LIKE ? OR transaction_type LIKE ?)");
                        } else {
                            sql.append(" WHERE (admin_name LIKE ? OR nasabah_name LIKE ? OR transaction_type LIKE ?)");
                            whereAdded = true;
                        }
                        break;
                    case "Nama Admin":
                        if (whereAdded) {
                            sql.append(" AND admin_name LIKE ?");
                        } else {
                            sql.append(" WHERE admin_name LIKE ?");
                            whereAdded = true;
                        }
                        break;
                    case "Nama Nasabah":
                        if (whereAdded) {
                            sql.append(" AND nasabah_name LIKE ?");
                        } else {
                            sql.append(" WHERE nasabah_name LIKE ?");
                            whereAdded = true;
                        }
                        break;
                    case "Jenis Transaksi":
                        if (whereAdded) {
                            sql.append(" AND transaction_type LIKE ?");
                        } else {
                            sql.append(" WHERE transaction_type LIKE ?");
                            whereAdded = true;
                        }
                        break;
                }
            }

            // Filter berdasarkan tanggal
            if (isRange) {
                sql.append(whereAdded ? " AND " : " WHERE ");
                sql.append("DATE(transaction_date) BETWEEN ? AND ? ");
                whereAdded = true;
            } else if (isSingleDate) {
                sql.append(whereAdded ? " AND " : " WHERE ");
                sql.append("DATE(transaction_date) = ? ");
                whereAdded = true;
            }

            sql.append(" ORDER BY transaction_date DESC LIMIT ? OFFSET ?");

            try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
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

                st.setInt(paramIndex++, dataPerHalaman);
                st.setInt(paramIndex, (halamanSaatIni - 1) * dataPerHalaman);

                try (ResultSet rs = st.executeQuery()) {
                    int no = (halamanSaatIni - 1) * dataPerHalaman + 1;
                    while (rs.next()) {
                        // Format saldo masuk
                        String saldoMasuk = formatToRupiah(rs.getDouble("saldo_masuk"));

                        // Format saldo keluar
                        String saldoKeluar = formatToRupiah(rs.getDouble("saldo_keluar"));

                        model.addRow(new Object[] {
                                no++,
                                rs.getString("admin_name"),
                                rs.getString("nasabah_name"),
                                rs.getString("transaction_type"),
                                rs.getString("description"),
                                saldoMasuk,
                                saldoKeluar,
                                rs.getString("transaction_date")
                        });
                    }

                    tb_laporan.setModel(model);
                    calculateTotalPage();
                    updateLabels();
                    updateLabelsWithFilter(kataKunci, filter, tanggalMulai, tanggalAkhir, isRange, isSingleDate);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
            e.printStackTrace();
        }
    } // Method tambahan untuk update labels berdasarkan filter yang diterapkan

    private void updateLabelsWithFilter(String kataKunci, String filter, String tanggalMulai, String tanggalAkhir,
            boolean isRange, boolean isSingleDate) {
        try (Connection conn = DBconnect.getConnection()) {
            // Query untuk total saldo masuk (setor sampah) dengan filter
            StringBuilder querySaldoMasukBuilder = new StringBuilder();
            querySaldoMasukBuilder.append("SELECT SUM(harga) as total_saldo_masuk FROM setor_sampah st ");
            querySaldoMasukBuilder.append("JOIN laporan_pengeluaran lpn ON st.id_setoran = lpn.id_setoran ");
            querySaldoMasukBuilder.append("JOIN manajemen_nasabah mn ON lpn.id_nasabah = mn.id_nasabah ");
            querySaldoMasukBuilder.append("JOIN login l ON lpn.id_user = l.id_user ");

            boolean whereAdded = false;

            // Tambahkan filter kata kunci jika ada
            if (!kataKunci.isEmpty()) {
                whereAdded = true;
                querySaldoMasukBuilder.append(" WHERE ");

                switch (filter) {
                    case "Default":
                        querySaldoMasukBuilder.append("(l.nama_user LIKE ? OR mn.nama_nasabah LIKE ?)");
                        break;
                    case "Nama Admin":
                        querySaldoMasukBuilder.append("l.nama_user LIKE ?");
                        break;
                    case "Nama Nasabah":
                        querySaldoMasukBuilder.append("mn.nama_nasabah LIKE ?");
                        break;
                }
            }

            // Tambahkan filter tanggal jika ada
            if (isRange) {
                querySaldoMasukBuilder.append(whereAdded ? " AND " : " WHERE ");
                querySaldoMasukBuilder.append("DATE(st.tanggal) BETWEEN ? AND ?");
            } else if (isSingleDate) {
                querySaldoMasukBuilder.append(whereAdded ? " AND " : " WHERE ");
                querySaldoMasukBuilder.append("DATE(st.tanggal) = ?");
            }

            // Query untuk total saldo keluar (penarikan saldo) dengan filter
            StringBuilder querySaldoKeluarBuilder = new StringBuilder();
            querySaldoKeluarBuilder
                    .append("SELECT SUM(jumlah_penarikan) as total_saldo_keluar FROM penarikan_saldo ps ");
            querySaldoKeluarBuilder.append("JOIN manajemen_nasabah mn ON ps.id_nasabah = mn.id_nasabah ");
            querySaldoKeluarBuilder.append("JOIN login l ON ps.id_user = l.id_user ");

            boolean whereAddedKeluar = false;

            // Tambahkan filter kata kunci jika ada
            if (!kataKunci.isEmpty()) {
                whereAddedKeluar = true;
                querySaldoKeluarBuilder.append(" WHERE ");

                switch (filter) {
                    case "Default":
                        querySaldoKeluarBuilder.append("(l.nama_user LIKE ? OR mn.nama_nasabah LIKE ?)");
                        break;
                    case "Nama Admin":
                        querySaldoKeluarBuilder.append("l.nama_user LIKE ?");
                        break;
                    case "Nama Nasabah":
                        querySaldoKeluarBuilder.append("mn.nama_nasabah LIKE ?");
                        break;
                }
            }

            // Tambahkan filter tanggal jika ada
            if (isRange) {
                querySaldoKeluarBuilder.append(whereAddedKeluar ? " AND " : " WHERE ");
                querySaldoKeluarBuilder.append("DATE(ps.tanggal_penarikan) BETWEEN ? AND ?");
            } else if (isSingleDate) {
                querySaldoKeluarBuilder.append(whereAddedKeluar ? " AND " : " WHERE ");
                querySaldoKeluarBuilder.append("DATE(ps.tanggal_penarikan) = ?");
            } // Mengambil total saldo masuk
            double totalSaldoMasuk = 0;
            try (PreparedStatement pstSaldoMasuk = conn.prepareStatement(querySaldoMasukBuilder.toString())) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci saldo masuk
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pstSaldoMasuk.setString(paramIndex++, searchPattern);
                            pstSaldoMasuk.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pstSaldoMasuk.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal saldo masuk
                if (isRange) {
                    pstSaldoMasuk.setString(paramIndex++, tanggalMulai);
                    pstSaldoMasuk.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstSaldoMasuk.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsSaldoMasuk = pstSaldoMasuk.executeQuery()) {
                    if (rsSaldoMasuk.next()) {
                        totalSaldoMasuk = rsSaldoMasuk.getDouble("total_saldo_masuk");
                    }
                }
            }

            // Mengambil total saldo keluar
            double totalSaldoKeluar = 0;
            try (PreparedStatement pstSaldoKeluar = conn.prepareStatement(querySaldoKeluarBuilder.toString())) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci saldo keluar
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pstSaldoKeluar.setString(paramIndex++, searchPattern);
                            pstSaldoKeluar.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pstSaldoKeluar.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal saldo keluar
                if (isRange) {
                    pstSaldoKeluar.setString(paramIndex++, tanggalMulai);
                    pstSaldoKeluar.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstSaldoKeluar.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsSaldoKeluar = pstSaldoKeluar.executeQuery()) {
                    if (rsSaldoKeluar.next()) {
                        totalSaldoKeluar = rsSaldoKeluar.getDouble("total_saldo_keluar");
                    }
                }
            }

            // Query untuk total transaksi
            StringBuilder queryTotalTransaksiBuilder = new StringBuilder();
            queryTotalTransaksiBuilder.append("SELECT (");
            queryTotalTransaksiBuilder.append("(SELECT COUNT(*) FROM setor_sampah st ");
            queryTotalTransaksiBuilder.append("JOIN laporan_pengeluaran lpn ON st.id_setoran = lpn.id_setoran ");
            queryTotalTransaksiBuilder.append("JOIN manajemen_nasabah mn ON lpn.id_nasabah = mn.id_nasabah ");
            queryTotalTransaksiBuilder.append("JOIN login l ON lpn.id_user = l.id_user ");

            if (!kataKunci.isEmpty() || isRange || isSingleDate) {
                queryTotalTransaksiBuilder.append("WHERE ");
                boolean whereClauseAdded = false;

                if (!kataKunci.isEmpty()) {
                    whereClauseAdded = true;
                    switch (filter) {
                        case "Default":
                            queryTotalTransaksiBuilder.append("(l.nama_user LIKE ? OR mn.nama_nasabah LIKE ?)");
                            break;
                        case "Nama Admin":
                            queryTotalTransaksiBuilder.append("l.nama_user LIKE ?");
                            break;
                        case "Nama Nasabah":
                            queryTotalTransaksiBuilder.append("mn.nama_nasabah LIKE ?");
                            break;
                    }
                }

                if (isRange) {
                    if (whereClauseAdded)
                        queryTotalTransaksiBuilder.append(" AND ");
                    queryTotalTransaksiBuilder.append("DATE(st.tanggal) BETWEEN ? AND ?");
                } else if (isSingleDate) {
                    if (whereClauseAdded)
                        queryTotalTransaksiBuilder.append(" AND ");
                    queryTotalTransaksiBuilder.append("DATE(st.tanggal) = ?");
                }
            }

            queryTotalTransaksiBuilder.append(") + ");
            queryTotalTransaksiBuilder.append("(SELECT COUNT(*) FROM penarikan_saldo ps ");
            queryTotalTransaksiBuilder.append("JOIN manajemen_nasabah mn ON ps.id_nasabah = mn.id_nasabah ");
            queryTotalTransaksiBuilder.append("JOIN login l ON ps.id_user = l.id_user ");

            if (!kataKunci.isEmpty() || isRange || isSingleDate) {
                queryTotalTransaksiBuilder.append("WHERE ");
                boolean whereClauseAdded = false;

                if (!kataKunci.isEmpty()) {
                    whereClauseAdded = true;
                    switch (filter) {
                        case "Default":
                            queryTotalTransaksiBuilder.append("(l.nama_user LIKE ? OR mn.nama_nasabah LIKE ?)");
                            break;
                        case "Nama Admin":
                            queryTotalTransaksiBuilder.append("l.nama_user LIKE ?");
                            break;
                        case "Nama Nasabah":
                            queryTotalTransaksiBuilder.append("mn.nama_nasabah LIKE ?");
                            break;
                    }
                }

                if (isRange) {
                    if (whereClauseAdded)
                        queryTotalTransaksiBuilder.append(" AND ");
                    queryTotalTransaksiBuilder.append("DATE(ps.tanggal_penarikan) BETWEEN ? AND ?");
                } else if (isSingleDate) {
                    if (whereClauseAdded)
                        queryTotalTransaksiBuilder.append(" AND ");
                    queryTotalTransaksiBuilder.append("DATE(ps.tanggal_penarikan) = ?");
                }
            }

            queryTotalTransaksiBuilder.append(")) as total_transaksi");

            // Calculate net saldo
            double netSaldo = totalSaldoMasuk - totalSaldoKeluar;
            DecimalFormat formatRupiah = new DecimalFormat("'Rp '###,###");
            lbl_saldoNasabah.setText(formatRupiah.format(netSaldo));

            // Get total transactions count
            try (PreparedStatement pstTotalTransaksi = conn.prepareStatement(queryTotalTransaksiBuilder.toString())) {
                int paramIndex = 1;

                // Set parameters for setor_sampah query
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

                if (isRange) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                    pstTotalTransaksi.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                }

                // Set parameters for penarikan_saldo query
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

                if (isRange) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                    pstTotalTransaksi.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstTotalTransaksi.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsTotalTransaksi = pstTotalTransaksi.executeQuery()) {
                    if (rsTotalTransaksi.next()) {
                        int totalTransaksi = rsTotalTransaksi.getInt("total_transaksi");
                        lbl_jumlahPenarikan.setText(String.valueOf(totalTransaksi) + " Transaksi");
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            e.printStackTrace();
            lbl_saldoNasabah.setText("Rp 0");
            lbl_jumlahPenarikan.setText("0 Transaksi");
        }
    } // This method has been removed since we're now using the penarikan_saldo table
      // which is already created in the database // Method to reset the filter type
      // and reload all data

    private void resetFilter() {
        currentFilterType = "";
        filterJenis = null;
        txt_search.setText("");
        txt_date.setText("");
        box_pilih.setSelectedIndex(0);
        box_FilterMK.setSelectedIndex(0); // Reset filter dropdown to "Default"
        loadData("");
        updateLabels(); // Update labels to reflect the reset filter
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelTotalSaldo;
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private javax.swing.JComboBox<String> box_FilterMK;
    private javax.swing.JComboBox<String> box_pilih;
    private component.Jbutton btn_Export5;
    private javax.swing.JButton btn_before5;
    private component.Jbutton btn_cancel;
    private javax.swing.JButton btn_first5;
    private javax.swing.JButton btn_last5;
    private javax.swing.JButton btn_next5;
    private javax.swing.JComboBox<String> cbx_data5;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_halaman5;
    private javax.swing.JLabel lbl_jumlahPenarikan;
    private javax.swing.JLabel lbl_saldoNasabah;
    private javax.swing.JLabel lbl_totalJumlahPenarikan;
    private component.ShadowPanel panelBawah5;
    private component.ShadowPanel panelFilter;
    private grafik.panel.PanelGradient panelGradient1;
    private javax.swing.JPanel panelMain;
    private component.ShadowPanel panelTable;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.Table tb_laporan;
    private swing.TextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}
