package com.example.aplikasikonversisuhu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aplikasikonversisuhu.R;

public class MainActivity extends AppCompatActivity {

    // Deklarasi variabel untuk komponen UI
    private EditText editTextSuhu;
    private RadioGroup radioGroupKonversi;
    private TextView textViewHasil, textViewFormula;
    private Button buttonKonversi, buttonClear;

    // RadioButton variables
    private RadioButton radioCelciusToReamur, radioCelciusToFahrenheit,
            radioCelciusToKelvin, radioReamurToCelcius, radioReamurToFahrenheit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen UI
        initializeViews();

        // Setup event listeners
        setupClickListeners();
    }

    private void initializeViews() {
        editTextSuhu = findViewById(R.id.editTextSuhu);
        radioGroupKonversi = findViewById(R.id.radioGroupKonversi);
        textViewHasil = findViewById(R.id.textViewHasil);
        textViewFormula = findViewById(R.id.textViewFormula);
        buttonKonversi = findViewById(R.id.buttonKonversi);
        buttonClear = findViewById(R.id.buttonClear);

        // RadioButtons
        radioCelciusToReamur = findViewById(R.id.radioCelciusToReamur);
        radioCelciusToFahrenheit = findViewById(R.id.radioCelciusToFahrenheit);
        radioCelciusToKelvin = findViewById(R.id.radioCelciusToKelvin);
        radioReamurToCelcius = findViewById(R.id.radioReamurToCelcius);
        radioReamurToFahrenheit = findViewById(R.id.radioReamurToFahrenheit);
    }

    private void setupClickListeners() {
        // Event listener untuk tombol Konversi
        buttonKonversi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });

        // Event listener untuk tombol Clear
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        // Event listener untuk RadioGroup untuk update formula
        radioGroupKonversi.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateFormulaDisplay();
            }
        });
    }

    private void performConversion() {
        try {
            // Ambil input suhu
            String inputSuhu = editTextSuhu.getText().toString().trim();

            // Validasi input tidak kosong
            if (inputSuhu.isEmpty()) {
                showToast("⚠️ Silakan masukkan nilai suhu!");
                editTextSuhu.requestFocus();
                return;
            }

            // Konversi string ke double
            double suhuInput = Double.parseDouble(inputSuhu);

            // Tentukan jenis konversi berdasarkan radio button yang dipilih
            int selectedRadioId = radioGroupKonversi.getCheckedRadioButtonId();
            double hasilKonversi = 0;
            String satuan = "";
            String jenisKonversi = "";

            if (selectedRadioId == R.id.radioCelciusToReamur) {
                hasilKonversi = celciusToReamur(suhuInput);
                satuan = "°R";
                jenisKonversi = "Celsius → Reamur";
            } else if (selectedRadioId == R.id.radioCelciusToFahrenheit) {
                hasilKonversi = celciusToFahrenheit(suhuInput);
                satuan = "°F";
                jenisKonversi = "Celsius → Fahrenheit";
            } else if (selectedRadioId == R.id.radioCelciusToKelvin) {
                hasilKonversi = celciusToKelvin(suhuInput);
                satuan = "K";
                jenisKonversi = "Celsius → Kelvin";
            } else if (selectedRadioId == R.id.radioReamurToCelcius) {
                hasilKonversi = reamurToCelcius(suhuInput);
                satuan = "°C";
                jenisKonversi = "Reamur → Celsius";
            } else if (selectedRadioId == R.id.radioReamurToFahrenheit) {
                hasilKonversi = reamurToFahrenheit(suhuInput);
                satuan = "°F";
                jenisKonversi = "Reamur → Fahrenheit";
            }

            // Tampilkan hasil
            displayResult(hasilKonversi, satuan, jenisKonversi, suhuInput);

        } catch (NumberFormatException e) {
            showToast("❌ Error: Format angka tidak valid!");
            editTextSuhu.requestFocus();
        } catch (Exception e) {
            showToast("❌ Error: Terjadi kesalahan dalam konversi!");
        }
    }

    // Method konversi suhu
    private double celciusToReamur(double celsius) {
        return (4.0/5.0) * celsius;
    }

    private double celciusToFahrenheit(double celsius) {
        return (9.0/5.0) * celsius + 32;
    }

    private double celciusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    private double reamurToCelcius(double reamur) {
        return (5.0/4.0) * reamur;
    }

    private double reamurToFahrenheit(double reamur) {
        return (9.0/4.0) * reamur + 32;
    }

    private void displayResult(double hasil, String satuan, String jenisKonversi, double inputSuhu) {
        // Format hasil dengan 2 decimal places atau integer jika bulat
        String hasilText;
        if (hasil == (long) hasil) {
            hasilText = String.format("%.0f %s", hasil, satuan);
        } else {
            hasilText = String.format("%.2f %s", hasil, satuan);
        }

        // Update TextView hasil
        textViewHasil.setText(hasilText);

        // Update formula dengan input yang sebenarnya
        updateFormulaWithResult(jenisKonversi, inputSuhu, hasil);

        // Toast sukses
        showToast("✅ Konversi berhasil: " + hasilText);
    }

    private void updateFormulaDisplay() {
        int selectedRadioId = radioGroupKonversi.getCheckedRadioButtonId();
        String formula = "";

        if (selectedRadioId == R.id.radioCelciusToReamur) {
            formula = "Rumus: R = (4/5) × C";
        } else if (selectedRadioId == R.id.radioCelciusToFahrenheit) {
            formula = "Rumus: F = (9/5) × C + 32";
        } else if (selectedRadioId == R.id.radioCelciusToKelvin) {
            formula = "Rumus: K = C + 273.15";
        } else if (selectedRadioId == R.id.radioReamurToCelcius) {
            formula = "Rumus: C = (5/4) × R";
        } else if (selectedRadioId == R.id.radioReamurToFahrenheit) {
            formula = "Rumus: F = (9/4) × R + 32";
        }

        textViewFormula.setText(formula);
    }

    private void updateFormulaWithResult(String jenisKonversi, double input, double hasil) {
        String formulaWithResult = "";

        if (jenisKonversi.equals("Celsius → Reamur")) {
            formulaWithResult = String.format("%.1f°C = (4/5) × %.1f = %.2f°R", input, input, hasil);
        } else if (jenisKonversi.equals("Celsius → Fahrenheit")) {
            formulaWithResult = String.format("%.1f°C = (9/5) × %.1f + 32 = %.2f°F", input, input, hasil);
        } else if (jenisKonversi.equals("Celsius → Kelvin")) {
            formulaWithResult = String.format("%.1f°C = %.1f + 273.15 = %.2fK", input, input, hasil);
        } else if (jenisKonversi.equals("Reamur → Celsius")) {
            formulaWithResult = String.format("%.1f°R = (5/4) × %.1f = %.2f°C", input, input, hasil);
        } else if (jenisKonversi.equals("Reamur → Fahrenheit")) {
            formulaWithResult = String.format("%.1f°R = (9/4) × %.1f + 32 = %.2f°F", input, input, hasil);
        }

        textViewFormula.setText(formulaWithResult);
    }

    private void clearAll() {
        // Bersihkan input
        editTextSuhu.setText("");

        // Reset hasil
        textViewHasil.setText("Masukkan suhu dan pilih jenis konversi");

        // Reset radio button ke pilihan pertama
        radioCelciusToReamur.setChecked(true);

        // Update formula display
        updateFormulaDisplay();

        // Set focus ke input
        editTextSuhu.requestFocus();

        showToast("🗑️ Konverter berhasil direset!");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}