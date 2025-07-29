package com.daniellbintang.gudangapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.daniellbintang.gudangapp.database.DatabaseHelper;
import com.daniellbintang.gudangapp.model.Barang;

public class FormBarangActivity extends AppCompatActivity {

    private EditText etNama, etStok, etHarga;
    private Button btnSimpan;
    private DatabaseHelper db;

    private boolean isEdit = false;
    private int idBarang = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_barang);

        etNama = findViewById(R.id.etNamaBarang);
        etStok = findViewById(R.id.etStokBarang);
        etHarga = findViewById(R.id.etHargaBarang);
        btnSimpan = findViewById(R.id.btnSimpanBarang);

        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("edit", false);
        if (isEdit) {
            idBarang = intent.getIntExtra("id", -1);
            etNama.setText(intent.getStringExtra("nama"));
            etStok.setText(String.valueOf(intent.getIntExtra("stok", 0)));
            etHarga.setText(String.valueOf(intent.getDoubleExtra("harga", 0)));
        }

        btnSimpan.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String stokStr = etStok.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();

            if (nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            int stok = Integer.parseInt(stokStr);
            double harga = Double.parseDouble(hargaStr);

            if (isEdit && idBarang != -1) {
                Barang barang = new Barang(idBarang, nama, stok, harga);
                db.updateBarang(barang);
                Toast.makeText(this, "Barang diupdate!", Toast.LENGTH_SHORT).show();
            } else {
                Barang barang = new Barang(nama, stok, harga);
                db.tambahBarang(barang);
                Toast.makeText(this, "Barang ditambah!", Toast.LENGTH_SHORT).show();
            }
            setResult(RESULT_OK);
            finish();
        });
    }
}