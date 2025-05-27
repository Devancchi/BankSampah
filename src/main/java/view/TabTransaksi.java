/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import component.Jbutton;
import component.Table;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * Creates new form ManajemenNasabah
     */
    public TabTransaksi() {
        initComponents();
    }

    private void showPanel() {
        panelMain.removeAll();
        panelMain.add(new TabTransaksi());
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
            String printerName = "POS-58"; // Ganti sesuai nama printer kamu
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
            sb.append(center("Jl. Perumahan Taman Gading, Tumpengsari, Tegal Besar,")).append("\n");
            sb.append(center("Kec. Kaliwates, Jember (68131)")).append("\n");
            sb.append(center("Telp: 082141055879")).append("\n");
            sb.append(center("Nota: " + kode)).append("\n");
            sb.append("--------------------------------\n");

            String tgl = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            sb.append("Tanggal : ").append(tgl).append("\n");
            sb.append("Kode    : ").append(kode).append("\n\n");

            int total = 0;
            int qtyTotal = 0;
            int no = 1;

            for (Object[] row : pesanan) {
                String nama = row[0].toString();
                int jumlah = (int) row[1];
                int harga = (int) row[2];
                int subtotal = jumlah * harga;
                total += subtotal;
                qtyTotal += jumlah;

                sb.append(no++).append(". ").append(potong(nama, 20)).append("\n");
                sb.append("   ").append(jumlah).append(" x Rp").append(formatRupiah(harga));
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
            out.write(new byte[]{ 0x1D, 0x48, 0x02 }); // Tampilkan kode
            out.write(new byte[]{ 0x1D, 0x77, 0x02 }); // Lebar
            out.write(new byte[]{ 0x1D, 0x68, 0x40 }); // Tinggi
            out.write(new byte[]{ 0x1D, 0x6B, 0x49 }); // Code 128
            out.write((byte) kode.length());
            out.write(kode.getBytes("UTF-8"));

            out.write(new byte[]{ 0x0A, 0x0A });
            out.write(new byte[]{ 0x1D, 0x56, 0x00 }); // Cut

            DocPrintJob job = printer.createPrintJob();
            Doc doc = new SimpleDoc(out.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, null);

            System.out.println("Struk dicetak.");

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

    
    public void tampilkanDataPembelian() {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Kode Barang");
    model.addColumn("Nama Barang");
    model.addColumn("Qty");
    model.addColumn("Total Harga");
    model.addColumn("Tanggal");

    try {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");
        String sql = "SELECT kode_barang, nama_barang, qty, total_harga FROM transaksi_awal";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_barang"),
                rs.getString("nama_barang"),
                rs.getInt("qty"),
                rs.getInt("total_harga"),
            });
        }

        rs.close();
        st.close();
        con.close();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Gagal menampilkan data: " + ex.getMessage());
    }
    
    tabletransaksi.setModel(model);
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
            double saldo = rs.getDouble("saldo");

            // Tampilkan input pembayaran
            String inputBayar = JOptionPane.showInputDialog(this,
                "Nama: " + nama + "\nSaldo saat ini: Rp" + saldo +
                "\n\nMasukkan jumlah pembayaran:");

            if (inputBayar != null && !inputBayar.isEmpty()) {
                try {
                    double jumlahBayar = Double.parseDouble(inputBayar);

                    if (jumlahBayar > saldo) {
                        JOptionPane.showMessageDialog(this,
                            "Saldo tidak mencukupi!", "Gagal", JOptionPane.WARNING_MESSAGE);
                    } else {
                        double saldoBaru = saldo - jumlahBayar;

                        // Update saldo di database
                        String updateSql = "UPDATE manajemen_nasabah SET saldo = ? WHERE id_nasabah = ?";
                        PreparedStatement updatePst = conn.prepareStatement(updateSql);
                        updatePst.setDouble(1, saldoBaru);
                        updatePst.setString(2, id_nasabah);
                        updatePst.executeUpdate();
                        updatePst.close();

                        // Tampilkan saldo baru di txttunai
                        txttunai.setText(String.valueOf((int) jumlahBayar));

                        JOptionPane.showMessageDialog(this,
                            "Pembayaran berhasil!\nSisa Saldo: Rp" + saldoBaru);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Input tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
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
    private void hitungTotal() {
    DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
    int total = 0;
    for (int i = 0; i < model.getRowCount(); i++) {
        int qty = (int) model.getValueAt(i, 2);   // kolom Qty
        int harga = (int) model.getValueAt(i, 3); // kolom Harga
        total += qty * harga;
    }
    txttotal.setText(String.valueOf(total));
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
        shadowPanel2 = new component.ShadowPanel();
        scanbarang = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtbarang = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtharga = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtstok = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtqty = new javax.swing.JTextField();
        btntambah = new component.Jbutton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabletransaksi = new component.Table();
        btnbatal = new component.Jbutton();
        txtnasabah = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txttotal = new javax.swing.JTextField();
        btnbayar = new component.Jbutton();
        txttunai = new javax.swing.JTextField();
        txtkembalian = new javax.swing.JTextField();
        jbutton7 = new component.Jbutton();

        setPreferredSize(new java.awt.Dimension(1200, 716));
        setLayout(new java.awt.CardLayout());

        panelMain.setLayout(new java.awt.CardLayout());

        panelView.setLayout(new java.awt.CardLayout());

        ShadowUtama.setBackground(new java.awt.Color(245, 245, 245));

        scanbarang.setBackground(new java.awt.Color(245, 245, 245));
        scanbarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanbarangActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Scan Barang Aktif . . .");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Nama Barang");

        txtbarang.setEditable(false);
        txtbarang.setBackground(new java.awt.Color(245, 245, 245));
        txtbarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtbarangActionPerformed(evt);
            }
        });
        txtbarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtbarangKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtbarangKeyTyped(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Harga");

        txtharga.setEditable(false);
        txtharga.setBackground(new java.awt.Color(245, 245, 245));
        txtharga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txthargaActionPerformed(evt);
            }
        });
        txtharga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txthargaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txthargaKeyTyped(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setText("Stok");

        txtstok.setEditable(false);
        txtstok.setBackground(new java.awt.Color(245, 245, 245));
        txtstok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtstokActionPerformed(evt);
            }
        });
        txtstok.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtstokKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtstokKeyTyped(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Qty");

        txtqty.setBackground(new java.awt.Color(245, 245, 245));
        txtqty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtqtyActionPerformed(evt);
            }
        });
        txtqty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtqtyKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtqtyKeyTyped(evt);
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
        btntambah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btntambahKeyPressed(evt);
            }
        });

        tabletransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Kode_Barang", "Nama Barang", "Harga", "Qty", "Total_Harga"
            }
        ));
        jScrollPane2.setViewportView(tabletransaksi);

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

        txtnasabah.setBackground(new java.awt.Color(245, 245, 245));
        txtnasabah.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        txtnasabah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnasabahActionPerformed(evt);
            }
        });
        txtnasabah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtnasabahKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtnasabahKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtnasabahKeyTyped(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Tap Member Nasabah . . .");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Total            :");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Tunai           :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Kembalian  :");

        txttotal.setBackground(new java.awt.Color(245, 245, 245));
        txttotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txttotalActionPerformed(evt);
            }
        });
        txttotal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txttotalKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txttotalKeyTyped(evt);
            }
        });

        btnbayar.setForeground(new java.awt.Color(0, 0, 0));
        btnbayar.setText("Bayar");
        btnbayar.setFillClick(new java.awt.Color(230, 210, 20));
        btnbayar.setFillOriginal(new java.awt.Color(255, 254, 84));
        btnbayar.setFillOver(new java.awt.Color(245, 234, 50));
        btnbayar.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnbayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbayarActionPerformed(evt);
            }
        });

        txttunai.setBackground(new java.awt.Color(245, 245, 245));
        txttunai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txttunaiActionPerformed(evt);
            }
        });
        txttunai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txttunaiKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txttunaiKeyTyped(evt);
            }
        });

        txtkembalian.setBackground(new java.awt.Color(245, 245, 245));
        txtkembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtkembalianActionPerformed(evt);
            }
        });
        txtkembalian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtkembalianKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtkembalianKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtkembalianKeyTyped(evt);
            }
        });

        jbutton7.setForeground(new java.awt.Color(0, 0, 0));
        jbutton7.setText("Riwayat");
        jbutton7.setFillClick(new java.awt.Color(230, 210, 20));
        jbutton7.setFillOriginal(new java.awt.Color(255, 254, 84));
        jbutton7.setFillOver(new java.awt.Color(245, 234, 50));
        jbutton7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jbutton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbutton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowPanel2Layout = new javax.swing.GroupLayout(shadowPanel2);
        shadowPanel2.setLayout(shadowPanel2Layout);
        shadowPanel2Layout.setHorizontalGroup(
            shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel8)
                        .addComponent(jLabel9)
                        .addComponent(jLabel6)
                        .addComponent(txtstok, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                        .addComponent(txtharga)
                        .addComponent(jLabel2)
                        .addComponent(scanbarang)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtbarang)
                        .addComponent(txtqty)
                        .addGroup(shadowPanel2Layout.createSequentialGroup()
                            .addComponent(btnbatal, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btntambah, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(txtnasabah))
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(shadowPanel2Layout.createSequentialGroup()
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(35, 35, 35)
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txttunai, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txttotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .addComponent(txtkembalian))
                        .addGap(45, 45, 45)
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnbayar, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jbutton7, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(28, 28, 28))
        );
        shadowPanel2Layout.setVerticalGroup(
            shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scanbarang, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtbarang, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(txtharga, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtstok, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtqty, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btntambah, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnbatal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(jLabel10)
                        .addGap(20, 20, 20)
                        .addComponent(txtnasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(shadowPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(shadowPanel2Layout.createSequentialGroup()
                                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(txttunai, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(shadowPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(txtkembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(shadowPanel2Layout.createSequentialGroup()
                                .addComponent(btnbayar, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jbutton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(124, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ShadowUtamaLayout = new javax.swing.GroupLayout(ShadowUtama);
        ShadowUtama.setLayout(ShadowUtamaLayout);
        ShadowUtamaLayout.setHorizontalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ShadowUtamaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shadowPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        ShadowUtamaLayout.setVerticalGroup(
            ShadowUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelView.add(ShadowUtama, "card2");

        panelMain.add(panelView, "card2");

        add(panelMain, "card2");
    }// </editor-fold>//GEN-END:initComponents

    private void scanbarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanbarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_scanbarangActionPerformed

    private void txtbarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtbarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtbarangActionPerformed

    private void txtbarangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtbarangKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtbarangKeyPressed

    private void txtbarangKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtbarangKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtbarangKeyTyped

    private void txthargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txthargaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txthargaActionPerformed

    private void txthargaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txthargaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txthargaKeyPressed

    private void txthargaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txthargaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txthargaKeyTyped

    private void txtstokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtstokActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtstokActionPerformed

    private void txtstokKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtstokKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtstokKeyPressed

    private void txtstokKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtstokKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtstokKeyTyped

    private void txtqtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtqtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtqtyActionPerformed

    private void txtqtyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtqtyKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtqtyKeyPressed

    private void txtqtyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtqtyKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtqtyKeyTyped

    private void btntambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahActionPerformed
        // TODO add your handling code here:
        btntambah.addActionListener(e -> {
            String kode = scanbarang.getText().trim();
            String nama = txtbarang.getText().trim();
            int harga = Integer.parseInt(txtharga.getText().trim());
            int qty = Integer.parseInt(txtqty.getText().trim());

            // Harga total pembelian
            int totalHarga = harga * qty;
            java.sql.Date tanggal = new java.sql.Date(System.currentTimeMillis());

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu", "root", "");

                // Cek apakah barang sudah ada
                String cekSql = "SELECT stok FROM data_barang WHERE kode_barang = ?";
                PreparedStatement pstCek = con.prepareStatement(cekSql);
                pstCek.setString(1, kode);
                ResultSet rs = pstCek.executeQuery();

                if (rs.next()) {
                    int stokLama = rs.getInt("stok");
                    if (stokLama < qty) {
                        JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                        pstCek.close();
                        rs.close();
                        con.close();
                        return;
                    }

                    // Update stok (stok dikurangi qty)
                    String updateSql = "UPDATE data_barang SET nama_barang=?, harga=?, stok=stok - ? WHERE kode_barang=?";
                    PreparedStatement pstUpdate = con.prepareStatement(updateSql);
                    pstUpdate.setString(1, nama);
                    pstUpdate.setInt(2, harga);
                    pstUpdate.setInt(3, qty);
                    pstUpdate.setString(4, kode);
                    pstUpdate.executeUpdate();
                    pstUpdate.close();
                } else {
                    JOptionPane.showMessageDialog(this, "Barang dengan kode tersebut belum terdaftar!");
                    pstCek.close();
                    rs.close();
                    con.close();
                    return;
                }

                pstCek.close();
                rs.close();

                // Insert ke tabel pembelian
                String sqlPembelian = "INSERT INTO transaksi_awal (kode_barang, nama_barang, qty, total_harga, tanggal) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstPembelian = con.prepareStatement(sqlPembelian);
                pstPembelian.setString(1, kode);
                pstPembelian.setString(2, nama);
                pstPembelian.setInt(3, qty);
                pstPembelian.setInt(4, totalHarga);
                pstPembelian.setDate(5, tanggal);
                pstPembelian.executeUpdate();
                pstPembelian.close();

                tampilkanDataPembelian(); // Refresh tampilan tabel

                // Tambahkan hanya yang baru ke tabletransaksi
                DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();
                model.setRowCount(0);
                model.addRow(new Object[]{kode, nama, qty, totalHarga});

                try {
                    int totalLama = txttotal.getText().isEmpty() ? 0 : Integer.parseInt(txttotal.getText());
                    int totalBaru = totalLama + totalHarga;
                    txttotal.setText(String.valueOf(totalBaru));
                } catch (NumberFormatException ex) {
                    txttotal.setText(String.valueOf(totalHarga));
                }

                JOptionPane.showMessageDialog(this, "Pembelian berhasil ditambahkan dan stok dikurangi.");
                bersihkanForm();

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal tambah: " + ex.getMessage());
            }
        });

    }//GEN-LAST:event_btntambahActionPerformed

    private void btntambahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btntambahKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btntambahKeyPressed

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
            }
        });
    }//GEN-LAST:event_btnbatalActionPerformed

    private void txtnasabahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnasabahActionPerformed
        // TODO add your handling code here:
        prosesPembayaranNasabah(txtnasabah.getText());
    }//GEN-LAST:event_txtnasabahActionPerformed

    private void txtnasabahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtnasabahKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnasabahKeyPressed

    private void txtnasabahKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtnasabahKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnasabahKeyReleased

    private void txtnasabahKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtnasabahKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnasabahKeyTyped

    private void txttotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txttotalActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txttotalActionPerformed

    private void txttotalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txttotalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txttotalKeyPressed

    private void txttotalKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txttotalKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txttotalKeyTyped

    private void btnbayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbayarActionPerformed

        DefaultTableModel model = (DefaultTableModel) tabletransaksi.getModel();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada barang di keranjang.");
            return;
        }

        // Ambil total dari txttotal
        int total;
        try {
            total = Integer.parseInt(txttotal.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total tidak valid.");
            return;
        }

        // Ambil dan validasi input tunai
        int tunai;
        try {
            tunai = Integer.parseInt(txttunai.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah tunai yang valid.");
            return;
        }

        // Cek apakah tunai mencukupi
        if (tunai < total) {
            JOptionPane.showMessageDialog(this, "Uang tunai tidak mencukupi.");
            return;
        }

        // Hitung kembalian
        int kembali = tunai - total;
        txtkembalian.setText(String.valueOf(kembali));

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bank_sampah_sahabat_ibu",
                "root", ""
            );

            // Generate kode transaksi unik
            String kodeTransaksi = "TRX" + System.currentTimeMillis();

            String insertSql = "INSERT INTO transaksi (kode_transaksi, kode_barang, nama_barang, qty, harga, total_harga, bayar, kembalian, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(insertSql);

            List<Object[]> pesanan = new ArrayList<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                String kodeBarang = model.getValueAt(i, 0).toString();
                String namaBarang = model.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                int harga = Integer.parseInt(model.getValueAt(i, 3).toString());
                int totalHarga = qty * harga;

                // Simpan ke database
                ps.setString(1, kodeTransaksi);
                ps.setString(2, kodeBarang);
                ps.setString(3, namaBarang);
                ps.setInt(4, qty);
                ps.setInt(5, harga);
                ps.setInt(6, totalHarga);
                ps.setInt(7, tunai);
                ps.setInt(8, kembali);
                ps.addBatch();

                // Simpan juga ke list untuk cetak struk
                pesanan.add(new Object[]{namaBarang, qty, harga});
            }

            ps.executeBatch();
            conn.close();

            // CETAK STRUK PRINTER THERMAL
            Print printer = new Print();
            printer.cetakStruk(kodeTransaksi, pesanan, tunai);

            // Reset UI
            model.setRowCount(0);
            bersihkanForm();
            bersihkanForm2();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan dan struk dicetak.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnbayarActionPerformed

    private void txttunaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txttunaiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txttunaiActionPerformed

    private void txttunaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txttunaiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txttunaiKeyPressed

    private void txttunaiKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txttunaiKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txttunaiKeyTyped

    private void txtkembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtkembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtkembalianActionPerformed

    private void txtkembalianKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtkembalianKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtkembalianKeyPressed

    private void txtkembalianKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtkembalianKeyReleased
        // TODO add your handling code here
        try {
            int total = Integer.parseInt(txttotal.getText());
            int tunai = Integer.parseInt(txttunai.getText());
            int kembali = tunai - total;

            if (kembali >= 0) {
                txtkembalian.setText(String.valueOf(kembali));
            } else {
                txtkembalian.setText("0");
            }
        } catch (NumberFormatException e) {
            txtkembalian.setText("0");
        }
    }//GEN-LAST:event_txtkembalianKeyReleased

    private void txtkembalianKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtkembalianKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtkembalianKeyTyped

    private void jbutton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbutton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jbutton7ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.ShadowPanel ShadowUtama;
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
    private component.Jbutton jbutton7;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelView;
    private javax.swing.JTextField scanbarang;
    private component.ShadowPanel shadowPanel2;
    private component.Table tabletransaksi;
    private javax.swing.JTextField txtbarang;
    private javax.swing.JTextField txtharga;
    private javax.swing.JTextField txtkembalian;
    private javax.swing.JTextField txtnasabah;
    private javax.swing.JTextField txtqty;
    private javax.swing.JTextField txtstok;
    private javax.swing.JTextField txttotal;
    private javax.swing.JTextField txttunai;
    // End of variables declaration//GEN-END:variables
}
