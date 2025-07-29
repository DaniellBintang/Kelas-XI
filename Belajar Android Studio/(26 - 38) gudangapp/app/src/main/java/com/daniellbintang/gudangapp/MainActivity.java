package com.daniellbintang.gudangapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daniellbintang.gudangapp.adapter.BarangAdapter;
import com.daniellbintang.gudangapp.database.DatabaseHelper;
import com.daniellbintang.gudangapp.model.Barang;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvBarang;
    private BarangAdapter adapter;
    private List<Barang> listBarang;
    private DatabaseHelper db;
    private Button btnTambahBarang;

    private ActivityResultLauncher<Intent> launcher;

    private Barang barangEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvBarang = findViewById(R.id.rvBarang);
        btnTambahBarang = findViewById(R.id.btnTambahBarang);

        db = new DatabaseHelper(this);

        listBarang = db.getSemuaBarang();

        adapter = new BarangAdapter(this, listBarang, new BarangAdapter.OnMenuClickListener() {
            @Override
            public void onEdit(Barang barang) {
                barangEdit = barang;
                Intent intent = new Intent(MainActivity.this, FormBarangActivity.class);
                intent.putExtra("edit", true);
                intent.putExtra("id", barang.getId());
                intent.putExtra("nama", barang.getNama());
                intent.putExtra("stok", barang.getStok());
                intent.putExtra("harga", barang.getHarga());
                launcher.launch(intent);
            }

            @Override
            public void onDelete(Barang barang) {
                showDeleteDialog(barang);
            }
        });

        rvBarang.setLayoutManager(new LinearLayoutManager(this));
        rvBarang.setAdapter(adapter);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // reload list
                    listBarang.clear();
                    listBarang.addAll(db.getSemuaBarang());
                    adapter.notifyDataSetChanged();
                }
        );

        btnTambahBarang.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormBarangActivity.class);
            launcher.launch(intent);
        });
    }

    private void showDeleteDialog(Barang barang) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Barang")
                .setMessage("Yakin ingin menghapus barang '" + barang.getNama() + "'?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    db.hapusBarang(barang.getId());
                    listBarang.clear();
                    listBarang.addAll(db.getSemuaBarang());
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}