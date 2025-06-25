/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import component.Jbutton;
import component.Table;
import component.UserSession;
import java.awt.Color;
import java.awt.event.KeyEvent;
import component.LoggerUtil;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import notification.toast.Notifications;

/**
 *
 * @author zal
 */
public class TabTransaksi extends javax.swing.JPanel {

    private static final NumberFormat Rp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    static {
        Rp.setMaximumFractionDigits(0);
        Rp.setMinimumFractionDigits(0);
    }
    private int harga, total;
    private final UserSession users;

    /**
     * Creates new form ManajemenNasabah
     */
    public TabTransaksi(UserSession user) {
        this.users = user;
        initComponents();
        btnbatal.setVisible(false);

        // Remove existing document and key listeners from scanbarang
        scanbarang.getDocument().removeDocumentListener(null);
        for (java.awt.event.KeyListener kl : scanbarang.getKeyListeners()) {
            scanbarang.removeKeyListener(kl);
        }

        // Add document listener with timer for automatic processing
        scanbarang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer delayTimer;

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                startTimer();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                startTimer();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                startTimer();
            }

            private void startTimer() {
                if (delayTimer != null && delayTimer.isRunning()) {
                    delayTimer.restart();
                } else {
                    delayTimer = new javax.swing.Timer(500, evt -> {
                        String barcode = scanbarang.getText().trim();
                        if (!barcode.isEmpty()) {
                            fetchProductInfo(barcode);
                            txtqty.requestFocus(); // Move focus to quantity field
                        }
                    });
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                }
            }
        });

        // Set up enter key behavior for txtqty
        txtqty.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume(); // Prevent default Enter key behavior
                    btntambah.doClick(); // Programmatically click the add button
                }
            }
        });

        // Add validation to prevent negative values in quantity field
        txtqty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Only allow digits, backspace and delete
                if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    evt.consume();
                }
                // Prevent leading zero when entering a second digit
                if (c == '0' && txtqty.getText().isEmpty()) {
                    // Allow single '0'
                } else if (txtqty.getText().equals("0") && Character.isDigit(c)) {
                    evt.consume();
                }
            }
        });

        // Add validation to prevent negative values in tunai field
        txttunai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Only allow digits, backspace and delete
                if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    evt.consume();
                }
                // Prevent leading zeros
                if (c == '0' && txttunai.getText().isEmpty()) {
                    // Allow single '0'
                } else if (txttunai.getText().equals("0") && Character.isDigit(c)) {
                    evt.consume();
                }
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Calculate change amount whenever tunai value changes
                hitungKembalian();
            }
        });

        // Add mouse listener to tabletransaksi
        tabletransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabletransaksi.getSelectedRow();
                btnbatal.setVisible(row != -1);
            }
        });

        // Add selection listener to handle deselection
        tabletransaksi.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    int row = tabletransaksi.getSelectedRow();
                    btnbatal.setVisible(row != -1);
                }
            }
        });

        // Add document listener to automatically trigger Enter key
        scanbarang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!scanbarang.getText().isEmpty()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        scanbarangActionPerformed(new java.awt.event.ActionEvent(scanbarang,
                                java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
                    });
                }
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        // Add document listener to automatically calculate kembalian
        txttunai.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian();
            }
        });
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabTransaksi(users));
        panelMain.repaint();
        panelMain.revalidate();
    }

    private void bersihkanForm() {
        scanbarang.setText("");
        txtbarang.setText("");
        txtharga.setText("");
        txtstok.setText("");
        txtqty.setText("");
        scanbarang.requestFocus();
    }

    private void bersihkanForm2() {
        txttotal.setText("");
        txttunai.setText("");
        txtkembalian.setText("");
    }

    private String potong(String teks, int panjangMaksimal) {
        if (teks.length() <= panjangMaksimal) {
            return teks;
        } else {
            return teks.substring(0, panjangMaksimal - 1) + ".";
        }
    }

    private void cetakStruk(String kodeNota, Table tabletransaksi) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public class Print {

        public void cetakStruk(String kode, List<Object[]> pesanan, int tunai) {
            try {
                String printerName = "POS-80"; // Ganti sesuai nama printer kamu
                PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                PrintService printer = null;

                for (PrintService ps : services) {
                    if (ps.getName().equalsIgnoreCase(printerName)) {
                        printer = ps;
                        break;
                    }
                }

                if (printer == null) {
                    System.out.println("Printer tidak ditemukan.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append(center("Bank Sampah Sahabat Ibu")).append("\n");
                sb.append(center("Jl. Perumahan Taman Gading, Tumpengsari,")).append("\n");
                sb.append(center("Kec. Kaliwates, Jember (68131)")).append("\n");
                sb.append(center("Telp: 082141055879")).append("\n");
                sb.append(center("Nota: " + kode)).append("\n");
                sb.append("--------------------------------\n");

                String tgl = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                sb.append("Tanggal : ").append(tgl).append("\n");
                sb.append("Kode    : ").append(kode).append("\n\n");

                total = 0;
                int qtyTotal = 0;
                int no = 1;

                for (Object[] row : pesanan) {
                    String nama = row[0].toString();
                    int jumlah = (int) row[1];
                    int Harganota = (int) row[2];
                    int subtotal = jumlah * Harganota;
                    total += subtotal;
                    qtyTotal += jumlah;

                    sb.append(no++).append(". ").append(potong(nama, 20)).append("\n");
                    sb.append("   ").append(jumlah).append(" x Rp").append(formatRupiah(Harganota));
                    sb.append(" = Rp").append(formatRupiah(subtotal)).append("\n");
                }

                int kembali = tunai - total;

                sb.append("--------------------------------\n");
                sb.append(String.format("Total QTY : %d\n", qtyTotal));
                sb.append(String.format("Sub Total : Rp%s\n", formatRupiah(total)));
                sb.append(String.format("Tunai     : Rp%s\n", formatRupiah(tunai)));
                sb.append(String.format("Kembali   : Rp%s\n", formatRupiah(kembali)));
                sb.append("--------------------------------\n");
                sb.append(center("Terima kasih!")).append("\n");
                sb.append(center("Telah Peduli Terhadap Lingkungan")).append("\n\n");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(sb.toString().getBytes("UTF-8"));

                // Barcode (opsional)
                out.write(new byte[] { 0x1D, 0x48, 0x02 }); // Tampilkan kode
                out.write(new byte[] { 0x1D, 0x77, 0x02 }); // Lebar
                out.write(new byte[] { 0x1D, 0x68, 0x40 }); // Tinggi
                out.write(new byte[] { 0x1D, 0x6B, 0x49 }); // Code 128
                out.write((byte) kode.length());
                out.write(kode.getBytes("UTF-8"));

                out.write(new byte[] { 0x0A, 0x0A });
                out.write(new byte[] { 0x1D, 0x56, 0x00 }); // Cut

                DocPrintJob job = printer.createPrintJob();
                Doc doc = new SimpleDoc(out.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
                job.print(doc, null);

                System.out.println("Struk Sahabat Ibu dicetak.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String center(String teks) {
            int width = 32; // Lebar maksimum untuk printer 58mm
            teks = teks.trim();
            int padding = (width - teks.length()) / 2;
            return " ".repeat(Math.max(0, padding)) + teks;
        }

        private String potong(String teks, int max) {
            return teks.length() > max ? teks.substring(0, max) : teks;
        }

        private String formatRupiah(int angka) {
            return String.format("%,d", angka).replace(",", ".");
        }
    }

    public void cetakStrukTarikTunai(String kode, String nama, double jumlahTarik, double saldoBaru) {
        try {
            String printerName = "POS-80"; // Ganti sesuai nama printer thermal kamu
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService printer = null;

            for (PrintService ps : services) {
                if (ps.getName().equalsIgnoreCase(printerName)) {
                    printer = ps;
                    break;
                }
            }

            if (printer == null) {
                System.out.println("Printer tidak ditemukan.");
                return;
            }

            NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            StringBuilder sb = new StringBuilder();

            sb.append(center("Bank Sampah Sahabat Ibu")).append("\n");
            sb.append(center("Jl. Perumahan Taman Gading")).append("\n");
            sb.append(center("Tumpengsari, Kaliwates, Jember")).append("\n");
            sb.append(center("Telp: 082141055879")).append("\n");
            sb.append(center("STRUK TARIK TUNAI")).append("\n");
            sb.append("--------------------------------\n");

            String tgl = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            sb.append("Tanggal     : ").append(tgl).append("\n");
            sb.append("Kode Trans  : ").append(kode).append("\n");
            sb.append("Nama        : ").append(potong(nama, 20)).append("\n");
            sb.append("Tarik Tunai : ").append(rupiah.format(jumlahTarik)).append("\n");
            sb.append("Sisa Saldo  : ").append(rupiah.format(saldoBaru)).append("\n");

            sb.append("--------------------------------\n");
            sb.append(center("Terima Kasih")).append("\n");
            sb.append(center("Telah Menabung & Peduli Lingkungan")).append("\n\n");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(sb.toString().getBytes("UTF-8"));

            // Tambahan baris kosong & potong kertas
            out.write(new byte[] { 0x0A, 0x0A }); // Baris kosong
            out.write(new byte[] { 0x1D, 0x56, 0x00 }); // Perintah cut kertas (ESC/POS)

            DocPrintJob job = printer.createPrintJob();
            Doc doc = new SimpleDoc(out.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, null);

            System.out.println("Struk tarik tunai dicetak.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String center(String teks) {
        int width = 32; // Untuk printer 58mm (ganti 48 untuk printer 80mm)
        teks = teks.trim();
        int padding = (width - teks.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + teks;
    }

    private void prosesPembayaranNasabah(String id_nasabah) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");

            String sql = "SELECT * FROM manajemen_nasabah WHERE id_nasabah = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, id_nasabah);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_nasabah");
                double saldo = rs.getDouble("saldo_total");

                hitungTotal(); // asumsi ini set nilai ke 'total'
                double jumlahBayar = total;

                NumberFormat Rp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                String totalFormatted = Rp.format(jumlahBayar);

                String[] pilihan = { "Pembayaran", "Tarik Tunai", "Batal" };
                int pilihanUser = JOptionPane.showOptionDialog(this,
                        "Nama: " + nama
                                + "\nSaldo saat ini: " + Rp.format(saldo)
                                + "\n\nPilih jenis transaksi:",
                        "Pilih Transaksi",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        pilihan,
                        pilihan[0]);

                if (pilihanUser == 0) { // PEMBAYARAN
                    int konfirmasi = JOptionPane.showConfirmDialog(this,
                            "Nama: " + nama
                                    + "\nSaldo saat ini: " + Rp.format(saldo)
                                    + "\nTotal transaksi: " + totalFormatted
                                    + "\n\nLanjutkan pembayaran?",
                            "Konfirmasi Pembayaran",
                            JOptionPane.YES_NO_OPTION);

                    if (konfirmasi == JOptionPane.YES_OPTION) {
                        if (jumlahBayar > saldo) {
                            JOptionPane.showMessageDialog(this, "Saldo tidak mencukupi!", "Gagal",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            double saldoBaru = saldo - jumlahBayar;

                            String updateSql = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                            PreparedStatement updatePst = conn.prepareStatement(updateSql);
                            updatePst.setDouble(1, saldoBaru);
                            updatePst.setString(2, id_nasabah);
                            updatePst.executeUpdate();
                            updatePst.close();
                            txttunai.setText(String.valueOf((int) jumlahBayar));

                            // Log transaction
                            LoggerUtil.insert(users.getId(), "Melakukan pembayaran dengan saldo nasabah: " + nama
                                    + " (ID: " + id_nasabah + ") sejumlah " + totalFormatted);

                            JOptionPane.showMessageDialog(this,
                                    "Pembayaran berhasil!\nSisa Saldo: " + Rp.format(saldoBaru));
                            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                                    "Pembayaran dengan saldo berhasil");
                        }
                    }

                } else if (pilihanUser == 1) { // TARIK TUNAI
                    String input = JOptionPane.showInputDialog(this,
                            "Saldo saat ini: " + Rp.format(saldo)
                                    + "\nMasukkan jumlah tarik tunai (Rp):",
                            "Input Jumlah",
                            JOptionPane.PLAIN_MESSAGE);

                    if (input != null && !input.trim().isEmpty()) {
                        try {
                            double jumlahTarik = Double.parseDouble(input);

                            if (jumlahTarik <= 0) {
                                JOptionPane.showMessageDialog(this, "Jumlah tidak valid.");
                                return;
                            }

                            if (jumlahTarik > saldo) {
                                JOptionPane.showMessageDialog(this, "Saldo tidak mencukupi!", "Gagal",
                                        JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            int konfirmasi = JOptionPane.showConfirmDialog(this,
                                    "Nama: " + nama
                                            + "\nSaldo saat ini: " + Rp.format(saldo)
                                            + "\nJumlah yang akan ditarik: " + Rp.format(jumlahTarik)
                                            + "\n\nLanjutkan tarik tunai?",
                                    "Konfirmasi Tarik Tunai",
                                    JOptionPane.YES_NO_OPTION);
                            if (konfirmasi == JOptionPane.YES_OPTION) {
                                double saldoBaru = saldo - jumlahTarik; // Update the saldo in manajemen_nasabah
                                String updateSql = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                                PreparedStatement updatePst = conn.prepareStatement(updateSql);
                                updatePst.setDouble(1, saldoBaru);
                                updatePst.setString(2, id_nasabah);
                                updatePst.executeUpdate();
                                updatePst.close(); // Insert record into penarikan_saldo table
                                String insertSql = "INSERT INTO penarikan_saldo (id_nasabah, id_user, jumlah_penarikan, tanggal_penarikan) VALUES (?, ?, ?, NOW())";
                                PreparedStatement insertPst = conn.prepareStatement(insertSql);
                                insertPst.setString(1, id_nasabah);
                                insertPst.setInt(2, users.getId());
                                insertPst.setDouble(3, jumlahTarik);
                                insertPst.executeUpdate();
                                insertPst.close();

                                String kodeTransaksi = "TRX" + System.currentTimeMillis(); // contoh kode unik
                                cetakStrukTarikTunai(kodeTransaksi, nama, jumlahTarik, saldoBaru); // Log tarik tunai
                                LoggerUtil.insert(users.getId(), "Melakukan tarik tunai nasabah: " + nama + " (ID: "
                                        + id_nasabah + ") sejumlah " + Rp.format(jumlahTarik));

                                JOptionPane.showMessageDialog(this,
                                        "Tarik tunai berhasil!\nSisa Saldo: " + Rp.format(saldoBaru));
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, "Tarik tunai berhasil");
                            }

                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Input tidak valid. Harap masukkan angka.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Transaksi dibatalkan.");
                }

            } else {
                JOptionPane.showMessageDialog(this, "Nasabah tidak ditemukan.");
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void tambahKeTabel(String kode, String nama, int hargaSatuan, int qty) {
        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
        boolean ditemukan = false;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(kode)) {
                int qtyLama = (int) model.getValueAt(i, 2);
                int qtyBaru = qtyLama + qty;
                model.setValueAt(qtyBaru, i, 2); // kolom qty
                model.setValueAt(hargaSatuan, i, 3); // harga satuan
                model.setValueAt(qtyBaru * hargaSatuan, i, 4); // total harga
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            int totalHarga = qty * hargaSatuan;
            model.addRow(new Object[] { kode, nama, qty, hargaSatuan, totalHarga });
        }

        hitungTotal();
    }

    private void hitungTotal() {
        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
        total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (int) model.getValueAt(i, 4); // Kolom subtotal
        }

        txttotal.setText(Rp.format(total));

        // Also update kembalian when total changes
        if (!txttunai.getText().isEmpty()) {
            hitungKembalian();
        }
    }

    private void hitungKembalian() {
        try {
            // Clean the input text (remove non-digits)
            String cleanInput = txttunai.getText().replaceAll("[^\\d]", "");
            int tunai = cleanInput.isEmpty() ? 0 : Integer.parseInt(cleanInput);
            int kembali = tunai - total;

            // Format the display value
            txtkembalian.setText(kembali >= 0 ? Rp.format(kembali) : Rp.format(0));
        } catch (NumberFormatException e) {
            txtkembalian.setText(Rp.format(0));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelView = new javax.swing.JPanel();
        shadowDataBarang = new component.ShadowPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnbatal = new component.Jbutton();
        btntambah = new component.Jbutton();
        jLabel10 = new javax.swing.JLabel();
        scanbarang = new component.PlaceholderTextField();
        txtbarang = new component.PlaceholderTextField();
        txtharga = new component.PlaceholderTextField();
        txtstok = new component.PlaceholderTextField();
        txtqty = new component.PlaceholderTextField();
        txtnasabah = new component.PlaceholderTextField();
        shadowTabel = new component.ShadowPanel();
        btnbayar = new component.Jbutton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabletransaksi = new component.Table();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txttotal = new component.PlaceholderTextField();
        txttunai = new component.PlaceholderTextField();
        txtkembalian = new component.PlaceholderTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(253, 253, 253));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()+6));
        jLabel2.setText("Scan Barang Aktif . . .");

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getStyle() | java.awt.Font.BOLD, jLabel7.getFont().getSize()+6));
        jLabel7.setText("Nama Barang");

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getStyle() | java.awt.Font.BOLD, jLabel6.getFont().getSize()+6));
        jLabel6.setText("Harga");

        jLabel9.setFont(jLabel9.getFont().deriveFont(jLabel9.getFont().getStyle() | java.awt.Font.BOLD, jLabel9.getFont().getSize()+6));
        jLabel9.setText("Stok");

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD, jLabel8.getFont().getSize()+6));
        jLabel8.setText("Jumlah");

        btnbatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnbatal.setText("Batal");
        btnbatal.setFillClick(new java.awt.Color(200, 125, 0));
        btnbatal.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnbatal.setFillOver(new java.awt.Color(230, 145, 10));
        btnbatal.setFont(btnbatal.getFont().deriveFont(btnbatal.getFont().getStyle() | java.awt.Font.BOLD, btnbatal.getFont().getSize()-1));
        btnbatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbatalActionPerformed(evt);
            }
        });

        btntambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_tambah.png"))); // NOI18N
        btntambah.setText("Tambah");
        btntambah.setFillClick(new java.awt.Color(55, 130, 60));
        btntambah.setFillOriginal(new java.awt.Color(76, 175, 80));
        btntambah.setFillOver(new java.awt.Color(69, 160, 75));
        btntambah.setFont(btntambah.getFont().deriveFont(btntambah.getFont().getStyle() | java.awt.Font.BOLD, btntambah.getFont().getSize()-1));
        btntambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahActionPerformed(evt);
            }
        });

        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getStyle() | java.awt.Font.BOLD, jLabel10.getFont().getSize()+6));
        jLabel10.setText("Tarik Tunai Nasabah");

        scanbarang.setPlaceholder("Scan code barang");
        scanbarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanbarangActionPerformed(evt);
            }
        });

        txtbarang.setEditable(false);
        txtbarang.setPlaceholder("Nama barang");

        txtharga.setEditable(false);
        txtharga.setPlaceholder("Harga");

        txtstok.setEditable(false);
        txtstok.setPlaceholder("Stok");

        txtqty.setPlaceholder("Jumlah");

        txtnasabah.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtnasabah.setFont(new java.awt.Font("Poppins", 0, 24)); // NOI18N
        txtnasabah.setPlaceholder("ID Member");
        txtnasabah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnasabahActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowDataBarangLayout = new javax.swing.GroupLayout(shadowDataBarang);
        shadowDataBarang.setLayout(shadowDataBarangLayout);
        shadowDataBarangLayout.setHorizontalGroup(
            shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addGroup(shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 166, Short.MAX_VALUE))
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addGroup(shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shadowDataBarangLayout.createSequentialGroup()
                        .addComponent(btnbatal, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btntambah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(txtstok, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtqty, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtnasabah, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scanbarang, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtbarang, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtharga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        shadowDataBarangLayout.setVerticalGroup(
            shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowDataBarangLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel2)
                .addGap(13, 13, 13)
                .addComponent(scanbarang, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtbarang, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addGap(13, 13, 13)
                .addComponent(txtharga, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtstok, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jLabel8)
                .addGap(7, 7, 7)
                .addComponent(txtqty, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btntambah, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnbatal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtnasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnbayar.setForeground(new java.awt.Color(0, 0, 0));
        btnbayar.setText("Bayar");
        btnbayar.setFillClick(new java.awt.Color(194, 65, 12));
        btnbayar.setFillOriginal(new java.awt.Color(234, 88, 12));
        btnbayar.setFillOver(new java.awt.Color(251, 146, 60));
        btnbayar.setFont(btnbayar.getFont().deriveFont(btnbayar.getFont().getStyle() | java.awt.Font.BOLD, btnbayar.getFont().getSize()+11));
        btnbayar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnbayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbayarActionPerformed(evt);
            }
        });

        tabletransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kode Barang", "Nama Barang", "Qty", "Harga", "Total Harga"
            }
        ));
        jScrollPane2.setViewportView(tabletransaksi);

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getStyle() | java.awt.Font.BOLD, jLabel5.getFont().getSize()+6));
        jLabel5.setText("Total");

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() | java.awt.Font.BOLD, jLabel3.getFont().getSize()+6));
        jLabel3.setText("Tunai");

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD, jLabel4.getFont().getSize()+6));
        jLabel4.setText("Kembalian");

        txttotal.setEditable(false);
        txttotal.setPlaceholder("Total harga");

        txttunai.setPlaceholder("Tunai");

        txtkembalian.setEditable(false);
        txtkembalian.setPlaceholder("Kembalian");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText(":");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText(":");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText(":");

        javax.swing.GroupLayout shadowTabelLayout = new javax.swing.GroupLayout(shadowTabel);
        shadowTabel.setLayout(shadowTabelLayout);
        shadowTabelLayout.setHorizontalGroup(
            shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowTabelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                .addGap(6, 6, 6))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowTabelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txttunai, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txttotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtkembalian, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnbayar, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowTabelLayout.setVerticalGroup(
            shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowTabelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(shadowTabelLayout.createSequentialGroup()
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txttunai, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtkembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)))
                    .addComponent(btnbayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shadowDataBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelViewLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shadowTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowDataBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void btntambahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btntambahActionPerformed
        // TODO add your handling code here:
        String kode = scanbarang.getText().trim();
        String nama = txtbarang.getText().trim();
        String qtyStr = txtqty.getText().trim();
        String hargaStr = txtharga.getText().trim();
        String idNasabah = txtnasabah.getText().trim();

        if (kode.isEmpty() || nama.isEmpty() || qtyStr.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            int stok = Integer.parseInt(txtstok.getText().trim());
            int hargaParsed = Integer.parseInt(hargaStr);

            if (qty > stok) {
                JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                return;
            }

            DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
            boolean found = false;

            for (int i = 0; i < model.getRowCount(); i++) {
                String kodeTabel = model.getValueAt(i, 0).toString();
                if (kode.equals(kodeTabel)) {
                    // Tambah qty dan total harga
                    int qtyLama = Integer.parseInt(model.getValueAt(i, 2).toString());
                    int qtyBaru = qtyLama + qty;

                    if (qtyBaru > stok) {
                        JOptionPane.showMessageDialog(this, "Total qty melebihi stok yang tersedia!");
                        return;
                    }

                    model.setValueAt(qtyBaru, i, 2); // Update qty
                    model.setValueAt(qtyBaru * hargaParsed, i, 4); // Update total harga
                    found = true;
                    break;
                }
            }
            if (!found) {
                int totalHarga = qty * hargaParsed;
                model.addRow(new Object[] { kode, nama, qty, hargaParsed, totalHarga, idNasabah });

                // Log adding new item to cart
                LoggerUtil.insert(users.getId(),
                        "Menambahkan barang ke keranjang: " + nama + " (Kode: " + kode + "), Qty: " + qty);
            } else {
                // Log updating item in cart
                LoggerUtil.insert(users.getId(),
                        "Memperbarui qty barang di keranjang: " + nama + " (Kode: " + kode + ")");
            }

            hitungTotal();
            bersihkanForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Input harus berupa angka valid!");
        }
    }// GEN-LAST:event_btntambahActionPerformed

    private void btnbatalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnbatalActionPerformed
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Yakin membatalkan transaksi barang ini?",
                "Konfirmasi Batal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int selectedRow = tabletransaksi.getSelectedRow();
            if (selectedRow >= 0) {
                DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
                // Get info about the item before removing
                String kode = model.getValueAt(selectedRow, 0).toString();
                String nama = model.getValueAt(selectedRow, 1).toString();
                int qty = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());

                model.removeRow(selectedRow);
                hitungTotal();
                btnbatal.setVisible(false); // Hide button after deletion

                // Log removing item from cart
                LoggerUtil.insert(users.getId(),
                        "Menghapus barang dari keranjang: " + nama + " (Kode: " + kode + "), Qty: " + qty);
                Notifications.getInstance().show(Notifications.Type.INFO, "Barang dihapus dari keranjang");
            } else {
                JOptionPane.showMessageDialog(this, "Pilih baris yang akan dibatalkan!");
            }
            bersihkanForm();
        }
    }// GEN-LAST:event_btnbatalActionPerformed

    private void btnbayarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnbayarActionPerformed
        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada barang di keranjang.");
            return;
        }

        // Get the total amount to be paid
        hitungTotal();

        // Always show all three payment options
        String[] options = { "Bayar dengan Tunai", "Bayar dengan Saldo", "Batal" };

        int choice = JOptionPane.showOptionDialog(this,
                "Pilih metode pembayaran:",
                "Metode Pembayaran",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) { // Tunai payment
            processCashPayment();
        } else if (choice == 1) { // Saldo payment - always prompt for ID
            // Prompt for customer ID
            String idNasabah = JOptionPane.showInputDialog(this,
                    "Masukkan ID Nasabah:",
                    "ID Nasabah",
                    JOptionPane.QUESTION_MESSAGE);

            if (idNasabah != null && !idNasabah.trim().isEmpty()) {
                processBalancePayment(idNasabah.trim());
            } else {
                JOptionPane.showMessageDialog(this,
                        "ID Nasabah diperlukan untuk pembayaran dengan saldo.",
                        "Pembayaran Dibatalkan",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        // Choice 2 or any other value is Cancel - do nothing
    }// GEN-LAST:event_btnbayarActionPerformed

    // Process cash payment
    private void processCashPayment() {
        String input = txttunai.getText().trim().replaceAll("[^\\d]", "");
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah tunai yang valid.");
            return;
        }

        int tunai = Integer.parseInt(input);
        if (tunai < total) {
            JOptionPane.showMessageDialog(this, "Uang tunai tidak mencukupi.");
            return;
        }

        int kembali = tunai - total;
        txtkembalian.setText(String.valueOf(kembali));

        saveTransaction(null, tunai, kembali);
    }

    // Process balance payment
    private void processBalancePayment(String idNasabah) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");

            String sql = "SELECT * FROM manajemen_nasabah WHERE id_nasabah = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idNasabah);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_nasabah");
                double saldo = rs.getDouble("saldo_total");
                double jumlahBayar = total;

                // Format numbers for display
                NumberFormat Rp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
                Rp.setMaximumFractionDigits(0);
                String totalFormatted = Rp.format(jumlahBayar);

                int konfirmasi = JOptionPane.showConfirmDialog(this,
                        "Nama: " + nama
                                + "\nSaldo saat ini: " + Rp.format(saldo)
                                + "\nTotal transaksi: " + totalFormatted
                                + "\n\nLanjutkan pembayaran dengan saldo?",
                        "Konfirmasi Pembayaran",
                        JOptionPane.YES_NO_OPTION);

                if (konfirmasi == JOptionPane.YES_OPTION) {
                    if (jumlahBayar > saldo) {
                        JOptionPane.showMessageDialog(this, "Saldo tidak mencukupi!", "Gagal",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    double saldoBaru = saldo - jumlahBayar;

                    // Update saldo nasabah
                    String updateSql = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                    PreparedStatement updatePst = conn.prepareStatement(updateSql);
                    updatePst.setDouble(1, saldoBaru);
                    updatePst.setString(2, idNasabah);
                    updatePst.executeUpdate();
                    updatePst.close();

                    // Set tunai field to show payment amount
                    txttunai.setText(String.valueOf((int) jumlahBayar));
                    txtkembalian.setText("Rp0");

                    // Save the transaction
                    saveTransaction(idNasabah, (int) jumlahBayar, 0);

                    // Log transaction
                    LoggerUtil.insert(users.getId(), "Melakukan pembayaran dengan saldo nasabah: " + nama
                            + " (ID: " + idNasabah + ") sejumlah " + totalFormatted);

                    JOptionPane.showMessageDialog(this,
                            "Pembayaran berhasil!\nSisa Saldo: " + Rp.format(saldoBaru));
                    Notifications.getInstance().show(Notifications.Type.SUCCESS,
                            "Pembayaran dengan saldo berhasil");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nasabah tidak ditemukan.");
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save transaction to database
    private void saveTransaction(String idNasabah, int tunai, int kembali) {
        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
        String kodeTransaksi = "TRX" + System.currentTimeMillis();
        List<Object[]> pesanan = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transaksi (id_user, id_nasabah, kode_transaksi, kode_barang, nama_barang, qty, harga, total_harga, bayar, kembalian, tanggal) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
                PreparedStatement pstUpdate = conn.prepareStatement(
                        "UPDATE data_barang SET stok = stok - ? WHERE kode_barang = ? AND stok >=?")) {

            for (int i = 0; i < model.getRowCount(); i++) {
                String kodeBarang = model.getValueAt(i, 0).toString();
                String namaBarang = model.getValueAt(i, 1).toString();
                int qty = (int) model.getValueAt(i, 2);
                int harga = (int) model.getValueAt(i, 3);
                int totalHarga = (int) model.getValueAt(i, 4);

                // Simpan transaksi
                ps.setInt(1, users.getId()); // id_user

                // Set id_nasabah parameter, use "non-nasabah" instead of NULL for cash payments
                if (idNasabah == null || idNasabah.isEmpty()) {
                    ps.setString(2, "non-nasabah"); // Use "non-nasabah" for cash transactions
                } else {
                    ps.setString(2, idNasabah);
                }

                ps.setString(3, kodeTransaksi); // kode_transaksi
                ps.setString(4, kodeBarang); // kode_barang
                ps.setString(5, namaBarang); // nama_barang
                ps.setInt(6, qty); // qty
                ps.setInt(7, harga); // harga
                ps.setInt(8, totalHarga); // total_harga
                ps.setInt(9, tunai); // bayar
                ps.setInt(10, kembali); // kembalian
                ps.addBatch();

                // Simpan data untuk cetak
                pesanan.add(new Object[] { namaBarang, qty, harga });

                // Update stok
                pstUpdate.setInt(1, qty);
                pstUpdate.setString(2, kodeBarang);
                pstUpdate.setInt(3, qty);

                int affected = pstUpdate.executeUpdate();
                if (affected == 0) {
                    JOptionPane.showMessageDialog(this, "Stok tidak cukup untuk " + namaBarang);
                    return;
                }
            }
            ps.executeBatch();

            // Log the transaction with appropriate message
            String logMessage = "Melakukan transaksi penjualan dengan kode: " + kodeTransaksi;
            if (idNasabah == null || idNasabah.isEmpty()) {
                logMessage += " untuk non-member dengan tunai";
            } else {
                logMessage += " untuk nasabah: " + idNasabah + " dengan " +
                        (kembali > 0 ? "tunai" : "saldo");
            }
            LoggerUtil.insert(users.getId(), logMessage);

            // Cetak struk
            Print printer = new Print();
            printer.cetakStruk(kodeTransaksi, pesanan, tunai);

            // Reset UI
            model.setRowCount(0);
            bersihkanForm();
            bersihkanForm2();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil dan struk dicetak.");
            Notifications.getInstance().show(Notifications.Type.SUCCESS, "Transaksi berhasil disimpan");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchProductInfo(String barcode) {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:MySQL://localhost:3306/bank_sampah_sahabat_ibu",
                    "root",
                    "");

            String sql = "SELECT * FROM data_barang WHERE kode_barang = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, barcode);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtbarang.setText(rs.getString("nama_barang"));
                txtharga.setText(String.valueOf(rs.getInt("harga")));
                txtstok.setText(String.valueOf(rs.getInt("stok")));
                txtqty.setText("1"); // Default to 1 for quantity

                // Log scan success
                LoggerUtil.insert(users.getId(),
                        "Memindai barcode barang: " + rs.getString("nama_barang") + " (Kode: " + barcode + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Barang tidak ditemukan.");
                bersihkanForm();
            }

            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saat memproses barcode: " + e.getMessage());
        }
    }

    private void scanbarangActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_scanbarangActionPerformed
        // This is now handled by the KeyListener above
        // But keep the method since it's referenced in the form designer
    }// GEN-LAST:event_scanbarangActionPerformed

    private void txtnasabahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtnasabahActionPerformed
        // Update to only handle withdrawals
        handleTarikTunai(txtnasabah.getText());
    }// GEN-LAST:event_txtnasabahActionPerformed

    // New method for handling withdrawals only
    private void handleTarikTunai(String id_nasabah) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");

            String sql = "SELECT * FROM manajemen_nasabah WHERE id_nasabah = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, id_nasabah);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_nasabah");
                double saldo = rs.getDouble("saldo_total");

                String input = JOptionPane.showInputDialog(this,
                        "Saldo saat ini: " + Rp.format(saldo)
                                + "\nMasukkan jumlah tarik tunai (Rp):",
                        "Input Jumlah",
                        JOptionPane.PLAIN_MESSAGE);

                if (input != null && !input.trim().isEmpty()) {
                    try {
                        double jumlahTarik = Double.parseDouble(input);

                        if (jumlahTarik <= 0) {
                            JOptionPane.showMessageDialog(this, "Jumlah tidak valid.");
                            return;
                        }

                        if (jumlahTarik > saldo) {
                            JOptionPane.showMessageDialog(this, "Saldo tidak mencukupi!", "Gagal",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        int konfirmasi = JOptionPane.showConfirmDialog(this,
                                "Nama: " + nama
                                        + "\nSaldo saat ini: " + Rp.format(saldo)
                                        + "\nJumlah yang akan ditarik: " + Rp.format(jumlahTarik)
                                        + "\n\nLanjutkan tarik tunai?",
                                "Konfirmasi Tarik Tunai",
                                JOptionPane.YES_NO_OPTION);
                        if (konfirmasi == JOptionPane.YES_OPTION) {
                            double saldoBaru = saldo - jumlahTarik;

                            // Update the saldo in manajemen_nasabah
                            String updateSql = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                            PreparedStatement updatePst = conn.prepareStatement(updateSql);
                            updatePst.setDouble(1, saldoBaru);
                            updatePst.setString(2, id_nasabah);
                            updatePst.executeUpdate();
                            updatePst.close();

                            // Insert record into penarikan_saldo table
                            String insertSql = "INSERT INTO penarikan_saldo (id_nasabah, id_user, jumlah_penarikan, tanggal_penarikan) VALUES (?, ?, ?, NOW())";
                            PreparedStatement insertPst = conn.prepareStatement(insertSql);
                            insertPst.setString(1, id_nasabah);
                            insertPst.setInt(2, users.getId());
                            insertPst.setDouble(3, jumlahTarik);
                            insertPst.executeUpdate();
                            insertPst.close();

                            String kodeTransaksi = "TRX" + System.currentTimeMillis();
                            cetakStrukTarikTunai(kodeTransaksi, nama, jumlahTarik, saldoBaru);

                            // Log tarik tunai
                            LoggerUtil.insert(users.getId(), "Melakukan tarik tunai nasabah: " + nama + " (ID: "
                                    + id_nasabah + ") sejumlah " + Rp.format(jumlahTarik));

                            JOptionPane.showMessageDialog(this,
                                    "Tarik tunai berhasil!\nSisa Saldo: " + Rp.format(saldoBaru));
                            Notifications.getInstance().show(Notifications.Type.SUCCESS, "Tarik tunai berhasil");
                        }

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Input tidak valid. Harap masukkan angka.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nasabah tidak ditemukan.");
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    public component.PlaceholderTextField getScanbarang() {
        return scanbarang;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Jbutton btnbatal;
    private component.Jbutton btnbayar;
    private component.Jbutton btntambah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private component.PlaceholderTextField scanbarang;
    private component.ShadowPanel shadowDataBarang;
    private component.ShadowPanel shadowTabel;
    private component.Table tabletransaksi;
    private component.PlaceholderTextField txtbarang;
    private component.PlaceholderTextField txtharga;
    private component.PlaceholderTextField txtkembalian;
    private component.PlaceholderTextField txtnasabah;
    private component.PlaceholderTextField txtqty;
    private component.PlaceholderTextField txtstok;
    private component.PlaceholderTextField txttotal;
    private component.PlaceholderTextField txttunai;
    // End of variables declaration//GEN-END:variables
}
