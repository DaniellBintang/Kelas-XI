package com.example.recyclercardview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclercardview.adapter.PersonAdapter;
import com.example.recyclercardview.model.Person;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PersonAdapter.OnPersonClickListener {

    // UI Components
    private EditText editTextName, editTextAddress;
    private Button buttonAdd, buttonClear;
    private RecyclerView recyclerViewPersons;
    private TextView textViewCount;
    private LinearLayout layoutEmptyState;

    // Data and Adapter
    private List<Person> personList;
    private PersonAdapter personAdapter;
    private int nextId = 1;

    // Date formatter
    private SimpleDateFormat dateFormat;

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

        // Initialize components
        initializeViews();
        initializeData();
        setupRecyclerView();
        setupClickListeners();

        // Add sample data
        addSampleData();

        // Update UI
        updateUI();
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonClear = findViewById(R.id.buttonClear);
        recyclerViewPersons = findViewById(R.id.recyclerViewPersons);
        textViewCount = findViewById(R.id.textViewCount);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Initialize date formatter
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    private void initializeData() {
        // Initialize person list
        personList = new ArrayList<>();

        // Create adapter
        personAdapter = new PersonAdapter(personList);
        personAdapter.setOnPersonClickListener(this);
    }

    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPersons.setLayoutManager(layoutManager);

        // Set adapter
        recyclerViewPersons.setAdapter(personAdapter);

        // Optional: Add item animator
        recyclerViewPersons.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    private void setupClickListeners() {
        // Add button click
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });

        // Clear button click
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputs();
            }
        });
    }

    private void addSampleData() {
        // Add some sample data
        String currentDate = dateFormat.format(new Date());

        personList.add(new Person(nextId++, "DaniellBintang", "Jakarta, Indonesia", currentDate));
        personList.add(new Person(nextId++, "John Doe", "New York, USA", currentDate));
        personList.add(new Person(nextId++, "Jane Smith", "London, UK", currentDate));

        personAdapter.notifyDataSetChanged();
    }

    private void addPerson() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            editTextName.setError("Nama tidak boleh kosong");
            editTextName.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Alamat tidak boleh kosong");
            editTextAddress.requestFocus();
            return;
        }

        // Create new person
        String currentDate = dateFormat.format(new Date());
        Person newPerson = new Person(nextId++, name, address, currentDate);

        // Add to list
        personList.add(newPerson);
        personAdapter.notifyItemInserted(personList.size() - 1);

        // Clear inputs
        clearInputs();

        // Update UI
        updateUI();

        // Show success message
        showToast("✅ Kontak berhasil ditambahkan: " + name);

        // Scroll to bottom
        recyclerViewPersons.smoothScrollToPosition(personList.size() - 1);
    }

    private void clearInputs() {
        editTextName.setText("");
        editTextAddress.setText("");
        editTextName.clearFocus();
        editTextAddress.clearFocus();
    }

    private void updateUI() {
        // Update count
        textViewCount.setText(String.valueOf(personList.size()));

        // Show/hide empty state
        if (personList.isEmpty()) {
            recyclerViewPersons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPersons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // PersonAdapter.OnPersonClickListener implementation
    @Override
    public void onEditClick(Person person, int position) {
        showEditDialog(person, position);
    }

    @Override
    public void onDeleteClick(Person person, int position) {
        showDeleteDialog(person, position);
    }

    @Override
    public void onItemClick(Person person, int position) {
        showPersonDetails(person);
    }

    private void showEditDialog(Person person, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Kontak");

        // Create layout for dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name input
        EditText nameInput = new EditText(this);
        nameInput.setHint("Nama lengkap");
        nameInput.setText(person.getName());
        layout.addView(nameInput);

        // Address input
        EditText addressInput = new EditText(this);
        addressInput.setHint("Alamat lengkap");
        addressInput.setText(person.getAddress());
        layout.addView(addressInput);

        builder.setView(layout);

        builder.setPositiveButton("💾 Simpan", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newAddress = addressInput.getText().toString().trim();

            if (!newName.isEmpty() && !newAddress.isEmpty()) {
                person.setName(newName);
                person.setAddress(newAddress);
                person.setDateAdded(dateFormat.format(new Date()));

                personAdapter.notifyItemChanged(position);
                showToast("✅ Kontak berhasil diupdate");
            } else {
                showToast("❌ Nama dan alamat tidak boleh kosong");
            }
        });

        builder.setNegativeButton("❌ Batal", null);
        builder.show();
    }

    private void showDeleteDialog(Person person, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🗑️ Hapus Kontak");
        builder.setMessage("Apakah Anda yakin ingin menghapus kontak \"" + person.getName() + "\"?");

        builder.setPositiveButton("🗑️ Hapus", (dialog, which) -> {
            personList.remove(position);
            personAdapter.notifyItemRemoved(position);
            personAdapter.notifyItemRangeChanged(position, personList.size());

            updateUI();
            showToast("🗑️ Kontak berhasil dihapus: " + person.getName());
        });

        builder.setNegativeButton("❌ Batal", null);
        builder.show();
    }

    private void showPersonDetails(Person person) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👤 Detail Kontak");

        String details = "ID: #" + String.format("%03d", person.getId()) + "\n" +
                "Nama: " + person.getName() + "\n" +
                "Alamat: " + person.getAddress() + "\n" +
                "Ditambahkan: " + person.getDateAdded();

        builder.setMessage(details);
        builder.setPositiveButton("✅ OK", null);
        builder.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Method untuk mendapatkan semua data (untuk keperluan lain)
    public List<Person> getAllPersons() {
        return new ArrayList<>(personList);
    }

    // Method untuk search (optional - bisa dikembangkan)
    public void searchPerson(String query) {
        List<Person> filteredList = new ArrayList<>();
        for (Person person : personList) {
            if (person.getName().toLowerCase().contains(query.toLowerCase()) ||
                    person.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(person);
            }
        }
        personAdapter.updateData(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}