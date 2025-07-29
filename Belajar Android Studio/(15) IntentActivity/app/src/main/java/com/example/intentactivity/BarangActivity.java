package com.example.intentactivity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BarangActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barang);

        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewResult = findViewById(R.id.textViewResult);

        textViewTitle.setText("Barang");
        String input = getIntent().getStringExtra("input_user");
        textViewResult.setText("Input: " + input);
    }
}