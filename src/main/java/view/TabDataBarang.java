package view;

import component.Item;
import component.Jbutton;
import component.LoggerUtil;
import component.ModelItem;
import component.UserSession;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import main.DBconnect;
import java.awt.image.BufferedImage;
import notification.toast.Notifications;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import component.ExcelExporter;
import java.text.DecimalFormat;

/**
 *
 * @author devan
 */
public class TabDataBarang extends javax.swing.JPanel {

    private final Connection conn = DBconnect.getConnection();
    private File selectedImageFile;
    private List<Item> itemPanels = new ArrayList<>();
    private ModelItem selectedItem = null; // menyimpan item yang dipilih
    private final UserSession users;

    // Pagination variables
    private int halamanSaatIni = 1;
    private int dataPerHalaman = 10;
    private int totalPages;
    private int totalData;

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

        // Set initial data per page from combobox
        dataPerHalaman = Integer.parseInt(cbx_data2.getSelectedItem().toString());

        // Initialize pagination
        calculateTotalPage();
        loadDataBarang();

        // Add pagination listeners
        btn_first2.addActionListener(e -> {
            halamanSaatIni = 1;
            loadDataBarang();
        });

        btn_before2.addActionListener(e -> {
            if (halamanSaatIni > 1) {
                halamanSaatIni--;
                loadDataBarang();
            }
        });

        btn_next2.addActionListener(e -> {
            if (halamanSaatIni < totalPages) {
                halamanSaatIni++;
                loadDataBarang();
            }
        });

        btn_last2.addActionListener(e -> {
            halamanSaatIni = totalPages;
            loadDataBarang();
        });

