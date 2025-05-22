package view;

import component.Item;
import component.Jbutton;
import component.ModelItem;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
import main.DBconnect;
import java.awt.image.BufferedImage;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

/**
 *
 * @author devan
 */
public class TabDataBarang extends javax.swing.JPanel {

    private final Connection conn = DBconnect.getConnection();
    private File selectedImageFile;
    private List<Item> itemPanels = new ArrayList<>();
    private ModelItem selectedItem = null;  // menyimpan item yang dipilih

    public TabDataBarang() {
        initComponents();


        scrollBarang.setViewportView(panelBarang);
        scrollBarang.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Hapus isi panel sebelum load data
        panelBarang.removeAll();
        panelBarang.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 19));
        ////////// get data barang  saat item di klik //////////
        try {
            String sql = "SELECT * FROM data_barang";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_barang"); // sesuaikan nama kolomnya
                String nama = rs.getString("nama_barang");
                String kode = rs.getString("kode_barang");
                double harga = rs.getDouble("harga");
                int stok = rs.getInt("stok");
                byte[] gambarBytes = rs.getBytes("gambar"); // jika gambar disimpan sebagai BLOB
                Icon icon = null;
                if (gambarBytes != null) {
                    icon = new ImageIcon(gambarBytes);
                }
                // Di dalam method pembuatan item
                ModelItem model = new ModelItem(id, nama, kode, harga, stok, icon);
                Item itemPanel = new Item();
                itemPanel.setData(model);
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 1. Reset semua item
                        for (Item panel : itemPanels) {
                            if (panel != itemPanel) { // Skip item yang sedang dipilih
                                panel.setSelected(false);
                                panel.repaint(); // Tambahkan ini
                            }
                        }

                        // 2. Pilih item baru
                        itemPanel.setSelected(true);
                        itemPanel.repaint();

                        // 3. Update state
                        selectedItem = model;
                        btnEdit.setEnabled(true);
                        btnHapus.setEnabled(true);
                    }
                });
                itemPanels.add(itemPanel); // Simpan ke list

                panelBarang.add(itemPanel);
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        panelBarang.revalidate();
        panelBarang.repaint();

    }

    ////////// mencari data barang //////////
    private void searchDataBarang(String keyword) {
        try {
            panelBarang.removeAll();
            panelBarang.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 19));

            String sql = "SELECT * FROM data_barang WHERE nama LIKE ? OR kode_barang LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            String likeKeyword = "%" + keyword + "%";
            pst.setString(1, likeKeyword);
            pst.setString(2, likeKeyword);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_barang");
                String nama = rs.getString("nama_barang");
                String kode = rs.getString("kode_barang");
                double harga = rs.getDouble("harga");
                int stok = rs.getInt("stok");
                byte[] gambarBytes = rs.getBytes("gambar");

                Icon icon = null;
                if (gambarBytes != null) {
                    icon = new ImageIcon(gambarBytes);
                }

                ModelItem model = new ModelItem(id, nama, kode, harga, stok, icon);
                Item itemPanel = new Item();
                itemPanel.setData(model);
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectedItem = model;
                        btnEdit.setEnabled(true);
                        btnHapus.setEnabled(true);
                    }
                });

                panelBarang.add(itemPanel);
            }

            rs.close();
            pst.close();

            panelBarang.revalidate();
            panelBarang.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ////////// generate random number untuk barcode //////////
    public String getRandomNumberString() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        // Digit pertama tidak boleh 0 agar tetap 12 digit signifikan
        sb.append(rnd.nextInt(9) + 1); // 1-9
        // Tambahan 11 digit acak lainnya (0-9)
        for (int i = 0; i < 11; i++) {
            sb.append(rnd.nextInt(10));
        }

        return sb.toString(); // Total 12 digit
    }

    ////////// generate barcode //////////
    public void generate(String kode) {
        String namaBarang = txt_nama.getText().trim();
        try {
            Code128Bean barcode = new Code128Bean();
            final int dpi = 160;

            int length = kode.length();

            // Menyesuaikan module width berdasarkan panjang kode
            double moduleWidth;
            if (length <= 8) {
                moduleWidth = 0.6; // mm
            } else if (length <= 15) {
                moduleWidth = 0.45;
            } else {
                moduleWidth = 0.30;
            }

            barcode.setModuleWidth(moduleWidth); // Lebar tiap garis barcode (mm)
            barcode.setBarHeight(15); // Tinggi barcode (mm)
            barcode.setFontSize(4); // Ukuran font
            barcode.setQuietZone(2); // Margin samping (mm)
            barcode.doQuietZone(true);

            // Sanitasi nama barang untuk nama file
            String cleanName = namaBarang
                    .replaceAll("[\\\\/:*?\"<>|]", "") // Hapus karakter ilegal
                    .replace(" ", "_"); // Ganti spasi dengan underscore

            // Output file ke folder resources/barcode
            File outputFile = new File("C:/Users/33/Downloads/" + kode + "_" + cleanName + ".png");
            outputFile.getParentFile().mkdirs();

            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    new FileOutputStream(outputFile),
                    "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

            barcode.generateBarcode(canvas, kode);
            canvas.finish();

            System.out.println("Barcode berhasil dibuat di: " + outputFile.getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPanelEditFormData(ModelItem item) {
        txt_kode1.setText(item.getKode());    // Contoh, asumsi txt_kode1 di panelEdit
        txt_nama1.setText(item.getNama());
        txt_harga1.setText(String.valueOf(item.getHarga()));
        txt_stok1.setText(String.valueOf(item.getStok()));
    }
    
    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabDataBarang());
        panelMain.repaint();
        panelMain.revalidate();
    }

    private void loadDataBarang() {
        try {
            panelBarang.removeAll();
            panelBarang.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 19));

            String sql = "SELECT * FROM data_barang";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_barang");
                String nama = rs.getString("nama_barang");
                String kode = rs.getString("kode_barang");
                double harga = rs.getDouble("harga");
                int stok = rs.getInt("stok");
                byte[] gambarBytes = rs.getBytes("gambar");

                Icon icon = null;
                if (gambarBytes != null) {
                    icon = new ImageIcon(gambarBytes);
                }

                ModelItem model = new ModelItem(id, nama, kode, harga, stok, icon);
                Item itemPanel = new Item();
                itemPanel.setData(model);
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectedItem = model;
                        btnEdit.setEnabled(true);
                        btnHapus.setEnabled(true);
                    }
                });

                panelBarang.add(itemPanel);
            }

            rs.close();
            pst.close();

            panelBarang.revalidate();
            panelBarang.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void clearForm() {
        txt_gambar.setText("");
        txt_gambar1.setText("");
        txt_harga.setText("");
        txt_harga1.setText("");
        txt_kode.setText("");
        txt_nama.setText("");
        txt_nama1.setText("");
        txt_stok.setText("");
        txt_stok1.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        ShadowUtama = new component.ShadowPanel();
        shadowPanel = new component.ShadowPanel();
        btnEdit = new component.Jbutton();
        btnHapus = new component.Jbutton();
        btnKembali = new component.Jbutton();
        btnTambah = new component.Jbutton();
        ShadowSearch = new component.ShadowPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_search = new swing.TextField();
        scrollBarang = new javax.swing.JScrollPane();
        panelBarang = new javax.swing.JPanel();
        panelAdd = new javax.swing.JPanel();
        ShadowUtama1 = new component.ShadowPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txt_nama = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txt_harga = new javax.swing.JTextField();
        btn_SaveAdd = new component.Jbutton();
        btn_CancelAdd = new component.Jbutton();
        jLabel14 = new javax.swing.JLabel();
        txt_stok = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txt_gambar = new javax.swing.JTextField();
        btnPilihGambar = new component.Jbutton();
        txt_kode = new javax.swing.JTextField();
        btn_GenerateCode = new component.Jbutton();
        panelEdit = new javax.swing.JPanel();
        ShadowUtama2 = new component.ShadowPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txt_nama1 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txt_harga1 = new javax.swing.JTextField();
        btn_SaveEdit = new component.Jbutton();
        btn_CancelEdit = new component.Jbutton();
        jLabel19 = new javax.swing.JLabel();
        txt_stok1 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txt_gambar1 = new javax.swing.JTextField();
        btnPilihGambar1 = new component.Jbutton();
        txt_kode1 = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(1206, 743));
        setLayout(new java.awt.CardLayout());

        panelMain.setPreferredSize(new java.awt.Dimension(1206, 743));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(250, 250, 250));

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setEnabled(false);
        btnEdit.setFillClick(new java.awt.Color(0, 51, 153));
        btnEdit.setFillOriginal(new java.awt.Color(0, 204, 204));
        btnEdit.setFillOver(new java.awt.Color(51, 153, 255));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.setEnabled(false);
        btnHapus.setFillClick(new java.awt.Color(190, 30, 20));
        btnHapus.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapus.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnKembali.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnKembali.setText("Batal");
        btnKembali.setFillClick(new java.awt.Color(200, 125, 0));
        btnKembali.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnKembali.setFillOver(new java.awt.Color(230, 145, 10));
        btnKembali.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliActionPerformed(evt);
            }
        });

        btnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambah.setText("Tambah");
        btnTambah.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambah.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambah.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        ShadowSearch.setBackground(new java.awt.Color(249, 251, 255));
        ShadowSearch.setPreferredSize(new java.awt.Dimension(259, 43));

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setForeground(new java.awt.Color(204, 204, 204));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_search.png"))); // NOI18N

        txt_search.setBorder(null);
        txt_search.setForeground(new java.awt.Color(0, 0, 0));
        txt_search.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_search.setHint("  Cari Barang");
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

        javax.swing.GroupLayout ShadowSearchLayout = new javax.swing.GroupLayout(ShadowSearch);
        ShadowSearch.setLayout(ShadowSearchLayout);
        ShadowSearchLayout.setHorizontalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );
        ShadowSearchLayout.setVerticalGroup(
            ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_search, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addGroup(ShadowSearchLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 15, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout shadowPanelLayout = new javax.swing.GroupLayout(shadowPanel);
        shadowPanel.setLayout(shadowPanelLayout);
        shadowPanelLayout.setHorizontalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanelLayout.createSequentialGroup()
                .addGap(745, 745, 745)
                .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
            .addGroup(shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(shadowPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ShadowSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(623, Short.MAX_VALUE)))
        );
        shadowPanelLayout.setVerticalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnKembali, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                    .addComponent(btnEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTambah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(shadowPanelLayout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(ShadowSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addGap(8, 8, 8)))
        );

        scrollBarang.setBackground(new java.awt.Color(255, 255, 255));
        scrollBarang.setBorder(null);
        scrollBarang.setForeground(new java.awt.Color(255, 255, 255));
        scrollBarang.setMaximumSize(new java.awt.Dimension(300, 300));

        panelBarang.setBackground(new java.awt.Color(255, 255, 255));
        panelBarang.setForeground(new java.awt.Color(255, 255, 255));
        panelBarang.setName(""); // NOI18N
        panelBarang.setPreferredSize(new java.awt.Dimension(300, 300));

        javax.swing.GroupLayout panelBarangLayout = new javax.swing.GroupLayout(panelBarang);
        panelBarang.setLayout(panelBarangLayout);
        panelBarangLayout.setHorizontalGroup(
            panelBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1168, Short.MAX_VALUE)
        );
        panelBarangLayout.setVerticalGroup(
            panelBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 826, Short.MAX_VALUE)
        );

        scrollBarang.setViewportView(panelBarang);

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(shadowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 826, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelView.add(ShadowUtama, "card2");

        panelMain.add(panelView, "card2");

        panelAdd.setLayout(new java.awt.CardLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel6.setText("Tambah Data Barang");

        jLabel11.setFont(new java.awt.Font("Mongolian Baiti", 0, 22)); // NOI18N
        jLabel11.setText("Kode Barang");

        jLabel12.setFont(new java.awt.Font("Mongolian Baiti", 0, 22)); // NOI18N
        jLabel12.setText("Nama Barang");

        txt_nama.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel13.setFont(new java.awt.Font("Mongolian Baiti", 0, 22)); // NOI18N
        jLabel13.setText("Harga");

        txt_harga.setPreferredSize(new java.awt.Dimension(20, 22));

        btn_SaveAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btn_SaveAdd.setText("Simpan");
        btn_SaveAdd.setFillClick(new java.awt.Color(30, 100, 150));
        btn_SaveAdd.setFillOriginal(new java.awt.Color(41, 128, 185));
        btn_SaveAdd.setFillOver(new java.awt.Color(36, 116, 170));
        btn_SaveAdd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SaveAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SaveAddActionPerformed(evt);
            }
        });

        btn_CancelAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btn_CancelAdd.setText("Batal");
        btn_CancelAdd.setFillClick(new java.awt.Color(200, 125, 0));
        btn_CancelAdd.setFillOriginal(new java.awt.Color(243, 156, 18));
        btn_CancelAdd.setFillOver(new java.awt.Color(230, 145, 10));
        btn_CancelAdd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_CancelAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancelAddActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Mongolian Baiti", 0, 22)); // NOI18N
        jLabel14.setText("Jumlah Stok");

        txt_stok.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel15.setFont(new java.awt.Font("Mongolian Baiti", 0, 22)); // NOI18N
        jLabel15.setText("Gambar");

        txt_gambar.setPreferredSize(new java.awt.Dimension(20, 22));
        txt_gambar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_gambarActionPerformed(evt);
            }
        });

        btnPilihGambar.setText("Pilih Gambar");
        btnPilihGambar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPilihGambarActionPerformed(evt);
            }
        });

        txt_kode.setPreferredSize(new java.awt.Dimension(20, 22));

        btn_GenerateCode.setText("Input Otomatis");
        btn_GenerateCode.setFillClick(new java.awt.Color(153, 0, 153));
        btn_GenerateCode.setFillOriginal(new java.awt.Color(255, 51, 204));
        btn_GenerateCode.setFillOver(new java.awt.Color(255, 102, 204));
        btn_GenerateCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_GenerateCodeActionPerformed(evt);
            }
        });

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
                        .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                .addComponent(btnPilihGambar, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_gambar, javax.swing.GroupLayout.PREFERRED_SIZE, 1003, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txt_nama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txt_harga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtama1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_SaveAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_CancelAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txt_stok, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtama1Layout.createSequentialGroup()
                                .addComponent(btn_GenerateCode, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_kode, javax.swing.GroupLayout.PREFERRED_SIZE, 1003, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 53, Short.MAX_VALUE))))
        );
        ShadowUtama1Layout.setVerticalGroup(
            ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_CancelAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                        .addComponent(btn_SaveAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txt_kode, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(btn_GenerateCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_harga, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_stok, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txt_gambar, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(btnPilihGambar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(243, Short.MAX_VALUE))
        );

        panelAdd.add(ShadowUtama1, "card2");

        panelMain.add(panelAdd, "card2");

        panelEdit.setLayout(new java.awt.CardLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel7.setText("Edit Data Barang");

        jLabel16.setFont(new java.awt.Font("Mongolian Baiti", 1, 21)); // NOI18N
        jLabel16.setText("Kode Barang");

        jLabel17.setFont(new java.awt.Font("Mongolian Baiti", 1, 22)); // NOI18N
        jLabel17.setText("Nama Barang");

        txt_nama1.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel18.setFont(new java.awt.Font("Mongolian Baiti", 1, 22)); // NOI18N
        jLabel18.setText("Harga");

        txt_harga1.setPreferredSize(new java.awt.Dimension(20, 22));

        btn_SaveEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btn_SaveEdit.setText("Simpan");
        btn_SaveEdit.setFillClick(new java.awt.Color(30, 100, 150));
        btn_SaveEdit.setFillOriginal(new java.awt.Color(41, 128, 185));
        btn_SaveEdit.setFillOver(new java.awt.Color(36, 116, 170));
        btn_SaveEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SaveEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SaveEditActionPerformed(evt);
            }
        });

        btn_CancelEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btn_CancelEdit.setText("Batal");
        btn_CancelEdit.setFillClick(new java.awt.Color(200, 125, 0));
        btn_CancelEdit.setFillOriginal(new java.awt.Color(243, 156, 18));
        btn_CancelEdit.setFillOver(new java.awt.Color(230, 145, 10));
        btn_CancelEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_CancelEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancelEditActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Mongolian Baiti", 1, 22)); // NOI18N
        jLabel19.setText("Jumlah Stok");

        txt_stok1.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel20.setFont(new java.awt.Font("Mongolian Baiti", 1, 22)); // NOI18N
        jLabel20.setText("Gambar");

        txt_gambar1.setPreferredSize(new java.awt.Dimension(20, 22));

        btnPilihGambar1.setText("Pilih Gambar");
        btnPilihGambar1.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        btnPilihGambar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPilihGambar1ActionPerformed(evt);
            }
        });

        txt_kode1.setEnabled(false);
        txt_kode1.setPreferredSize(new java.awt.Dimension(20, 22));

        javax.swing.GroupLayout ShadowUtama2Layout = new javax.swing.GroupLayout(ShadowUtama2);
        ShadowUtama2.setLayout(ShadowUtama2Layout);
        ShadowUtama2Layout.setHorizontalGroup(
            ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_kode1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ShadowUtama2Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addContainerGap())
                    .addGroup(ShadowUtama2Layout.createSequentialGroup()
                        .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                                .addComponent(btnPilihGambar1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_gambar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(txt_nama1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txt_harga1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ShadowUtama2Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_SaveEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_CancelEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txt_stok1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel20)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel16))
                                .addGap(998, 998, 998)))
                        .addGap(0, 46, Short.MAX_VALUE))))
        );
        ShadowUtama2Layout.setVerticalGroup(
            ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtama2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_CancelEdit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .addComponent(btn_SaveEdit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_kode1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_nama1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_harga1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_stok1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ShadowUtama2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txt_gambar1, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(btnPilihGambar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(247, Short.MAX_VALUE))
        );

        panelEdit.add(ShadowUtama2, "card2");

        panelMain.add(panelEdit, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        setPanelEditFormData(selectedItem);
        // Ganti tampilan ke panelEdit
        panelMain.removeAll();
        panelMain.add(panelEdit);
        panelMain.repaint();
        panelMain.revalidate();

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliActionPerformed

    private void btn_CancelAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancelAddActionPerformed
        showPanel();
    }//GEN-LAST:event_btn_CancelAddActionPerformed

    private void btn_SaveAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SaveAddActionPerformed
        String kodeBrg = txt_kode.getText();
        String namaBrg = txt_nama.getText();
        String hargaBrg = txt_harga.getText();
        int harga = Integer.parseInt(hargaBrg);
        String stok = txt_stok.getText();
        int stock = Integer.parseInt(stok);

        // Baca gambar ke dalam byte array
        byte[] gambarData = null;
        if (selectedImageFile != null) {
            try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                gambarData = fis.readAllBytes(); // Jika pakai Java 8: pakai ByteArrayOutputStream
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal membaca file gambar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            String sql = "INSERT INTO data_barang (kode_barang, nama_barang, harga, stok, gambar) "
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            generate(kodeBrg);
            pst.setString(1, kodeBrg);
            pst.setString(2, namaBrg);
            pst.setDouble(3, harga);
            pst.setInt(4, stock);
            pst.setBytes(5, gambarData);
            pst.executeUpdate();
            clearForm();
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data : " + ex.getMessage());
        }
    }//GEN-LAST:event_btn_SaveAddActionPerformed

    private void btnPilihGambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilihGambarActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar", "jpg", "png", "jpeg", "gif"));

        int result = fileChooser.showOpenDialog(this);  // this = panel atau parent component
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            txt_gambar.setText(selectedImageFile.getName()); // tampilkan nama file
        }
    }//GEN-LAST:event_btnPilihGambarActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        panelMain.removeAll();
        panelMain.add(panelAdd);
        panelMain.repaint();
        panelMain.revalidate();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        if (selectedItem != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String sql = "DELETE FROM data_barang WHERE id_barang = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, selectedItem.getId());
                    int rowsAffected = pst.executeUpdate();
                    pst.close();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Barang berhasil dihapus.");
                        loadDataBarang();
                        selectedItem = null;
                        btnEdit.setEnabled(false);
                        btnHapus.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menghapus barang.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus barang :" + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih dulu barang yang ingin dihapus.");
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btn_SaveEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SaveEditActionPerformed
        try {
            int id = selectedItem.getId(); // Get the item ID to update
            String kodeBrg = txt_kode1.getText();
            String namaBrg = txt_nama1.getText();
            double harga = Double.parseDouble(txt_harga1.getText());
            int stok = Integer.parseInt(txt_stok1.getText());

            // Read the new image file if selected
            byte[] gambarData = null;
            if (selectedImageFile != null) {
                try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                    gambarData = fis.readAllBytes();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membaca file gambar : " + ex.getMessage());
                    return;
                }
            }

            String sql = "UPDATE data_barang SET kode_barang = ?, nama_barang = ?, harga = ?, stok = ?, gambar = ? WHERE id_barang = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kodeBrg);
            pst.setString(2, namaBrg);
            pst.setDouble(3, harga);
            pst.setInt(4, stok);
            pst.setBytes(5, gambarData);
            pst.setInt(6, id);
            pst.executeUpdate();
            clearForm();

            JOptionPane.showMessageDialog(this, "Data berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data : " + ex.getMessage());
        }

    }//GEN-LAST:event_btn_SaveEditActionPerformed

    private void btn_CancelEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancelEditActionPerformed
        showPanel();
    }//GEN-LAST:event_btn_CancelEditActionPerformed

    private void btnPilihGambar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilihGambar1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar", "jpg", "png", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(this);  // this = panel atau parent component
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            txt_gambar1.setText(selectedImageFile.getName()); // tampilkan nama file
        }
    }//GEN-LAST:event_btnPilihGambar1ActionPerformed

    private void txt_gambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_gambarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_gambarActionPerformed

    private void txt_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchActionPerformed

    private void btn_GenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_GenerateCodeActionPerformed
        txt_kode.setText(getRandomNumberString());
    }//GEN-LAST:event_btn_GenerateCodeActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchDataBarang(txt_search.getText());
    }//GEN-LAST:event_txt_searchKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowSearch;
    private component.ShadowPanel ShadowUtama;
    private component.ShadowPanel ShadowUtama1;
    private component.ShadowPanel ShadowUtama2;
    private component.Jbutton btnEdit;
    private component.Jbutton btnHapus;
    private component.Jbutton btnKembali;
    private component.Jbutton btnPilihGambar;
    private component.Jbutton btnPilihGambar1;
    private component.Jbutton btnTambah;
    private component.Jbutton btn_CancelAdd;
    private component.Jbutton btn_CancelEdit;
    private component.Jbutton btn_GenerateCode;
    private component.Jbutton btn_SaveAdd;
    private component.Jbutton btn_SaveEdit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel panelAdd;
    private javax.swing.JPanel panelBarang;
    private javax.swing.JPanel panelEdit;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JScrollPane scrollBarang;
    private component.ShadowPanel shadowPanel;
    private javax.swing.JTextField txt_gambar;
    private javax.swing.JTextField txt_gambar1;
    private javax.swing.JTextField txt_harga;
    private javax.swing.JTextField txt_harga1;
    private javax.swing.JTextField txt_kode;
    private javax.swing.JTextField txt_kode1;
    private javax.swing.JTextField txt_nama;
    private javax.swing.JTextField txt_nama1;
    private swing.TextField txt_search;
    private javax.swing.JTextField txt_stok;
    private javax.swing.JTextField txt_stok1;
    // End of variables declaration//GEN-END:variables
}
