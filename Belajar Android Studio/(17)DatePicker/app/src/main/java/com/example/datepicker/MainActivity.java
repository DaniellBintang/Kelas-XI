package com.example.datepicker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Deklarasi variabel untuk komponen UI
    private EditText editTextDate;
    private TextView textViewSelectedDate, textViewDateInfo;
    private Button buttonToday, buttonClear;

    // Calendar untuk menyimpan tanggal yang dipilih
    private Calendar selectedCalendar;
    private Calendar currentCalendar;

    // Format tanggal
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;
    private SimpleDateFormat fullDateFormat;

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

        // Inisialisasi komponen dan setup
        initializeViews();
        initializeDateFormats();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextDate = findViewById(R.id.editTextDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        textViewDateInfo = findViewById(R.id.textViewDateInfo);
        buttonToday = findViewById(R.id.buttonToday);
        buttonClear = findViewById(R.id.buttonClear);

        // Inisialisasi Calendar
        selectedCalendar = Calendar.getInstance();
        currentCalendar = Calendar.getInstance();
    }

    private void initializeDateFormats() {
        // Format tanggal untuk berbagai keperluan
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dayFormat = new SimpleDateFormat("EEEE", new Locale("id", "ID"));
        fullDateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
    }

    private void setupClickListeners() {
        // Event listener untuk EditText - membuka DatePicker Dialog
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Event listener untuk tombol Today
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTodayDate();
            }
        });

        // Event listener untuk tombol Clear
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDate();
            }
        });
    }

    private void showDatePickerDialog() {
        // Ambil tanggal saat ini untuk default DatePicker
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        // Buat DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Update calendar dengan tanggal yang dipilih
                        selectedCalendar.set(Calendar.YEAR, selectedYear);
                        selectedCalendar.set(Calendar.MONTH, selectedMonth);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        // Update tampilan
                        updateDateDisplay();
                    }
                },
                year, month, day
        );

        // Customisasi DatePickerDialog
        datePickerDialog.setTitle("📅 Pilih Tanggal");

        // Optional: Set minimum dan maximum date
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Tidak bisa pilih tanggal masa lalu
        // datePickerDialog.getDatePicker().setMaxDate(maxDate); // Set tanggal maksimum

        // Tampilkan dialog
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        // Format tanggal untuk EditText
        String formattedDate = dateFormat.format(selectedCalendar.getTime());
        editTextDate.setText(formattedDate);

        // Format tanggal untuk display hasil
        String displayDate = fullDateFormat.format(selectedCalendar.getTime());
        textViewSelectedDate.setText(displayDate);

        // Informasi tambahan tentang tanggal
        String dayName = dayFormat.format(selectedCalendar.getTime());
        String dateInfo = "Hari: " + dayName + " • " + calculateDaysDifference();
        textViewDateInfo.setText(dateInfo);

        // Toast konfirmasi
        showToast("📅 Tanggal berhasil dipilih: " + formattedDate);
    }

    private String calculateDaysDifference() {
        // Hitung selisih hari dari hari ini
        long selectedTimeMillis = selectedCalendar.getTimeInMillis();
        long currentTimeMillis = currentCalendar.getTimeInMillis();

        long diffInMillis = selectedTimeMillis - currentTimeMillis;
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays == 0) {
            return "Hari ini";
        } else if (diffInDays == 1) {
            return "Besok";
        } else if (diffInDays == -1) {
            return "Kemarin";
        } else if (diffInDays > 1) {
            return diffInDays + " hari lagi";
        } else {
            return Math.abs(diffInDays) + " hari yang lalu";
        }
    }

    private void setTodayDate() {
        // Set tanggal hari ini
        selectedCalendar = Calendar.getInstance();
        updateDateDisplay();
        showToast("📅 Tanggal diset ke hari ini");
    }

    private void clearDate() {
        // Reset semua tampilan
        editTextDate.setText("");
        textViewSelectedDate.setText("Belum ada tanggal yang dipilih");
        textViewDateInfo.setText("");

        // Reset calendar ke hari ini (untuk default DatePicker selanjutnya)
        selectedCalendar = Calendar.getInstance();

        showToast("🗑️ Tanggal berhasil di-reset");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Method untuk mendapatkan tanggal yang dipilih (untuk keperluan lain)
    public String getSelectedDate() {
        if (editTextDate.getText().toString().isEmpty()) {
            return null;
        }
        return dateFormat.format(selectedCalendar.getTime());
    }

    // Method untuk mendapatkan Calendar yang dipilih (untuk keperluan lain)
    public Calendar getSelectedCalendar() {
        return selectedCalendar;
    }
}