package component;

import javax.swing.Icon;

public class ModelItem {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }
    
    public double getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public Icon getGambar() {
        return gambar;
    }

    public void setGambar(Icon gambar) {
        this.gambar = gambar;
    }


    public ModelItem(int id, String nama, String kode, double harga, int stok, Icon gambar) {
        this.id = id;
        this.nama = nama;
        this.kode = kode;
        this.harga = harga;
        this.stok = stok;
        this.gambar = gambar;
    }

    public ModelItem() {
    }

    private int id;
    private String nama;
    private String kode;
    private double harga;
    private int stok;
    private Icon gambar;
}
