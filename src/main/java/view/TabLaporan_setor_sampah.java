package view;

import component.ExcelExporter;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;
import main.DBconnect;


public class TabLaporan_setor_sampah extends javax.swing.JPanel {
    
    public TabLaporan_setor_sampah() {
         initComponents();
         
        loadData("");
        
    }
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

    try (Connection conn = DBconnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(baseQuery)) {

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
        ShadowUtama = new component.ShadowPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_laporan = new component.Table();
        card4 = new component.Card();
        ShadowSearch = new component.ShadowPanel();
        txt_search = new swing.TextField();
        box_pilih = new javax.swing.JComboBox<>();
        ShadowSearch1 = new component.ShadowPanel();
        txt_date = new javax.swing.JTextField();
        pilihtanggal = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        card6 = new component.Card();
        btn_detail_pemasukan = new ripple.button.Button();
        btn_laporan_transaksi = new ripple.button.Button();
        btn_laporan_jual_sampah = new ripple.button.Button();
        jLabel1 = new javax.swing.JLabel();
        btn_add = new component.Jbutton();

        dateChooser1.setDateChooserRender(defaultDateChooserRender1);
        dateChooser1.setDateSelectable(null);
        dateChooser1.setDateSelectionMode(datechooser.Main.DateChooser.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dateChooser1.setTextField(txt_date);

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(255, 255, 255));
        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(248, 248, 248));

        tb_laporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
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
                .addContainerGap()
                .addComponent(box_pilih, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addContainerGap())
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowSearchLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(box_pilih, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        txt_date.setBackground(new java.awt.Color(230, 245, 241));
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
                .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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

        card6.setFillColor(new java.awt.Color(255, 255, 255));

        btn_detail_pemasukan.setBackground(new java.awt.Color(0, 204, 204));
        btn_detail_pemasukan.setText("Laporan Jual Sampah");
        btn_detail_pemasukan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_detail_pemasukan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_detail_pemasukanActionPerformed(evt);
            }
        });

        btn_laporan_transaksi.setBackground(new java.awt.Color(0, 204, 204));
        btn_laporan_transaksi.setText("Laporan & Statistik");
        btn_laporan_transaksi.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_laporan_transaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_laporan_transaksiActionPerformed(evt);
            }
        });

        btn_laporan_jual_sampah.setBackground(new java.awt.Color(0, 204, 204));
        btn_laporan_jual_sampah.setText("Laporan Transaksi");
        btn_laporan_jual_sampah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_laporan_jual_sampah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_laporan_jual_sampahActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Halaman Laporan");

        javax.swing.GroupLayout card6Layout = new javax.swing.GroupLayout(card6);
        card6.setLayout(card6Layout);
        card6Layout.setHorizontalGroup(
            card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card6Layout.createSequentialGroup()
                        .addGroup(card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btn_laporan_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_laporan_jual_sampah, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_detail_pemasukan, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card6Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(118, 118, 118))))
        );
        card6Layout.setVerticalGroup(
            card6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(btn_laporan_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_laporan_jual_sampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_detail_pemasukan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87))
        );

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

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtamaLayout.createSequentialGroup()
                .addGap(0, 27, Short.MAX_VALUE)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(ShadowUtamaLayout.createSequentialGroup()
                            .addComponent(jScrollPane1)
                            .addGap(39, 39, 39)
                            .addComponent(card6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 777, Short.MAX_VALUE)
                        .addGap(5, 5, 5)
                        .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ShadowUtamaLayout.createSequentialGroup()
                        .addComponent(card6, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        panelView.add(ShadowUtama, "card2");

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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txt_date.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btn_detail_pemasukanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_detail_pemasukanActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_jual_sampah());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_detail_pemasukanActionPerformed

    private void btn_laporan_transaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_laporan_transaksiActionPerformed
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_laporan_transaksiActionPerformed

    private void btn_laporan_jual_sampahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_laporan_jual_sampahActionPerformed
       panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporan_transaksi());
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btn_laporan_jual_sampahActionPerformed

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

        if (isRange) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("riwayat BETWEEN ? AND ? ");
        } else if (isSingleDate) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("riwayat = ? ");
        }

        sql.append(" ORDER BY riwayat DESC ");

        st = conn.prepareStatement(sql.toString());

        int paramIndex = 1;
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

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
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
    private component.Jbutton btn_add;
    private ripple.button.Button btn_detail_pemasukan;
    private ripple.button.Button btn_laporan_jual_sampah;
    private ripple.button.Button btn_laporan_transaksi;
    private component.Card card4;
    private component.Card card6;
    private datechooser.Main.DateBetween dateBetween1;
    private datechooser.Main.DateChooser dateChooser1;
    private datechooser.render.DefaultDateChooserRender defaultDateChooserRender1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JButton pilihtanggal;
    private component.Table tb_laporan;
    private javax.swing.JTextField txt_date;
    private swing.TextField txt_search;
    // End of variables declaration//GEN-END:variables
}


