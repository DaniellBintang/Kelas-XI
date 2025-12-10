package com.example.aplikasimonitoringkelas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.unit.sp
import com.example.aplikasimonitoringkelas.data.remote.request.KehadiranRequest
import com.example.aplikasimonitoringkelas.data.remote.request.LaporanGuruRequest
import com.example.aplikasimonitoringkelas.data.remote.request.TugasRequest
import com.example.aplikasimonitoringkelas.data.remote.response.GuruItem
import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.repository.GuruRepository
import com.example.aplikasimonitoringkelas.data.repository.JadwalRepository
import com.example.aplikasimonitoringkelas.data.repository.KehadiranRepository
import com.example.aplikasimonitoringkelas.data.repository.TugasRepository
import com.example.aplikasimonitoringkelas.ui.theme.AplikasiMonitoringKelasTheme
import com.example.aplikasimonitoringkelas.data.remote.response.KehadiranItem
import com.example.aplikasimonitoringkelas.data.remote.response.TugasItem
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Canvas // ✅ TAMBAHKAN INI
import androidx.compose.ui.geometry.Offset // ✅ TAMBAHKAN INI
import androidx.compose.ui.graphics.Color // ✅ TAMBAHKAN INI
import android.content.Context

class SiswaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Receive user data from Intent
        val userId = intent.getIntExtra("USER_ID", 0)
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: ""
        val userKelas = intent.getStringExtra("USER_KELAS") ?: "XI RPL 1" // ✅ DEFAULT FALLBACK
        val authToken = intent.getStringExtra("AUTH_TOKEN")

        // ✅ TAMBAHKAN LOG untuk debugging
        if (AppConfig.isDebugMode()) {
            android.util.Log.d("SiswaActivity", "=== RECEIVED INTENT EXTRAS ===")
            android.util.Log.d("SiswaActivity", "  USER_ID: $userId")
            android.util.Log.d("SiswaActivity", "  USER_NAME: $userName")
            android.util.Log.d("SiswaActivity", "  USER_EMAIL: $userEmail")
            android.util.Log.d("SiswaActivity", "  USER_ROLE: $userRole")
            android.util.Log.d("SiswaActivity", "  USER_KELAS: $userKelas") // ✅ CEK INI
            android.util.Log.d("SiswaActivity", "  AUTH_TOKEN: ${authToken?.take(10)}...")
        }

        setContent {
            AplikasiMonitoringKelasTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SiswaTopBar(
                            userName = userName,
                            userRole = userRole,
                            onLogout = {
                                // ✅ PERBAIKAN: Clear SharedPreferences SEBELUM navigate
                                val sharedPreferences = getSharedPreferences("MonitoringKelasPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    clear()
                                    apply() // ← Pastikan apply() selesai
                                }

                                // Navigate ke MainActivity dengan flag clear
                                val intent = Intent(this@SiswaActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                                // ✅ TAMBAHAN: Kirim extra untuk menandakan logout
                                intent.putExtra("FROM_LOGOUT", true)

                                startActivity(intent)
                                finish()

                                Toast.makeText(
                                    this@SiswaActivity,
                                    "Anda telah logout",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                ) { innerPadding ->
                    SiswaScreen(
                        modifier = Modifier.padding(innerPadding),
                        userId = userId,
                        userName = userName,
                        userEmail = userEmail,
                        userRole = userRole,
                        authToken = authToken,
                        userKelas = userKelas
                    )
                }
            }
        }
    }
}

// ✅ PERBAIKAN 1: TopBar - Kembalikan ke default (biarkan menempel dengan status bar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiswaTopBar(
    userName: String,
    userRole: String,
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout Confirmation Dialog (tidak berubah)
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
                                    text = userRole,
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
                    text = "Dashboard Siswa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        },
        actions = {
            // User Info Badge
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

            // Logout Button
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
        // ✅ HAPUS windowInsets - Biarkan default Material 3
    )
}

