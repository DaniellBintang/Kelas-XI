package com.example.messagedialog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button buttonShowToast, buttonShowAlert, buttonShowSimpleAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonShowToast = findViewById(R.id.buttonShowToast);
        buttonShowAlert = findViewById(R.id.buttonShowAlert);
        buttonShowSimpleAlert = findViewById(R.id.buttonShowSimpleAlert);

        // Show Toast
        buttonShowToast.setOnClickListener(v ->
                showToast("Ini adalah Toast sederhana!")
        );

        // Show Alert Dialog (Positive & Negative Button)
        buttonShowAlert.setOnClickListener(v ->
                showAlertDialog()
        );

        // Show Simple Alert Dialog (Hanya 1 tombol OK)
        buttonShowSimpleAlert.setOnClickListener(v ->
                showSimpleAlertDialog()
        );
    }

    // Fungsi untuk menampilkan Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Fungsi untuk menampilkan AlertDialog dengan Positive dan Negative Button
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert Dialog");
        builder.setMessage("Ini adalah contoh Alert Dialog. Apakah kamu suka fitur ini?");
        builder.setCancelable(false);

        builder.setPositiveButton("Ya", (dialog, id) -> {
            showToast("Kamu memilih 'Ya'");
            dialog.dismiss();
        });

        builder.setNegativeButton("Tidak", (dialog, id) -> {
            showToast("Kamu memilih 'Tidak'");
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Fungsi untuk menampilkan AlertDialog biasa (satu tombol OK)
    private void showSimpleAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert Biasa");
        builder.setMessage("Ini adalah alert biasa dengan satu tombol OK saja.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }
}