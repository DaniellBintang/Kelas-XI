package com.example.scrollview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Deklarasi variabel untuk komponen UI
    private ScrollView scrollView;
    private EditText editTextName, editTextEmail;
    private Button buttonSubmit;
    private TextView textViewLongContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen UI
        initializeViews();

        // Setup event listeners
        setupClickListeners();

        // Demo scroll programmatically
        demoScrollFeatures();
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewLongContent = findViewById(R.id.textViewLongContent);
    }

    private void setupClickListeners() {
        // Event listener untuk tombol Submit
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
    }

    private void handleSubmit() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            showToast("⚠️ Silakan isi semua field!");
            return;
        }

        // Validasi email sederhana
        if (!email.contains("@")) {
            showToast("❌ Format email tidak valid!");
            return;
        }

        // Tampilkan hasil
        String message = "✅ Data berhasil disubmit!\n" +
                "Nama: " + name + "\n" +
                "Email: " + email;
        showToast(message);

        // Clear input
        editTextName.setText("");
        editTextEmail.setText("");

        // Scroll ke atas setelah submit
        scrollToTop();
    }

    private void demoScrollFeatures() {
        // Demo fitur scroll programmatically
        // Anda bisa menambahkan fungsi scroll otomatis di sini

        // Contoh: Scroll ke posisi tertentu setelah 3 detik
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // scrollView.smoothScrollTo(0, 500); // Uncomment untuk demo auto scroll
            }
        }, 3000);
    }

    // Method untuk scroll ke atas
    private void scrollToTop() {
        scrollView.smoothScrollTo(0, 0);
    }

    // Method untuk scroll ke bawah
    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // Method untuk scroll ke posisi tertentu
    private void scrollToPosition(int x, int y) {
        scrollView.smoothScrollTo(x, y);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Scroll ke atas saat aplikasi kembali aktif
        scrollToTop();
    }
}