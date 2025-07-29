package com.daniellbintang.gudangapp.model;

public class Barang {
    private int id;
    private String nama;
    private int stok;
    private double harga;

    public Barang(int id, String nama, int stok, double harga) {
        this.id = id;
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
    }

    public Barang(String nama, int stok, double harga) {
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public int getStok() { return stok; }
    public double getHarga() { return harga; }

    public void setId(int id) { this.id = id; }
    public void setNama(String nama) { this.nama = nama; }
    public void setStok(int stok) { this.stok = stok; }
    public void setHarga(double harga) { this.harga = harga; }
}