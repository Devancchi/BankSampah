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

public class TabLaporan_setor_sampah extends javax.swing.JPanel {

    public TabLaporan_setor_sampah() {
        initComponents();

        loadData("");

    }

    private void updateLabels() {
        try (Connection conn = DBconnect.getConnection()) {
            // Query untuk total pengeluaran (sum harga)
            String queryPengeluaran = "SELECT SUM(harga) as total_pengeluaran FROM setor_sampah WHERE harga != '-'";

            // Query untuk total transaksi (count semua data)
            String queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM setor_sampah";

            // Mengambil total pengeluaran
            try (PreparedStatement pstPengeluaran = conn.prepareStatement(queryPengeluaran); ResultSet rsPengeluaran = pstPengeluaran.executeQuery()) {

                if (rsPengeluaran.next()) {
                    double totalPengeluaran = rsPengeluaran.getDouble("total_pengeluaran");

                    // Format ke rupiah
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    String pengeluaranFormatted = formatRupiah.format(totalPengeluaran);

                    lbl_pengeluaran.setText(pengeluaranFormatted);
                }
            }

            // Mengambil total transaksi
            try (PreparedStatement pstTotalTransaksi = conn.prepareStatement(queryTotalTransaksi); ResultSet rsTotalTransaksi = pstTotalTransaksi.executeQuery()) {

                if (rsTotalTransaksi.next()) {
                    int totalTransaksi = rsTotalTransaksi.getInt("total_transaksi");
                    lbl_total_transaksi.setText(String.valueOf(totalTransaksi) + " Transaksi");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            // Set default values jika terjadi error
            lbl_pengeluaran.setText("Rp 0");
            lbl_total_transaksi.setText("0 Transaksi");
        }
    }

// Method loadData yang sudah dimodifikasi untuk memanggil updateLabels()
    private void loadData(String filterJenis) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // Kolom tanpa ID
        model.setColumnIdentifiers(new String[]{
            "No", "Nama Admin", "Nama Nasabah", "Nama Sampah", "Berat Sampah", "Harga", "Saldo Didapatkan", "Riwayat"
        });
        String baseQuery = """
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
                ) AS combine
    """;
        baseQuery += " ORDER BY riwayat DESC";
        try (Connection conn = DBconnect.getConnection(); PreparedStatement pst = conn.prepareStatement(baseQuery)) {
            if (filterJenis != null && !filterJenis.isEmpty()) {
                pst.setString(1, filterJenis);
            }
            try (ResultSet rs = pst.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    String harga = rs.getString("harga");
                    if (!harga.equals("-")) {
                        try {
                            double nominal = Double.parseDouble(harga);
                            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                            harga = formatRupiah.format(nominal);
                        } catch (NumberFormatException e) {
                            // Biarkan harga tetap apa adanya jika gagal format
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
            tb_laporan.setModel(model);

            // Update labels setelah data berhasil dimuat
            updateLabels();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
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
        panelFilter = new component.ShadowPanel();
        ShadowSearch = new component.ShadowPanel();
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        txt_date = new javax.swing.JTextField();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btn_cancel = new component.Jbutton();
        panelTable = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        btn_add = new component.Jbutton();
        panelGradient1 = new grafik.panel.PanelGradient();
        jLabel2 = new javax.swing.JLabel();
        lbl_pengeluaran = new javax.swing.JLabel();
        lbl_total_transaksi = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        box_rentang_waktu = new javax.swing.JComboBox<>();

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

        txt_date.setText("");
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
        pilihtanggal.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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
                .addGap(6, 6, 6)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(txt_date, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );
        ShadowSearch1Layout.setVerticalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowSearch1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ShadowSearch1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(pilihtanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txt_date))
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
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelFilterLayout.setVerticalGroup(
            panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFilterLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(ShadowSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                        .addComponent(ShadowSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Nama Admin", "Nama Nasabah", "Nama Sampah", "Berat Sampah", "Harga", "Saldo Didapatkan", "Riwayat"
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

        btn_add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_add.setText("Export To Excel");
        btn_add.setFillClick(new java.awt.Color(55, 130, 60));
        btn_add.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_add.setFillOver(new java.awt.Color(69, 160, 75));
        btn_add.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_add.setRoundedCorner(40);
        btn_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_addActionPerformed(evt);
            }
        });

        panelGradient1.setBackground(new java.awt.Color(0, 153, 153));
        panelGradient1.setColorGradient(new java.awt.Color(0, 204, 204));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/total_item.png"))); // NOI18N
        jLabel2.setText("jLabel2");
        panelGradient1.add(jLabel2);
        jLabel2.setBounds(20, 120, 60, 48);

        lbl_pengeluaran.setBackground(new java.awt.Color(255, 255, 255));
        lbl_pengeluaran.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lbl_pengeluaran.setForeground(new java.awt.Color(255, 255, 255));
        lbl_pengeluaran.setText("0");
        panelGradient1.add(lbl_pengeluaran);
        lbl_pengeluaran.setBounds(30, 50, 240, 30);

        lbl_total_transaksi.setBackground(new java.awt.Color(255, 255, 255));
        lbl_total_transaksi.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        lbl_total_transaksi.setForeground(new java.awt.Color(255, 255, 255));
        lbl_total_transaksi.setText("0");
        panelGradient1.add(lbl_total_transaksi);
        lbl_total_transaksi.setBounds(80, 140, 160, 20);

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Rentan Periode");
        panelGradient1.add(jLabel5);
        jLabel5.setBounds(20, 190, 160, 20);

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Total Transaksi");
        panelGradient1.add(jLabel6);
        jLabel6.setBounds(80, 120, 160, 20);

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Total Pengeluaran");
        panelGradient1.add(jLabel9);
        jLabel9.setBounds(30, 20, 160, 20);

        box_rentang_waktu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua", "Laporan per 7 Hari Terakhir", "Laporan per 30 Hari Terakhir", "Laporan per 1 Tahun Terakhir" }));
        panelGradient1.add(box_rentang_waktu);
        box_rentang_waktu.setBounds(20, 220, 160, 22);

        javax.swing.GroupLayout panelTableLayout = new javax.swing.GroupLayout(panelTable);
        panelTable.setLayout(panelTableLayout);
        panelTableLayout.setHorizontalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTableLayout.createSequentialGroup()
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTableLayout.createSequentialGroup()
                        .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelTableLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelTableLayout.setVerticalGroup(
            panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelGradient1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 754, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
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

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_searchKeyTyped

    private void box_pilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_pilihActionPerformed
        searchByKeywordAndDate();
    }//GEN-LAST:event_box_pilihActionPerformed

    private void txt_datePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txt_datePropertyChange
        searchByKeywordAndDate();
    }//GEN-LAST:event_txt_datePropertyChange

    private void txt_dateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_dateKeyTyped

    }//GEN-LAST:event_txt_dateKeyTyped

    private void pilihtanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pilihtanggalActionPerformed
        dateChooser1.showPopup();
    }//GEN-LAST:event_pilihtanggalActionPerformed

    private void btn_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_addActionPerformed
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
    }//GEN-LAST:event_btn_addActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txt_date.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cancelActionPerformed
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void searchByKeywordAndDate() {
        String kataKunci = txt_search.getText().trim();
        String tanggalRange = txt_date.getText().trim();
        String filter = box_pilih.getSelectedItem().toString();
        String rentangWaktu = box_rentang_waktu.getSelectedItem().toString(); // Combobox rentang waktu baru

        String tanggalMulai = "";
        String tanggalAkhir = "";
        boolean isRange = false;
        boolean isSingleDate = false;
        boolean isTimeRangeFilter = false;

        // Cek rentang waktu dari combobox
        if (!rentangWaktu.equals("Semua")) {
            isTimeRangeFilter = true;
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            switch (rentangWaktu) {
                case "Laporan 7 Hari Terakhir":
                    tanggalMulai = today.minusDays(7).format(formatter);
                    tanggalAkhir = today.format(formatter);
                    isRange = true;
                    break;
                case "Laporan per 30 Hari Terakhir":
                    tanggalMulai = today.minusDays(30).format(formatter);
                    tanggalAkhir = today.format(formatter);
                    isRange = true;
                    break;
                case "Laporan per 1 Tahun Terakhir":
                    tanggalMulai = today.minusYears(1).format(formatter);
                    tanggalAkhir = today.format(formatter);
                    isRange = true;
                    break;
            }
        } // Jika tidak ada filter rentang waktu dari combobox, cek input manual tanggal
        else if (!tanggalRange.isEmpty()) {
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

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DBconnect.getConnection();

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
            ) AS combine
        """);

            boolean whereAdded = false;

            // Filter berdasarkan kata kunci
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        sql.append(" WHERE (nama_admin LIKE ? OR nama_nasabah LIKE ? OR nama_sampah LIKE ?) ");
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
                    case "Nama Sampah":
                        sql.append(" WHERE nama_sampah LIKE ? ");
                        whereAdded = true;
                        break;
                }
            }

            // Filter berdasarkan tanggal (baik dari combobox maupun input manual)
            if (isRange) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(riwayat) BETWEEN ? AND ? ");
            } else if (isSingleDate) {
                sql.append(whereAdded ? "AND " : "WHERE ");
                sql.append("DATE(riwayat) = ? ");
            }

            sql.append(" ORDER BY riwayat DESC ");

            st = conn.prepareStatement(sql.toString());

            int paramIndex = 1;

            // Set parameter untuk kata kunci
            if (!kataKunci.isEmpty()) {
                String searchPattern = "%" + kataKunci + "%";
                switch (filter) {
                    case "Default":
                        st.setString(paramIndex++, searchPattern); // nama_admin
                        st.setString(paramIndex++, searchPattern); // nama_nasabah
                        st.setString(paramIndex++, searchPattern); // nama_sampah
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

            tb_laporan.setModel(model);
            tb_laporan.clearSelection();

            // Update labels setelah filter diterapkan
            updateLabelsWithFilter(kataKunci, filter, tanggalMulai, tanggalAkhir, isRange, isSingleDate);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

// Method tambahan untuk update labels berdasarkan filter yang diterapkan
    private void updateLabelsWithFilter(String kataKunci, String filter, String tanggalMulai, String tanggalAkhir, boolean isRange, boolean isSingleDate) {
        try (Connection conn = DBconnect.getConnection()) {

            // Base query untuk mengambil data yang sudah difilter
            StringBuilder baseQuery = new StringBuilder();
            baseQuery.append("""
            SELECT harga FROM (
                SELECT 
                    u.nama_user AS nama_admin,
                    n.nama_nasabah AS nama_nasabab,
                    kate.nama_kategori AS nama_sampah,
                    st.harga AS harga,
                    st.tanggal AS riwayat
                FROM laporan_pengeluaran lpn
                INNER JOIN login u ON lpn.id_user = u.id_user
                INNER JOIN setor_sampah st ON lpn.id_setoran = st.id_setoran
                INNER JOIN sampah s ON st.id_sampah = s.id_sampah
                INNER JOIN kategori_sampah kate ON s.id_kategori = kate.id_kategori
                INNER JOIN manajemen_nasabah n ON lpn.id_nasabah = n.id_nasabah
                WHERE lpn.id_setoran IS NOT NULL
            ) AS combine
        """);

            boolean whereAdded = false;

            // Tambahkan filter kata kunci jika ada
            if (!kataKunci.isEmpty()) {
                switch (filter) {
                    case "Default":
                        baseQuery.append(" WHERE (nama_admin LIKE ? OR nama_nasabab LIKE ? OR nama_sampah LIKE ?) ");
                        whereAdded = true;
                        break;
                    case "Nama Admin":
                        baseQuery.append(" WHERE nama_admin LIKE ? ");
                        whereAdded = true;
                        break;
                    case "Nama Nasabah":
                        baseQuery.append(" WHERE nama_nasabab LIKE ? ");
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

            // Query untuk total pengeluaran
            String queryPengeluaran = "SELECT SUM(CASE WHEN harga != '-' THEN CAST(harga AS DECIMAL(10,2)) ELSE 0 END) as total_pengeluaran FROM (" + baseQuery.toString() + ") AS filtered_data";

            // Query untuk total transaksi
            String queryTotalTransaksi = "SELECT COUNT(*) as total_transaksi FROM (" + baseQuery.toString() + ") AS filtered_data";

            // Mengambil total pengeluaran
            try (PreparedStatement pstPengeluaran = conn.prepareStatement(queryPengeluaran)) {
                int paramIndex = 1;

                // Set parameter untuk kata kunci
                if (!kataKunci.isEmpty()) {
                    String searchPattern = "%" + kataKunci + "%";
                    switch (filter) {
                        case "Default":
                            pstPengeluaran.setString(paramIndex++, searchPattern);
                            pstPengeluaran.setString(paramIndex++, searchPattern);
                            pstPengeluaran.setString(paramIndex++, searchPattern);
                            break;
                        default:
                            pstPengeluaran.setString(paramIndex++, searchPattern);
                            break;
                    }
                }

                // Set parameter untuk tanggal
                if (isRange) {
                    pstPengeluaran.setString(paramIndex++, tanggalMulai);
                    pstPengeluaran.setString(paramIndex++, tanggalAkhir);
                } else if (isSingleDate) {
                    pstPengeluaran.setString(paramIndex++, tanggalMulai);
                }

                try (ResultSet rsPengeluaran = pstPengeluaran.executeQuery()) {
                    if (rsPengeluaran.next()) {
                        double totalPengeluaran = rsPengeluaran.getDouble("total_pengeluaran");
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        String pengeluaranFormatted = formatRupiah.format(totalPengeluaran);
                        lbl_pengeluaran.setText(pengeluaranFormatted);
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
                        lbl_total_transaksi.setText(String.valueOf(totalTransaksi) + " Transaksi");
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data label: " + e.getMessage());
            lbl_pengeluaran.setText("Rp 0");
            lbl_total_transaksi.setText("0 Transaksi");
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowSearch1;
    private javax.swing.JComboBox<String> box_pilih;
    private javax.swing.JComboBox<String> box_rentang_waktu;
    private component.Jbutton btn_add;
    private component.Jbutton btn_cancel;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_pengeluaran;
    private javax.swing.JLabel lbl_total_transaksi;
    private component.ShadowPanel panelFilter;
    private grafik.panel.PanelGradient panelGradient1;
    private javax.swing.JPanel panelMain;
    private component.ShadowPanel panelTable;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.Table tb_laporan;
    private javax.swing.JTextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}