        cbx_data2.addActionListener(e -> {
            dataPerHalaman = Integer.parseInt(cbx_data2.getSelectedItem().toString());
            halamanSaatIni = 1;
            calculateTotalPage();
            loadDataBarang();
        });
    }

    private void calculateTotalPage() {
        try {
            String sql = "SELECT COUNT(*) as total FROM data_barang";
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

    private void loadDataBarang() {
        try {
            panelBarang.removeAll();
            // Clear the item panels list when reloading data
            itemPanels.clear();

            // Always use GridLayout regardless of item count
            panelBarang.setLayout(new java.awt.GridLayout(0, 5, 10, 19));

            int startIndex = (halamanSaatIni - 1) * dataPerHalaman;

            String sql = "SELECT * FROM data_barang LIMIT ? OFFSET ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, dataPerHalaman);
            pst.setInt(2, startIndex);
            ResultSet rs = pst.executeQuery();

            int itemCount = 0;
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

                // Set fixed size for each card
                itemPanel.setPreferredSize(new java.awt.Dimension(200, 300));
                itemPanel.setMinimumSize(new java.awt.Dimension(200, 300));
                itemPanel.setMaximumSize(new java.awt.Dimension(200, 300));

                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleItemSelection(itemPanel, model);
                    }
                });

                // Add this line to track the panel
                itemPanels.add(itemPanel);

                panelBarang.add(itemPanel);
                itemCount++;
            }

            // Always add empty panels to fill at least one row
            // This forces the grid layout to maintain proper structure
            int emptyPanelsNeeded = 5 - (itemCount % 5);
            if (emptyPanelsNeeded < 5) {
                for (int i = 0; i < emptyPanelsNeeded; i++) {
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setOpaque(false);
                    // Set consistent size for empty panels too
                    emptyPanel.setPreferredSize(new java.awt.Dimension(200, 300));
                    emptyPanel.setMinimumSize(new java.awt.Dimension(200, 300));
                    emptyPanel.setMaximumSize(new java.awt.Dimension(200, 300));
                    panelBarang.add(emptyPanel);
                }
            }

            rs.close();
            pst.close();

            // Update page label
            lb_halaman2.setText("Page " + halamanSaatIni + " dari total " + totalData + " data");

            panelBarang.revalidate();
            panelBarang.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchDataBarang(String keyword) {
        try {
            // Hitung total data hasil pencarian
            String sqlCount = "SELECT COUNT(*) as total FROM data_barang WHERE nama_barang LIKE ? OR kode_barang LIKE ?";
            PreparedStatement pstCount = conn.prepareStatement(sqlCount);
            String likeKeyword = "%" + keyword + "%";
            pstCount.setString(1, likeKeyword);
            pstCount.setString(2, likeKeyword);
            ResultSet rsCount = pstCount.executeQuery();
            if (rsCount.next()) {
                totalData = rsCount.getInt("total");
                totalPages = (int) Math.ceil((double) totalData / dataPerHalaman);
            }
            rsCount.close();
            pstCount.close();

            panelBarang.removeAll();
            // Clear the item panels list when searching
            itemPanels.clear();

            // Always use GridLayout regardless of item count
            panelBarang.setLayout(new java.awt.GridLayout(0, 5, 10, 19));

            String sql = "SELECT * FROM data_barang WHERE nama_barang LIKE ? OR kode_barang LIKE ? LIMIT ? OFFSET ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, likeKeyword);
            pst.setString(2, likeKeyword);
            pst.setInt(3, dataPerHalaman);
            pst.setInt(4, (halamanSaatIni - 1) * dataPerHalaman);

            ResultSet rs = pst.executeQuery();

            int itemCount = 0;
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
                // Set a fixed size for each card
                itemPanel.setPreferredSize(new java.awt.Dimension(200, 300));
                itemPanel.setMinimumSize(new java.awt.Dimension(200, 300));
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleItemSelection(itemPanel, model);
                    }
                });
                itemPanels.add(itemPanel);

                panelBarang.add(itemPanel);
                itemCount++;
            }

            // In searchDataBarang method - modify the empty panels section
            // Always add empty panels to fill at least one row
            int emptyPanelsNeeded = 5 - (itemCount % 5);
            if (emptyPanelsNeeded < 5) {
                for (int i = 0; i < emptyPanelsNeeded; i++) {
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setOpaque(false);
                    // Set consistent size for empty panels too
                    emptyPanel.setPreferredSize(new java.awt.Dimension(200, 300));
                    emptyPanel.setMinimumSize(new java.awt.Dimension(200, 300));
                    emptyPanel.setMaximumSize(new java.awt.Dimension(200, 300));
                    panelBarang.add(emptyPanel);
                }
            }

            rs.close();
            pst.close();

            // Update page label
            lb_halaman2.setText("Halaman " + halamanSaatIni + " dari total " + totalData + " data");

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

            // Gunakan folder di desktop untuk menyimpan barcode
            String userHome = System.getProperty("user.home");
            File barcodeDir = new File(userHome + "/Desktop/barcode");

            // Buat folder barcode di desktop jika belum ada
            if (!barcodeDir.exists()) {
                barcodeDir.mkdirs();
            }

            // Output file ke folder desktop/barcode
            File outputFile = new File(barcodeDir, kode + "_" + cleanName + ".png");

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
        txt_kode.setText(item.getKode()); // Contoh, asumsi txt_kode1 di panelEdit
        txt_nama.setText(item.getNama());
        txt_harga.setText(String.valueOf((int) item.getHarga()));
        txt_stok.setText(String.valueOf(item.getStok()));

        // Reset selectedImageFile to null
        selectedImageFile = null;

        try {
            // Cari file gambar yang sesuai di Desktop/dataBarang
            String userHome = System.getProperty("user.home");
            File dataBarangDir = new File(userHome + "/Desktop/dataBarang");

            if (dataBarangDir.exists()) {
                // Mencari file dengan pattern kodeBarang_*
                String kodeBrg = item.getKode();
                File[] matchingFiles = dataBarangDir
                        .listFiles(file -> file.isFile() && file.getName().startsWith(kodeBrg + "_"));

                if (matchingFiles != null && matchingFiles.length > 0) {
                    // Gunakan file pertama yang ditemukan
                    selectedImageFile = matchingFiles[0];
                    txt_gambar.setText(selectedImageFile.getName());
                }
            }

            // Jika tidak ada file yang ditemukan
            if (selectedImageFile == null) {
                txt_gambar.setText("(Tidak ada gambar)");
            }
        } catch (Exception ex) {
            System.out.println("Gagal mencari gambar: " + ex.getMessage());
            txt_gambar.setText("(Error memuat gambar)");
        }
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabDataBarang(users));
        panelMain.repaint();
        panelMain.revalidate();
        btnHapus.setVisible(false);
        btnKembali.setVisible(false);
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
        btnTambah.setFillClick(new java.awt.Color(30, 100, 150));
        btnTambah.setFillOriginal(new java.awt.Color(41, 128, 185));
        btnTambah.setFillOver(new java.awt.Color(36, 116, 170));
        btnTambah.setEnabled(true);
        btnHapus.setVisible(true);
        btnKembali.setVisible(true);
    }

    /**
     * Checks if a kode_barang or nama_barang already exists in the database
     * 
     * @param kode      The item code to check
     * @param nama      The item name to check
     * @param excludeId Optional ID to exclude from the check (for updates)
     * @return A string indicating the duplicate field ("kode", "nama", or null if
     *         no duplicates)
     */
    private String checkDuplicateBarang(String kode, String nama, int excludeId) {
        try {
            // Check for duplicate kode_barang
            String sqlKode = "SELECT COUNT(*) FROM data_barang WHERE kode_barang = ? AND id_barang != ?";
            PreparedStatement pstKode = conn.prepareStatement(sqlKode);
            pstKode.setString(1, kode);
            pstKode.setInt(2, excludeId);

            ResultSet rsKode = pstKode.executeQuery();
            if (rsKode.next() && rsKode.getInt(1) > 0) {
                return "kode";
            }

            // Check for duplicate nama_barang
            String sqlNama = "SELECT COUNT(*) FROM data_barang WHERE nama_barang = ? AND id_barang != ?";
            PreparedStatement pstNama = conn.prepareStatement(sqlNama);
            pstNama.setString(1, nama);
            pstNama.setInt(2, excludeId);

            ResultSet rsNama = pstNama.executeQuery();
            if (rsNama.next() && rsNama.getInt(1) > 0) {
                return "nama";
            }

            return null; // No duplicates found
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete old files associated with a product when updating
     * 
     * @param oldKode the old product code
     */
    private void deleteOldFiles(String oldKode) {
        try {
            String userHome = System.getProperty("user.home");

            // Delete old barcode files
            File barcodeDir = new File(userHome + "/Desktop/barcode");
            if (barcodeDir.exists()) {
                File[] oldBarcodes = barcodeDir
                        .listFiles((dir, name) -> name.startsWith(oldKode + ".") || name.startsWith(oldKode + "_"));
                if (oldBarcodes != null) {
                    for (File file : oldBarcodes) {
                        if (file.delete()) {
                            System.out.println("Berhasil menghapus barcode lama: " + file.getName());
                        } else {
                            System.out.println("Gagal menghapus barcode lama: " + file.getName());
                        }
                    }
                }
            }

            // Delete old image files
            File dataBarangDir = new File(userHome + "/Desktop/dataBarang");
            if (dataBarangDir.exists()) {
                File[] oldImages = dataBarangDir.listFiles((dir, name) -> name.startsWith(oldKode + "_"));
                if (oldImages != null) {
                    for (File file : oldImages) {
                        if (file.delete()) {
                            System.out.println("Berhasil menghapus gambar lama: " + file.getName());
                        } else {
                            System.out.println("Gagal menghapus gambar lama: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error saat menghapus file lama: " + ex.getMessage());
        }
    }

    /**
     * Delete only old image files associated with a product without deleting
     * barcodes
     * 
     * @param oldKode the old product code
     */
    private void deleteOldImageOnly(String oldKode) {
        try {
            String userHome = System.getProperty("user.home");

            // Delete only old image files, not barcode files
            File dataBarangDir = new File(userHome + "/Desktop/dataBarang");
            if (dataBarangDir.exists()) {
                File[] oldImages = dataBarangDir.listFiles((dir, name) -> name.startsWith(oldKode + "_"));
                if (oldImages != null) {
                    for (File file : oldImages) {
                        if (file.delete()) {
                            System.out.println("Berhasil menghapus gambar lama: " + file.getName());
                        } else {
                            System.out.println("Gagal menghapus gambar lama: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error saat menghapus file gambar lama: " + ex.getMessage());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        shadowBarang = new component.ShadowPanel();
        scrollBarang = new javax.swing.JScrollPane();
        panelBarang = new javax.swing.JPanel();
        lb_dataNasabah = new javax.swing.JLabel();
        panelBawah2 = new component.ShadowPanel();
        lb_halaman2 = new javax.swing.JLabel();
        btn_before2 = new javax.swing.JButton();
        cbx_data2 = new javax.swing.JComboBox<>();
        btn_next2 = new javax.swing.JButton();
        btn_last2 = new javax.swing.JButton();
        btn_first2 = new javax.swing.JButton();
        btn_Export2 = new component.Jbutton();
        btn_import2 = new component.Jbutton();
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

        panelView.setBackground(new java.awt.Color(253, 253, 253));
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
                        .addGap(0, 1152, Short.MAX_VALUE));
        panelBarangLayout.setVerticalGroup(
                panelBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 679, Short.MAX_VALUE));

        scrollBarang.setViewportView(panelBarang);

        lb_dataNasabah.setFont(lb_dataNasabah.getFont().deriveFont(
                lb_dataNasabah.getFont().getStyle() | java.awt.Font.BOLD, lb_dataNasabah.getFont().getSize() + 10));
        lb_dataNasabah.setText("Data Barang");

        lb_halaman2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lb_halaman2.setText("hal");

        btn_before2.setText("<");

        cbx_data2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10" }));

        btn_next2.setText(">");

        btn_last2.setText("Last Page");
        btn_last2.setMaximumSize(new java.awt.Dimension(100, 23));
        btn_last2.setName(""); // NOI18N
        btn_last2.setPreferredSize(new java.awt.Dimension(100, 23));

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
                                .addComponent(btn_Export2, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_import2, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lb_halaman2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_first2, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_before2, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbx_data2, javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_next2, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_last2, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)));
        panelBawah2Layout.setVerticalGroup(
                panelBawah2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBawah2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelBawah2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_Export2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lb_halaman2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_first2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_before2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbx_data2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_next2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_last2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_import2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        javax.swing.GroupLayout shadowBarangLayout = new javax.swing.GroupLayout(shadowBarang);
        shadowBarang.setLayout(shadowBarangLayout);
        shadowBarangLayout.setHorizontalGroup(
                shadowBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelBawah2, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(lb_dataNasabah, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        shadowBarangLayout.setVerticalGroup(
                shadowBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(shadowBarangLayout.createSequentialGroup()
                                .addComponent(lb_dataNasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 44,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(scrollBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelBawah2, javax.swing.GroupLayout.PREFERRED_SIZE, 50,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        btnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btnTambah.setText("Tambah");
        btnTambah.setFillClick(new java.awt.Color(55, 130, 60));
        btnTambah.setFillOriginal(new java.awt.Color(76, 175, 80));
        btnTambah.setFillOver(new java.awt.Color(69, 160, 75));
        btnTambah.setFont(btnTambah.getFont().deriveFont(btnTambah.getFont().getStyle() | java.awt.Font.BOLD,
                btnTambah.getFont().getSize() - 1));
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
        btnHapus.setFont(btnHapus.getFont().deriveFont(btnHapus.getFont().getStyle() | java.awt.Font.BOLD,
                btnHapus.getFont().getSize() - 1));
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
        btnKembali.setFont(btnKembali.getFont().deriveFont(btnKembali.getFont().getStyle() | java.awt.Font.BOLD,
                btnKembali.getFont().getSize() - 1));
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
                                .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 872,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnTambah, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnKembali, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                .addContainerGap()));
        shadowActionLayout.setVerticalGroup(
                shadowActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowActionLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(shadowActionLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txt_search, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(30, Short.MAX_VALUE)));

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
                panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelViewLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(shadowBarang, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(shadowAction, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(20, 20, 20)));
        panelViewLayout.setVerticalGroup(
                panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelViewLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(shadowAction, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(shadowBarang, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(20, 20, 20)));

        panelMain.add(panelView, "card2");

        panelAdd.setPreferredSize(new java.awt.Dimension(1192, 944));
        panelAdd.setLayout(new java.awt.CardLayout());

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getStyle() | java.awt.Font.BOLD,
                jLabel6.getFont().getSize() + 10));
        jLabel6.setText("Tambah Data Barang");

        jLabel11.setFont(jLabel11.getFont().deriveFont(jLabel11.getFont().getSize() + 10f));
        jLabel11.setText("Kode Barang");

        jLabel12.setFont(jLabel12.getFont().deriveFont(jLabel12.getFont().getSize() + 10f));
        jLabel12.setText("Nama Barang");

        txt_nama.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel13.setFont(jLabel13.getFont().deriveFont(jLabel13.getFont().getSize() + 10f));
        jLabel13.setText("Harga");

        txt_harga.setPreferredSize(new java.awt.Dimension(20, 22));
        txt_harga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_hargaKeyTyped(evt);
            }
        });

        btn_SaveAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_simpan.png"))); // NOI18N
        btn_SaveAdd.setText("Simpan");
        btn_SaveAdd.setFillClick(new java.awt.Color(30, 100, 150));
        btn_SaveAdd.setFillOriginal(new java.awt.Color(41, 128, 185));
        btn_SaveAdd.setFillOver(new java.awt.Color(36, 116, 170));
        btn_SaveAdd.setFont(btn_SaveAdd.getFont().deriveFont(btn_SaveAdd.getFont().getStyle() | java.awt.Font.BOLD,
                btn_SaveAdd.getFont().getSize() - 1));
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
        btn_CancelAdd.setFont(btn_CancelAdd.getFont().deriveFont(
                btn_CancelAdd.getFont().getStyle() | java.awt.Font.BOLD, btn_CancelAdd.getFont().getSize() - 1));
        btn_CancelAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancelAddActionPerformed(evt);
            }
        });

        jLabel14.setFont(jLabel14.getFont().deriveFont(jLabel14.getFont().getSize() + 10f));
        jLabel14.setText("Jumlah Stok");

        txt_stok.setPreferredSize(new java.awt.Dimension(20, 22));
        txt_stok.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_stokKeyTyped(evt);
            }
        });

        jLabel15.setFont(jLabel15.getFont().deriveFont(jLabel15.getFont().getSize() + 10f));
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
                                .addGroup(ShadowUtama1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                                .addGroup(ShadowUtama1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                                                .addComponent(btnPilihGambar,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 111,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(txt_gambar,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 1003,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(txt_nama, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txt_harga, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                ShadowUtama1Layout.createSequentialGroup()
                                                                        .addComponent(jLabel6)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(btn_SaveAdd,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                79,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(btn_CancelAdd,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                79,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(txt_stok, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel14)
                                                        .addComponent(jLabel15)
                                                        .addComponent(jLabel12)
                                                        .addComponent(jLabel11)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                ShadowUtama1Layout.createSequentialGroup()
                                                                        .addComponent(btn_GenerateCode,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                111,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(txt_kode,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                1003,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 40, Short.MAX_VALUE)))));
        ShadowUtama1Layout.setVerticalGroup(
                ShadowUtama1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ShadowUtama1Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(ShadowUtama1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(btn_CancelAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 50,
                                                Short.MAX_VALUE)
                                        .addGroup(ShadowUtama1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                                                .addComponent(btn_SaveAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 43,
                                                        Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(ShadowUtama1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txt_kode, javax.swing.GroupLayout.DEFAULT_SIZE, 36,
                                                Short.MAX_VALUE)
                                        .addComponent(btn_GenerateCode, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(13, 13, 13)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_harga, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_stok, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(ShadowUtama1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txt_gambar, javax.swing.GroupLayout.DEFAULT_SIZE, 36,
                                                Short.MAX_VALUE)
                                        .addComponent(btnPilihGambar, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(431, Short.MAX_VALUE)));

        panelAdd.add(ShadowUtama1, "card2");

        panelMain.add(panelAdd, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void txt_hargaKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_hargaKeyTyped
        char c = evt.getKeyChar();

        // Allow only digits, decimal point, and control characters (backspace, delete,
        // etc.)
        if ((c == '-') || // No minus sign
                (c != '.' && !Character.isDigit(c) && !Character.isISOControl(c))) {
            evt.consume(); // Ignore invalid characters
        }

        // Only allow one decimal point
        if (c == '.') {
            evt.consume(); // Abaikan semua titik desimal
        }
    }// GEN-LAST:event_txt_hargaKeyTyped

    private void txt_stokKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_stokKeyTyped
        char c = evt.getKeyChar();

        // Allow only digits, decimal point, and control characters (backspace, delete,
        // etc.)
        if ((c == '-') || // No minus sign
                (c != '.' && !Character.isDigit(c) && !Character.isISOControl(c))) {
            evt.consume(); // Ignore invalid characters
        }

        if (c == '.') {
            evt.consume(); // Abaikan semua titik desimal
        }
    }// GEN-LAST:event_txt_stokKeyTyped

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnKembaliActionPerformed
        showPanel();
    }// GEN-LAST:event_btnKembaliActionPerformed

    private void btn_CancelAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_CancelAddActionPerformed
        showPanel();
    }// GEN-LAST:event_btn_CancelAddActionPerformed

    private void btn_SaveAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_SaveAddActionPerformed
        if (btn_SaveAdd.getText().equals("Tambah")) {
            btn_SaveAdd.setText("Simpan");
        } else if (btn_SaveAdd.getText().equals("Simpan")) {
            insertData();
        } else if (btn_SaveAdd.getText().equals("Perbarui")) {
            updateData();
        }
    }// GEN-LAST:event_btn_SaveAddActionPerformed

    private void insertData() {
        String kodeBrg = txt_kode.getText().trim();
        String namaBrg = txt_nama.getText().trim();

        // Validasi kode barang dan nama barang tidak boleh sama
        if (kodeBrg.equalsIgnoreCase(namaBrg)) {
            JOptionPane.showMessageDialog(this, "Kode barang dan nama barang tidak boleh sama!",
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi kode barang dan nama barang tidak boleh kosong
        if (kodeBrg.isEmpty() || namaBrg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode barang dan nama barang harus diisi!",
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi kode barang dan nama barang tidak boleh duplikat di database
        String duplikat = checkDuplicateBarang(kodeBrg, namaBrg, -1); // -1 because we're inserting new record
        if (duplikat != null) {
            if (duplikat.equals("kode")) {
                JOptionPane.showMessageDialog(this, "Kode barang '" + kodeBrg + "' sudah digunakan!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nama barang '" + namaBrg + "' sudah digunakan!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // Validasi format harga and stok
        try {
            String hargaBrg = txt_harga.getText().trim();
            int harga = Integer.parseInt(hargaBrg);
            String stokText = txt_stok.getText().trim();
            int stock = Integer.parseInt(stokText);

            if (harga < 0) {
                JOptionPane.showMessageDialog(this, "Harga tidak boleh negatif!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stok tidak boleh negatif!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Baca gambar ke dalam byte array
            byte[] gambarData = null;
            String destinationFileName = "";

            if (selectedImageFile != null) {
                try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                    gambarData = fis.readAllBytes();

                    // Sanitasi nama barang untuk nama file
                    String cleanName = namaBrg
                            .replaceAll("[\\\\/:*?\"<>|]", "") // Hapus karakter ilegal
                            .replace(" ", "_"); // Ganti spasi dengan underscore

                    // Save image to Desktop/dataBarang
                    String userHome = System.getProperty("user.home");
                    File dataBarangDir = new File(userHome + "/Desktop/dataBarang");

                    // Buat folder dataBarang di desktop jika belum ada
                    if (!dataBarangDir.exists()) {
                        dataBarangDir.mkdirs();
                    }

                    // Format nama file: kodeBarang_namaBarang.extension
                    String extension = selectedImageFile.getName()
                            .substring(selectedImageFile.getName().lastIndexOf('.'));
                    destinationFileName = kodeBrg + "_" + cleanName + extension;
                    File destinationFile = new File(dataBarangDir, destinationFileName);

                    // Copy file to Desktop/dataBarang
                    Files.copy(selectedImageFile.toPath(), destinationFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Gambar tersimpan di: " + destinationFile.getPath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membaca file gambar: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
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
                pst.setBytes(5, gambarData); // gambarData bisa null jika tidak ada gambar
                pst.executeUpdate();
                clearForm();
                notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS,
                        "Data berhasil disimpan.");
                LoggerUtil.insert(users.getId(), "Menambah Data Barang ID: " + kodeBrg);
                showPanel();
            } catch (SQLException ex) {
                Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
                notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING,
                        "Gagal menyimpan data: " + ex.getMessage());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format harga atau stok tidak valid! Masukkan angka yang valid.",
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateData() {
        try {
            int id = selectedItem.getId();
            String kodeBrg = txt_kode.getText().trim();
            String namaBrg = txt_nama.getText().trim();

            // Validasi kode barang dan nama barang tidak boleh sama
            if (kodeBrg.equalsIgnoreCase(namaBrg)) {
                JOptionPane.showMessageDialog(this, "Kode barang dan nama barang tidak boleh sama!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validasi kode barang dan nama barang tidak boleh kosong
            if (kodeBrg.isEmpty() || namaBrg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kode barang dan nama barang harus diisi!",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validasi kode barang dan nama barang tidak boleh duplikat di database
            // Kecuali untuk item yang sedang diupdate (exclude item dengan ID yang sama)
            String duplikat = checkDuplicateBarang(kodeBrg, namaBrg, id);
            if (duplikat != null) {
                if (duplikat.equals("kode")) {
                    JOptionPane.showMessageDialog(this, "Kode barang '" + kodeBrg + "' sudah digunakan oleh item lain!",
                            "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Nama barang '" + namaBrg + "' sudah digunakan oleh item lain!",
                            "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                }
                return;
            }

            // Validasi format harga dan stok
            try {
                double harga = Double.parseDouble(txt_harga.getText().trim());
                int stok = Integer.parseInt(txt_stok.getText().trim());

                if (harga < 0) {
                    JOptionPane.showMessageDialog(this, "Harga tidak boleh negatif!",
                            "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (stok < 0) {
                    JOptionPane.showMessageDialog(this, "Stok tidak boleh negatif!",
                            "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Read the new image file if selected
                byte[] gambarData = null;
                if (selectedImageFile != null) {
                    try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                        gambarData = fis.readAllBytes();

                        // Sanitasi nama barang untuk nama file
                        String cleanName = namaBrg
                                .replaceAll("[\\\\/:*?\"<>|]", "") // Hapus karakter ilegal
                                .replace(" ", "_"); // Ganti spasi dengan underscore

                        // Save image to Desktop/dataBarang
                        String userHome = System.getProperty("user.home");
                        File dataBarangDir = new File(userHome + "/Desktop/dataBarang");

                        // Buat folder dataBarang di desktop jika belum ada
                        if (!dataBarangDir.exists()) {
                            dataBarangDir.mkdirs();
                        }

                        // Format nama file: kodeBarang_namaBarang.extension
                        String extension = selectedImageFile.getName()
                                .substring(selectedImageFile.getName().lastIndexOf('.'));
                        String destinationFileName = kodeBrg + "_" + cleanName + extension;
                        File destinationFile = new File(dataBarangDir, destinationFileName);

                        // Copy file to Desktop/dataBarang
                        Files.copy(selectedImageFile.toPath(), destinationFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Gambar tersimpan di: " + destinationFile.getPath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Gagal membaca file gambar : " + ex.getMessage());
                        return;
                    }
                } else {
                    // Jika tidak ada gambar baru dipilih, coba ambil gambar lama dari database
                    try {
                        String sqlGetOldImage = "SELECT gambar FROM data_barang WHERE id_barang = ?";
                        PreparedStatement pstGetOld = conn.prepareStatement(sqlGetOldImage);
                        pstGetOld.setInt(1, id);
                        ResultSet rs = pstGetOld.executeQuery();
                        if (rs.next()) {
                            gambarData = rs.getBytes("gambar");
                        }
                        rs.close();
                        pstGetOld.close();
                    } catch (SQLException ex) {
                        System.out.println("Gagal mengambil gambar lama: " + ex.getMessage());
                        // Lanjutkan proses update meskipun gagal mengambil gambar lama
                    }
                }

                // Get the old code before updating
                String oldKode = selectedItem.getKode();

                if (!kodeBrg.equals(oldKode)) {
                    // If the product code is changed, delete all old files and generate new barcode
                    deleteOldFiles(oldKode);
                    generate(kodeBrg);
                } else if (selectedImageFile != null) {
                    // If only the image is changed (code stays the same), only delete old images,
                    // not barcodes
                    deleteOldImageOnly(oldKode);
                }

                String sql = "UPDATE data_barang SET kode_barang = ?, nama_barang = ?, harga = ?, stok = ?, gambar = ? WHERE id_barang = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, kodeBrg);
                pst.setString(2, namaBrg);
                pst.setDouble(3, harga);
                pst.setInt(4, stok);
                pst.setBytes(5, gambarData); // gambarData bisa null jika tidak ada gambar
                pst.setInt(6, id);
                pst.executeUpdate();
                clearForm();
                notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS,
                        "Data berhasil diupdate.");
                LoggerUtil.insert(users.getId(), "Mengupdate Data barang ID: " + kodeBrg);
                showPanel();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Format harga atau stok tidak valid! Masukkan angka yang valid.",
                        "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TabDataBarang.class.getName()).log(Level.SEVERE, null, ex);
            notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING,
                    "Gagal mengupdate data: " + ex.getMessage());
        }
    }

    private void btnPilihGambarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPilihGambarActionPerformed
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

        int result = fileChooser.showOpenDialog(this); // this = panel atau parent component
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            txt_gambar.setText(selectedImageFile.getName()); // tampilkan nama file
        }
    }// GEN-LAST:event_btnPilihGambarActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTambahActionPerformed
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
    }// GEN-LAST:event_btnTambahActionPerformed

    private void txt_gambarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txt_gambarActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txt_gambarActionPerformed

    private void btn_GenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_GenerateCodeActionPerformed
        txt_kode.setText(getRandomNumberString());
    }// GEN-LAST:event_btn_GenerateCodeActionPerformed

    private void btnHapusMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnHapusMouseClicked
        if (selectedItem != null) {
            String kodeBrg = selectedItem.getKode(); // ⬅️ AMAN dan AKURAT

            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String sql = "DELETE FROM data_barang WHERE id_barang = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, selectedItem.getId());
                    int rowsAffected = pst.executeUpdate();
                    pst.close();

                    if (rowsAffected > 0) {
                        deleteOldFiles(kodeBrg);

                        notification.toast.Notifications.getInstance().show(Notifications.Type.SUCCESS,
                                "Berhasil Menghapus Barang.");
                        loadDataBarang();
                        selectedItem = null;
                        btnTambah.setEnabled(false);
                        btnHapus.setEnabled(false);
                        showPanel();
                        LoggerUtil.insert(users.getId(), "Menghapus Data Barang Kode: " + kodeBrg); // ⬅️ log aman
                    } else {
                        notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING,
                                "Gagal menghapus barang.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus barang :" + ex.getMessage());
                }
            }
        } else {
            notification.toast.Notifications.getInstance().show(Notifications.Type.WARNING,
                    "Pilih dulu barang yang ingin dihapus.");
        }
    }// GEN-LAST:event_btnHapusMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHapusActionPerformed

    }// GEN-LAST:event_btnHapusActionPerformed

    private void txt_searchKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txt_searchKeyTyped
        searchDataBarang(txt_search.getText());
    }// GEN-LAST:event_txt_searchKeyTyped

    private void btn_Export2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_Export2ActionPerformed
        try {
            // Siapkan model dan ambil data barang
            DefaultTableModel model = new DefaultTableModel(
                    new String[] { "ID", "Kode Barang", "Nama Barang", "Harga", "Stok" }, 0);
            getAllBarangData(model); // Ambil data dari DB ke model

            // Cek jika tidak ada data
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Pilih lokasi penyimpanan file
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file Excel");
            chooser.setSelectedFile(new File("data_barang.xls")); // Nama default

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

            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();

                // Konfirmasi sebelum import
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Apakah Anda yakin ingin mengimport data dari file ini?\n" +
                                "Data yang sudah ada dengan ID yang sama akan diupdate.",
                        "Konfirmasi Import",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    importExcelToDatabase(selectedFile);
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

    private void getAllBarangData(DefaultTableModel model) {
        try {
            String sql = "SELECT * FROM data_barang";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_barang");
                String kode = rs.getString("kode_barang");
                String nama = rs.getString("nama_barang");
                double harga = rs.getDouble("harga");
                int stok = rs.getInt("stok");

                model.addRow(new Object[] {
                        id,
                        kode,
                        nama,
                        harga,
                        stok
                });
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void importExcelToDatabase(File excelFile) {
        try {
            // Baca file Excel
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            int insertCount = 0;
            int skippedCount = 0;

            // Mulai dari baris kedua (indeks 1) karena baris pertama adalah header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String kodeBarang = getCellValueAsString(row.getCell(1)).trim();
                String namaBarang = getCellValueAsString(row.getCell(2)).trim();
                double harga = getCellValueAsDouble(row.getCell(3));
                int stok = (int) getCellValueAsDouble(row.getCell(4));

                // Lewati baris header yang ikut terbaca
                if (kodeBarang.equalsIgnoreCase("Kode Barang") || namaBarang.equalsIgnoreCase("Nama Barang")) {
                    continue;
                }

                // Validasi: Lewati jika kodeBarang atau namaBarang kosong
                if (kodeBarang.isEmpty() || namaBarang.isEmpty()) {
                    continue;
                }

                // Cek apakah data sudah ada
                String checkSql = "SELECT id_barang FROM data_barang WHERE kode_barang = ?";
                PreparedStatement checkPst = conn.prepareStatement(checkSql);
                checkPst.setString(1, kodeBarang);
                ResultSet rs = checkPst.executeQuery();

                if (rs.next()) {
                    // Data sudah ada, skip
                    skippedCount++;
                } else {
                    // Insert data baru dengan gambar default
                    String insertSql = "INSERT INTO data_barang (kode_barang, nama_barang, harga, stok, gambar) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertPst = conn.prepareStatement(insertSql);
                    insertPst.setString(1, kodeBarang);
                    insertPst.setString(2, namaBarang);
                    insertPst.setDouble(3, harga);
                    insertPst.setInt(4, stok);
                    insertPst.setBytes(5, null);
                    insertPst.executeUpdate();
                    insertPst.close();
                    insertCount++;
                }

                rs.close();
                checkPst.close();
            }

            workbook.close();

            // Refresh tampilan
            calculateTotalPage();
            loadDataBarang();

            String message = String.format(
                    "Import selesai!\n" +
                            "Data baru: %d\n" +
                            "Data dilewati (sudah ada): %d",
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
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null)
            return 0.0;
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
    private component.ShadowPanel ShadowUtama1;
    private component.Jbutton btnHapus;
    private component.Jbutton btnKembali;
    private component.Jbutton btnPilihGambar;
    private component.Jbutton btnTambah;
    private component.Jbutton btn_CancelAdd;
    private component.Jbutton btn_Export2;
    private component.Jbutton btn_GenerateCode;
    private component.Jbutton btn_SaveAdd;
    private javax.swing.JButton btn_before2;
    private javax.swing.JButton btn_first2;
    private component.Jbutton btn_import2;
    private javax.swing.JButton btn_last2;
    private javax.swing.JButton btn_next2;
    private javax.swing.JComboBox<String> cbx_data2;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lb_dataNasabah;
    private javax.swing.JLabel lb_halaman2;
    private javax.swing.JPanel panelAdd;
    private javax.swing.JPanel panelBarang;
    private component.ShadowPanel panelBawah2;
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
