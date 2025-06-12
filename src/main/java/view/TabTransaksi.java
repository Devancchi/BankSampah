/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import component.Jbutton;
import component.Table;
import component.UserSession;
import java.awt.Color;
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

/**
 *
 * @author zal
 */
public class TabTransaksi extends javax.swing.JPanel {

    private static final NumberFormat Rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private int harga, total;
    private final UserSession users;

    /**
     * Creates new form ManajemenNasabah
     */
    public TabTransaksi(UserSession user) {
        this.users = user;
        initComponents();

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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public class Print {

        public void cetakStruk(String kode, List<Object[]> pesanan, int tunai) {
            try {
                String printerName = "OK-58D"; // Ganti sesuai nama printer kamu
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
                out.write(new byte[]{0x1D, 0x48, 0x02}); // Tampilkan kode
                out.write(new byte[]{0x1D, 0x77, 0x02}); // Lebar
                out.write(new byte[]{0x1D, 0x68, 0x40}); // Tinggi
                out.write(new byte[]{0x1D, 0x6B, 0x49}); // Code 128
                out.write((byte) kode.length());
                out.write(kode.getBytes("UTF-8"));

                out.write(new byte[]{0x0A, 0x0A});
                out.write(new byte[]{0x1D, 0x56, 0x00}); // Cut

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

                // Hitung total transaksi
                hitungTotal(); // pastikan method ini menghitung dan menyet nilai ke variabel `total`
                double jumlahBayar = total;

                // Format total dan saldo dalam Rupiah
                NumberFormat Rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                String totalFormatted = Rp.format(jumlahBayar);

                // Tampilkan dialog konfirmasi
                int konfirmasi = JOptionPane.showConfirmDialog(this,
                        "Nama: " + nama
                        + "\nSaldo saat ini: " + Rp.format(saldo)
                        + "\nTotal transaksi: " + totalFormatted
                        + "\n\nLanjutkan pembayaran?",
                        "Konfirmasi Pembayaran",
                        JOptionPane.YES_NO_OPTION);

                if (konfirmasi == JOptionPane.YES_OPTION) {
                    if (jumlahBayar > saldo) {
                        JOptionPane.showMessageDialog(this,
                                "Saldo tidak mencukupi!", "Gagal", JOptionPane.WARNING_MESSAGE);
                    } else {
                        double saldoBaru = saldo - jumlahBayar;

                        // Update saldo di database
                        String updateSql = "UPDATE manajemen_nasabah SET saldo_total = ? WHERE id_nasabah = ?";
                        PreparedStatement updatePst = conn.prepareStatement(updateSql);
                        updatePst.setDouble(1, saldoBaru);
                        updatePst.setString(2, id_nasabah);
                        updatePst.executeUpdate();
                        updatePst.close();

                        // Set jumlah bayar ke txttunai
                        txttunai.setText(String.valueOf((int) jumlahBayar));

                        JOptionPane.showMessageDialog(this,
                                "Pembayaran berhasil!\nSisa Saldo: " + Rp.format(saldoBaru));
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
            model.addRow(new Object[]{kode, nama, qty, hargaSatuan, totalHarga});
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
    }

    private void hitungKembalian() {
        try {
            int tunai = txttunai.getText().isEmpty() ? 0 : Integer.parseInt(txttunai.getText());
            int kembali = tunai - total;
            txtkembalian.setText(kembali >= 0 ? String.valueOf(kembali) : "0");
        } catch (NumberFormatException e) {
            txtkembalian.setText("0");
        }
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

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setBackground(new java.awt.Color(250, 250, 250));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Scan Barang Aktif . . .");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Nama Barang");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Harga");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setText("Stok");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Jumlah");

        btnbatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_batal.png"))); // NOI18N
        btnbatal.setText("Batal");
        btnbatal.setFillClick(new java.awt.Color(200, 125, 0));
        btnbatal.setFillOriginal(new java.awt.Color(243, 156, 18));
        btnbatal.setFillOver(new java.awt.Color(230, 145, 10));
        btnbatal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
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
        btntambah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btntambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Tap Member Nasabah . . .");

        scanbarang.setPlaceholder("Scan code barang");
        scanbarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanbarangActionPerformed(evt);
            }
        });

        txtbarang.setPlaceholder("Nama barang");

        txtharga.setPlaceholder("Harga");

        txtstok.setPlaceholder("Stok");

        txtqty.setPlaceholder("Jumlah");

        txtnasabah.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtnasabah.setText("NSB000");
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
                .addGroup(shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtbarang, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scanbarang, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shadowDataBarangLayout.createSequentialGroup()
                        .addComponent(btnbatal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(28, 28, 28)
                        .addComponent(btntambah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shadowDataBarangLayout.createSequentialGroup()
                        .addGroup(shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 95, Short.MAX_VALUE)))
                .addGap(13, 13, 13))
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addComponent(txtharga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addComponent(txtstok, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addComponent(txtqty, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addComponent(txtnasabah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtnasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnbayar.setForeground(new java.awt.Color(0, 0, 0));
        btnbayar.setText("Bayar");
        btnbayar.setFillClick(new java.awt.Color(194, 65, 12));
        btnbayar.setFillOriginal(new java.awt.Color(234, 88, 12));
        btnbayar.setFillOver(new java.awt.Color(251, 146, 60));
        btnbayar.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
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
                "Kode Barang", "Nama Barang", "Qty", "Harga", "Total Harga", "Nasabah"
            }
        ));
        jScrollPane2.setViewportView(tabletransaksi);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Total            :");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Tunai           :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Kembalian   :");

        txttotal.setPlaceholder("Total harga");

        txttunai.setPlaceholder("Tunai");

        txtkembalian.setPlaceholder("Kembalian");

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
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txttunai, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtkembalian, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txttotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnbayar, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        shadowTabelLayout.setVerticalGroup(
            shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowTabelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(shadowTabelLayout.createSequentialGroup()
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txttunai, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(shadowTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtkembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

    private void btntambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahActionPerformed
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
                model.addRow(new Object[]{kode, nama, qty, hargaParsed, totalHarga, idNasabah});
            }

            hitungTotal();
            bersihkanForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Input harus berupa angka valid!");
        }
    }//GEN-LAST:event_btntambahActionPerformed

    private void btnbatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatalActionPerformed
        btnbatal.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Yakin membatalkan transaksi barang ini?",
                    "Konfirmasi Batal",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                bersihkanForm();
                DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
                int a = tabletransaksi.getSelectedRow();
                model.removeRow(a);
                hitungTotal();
            }
        });
    }//GEN-LAST:event_btnbatalActionPerformed

    private void btnbayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbayarActionPerformed

        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada barang di keranjang.");
            return;
        }

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

        String kodeTransaksi = "TRX" + System.currentTimeMillis();
        String idNasabah = txtnasabah.getText().trim(); // Ambil id_nasabah
        List<Object[]> pesanan = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", ""); PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transaksi (id_user, id_nasabah, kode_transaksi, kode_barang, nama_barang, qty, harga, total_harga, bayar, kembalian, tanggal) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())"); PreparedStatement pstUpdate = conn.prepareStatement(
                        "UPDATE data_barang SET stok = stok - ? WHERE kode_barang = ? AND stok >= ?")) {

            for (int i = 0; i < model.getRowCount(); i++) {
                String kodeBarang = model.getValueAt(i, 0).toString();
                String namaBarang = model.getValueAt(i, 1).toString();
                int qty = (int) model.getValueAt(i, 2);
                int harga = (int) model.getValueAt(i, 3);
                int totalHarga = (int) model.getValueAt(i, 4);

                // Simpan transaksi
                ps.setInt(1, users.getId());           // id_user
                ps.setString(2, idNasabah);            // id_nasabah
                ps.setString(3, kodeTransaksi);        // kode_transaksi
                ps.setString(4, kodeBarang);           // kode_barang
                ps.setString(5, namaBarang);           // nama_barang
                ps.setInt(6, qty);                     // qty
                ps.setInt(7, harga);                   // harga
                ps.setInt(8, totalHarga);              // total_harga
                ps.setInt(9, tunai);                   // bayar
                ps.setInt(10, kembali);                // kembalian
                ps.addBatch();

                // Simpan data untuk cetak
                pesanan.add(new Object[]{namaBarang, qty, harga});

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

            // Cetak struk
            Print printer = new Print();
            printer.cetakStruk(kodeTransaksi, pesanan, tunai);

            // Reset UI
            model.setRowCount(0);
            bersihkanForm();
            bersihkanForm2();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil dan struk dicetak.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnbayarActionPerformed

    private void scanbarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanbarangActionPerformed
        String SUrl, SUser, SPass;
        String kode = scanbarang.getText();
        SUrl = "jdbc:MySQL://localhost:3306/bank_sampah_sahabat_ibu";
        SUser = "root";
        SPass = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(SUrl, SUser, SPass);

            String sql = "SELECT * FROM data_barang WHERE kode_barang = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, kode); // gunakan kode sebagai string

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtbarang.setText(rs.getString("nama_barang"));
                txtharga.setText(String.valueOf(rs.getInt("harga")));
                txtstok.setText(String.valueOf(rs.getInt("stok")));
                txtqty.setText("1");
            } else {
                JOptionPane.showMessageDialog(this, "Barang tidak ditemukan.");
            }

            rs.close();
            pst.close();
            con.close();

        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
    }//GEN-LAST:event_scanbarangActionPerformed

    private void txtnasabahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnasabahActionPerformed
        prosesPembayaranNasabah(txtnasabah.getText());
    }//GEN-LAST:event_txtnasabahActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Jbutton btnbatal;
    private component.Jbutton btnbayar;
    private component.Jbutton btntambah;
    private javax.swing.JLabel jLabel10;
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
