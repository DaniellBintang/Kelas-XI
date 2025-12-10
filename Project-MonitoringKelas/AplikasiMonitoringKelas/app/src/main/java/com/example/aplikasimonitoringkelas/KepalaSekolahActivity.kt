package com.example.aplikasimonitoringkelas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplikasimonitoringkelas.ui.theme.AplikasiMonitoringKelasTheme
import com.example.aplikasimonitoringkelas.data.repository.KehadiranRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import com.example.aplikasimonitoringkelas.MainActivity

// ✅ UPDATE: Tambahkan Context dan onLogout ke KepalaSekolahActivity
class KepalaSekolahActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Receive user data from Intent
        val userId = intent.getIntExtra("USER_ID", 0)
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: ""
        val authToken = intent.getStringExtra("AUTH_TOKEN")

        setContent {
            AplikasiMonitoringKelasTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        KepalaSekolahTopBar(
                            userName = userName,
                            userRole = userRole,
                            onLogout = {
                                // ✅ TAMBAHKAN: Handle logout seperti di Kurikulum
                                val sharedPreferences = getSharedPreferences("MonitoringKelasPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    clear()
                                    apply()
                                }

                                val intent = Intent(this@KepalaSekolahActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("FROM_LOGOUT", true)

                                startActivity(intent)
                                finish()

                                Toast.makeText(
                                    this@KepalaSekolahActivity,
                                    "Anda telah logout",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                ) { innerPadding ->
                    // ✅ LANGSUNG tampilkan halaman Kelas Kosong
                    KelasKosongPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// ✅ UPDATE: TopBar dengan Logout Dialog dan Button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KepalaSekolahTopBar(
    userName: String,
    userRole: String,
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ✅ TAMBAHKAN: Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Konfirmasi Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin keluar?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = userRole.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    TopAppBar(
        title = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Kepala Sekolah",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = userName,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            // ✅ TAMBAHKAN: User Info Badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Role Icon",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userRole.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp
                    )
                }
            }

            // ✅ TAMBAHKAN: Logout Button
            IconButton(
                onClick = { showLogoutDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

// 🚨 MENU KELAS KOSONG (READ-ONLY untuk Kepala Sekolah)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasKosongPage(modifier: Modifier = Modifier) {
    // ✅ UBAH: State untuk tanggal, bukan hari
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // ✅ TAMBAHKAN: State untuk filter status
    var selectedStatus by remember { mutableStateOf("Kelas Kosong") }
    var expandedStatus by remember { mutableStateOf(false) }

    // API state
    val kelasKosongList = remember { mutableStateListOf<com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongItem>() }
    var jumlahKelasKosong by remember { mutableIntStateOf(0) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Repository and coroutine scope
    val kehadiranRepository = remember { KehadiranRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Format tanggal untuk tampilan (Indonesia)
    val displayDateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }
    val apiDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // ✅ Get display date dari selectedDate
    val displayDate = remember(selectedDate) {
        try {
            val date = apiDateFormat.parse(selectedDate)
            displayDateFormat.format(date ?: Date())
        } catch (e: Exception) {
            selectedDate
        }
    }

    // Get current day name
    val currentDayName = remember(selectedDate) {
        try {
            val date = apiDateFormat.parse(selectedDate)
            SimpleDateFormat("EEEE", Locale("id", "ID")).format(date ?: Date())
        } catch (e: Exception) {
            "Hari Ini"
        }
    }

    // ✅ OPSI filter status
    val statusOptions = listOf(
        "Kelas Kosong",
        "Semua Status", 
        "Hadir", 
        "Telat", 
        "Tidak Hadir", 
        "Izin",
        "Guru Pengganti"
    )

    // ✅ UBAH: Fetch berdasarkan tanggal DAN status
    LaunchedEffect(selectedDate, selectedStatus) {
        isFetching = true
        errorMessage = null

        scope.launch {
            // ✅ Tentukan status untuk backend
            val backendStatus = when (selectedStatus) {
                "Kelas Kosong" -> null // Default: kelas bermasalah saja
                "Semua Status" -> "Hadir" // Kirim "Hadir" agar backend return semua data
                "Guru Pengganti" -> "Hadir" // Filter di client
                else -> selectedStatus
            }
            
            kehadiranRepository.getKelasKosongKehadiran(
                tanggal = selectedDate,
                kelas = null,
                status = backendStatus
            ).onSuccess { response ->
                // ✅ Filter data berdasarkan selectedStatus
                val filteredData = when (selectedStatus) {
                    "Kelas Kosong" -> response.data.filter { 
                        it.namaGuruPengganti.isNullOrBlank() 
                    }
                    "Semua Status" -> response.data
                    "Hadir" -> response.data.filter { it.status == "Hadir" }
                    "Telat" -> response.data.filter { it.status == "Telat" }
                    "Tidak Hadir" -> response.data.filter { it.status == "Tidak Hadir" }
                    "Izin" -> response.data.filter { it.status == "Izin" }
                    "Guru Pengganti" -> response.data.filter { 
                        !it.namaGuruPengganti.isNullOrBlank() 
                    }
                    else -> response.data
                }
                
                kelasKosongList.clear()
                kelasKosongList.addAll(filteredData)
                jumlahKelasKosong = filteredData.size
                successMessage = response.message
                isFetching = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Gagal memuat data kelas kosong"
                isFetching = false

                snackbarHostState.showSnackbar(
                    message = "✗ ${error.message}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Refresh function
    fun refreshKelasKosong() {
        isFetching = true
        errorMessage = null

        scope.launch {
            val backendStatus = when (selectedStatus) {
                "Kelas Kosong" -> null
                "Semua Status" -> "Hadir"
                "Guru Pengganti" -> "Hadir"
                else -> selectedStatus
            }
            
            kehadiranRepository.getKelasKosongKehadiran(
                tanggal = selectedDate,
                kelas = null,
                status = backendStatus
            ).onSuccess { response ->
                val filteredData = when (selectedStatus) {
                    "Kelas Kosong" -> response.data.filter { 
                        it.namaGuruPengganti.isNullOrBlank() 
                    }
                    "Semua Status" -> response.data
                    "Hadir" -> response.data.filter { it.status == "Hadir" }
                    "Telat" -> response.data.filter { it.status == "Telat" }
                    "Tidak Hadir" -> response.data.filter { it.status == "Tidak Hadir" }
                    "Izin" -> response.data.filter { it.status == "Izin" }
                    "Guru Pengganti" -> response.data.filter { 
                        !it.namaGuruPengganti.isNullOrBlank() 
                    }
                    else -> response.data
                }
                
                kelasKosongList.clear()
                kelasKosongList.addAll(filteredData)
                jumlahKelasKosong = filteredData.size
                successMessage = response.message
                isFetching = false

                snackbarHostState.showSnackbar(
                    message = "✓ Data berhasil dimuat: $jumlahKelasKosong data",
                    duration = SnackbarDuration.Short
                )
            }.onFailure { error ->
                errorMessage = error.message ?: "Gagal memuat data kelas kosong"
                isFetching = false

                snackbarHostState.showSnackbar(
                    message = "✗ ${error.message}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // ✅ DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                apiDateFormat.parse(selectedDate)?.time
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = apiDateFormat.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Pilih Tanggal",
                        modifier = Modifier.padding(16.dp)
                    )
                },
                headline = {
                    Text(
                        text = datePickerState.selectedDateMillis?.let { millis ->
                            displayDateFormat.format(Date(millis))
                        } ?: "Pilih tanggal",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Alert
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            selectedStatus == "Hadir" -> MaterialTheme.colorScheme.tertiaryContainer
                            jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                selectedStatus == "Hadir" -> Icons.Default.CheckCircle
                                jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Alert Icon",
                            modifier = Modifier.size(32.dp),
                            tint = when {
                                selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedStatus) {
                                    "Hadir" -> "Monitoring Guru Hadir (Read Only)"
                                    "Kelas Kosong" -> "Monitoring Kelas Kosong (Read Only)"
                                    else -> "Monitoring Kehadiran Guru (Read Only)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isFetching)
                                    "⏳ Memuat data..."
                                else when (selectedStatus) {
                                    "Kelas Kosong" -> if (jumlahKelasKosong > 0)
                                        "⚠️ Jumlah kelas kosong: $jumlahKelasKosong kelas"
                                    else
                                        "✅ Tidak ada kelas kosong pada tanggal ini"
                                    "Hadir" -> "✓ Ditemukan $jumlahKelasKosong guru hadir"
                                    "Semua Status" -> "📊 Total data: $jumlahKelasKosong"
                                    else -> "📋 Ditemukan $jumlahKelasKosong data ($selectedStatus)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                            Text(
                                text = "📋 Hanya bisa melihat data. Koordinasi dengan Kurikulum untuk assign guru pengganti.",
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { refreshKelasKosong() },
                            enabled = !isFetching
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = when {
                                        selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                        jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = "Refresh",
                                    tint = when {
                                        selectedStatus == "Hadir" -> MaterialTheme.colorScheme.onTertiaryContainer
                                        jumlahKelasKosong > 0 && selectedStatus == "Kelas Kosong" -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Error Message Card
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { errorMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ✅ FILTER TANGGAL DAN STATUS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter Pencarian",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Total: $jumlahKelasKosong data",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (jumlahKelasKosong > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ✅ OutlinedTextField untuk DatePicker
                        OutlinedTextField(
                            value = displayDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isFetching,
                            label = { Text("Pilih Tanggal") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date Icon"
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    enabled = !isFetching
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Open Calendar"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableWithoutRipple { showDatePicker = true },
                            shape = RoundedCornerShape(8.dp)
                        )

                        // ✅ Quick Date Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Today Button
                            OutlinedButton(
                                onClick = {
                                    selectedDate = apiDateFormat.format(Date())
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isFetching,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedDate == apiDateFormat.format(Date()))
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("Hari Ini", style = MaterialTheme.typography.labelSmall)
                            }

                            // Yesterday Button
                            OutlinedButton(
                                onClick = {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                                    selectedDate = apiDateFormat.format(calendar.time)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isFetching
                            ) {
                                Text("Kemarin", style = MaterialTheme.typography.labelSmall)
                            }

                            // Tomorrow Button
                            OutlinedButton(
                                onClick = {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                    selectedDate = apiDateFormat.format(calendar.time)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isFetching
                            ) {
                                Text("Besok", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        
                        // ✅ TAMBAHKAN: Filter Status Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus }
                        ) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isFetching,
                                label = { Text("Filter Status") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(selectedStatus) {
                                            "Hadir" -> Icons.Default.CheckCircle
                                            "Kelas Kosong", "Tidak Hadir", "Izin", "Telat" -> Icons.Default.Warning
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = "Status Icon",
                                        tint = when(selectedStatus) {
                                            "Hadir" -> MaterialTheme.colorScheme.tertiary
                                            "Telat" -> MaterialTheme.colorScheme.secondary
                                            "Tidak Hadir", "Izin" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                statusOptions.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when(status) {
                                                        "Hadir" -> Icons.Default.CheckCircle
                                                        "Kelas Kosong", "Tidak Hadir", "Izin", "Telat" -> Icons.Default.Warning
                                                        else -> Icons.Default.Info
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = when(status) {
                                                        "Hadir" -> MaterialTheme.colorScheme.tertiary
                                                        "Telat" -> MaterialTheme.colorScheme.secondary
                                                        "Tidak Hadir", "Izin" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                                Text(
                                                    text = status,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (status == selectedStatus) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedStatus = status
                                            expandedStatus = false
                                        },
                                        leadingIcon = if (status == selectedStatus) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        colors = MenuDefaults.itemColors(
                                            textColor = if (status == selectedStatus)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                        
                        // ✅ Info filter aktif
                        if (selectedStatus != "Kelas Kosong") {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Filter aktif: $selectedStatus",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Card - Instruksi untuk Kepala Sekolah (hanya untuk Kelas Kosong)
            if (jumlahKelasKosong > 0 && !isFetching && selectedStatus == "Kelas Kosong") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tindakan yang Diperlukan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Koordinasi dengan Waka Kurikulum untuk mencari guru pengganti\n" +
                                        "• Pastikan siswa tetap dalam pengawasan\n" +
                                        "• Monitor perkembangan penanganan di sistem Kurikulum\n" +
                                        "• Dokumentasikan semua tindakan yang diambil",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // List Header
            item {
                Text(
                    text = when (selectedStatus) {
                        "Hadir" -> "Daftar Guru Hadir - $displayDate"
                        "Kelas Kosong" -> "Detail Kelas Kosong - $displayDate"
                        "Semua Status" -> "Semua Data Kehadiran - $displayDate"
                        else -> "Data $selectedStatus - $displayDate"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Content States
            if (isFetching && kelasKosongList.isEmpty()) {
                // Loading state
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Memuat data kelas kosong...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (kelasKosongList.isNotEmpty()) {
                // List items
                itemsIndexed(kelasKosongList) { index, kelasKosong ->
                    KelasKosongCardReadOnly(
                        kelasKosong = kelasKosong,
                        nomor = index + 1
                    )
                }
            } else {
                // Empty state
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (selectedStatus) {
                                    "Hadir" -> Icons.Default.Person
                                    "Kelas Kosong" -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Info
                                },
                                contentDescription = "Empty Icon",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (selectedStatus) {
                                    "Hadir" -> "Tidak ada guru hadir"
                                    "Kelas Kosong" -> "✅ Semua kelas aman!"
                                    "Tidak Hadir" -> "Tidak ada guru tidak hadir"
                                    "Izin" -> "Tidak ada guru izin"
                                    "Telat" -> "Tidak ada guru telat"
                                    "Guru Pengganti" -> "Tidak ada guru pengganti"
                                    else -> "Tidak ada data"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedStatus) {
                                    "Kelas Kosong" -> "Tidak ada kelas kosong pada $displayDate"
                                    else -> "Tidak ada data untuk filter \"$selectedStatus\" pada $displayDate"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (selectedStatus == "Kelas Kosong") {
                                Text(
                                    text = "Semua jadwal pelajaran berjalan normal dan terkendali dengan baik",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar Host
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

// ✅ TAMBAHKAN: Modifier extension untuk clickable tanpa ripple
@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )
)

// Card Read-Only untuk Kelas Kosong
@Composable
fun KelasKosongCardReadOnly(
    kelasKosong: com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongItem,
    nomor: Int
) {
    // ✅ PERBAIKAN: Cek status guru
    val sudahAdaPengganti = !kelasKosong.namaGuruPengganti.isNullOrBlank()
    val guruHadir = kelasKosong.status == "Hadir"
    val isNormal = guruHadir || sudahAdaPengganti // Normal jika hadir atau sudah ada pengganti

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                guruHadir -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f) // Hijau untuk hadir
                sudahAdaPengganti -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) // Biru untuk ada pengganti
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) // Merah untuk bermasalah
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isNormal) {
            BorderStroke(2.dp, if (guruHadir) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = when {
                            guruHadir -> MaterialTheme.colorScheme.tertiary
                            sudahAdaPengganti -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isNormal) {
                                // ✅ Icon check untuk yang hadir atau sudah ada pengganti
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Normal Status",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                // Nomor untuk yang belum ada pengganti
                                Text(
                                    text = nomor.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = kelasKosong.mataPelajaran,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        kelasKosong.jadwal?.let { jadwal ->
                            Text(
                                text = "${jadwal.hari}, ${jadwal.jam} • ${jadwal.kelas}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ✅ BADGE STATUS
                Surface(
                    color = when {
                        guruHadir -> MaterialTheme.colorScheme.tertiary
                        sudahAdaPengganti -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            guruHadir -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "HADIR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            sudahAdaPengganti -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ADA PENGGANTI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {
                                Text(
                                    text = kelasKosong.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ PERBAIKAN: Konten berbeda berdasarkan status
            when {
                // KASUS 1: Guru Hadir (Tidak perlu warning)
                guruHadir -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Teacher",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Guru Mengajar:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${kelasKosong.kodeGuru} - ${kelasKosong.namaGuru}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Info kehadiran
                    if (!kelasKosong.jamMasuk.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "✓ Guru telah hadir",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Jam masuk: ${kelasKosong.jamMasuk}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // KASUS 2: Sudah Ada Guru Pengganti
                sudahAdaPengganti -> {
                    // Info Guru Asli (yang tidak hadir)
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Absent Teacher",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Guru Tidak Hadir:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${kelasKosong.kodeGuru} - ${kelasKosong.namaGuru}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Status: ${kelasKosong.status}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ INFO GURU PENGGANTI (HIGHLIGHT)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Replacement Teacher",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✅ Guru Pengganti:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${kelasKosong.kodeGuruPengganti} - ${kelasKosong.namaGuruPengganti}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!kelasKosong.keteranganPengganti.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = kelasKosong.keteranganPengganti!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                if (!kelasKosong.waktuAssignPengganti.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⏰ Ditugaskan: ${kelasKosong.waktuAssignPengganti}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                // KASUS 3: Belum Ada Guru Pengganti
                else -> {
                    // Info Guru Tidak Hadir
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Teacher",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Guru Tidak Hadir:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${kelasKosong.kodeGuru} - ${kelasKosong.namaGuru}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ ALERT: Belum Ada Pengganti
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ Belum ada guru pengganti. Segera koordinasi dengan Waka Kurikulum!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Keterangan (jika ada)
            if (!kelasKosong.keterangan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keterangan: ${kelasKosong.keterangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Ruangan
            kelasKosong.jadwal?.let { jadwal ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Room",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruangan: ${jadwal.ruangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}