// ✅ PERBAIKAN 2: SiswaScreen - HAPUS Scaffold dalam, pakai Column langsung
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiswaScreen(
    modifier: Modifier = Modifier,
    userId: Int = -1,
    userName: String = "Siswa",
    userEmail: String = "",
    userRole: String = "siswa",
    userKelas: String = "XI RPL", // ✅ Parameter sudah ada
    authToken: String? = null
){
    var selectedItem by remember { mutableIntStateOf(0) }

    // ✅ UPDATED: Hapus menu List, hanya 2 menu
    val items = listOf(
        BottomNavigationItem("Jadwal Pelajaran", Icons.Default.Home),
        BottomNavigationItem("Entri", Icons.Default.Edit)
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedItem) {
                0 -> JadwalPelajaranSiswaPage(
                    userName = userName,
                    userKelas = userKelas
                )
                1 -> EntriSiswaPage(
                    userId = userId,
                    userName = userName,
                    userKelas = userKelas
                )
            }
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class EntriKehadiran(
    val tanggal: String,
    val mataPelajaran: String,
    val status: String,
    val keterangan: String
)

data class EntriTugas(
    val tanggal: String,
    val mataPelajaran: String,
    val judulTugas: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalPelajaranSiswaPage(
    modifier: Modifier = Modifier,
    userName: String = "Siswa",
    userKelas: String = "XI RPL 1" // ✅ Default dengan angka kelas
) {
    val repository = remember { JadwalRepository() }
    val coroutineScope = rememberCoroutineScope()

    // ✅ PERBAIKAN: Auto-select hari sesuai system
    val hariIni = remember {
        SimpleDateFormat("EEEE", Locale("id", "ID"))
            .format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    // Filter states - Auto-set hari dari system
    var selectedHari by remember { mutableStateOf(hariIni) } // ✅ DEFAULT = Hari ini
    // ✅ PERBAIKAN: Gunakan remember untuk kelas, BUKAN by delegate
    val selectedKelas = remember(userKelas) { userKelas } // ← FIX: Langsung ambil dari parameter
    var expandedHari by remember { mutableStateOf(false) }

    val hariList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    // API states
    var jadwalList by remember { mutableStateOf<List<JadwalItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ TAMBAHKAN LOG untuk debugging
    LaunchedEffect(Unit) {
        android.util.Log.d("JadwalPelajaranSiswa", "=== PARAMS CHECK ===")
        android.util.Log.d("JadwalPelajaranSiswa", "  userName: $userName")
        android.util.Log.d("JadwalPelajaranSiswa", "  userKelas: $userKelas") // ← Should be "XI RPL 1"
        android.util.Log.d("JadwalPelajaranSiswa", "  selectedKelas: $selectedKelas") // ← Should match
        android.util.Log.d("JadwalPelajaranSiswa", "  hariIni: $hariIni")
    }

    // Fetch data when hari changes
    LaunchedEffect(selectedHari, selectedKelas) {
        isLoading = true
        errorMessage = null

        // ✅ TAMBAHKAN LOG sebelum fetch
        android.util.Log.d("JadwalPelajaranSiswa", "=== FETCHING JADWAL ===")
        android.util.Log.d("JadwalPelajaranSiswa", "  hari: $selectedHari")
        android.util.Log.d("JadwalPelajaranSiswa", "  kelas: $selectedKelas")

        coroutineScope.launch {
            // ✅ PERBAIKAN: Gunakan endpoint dengan status guru
            repository.getJadwalDenganStatusGuru(hari = selectedHari, kelas = selectedKelas)
                .onSuccess { data ->
                    jadwalList = data
                    isLoading = false
                    
                    // Log statistik guru izin
                    val guruIzinCount = data.count { it.statusGuru == "izin" }
                    android.util.Log.d("JadwalPelajaranSiswa", "✓ Loaded ${data.size} jadwal, $guruIzinCount guru izin")
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header dengan info siswa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Student Icon",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Jadwal Pelajaran",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$userName • $selectedKelas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    // ✅ TAMBAHKAN: Info hari ini
                    if (selectedHari == hariIni) {
                        Text(
                            text = "📅 Jadwal Hari Ini ($hariIni)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Filter Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Hari",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // ✅ TAMBAHKAN: Quick button untuk kembali ke hari ini
            if (selectedHari != hariIni) {
                TextButton(
                    onClick = { selectedHari = hariIni },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Today",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hari Ini", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Hari
        ExposedDropdownMenuBox(
            expanded = expandedHari,
            onExpandedChange = { expandedHari = !expandedHari && !isLoading },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = selectedHari,
                onValueChange = {},
                readOnly = true,
                enabled = !isLoading,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHari)
                },
                label = { Text("Hari") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                // ✅ TAMBAHKAN: Indicator jika hari ini
                leadingIcon = if (selectedHari == hariIni) {
                    {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Today",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (selectedHari == hariIni)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.primary
                )
            )
            ExposedDropdownMenu(
                expanded = expandedHari,
                onDismissRequest = { expandedHari = false }
            ) {
                hariList.forEach { hari ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = hari,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (hari == hariIni)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal,
                                    color = if (hari == hariIni)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )

                                // ✅ TAMBAHKAN: Badge "Hari Ini"
                                if (hari == hariIni) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Hari Ini",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            selectedHari = hari
                            expandedHari = false
                        }
                    )
                }
            }
        }

        // Loading State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat jadwal $selectedHari...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error State
        else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Icon",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gagal memuat jadwal",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Success State - Display Jadwal
        else if (jadwalList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jadwalList) { jadwal ->
                    JadwalCard(jadwalItem = jadwal)
                }
            }
        }

        // Empty State
        else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Empty Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada jadwal",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Jadwal untuk $selectedHari kelas $selectedKelas\nbelum tersedia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun JadwalCard(jadwalItem: JadwalItem) {
    // ✅ UPDATED: Cek apakah guru sedang izin ATAU tidak hadir
    val isGuruIzin = jadwalItem.statusGuru == "izin"
    val isGuruTidakHadir = jadwalItem.statusGuru == "tidak_hadir"
    val isGuruBermasalah = isGuruIzin || isGuruTidakHadir
    val adaPengganti = jadwalItem.guruPengganti != null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            // ✅ UPDATED: Warna berbeda jika guru izin atau tidak hadir
            containerColor = when {
                isGuruBermasalah && !adaPengganti -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isGuruBermasalah && adaPengganti -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        // ✅ UPDATED: Border jika guru izin atau tidak hadir
        border = if (isGuruBermasalah) {
            BorderStroke(
                width = 2.dp,
                color = if (adaPengganti) 
                    MaterialTheme.colorScheme.tertiary 
                else 
                    MaterialTheme.colorScheme.error
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header dengan jam dan mata pelajaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = jadwalItem.jam,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ✅ UPDATED: Badge status guru (izin atau tidak hadir)
                if (isGuruBermasalah) {
                    Surface(
                        color = if (adaPengganti) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (adaPengganti) 
                                    Icons.Default.PersonAdd 
                                else 
                                    Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (adaPengganti) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    adaPengganti -> "Ada Pengganti"
                                    isGuruTidakHadir -> "Tidak Hadir"
                                    else -> "Guru Izin"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (adaPengganti) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time Icon",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mata Pelajaran
            Text(
                text = jadwalItem.mataPelajaran,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Info Guru dan Ruangan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Teacher Icon",
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                jadwalItem.namaGuru.isNullOrBlank() -> MaterialTheme.colorScheme.error
                                isGuruBermasalah -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            // ✅ UPDATED: Tampilkan status izin atau tidak hadir
                            if (isGuruBermasalah) {
                                Text(
                                    text = "${jadwalItem.namaGuru ?: "Guru"} (${if (isGuruTidakHadir) "Tidak Hadir" else "Izin"})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = jadwalItem.namaGuru ?: "Belum ada guru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (jadwalItem.namaGuru.isNullOrBlank())
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (jadwalItem.namaGuru.isNullOrBlank())
                                        FontWeight.Bold
                                    else
                                        FontWeight.Medium
                                )
                            }
                            Text(
                                text = jadwalItem.kodeGuru ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = jadwalItem.ruangan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // ✅ UPDATED: Info Guru Pengganti (jika ada) - untuk izin atau tidak hadir
            if (isGuruBermasalah && adaPengganti) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Guru Pengganti",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Guru Pengganti",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = jadwalItem.guruPengganti?.namaGuru ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (!jadwalItem.guruPengganti?.kodeGuru.isNullOrBlank()) {
                                Text(
                                    text = jadwalItem.guruPengganti?.kodeGuru ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
            
            // ✅ UPDATED: Warning jika guru izin/tidak hadir tanpa pengganti
            if (isGuruBermasalah && !adaPengganti) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isGuruTidakHadir) 
                                    "Guru tidak hadir" 
                                else 
                                    "Guru sedang izin",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Guru pengganti akan segera hadir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                            if (!jadwalItem.infoIzin?.keterangan.isNullOrBlank()) {
                                Text(
                                    text = jadwalItem.infoIzin?.keterangan ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✍️ MENU 2: ENTRI (Input Laporan Guru) - UPDATED
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriSiswaPage(
    modifier: Modifier = Modifier,
    userId: Int = -1,
    userName: String = "Siswa",
    userKelas: String = "XI RPL" // ✅ TAMBAHKAN parameter
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Entri Icon",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Entri Laporan Guru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$userName - Kelas: $userKelas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ LANGSUNG TAMPILKAN FORM LAPORAN GURU (tanpa tab)
        LaporanGuruForm(userKelas = userKelas)
    }
}

// UPDATED EntriTugasForm dengan Save Functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriTugasForm(userId: Int = -1) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { TugasRepository() }

    // Form states
    var selectedMataPelajaran by remember { mutableStateOf("Matematika") }
    var judulTugas by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Selesai") }
    var expandedMapel by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // API states
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val mataPelajaranList = listOf("Matematika", "Pemrograman Web", "Basis Data", "Bahasa Inggris", "Pemrograman Mobile", "Jaringan Komputer")
    val statusList = listOf("Selesai", "Belum Selesai", "Terlambat")

    // Get current date
    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Save tugas function
    fun saveTugas() {
        // Validasi input
        if (selectedMataPelajaran.isBlank()) {
            errorMessage = "Mata pelajaran harus dipilih"
            showErrorDialog = true
            return
        }

        if (judulTugas.isBlank()) {
            errorMessage = "Judul tugas tidak boleh kosong"
            showErrorDialog = true
            return
        }

        if (selectedStatus.isBlank()) {
            errorMessage = "Status tugas harus dipilih"
            showErrorDialog = true
            return
        }

        if (userId == -1) {
            errorMessage = "User ID tidak valid. Silakan login ulang."
            showErrorDialog = true
            return
        }

        // Check if using Dummy Mode
        if (AppConfig.isDummyMode()) {
            Toast.makeText(
                context,
                "Mode Dummy: Data tidak bisa disimpan. Aktifkan Real API di AppConfig.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Real API mode - save tugas
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val request = TugasRequest(
                    userId = userId,
                    tanggal = currentDate,
                    mataPelajaran = selectedMataPelajaran,
                    judulTugas = judulTugas,
                    status = selectedStatus
                )

                repository.createTugas(request)
                    .onSuccess { tugasItem ->
                        isLoading = false
                        showSuccessDialog = true

                        // Clear form after success
                        selectedMataPelajaran = "Matematika"
                        judulTugas = ""
                        selectedStatus = "Selesai"

                        Toast.makeText(
                            context,
                            "Tugas berhasil disimpan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onFailure { exception ->
                        isLoading = false
                        errorMessage = exception.message ?: "Gagal menyimpan tugas"
                        showErrorDialog = true
                    }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "Berhasil!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Tugas berhasil disimpan:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Mata Pelajaran: $selectedMataPelajaran", style = MaterialTheme.typography.bodyMedium)
                    Text("• Judul: $judulTugas", style = MaterialTheme.typography.bodyMedium)
                    Text("• Status: $selectedStatus", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        )
    }

    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Text(
                    text = "Gagal Menyimpan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage ?: "Terjadi kesalahan")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error Icon",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entri Status Tugas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = currentDate,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // User ID Info (only in debug mode)
            if (AppConfig.isDebugMode() && userId != -1) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "User ID: $userId",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Mata Pelajaran Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedMapel,
                onExpandedChange = { expandedMapel = !expandedMapel && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedMataPelajaran,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isLoading,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMapel) },
                    label = { Text("Mata Pelajaran") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Subject Icon"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedMapel,
                    onDismissRequest = { expandedMapel = false }
                ) {
                    mataPelajaranList.forEach { mapel ->
                        DropdownMenuItem(
                            text = { Text(mapel) },
                            onClick = {
                                selectedMataPelajaran = mapel
                                expandedMapel = false
                            }
                        )
                    }
                }
            }

            // Judul Tugas
            OutlinedTextField(
                value = judulTugas,
                onValueChange = {
                    if (it.length <= 100) {
                        judulTugas = it
                    }
                },
                enabled = !isLoading,
                label = { Text("Judul Tugas") },
                placeholder = { Text("Masukkan judul tugas...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Task Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                    Text(
                        text = "${judulTugas.length}/100",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                isError = judulTugas.isBlank()
            )

            // Status Tugas Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isLoading,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    label = { Text("Status Tugas") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status Icon"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when (selectedStatus) {
                            "Selesai" -> MaterialTheme.colorScheme.primary
                            "Belum Selesai" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusList.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                selectedStatus = status
                                expandedStatus = false
                            },
                            leadingIcon = {
                                Surface(
                                    color = when (status) {
                                        "Selesai" -> MaterialTheme.colorScheme.primaryContainer
                                        "Belum Selesai" -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.errorContainer
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }

            // Mode Info
            if (AppConfig.isDebugMode()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (AppConfig.isDummyMode())
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (AppConfig.isDummyMode())
                                Icons.Default.Error
                            else
                                Icons.Default.CheckCircle,
                            contentDescription = "Mode Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (AppConfig.isDummyMode())
                                "Mode Dummy: Data tidak akan tersimpan"
                            else
                                "Mode API: Data akan disimpan ke server",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Submit Button with Loading State
            Button(
                onClick = { saveTugas() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = selectedMataPelajaran.isNotBlank() &&
                        judulTugas.isNotBlank() &&
                        selectedStatus.isNotBlank() &&
                        !isLoading
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menyimpan...",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Save Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Tugas",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanGuruForm(
    userKelas: String = "XI RPL" // ✅ TAMBAHKAN parameter
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val guruRepository = remember { GuruRepository() }
    val kehadiranRepository = remember { KehadiranRepository() }

    // Form states
    var selectedGuru by remember { mutableStateOf<GuruItem?>(null) }
    var selectedStatus by remember { mutableStateOf("Telat") }
    var jamMasuk by remember { mutableStateOf("") }
    var jamKeluar by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var expandedGuru by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // Guru list
    var guruList by remember { mutableStateOf<List<GuruItem>>(emptyList()) }
    var isLoadingGuru by remember { mutableStateOf(false) }
    var errorLoadGuru by remember { mutableStateOf<String?>(null) }

    // API states
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ PERBAIKAN: State untuk menyimpan data yang akan ditampilkan di dialog
    var savedGuru by remember { mutableStateOf<GuruItem?>(null) }
    var savedStatus by remember { mutableStateOf("") }
    var savedJamMasuk by remember { mutableStateOf("") }
    var savedJamKeluar by remember { mutableStateOf("") }
    var savedKeterangan by remember { mutableStateOf("") }
    var savedTanggal by remember { mutableStateOf("") }
    var savedKelas by remember { mutableStateOf("") } // ✅ TAMBAHKAN STATE INI

    // ✅ UPDATED: Hapus "Izin" - siswa hanya bisa laporkan Hadir/Telat/Tidak Hadir
    val statusList = listOf("Hadir", "Telat", "Tidak Hadir")

    // Get current date dengan format yang benar
    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Fetch guru list
    LaunchedEffect(Unit) {
        isLoadingGuru = true
        errorLoadGuru = null

        coroutineScope.launch {
            guruRepository.getGuru(status = "Aktif")
                .onSuccess { data ->
                    guruList = data
                    isLoadingGuru = false

                    if (AppConfig.isDebugMode()) {
                        android.util.Log.d("LaporanGuruForm", "✓ Loaded ${data.size} guru")
                    }
                }
                .onFailure { exception ->
                    errorLoadGuru = exception.message
                    isLoadingGuru = false

                    Toast.makeText(
                        context,
                        "Gagal memuat data guru: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // ✅ PERBAIKAN: Validasi format jam
    fun isValidTimeFormat(time: String): Boolean {
        if (time.isBlank()) return true
        val timePattern = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$".toRegex()
        return time.matches(timePattern)
    }

    // Save laporan function
    fun saveLaporan() {
        // Validasi (tidak berubah)
        if (selectedGuru == null) {
            errorMessage = "Guru harus dipilih"
            showErrorDialog = true
            return
        }

        if (selectedStatus.isBlank()) {
            errorMessage = "Status harus dipilih"
            showErrorDialog = true
            return
        }

        if ((selectedStatus == "Hadir" || selectedStatus == "Telat") && jamMasuk.isBlank()) {
            errorMessage = "Jam masuk wajib diisi untuk status $selectedStatus"
            showErrorDialog = true
            return
        }

        if (jamMasuk.isNotBlank() && !isValidTimeFormat(jamMasuk)) {
            errorMessage = "Format jam masuk tidak valid. Gunakan format HH:mm (contoh: 07:30)"
            showErrorDialog = true
            return
        }

        if (jamKeluar.isNotBlank() && !isValidTimeFormat(jamKeluar)) {
            errorMessage = "Format jam keluar tidak valid. Gunakan format HH:mm (contoh: 14:00)"
            showErrorDialog = true
            return
        }

        if (AppConfig.isDummyMode()) {
            Toast.makeText(
                context,
                "Mode Dummy: Data tidak bisa disimpan. Aktifkan Real API di AppConfig.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // ✅ PERBAIKAN: Simpan data SEBELUM kirim ke API
        savedGuru = selectedGuru
        savedStatus = selectedStatus
        savedJamMasuk = jamMasuk
        savedJamKeluar = jamKeluar
        savedKeterangan = keterangan
        savedTanggal = currentDate
        // ✅ TAMBAHKAN kelas di saved state
        savedKelas = userKelas

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val cleanNamaGuru = selectedGuru!!.nama
                    .replace(", S.Pd.", "")
                    .replace(", M.Pd.", "")
                    .replace(", S.Kom.", "")
                    .replace(", M.T.", "")
                    .replace(", S.Si.", "")
                    .replace("Dr. ", "")
                    .trim()

                // ✅ PERBAIKAN AKHIR: TIDAK kirim jadwalId sama sekali
                // Backend akan auto-detect berdasarkan: tanggal→hari + kode_guru + mata_pelajaran + kelas
                val request = KehadiranRequest(
                    // jadwalId = null, ❌ HAPUS - biar tidak dikirim ke backend
                    tanggal = currentDate,
                    jamMasuk = when (selectedStatus) {
                        "Hadir", "Telat" -> jamMasuk.ifBlank { "07:00" }
                        else -> null
                    },
                    jamKeluar = when (selectedStatus) {
                        "Hadir", "Telat" -> jamKeluar.ifBlank { null }
                        else -> null
                    },
                    mataPelajaran = selectedGuru!!.mataPelajaran,
                    kelas = userKelas,
                    namaGuru = cleanNamaGuru,
                    kodeGuru = selectedGuru!!.kodeGuru,
                    status = selectedStatus,
                    keterangan = keterangan.trim().ifBlank {
                        when (selectedStatus) {
                            "Hadir" -> "Guru hadir tepat waktu"
                            "Telat" -> "Guru terlambat ${jamMasuk.ifBlank { "15" }} menit"
                            "Tidak Hadir" -> "Guru tidak hadir tanpa keterangan"
                            "Izin" -> "Guru berhalangan hadir"
                            else -> null
                        }
                    }
                )

                if (AppConfig.isDebugMode()) {
                    android.util.Log.d("LaporanGuruForm", "=== SENDING REQUEST ===")
                    android.util.Log.d("LaporanGuruForm", "  jadwal_id: ${request.jadwalId ?: "null (auto-detect)"}")
                    android.util.Log.d("LaporanGuruForm", "  tanggal: ${request.tanggal}")
                    android.util.Log.d("LaporanGuruForm", "  mata_pelajaran: ${request.mataPelajaran}")
                    android.util.Log.d("LaporanGuruForm", "  kelas: ${request.kelas}")
                    android.util.Log.d("LaporanGuruForm", "  nama_guru: ${request.namaGuru}")
                    android.util.Log.d("LaporanGuruForm", "  kode_guru: ${request.kodeGuru}")
                    android.util.Log.d("LaporanGuruForm", "  status: ${request.status}")
                    android.util.Log.d("LaporanGuruForm", "  keterangan: ${request.keterangan ?: "null"}")
                    android.util.Log.d("LaporanGuruForm", "  jam_masuk: ${request.jamMasuk ?: "null"}")
                    android.util.Log.d("LaporanGuruForm", "  jam_keluar: ${request.jamKeluar ?: "null"}")
                }

                kehadiranRepository.createKehadiran(request)
                    .onSuccess { createdItem ->
                        isLoading = false
                        showSuccessDialog = true

                        // Clear form
                        selectedGuru = null
                        selectedStatus = "Telat"
                        jamMasuk = ""
                        jamKeluar = ""
                        keterangan = ""

                        Toast.makeText(
                            context,
                            "✓ Laporan berhasil dikirim",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (AppConfig.isDebugMode()) {
                            android.util.Log.d("LaporanGuruForm", "✓ Laporan berhasil dikirim")
                            android.util.Log.d("LaporanGuruForm", "  Response ID: ${createdItem.id}")
                            android.util.Log.d("LaporanGuruForm", "  ✅ jadwal_id: ${createdItem.jadwalId ?: "NULL - MASALAH!"}")
                            android.util.Log.d("LaporanGuruForm", "  Has jadwal object: ${createdItem.jadwal != null}")
                            if (createdItem.jadwal != null) {
                                android.util.Log.d("LaporanGuruForm", "  Jadwal matched: ${createdItem.jadwal!!.hari} ${createdItem.jadwal!!.jam}")
                            }
                        }
                    }
                    .onFailure { exception ->
                        isLoading = false
                        errorMessage = exception.message

                        val errorMsg = exception.message ?: "Terjadi kesalahan"
                        showErrorDialog = true

                        Toast.makeText(
                            context,
                            "✗ $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()

                        if (AppConfig.isDebugMode()) {
                            android.util.Log.e("LaporanGuruForm", "✗ Error: ${exception.message}")
                        }
                    }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error: ${e.message}"
                showErrorDialog = true

                if (AppConfig.isDebugMode()) {
                    android.util.Log.e("LaporanGuruForm", "✗ Exception: ${e.message}", e)
                }
            }
        }
    }

    // ✅ PERBAIKAN: Success Dialog menggunakan saved state
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "✓ Laporan Terkirim!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Laporan kehadiran guru berhasil dikirim:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Nama Guru (menggunakan savedGuru)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guru: ${savedGuru?.nama ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Kode Guru
                    Row(
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
                            text = "Kode: ${savedGuru?.kodeGuru ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // ✅ TAMBAHKAN: Info Kelas
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kelas: $userKelas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Mata Pelajaran
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mapel: ${savedGuru?.mataPelajaran ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Status (menggunakan savedStatus)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (savedStatus) {
                                "Hadir" -> MaterialTheme.colorScheme.primary
                                "Telat" -> MaterialTheme.colorScheme.tertiary
                                "Izin" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: $savedStatus",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (savedStatus) {
                                "Hadir" -> MaterialTheme.colorScheme.primary
                                "Telat" -> MaterialTheme.colorScheme.tertiary
                                "Izin" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    // Tanggal (menggunakan savedTanggal)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanggal: $savedTanggal",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Jam Masuk (menggunakan savedJamMasuk)
                    if (savedJamMasuk.isNotBlank() && (savedStatus == "Hadir" || savedStatus == "Telat")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Jam Masuk: $savedJamMasuk",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Jam Keluar (menggunakan savedJamKeluar)
                    if (savedJamKeluar.isNotBlank() && (savedStatus == "Hadir" || savedStatus == "Telat")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Jam Keluar: $savedJamKeluar",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Keterangan (menggunakan savedKeterangan)
                    if (savedKeterangan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Keterangan:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = savedKeterangan,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OK, Mengerti")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Text(
                    text = "Gagal Mengirim Laporan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage ?: "Terjadi kesalahan")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error Icon",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    // UI Form
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Laporan Kehadiran Guru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Laporkan guru yang telat atau tidak hadir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = currentDate,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ✅ PERBAIKAN: Tambahkan info jumlah guru yang dimuat
            if (AppConfig.isDebugMode() && !isLoadingGuru && guruList.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "📊 ${guruList.size} guru tersedia",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // ✅ PERBAIKAN: Tampilkan error loading guru jika ada
            if (errorLoadGuru != null) {
                Card(
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
                        Column {
                            Text(
                                text = "Gagal memuat data guru",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = errorLoadGuru ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Pilih Guru Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedGuru,
                onExpandedChange = { expandedGuru = !expandedGuru && !isLoading && !isLoadingGuru && guruList.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = selectedGuru?.nama ?: "Pilih Guru",
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isLoading && !isLoadingGuru && guruList.isNotEmpty(),
                    trailingIcon = {
                        if (isLoadingGuru) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGuru)
                        }
                    },
                    label = {
                        Text(
                            if (isLoadingGuru) "Memuat guru..."
                            else if (guruList.isEmpty() && errorLoadGuru == null) "Tidak ada guru"
                            else "Nama Guru"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Guru Icon"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = selectedGuru == null && !isLoadingGuru,
                    supportingText = {
                        when {
                            isLoadingGuru -> Text("Sedang memuat data guru...")
                            errorLoadGuru != null -> Text(
                                "Error: $errorLoadGuru",
                                color = MaterialTheme.colorScheme.error
                            )
                            guruList.isEmpty() -> Text("Tidak ada guru aktif")
                            else -> Text("Pilih guru yang ingin dilaporkan")
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = expandedGuru,
                    onDismissRequest = { expandedGuru = false }
                ) {
                    if (guruList.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Tidak ada data guru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { }
                        )
                    } else {
                        guruList.forEach { guru ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = guru.nama,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${guru.mataPelajaran} • ${guru.kodeGuru}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedGuru = guru
                                    expandedGuru = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isLoading,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    label = { Text("Status Kehadiran") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status Icon"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when (selectedStatus) {
                            "Hadir" -> MaterialTheme.colorScheme.primary
                            "Telat" -> MaterialTheme.colorScheme.tertiary
                            "Izin" -> MaterialTheme.colorScheme.secondary
                            "Tidak Hadir" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusList.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when (status) {
                                            "Hadir" -> MaterialTheme.colorScheme.primaryContainer
                                            "Telat" -> MaterialTheme.colorScheme.tertiaryContainer
                                            "Izin" -> MaterialTheme.colorScheme.secondaryContainer
                                            "Tidak Hadir" -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedStatus = status
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            // Jam Masuk (untuk Hadir dan Telat)
            if (selectedStatus == "Hadir" || selectedStatus == "Telat") {
                OutlinedTextField(
                    value = jamMasuk,
                    onValueChange = { jamMasuk = it },
                    enabled = !isLoading,
                    label = { Text("Jam Masuk (Opsional)") },
                    placeholder = { Text("Contoh: 07:45") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time Icon"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Format: HH:mm (contoh: 07:45)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            // Jam Keluar (untuk Hadir dan Telat)
            if (selectedStatus == "Hadir" || selectedStatus == "Telat") {
                OutlinedTextField(
                    value = jamKeluar,
                    onValueChange = { jamKeluar = it },
                    enabled = !isLoading,
                    label = { Text("Jam Keluar (Opsional)") },
                    placeholder = { Text("Contoh: 08:30") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time Icon"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Format: HH:mm (contoh: 08:30)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            // Keterangan
            OutlinedTextField(
                value = keterangan,
                onValueChange = { if (it.length <= 200) keterangan = it },
                enabled = !isLoading,
                label = { Text("Keterangan") },
                placeholder = { Text("Jelaskan situasinya...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                    Text(
                        text = "${keterangan.length}/200",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )

            // Mode Info
            if (AppConfig.isDebugMode()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (AppConfig.isDummyMode())
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (AppConfig.isDummyMode())
                                Icons.Default.Error
                            else
                                Icons.Default.CheckCircle,
                            contentDescription = "Mode Icon",
                            modifier = Modifier.size(20.dp),
                            tint = if (AppConfig.isDummyMode())
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (AppConfig.isDummyMode())
                                "Mode: Dummy (data tidak tersimpan)"
                            else
                                "Mode: Real API",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (AppConfig.isDummyMode())
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = { saveLaporan() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = selectedGuru != null &&
                        selectedStatus.isNotBlank() &&
                        !isLoading
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mengirim...",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Send Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Laporan",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

// Kehadiran List with Loading/Error/Empty States
@Composable
fun KehadiranListWithState(
    kehadiranList: List<KehadiranItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    when {
        // Loading State
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat data kehadiran...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error State
        errorMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gagal Memuat Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        }

        // Empty State
        kehadiranList.isEmpty() -> {
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
                        .padding(48.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Empty Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada data kehadiran",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mulai entri kehadiran Anda\ndi menu Entri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Success State - Display Data
        else -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(kehadiranList) { kehadiran ->
                    KehadiranCard(kehadiranItem = kehadiran)
                }
            }
        }
    }
}

// Laporan Guru List with Loading/Error/Empty States
@Composable
fun LaporanGuruListWithState(
    kehadiranList: List<KehadiranItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    when {
        // Loading State
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat laporan guru...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error State
        errorMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gagal Memuat Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        }

        // Empty State
        kehadiranList.isEmpty() -> {
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
                        .padding(48.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Empty Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada laporan guru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laporkan kehadiran guru yang telat\natau tidak hadir di menu Entri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Success State - Display Data
        else -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(kehadiranList) { kehadiran ->
                    LaporanGuruCard(kehadiranItem = kehadiran)
                }
            }
        }
    }
}

// Tugas List with Loading/Error/Empty States
@Composable
fun TugasListWithState(
    tugasList: List<TugasItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    when {
        // Loading State
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat data tugas...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error State
        errorMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gagal Memuat Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        }

        // Empty State
        tugasList.isEmpty() -> {
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
                        .padding(48.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Empty Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada data tugas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mulai entri status tugas Anda\ndi menu Entri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Success State - Display Data
        else -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tugasList) { tugas ->
                    TugasCard(tugasItem = tugas)
                }
            }
        }
    }
}

// Updated KehadiranCard to use KehadiranItem
@Composable
fun KehadiranCard(kehadiranItem: KehadiranItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Mata Pelajaran
                Text(
                    text = kehadiranItem.mataPelajaran,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Tanggal
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date Icon",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = kehadiranItem.tanggal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Keterangan (if exists)
                if (!kehadiranItem.keterangan.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ket: ${kehadiranItem.keterangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status Badge
            Surface(
                color = when (kehadiranItem.status) {
                    "Hadir" -> MaterialTheme.colorScheme.primaryContainer
                    "Sakit" -> MaterialTheme.colorScheme.tertiaryContainer
                    "Izin" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = kehadiranItem.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (kehadiranItem.status) {
                        "Hadir" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Sakit" -> MaterialTheme.colorScheme.onTertiaryContainer
                        "Izin" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

// Laporan Guru Card - untuk menampilkan laporan kehadiran guru
@Composable
fun LaporanGuruCard(kehadiranItem: KehadiranItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header dengan Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kehadiranItem.mataPelajaran,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Status Badge
                Surface(
                    color = when (kehadiranItem.status) {
                        "Hadir" -> MaterialTheme.colorScheme.primaryContainer
                        "Telat" -> MaterialTheme.colorScheme.tertiaryContainer
                        "Izin" -> MaterialTheme.colorScheme.secondaryContainer
                        "Tidak Hadir" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = kehadiranItem.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (kehadiranItem.status) {
                            "Hadir" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "Telat" -> MaterialTheme.colorScheme.onTertiaryContainer
                            "Izin" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "Tidak Hadir" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ PERBAIKAN: Info Guru dengan safe access
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Teacher Icon",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))

                // ✅ FIX ERROR LINE 3029-3030: Gunakan safe navigation dan fallback
                val guruName = when {
                    !kehadiranItem.namaGuru.isNullOrBlank() -> kehadiranItem.namaGuru
                    kehadiranItem.user != null -> kehadiranItem.user.nama // ✅ BENAR: user.nama (bukan user?.nama)
                    else -> "Guru tidak diketahui"
                }

                Text(
                    text = guruName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Kode Guru (jika ada)
                if (!kehadiranItem.kodeGuru.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = kehadiranItem.kodeGuru,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tanggal
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Icon",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = kehadiranItem.tanggal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Jam Masuk/Keluar dengan safe parsing
            if (!kehadiranItem.jamMasuk.isNullOrBlank() || !kehadiranItem.jamKeluar.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time Icon",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Build string dengan safe handling
                    val timeText = buildString {
                        if (!kehadiranItem.jamMasuk.isNullOrBlank()) {
                            // Extract HH:mm from "HH:mm:ss" or "HH:mm"
                            val jamMasukCleaned = kehadiranItem.jamMasuk.split(":").take(2).joinToString(":")
                            append("Masuk: $jamMasukCleaned")
                        }
                        if (!kehadiranItem.jamKeluar.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            val jamKeluarCleaned = kehadiranItem.jamKeluar.split(":").take(2).joinToString(":")
                            append("Keluar: $jamKeluarCleaned")
                        }
                    }

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Keterangan (if exists)
            if (!kehadiranItem.keterangan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Icon",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = kehadiranItem.keterangan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

// Updated TugasCard to use TugasItem
@Composable
fun TugasCard(tugasItem: TugasItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Judul Tugas
                Text(
                    text = tugasItem.judulTugas,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Mata Pelajaran
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Subject Icon",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tugasItem.mataPelajaran,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Tanggal
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date Icon",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tugasItem.tanggal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status Badge
            Surface(
                color = when (tugasItem.status) {
                    "Selesai" -> MaterialTheme.colorScheme.primaryContainer
                    "Belum Selesai" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tugasItem.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (tugasItem.status) {
                        "Selesai" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Belum Selesai" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

// Previews - UPDATED dengan parameter
@Preview(showBackground = true, name = "Jadwal Page Preview")
@Composable
fun JadwalPagePreview() {
    AplikasiMonitoringKelasTheme {
        JadwalPelajaranSiswaPage(
            userName = "Siswa Preview"
        )
    }
}

@Preview(showBackground = true, name = "Siswa Screen - Full")
@Composable
fun SiswaScreenPreview() {
    AplikasiMonitoringKelasTheme {
        SiswaScreen(
            userId = 1,
            userName = "Ahmad Siswa",
            userEmail = "ahmad@sekolah.com",
            userRole = "siswa",
            authToken = null
        )
    }
}

@Preview(
    showBackground = true,
    name = "Siswa Screen - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SiswaScreenDarkPreview() {
    AplikasiMonitoringKelasTheme {
        SiswaScreen(
            userId = 1,
            userName = "Ahmad Siswa",
            userEmail = "ahmad@sekolah.com",
            userRole = "siswa",
            authToken = null
        )
    }
}

// ✅ TAMBAHKAN PREVIEW LENGKAP - Gabungan TopBar + SiswaScreen
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    name = "📱 Full Siswa Activity Preview",
    showSystemUi = true, // ✅ Tampilkan system UI untuk melihat full screen
    device = "id:pixel_5"
)
@Composable
fun FullSiswaActivityPreview() {
    AplikasiMonitoringKelasTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SiswaTopBar(
                    userName = "Andi Prasetyo",
                    userRole = "Siswa",
                    onLogout = { }
                )
            }
        ) { innerPadding ->
            // ✅ KUNCI: innerPadding ini yang mengatur jarak dari TopBar
            SiswaScreen(
                modifier = Modifier.padding(innerPadding), // ← MARGIN UTAMA
                userId = 1,
                userName = "Andi Prasetyo",
                userEmail = "andi@sekolah.com",
                userRole = "siswa",
                authToken = null,
                userKelas = "XI RPL 1"
            )
        }
    }
}

// ✅ PREVIEW DENGAN VARIASI PADDING
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    name = "📐 Margin Comparison Preview",
    heightDp = 800
)
@Composable
fun MarginComparisonPreview() {
    AplikasiMonitoringKelasTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== TANPA CUSTOM PADDING ==========
            Text(
                text = "❌ Default (Ada gap bawah TopBar)",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Scaffold(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Default TopBar", fontSize = 16.sp)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            ) { innerPadding ->
                // ← PERHATIKAN: innerPadding mengandung padding bottom
                Surface(
                    modifier = Modifier
                        .padding(innerPadding) // ← PADDING DARI SCAFFOLD
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Content dengan default padding",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== DENGAN CUSTOM PADDING ==========
            Text(
                text = "✅ Custom WindowInsets (No gap)",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Scaffold(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Custom TopBar", fontSize = 16.sp)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        // ✅ KUNCI: Hilangkan padding bottom
                        windowInsets = WindowInsets(
                            left = 0.dp,
                            top = 0.dp,
                            right = 0.dp,
                            bottom = 0.dp // ← SET KE 0 untuk menghilangkan gap
                        )
                    )
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "Content tanpa gap bawah TopBar",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// ✅ PREVIEW DENGAN ANNOTATED MARGINS
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    name = "📏 Annotated Margins Guide",
    heightDp = 600
)
@Composable
fun AnnotatedMarginsPreview() {
    AplikasiMonitoringKelasTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Box {
                        TopAppBar(
                            title = {
                                Column(
                                    modifier = Modifier.padding(vertical = 2.dp) // ← MARGIN 1
                                ) {
                                    Text(
                                        text = "Dashboard Siswa",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Andi Prasetyo",
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            actions = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(end = 6.dp) // ← MARGIN 2
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp, // ← MARGIN 3
                                            vertical = 4.dp     // ← MARGIN 4
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SISWA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .padding(end = 4.dp) // ← MARGIN 5
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Logout"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            windowInsets = WindowInsets(
                                left = 0.dp,
                                top = 0.dp,
                                right = 0.dp,
                                bottom = 0.dp // ← MARGIN 6 (Bottom Gap)
                            )
                        )

                        // ✅ ANNOTATIONS - Panah menunjuk ke margin
                        // Margin 1: Title Padding
                        Canvas(
                            modifier = Modifier
                                .offset(x = 16.dp, y = 16.dp)
                                .size(20.dp)
                        ) {
                            drawCircle(
                                color = Color.Red,
                                radius = 10f
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding) // ← MARGIN 7 (Content dari TopBar)
                        .fillMaxSize()
                ) {
                    // Sample content
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // ← MARGIN 8 (Card dari edge)
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp) // ← MARGIN 9 (Inside card)
                        ) {
                            Text(
                                text = "📍 Margin Guide:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val margins = listOf(
                                "1️⃣ Title vertical padding: 2.dp",
                                "2️⃣ Badge end padding: 6.dp",
                                "3️⃣ Badge horizontal: 10.dp",
                                "4️⃣ Badge vertical: 4.dp",
                                "5️⃣ Logout button end: 4.dp",
                                "6️⃣ TopBar bottom inset: 0.dp",
                                "7️⃣ Content from TopBar: auto",
                                "8️⃣ Card from edge: 16.dp",
                                "9️⃣ Inside card padding: 16.dp"
                            )

                            margins.forEach { margin ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
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
                                        text = margin,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ✅ Overlay Guide Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Horizontal line showing TopBar height
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, 120f),
                    end = Offset(size.width, 120f),
                    strokeWidth = 2f
                )
            }
        }
    }
}