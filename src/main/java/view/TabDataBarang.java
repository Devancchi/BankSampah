package view;

import component.Item;
import component.Jbutton;
import component.LoggerUtil;
import component.ModelItem;
import component.UserSession;
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
import notification.toast.Notifications;
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
    private final UserSession users;

    public TabDataBarang(UserSession user) {
        this.users = user;
        initComponents();
        initializePanel();
    }

    private void initializePanel() {
        btnKembali.setVisible(false);
        btnHapus.setVisible(false);

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
                        handleItemSelection(itemPanel, model);
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

            String sql = "SELECT * FROM data_barang WHERE nama_barang LIKE ? OR kode_barang LIKE ?";
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
                        handleItemSelection(itemPanel, model);
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
            File outputFile = new File("C:/Users/devan/Downloads/" + kode + "_" + cleanName + ".png");
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
        txt_kode.setText(item.getKode());    // Contoh, asumsi txt_kode1 di panelEdit
        txt_nama.setText(item.getNama());
        txt_harga.setText(String.valueOf(item.getHarga()));
        txt_stok.setText(String.valueOf(item.getStok()));
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabDataBarang(users));
        panelMain.repaint();
        panelMain.revalidate();
        btnHapus.setVisible(false);
        btnKembali.setVisible(false);
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
                        handleItemSelection(itemPanel, model);
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
        txt_gambar.setText("");
        txt_harga.setText("");
        txt_harga.setText("");
        txt_kode.setText("");
        txt_nama.setText("");
        txt_nama.setText("");
        txt_stok.setText("");
        txt_stok.setText("");
    }

    private void handleItemSelection(Item itemPanel, ModelItem model) {
        // Reset all items first
        for (Item panel : itemPanels) {
            panel.deselect();
        }

        // Then select the new item
        itemPanel.setSelected(true);
        itemPanel.repaint();

        // Update state
        selectedItem = model;
        btnTambah.setText("Ubah");
        btnTambah.setIcon(new ImageIcon("src\\main\\resources\\icon\\icon_edit.png"));
        btnTambah.setFillClick(new Color(30, 100, 150));
        btnTambah.setFillOriginal(new Color(41, 128, 185));
        btnTambah.setFillOver(new Color(36, 116, 170));
        btnTambah.setEnabled(true);
        btnHapus.setVisible(true);
        btnKembali.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        shadowBarang = new component.ShadowPanel();
        scrollBarang = new javax.swing.JScrollPane();
        panelBarang = new javax.swing.JPanel();
        lb_dataNasabah = new javax.swing.JLabel();
        shadowAction = new component.ShadowPanel();
        btnTambah = new component.Jbutton();
        btnHapus = new component.Jbutton();
        btnKembali = new component.Jbutton();
        txt_search = new component.PlaceholderTextField();
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

        setPreferredSize(new java.awt.Dimension(1192, 944));
        setLayout(new java.awt.CardLayout());

        panelMain.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(250, 250, 250));
        panelView.setPreferredSize(new java.awt.Dimension(1192, 944));

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
            .addGap(0, 1146, Short.MAX_VALUE)
        );
        panelBarangLayout.setVerticalGroup(
            panelBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 735, Short.MAX_VALUE)
        );

        scrollBarang.setViewportView(panelBarang);

        lb_dataNasabah.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lb_dataNasabah.setText("Data Barang");

        javax.swing.GroupLayout shadowBarangLayout = new javax.swing.GroupLayout(shadowBarang);
        shadowBarang.setLayout(shadowBarangLayout);
        shadowBarangLayout.setHorizontalGroup(
            shadowBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowBarangLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(shadowBarangLayout.createSequentialGroup()
                .addComponent(lb_dataNasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        shadowBarangLayout.setVerticalGroup(
            shadowBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowBarangLayout.createSequentialGroup()
                .addComponent(lb_dataNasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
                .addContainerGap())
        );

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

        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_hapus.png"))); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.setFillClick(new java.awt.Color(190, 30, 20));
        btnHapus.setFillOriginal(new java.awt.Color(231, 76, 60));
        btnHapus.setFillOver(new java.awt.Color(210, 50, 40));
        btnHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnHapusMouseClicked(evt);
            }
        });
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

        txt_search.setPlaceholder("Cari Barang");
        txt_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_searchKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout shadowActionLayout = new javax.swing.GroupLayout(shadowAction);
        shadowAction.setLayout(shadowActionLayout);
        shadowActionLayout.setHorizontalGroup(
            shadowActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowActionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 940, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTambah, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnKembali, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowActionLayout.setVerticalGroup(
            shadowActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowActionLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(shadowActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shadowAction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowAction, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(shadowBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        panelAdd.setPreferredSize(new java.awt.Dimension(1192, 944));
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
                        .addGap(0, 40, Short.MAX_VALUE))))
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
                .addContainerGap(451, Short.MAX_VALUE))
        );

        panelAdd.add(ShadowUtama1, "card2");

        panelMain.add(panelAdd, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        showPanel();
    }//GEN-LAST:event_btnKembaliActionPerformed

    private void btn_CancelAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancelAddActionPerformed
        showPanel();
    }//GEN-LAST:event_btn_CancelAddActionPerformed

    private void btn_SaveAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SaveAddActionPerformed
        if (btn_SaveAdd.getText().equals("Tambah")) {
            btn_SaveAdd.setText("Simpan");
        } else if (btn_SaveAdd.getText().equals("Simpan")) {
            insertData();
        } else if (btn_SaveAdd.getText().equals("Perbarui")) {
            updateData();
        }
    }//GEN-LAST:event_btn_SaveAddActionPerformed

    private void insertData() {
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
                gambarData = fis.readAllBytes();
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
            notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS, "Data berhasil disimpan.");
            LoggerUtil.insert(users.getId(), "Menambah Data Barang ID: " + kodeBrg);
            showPanel();
        } catch (SQLException ex) {
            Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
            notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING, "Gagal menyimpan data: " + ex.getMessage());
        }
    }

    private void updateData() {
        try {
            int id = selectedItem.getId();
            String kodeBrg = txt_kode.getText();
            String namaBrg = txt_nama.getText();
            double harga = Double.parseDouble(txt_harga.getText());
            int stok = Integer.parseInt(txt_stok.getText());

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
            notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS, "Data berhasil diupdate.");
            LoggerUtil.insert(users.getId(), "Mengupdate Data barang ID: " + kodeBrg);
            showPanel();
        } catch (SQLException ex) {
            Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
            notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING, "Gagal mengupdate data: " + ex.getMessage());
        }
    }

    private void btnPilihGambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilihGambarActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        // Set initial directory to desktop/dataBarang
        String userHome = System.getProperty("user.home");
        File initialDir = new File(userHome + "/Desktop/dataBarang");
        // Create directory if it doesn't exist
        if (!initialDir.exists()) {
            initialDir.mkdirs();
        }
        fileChooser.setCurrentDirectory(initialDir);
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

        if (btnTambah.getText().equals("Ubah")) {
            setPanelEditFormData(selectedItem);
            btn_SaveAdd.setText("Perbarui");
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                txt_kode.requestFocusInWindow();
            }
        });

        txt_kode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txt_nama.requestFocus();
                }
            }
        });

        txt_nama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txt_harga.requestFocus();
                }
            }
        });

        txt_harga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txt_stok.requestFocus();
                }
            }
        });

        txt_stok.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    btn_SaveAdd.requestFocus();
                }
            }
        });
    }//GEN-LAST:event_btnTambahActionPerformed

    private void txt_gambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_gambarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_gambarActionPerformed

    private void btn_GenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_GenerateCodeActionPerformed
        txt_kode.setText(getRandomNumberString());
    }//GEN-LAST:event_btn_GenerateCodeActionPerformed

    private void btnHapusMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnHapusMouseClicked
        if (selectedItem != null) {
            String kodeBrg = selectedItem.getKode(); // ⬅️ AMAN dan AKURAT

            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String sql = "DELETE FROM data_barang WHERE id_barang = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, selectedItem.getId());
                    int rowsAffected = pst.executeUpdate();
                    pst.close();

                    if (rowsAffected > 0) {
                        notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS, "Berhasil Menghapus Barang.");
                        loadDataBarang();
                        selectedItem = null;
                        btnTambah.setEnabled(false);
                        btnHapus.setEnabled(false);
                        LoggerUtil.insert(users.getId(), "Menghapus Data Barang Kode: " + kodeBrg); // ⬅️ log aman
                    } else {
                        notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING, "Gagal menghapus barang.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus barang :" + ex.getMessage());
                }
            }
        } else {
            notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING, "Pilih dulu barang yang ingin dihapus.");
        }
    }//GEN-LAST:event_btnHapusMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed

    }//GEN-LAST:event_btnHapusActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyTyped
        searchDataBarang(txt_search.getText());
    }//GEN-LAST:event_txt_searchKeyTyped

    private void resetFormAndButtons() {
        btnTambah.setText("Tambah");
        btnTambah.setIcon(new ImageIcon("src\\main\\resources\\icon\\icon_tambah.png"));
        btnTambah.setFillClick(new Color(55, 130, 60));
        btnTambah.setFillOriginal(new Color(76, 175, 80));
        btnTambah.setFillOver(new Color(69, 160, 75));
        btnTambah.setEnabled(false);
        btnHapus.setVisible(false);
        btnKembali.setVisible(false);
        selectedItem = null;
        clearForm();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowUtama1;
    private component.Jbutton btnHapus;
    private component.Jbutton btnKembali;
    private component.Jbutton btnPilihGambar;
    private component.Jbutton btnTambah;
    private component.Jbutton btn_CancelAdd;
    private component.Jbutton btn_GenerateCode;
    private component.Jbutton btn_SaveAdd;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lb_dataNasabah;
    private javax.swing.JPanel panelAdd;
    private javax.swing.JPanel panelBarang;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JScrollPane scrollBarang;
    private component.ShadowPanel shadowAction;
    private component.ShadowPanel shadowBarang;
    private javax.swing.JTextField txt_gambar;
    private javax.swing.JTextField txt_harga;
    private javax.swing.JTextField txt_kode;
    private javax.swing.JTextField txt_nama;
    private component.PlaceholderTextField txt_search;
    private javax.swing.JTextField txt_stok;
    // End of variables declaration//GEN-END:variables
}
