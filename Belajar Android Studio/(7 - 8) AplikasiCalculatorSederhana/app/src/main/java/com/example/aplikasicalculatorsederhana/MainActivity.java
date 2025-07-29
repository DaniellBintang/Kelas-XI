package com.example.aplikasicalculatorsederhana;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasicalculatorsederhana.R;

public class MainActivity extends AppCompatActivity {

    // Deklarasi variabel untuk komponen UI
    private EditText editTextAngka1, editTextAngka2;
    private TextView textViewResult;
    private Button buttonTambah, buttonKurang, buttonKali, buttonBagi, buttonClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi komponen UI
        initializeViews();

        // Setup event listeners untuk tombol-tombol
        setupClickListeners();
    }

    private void initializeViews() {
        // Hubungkan variabel dengan komponen di layout XML
        editTextAngka1 = findViewById(R.id.editTextAngka1);
        editTextAngka2 = findViewById(R.id.editTextAngka2);
        textViewResult = findViewById(R.id.textViewResult);

        buttonTambah = findViewById(R.id.buttonTambah);
        buttonKurang = findViewById(R.id.buttonKurang);
        buttonKali = findViewById(R.id.buttonKali);
        buttonBagi = findViewById(R.id.buttonBagi);
        buttonClear = findViewById(R.id.buttonClear);
    }

    private void setupClickListeners() {
        // Event listener untuk tombol Penjumlahan
        buttonTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation("+");
            }
        });

        // Event listener untuk tombol Pengurangan
        buttonKurang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation("-");
            }
        });

        // Event listener untuk tombol Perkalian
        buttonKali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation("*");
            }
        });

        // Event listener untuk tombol Pembagian
        buttonBagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation("/");
            }
        });

        // Event listener untuk tombol Clear/Reset
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });
    }

    private void performCalculation(String operation) {
        try {
            // Ambil input dari EditText
            String input1 = editTextAngka1.getText().toString().trim();
            String input2 = editTextAngka2.getText().toString().trim();

            // Validasi input tidak kosong
            if (input1.isEmpty() || input2.isEmpty()) {
                showToast("Silakan masukkan kedua angka!");
                return;
            }

            // Konversi string ke double
            double angka1 = Double.parseDouble(input1);
            double angka2 = Double.parseDouble(input2);
            double hasil = 0;

            // Lakukan operasi berdasarkan parameter yang diterima
            switch (operation) {
                case "+":
                    hasil = angka1 + angka2;
                    break;
                case "-":
                    hasil = angka1 - angka2;
                    break;
                case "*":
                    hasil = angka1 * angka2;
                    break;
                case "/":
                    if (angka2 == 0) {
                        showToast("Error: Tidak bisa dibagi dengan nol!");
                        return;
                    }
                    hasil = angka1 / angka2;
                    break;
            }

            // Tampilkan hasil dengan format yang rapi
            displayResult(hasil, angka1, angka2, operation);

        } catch (NumberFormatException e) {
            showToast("Error: Format angka tidak valid!");
        } catch (Exception e) {
            showToast("Error: Terjadi kesalahan dalam perhitungan!");
        }
    }

    private void displayResult(double hasil, double angka1, double angka2, String operation) {
        // Format hasil untuk menghindari desimal yang tidak perlu
        String resultText;
        if (hasil == (long) hasil) {
            // Jika hasil adalah bilangan bulat, tampilkan tanpa desimal
            resultText = String.format("%.0f", hasil);
        } else {
            // Jika hasil adalah desimal, tampilkan dengan 2 digit dibelakang koma
            resultText = String.format("%.2f", hasil);
        }

        // Update TextView hasil
        textViewResult.setText(resultText);

        // Tampilkan operasi yang dilakukan dalam Toast
        String operationSymbol = getOperationSymbol(operation);
        String message = String.format("%.0f %s %.0f = %s", angka1, operationSymbol, angka2, resultText);
        showToast(message);
    }

    private String getOperationSymbol(String operation) {
        switch (operation) {
            case "+": return "+";
            case "-": return "-";
            case "*": return "×";
            case "/": return "÷";
            default: return "";
        }
    }

    private void clearAll() {
        // Bersihkan semua input dan hasil
        editTextAngka1.setText("");
        editTextAngka2.setText("");
        textViewResult.setText("0");

        // Set focus ke input pertama untuk kemudahan penggunaan
        editTextAngka1.requestFocus();

        showToast("Calculator berhasil direset!");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}