package view;

import component.ExcelExporter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.DBconnect;
import notification.toast.Notifications;

public class TabLaporan_transaksi extends javax.swing.JPanel {

    private final Connection conn = DBconnect.getConnection();
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 20;
    private int totalPages;
    private int totalData;

    public TabLaporan_transaksi() {
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
            String sql = """
                SELECT COUNT(*) as total FROM (
                    SELECT 
                        u.nama_user AS nama_admin,
                        n.nama_nasabah AS nama_nasabah,
                        kate.nama_kategori AS nama_sampah,
                        st.berat_sampah AS berat_sampah,                
                        st.harga AS harga,
                        st.tanggal AS riwayat,
                        st.saldo_nasabah AS saldo_didapatkan
                    FROM laporan_pengeluaran lpn
                    INNER JOIN login u ON lpn.id_user = u.id_user
                    INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                    INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                    INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                    INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                    WHERE lpn.id_setoran IS NOT NULL
                ) AS combine
            """;

            try (PreparedStatement st = conn.prepareStatement(sql);
                 ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        return 0;
    }

    private void loadData(String filterJenis) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.setColumnIdentifiers(new String[]{
            "No", "Nama Admin", "Nama Nasabah", "Nama Sampah", "Berat Sampah", "Harga", "Saldo Didapatkan", "Riwayat"
        });

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                SELECT 
                    nama_admin, 
                    nama_nasabah,
                    nama_sampah,
                    berat_sampah,
                    harga,
                    saldo_didapatkan,
                    riwayat
                FROM (
                    SELECT 
                        u.nama_user AS nama_admin,
                        n.nama_nasabah AS nama_nasabah,
                        kate.nama_kategori AS nama_sampah,
                        st.berat_sampah AS berat_sampah,                
                        st.harga AS harga,
                        st.tanggal AS riwayat,
                        st.saldo_nasabah AS saldo_didapatkan
                    FROM laporan_pengeluaran lpn
                    INNER JOIN login u ON lpn.id_user = u.id_user
                    INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                    INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                    INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                    INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                    WHERE lpn.id_setoran IS NOT NULL
            """);

            if (!filterJenis.isEmpty()) {
                sql.append(" AND kate.nama_kategori = ?");
            }

            sql.append(") AS combine ORDER BY riwayat DESC LIMIT ? OFFSET ?");

            try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (!filterJenis.isEmpty()) {
                    st.setString(paramIndex++, filterJenis);
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
                                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                                harga = formatRupiah.format(nominal);
                            } catch (NumberFormatException e) {
                                // biarkan harga tetap
                            }
                        }

                        model.addRow(new Object[]{
                            no++,
                            rs.getString("nama_admin"),
                            rs.getString("nama_nasabah"),
                            rs.getString("nama_sampah"),
                            rs.getString("berat_sampah"),
                            harga,
                            rs.getString("saldo_didapatkan"),
                            rs.getString("riwayat")
                        });
                    }
                }
            }

            tb_laporan.setModel(model);
            calculateTotalPage();
            updateTotalLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
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
        btn_cancel = new component.Jbutton();
        jButton1 = new javax.swing.JButton();
        shadowTable = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        panelGradient2 = new grafik.panel.PanelGradient();
        lbl_item_terjual = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lbl_total_transaksi = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbl_total_harga = new javax.swing.JLabel();
        txt_total_harga1 = new javax.swing.JLabel();
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

        txt_search.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        box_pilih.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nama Admin", "Nama Barang" }));
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowSearchLayout.createSequentialGroup()
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowSearchLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(ShadowSearchLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(box_pilih)))
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
                .addContainerGap()
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

        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowFilterLayout = new javax.swing.GroupLayout(shadowFilter);
        shadowFilter.setLayout(shadowFilterLayout);
        shadowFilterLayout.setHorizontalGroup(
            shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowFilterLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowFilterLayout.setVerticalGroup(
            shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowFilterLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(shadowFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(ShadowSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                        .addComponent(ShadowSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Nama Admin", "Nama Barang", "Kode Transaksi", "Quantity", "Total Harga", "Riwayat"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tb_laporan);

        panelGradient2.setBackground(new java.awt.Color(0, 204, 204));
        panelGradient2.setColorGradient(new java.awt.Color(0, 153, 153));

        lbl_item_terjual.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        lbl_item_terjual.setForeground(new java.awt.Color(255, 255, 255));
        lbl_item_terjual.setText("12.000000");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Total Item Terjual");

        lbl_total_transaksi.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        lbl_total_transaksi.setForeground(new java.awt.Color(255, 255, 255));
        lbl_total_transaksi.setText("12.000000");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Total Transaksi");

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/total_item.png"))); // NOI18N

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/total_transaksi.png"))); // NOI18N

        lbl_total_harga.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lbl_total_harga.setForeground(new java.awt.Color(255, 255, 255));
        lbl_total_harga.setText("Rp.12.00000");

        txt_total_harga1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txt_total_harga1.setForeground(new java.awt.Color(255, 255, 255));
        txt_total_harga1.setText("Total Pendapatan Transaksi");

        javax.swing.GroupLayout panelGradient2Layout = new javax.swing.GroupLayout(panelGradient2);
        panelGradient2.setLayout(panelGradient2Layout);
        panelGradient2Layout.setHorizontalGroup(
            panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient2Layout.createSequentialGroup()
                .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_total_harga)
                            .addComponent(txt_total_harga1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel4)
                        .addGap(8, 8, 8)
                        .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_item_terjual, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel6)
                        .addGap(8, 8, 8)
                        .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_total_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelGradient2Layout.setVerticalGroup(
            panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGradient2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(lbl_total_harga))
                    .addComponent(txt_total_harga1))
                .addGap(22, 22, 22)
                .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(4, 4, 4)
                        .addComponent(lbl_item_terjual, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28)
                .addGroup(panelGradient2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel10)
                    .addGroup(panelGradient2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lbl_total_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(508, Short.MAX_VALUE))
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
                .addGap(0, 0, 0)
                .addGroup(shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE)
                    .addComponent(panelBawah2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGradient2, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowTableLayout.setVerticalGroup(
            shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowTableLayout.createSequentialGroup()
                        .addComponent(panelGradient2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(48, 48, 48))
                    .addGroup(shadowTableLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelBawah2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shadowTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(shadowTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_searchKeyTyped

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_pilihActionPerformed
        searchByKeywordAndDate();
    }//GEN-LAST:event_box_pilihActionPerformed

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pilihtanggalActionPerformed
        dateChooser1.showPopup();
    }//GEN-LAST:event_pilihtanggalActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cancelActionPerformed
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txt_date.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

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
            "No", "Nama Admin", "Nama Nasabah", "Nama Sampah", "Berat Sampah", "Harga", "Saldo Didapatkan", "Riwayat"
        });

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("""
                SELECT 
                    nama_admin, 
                    nama_nasabah,
                    nama_sampah,
                    berat_sampah,
                    harga,
                    saldo_didapatkan,
                    riwayat
                FROM (
                    SELECT 
                        u.nama_user AS nama_admin,
                        n.nama_nasabah AS nama_nasabah,
                        kate.nama_kategori AS nama_sampah,
                        st.berat_sampah AS berat_sampah,                
                        st.harga AS harga,
                        st.tanggal AS riwayat,
                        st.saldo_nasabah AS saldo_didapatkan
                    FROM laporan_pengeluaran lpn
                    INNER JOIN login u ON lpn.id_user = u.id_user
                    INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                    INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                    INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                    INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                    WHERE lpn.id_setoran IS NOT NULL
            """);

            boolean whereAdded = false;

            // Filter berdasarkan kata kunci
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append(" AND (nama_admin LIKE ? OR nama_nasabah LIKE ? OR nama_sampah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        sql.append(" AND nama_admin LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Nasabah":
                        sql.append(" AND nama_nasabah LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Sampah":
                        sql.append(" AND nama_sampah LIKE ? ");
                        whereAdded = true;
                        break;
                }
            }

            // Filter berdasarkan tanggal
            if (isRange) {
                sql.append(" AND DATE(riwayat) BETWEEN ? AND ? ");
            } else if (isSingleDate) {
                sql.append(" AND DATE(riwayat) = ? ");
            }

            sql.append(") AS combine ORDER BY riwayat DESC LIMIT ? OFFSET ?");

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
                        String harga = rs.getString("harga");
                        if (harga != null && !harga.equals("-")) {
                            try {
                                double nominal = Double.parseDouble(harga);
                                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                                harga = formatRupiah.format(nominal);
                            } catch (NumberFormatException e) {
                                // biarkan harga tetap
                            }
                        }

                        model.addRow(new Object[]{
                            no++,
                            rs.getString("nama_admin"),
                            rs.getString("nama_nasabah"),
                            rs.getString("nama_sampah"),
                            rs.getString("berat_sampah"),
                            harga,
                            rs.getString("saldo_didapatkan"),
                            rs.getString("riwayat")
                        });
                    }
                }
            }

            tb_laporan.setModel(model);
            calculateTotalPage();
            updateTotalLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateTotalLabels() {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        try {
            conn = DBconnect.getConnection();
            
            // Query ini TIDAK menggunakan filter apapun - selalu menghitung SEMUA data
            String sql = """
                SELECT 
                    SUM(CASE WHEN tr.total_harga IS NOT NULL AND tr.total_harga != '-' 
                        THEN CAST(tr.total_harga AS DECIMAL(15,2)) ELSE 0 END) AS total_harga_keseluruhan,
                    SUM(CASE WHEN tr.qty IS NOT NULL AND tr.qty != '-' 
                        THEN CAST(tr.qty AS DECIMAL(10,0)) ELSE 0 END) AS total_item_terjual,
                    COUNT(*) AS total_transaksi
                FROM laporan_pemasukan lp
                JOIN login u ON lp.id_user = u.id_user
                LEFT JOIN data_barang db ON lp.id_barang = db.id_barang
                LEFT JOIN transaksi tr ON lp.id_transaksi = tr.id_transaksi
                WHERE lp.id_transaksi IS NOT NULL
            """;
            
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            
            if (rs.next()) {
                double totalHarga = rs.getDouble("total_harga_keseluruhan");
                int totalItem = rs.getInt("total_item_terjual");
                int totalTransaksi = rs.getInt("total_transaksi");
                
                // Format dan update JLabel dengan total keseluruhan
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                NumberFormat formatAngka = NumberFormat.getNumberInstance(new Locale("id", "ID"));
                
                // Update JLabel dengan nilai SEMUA data (tidak terpengaruh filter)
                lbl_total_harga.setText(formatRupiah.format(totalHarga));
                lbl_item_terjual.setText(formatAngka.format(totalItem) + " Item");
                lbl_total_transaksi.setText(formatAngka.format(totalTransaksi) + " Transaksi");
            }
            
        } catch (SQLException e) {
            Logger.getLogger(TabManajemenNasabah.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_halaman2;
    private javax.swing.JLabel lbl_item_terjual;
    private javax.swing.JLabel lbl_total_harga;
    private javax.swing.JLabel lbl_total_transaksi;
    private component.ShadowPanel panelBawah2;
    private grafik.panel.PanelGradient panelGradient2;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.ShadowPanel shadowFilter;
    private component.ShadowPanel shadowTable;
    private component.Table tb_laporan;
    private swing.TextField txt_date;
    private swing.TextField txt_search;
    private javax.swing.JLabel txt_total_harga1;
    // End of variables declaration//GEN-END:variables
}
