package view;

import component.UserSession;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.DBconnect;

public class TabManajemenSampah extends javax.swing.JPanel {

    private int id_user;
    private final Connection conn = DBconnect.getConnection();
    private DefaultTableModel tblModel;
    private int selectedIdSampah = -1; // default -1 berarti belum ada yang dipilih
    private String lastButtonClicked = ""; // global di luar method, misalnya di class kamu

    public TabManajemenSampah() {
        initComponents();
        /// load tabel utama ///
        inisialisasiTabel();
        /// load combo box jenis sampah ///
        loadJenisSampah();
        /// load tabel ////
        loadTabelKategori();
        loadTabelJenis();

    }

    public void setId(int x){
        this.id_user = x;
    }
    
    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabManajemenSampah());
        panelMain.repaint();
        panelMain.revalidate();
    }

    private void inisialisasiTabel() {
        // Mengatur model tabel
        tblModel = new DefaultTableModel(new String[]{"ID Sampah", "Jenis Sampah", "Kategori Sampah", "Harga Setor/Kg", "Harga Jual/Kg", "Tanggal"}, 0);
        tblSampah.setModel(tblModel);
        tblSampah.getColumnModel().getColumn(0).setMinWidth(0);
        tblSampah.getColumnModel().getColumn(0).setMaxWidth(0);
        tblSampah.getColumnModel().getColumn(0).setWidth(0);

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT s.id_sampah, s.harga_setor, s.harga_jual, s.tanggal,  k.nama_kategori,  j.nama_jenis "
                + "FROM sampah s "
                + "JOIN kategori_sampah k ON s.id_kategori = k.id_kategori "
                + "JOIN jenis_sampah j ON k.id_jenis = j.id_jenis "
                + "ORDER BY s.id_sampah")) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id_sampah"),
                    rs.getString("nama_jenis"),
                    rs.getString("nama_kategori"),
                    rs.getInt("harga_setor"),
                    rs.getInt("harga_jual"),
                    rs.getDate("tanggal")
                };
                tblModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void loadTabelKategori() {
        // Mengatur model tabel
        tblModel = new DefaultTableModel(new String[]{"ID Kategori", "Kategori Sampah", "Jenis Sampah"}, 0);
        tblKategori.setModel(tblModel);
        tblKategori.getColumnModel().getColumn(0).setMinWidth(0);
        tblKategori.getColumnModel().getColumn(0).setMaxWidth(0);
        tblKategori.getColumnModel().getColumn(0).setWidth(0);

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT k.id_kategori, k.nama_kategori,  j.nama_jenis "
                + "FROM kategori_sampah k "
                + "JOIN jenis_sampah j ON k.id_jenis = j.id_jenis "
                + "ORDER BY k.id_kategori")) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_kategori"),
                    rs.getString("nama_kategori"),
                    rs.getString("nama_jenis")
                };
                tblModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void loadTabelJenis() {
        // Mengatur model tabel
        tblModel = new DefaultTableModel(new String[]{"ID Jenis", "Jenis Sampah"}, 0);
        tblJenis.setModel(tblModel);
        tblJenis.getColumnModel().getColumn(0).setMinWidth(0);
        tblJenis.getColumnModel().getColumn(0).setMaxWidth(0);
        tblJenis.getColumnModel().getColumn(0).setWidth(0);

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM jenis_sampah")) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_jenis"),
                    rs.getString("nama_jenis")
                };
                tblModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    /// method membersihkan seluruh tf dan cbBox ///
    private void clearForm() {
        txt_Berat.setText("");
        txt_HargaAdd.setText("");
        txt_HargaEdit.setText("");

        txt_HargaAdd2.setText("");
        txt_HargaEdit2.setText("");

        txt_Jenis.setText("");
        txt_Kategori.setText("");

        txt_Kode.setText("");
        txt_Nama.setText("");

        cbxJenis_pnAdd.removeAllItems();
        cbxJenis_pnAdd.addItem("-- Pilih Kategori --");

        cbxKategori_pnAdd.removeAllItems();
        cbxKategori_pnAdd.addItem("-- Pilih Kategori --");

        cbxJenis_pnEdit.removeAllItems();
        cbxJenis_pnEdit.addItem("-- Pilih Kategori --");

        cbxKategori_pnEdit.removeAllItems();
        cbxKategori_pnEdit.addItem("-- Pilih Kategori --");

        cbxJenis_pnJK.removeAllItems();
        cbxJenis_pnJK.addItem("-- Pilih Kategori --");

        cbxJenis_pnView.removeAllItems();
        cbxJenis_pnView.addItem("-- Pilih Kategori --");

        cbxKategori_pnView.removeAllItems();
        cbxKategori_pnView.addItem("-- Pilih Kategori --");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        ShadowUtama = new component.ShadowPanel();
        shadowPanel1 = new component.ShadowPanel();
        panelTransaksiSampah = new component.ShadowPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_Nama = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxJenis_pnView = new javax.swing.JComboBox<>();
        cbxKategori_pnView = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        txt_Berat = new javax.swing.JTextField();
        btn_SampahMasuk = new component.Jbutton();
        jLabel10 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        txt_Kode = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        btn_SampahKeluar = new component.Jbutton();
        btn_ProsesSampah = new component.Jbutton();
        btnBatalProses = new component.Jbutton();
        shadowPanel3 = new component.ShadowPanel();
        jLabel16 = new javax.swing.JLabel();
        btnTambahHarga = new component.Jbutton();
        btnHapusHarga = new component.Jbutton();
        btnBatalHarga = new component.Jbutton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSampah = new component.Table();
        btnEditHarga = new component.Jbutton();
        btnKelola_JK = new component.Jbutton();
        jLabel2 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        panelAdd = new javax.swing.JPanel();
        ShadowUtama1 = new component.ShadowPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txt_HargaAdd = new javax.swing.JTextField();
        btnSimpanHarga = new component.Jbutton();
        btnKembaliT = new component.Jbutton();
        jLabel21 = new javax.swing.JLabel();
        cbxJenis_pnAdd = new javax.swing.JComboBox<>();
        cbxKategori_pnAdd = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        txt_HargaAdd2 = new javax.swing.JTextField();
        tgl_Add = new datechooser.beans.DateChooserCombo();
        panelRiwayat = new javax.swing.JPanel();
        ShadowUtama2 = new component.ShadowPanel();
        jLabel7 = new javax.swing.JLabel();
        btnKembaliR = new component.Jbutton();
        jScrollPane2 = new javax.swing.JScrollPane();
        table2 = new component.Table();
        ShadowSearch1 = new component.ShadowPanel();
        jLabel9 = new javax.swing.JLabel();
        btnFirstPage = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        cbxPage = new javax.swing.JComboBox<>();
        btnNext = new javax.swing.JButton();
        btnLastPage = new javax.swing.JButton();
        panelJK = new javax.swing.JPanel();
        ShadowUtama4 = new component.ShadowPanel();
        jLabel24 = new javax.swing.JLabel();
        txt_Jenis = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        btnKembaliJK = new component.Jbutton();
        btnTambahJenis = new component.Jbutton();
        btnHapusJenis = new component.Jbutton();
        jLabel25 = new javax.swing.JLabel();
        txt_Kategori = new javax.swing.JTextField();
        btnTambahKategori = new component.Jbutton();
        btnHapusKategori = new component.Jbutton();
        jLabel26 = new javax.swing.JLabel();
        cbxJenis_pnJK = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblJenis = new component.Table();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblKategori = new component.Table();
        panelEdit = new javax.swing.JPanel();
        ShadowUtama3 = new component.ShadowPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txt_HargaEdit = new javax.swing.JTextField();
        btnSimpanEdit = new component.Jbutton();
        btnKembaliE = new component.Jbutton();
        cbxJenis_pnEdit = new javax.swing.JComboBox<>();
        cbxKategori_pnEdit = new javax.swing.JComboBox<>();
        txt_HargaEdit2 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        tgl_Edit = new datechooser.beans.DateChooserCombo();

        setPreferredSize(new java.awt.Dimension(1192, 944));
        setLayout(new java.awt.CardLayout());

        panelMain.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(250, 250, 250));

        shadowPanel1.setMaximumSize(new java.awt.Dimension(1206, 400));
        shadowPanel1.setPreferredSize(new java.awt.Dimension(1206, 400));

        panelTransaksiSampah.setMaximumSize(new java.awt.Dimension(1206, 343));

        jLabel1.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel1.setText("Kode Nasabah");

        txt_Nama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_NamaActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel3.setText("Jenis Sampah");

        jLabel4.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel4.setText("Kategori");

        cbxJenis_pnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnViewActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel5.setText("Berat Sampah");

        txt_Berat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_BeratActionPerformed(evt);
            }
        });

        btn_SampahMasuk.setBackground(new java.awt.Color(255, 255, 51));
        btn_SampahMasuk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_masuk.png"))); // NOI18N
        btn_SampahMasuk.setText("Setor Sampah");
        btn_SampahMasuk.setFillClick(new java.awt.Color(101, 24, 148));
        btn_SampahMasuk.setFillOriginal(new java.awt.Color(135, 32, 198));
        btn_SampahMasuk.setFillOver(new java.awt.Color(174, 95, 222));
        btn_SampahMasuk.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SampahMasuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SampahMasukActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("NSimSun", 1, 20)); // NOI18N
        jLabel10.setText("Rp.");

        lblTotal.setFont(new java.awt.Font("NSimSun", 1, 48)); // NOI18N
        lblTotal.setText("0");

        txt_Kode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_KodeActionPerformed(evt);
            }
        });
        txt_Kode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txt_KodeKeyPressed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel17.setText("Nama Nasabah");

        btn_SampahKeluar.setBackground(new java.awt.Color(255, 255, 51));
        btn_SampahKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_keluar.png"))); // NOI18N
        btn_SampahKeluar.setText("Jual Sampah");
        btn_SampahKeluar.setFillClick(new java.awt.Color(153, 0, 153));
        btn_SampahKeluar.setFillOriginal(new java.awt.Color(204, 0, 255));
        btn_SampahKeluar.setFillOver(new java.awt.Color(255, 51, 255));
        btn_SampahKeluar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SampahKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SampahKeluarActionPerformed(evt);
            }
        });

        btn_ProsesSampah.setBackground(new java.awt.Color(255, 255, 51));
        btn_ProsesSampah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_addcollection.png"))); // NOI18N
        btn_ProsesSampah.setText("Proses Sampah");
        btn_ProsesSampah.setFillClick(new java.awt.Color(51, 0, 204));
        btn_ProsesSampah.setFillOriginal(new java.awt.Color(51, 51, 255));
        btn_ProsesSampah.setFillOver(new java.awt.Color(51, 153, 255));
        btn_ProsesSampah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_ProsesSampah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ProsesSampahActionPerformed(evt);
            }
        });

        btnBatalProses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnBatalProses.setText("Batal");
        btnBatalProses.setFillClick(new java.awt.Color(200, 125, 0));
        btnBatalProses.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnBatalProses.setFillOver(new java.awt.Color(230, 145, 10));
        btnBatalProses.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBatalProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalProsesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTransaksiSampahLayout = new javax.swing.GroupLayout(panelTransaksiSampah);
        panelTransaksiSampah.setLayout(panelTransaksiSampahLayout);
        panelTransaksiSampahLayout.setHorizontalGroup(
            panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelTransaksiSampahLayout.createSequentialGroup()
                        .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txt_Kode, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17)
                                    .addComponent(txt_Nama, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btn_ProsesSampah, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 710, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelTransaksiSampahLayout.createSequentialGroup()
                        .addComponent(btn_SampahMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_SampahKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBatalProses, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cbxJenis_pnView, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txt_Berat, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cbxKategori_pnView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 710, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        panelTransaksiSampahLayout.setVerticalGroup(
            panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_SampahKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_SampahMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBatalProses, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                        .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_Nama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_Kode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(lblTotal))
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxJenis_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxKategori_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_Berat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_ProsesSampah, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(90, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout shadowPanel1Layout = new javax.swing.GroupLayout(shadowPanel1);
        shadowPanel1.setLayout(shadowPanel1Layout);
        shadowPanel1Layout.setHorizontalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTransaksiSampah, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        shadowPanel1Layout.setVerticalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel1Layout.createSequentialGroup()
                .addComponent(panelTransaksiSampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel16.setText("Daftar Harga Sampah/KG");

        btnTambahHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahHarga.setText("Tambah");
        btnTambahHarga.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambahHarga.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambahHarga.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambahHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahHargaActionPerformed(evt);
            }
        });

        btnHapusHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusHarga.setText("Hapus");
        btnHapusHarga.setEnabled(false);
        btnHapusHarga.setFillClick(new java.awt.Color(190, 30, 20));
        btnHapusHarga.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapusHarga.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapusHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapusHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusHargaActionPerformed(evt);
            }
        });

        btnBatalHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnBatalHarga.setText("Batal");
        btnBatalHarga.setFillClick(new java.awt.Color(200, 125, 0));
        btnBatalHarga.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnBatalHarga.setFillOver(new java.awt.Color(230, 145, 10));
        btnBatalHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBatalHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalHargaActionPerformed(evt);
            }
        });

        tblSampah.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID Sampah", "Jenis Sampah", "Kategori Sampah", "Harga Setor/Kg", "Harga Jual/Kg", "Tanggal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSampah.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblSampah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSampahMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSampah);
        if (tblSampah.getColumnModel().getColumnCount() > 0) {
            tblSampah.getColumnModel().getColumn(0).setResizable(false);
            tblSampah.getColumnModel().getColumn(1).setResizable(false);
            tblSampah.getColumnModel().getColumn(2).setResizable(false);
            tblSampah.getColumnModel().getColumn(3).setResizable(false);
            tblSampah.getColumnModel().getColumn(4).setResizable(false);
            tblSampah.getColumnModel().getColumn(5).setResizable(false);
        }

        btnEditHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_edit.png"))); // NOI18N
        btnEditHarga.setText("Edit");
        btnEditHarga.setEnabled(false);
        btnEditHarga.setFillClick(new java.awt.Color(51, 51, 255));
        btnEditHarga.setFillOriginal(new java.awt.Color(102, 204, 255));
        btnEditHarga.setFillOver(new java.awt.Color(102, 102, 255));
        btnEditHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditHargaActionPerformed(evt);
            }
        });

        btnKelola_JK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_edit.png"))); // NOI18N
        btnKelola_JK.setText("Jenis dan Kategori");
        btnKelola_JK.setFillClick(new java.awt.Color(55, 130, 60));
        btnKelola_JK.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnKelola_JK.setFillOver(new java.awt.Color(69, 160, 75));
        btnKelola_JK.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKelola_JK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKelola_JKActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel2.setText("Kelola Sampah :");

        jLabel20.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel20.setText("Kelola Harga :");

        javax.swing.GroupLayout shadowPanel3Layout = new javax.swing.GroupLayout(shadowPanel3);
        shadowPanel3.setLayout(shadowPanel3Layout);
        shadowPanel3Layout.setHorizontalGroup(
            shadowPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel3Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(shadowPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 266, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnKelola_JK, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(77, 77, 77)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnTambahHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnHapusHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBatalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGap(35, 35, 35))
        );
        shadowPanel3Layout.setVerticalGroup(
            shadowPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel3Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(shadowPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(shadowPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBatalHarga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHapusHarga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEditHarga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnKelola_JK, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTambahHarga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(shadowPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1198, Short.MAX_VALUE)
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addComponent(shadowPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelView.add(ShadowUtama, "card2");

        panelMain.add(panelView, "card2");

        panelAdd.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelAdd.setLayout(new java.awt.CardLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel6.setText("Tambah Harga Sampah");

        jLabel11.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel11.setText("Jenis Sampah");

        jLabel12.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel12.setText("Kategori");

        jLabel13.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel13.setText("Harga Setor/Kg");

        txt_HargaAdd.setPreferredSize(new java.awt.Dimension(20, 22));

        btnSimpanHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btnSimpanHarga.setText("Simpan");
        btnSimpanHarga.setFillClick(new java.awt.Color(30, 100, 150));
        btnSimpanHarga.setFillOriginal(new java.awt.Color(41, 128, 185));
        btnSimpanHarga.setFillOver(new java.awt.Color(36, 116, 170));
        btnSimpanHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanHargaActionPerformed(evt);
            }
        });

        btnKembaliT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembaliT.setText("Kembali");
        btnKembaliT.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembaliT.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembaliT.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembaliT.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKembaliT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliTActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel21.setText("Tanggal");

        cbxJenis_pnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnAddActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel23.setText("Harga Jual/Kg");

        txt_HargaAdd2.setPreferredSize(new java.awt.Dimension(20, 22));

        javax.swing.GroupLayout ShadowUtama1Layout = new javax.swing.GroupLayout(ShadowUtama1);
        ShadowUtama1.setLayout(ShadowUtama1Layout);
        ShadowUtama1Layout.setHorizontalGroup(
            ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtama1Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(ShadowUtama1Layout.createSequentialGroup()
                        .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tgl_Add, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                    .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnSimpanHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnKembaliT, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                    .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(cbxJenis_pnAdd, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cbxKategori_pnAdd, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txt_HargaAdd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txt_HargaAdd2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                            .addComponent(jLabel21)
                                            .addGap(913, 913, 913)))
                                    .addGap(169, 169, 169))))
                        .addGap(0, 38, Short.MAX_VALUE))))
        );
        ShadowUtama1Layout.setVerticalGroup(
            ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSimpanHarga, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                    .addComponent(btnKembaliT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxJenis_pnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxKategori_pnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaAdd2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_Add, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(616, Short.MAX_VALUE))
        );

        panelAdd.add(ShadowUtama1, "card2");

        panelMain.add(panelAdd, "card2");

        panelRiwayat.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelRiwayat.setLayout(new java.awt.CardLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel7.setText("Riwayat Setoran");

        btnKembaliR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembaliR.setText("Kembali");
        btnKembaliR.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembaliR.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembaliR.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembaliR.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKembaliR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliRActionPerformed(evt);
            }
        });

        table2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(table2);

        ShadowSearch1.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch1.setPreferredSize(new java.awt.Dimension(259, 43));

        jLabel9.setBackground(new java.awt.Color(204, 204, 204));
        jLabel9.setForeground(new java.awt.Color(204, 204, 204));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_search.png"))); // NOI18N
        jLabel9.setText("Search");

        javax.swing.GroupLayout ShadowSearch1Layout = new javax.swing.GroupLayout(ShadowSearch1);
        ShadowSearch1.setLayout(ShadowSearch1Layout);
        ShadowSearch1Layout.setHorizontalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearch1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel9)
                .addContainerGap(180, Short.MAX_VALUE))
        );
        ShadowSearch1Layout.setVerticalGroup(
            ShadowSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowSearch1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnFirstPage.setText("First Page");
        btnFirstPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstPageActionPerformed(evt);
            }
        });

        btnPrevious.setText("<");
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });

        cbxPage.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnNext.setText(">");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnLastPage.setText("Last Page");
        btnLastPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastPageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ShadowUtama2Layout = new javax.swing.GroupLayout(ShadowUtama2);
        ShadowUtama2.setLayout(ShadowUtama2Layout);
        ShadowUtama2Layout.setHorizontalGroup(
            ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ShadowUtama2Layout.createSequentialGroup()
                        .addComponent(btnFirstPage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrevious)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLastPage))
                    .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane2)
                        .addGroup(ShadowUtama2Layout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addGap(603, 603, 603)
                            .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnKembaliR, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 37, Short.MAX_VALUE))
        );
        ShadowUtama2Layout.setVerticalGroup(
            ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembaliR, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 806, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLastPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFirstPage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbxPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelRiwayat.add(ShadowUtama2, "card2");

        panelMain.add(panelRiwayat, "card2");

        panelJK.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelJK.setLayout(new java.awt.CardLayout());

        jLabel24.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel24.setText("Jenis Sampah");

        txt_Jenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_JenisActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel22.setText("Kelola Jenis dan Kategori Sampah");

        btnKembaliJK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembaliJK.setText("Kembali");
        btnKembaliJK.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembaliJK.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembaliJK.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembaliJK.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKembaliJK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliJKActionPerformed(evt);
            }
        });

        btnTambahJenis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahJenis.setText("Tambah");
        btnTambahJenis.setFillClick(new java.awt.Color(0, 153, 0));
        btnTambahJenis.setFillOriginal(new java.awt.Color(0, 204, 0));
        btnTambahJenis.setFillOver(new java.awt.Color(51, 255, 51));
        btnTambahJenis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahJenisActionPerformed(evt);
            }
        });

        btnHapusJenis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusJenis.setText("Hapus");
        btnHapusJenis.setFillClick(new java.awt.Color(153, 0, 0));
        btnHapusJenis.setFillOriginal(new java.awt.Color(255, 0, 0));
        btnHapusJenis.setFillOver(new java.awt.Color(255, 51, 51));
        btnHapusJenis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapusJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusJenisActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel25.setText("Jenis Sampah");

        txt_Kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_KategoriActionPerformed(evt);
            }
        });

        btnTambahKategori.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahKategori.setText("Tambah");
        btnTambahKategori.setFillClick(new java.awt.Color(0, 153, 0));
        btnTambahKategori.setFillOriginal(new java.awt.Color(0, 204, 0));
        btnTambahKategori.setFillOver(new java.awt.Color(51, 255, 51));
        btnTambahKategori.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKategoriActionPerformed(evt);
            }
        });

        btnHapusKategori.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusKategori.setText("Hapus");
        btnHapusKategori.setFillClick(new java.awt.Color(153, 0, 0));
        btnHapusKategori.setFillOriginal(new java.awt.Color(255, 0, 0));
        btnHapusKategori.setFillOver(new java.awt.Color(255, 51, 51));
        btnHapusKategori.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapusKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusKategoriActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Mongolian Baiti", 0, 12)); // NOI18N
        jLabel26.setText("Kategori Sampah");

        cbxJenis_pnJK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnJKActionPerformed(evt);
            }
        });

        tblJenis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ID Jenis", "Jenis Sampah"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblJenis.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblJenis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblJenisMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblJenis);
        if (tblJenis.getColumnModel().getColumnCount() > 0) {
            tblJenis.getColumnModel().getColumn(0).setResizable(false);
            tblJenis.getColumnModel().getColumn(1).setResizable(false);
            tblJenis.getColumnModel().getColumn(1).setHeaderValue("Jenis Sampah");
        }

        tblKategori.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID Kategori", "Kategori Sampah", "Jenis Sampah"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKategori.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblKategori.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKategoriMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblKategori);
        if (tblKategori.getColumnModel().getColumnCount() > 0) {
            tblKategori.getColumnModel().getColumn(0).setResizable(false);
            tblKategori.getColumnModel().getColumn(1).setResizable(false);
            tblKategori.getColumnModel().getColumn(2).setResizable(false);
        }

        javax.swing.GroupLayout ShadowUtama4Layout = new javax.swing.GroupLayout(ShadowUtama4);
        ShadowUtama4.setLayout(ShadowUtama4Layout);
        ShadowUtama4Layout.setHorizontalGroup(
            ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama4Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtama4Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 663, Short.MAX_VALUE)
                        .addComponent(btnKembaliJK, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63))
                    .addGroup(ShadowUtama4Layout.createSequentialGroup()
                        .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_Kategori, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24)
                            .addComponent(jLabel25)
                            .addComponent(jLabel26)
                            .addComponent(cbxJenis_pnJK, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ShadowUtama4Layout.createSequentialGroup()
                                .addComponent(btnTambahJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapusJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txt_Jenis, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ShadowUtama4Layout.createSequentialGroup()
                                .addComponent(btnTambahKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapusKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(143, 143, 143)
                        .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        ShadowUtama4Layout.setVerticalGroup(
            ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama4Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembaliJK, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtama4Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_Jenis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTambahJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnHapusJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtama4Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(1, 1, 1)
                        .addComponent(cbxJenis_pnJK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel26)
                        .addGap(4, 4, 4)
                        .addComponent(txt_Kategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(ShadowUtama4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTambahKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnHapusKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(511, Short.MAX_VALUE))
        );

        panelJK.add(ShadowUtama4, "card2");

        panelMain.add(panelJK, "card2");

        panelEdit.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelEdit.setLayout(new java.awt.CardLayout());

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel8.setText("Edit Harga Sampah");

        jLabel14.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel14.setText("Jenis Sampah");

        jLabel18.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel18.setText("Kategori");

        jLabel19.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel19.setText("Harga Setor/Kg");

        txt_HargaEdit.setPreferredSize(new java.awt.Dimension(20, 22));

        btnSimpanEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btnSimpanEdit.setText("Simpan");
        btnSimpanEdit.setFillClick(new java.awt.Color(30, 100, 150));
        btnSimpanEdit.setFillOriginal(new java.awt.Color(41, 128, 185));
        btnSimpanEdit.setFillOver(new java.awt.Color(36, 116, 170));
        btnSimpanEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanEditActionPerformed(evt);
            }
        });

        btnKembaliE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembaliE.setText("Kembali");
        btnKembaliE.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembaliE.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembaliE.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembaliE.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKembaliE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliEActionPerformed(evt);
            }
        });

        cbxJenis_pnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnEditActionPerformed(evt);
            }
        });

        txt_HargaEdit2.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel27.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel27.setText("Harga Jual/Kg");

        jLabel28.setFont(new java.awt.Font("Mongolian Baiti", 0, 14)); // NOI18N
        jLabel28.setText("Tanggal");

        javax.swing.GroupLayout ShadowUtama3Layout = new javax.swing.GroupLayout(ShadowUtama3);
        ShadowUtama3.setLayout(ShadowUtama3Layout);
        ShadowUtama3Layout.setHorizontalGroup(
            ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama3Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ShadowUtama3Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(ShadowUtama3Layout.createSequentialGroup()
                        .addGroup(ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tgl_Edit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(ShadowUtama3Layout.createSequentialGroup()
                                    .addComponent(jLabel8)
                                    .addGap(720, 720, 720)
                                    .addComponent(btnSimpanEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnKembaliE, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cbxJenis_pnEdit, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbxKategori_pnEdit, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txt_HargaEdit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txt_HargaEdit2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(ShadowUtama3Layout.createSequentialGroup()
                                    .addComponent(jLabel28)
                                    .addGap(913, 913, 913))))
                        .addGap(0, 81, Short.MAX_VALUE))))
        );
        ShadowUtama3Layout.setVerticalGroup(
            ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama3Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembaliE, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .addComponent(btnSimpanEdit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxJenis_pnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxKategori_pnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaEdit2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_Edit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(616, Short.MAX_VALUE))
        );

        panelEdit.add(ShadowUtama3, "card2");

        panelMain.add(panelEdit, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanHargaActionPerformed

        String jenis = cbxJenis_pnAdd.getSelectedItem().toString();
        String kategori = cbxKategori_pnAdd.getSelectedItem().toString();
        String hargaS = txt_HargaAdd.getText();
        String hargaJ = txt_HargaAdd2.getText();

        Date date = tgl_Add.getSelectedDate().getTime();// ini ambil tanggal dari komponen
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tgl = sdf.format(date);

        if (jenis.isEmpty() || kategori.isEmpty() || hargaS.isEmpty() || hargaJ.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
        try {
            String sql = "INSERT INTO sampah (harga_setor, harga_jual, tanggal, id_kategori) "
                    + "VALUES (?, ?, ?, (SELECT id_kategori FROM kategori_sampah WHERE nama_kategori = ?))";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, hargaS);
            pst.setString(2, hargaJ);
            pst.setString(3, tgl);
            pst.setString(4, kategori);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Harga Sampah Berhasil di Tambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Menambahkan Data harga Sampah: " + e.getMessage());
        }

    }//GEN-LAST:event_btnSimpanHargaActionPerformed

    private void btnKembaliTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliTActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliTActionPerformed

    private void btnTambahHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahHargaActionPerformed
        panelMain.removeAll();
        panelMain.add(panelAdd);
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btnTambahHargaActionPerformed

    private void btnBatalHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalHargaActionPerformed
        showPanel();
    }//GEN-LAST:event_btnBatalHargaActionPerformed

    private void tblSampahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSampahMouseClicked
        int selectedRow = tblSampah.getSelectedRow(); // Mendapatkan indeks baris yang diklik

        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            btnTambahHarga.setEnabled(false);
            btnHapusHarga.setEnabled(true);
            btnEditHarga.setEnabled(true);

            selectedIdSampah = Integer.parseInt(tblSampah.getValueAt(selectedRow, 0).toString());

            cbxJenis_pnEdit.setSelectedItem(tblSampah.getValueAt(selectedRow, 1).toString());
            cbxKategori_pnEdit.setSelectedItem(tblSampah.getValueAt(selectedRow, 2).toString());
            txt_HargaEdit.setText(tblSampah.getValueAt(selectedRow, 3).toString());
            txt_HargaEdit2.setText(tblSampah.getValueAt(selectedRow, 4).toString());

        }
    }//GEN-LAST:event_tblSampahMouseClicked

    private void btnHapusHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusHargaActionPerformed
        int selectedRow = tblSampah.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Pilih data yang ingin dihapus!");
            return;
        }

        // Ambil kode produk dari tabel
        String idSampah = (String) tblSampah.getValueAt(selectedRow, 0);
        String kategori = (String) tblSampah.getValueAt(selectedRow, 2);

        // Konfirmasi penghapusan
        int confirm = JOptionPane.showConfirmDialog(null,
                "Apakah Anda yakin ingin menghapus harga sampah: " + kategori + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Hapus data terkait terlebih dahulu dari tabel lain
            String deleteHargaSampah = "DELETE FROM sampah WHERE id_sampah = ?";

            try (PreparedStatement pstmtDeleteFromSampah = conn.prepareStatement(deleteHargaSampah)) {
                pstmtDeleteFromSampah.setString(1, idSampah);
                pstmtDeleteFromSampah.executeUpdate();

                inisialisasiTabel(); ///////tambah

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saat menghapus data harga sampah: " + e.getMessage());
                return;
            }
        }
    }//GEN-LAST:event_btnHapusHargaActionPerformed

    private void btnEditHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditHargaActionPerformed
        panelMain.removeAll();
        panelMain.add(panelEdit);
        panelMain.repaint();
        panelMain.revalidate();

    }//GEN-LAST:event_btnEditHargaActionPerformed

    private void btnSimpanEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanEditActionPerformed
        int selectedRow = tblSampah.getSelectedRow(); // Mendapatkan indeks baris yang diklik
        if (selectedRow >= 0) { // Pastikan ada baris yang dipilih
            // Ambil nilai dari baris yang dipilih dan masukkan ke text field
        }

        String jenis = cbxJenis_pnEdit.getSelectedItem().toString();
        String kategori = cbxKategori_pnEdit.getSelectedItem().toString();
        String harga = txt_HargaEdit.getText();
        String harga2 = txt_HargaEdit2.getText();

        Date date = tgl_Edit.getSelectedDate().getTime();// ini ambil tanggal dari komponen
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tgl = sdf.format(date);

        if (jenis.isEmpty() || kategori.isEmpty() || harga.isEmpty() || harga2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedIdSampah == -1) {
            JOptionPane.showMessageDialog(this, "ID sampah tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String sql = "UPDATE sampah SET  harga_setor = ?, harga_jual = ?, tanggal = ?, "
                    + "id_kategori = (SELECT id_kategori FROM kategori_sampah WHERE nama_kategori = ?) WHERE id_sampah = ?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, harga);
            pst.setString(2, harga2);
            pst.setString(3, tgl);
            pst.setString(4, kategori);
            pst.setInt(5, selectedIdSampah);
            // Eksekusi update
            System.out.println("Harga yang akan diupdate: " + harga);

            int rowsUpdated = pst.executeUpdate();
            pst.close();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                clearForm();     // Bersihkan form
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Data harga harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanEditActionPerformed

    private void btnKembaliEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliEActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliEActionPerformed

    private void btnLastPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastPageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLastPageActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPreviousActionPerformed

    private void btnFirstPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstPageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFirstPageActionPerformed

    private void btnKembaliRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliRActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliRActionPerformed

    private void btnKelola_JKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKelola_JKActionPerformed
        panelMain.removeAll();
        panelMain.add(panelJK);
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btnKelola_JKActionPerformed

    private void btnKembaliJKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliJKActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliJKActionPerformed

    private void txt_JenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_JenisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_JenisActionPerformed

    private void btnTambahJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahJenisActionPerformed
        try {
            String jenis = txt_Jenis.getText();

            String sql = "INSERT INTO jenis_sampah (nama_jenis) "
                    + "VALUES (?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, jenis);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Jenis Sampah Tersimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTabelJenis();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Menyimpan Jenis Sampah.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnTambahJenisActionPerformed

    private void btnHapusJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusJenisActionPerformed
        try {
            String jenis = txt_Jenis.getText();

            String sql = "DELETE  FROM jenis_sampah WHERE jenis_sampah.nama_jenis = ? ";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jenis);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Berhasil Menghapus Jenis Sampah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadTabelJenis();
            clearForm();     // Bersihkan form

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal Menghapus Jenis Sampah.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnHapusJenisActionPerformed


    private void cbxJenis_pnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxJenis_pnAddActionPerformed
        if (cbxJenis_pnAdd.getSelectedIndex() > 0) { // Jika bukan item pertama ("-- Pilih --")
            String selectedJenis = cbxJenis_pnAdd.getSelectedItem().toString();
            loadKategoriByJenis(selectedJenis);
        } else {
            cbxKategori_pnAdd.removeAllItems();
            cbxKategori_pnAdd.addItem("-- Pilih Kategori --");
        }
    }//GEN-LAST:event_cbxJenis_pnAddActionPerformed

    private void btn_SampahKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SampahKeluarActionPerformed
        lastButtonClicked = "jual";
        txt_Kode.setEditable(false);
        txt_Nama.setEditable(false);

    }//GEN-LAST:event_btn_SampahKeluarActionPerformed

    private void cariNamaNasabah(String kode) {
        try {
            String sql = "SELECT nama_nasabah FROM manajemen_nasabah WHERE id_nasabah = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_nasabah");
                txt_Nama.setText(nama);
            } else {
                txt_Nama.setText(""); // Kosongkan kalau tidak ketemu
                JOptionPane.showMessageDialog(null, "Nasabah tidak ditemukan.");
            }

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat mencari data nasabah.");
        }
    }


    private void txt_KodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_KodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String kode = txt_Kode.getText();
            cariNamaNasabah(kode);
        }
    }//GEN-LAST:event_txt_KodeKeyPressed

    private void txt_KodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_KodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_KodeActionPerformed

    private void btn_SampahMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SampahMasukActionPerformed
        lastButtonClicked = "setor";
        txt_Kode.setEditable(true);
        txt_Nama.setEditable(true);
    }//GEN-LAST:event_btn_SampahMasukActionPerformed

    private void txt_BeratActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_BeratActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_BeratActionPerformed

    private void cbxJenis_pnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxJenis_pnViewActionPerformed
        if (cbxJenis_pnView.getSelectedIndex() > 0) { // Jika bukan item pertama ("-- Pilih --")
            String selectedJenis = cbxJenis_pnView.getSelectedItem().toString();
            loadKategoriByJenis(selectedJenis);
        } else {
            cbxKategori_pnView.removeAllItems();
            cbxKategori_pnView.addItem("-- Pilih Kategori --");
        }
    }//GEN-LAST:event_cbxJenis_pnViewActionPerformed

    private void txt_NamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_NamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_NamaActionPerformed

    private void cbxJenis_pnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxJenis_pnEditActionPerformed
        if (cbxJenis_pnEdit.getSelectedIndex() > 0) { // Jika bukan item pertama ("-- Pilih --")
            String selectedJenis = cbxJenis_pnEdit.getSelectedItem().toString();
            loadKategoriByJenis(selectedJenis);
        } else {
            cbxKategori_pnEdit.removeAllItems();
            cbxKategori_pnEdit.addItem("-- Pilih Kategori --");
        }
    }//GEN-LAST:event_cbxJenis_pnEditActionPerformed

    private void txt_KategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_KategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_KategoriActionPerformed

    private void btnTambahKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKategoriActionPerformed
        String kategori = txt_Kategori.getText();
        String jenis = cbxJenis_pnJK.getSelectedItem().toString();

        String query = "INSERT INTO kategori_sampah (nama_kategori, id_jenis) "
                + "VALUES (?, (SELECT id_jenis FROM jenis_sampah WHERE nama_jenis = ?))";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, kategori);
            pstmt.setString(2, jenis);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Kategori berhasil ditambahkan!");

            clearForm();  // Bersihkan form
            loadTabelKategori();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnTambahKategoriActionPerformed

    private void btnHapusKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusKategoriActionPerformed
        String kategori = txt_Kategori.getText();
        String jenis = cbxJenis_pnJK.getSelectedItem().toString();

        String query = "DELETE FROM kategori (nama_kategori, id_jenis) "
                + "VALUES (?, (SELECT id_jenis FROM jenis WHERE nama_jenis = ?))";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, kategori);
            pstmt.setString(2, jenis);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Kategori berhasil dihapus!");

            clearForm();  // Bersihkan form
            loadTabelKategori();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnHapusKategoriActionPerformed

    private void cbxJenis_pnJKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxJenis_pnJKActionPerformed

    }//GEN-LAST:event_cbxJenis_pnJKActionPerformed

    private void tblJenisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblJenisMouseClicked
        int row = tblJenis.rowAtPoint(evt.getPoint());
        int col = tblJenis.columnAtPoint(evt.getPoint());

        // Jika klik pada kolom nama (kolom index 1)
        if (col == 1 && row != -1) {
            String value = (String) tblJenis.getValueAt(row, col);
            txt_Jenis.setText(value);
        }
    }//GEN-LAST:event_tblJenisMouseClicked

    private void tblKategoriMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKategoriMouseClicked
        int selectedRow = tblKategori.getSelectedRow(); // Mendapatkan indeks baris yang diklik
        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            // Ambil nilai dari baris yang dipilih dan masukkan ke text field
            txt_Kategori.setText(tblKategori.getValueAt(selectedRow, 1).toString());
            cbxJenis_pnJK.setSelectedItem(tblKategori.getValueAt(selectedRow, 2).toString());
        }
    }//GEN-LAST:event_tblKategoriMouseClicked

    private void btn_ProsesSampahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ProsesSampahActionPerformed
        try {
            String kode = txt_Kode.getText(); // id_nasabah
            String namaJenis = cbxJenis_pnView.getSelectedItem().toString();
            String namaKategori = cbxKategori_pnView.getSelectedItem().toString();
            String strBerat = txt_Berat.getText();

            double berat = Double.parseDouble(strBerat);

            // Ambil ID Kategori
            String idKategori = "";
            String queryKategori = "SELECT id_kategori FROM kategori_sampah WHERE nama_kategori = ?";
            PreparedStatement psKategori = conn.prepareStatement(queryKategori);
            psKategori.setString(1, namaKategori);
            ResultSet rsKategori = psKategori.executeQuery();
            if (rsKategori.next()) {
                idKategori = rsKategori.getString("id_kategori");
            } else {
                JOptionPane.showMessageDialog(null, "Kategori tidak ditemukan.");
                return;
            }

            //Query Memanggil Saldo
            double saldoTerakhir = 0;
            String querySaldo = "SELECT saldo_nasabah FROM setor_sampah WHERE id_nasabah = ? ORDER BY tanggal DESC LIMIT 1";
            PreparedStatement psSaldo = conn.prepareStatement(querySaldo);
            psSaldo.setString(1, kode);
            ResultSet rsSaldo = psSaldo.executeQuery();
            if (rsSaldo.next()) {
                saldoTerakhir = rsSaldo.getDouble("saldo_nasabah");
            }

            if (lastButtonClicked.equals("setor")) {
                // QUERY UNTUK SETOR
                if (kode.isEmpty() || namaJenis.isEmpty() || namaKategori.isEmpty() || strBerat.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Harap lengkapi semua data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String querySampah = "SELECT id_sampah, harga_setor FROM sampah WHERE id_kategori = ?";
                PreparedStatement ps = conn.prepareStatement(querySampah);
                ps.setString(1, idKategori);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String id_sampah = rs.getString("id_sampah");
                    int harga = rs.getInt("harga_setor");
                    double total = harga * berat;
                    double saldoBaru = saldoTerakhir + total;

                    String insert = "INSERT INTO setor_sampah (id_nasabah, id_sampah, berat_sampah, harga, saldo_nasabah, id_user, tanggal ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE())";
                    PreparedStatement insertPs = conn.prepareStatement(insert);
                    insertPs.setString(1, kode);
                    insertPs.setString(2, id_sampah);
                    insertPs.setDouble(3, berat);
                    insertPs.setDouble(4, total);
                    insertPs.setDouble(5, saldoBaru);
                    insertPs.setInt(6, id_user);
                    insertPs.executeUpdate();

                    String updateSaldo = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                    PreparedStatement psUpdateSaldo = conn.prepareStatement(updateSaldo);
                    psUpdateSaldo.setDouble(1, saldoBaru);
                    psUpdateSaldo.setString(2, kode);
                    psUpdateSaldo.executeUpdate();

                    lblTotal.setText("Rp " + String.format("%,.2f", total));

                    String namaNasabah = "SELECT nama_nasabah FROM manajemen_nasabah WHERE id_nasabah = ?";
                    PreparedStatement psNasabah = conn.prepareStatement(namaNasabah);
                    psNasabah.setString(1, kode);
                    ResultSet rsNama = psNasabah.executeQuery();
                    String namaNasabahStr = kode; // fallback
                    if (rsNama.next()) {
                        namaNasabahStr = rsNama.getString("nama_nasabah");
                    }

                    int result = JOptionPane.showConfirmDialog(null,
                            "SETOR SAMPAH BERHASIL!\nTotal Harga: Rp " + String.format("%,.2f", total)
                            + "\nSaldo " + namaNasabahStr + " Bertambah Menjadi: Rp " + String.format("%,.2f", saldoBaru),
                            "Sukses",
                            JOptionPane.DEFAULT_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        lblTotal.setText("0");
                        clearForm();
                        loadJenisSampah();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Data sampah tidak ditemukan untuk setor.");
                }

            } else if (lastButtonClicked.equals("jual")) {
                // QUERY UNTUK JUAL
                if (namaJenis.isEmpty() || namaKategori.isEmpty() || strBerat.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Harap lengkapi semua data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String querySampah = "SELECT id_sampah, harga_jual FROM sampah WHERE id_kategori = ?";
                PreparedStatement ps = conn.prepareStatement(querySampah);
                ps.setString(1, idKategori);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String id_sampah = rs.getString("id_sampah");
                    int harga = rs.getInt("harga_jual");
                    double total = harga * berat;

                    String insert = "INSERT INTO jual_sampah (id_sampah, berat_sampah, harga, id_user, tanggal) VALUES (?, ?, ?, ?, CURRENT_DATE())";
                    PreparedStatement insertPs = conn.prepareStatement(insert);
                    insertPs.setString(1, id_sampah);
                    insertPs.setDouble(2, berat);
                    insertPs.setDouble(3, total);
                    insertPs.setInt(4, id_user);
                    insertPs.executeUpdate();

                    lblTotal.setText("Rp " + String.format("%,.2f", total));
                    int result = JOptionPane.showConfirmDialog(null, "TRANSAKSI JUAL SAMPAH BERHASIL!\nTotal Harga: Rp " + String.format("%,.2f", total), "Sukses", JOptionPane.DEFAULT_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        lblTotal.setText("0");
                        clearForm();
                        loadJenisSampah();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Data sampah tidak ditemukan untuk jual.");
                }

            } else {
                // BELUM PILIH TRANSAKSI
                JOptionPane.showMessageDialog(null, "Pilih dulu jenis transaksi: Setor atau Jual!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + ex.getMessage());
        }
    }//GEN-LAST:event_btn_ProsesSampahActionPerformed

    private void btnBatalProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalProsesActionPerformed
        showPanel();
    }//GEN-LAST:event_btnBatalProsesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch1;
    private component.ShadowPanel ShadowUtama;
    private component.ShadowPanel ShadowUtama1;
    private component.ShadowPanel ShadowUtama2;
    private component.ShadowPanel ShadowUtama3;
    private component.ShadowPanel ShadowUtama4;
    private component.Jbutton btnBatalHarga;
    private component.Jbutton btnBatalProses;
    private component.Jbutton btnEditHarga;
    private javax.swing.JButton btnFirstPage;
    private component.Jbutton btnHapusHarga;
    private component.Jbutton btnHapusJenis;
    private component.Jbutton btnHapusKategori;
    private component.Jbutton btnKelola_JK;
    private component.Jbutton btnKembaliE;
    private component.Jbutton btnKembaliJK;
    private component.Jbutton btnKembaliR;
    private component.Jbutton btnKembaliT;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private component.Jbutton btnSimpanEdit;
    private component.Jbutton btnSimpanHarga;
    private component.Jbutton btnTambahHarga;
    private component.Jbutton btnTambahJenis;
    private component.Jbutton btnTambahKategori;
    private component.Jbutton btn_ProsesSampah;
    private component.Jbutton btn_SampahKeluar;
    private component.Jbutton btn_SampahMasuk;
    private javax.swing.JComboBox<String> cbxJenis_pnAdd;
    private javax.swing.JComboBox<String> cbxJenis_pnEdit;
    private javax.swing.JComboBox<String> cbxJenis_pnJK;
    private javax.swing.JComboBox<String> cbxJenis_pnView;
    private javax.swing.JComboBox<String> cbxKategori_pnAdd;
    private javax.swing.JComboBox<String> cbxKategori_pnEdit;
    private javax.swing.JComboBox<String> cbxKategori_pnView;
    private javax.swing.JComboBox<String> cbxPage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelAdd;
    private javax.swing.JPanel panelEdit;
    private javax.swing.JPanel panelJK;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRiwayat;
    private component.ShadowPanel panelTransaksiSampah;
    private javax.swing.JPanel panelView;
    private component.ShadowPanel shadowPanel1;
    private component.ShadowPanel shadowPanel3;
    private component.Table table2;
    private component.Table tblJenis;
    private component.Table tblKategori;
    private component.Table tblSampah;
    private datechooser.beans.DateChooserCombo tgl_Add;
    private datechooser.beans.DateChooserCombo tgl_Edit;
    private javax.swing.JTextField txt_Berat;
    private javax.swing.JTextField txt_HargaAdd;
    private javax.swing.JTextField txt_HargaAdd2;
    private javax.swing.JTextField txt_HargaEdit;
    private javax.swing.JTextField txt_HargaEdit2;
    private javax.swing.JTextField txt_Jenis;
    private javax.swing.JTextField txt_Kategori;
    private javax.swing.JTextField txt_Kode;
    private javax.swing.JTextField txt_Nama;
    // End of variables declaration//GEN-END:variables

    private void loadJenisSampah() {
        try {
            String sql = "SELECT nama_jenis FROM jenis_sampah ORDER BY nama_jenis";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            cbxJenis_pnView.removeAllItems();
            cbxJenis_pnView.addItem("-- Pilih Jenis --");

            cbxJenis_pnAdd.removeAllItems();
            cbxJenis_pnAdd.addItem("-- Pilih Jenis --");

            cbxJenis_pnEdit.removeAllItems();
            cbxJenis_pnEdit.addItem("-- Pilih Jenis --");

            cbxJenis_pnJK.removeAllItems();
            cbxJenis_pnJK.addItem("-- Pilih Jenis --");

            while (rs.next()) {
                cbxJenis_pnView.addItem(rs.getString("nama_jenis"));
                cbxJenis_pnAdd.addItem(rs.getString("nama_jenis"));
                cbxJenis_pnEdit.addItem(rs.getString("nama_jenis"));
                cbxJenis_pnJK.addItem(rs.getString("nama_jenis"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load jenis sampah: " + e.getMessage());
        }
    }

    // Method untuk load kategori berdasarkan jenis (akan dipanggil oleh event handler)
    private void loadKategoriByJenis(String jenisSampah) {
        try {
            // Dapatkan dulu id_jenis dari nama jenis yang dipilih
            String idQuery = "SELECT id_jenis FROM jenis_sampah WHERE nama_jenis = ?";
            PreparedStatement idStmt = conn.prepareStatement(idQuery);
            idStmt.setString(1, jenisSampah);
            ResultSet idRs = idStmt.executeQuery();

            if (idRs.next()) {
                int idJenis = idRs.getInt("id_jenis");

                // Query kategori berdasarkan id_jenis
                String katQuery = "SELECT nama_kategori FROM kategori_sampah WHERE id_jenis = ? ORDER BY nama_kategori";
                PreparedStatement katStmt = conn.prepareStatement(katQuery);
                katStmt.setInt(1, idJenis);
                ResultSet katRs = katStmt.executeQuery();

                cbxKategori_pnAdd.removeAllItems();
                cbxKategori_pnAdd.addItem("-- Pilih Kategori --");

                cbxKategori_pnEdit.removeAllItems();
                cbxKategori_pnEdit.addItem("-- Pilih Kategori --");

                cbxKategori_pnView.removeAllItems();
                cbxKategori_pnView.addItem("-- Pilih Kategori --");

                while (katRs.next()) {
                    cbxKategori_pnAdd.addItem(katRs.getString("nama_kategori"));
                    cbxKategori_pnEdit.addItem(katRs.getString("nama_kategori"));
                    cbxKategori_pnView.addItem(katRs.getString("nama_kategori"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load kategori: " + e.getMessage());
        }
    }

}
