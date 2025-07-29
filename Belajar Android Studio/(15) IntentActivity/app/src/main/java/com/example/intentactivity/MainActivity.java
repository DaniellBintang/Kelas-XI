package com.example.intentactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editTextInput;
    Button buttonBarang, buttonPenjualan, buttonPembelian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextInput = findViewById(R.id.editTextInput);
        buttonBarang = findViewById(R.id.buttonBarang);
        buttonPenjualan = findViewById(R.id.buttonPenjualan);
        buttonPembelian = findViewById(R.id.buttonPembelian);

        buttonBarang.setOnClickListener(v -> openResultActivity("Barang"));
        buttonPenjualan.setOnClickListener(v -> openResultActivity("Penjualan"));
        buttonPembelian.setOnClickListener(v -> openResultActivity("Pembelian"));
    }

    private void openResultActivity(String type) {
        String input = editTextInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Input tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent;
        switch (type) {
            case "Barang":
                intent = new Intent(this, BarangActivity.class);
                break;
            case "Penjualan":
                intent = new Intent(this, PenjualanActivity.class);
                break;
            case "Pembelian":
                intent = new Intent(this, PembelianActivity.class);
                break;
            default:
                return;
        }
        intent.putExtra("input_user", input);
        startActivity(intent);
    }
}