package com.example.sharedpreferencesdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    // Konstanta untuk SharedPreferences
    private static final String PREF_NAME = "UserDataPrefs";
    private static final String KEY_NAMA = "nama";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TELEPON = "telepon";
    private static final String KEY_ALAMAT = "alamat";

    // Deklarasi komponen UI
    private TextInputEditText etNama, etEmail, etTelepon, etAlamat;
    private MaterialButton btnSimpan, btnTampil, btnHapus;
    private TextView tvNama, tvEmail, tvTelepon, tvAlamat;
    private CardView dataCard;

    // SharedPreferences instance
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Inisialisasi komponen UI
        initUI();

        // Set event listeners
        setupEventListeners();

        // Load data yang sudah tersimpan (jika ada)
        loadSavedData();
    }

    private void initUI() {
        // EditText components
        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etTelepon = findViewById(R.id.etTelepon);
        etAlamat = findViewById(R.id.etAlamat);

        // Button components
        btnSimpan = findViewById(R.id.btnSimpan);
        btnTampil = findViewById(R.id.btnTampil);
        btnHapus = findViewById(R.id.btnHapus);

        // TextView components untuk menampilkan data
        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelepon = findViewById(R.id.tvTelepon);
        tvAlamat = findViewById(R.id.tvAlamat);

        // CardView untuk menampilkan data
        dataCard = findViewById(R.id.dataCard);
    }

    private void setupEventListeners() {
        // Event listener untuk tombol Simpan
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });

        // Event listener untuk tombol Tampil
        btnTampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanData();
            }
        });

        // Event listener untuk tombol Hapus
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusData();
            }
        });
    }

    private void simpanData() {
        // Ambil data dari EditText
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telepon = etTelepon.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();

        // Validasi input
        if (nama.isEmpty() || email.isEmpty() || telepon.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan data ke SharedPreferences
        editor.putString(KEY_NAMA, nama);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TELEPON, telepon);
        editor.putString(KEY_ALAMAT, alamat);

        // Commit perubahan
        boolean success = editor.commit();

        if (success) {
            Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void tampilkanData() {
        // Ambil data dari SharedPreferences
        String nama = sharedPreferences.getString(KEY_NAMA, "");
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String telepon = sharedPreferences.getString(KEY_TELEPON, "");
        String alamat = sharedPreferences.getString(KEY_ALAMAT, "");

        // Cek apakah ada data tersimpan
        if (nama.isEmpty() && email.isEmpty() && telepon.isEmpty() && alamat.isEmpty()) {
            Toast.makeText(this, "Tidak ada data tersimpan!", Toast.LENGTH_SHORT).show();
            dataCard.setVisibility(View.GONE);
            return;
        }

        // Tampilkan data ke TextView
        tvNama.setText("Nama: " + (nama.isEmpty() ? "-" : nama));
        tvEmail.setText("Email: " + (email.isEmpty() ? "-" : email));
        tvTelepon.setText("Telepon: " + (telepon.isEmpty() ? "-" : telepon));
        tvAlamat.setText("Alamat: " + (alamat.isEmpty() ? "-" : alamat));

        // Tampilkan card data
        dataCard.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Data berhasil ditampilkan!", Toast.LENGTH_SHORT).show();
    }

    private void hapusData() {
        // Hapus semua data dari SharedPreferences
        editor.clear();
        boolean success = editor.commit();

        if (success) {
            // Reset tampilan
            clearForm();
            dataCard.setVisibility(View.GONE);
            Toast.makeText(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal menghapus data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        // Kosongkan semua EditText
        etNama.setText("");
        etEmail.setText("");
        etTelepon.setText("");
        etAlamat.setText("");
    }

    private void loadSavedData() {
        // Cek apakah ada data tersimpan saat aplikasi dibuka
        String nama = sharedPreferences.getString(KEY_NAMA, "");
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String telepon = sharedPreferences.getString(KEY_TELEPON, "");
        String alamat = sharedPreferences.getString(KEY_ALAMAT, "");

        // Jika ada data tersimpan, tampilkan otomatis
        if (!nama.isEmpty() || !email.isEmpty() || !telepon.isEmpty() || !alamat.isEmpty()) {
            tampilkanData();
        }
    }
}