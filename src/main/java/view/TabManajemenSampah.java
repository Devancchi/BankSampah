package view;

import component.LoggerUtil;
import component.UserSession;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.DBconnect;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import component.ExcelExporter;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class TabManajemenSampah extends javax.swing.JPanel {

    private int id_user;
    private final Connection conn = DBconnect.getConnection();
    private DefaultTableModel tblModel;
    private int selectedIdSampah = -1; // default -1 berarti belum ada yang dipilih
    private String lastButtonClicked = ""; // global di luar method, misalnya di class kamu
    UserSession users;

    // --- PAGINATION VARIABEL ---
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 5;
    private int totalPages;
    private int totalData;

    public TabManajemenSampah(UserSession user) {
        this.users = user;
        initComponents();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    txt_Kode.requestFocusInWindow();
                    txt_Kode.requestFocus();
                });
            }
        });

        // Add document listener to automatically trigger Enter key
        txt_Kode.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!txt_Kode.getText().isEmpty()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        txt_KodeActionPerformed(new java.awt.event.ActionEvent(txt_Kode,
                                java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
                    });
                }
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        // Inisialisasi pagination
        dataPerHalaman = Integer.parseInt(cbx_data2.getSelectedItem().toString());
        calculateTotalPageHarga();
        loadtabelSampah();
        // Listener pagination
        btn_first2.addActionListener(e -> {
            halamanSaatIni = 1;
            loadtabelSampah();
        });
        btn_before2.addActionListener(e -> {
            if (halamanSaatIni > 1) {
                halamanSaatIni--;
                loadtabelSampah();
            }
        });
        btn_next2.addActionListener(e -> {
            if (halamanSaatIni < totalPages) {
                halamanSaatIni++;
                loadtabelSampah();
            }
        });
        btn_last2.addActionListener(e -> {
            halamanSaatIni = totalPages;
            loadtabelSampah();
        });
        cbx_data2.addActionListener(e -> {
            dataPerHalaman = Integer.parseInt(cbx_data2.getSelectedItem().toString());
            halamanSaatIni = 1;
            calculateTotalPageHarga();
            loadtabelSampah();
        });
        /// load combo box jenis sampah ///
        loadJenisSampah();
        /// load tabel ////
        loadTabelKategori();
        loadTabelJenis();
        btnHapusHarga.setVisible(false);
        btnBatalHarga.setVisible(false);
        btnBatalProses.setVisible(false);
    }

    public component.PlaceholderTextField getTxt_Kode() {
        return txt_Kode;
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabManajemenSampah(users));
        panelMain.repaint();
        panelMain.revalidate();
    }

    private void calculateTotalPageHarga() {
        try {
            String sql = "SELECT COUNT(*) as total FROM sampah s JOIN kategori_sampah k ON s.id_kategori = k.id_kategori JOIN jenis_sampah j ON k.id_jenis = j.id_jenis";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                totalData = rs.getInt("total");
                totalPages = (int) Math.ceil((double) totalData / dataPerHalaman);
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadtabelSampah() {
        // Mengatur model tabel
        tblModel = new DefaultTableModel(new String[] { "ID Sampah", "Jenis Sampah", "Kategori Sampah",
                "Harga Setor/Kg", "Harga Jual/Kg", "Tanggal", "Stok" }, 0);
        tblSampah.setModel(tblModel);
        tblSampah.getColumnModel().getColumn(0).setMinWidth(0);
        tblSampah.getColumnModel().getColumn(0).setMaxWidth(0);
        tblSampah.getColumnModel().getColumn(0).setWidth(0);

        try {
            int startIndex = (halamanSaatIni - 1) * dataPerHalaman;
            String sql = "SELECT s.id_sampah, s.harga_setor, s.harga_jual, s.tanggal,  k.nama_kategori,  j.nama_jenis, s.stok_sampah "
                    + "FROM sampah s "
                    + "JOIN kategori_sampah k ON s.id_kategori = k.id_kategori "
                    + "JOIN jenis_sampah j ON k.id_jenis = j.id_jenis "
                    + "ORDER BY s.id_sampah LIMIT ? OFFSET ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, dataPerHalaman);
            stmt.setInt(2, startIndex);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                        rs.getString("id_sampah"),
                        rs.getString("nama_jenis"),
                        rs.getString("nama_kategori"),
                        rs.getInt("harga_setor"),
                        rs.getInt("harga_jual"),
                        rs.getDate("tanggal"),
                        String.format("%.2f", rs.getDouble("stok_sampah")).replace(",", ".")
                };
                tblModel.addRow(row);
            }
            rs.close();
            stmt.close();
            // Update page label
            lb_halaman2.setText("Halaman " + halamanSaatIni + " dari total " + totalData + " data");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void loadTabelKategori() {
        // Mengatur model tabel
        tblModel = new DefaultTableModel(new String[] { "ID Kategori", "Kategori Sampah", "Jenis Sampah" }, 0);
        tblKategori.setModel(tblModel);
        tblKategori.getColumnModel().getColumn(0).setMinWidth(0);
        tblKategori.getColumnModel().getColumn(0).setMaxWidth(0);
        tblKategori.getColumnModel().getColumn(0).setWidth(0);

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT k.id_kategori, k.nama_kategori,  j.nama_jenis "
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
        tblModel = new DefaultTableModel(new String[] { "ID Jenis", "Jenis Sampah" }, 0);
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

        txt_HargaAdd2.setText("");

        txt_Jenis.setText("");
        txt_Kategori.setText("");

        txt_Kode.setText("");
        txt_Nama.setText("");

        // cbxJenis_pnAdd.removeAllItems();
        // cbxJenis_pnAdd.addItem("-- Pilih Kategori --");
        //
        // cbxKategori_pnAdd.removeAllItems();
        // cbxKategori_pnAdd.addItem("-- Pilih Kategori --");
        //
        // cbxJenis_pnJK.removeAllItems();
        // cbxJenis_pnJK.addItem("-- Pilih Kategori --");
        //
        // cbxJenis_pnView.removeAllItems();
        // cbxJenis_pnView.addItem("-- Pilih Kategori --");
        //
        // cbxKategori_pnView.removeAllItems();
        // cbxKategori_pnView.addItem("-- Pilih Kategori --");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        panelHarga = new component.ShadowPanel();
        jLabel16 = new javax.swing.JLabel();
        btnTambahHarga = new component.Jbutton();
        btnBatalHarga = new component.Jbutton();
        btnKelola_JK = new component.Jbutton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSampah = new component.Table();
        txt_search = new component.PlaceholderTextField();
        panelBawah2 = new component.ShadowPanel();
        lb_halaman2 = new javax.swing.JLabel();
        btn_before2 = new javax.swing.JButton();
        cbx_data2 = new javax.swing.JComboBox<>();
        btn_next2 = new javax.swing.JButton();
        btn_last2 = new javax.swing.JButton();
        btn_first2 = new javax.swing.JButton();
        btn_Export2 = new component.Jbutton();
        btn_import2 = new component.Jbutton();
        btnHapusHarga = new component.Jbutton();
        panelTransaksiSampah = new component.ShadowPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxJenis_pnView = new javax.swing.JComboBox<>();
        cbxKategori_pnView = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        btn_SampahMasuk = new component.Jbutton();
        jLabel17 = new javax.swing.JLabel();
        btn_SampahKeluar = new component.Jbutton();
        btn_ProsesSampah = new component.Jbutton();
        jLabel18 = new javax.swing.JLabel();
        txt_Kode = new component.PlaceholderTextField();
        txt_Nama = new component.PlaceholderTextField();
        txt_Berat = new component.PlaceholderTextField();
        shadowTotal = new component.ShadowPanel();
        lblTotal = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        btnBatalProses = new component.Jbutton();
        panelAdd = new javax.swing.JPanel();
        shadowPanel2 = new component.ShadowPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cbxJenis_pnAdd = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        cbxKategori_pnAdd = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        txt_HargaAdd = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        txt_HargaAdd2 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        tgl_Add = new datechooser.beans.DateChooserCombo();
        btnSimpanHarga = new component.Jbutton();
        btnKembaliT = new component.Jbutton();
        panelRiwayat = new javax.swing.JPanel();
        shadowPanel1 = new component.ShadowPanel();
        jLabel7 = new javax.swing.JLabel();
        ShadowSearch1 = new component.ShadowPanel();
        jLabel9 = new javax.swing.JLabel();
        btnKembaliR = new component.Jbutton();
        jScrollPane2 = new javax.swing.JScrollPane();
        table2 = new component.Table();
        btnLastPage = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        cbxPage = new javax.swing.JComboBox<>();
        btnPrevious = new javax.swing.JButton();
        btnFirstPage = new javax.swing.JButton();
        panelJK = new javax.swing.JPanel();
        shadowJenis = new component.ShadowPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblJenis = new component.Table();
        txt_Jenis = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        btnTambahJenis = new component.Jbutton();
        btnHapusJenis = new component.Jbutton();
        jLabel22 = new javax.swing.JLabel();
        btnKembaliJK = new component.Jbutton();
        btn_cancelJenis = new component.Jbutton();
        shadowKategori = new component.ShadowPanel();
        jLabel25 = new javax.swing.JLabel();
        cbxJenis_pnJK = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        txt_Kategori = new javax.swing.JTextField();
        btnTambahKategori = new component.Jbutton();
        btnHapusKategori = new component.Jbutton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblKategori = new component.Table();
        jLabel27 = new javax.swing.JLabel();
        btn_cancelJenisKategori = new component.Jbutton();

        setPreferredSize(new java.awt.Dimension(1192, 944));
        setLayout(new java.awt.CardLayout());

        panelMain.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(250, 250, 250));
        panelView.setPreferredSize(new java.awt.Dimension(1192, 944));

        panelHarga.setFont(panelHarga.getFont());

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel16.setText("Daftar Harga Sampah/KG");

        btnTambahHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahHarga.setText("Tambah");
        btnTambahHarga.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambahHarga.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambahHarga.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambahHarga.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnTambahHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahHargaActionPerformed(evt);
            }
        });

        btnBatalHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnBatalHarga.setText("Batal");
        btnBatalHarga.setFillClick(new java.awt.Color(200, 125, 0));
        btnBatalHarga.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnBatalHarga.setFillOver(new java.awt.Color(230, 145, 10));
        btnBatalHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalHargaActionPerformed(evt);
            }
        });

        btnKelola_JK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_edit.png"))); // NOI18N
        btnKelola_JK.setText("Jenis dan Kategori");
        btnKelola_JK.setFillClick(new java.awt.Color(55, 130, 60));
        btnKelola_JK.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnKelola_JK.setFillOver(new java.awt.Color(69, 160, 75));
        btnKelola_JK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKelola_JKActionPerformed(evt);
            }
        });

        tblSampah.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID Sampah", "Jenis Sampah", "Kategori Sampah", "Harga Setor/Kg", "Harga Jual/Kg", "Tanggal", "Stok"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true
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

        txt_search.setPlaceholder("Cari daftar harga");
        txt_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_searchKeyTyped(evt);
            }
        });

        lb_halaman2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lb_halaman2.setText("hal");

        btn_before2.setText("<");

        cbx_data2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5", "10", "15", "20" }));

        btn_next2.setText(">");

        btn_last2.setText("Last Page");

        btn_first2.setText("First Page");

        btn_Export2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_Export2.setText("Export To Excel");
        btn_Export2.setFillClick(new java.awt.Color(55, 130, 60));
        btn_Export2.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_Export2.setFillOver(new java.awt.Color(69, 160, 75));
        btn_Export2.setRoundedCorner(40);
        btn_Export2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Export2ActionPerformed(evt);
            }
        });

        btn_import2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_excel.png"))); // NOI18N
        btn_import2.setText("Import To Excel");
        btn_import2.setFillClick(new java.awt.Color(60, 130, 200));
        btn_import2.setFillOriginal(new java.awt.Color(80, 150, 230));
        btn_import2.setFillOver(new java.awt.Color(70, 140, 220));
        btn_import2.setRoundedCorner(40);
        btn_import2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_import2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBawah2Layout = new javax.swing.GroupLayout(panelBawah2);
        panelBawah2.setLayout(panelBawah2Layout);
        panelBawah2Layout.setHorizontalGroup(
            panelBawah2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBawah2Layout.createSequentialGroup()
                .addComponent(btn_Export2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_import2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(btn_last2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_import2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnHapusHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusHarga.setText("Hapus");
        btnHapusHarga.setFillClick(new java.awt.Color(190, 30, 20));
        btnHapusHarga.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapusHarga.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapusHarga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnHapusHargaMouseClicked(evt);
            }
        });
        btnHapusHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusHargaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelHargaLayout = new javax.swing.GroupLayout(panelHarga);
        panelHarga.setLayout(panelHargaLayout);
        panelHargaLayout.setHorizontalGroup(
            panelHargaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(panelBawah2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelHargaLayout.createSequentialGroup()
                .addComponent(jLabel16)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelHargaLayout.createSequentialGroup()
                .addComponent(btnKelola_JK, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 738, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTambahHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHapusHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBatalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelHargaLayout.setVerticalGroup(
            panelHargaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHargaLayout.createSequentialGroup()
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelHargaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTambahHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBatalHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnHapusHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                    .addComponent(btnKelola_JK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(panelBawah2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        panelTransaksiSampah.setFont(panelTransaksiSampah.getFont());
        panelTransaksiSampah.setMaximumSize(new java.awt.Dimension(1206, 343));

        jLabel1.setText("Kode Nasabah");

        jLabel3.setText("Jenis Sampah");

        jLabel4.setText("Kategori");

        cbxJenis_pnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnViewActionPerformed(evt);
            }
        });

        jLabel5.setText("Berat Sampah");

        btn_SampahMasuk.setBackground(new java.awt.Color(255, 255, 51));
        btn_SampahMasuk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_masuk.png"))); // NOI18N
        btn_SampahMasuk.setText("Setor Sampah");
        btn_SampahMasuk.setFillClick(new java.awt.Color(55, 130, 60));
        btn_SampahMasuk.setFillOriginal(new java.awt.Color(76, 175, 80));
        btn_SampahMasuk.setFillOver(new java.awt.Color(69, 160, 75));
        btn_SampahMasuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SampahMasukActionPerformed(evt);
            }
        });

        jLabel17.setText("Nama Nasabah");

        btn_SampahKeluar.setBackground(new java.awt.Color(255, 255, 51));
        btn_SampahKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_keluar.png"))); // NOI18N
        btn_SampahKeluar.setText("Jual Sampah");
        btn_SampahKeluar.setFillClick(new java.awt.Color(60, 130, 200));
        btn_SampahKeluar.setFillOriginal(new java.awt.Color(80, 150, 230));
        btn_SampahKeluar.setFillOver(new java.awt.Color(70, 140, 220));
        btn_SampahKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SampahKeluarActionPerformed(evt);
            }
        });

        btn_ProsesSampah.setBackground(new java.awt.Color(255, 255, 51));
        btn_ProsesSampah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_addcollection.png"))); // NOI18N
        btn_ProsesSampah.setText("Proses Sampah");
        btn_ProsesSampah.setFillClick(new java.awt.Color(194, 65, 12));
        btn_ProsesSampah.setFillOriginal(new java.awt.Color(234, 88, 12));
        btn_ProsesSampah.setFillOver(new java.awt.Color(217, 108, 42));
        btn_ProsesSampah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ProsesSampahActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel18.setText("Setor / Jual");

        txt_Kode.setPlaceholder("Kode nasabah");
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

        txt_Nama.setPlaceholder("Nama");

        txt_Berat.setPlaceholder("Berat sampah");

        lblTotal.setFont(new java.awt.Font("NSimSun", 1, 48)); // NOI18N
        lblTotal.setText("0");
        lblTotal.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel10.setFont(new java.awt.Font("NSimSun", 1, 20)); // NOI18N
        jLabel10.setText("Rp.");

        javax.swing.GroupLayout shadowTotalLayout = new javax.swing.GroupLayout(shadowTotal);
        shadowTotal.setLayout(shadowTotalLayout);
        shadowTotalLayout.setHorizontalGroup(
            shadowTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTotalLayout.createSequentialGroup()
                .addGroup(shadowTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowTotalLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel10))
                    .addGroup(shadowTotalLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        shadowTotalLayout.setVerticalGroup(
            shadowTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTotalLayout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotal)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnBatalProses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnBatalProses.setText("Batal");
        btnBatalProses.setFillClick(new java.awt.Color(200, 125, 0));
        btnBatalProses.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnBatalProses.setFillOver(new java.awt.Color(230, 145, 10));
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
                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_ProsesSampah, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_Berat, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxKategori_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxJenis_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                        .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txt_Kode, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(txt_Nama, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3)
                    .addComponent(jLabel18)
                    .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                        .addComponent(btn_SampahMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_SampahKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addGroup(panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shadowTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                        .addComponent(btnBatalProses, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        panelTransaksiSampahLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_SampahKeluar, btn_SampahMasuk});

        panelTransaksiSampahLayout.setVerticalGroup(
            panelTransaksiSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTransaksiSampahLayout.createSequentialGroup()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
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
                            .addComponent(txt_Kode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_Nama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(12, 12, 12)
                        .addComponent(cbxJenis_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxKategori_pnView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_Berat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(btn_ProsesSampah, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(shadowTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTransaksiSampah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panelTransaksiSampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        panelAdd.setPreferredSize(new java.awt.Dimension(1192, 944));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel6.setText("Tambah Harga Sampah");

        jLabel11.setFont(jLabel11.getFont().deriveFont(jLabel11.getFont().getSize()+1f));
        jLabel11.setText("Jenis Sampah");

        cbxJenis_pnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnAddActionPerformed(evt);
            }
        });

        jLabel12.setFont(jLabel12.getFont().deriveFont(jLabel12.getFont().getSize()+1f));
        jLabel12.setText("Kategori");

        jLabel13.setFont(jLabel13.getFont().deriveFont(jLabel13.getFont().getSize()+1f));
        jLabel13.setText("Harga Setor/Kg");

        txt_HargaAdd.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel23.setFont(jLabel23.getFont().deriveFont(jLabel23.getFont().getSize()+1f));
        jLabel23.setText("Harga Jual/Kg");

        txt_HargaAdd2.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel21.setFont(jLabel21.getFont().deriveFont(jLabel21.getFont().getSize()+1f));
        jLabel21.setText("Tanggal");

        btnSimpanHarga.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btnSimpanHarga.setText("Simpan");
        btnSimpanHarga.setFillClick(new java.awt.Color(30, 100, 150));
        btnSimpanHarga.setFillOriginal(new java.awt.Color(41, 128, 185));
        btnSimpanHarga.setFillOver(new java.awt.Color(36, 116, 170));
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
        btnKembaliT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowPanel2Layout = new javax.swing.GroupLayout(shadowPanel2);
        shadowPanel2.setLayout(shadowPanel2Layout);
        shadowPanel2Layout.setHorizontalGroup(
            shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txt_HargaAdd2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbxJenis_pnAdd, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shadowPanel2Layout.createSequentialGroup()
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 733, Short.MAX_VALUE)
                        .addComponent(btnSimpanHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnKembaliT, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbxKategori_pnAdd, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txt_HargaAdd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shadowPanel2Layout.createSequentialGroup()
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(tgl_Add, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        shadowPanel2Layout.setVerticalGroup(
            shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel2Layout.createSequentialGroup()
                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnKembaliT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSimpanHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxJenis_pnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxKategori_pnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_HargaAdd2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_Add, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 495, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelAddLayout = new javax.swing.GroupLayout(panelAdd);
        panelAdd.setLayout(panelAddLayout);
        panelAddLayout.setHorizontalGroup(
            panelAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAddLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        panelAddLayout.setVerticalGroup(
            panelAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAddLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelAdd, "card2");

        panelRiwayat.setPreferredSize(new java.awt.Dimension(1192, 944));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel7.setText("Riwayat Setoran");

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
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addContainerGap())
        );

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

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(table2);

        btnLastPage.setText("Last Page");
        btnLastPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastPageActionPerformed(evt);
            }
        });

        btnNext.setText(">");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        cbxPage.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnPrevious.setText("<");
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });

        btnFirstPage.setText("First Page");
        btnFirstPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstPageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowPanel1Layout = new javax.swing.GroupLayout(shadowPanel1);
        shadowPanel1.setLayout(shadowPanel1Layout);
        shadowPanel1Layout.setHorizontalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel1Layout.createSequentialGroup()
                .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(shadowPanel1Layout.createSequentialGroup()
                        .addComponent(btnFirstPage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrevious)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLastPage))
                    .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel7)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(shadowPanel1Layout.createSequentialGroup()
                            .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnKembaliR, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(269, Short.MAX_VALUE))
        );
        shadowPanel1Layout.setVerticalGroup(
            shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ShadowSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembaliR, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 753, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(shadowPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLastPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFirstPage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbxPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelRiwayatLayout = new javax.swing.GroupLayout(panelRiwayat);
        panelRiwayat.setLayout(panelRiwayatLayout);
        panelRiwayatLayout.setHorizontalGroup(
            panelRiwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRiwayatLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        panelRiwayatLayout.setVerticalGroup(
            panelRiwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRiwayatLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelRiwayat, "card2");

        panelJK.setBackground(new java.awt.Color(250, 250, 250));
        panelJK.setPreferredSize(new java.awt.Dimension(1192, 944));

        tblJenis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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
        }

        txt_Jenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_JenisActionPerformed(evt);
            }
        });

        jLabel24.setFont(jLabel24.getFont().deriveFont(jLabel24.getFont().getSize()+1f));
        jLabel24.setText("Jenis Sampah");

        btnTambahJenis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahJenis.setText("Tambah");
        btnTambahJenis.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambahJenis.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambahJenis.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambahJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahJenisActionPerformed(evt);
            }
        });

        btnHapusJenis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusJenis.setText("Hapus");
        btnHapusJenis.setFillClick(new java.awt.Color(190, 30, 20));
        btnHapusJenis.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapusJenis.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapusJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusJenisActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel22.setText("Kelola Jenis Sampah");

        btnKembaliJK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembaliJK.setText("Kembali");
        btnKembaliJK.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembaliJK.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembaliJK.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembaliJK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliJKActionPerformed(evt);
            }
        });

        btn_cancelJenis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btn_cancelJenis.setText("Batal");
        btn_cancelJenis.setFillClick(new java.awt.Color(200, 125, 0));
        btn_cancelJenis.setFillOriginal(new java.awt.Color(243, 156, 18));
        btn_cancelJenis.setFillOver(new java.awt.Color(230, 145, 10));
        btn_cancelJenis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_cancelJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cancelJenisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowJenisLayout = new javax.swing.GroupLayout(shadowJenis);
        shadowJenis.setLayout(shadowJenisLayout);
        shadowJenisLayout.setHorizontalGroup(
            shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowJenisLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowJenisLayout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnKembaliJK, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(shadowJenisLayout.createSequentialGroup()
                        .addGroup(shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1146, Short.MAX_VALUE)
                            .addGroup(shadowJenisLayout.createSequentialGroup()
                                .addComponent(btnTambahJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapusJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_cancelJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txt_Jenis)
                            .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        shadowJenisLayout.setVerticalGroup(
            shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowJenisLayout.createSequentialGroup()
                .addGroup(shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembaliJK, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_Jenis, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnTambahJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnHapusJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btn_cancelJenis, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel25.setFont(jLabel25.getFont().deriveFont(jLabel25.getFont().getSize()+1f));
        jLabel25.setText("Jenis Sampah");

        cbxJenis_pnJK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxJenis_pnJKActionPerformed(evt);
            }
        });

        jLabel26.setFont(jLabel26.getFont().deriveFont(jLabel26.getFont().getSize()+1f));
        jLabel26.setText("Kategori Sampah");

        txt_Kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_KategoriActionPerformed(evt);
            }
        });

        btnTambahKategori.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambahKategori.setText("Tambah");
        btnTambahKategori.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambahKategori.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambahKategori.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambahKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKategoriActionPerformed(evt);
            }
        });

        btnHapusKategori.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapusKategori.setText("Hapus");
        btnHapusKategori.setFillClick(new java.awt.Color(153, 0, 0));
        btnHapusKategori.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapusKategori.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapusKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusKategoriActionPerformed(evt);
            }
        });

        tblKategori.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel27.setText("Kelola Kategori Sampah");

        btn_cancelJenisKategori.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btn_cancelJenisKategori.setText("Batal");
        btn_cancelJenisKategori.setFillClick(new java.awt.Color(200, 125, 0));
        btn_cancelJenisKategori.setFillOriginal(new java.awt.Color(243, 156, 18));
        btn_cancelJenisKategori.setFillOver(new java.awt.Color(230, 145, 10));
        btn_cancelJenisKategori.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        javax.swing.GroupLayout shadowKategoriLayout = new javax.swing.GroupLayout(shadowKategori);
        shadowKategori.setLayout(shadowKategoriLayout);
        shadowKategoriLayout.setHorizontalGroup(
            shadowKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowKategoriLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_Kategori)
                    .addComponent(cbxJenis_pnJK, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(shadowKategoriLayout.createSequentialGroup()
                        .addComponent(btnTambahKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnHapusKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_cancelJenisKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        shadowKategoriLayout.setVerticalGroup(
            shadowKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowKategoriLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addGap(1, 1, 1)
                .addComponent(cbxJenis_pnJK, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel26)
                .addGap(4, 4, 4)
                .addComponent(txt_Kategori, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(shadowKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowKategoriLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(shadowKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTambahKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnHapusKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(shadowKategoriLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_cancelJenisKategori, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelJKLayout = new javax.swing.GroupLayout(panelJK);
        panelJK.setLayout(panelJKLayout);
        panelJKLayout.setHorizontalGroup(
            panelJKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelJKLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelJKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shadowJenis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowKategori, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelJKLayout.setVerticalGroup(
            panelJKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelJKLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowJenis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelJK, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void btn_cancelJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cancelJenisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_cancelJenisActionPerformed

    private void btnSimpanHargaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSimpanHargaActionPerformed
        if (btnSimpanHarga.getText().equals("Tambah")) {
            btnSimpanHarga.setText("Simpan");
        } else if (btnSimpanHarga.getText().equals("Simpan")) {
            insertData();
        } else if (btnSimpanHarga.getText().equals("Perbarui")) {
            updateData();
        }
    }// GEN-LAST:event_btnSimpanHargaActionPerformed

    private void btnKembaliTActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnKembaliTActionPerformed
        showPanel();
    }// GEN-LAST:event_btnKembaliTActionPerformed

    private void btnTambahHargaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTambahHargaActionPerformed
        panelMain.removeAll();
        panelMain.add(panelAdd);
        panelMain.repaint();
        panelMain.revalidate();

        if (btnTambahHarga.getText().equals("Ubah")) {
            dataTabel();
            btnSimpanHarga.setText("Perbarui");
        }
    }// GEN-LAST:event_btnTambahHargaActionPerformed

    private void btnBatalHargaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBatalHargaActionPerformed
        showPanel();
    }// GEN-LAST:event_btnBatalHargaActionPerformed

    private void tblSampahMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tblSampahMouseClicked
        int selectedRow = tblSampah.getSelectedRow(); // Mendapatkan indeks baris yang diklik

        if (btnTambahHarga.getText().equals("Tambah")) {
            btnTambahHarga.setText("Ubah");
            btnTambahHarga.setIcon(new ImageIcon("src\\main\\resources\\icon\\icon_edit.png"));
            btnTambahHarga.setFillClick(new Color(30, 100, 150));
            btnTambahHarga.setFillOriginal(new Color(41, 128, 185));
            btnTambahHarga.setFillOver(new Color(36, 116, 170));
            btnHapusHarga.setVisible(true);
            btnBatalHarga.setVisible(true);
        }

        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            btnTambahHarga.setEnabled(true);
            btnHapusHarga.setEnabled(true);

            selectedIdSampah = Integer.parseInt(tblSampah.getValueAt(selectedRow, 0).toString());
            cbxJenis_pnAdd.setSelectedItem(tblSampah.getValueAt(selectedRow, 1).toString());
            cbxKategori_pnAdd.setSelectedItem(tblSampah.getValueAt(selectedRow, 2).toString());
            txt_HargaAdd.setText(tblSampah.getValueAt(selectedRow, 3).toString());
            txt_HargaAdd2.setText(tblSampah.getValueAt(selectedRow, 4).toString());

            String tanggalStr = tblSampah.getValueAt(selectedRow, 5).toString();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // atau format sesuai tabelmu
                Date date = sdf.parse(tanggalStr);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                tgl_Add.setSelectedDate(calendar); // <- DateChooserCombo pakai Calendar
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }// GEN-LAST:event_tblSampahMouseClicked

    private void btnLastPageActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLastPageActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnLastPageActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnNextActionPerformed

    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPreviousActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnPreviousActionPerformed

    private void btnFirstPageActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnFirstPageActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnFirstPageActionPerformed

    private void btnKembaliRActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnKembaliRActionPerformed
        showPanel();
    }// GEN-LAST:event_btnKembaliRActionPerformed

    private void btnKelola_JKActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnKelola_JKActionPerformed
        panelMain.removeAll();
        panelMain.add(panelJK);
        panelMain.repaint();
        panelMain.revalidate();
    }// GEN-LAST:event_btnKelola_JKActionPerformed

    private void btnKembaliJKActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnKembaliJKActionPerformed
        showPanel();
    }// GEN-LAST:event_btnKembaliJKActionPerformed

    private void txt_JenisActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_JenisActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_JenisActionPerformed

    private void btnTambahJenisActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTambahJenisActionPerformed
        try {
            String jenis = txt_Jenis.getText().trim();

            // Validasi input kosong
            if (jenis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama jenis sampah tidak boleh kosong!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Cek apakah jenis sudah ada
            String checkSql = "SELECT COUNT(*) FROM jenis_sampah WHERE LOWER(nama_jenis) = LOWER(?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, jenis);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Jenis sampah '" + jenis + "' sudah ada dalam database!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Jika belum ada, lakukan insert
            String sql = "INSERT INTO jenis_sampah (nama_jenis) VALUES (?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, jenis);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Jenis Sampah Tersimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTabelJenis();
            loadTabelKategori();
            loadJenisSampah();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Menyimpan Jenis Sampah: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_btnTambahJenisActionPerformed

    private void btnHapusJenisActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHapusJenisActionPerformed
        try {
            String jenis = txt_Jenis.getText();

            String sql = "DELETE  FROM jenis_sampah WHERE jenis_sampah.nama_jenis = ? ";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jenis);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Berhasil Menghapus Jenis Sampah!", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTabelJenis();
            loadTabelKategori();
            loadJenisSampah();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal Menghapus Jenis Sampah.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_btnHapusJenisActionPerformed

    private void cbxJenis_pnAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxJenis_pnAddActionPerformed
        if (cbxJenis_pnAdd.getSelectedIndex() > 0) { // Jika bukan item pertama ("-- Pilih --")
            String selectedJenis = cbxJenis_pnAdd.getSelectedItem().toString();
            loadKategoriByJenis(selectedJenis);
        } else {
            cbxKategori_pnAdd.removeAllItems();
            cbxKategori_pnAdd.addItem("-- Pilih Kategori --");
        }
    }// GEN-LAST:event_cbxJenis_pnAddActionPerformed

    private void btn_SampahKeluarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_SampahKeluarActionPerformed
        lastButtonClicked = "jual";
        txt_Kode.setEditable(false);
        txt_Nama.setEditable(false);

    }// GEN-LAST:event_btn_SampahKeluarActionPerformed

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

    private void btn_SampahMasukActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_SampahMasukActionPerformed
        lastButtonClicked = "setor";
        txt_Kode.setEditable(true);
        txt_Nama.setEditable(true);

        // Set static color for this button to show it's selected
        btn_SampahMasuk.setFillOriginal(new java.awt.Color(55, 130, 60));
        btn_SampahMasuk.setFillOver(new java.awt.Color(55, 130, 60));
        btn_SampahMasuk.setFillClick(new java.awt.Color(55, 130, 60));

        // Reset the other button's colors to default
        btn_SampahKeluar.setFillOriginal(new java.awt.Color(80, 150, 230));
        btn_SampahKeluar.setFillOver(new java.awt.Color(70, 140, 220));
        btn_SampahKeluar.setFillClick(new java.awt.Color(60, 130, 200));

        // Force repaint to show changes immediately
        btn_SampahMasuk.repaint();
        btn_SampahKeluar.repaint();
    }// GEN-LAST:event_btn_SampahMasukActionPerformed

    private void cbxJenis_pnViewActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxJenis_pnViewActionPerformed
        if (cbxJenis_pnView.getSelectedIndex() > 0) { // Jika bukan item pertama ("-- Pilih --")
            String selectedJenis = cbxJenis_pnView.getSelectedItem().toString();
            loadKategoriByJenis(selectedJenis);
        } else {
            cbxKategori_pnView.removeAllItems();
            cbxKategori_pnView.addItem("-- Pilih Kategori --");
        }
    }// GEN-LAST:event_cbxJenis_pnViewActionPerformed

    private void txt_KategoriActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_KategoriActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_KategoriActionPerformed

    private void btnTambahKategoriActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTambahKategoriActionPerformed
        String kategori = txt_Kategori.getText();
        String jenis = cbxJenis_pnJK.getSelectedItem().toString();

        if (kategori.isEmpty() || jenis.equals("-- Pilih Jenis --")) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String sql = "INSERT INTO kategori_sampah (nama_kategori, id_jenis) VALUES (?, (SELECT id_jenis FROM jenis_sampah WHERE nama_jenis = ?))";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, kategori);
            pstmt.setString(2, jenis);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Kategori berhasil ditambahkan!");
            txt_Kategori.setText("");
            loadTabelKategori();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_btnTambahKategoriActionPerformed

    private void btnHapusKategoriActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHapusKategoriActionPerformed
        String kategori = txt_Kategori.getText().trim();
        String jenis = cbxJenis_pnJK.getSelectedItem().toString();

        // Validasi input kosong
        if (kategori.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori sampah tidak boleh kosong!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi pilihan jenis
        if (jenis.equals("-- Pilih Jenis --")) {
            JOptionPane.showMessageDialog(this, "Pilih jenis sampah terlebih dahulu!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Konfirmasi penghapusan
        int confirm = JOptionPane.showConfirmDialog(null,
                "Apakah Anda yakin ingin menghapus kategori '" + kategori + "' dari jenis " + jenis + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM kategori_sampah "
                        + "WHERE nama_kategori = ? AND id_jenis = "
                        + "(SELECT id_jenis FROM jenis_sampah WHERE nama_jenis = ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, kategori);
                pstmt.setString(2, jenis);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Kategori berhasil dihapus!");
                    clearForm();
                    loadTabelJenis();
                    loadTabelKategori();
                    loadJenisSampah();
                } else {
                    JOptionPane.showMessageDialog(null, "Data tidak ditemukan!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
    }// GEN-LAST:event_btnHapusKategoriActionPerformed

    private void cbxJenis_pnJKActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxJenis_pnJKActionPerformed

    }// GEN-LAST:event_cbxJenis_pnJKActionPerformed

    private void tblJenisMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tblJenisMouseClicked
        int row = tblJenis.rowAtPoint(evt.getPoint());
        int col = tblJenis.columnAtPoint(evt.getPoint());

        // Jika klik pada kolom nama (kolom index 1)
        if (col == 1 && row != -1) {
            String value = (String) tblJenis.getValueAt(row, col);
            txt_Jenis.setText(value);
        }
    }// GEN-LAST:event_tblJenisMouseClicked

    private void tblKategoriMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tblKategoriMouseClicked
        int selectedRow = tblKategori.getSelectedRow(); // Mendapatkan indeks baris yang diklik
        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            // Ambil nilai dari baris yang dipilih dan masukkan ke text field
            txt_Kategori.setText(tblKategori.getValueAt(selectedRow, 1).toString());
            cbxJenis_pnJK.setSelectedItem(tblKategori.getValueAt(selectedRow, 2).toString());
        }
    }// GEN-LAST:event_tblKategoriMouseClicked

    private void btn_ProsesSampahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_ProsesSampahActionPerformed
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

            // Query Memanggil Saldo
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
                    JOptionPane.showMessageDialog(null, "Harap lengkapi semua data!", "Peringatan",
                            JOptionPane.WARNING_MESSAGE);
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
                    insertPs.setInt(6, users.getId());
                    insertPs.executeUpdate();

                    String updateSaldo = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                    PreparedStatement psUpdateSaldo = conn.prepareStatement(updateSaldo);
                    psUpdateSaldo.setDouble(1, saldoBaru);
                    psUpdateSaldo.setString(2, kode);
                    psUpdateSaldo.executeUpdate();

                    lblTotal.setText("Rp " + String.format("%,.0f", total));

                    String namaNasabah = "SELECT nama_nasabah FROM manajemen_nasabah WHERE id_nasabah = ?";
                    PreparedStatement psNasabah = conn.prepareStatement(namaNasabah);
                    psNasabah.setString(1, kode);
                    ResultSet rsNama = psNasabah.executeQuery();
                    String namaNasabahStr = kode; // fallback
                    if (rsNama.next()) {
                        namaNasabahStr = rsNama.getString("nama_nasabah");
                    }

                    int result = JOptionPane.showConfirmDialog(null,
                            "SETOR SAMPAH BERHASIL!\nTotal Harga: Rp " + String.format("%,.0f", total)
                                    + "\nSaldo " + namaNasabahStr + " Bertambah Menjadi: Rp "
                                    + String.format("%,.0f", saldoBaru),
                            "Sukses",
                            JOptionPane.DEFAULT_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        lblTotal.setText("0");
                        clearForm();
                        loadJenisSampah();
                        showPanel();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Data sampah tidak ditemukan untuk setor.");
                }

            } else if (lastButtonClicked.equals("jual")) {
                // QUERY UNTUK JUAL
                if (namaJenis.isEmpty() || namaKategori.isEmpty() || strBerat.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Harap lengkapi semua data!", "Peringatan",
                            JOptionPane.WARNING_MESSAGE);
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
                    insertPs.setInt(4, users.getId());
                    insertPs.executeUpdate();

                    lblTotal.setText("Rp " + String.format("%,.0f", total));
                    int result = JOptionPane.showConfirmDialog(null,
                            "TRANSAKSI JUAL SAMPAH BERHASIL!\nTotal Harga: Rp " + String.format("%,.0f", total),
                            "Sukses", JOptionPane.DEFAULT_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        lblTotal.setText("0");
                        clearForm();
                        loadJenisSampah();
                        showPanel();
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
    }// GEN-LAST:event_btn_ProsesSampahActionPerformed

    private void btnBatalProsesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBatalProsesActionPerformed
        showPanel();
    }// GEN-LAST:event_btnBatalProsesActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_searchKeyTyped
        searchDataHarga();
    }// GEN-LAST:event_txt_searchKeyTyped

    private void btn_Export2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_Export2ActionPerformed
        try {
            // Siapkan model dan ambil data sampah
            DefaultTableModel model = new DefaultTableModel(
                    new String[] { "ID Sampah", "Jenis Sampah", "Kategori Sampah", "Harga Setor/Kg", "Harga Jual/Kg",
                            "Tanggal", "Stok" },
                    0);
            getAllSampahData(model); // Ambil data dari DB ke model

            // Cek jika tidak ada data
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Pilih lokasi penyimpanan file
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file Excel");
            chooser.setSelectedFile(new File("daftar_harga_sampah.xls")); // Nama default

            chooser.setFileFilter(new FileFilter() {
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

                // Tambahkan ekstensi jika tidak ada
                String fileName = fileToSave.getName().toLowerCase();
                if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
                }

                // Konfirmasi jika file sudah ada
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

                // Ekspor data ke Excel
                try {
                    ExcelExporter.exportTableModelToExcel(model, fileToSave);

                    JOptionPane.showMessageDialog(this,
                            "Export berhasil!\nFile disimpan di: " + fileToSave.getAbsolutePath(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mengekspor file: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }// GEN-LAST:event_btn_Export2ActionPerformed

    private void btn_import2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_import2ActionPerformed
        try {
            // Pilih file Excel yang akan diimport
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih file Excel");
            chooser.setFileFilter(new FileFilter() {
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

            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();

                // Konfirmasi sebelum import
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Apakah Anda yakin ingin mengimport data dari file ini?\n"
                                + "Data yang sudah ada dengan ID yang sama akan diupdate.",
                        "Konfirmasi Import",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    importExcelSampahToDatabase(selectedFile);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan saat import: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }// GEN-LAST:event_btn_import2ActionPerformed

    private void btnHapusHargaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnHapusHargaMouseClicked

    }// GEN-LAST:event_btnHapusHargaMouseClicked

    private void btnHapusHargaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHapusHargaActionPerformed
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

                loadtabelSampah();
                /////// tambah

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saat menghapus data harga sampah: " + e.getMessage());
                return;
            }
        }
    }// GEN-LAST:event_btnHapusHargaActionPerformed

    private void txt_KodeKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_KodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String kode = txt_Kode.getText();
            cariNamaNasabah(kode);
            btnBatalProses.setVisible(true);
        }
    }// GEN-LAST:event_txt_KodeKeyPressed

    private void txt_KodeActionPerformed(java.awt.event.ActionEvent evt) {
        // Trigger the same action as when Enter key is pressed
        txt_KodeKeyPressed(new java.awt.event.KeyEvent(txt_Kode, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ENTER,
                java.awt.event.KeyEvent.CHAR_UNDEFINED));
    }

    public void importExcelSampahToDatabase(File excelFile) {
        try {
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            int insertCount = 0;
            int skippedCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String jenis = getCellValueAsString(row.getCell(1)).trim();
                String kategori = getCellValueAsString(row.getCell(2)).trim();
                double hargaSetor = getCellValueAsDouble(row.getCell(3));
                double hargaJual = getCellValueAsDouble(row.getCell(4));
                String tanggal = getCellValueAsString(row.getCell(5)).trim();
                int stok = (int) getCellValueAsDouble(row.getCell(6));

                // Lewati baris header yang ikut terbaca
                if (jenis.equalsIgnoreCase("Jenis Sampah") || kategori.equalsIgnoreCase("Kategori Sampah")) {
                    continue;
                }

                // Validasi: Lewati jika jenis atau kategori kosong
                if (jenis.isEmpty() || kategori.isEmpty()) {
                    continue;
                }

                // Cek id_kategori
                String idKategori = null;
                String sqlKategori = "SELECT id_kategori FROM kategori_sampah WHERE nama_kategori = ?";
                PreparedStatement pstKategori = conn.prepareStatement(sqlKategori);
                pstKategori.setString(1, kategori);
                ResultSet rsKategori = pstKategori.executeQuery();
                if (rsKategori.next()) {
                    idKategori = rsKategori.getString("id_kategori");
                }
                rsKategori.close();
                pstKategori.close();
                if (idKategori == null) {
                    skippedCount++;
                    continue;
                }

                // Cek apakah data sudah ada (berdasarkan id_kategori dan tanggal)
                String checkSql = "SELECT id_sampah FROM sampah WHERE id_kategori = ? AND tanggal = ?";
                PreparedStatement checkPst = conn.prepareStatement(checkSql);
                checkPst.setString(1, idKategori);
                checkPst.setString(2, tanggal);
                ResultSet rs = checkPst.executeQuery();

                if (rs.next()) {
                    // Data sudah ada, skip
                    skippedCount++;
                } else {
                    // Insert data baru
                    String insertSql = "INSERT INTO sampah (id_kategori, harga_setor, harga_jual, tanggal, stok_sampah) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertPst = conn.prepareStatement(insertSql);
                    insertPst.setString(1, idKategori);
                    insertPst.setDouble(2, hargaSetor);
                    insertPst.setDouble(3, hargaJual);
                    insertPst.setString(4, tanggal);
                    insertPst.setInt(5, stok);
                    insertPst.executeUpdate();
                    insertPst.close();
                    insertCount++;
                }

                rs.close();
                checkPst.close();
            }

            workbook.close();

            // Refresh tampilan
            calculateTotalPageHarga();
            loadtabelSampah();

            String message = String.format(
                    "Import selesai!\n"
                            + "Data baru: %d\n"
                            + "Data dilewati (sudah ada/invalid): %d",
                    insertCount, skippedCount);
            JOptionPane.showMessageDialog(this,
                    message,
                    "Hasil Import",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mengimport data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date date = cell.getDateCellValue();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(date);
                }
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch1;
    private component.Jbutton btnBatalHarga;
    private component.Jbutton btnBatalProses;
    private javax.swing.JButton btnFirstPage;
    private component.Jbutton btnHapusHarga;
    private component.Jbutton btnHapusJenis;
    private component.Jbutton btnHapusKategori;
    private component.Jbutton btnKelola_JK;
    private component.Jbutton btnKembaliJK;
    private component.Jbutton btnKembaliR;
    private component.Jbutton btnKembaliT;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private component.Jbutton btnSimpanHarga;
    private component.Jbutton btnTambahHarga;
    private component.Jbutton btnTambahJenis;
    private component.Jbutton btnTambahKategori;
    private component.Jbutton btn_Export2;
    private component.Jbutton btn_ProsesSampah;
    private component.Jbutton btn_SampahKeluar;
    private component.Jbutton btn_SampahMasuk;
    private javax.swing.JButton btn_before2;
    private component.Jbutton btn_cancelJenis;
    private component.Jbutton btn_cancelJenisKategori;
    private javax.swing.JButton btn_first2;
    private component.Jbutton btn_import2;
    private javax.swing.JButton btn_last2;
    private javax.swing.JButton btn_next2;
    private javax.swing.JComboBox<String> cbxJenis_pnAdd;
    private javax.swing.JComboBox<String> cbxJenis_pnJK;
    private javax.swing.JComboBox<String> cbxJenis_pnView;
    private javax.swing.JComboBox<String> cbxKategori_pnAdd;
    private javax.swing.JComboBox<String> cbxKategori_pnView;
    private javax.swing.JComboBox<String> cbxPage;
    private javax.swing.JComboBox<String> cbx_data2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lb_halaman2;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelAdd;
    private component.ShadowPanel panelBawah2;
    private component.ShadowPanel panelHarga;
    private javax.swing.JPanel panelJK;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRiwayat;
    private component.ShadowPanel panelTransaksiSampah;
    private javax.swing.JPanel panelView;
    private component.ShadowPanel shadowJenis;
    private component.ShadowPanel shadowKategori;
    private component.ShadowPanel shadowPanel1;
    private component.ShadowPanel shadowPanel2;
    private component.ShadowPanel shadowTotal;
    private component.Table table2;
    private component.Table tblJenis;
    private component.Table tblKategori;
    private component.Table tblSampah;
    private datechooser.beans.DateChooserCombo tgl_Add;
    private component.PlaceholderTextField txt_Berat;
    private javax.swing.JTextField txt_HargaAdd;
    private javax.swing.JTextField txt_HargaAdd2;
    private javax.swing.JTextField txt_Jenis;
    private javax.swing.JTextField txt_Kategori;
    private component.PlaceholderTextField txt_Kode;
    private component.PlaceholderTextField txt_Nama;
    private component.PlaceholderTextField txt_search;
    // End of variables declaration//GEN-END:variables
    private void getData(int startIndex, int entriesPage, DefaultTableModel model) {
        model.setRowCount(0);

        try {
            String sql = "SELECT * FROM manajemen_nasabah ORDER BY id_nasabah DESC LIMIT ?,?";
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, startIndex);
                st.setInt(2, entriesPage);
                ResultSet rs = st.executeQuery();

                // Format angka sesuai locale Indonesia
                NumberFormat formatRupiah = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

                while (rs.next()) {
                    String idNasabah = rs.getString("id_nasabah");
                    String namaNasabah = rs.getString("nama_nasabah");
                    String alamat = rs.getString("alamat");
                    String telepon = rs.getString("no_telpon");
                    String email = rs.getString("email");
                    BigDecimal saldo = rs.getBigDecimal("saldo_total");

                    // Format saldo menjadi string dengan titik ribuan
                    String saldoFormatted = "Rp " + formatRupiah.format(saldo);

                    Object[] rowData = { idNasabah, namaNasabah, alamat, telepon, email, saldoFormatted };
                    model.addRow(rowData);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(TabManajemenNasabah.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void loadJenisSampah() {
        try {
            String sql = "SELECT nama_jenis FROM jenis_sampah ORDER BY nama_jenis";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            cbxJenis_pnView.removeAllItems();
            cbxJenis_pnView.addItem("-- Pilih Jenis --");

            cbxJenis_pnAdd.removeAllItems();
            cbxJenis_pnAdd.addItem("-- Pilih Jenis --");

            cbxJenis_pnJK.removeAllItems();
            cbxJenis_pnJK.addItem("-- Pilih Jenis --");

            while (rs.next()) {
                cbxJenis_pnView.addItem(rs.getString("nama_jenis"));
                cbxJenis_pnAdd.addItem(rs.getString("nama_jenis"));
                cbxJenis_pnJK.addItem(rs.getString("nama_jenis"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load jenis sampah: " + e.getMessage());
        }
    }

    // Method untuk load kategori berdasarkan jenis (akan dipanggil oleh event
    // handler)
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

                cbxKategori_pnView.removeAllItems();
                cbxKategori_pnView.addItem("-- Pilih Kategori --");

                while (katRs.next()) {
                    cbxKategori_pnAdd.addItem(katRs.getString("nama_kategori"));
                    cbxKategori_pnView.addItem(katRs.getString("nama_kategori"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load kategori: " + e.getMessage());
        }
    }

    private void dataTabel() {
        panelView.setVisible(false);
        panelAdd.setVisible(true);

        int row = tblSampah.getSelectedRow();

        cbxJenis_pnAdd.setSelectedItem(tblSampah.getValueAt(row, 1).toString());
        cbxKategori_pnAdd.setSelectedItem(tblSampah.getValueAt(row, 2).toString());
        txt_HargaAdd.setText(tblSampah.getValueAt(row, 3).toString());
        txt_HargaAdd2.setText(tblSampah.getValueAt(row, 4).toString());
    }

    private void insertData() {

        String jenis = cbxJenis_pnAdd.getSelectedItem().toString();
        String kategori = cbxKategori_pnAdd.getSelectedItem().toString();
        String hargaS = txt_HargaAdd.getText();
        String hargaJ = txt_HargaAdd2.getText();

        Date date = tgl_Add.getSelectedDate().getTime();// ini ambil tanggal dari komponen
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tgl = sdf.format(date);

        if (jenis.isEmpty() || kategori.isEmpty() || hargaS.isEmpty() || hargaJ.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
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

            JOptionPane.showMessageDialog(this, "Harga Sampah Berhasil di Tambahkan!", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            showPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Menambahkan Data harga Sampah: " + e.getMessage());
        }
    }

    private void updateData() {
        int selectedRow = tblSampah.getSelectedRow(); // Mendapatkan indeks baris yang diklik
        if (selectedRow >= 0) { // Pastikan ada baris yang dipilih
            // Ambil nilai dari baris yang dipilih dan masukkan ke text field
        }

        String jenis = cbxJenis_pnAdd.getSelectedItem().toString();
        String kategori = cbxKategori_pnAdd.getSelectedItem().toString();
        String harga = txt_HargaAdd.getText();
        String harga2 = txt_HargaAdd2.getText();

        Date date = tgl_Add.getSelectedDate().getTime();// ini ambil tanggal dari komponen
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tgl = sdf.format(date);

        if (jenis.isEmpty() || kategori.isEmpty() || harga.isEmpty() || harga2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Informasi",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm(); // Bersihkan form
                showPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Data harga harus berupa angka!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getAllSampahData(DefaultTableModel model) {
        try {
            String sql = "SELECT s.id_sampah, j.nama_jenis, k.nama_kategori, s.harga_setor, s.harga_jual, s.tanggal, s.stok_sampah "
                    + "FROM sampah s "
                    + "JOIN kategori_sampah k ON s.id_kategori = k.id_kategori "
                    + "JOIN jenis_sampah j ON k.id_jenis = j.id_jenis";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("id_sampah"),
                        rs.getString("nama_jenis"),
                        rs.getString("nama_kategori"),
                        rs.getInt("harga_setor"),
                        rs.getInt("harga_jual"),
                        rs.getDate("tanggal"),
                        String.format("%.2f", rs.getDouble("stok_sampah")).replace(",", ".")
                };
                model.addRow(row);
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Search daftar harga sampah seperti TabManajemenNasabah
    private void searchDataHarga() {
        String kataKunci = txt_search.getText();
        DefaultTableModel model = (DefaultTableModel) tblSampah.getModel();
        model.setRowCount(0);
        try {
            // Hitung total data hasil pencarian
            String sqlCount = "SELECT COUNT(*) as total FROM sampah s JOIN kategori_sampah k ON s.id_kategori = k.id_kategori JOIN jenis_sampah j ON k.id_jenis = j.id_jenis WHERE j.nama_jenis LIKE ? OR k.nama_kategori LIKE ?";
            PreparedStatement pstCount = conn.prepareStatement(sqlCount);
            String likeKeyword = "%" + kataKunci + "%";
            pstCount.setString(1, likeKeyword);
            pstCount.setString(2, likeKeyword);
            ResultSet rsCount = pstCount.executeQuery();
            if (rsCount.next()) {
                totalData = rsCount.getInt("total");
                totalPages = (int) Math.ceil((double) totalData / dataPerHalaman);
            }
            rsCount.close();
            pstCount.close();

            int startIndex = (halamanSaatIni - 1) * dataPerHalaman;
            String sql = "SELECT s.id_sampah, s.harga_setor, s.harga_jual, s.tanggal, k.nama_kategori, j.nama_jenis, s.stok_sampah "
                    + "FROM sampah s "
                    + "JOIN kategori_sampah k ON s.id_kategori = k.id_kategori "
                    + "JOIN jenis_sampah j ON k.id_jenis = j.id_jenis "
                    + "WHERE j.nama_jenis LIKE ? OR k.nama_kategori LIKE ? "
                    + "ORDER BY s.id_sampah LIMIT ? OFFSET ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, likeKeyword);
            pst.setString(2, likeKeyword);
            pst.setInt(3, dataPerHalaman);
            pst.setInt(4, startIndex);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] row = {
                        rs.getString("id_sampah"),
                        rs.getString("nama_jenis"),
                        rs.getString("nama_kategori"),
                        rs.getInt("harga_setor"),
                        rs.getInt("harga_jual"),
                        rs.getDate("tanggal"),
                        String.format("%.2f", rs.getDouble("stok_sampah")).replace(",", ".")
                };
                model.addRow(row);
            }
            rs.close();
            pst.close();
            // Update page label
            lb_halaman2.setText("Halaman " + halamanSaatIni + " dari total " + totalData + " data");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

}
