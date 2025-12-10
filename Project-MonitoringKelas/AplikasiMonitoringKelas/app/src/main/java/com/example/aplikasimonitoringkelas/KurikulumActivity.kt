package com.example.aplikasimonitoringkelas

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplikasimonitoringkelas.data.remote.request.JadwalRequest
import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongItem
import com.example.aplikasimonitoringkelas.data.repository.JadwalRepository
import com.example.aplikasimonitoringkelas.ui.theme.AplikasiMonitoringKelasTheme
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.launch
import com.example.aplikasimonitoringkelas.data.remote.ApiConfig
import com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongKehadiranResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.BorderStroke  // ✅ TAMBAHKAN
import androidx.compose.material.icons.filled.Edit  // ✅ TAMBAHKAN
import com.example.aplikasimonitoringkelas.data.repository.GuruRepository  // ✅ TAMBAHKAN
import com.example.aplikasimonitoringkelas.data.remote.response.GuruItem  // ✅ TAMBAHKAN
import androidx.compose.foundation.verticalScroll  // ✅ TAMBAHKAN
import androidx.compose.foundation.rememberScrollState  // ✅ TAMBAHKAN
import com.example.aplikasimonitoringkelas.data.remote.request.AssignGuruPenggantiRequest
import com.example.aplikasimonitoringkelas.data.repository.KehadiranRepository


class KurikulumActivity : ComponentActivity() {
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
                // ✅ PERBAIKAN: Sama seperti SiswaActivity
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        KurikulumTopBar(
                            userName = userName,
                            userRole = userRole,
                            onLogout = {
                                // ✅ Handle logout - kembali ke MainActivity
                                val sharedPreferences = getSharedPreferences("MonitoringKelasPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    clear()
                                    apply()
                                }

                                val intent =
                                    Intent(this@KurikulumActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("FROM_LOGOUT", true)

                                startActivity(intent)
                                finish()

                                Toast.makeText(
                                    this@KurikulumActivity,
                                    "Anda telah logout",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                ) { innerPadding ->
                    // ✅ PERBAIKAN: Langsung tampilkan KurikulumScreen dengan padding dari Scaffold
                    KurikulumScreen(
                        modifier = Modifier.padding(innerPadding), // ← Padding dari outer Scaffold
                        userId = userId,
                        userName = userName,
                        userEmail = userEmail,
                        userRole = userRole,
                        authToken = authToken
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KurikulumTopBar(
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
                    text = "Waka Kurikulum",
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
        // ✅ SAMA SEPERTI SISWA - BIARKAN DEFAULT (tidak perlu windowInsets custom)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KurikulumScreen(
    userId: Int = -1,
    userName: String = "Kurikulum",
    userEmail: String = "",
    userRole: String = "",
    authToken: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    // ✅ UPDATE: 3 menu (Kelas Kosong, Kelas Aktif, List)
    val items = listOf(
        BottomNavigationItemKurikulum("Kelas Kosong", Icons.Default.Error),
        BottomNavigationItemKurikulum("Kelas Aktif", Icons.Default.CheckCircle),
        BottomNavigationItemKurikulum("List", Icons.Default.List)
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedItem) {
                0 -> MonitoringKelasKosongPage(
                    modifier = Modifier.fillMaxSize()
                )
                1 -> MonitoringKelasAktifPage(
                    modifier = Modifier.fillMaxSize()
                )
                2 -> ListKurikulumPage(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Bottom Navigation Bar
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

data class BottomNavigationItemKurikulum(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class JadwalKurikulumItem(
    val jam: String,
    val mataPelajaran: String,
    val kodeGuru: String,
    val namaGuru: String,
    val ruangan: String,
    val kelas: String
)

data class PergantianGuru(
    val tanggal: String,
    val jam: String,
    val kelas: String,
    val mataPelajaran: String,
    val guruAsli: String,
    val guruPengganti: String,
    val alasan: String
)

data class GuruTersedia(
    val kode: String,
    val nama: String,
    val mataPelajaran: String,
    val status: String // "Tersedia", "Mengajar", "Izin"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringKelasKosongPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // States
    var selectedTanggal by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
    }
    var selectedStatus by remember { mutableStateOf("Semua Status") }
    var selectedKelas by remember { mutableStateOf("Semua Kelas") } // ✅ TAMBAHKAN
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) } // ✅ TAMBAHKAN

    // API states
    var kelasKosongList by remember { mutableStateOf<List<KelasKosongItem>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var jumlahKelasKosong by remember { mutableIntStateOf(0) }

    // Dialog states
    var selectedKelasKosong by remember { mutableStateOf<KelasKosongItem?>(null) }
    var showGantiGuruDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    val jadwalRepository = remember { JadwalRepository() }
    val snackbarHostState = remember { SnackbarHostState() }

    val statusOptions = listOf("Semua Status", "Telat", "Tidak Hadir", "Izin")

    // ✅ TAMBAHKAN: Opsi filter kelas
    val kelasOptions = listOf(
        "Semua Kelas",
        "X RPL 1", "X RPL 2", "X RPL 3",
        "XI RPL 1", "XI RPL 2", "XI RPL 3",
        "XII RPL 1", "XII RPL 2", "XII RPL 3"
    )

    // ✅ PERBAIKAN: Fetch dengan filter kelas
    LaunchedEffect(selectedTanggal, selectedStatus, selectedKelas) {
        isFetching = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.getKelasKosongKehadiran(
                    tanggal = selectedTanggal,
                    kelas = if (selectedKelas == "Semua Kelas") null else selectedKelas, // ✅ FILTER KELAS
                    status = if (selectedStatus == "Semua Status") null else selectedStatus
                )

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.success) {
                        // ✅ Filter kelas kosong yang BELUM ada guru pengganti
                        val filteredList = data.data.filter { kelasKosong ->
                            kelasKosong.namaGuruPengganti.isNullOrBlank()
                        }

                        kelasKosongList = filteredList
                        jumlahKelasKosong = filteredList.size
                        isFetching = false

                        if (AppConfig.isDebugMode()) {
                            Log.d("KelasKosong", "✓ Total dari API: ${data.data.size}")
                            Log.d("KelasKosong", "✓ Setelah filter (belum ada pengganti): ${filteredList.size}")
                            Log.d("KelasKosong", "  Filter - Kelas: $selectedKelas, Status: $selectedStatus")
                        }
                    } else {
                        errorMessage = data.message
                        isFetching = false
                    }
                } else {
                    errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    isFetching = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isFetching = false
                Log.e("KelasKosong", "✗ Error: ${e.message}", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Kelas Kosong Icon",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Monitoring Kelas Kosong",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Belum Ada Guru Pengganti",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (!isFetching) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$jumlahKelasKosong Kelas Kosong",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (jumlahKelasKosong > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ✅ FILTER SECTION - UPDATE dengan Filter Kelas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filter Pencarian",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tanggal Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanggal: $selectedTanggal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ TAMBAHKAN: Filter Kelas Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedKelas,
                        onExpandedChange = { expandedKelas = !expandedKelas }
                    ) {
                        OutlinedTextField(
                            value = selectedKelas,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter Kelas") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Class Icon"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedKelas,
                            onDismissRequest = { expandedKelas = false }
                        ) {
                            kelasOptions.forEach { kelas ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (kelas == "Semua Kelas")
                                                    Icons.Default.List
                                                else
                                                    Icons.Default.School,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(kelas)
                                        }
                                    },
                                    onClick = {
                                        selectedKelas = kelas
                                        expandedKelas = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter Status") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Status Icon"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (status) {
                                                    "Telat" -> Icons.Default.Schedule
                                                    "Tidak Hadir" -> Icons.Default.Error
                                                    "Izin" -> Icons.Default.Info
                                                    else -> Icons.Default.List
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = when (status) {
                                                    "Telat" -> MaterialTheme.colorScheme.tertiary
                                                    "Tidak Hadir" -> MaterialTheme.colorScheme.error
                                                    "Izin" -> MaterialTheme.colorScheme.secondary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(status)
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

                    // ✅ TAMBAHKAN: Info filter aktif
                    if (selectedKelas != "Semua Kelas" || selectedStatus != "Semua Status") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
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
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Filter aktif: ${if (selectedKelas != "Semua Kelas") selectedKelas else ""} ${if (selectedStatus != "Semua Status") "• $selectedStatus" else ""}".trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // CONTENT - List Kelas Kosong (tidak berubah)
            if (isFetching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat data kelas kosong...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                            text = "Gagal Memuat Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (kelasKosongList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No Problem Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🎉 Semua Guru Hadir!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada kelas kosong pada tanggal $selectedTanggal\n${if (selectedKelas != "Semua Kelas") "untuk $selectedKelas" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Display all cards in a Column instead of LazyColumn
                kelasKosongList.forEachIndexed { index, kelasKosong ->
                    KelasKosongCard(
                        kelasKosong = kelasKosong,
                        nomor = index + 1,
                        onGantiGuru = {
                            selectedKelasKosong = kelasKosong
                            showGantiGuruDialog = true
                        }
                    )
                    if (index < kelasKosongList.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
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

    // Dialog Ganti Guru (tidak berubah - tetap ada di sini)
    if (showGantiGuruDialog && selectedKelasKosong != null) {
        GantiGuruKelasKosongDialog(
            kelasKosong = selectedKelasKosong!!,
            isLoading = isUpdating,
            onDismiss = {
                if (!isUpdating) {
                    showGantiGuruDialog = false
                    selectedKelasKosong = null
                }
            },
            onGantiGuru = { selectedGuruItem ->
                isUpdating = true

                coroutineScope.launch {
                    try {
                        val cleanNamaGuru = selectedGuruItem.nama
                            .replace(", S.Pd.", "")
                            .replace(", M.Pd.", "")
                            .replace(", S.Kom.", "")
                            .replace(", M.T.", "")
                            .replace(", S.Si.", "")
                            .replace(", S.Sn.", "")
                            .replace(", S.T.", "")
                            .replace("Dr. ", "")
                            .trim()

                        val kehadiranRepository = KehadiranRepository()
                        val assignRequest = AssignGuruPenggantiRequest(
                            namaGuruPengganti = cleanNamaGuru,
                            kodeGuruPengganti = selectedGuruItem.kodeGuru,
                            keteranganPengganti = "Guru pengganti ditugaskan oleh Kurikulum karena ${selectedKelasKosong!!.namaGuru} ${selectedKelasKosong!!.status}"
                        )

                        kehadiranRepository.assignGuruPengganti(
                            kehadiranId = selectedKelasKosong!!.id,
                            request = assignRequest
                        ).onSuccess { updatedKehadiran ->
                            // Refresh list dengan filter yang sama
                            val apiService = ApiConfig.getApiService()
                            val response = apiService.getKelasKosongKehadiran(
                                tanggal = selectedTanggal,
                                kelas = if (selectedKelas == "Semua Kelas") null else selectedKelas,
                                status = if (selectedStatus == "Semua Status") null else selectedStatus
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val filteredList = response.body()!!.data.filter {
                                    it.namaGuruPengganti.isNullOrBlank()
                                }
                                kelasKosongList = filteredList
                                jumlahKelasKosong = filteredList.size
                            }

                            snackbarHostState.showSnackbar(
                                message = "✓ Guru pengganti '$cleanNamaGuru' berhasil ditugaskan",
                                duration = SnackbarDuration.Short
                            )

                            showGantiGuruDialog = false
                            selectedKelasKosong = null
                            isUpdating = false
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(
                                message = "✗ ${error.message}",
                                duration = SnackbarDuration.Long
                            )
                            isUpdating = false
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            message = "✗ Error: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                        isUpdating = false
                    }
                }
            }
        )
    }
}

// ✅ HALAMAN BARU: Monitoring Kelas Aktif (Kelas yang sudah ada guru)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringKelasAktifPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // States
    var selectedTanggal by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
    }
    var selectedKelas by remember { mutableStateOf("Semua Kelas") }
    var selectedStatus by remember { mutableStateOf("Semua Status") }
    var expandedKelas by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // API states
    var kelasAktifList by remember { mutableStateOf<List<KelasKosongItem>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var jumlahKelasAktif by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    val statusOptions = listOf("Semua Status", "Hadir", "Telat", "Guru Pengganti")
    val kelasOptions = listOf(
        "Semua Kelas",
        "X RPL 1", "X RPL 2", "X RPL 3",
        "XI RPL 1", "XI RPL 2", "XI RPL 3",
        "XII RPL 1", "XII RPL 2", "XII RPL 3"
    )

    // Fetch data when filters change
    LaunchedEffect(selectedTanggal, selectedKelas, selectedStatus) {
        isFetching = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.getKelasKosongKehadiran(
                    tanggal = selectedTanggal,
                    kelas = if (selectedKelas == "Semua Kelas") null else selectedKelas,
                    status = when (selectedStatus) {
                        "Semua Status" -> null
                        "Guru Pengganti" -> null // Filter di client
                        else -> selectedStatus
                    }
                )

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.success) {
                        // ✅ Filter: Kelas yang SUDAH ada guru
                        // 1. Status Hadir/Telat (guru asli hadir)
                        // 2. Ada guru_pengganti (guru pengganti sudah di-assign)
                        val filteredList = data.data.filter { kelasItem ->
                            val hasTeacher = kelasItem.status == "Hadir" || kelasItem.status == "Telat"
                            val hasReplacement = !kelasItem.namaGuruPengganti.isNullOrBlank()
                            
                            val matchesFilter = when (selectedStatus) {
                                "Hadir" -> kelasItem.status == "Hadir"
                                "Telat" -> kelasItem.status == "Telat"
                                "Guru Pengganti" -> hasReplacement
                                else -> true
                            }
                            
                            (hasTeacher || hasReplacement) && matchesFilter
                        }

                        kelasAktifList = filteredList
                        jumlahKelasAktif = filteredList.size
                        isFetching = false

                        if (AppConfig.isDebugMode()) {
                            Log.d("KelasAktif", "✓ Loaded ${filteredList.size} kelas aktif")
                        }
                    } else {
                        errorMessage = data.message
                        isFetching = false
                    }
                } else {
                    errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    isFetching = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isFetching = false
                Log.e("KelasAktif", "✗ Error: ${e.message}", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Kelas Aktif Icon",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Monitoring Kelas Aktif",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Kelas yang sudah ada guru",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        if (!isFetching) {
                            Text(
                                text = "$jumlahKelasAktif kelas sedang berjalan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Filter Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filter Pencarian",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tanggal Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                                .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedTanggal)!!),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Kelas Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedKelas,
                        onExpandedChange = { expandedKelas = !expandedKelas }
                    ) {
                        OutlinedTextField(
                            value = selectedKelas,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                            label = { Text("Filter Kelas") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedKelas,
                            onDismissRequest = { expandedKelas = false }
                        ) {
                            kelasOptions.forEach { kelas ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (kelas == selectedKelas) Icons.Default.CheckCircle else Icons.Default.School,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (kelas == selectedKelas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = kelas,
                                                fontWeight = if (kelas == selectedKelas) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedKelas = kelas
                                        expandedKelas = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            label = { Text("Filter Status") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when(status) {
                                                    "Hadir" -> Icons.Default.CheckCircle
                                                    "Telat" -> Icons.Default.Schedule
                                                    "Guru Pengganti" -> Icons.Default.SwapHoriz
                                                    else -> Icons.Default.Info
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = when(status) {
                                                    "Hadir" -> MaterialTheme.colorScheme.tertiary
                                                    "Telat" -> MaterialTheme.colorScheme.secondary
                                                    "Guru Pengganti" -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = status,
                                                fontWeight = if (status == selectedStatus) FontWeight.Bold else FontWeight.Normal
                                            )
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

                    // Active filter indicator
                    if (selectedKelas != "Semua Kelas" || selectedStatus != "Semua Status") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Filter aktif: ${if (selectedKelas != "Semua Kelas") selectedKelas else ""} ${if (selectedStatus != "Semua Status") selectedStatus else ""}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Content - List Kelas Aktif
            if (isFetching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat data kelas aktif...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gagal Memuat Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (kelasAktifList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak Ada Kelas Aktif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada kelas dengan guru pada tanggal ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Display all cards
                kelasAktifList.forEachIndexed { index, kelasItem ->
                    KelasAktifCard(
                        kelasItem = kelasItem,
                        nomor = index + 1
                    )
                    if (index < kelasAktifList.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
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

// ✅ Card untuk Kelas Aktif (Kelas yang sudah ada guru)
@Composable
fun KelasAktifCard(
    kelasItem: KelasKosongItem,
    nomor: Int
) {
    val hasReplacement = !kelasItem.namaGuruPengganti.isNullOrBlank()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasReplacement -> MaterialTheme.colorScheme.primaryContainer
                kelasItem.status == "Hadir" -> MaterialTheme.colorScheme.tertiaryContainer
                kelasItem.status == "Telat" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Nomor + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = "$nomor",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = when {
                        hasReplacement -> MaterialTheme.colorScheme.primary
                        kelasItem.status == "Hadir" -> MaterialTheme.colorScheme.tertiary
                        kelasItem.status == "Telat" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                hasReplacement -> Icons.Default.SwapHoriz
                                kelasItem.status == "Hadir" -> Icons.Default.CheckCircle
                                kelasItem.status == "Telat" -> Icons.Default.Schedule
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                hasReplacement -> MaterialTheme.colorScheme.onPrimary
                                kelasItem.status == "Hadir" -> MaterialTheme.colorScheme.onTertiary
                                kelasItem.status == "Telat" -> MaterialTheme.colorScheme.onSecondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasReplacement) "Guru Pengganti" else kelasItem.status,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                hasReplacement -> MaterialTheme.colorScheme.onPrimary
                                kelasItem.status == "Hadir" -> MaterialTheme.colorScheme.onTertiary
                                kelasItem.status == "Telat" -> MaterialTheme.colorScheme.onSecondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Jadwal Info (jika ada)
            kelasItem.jadwal?.let { jadwal ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${jadwal.hari}, ${jadwal.jam} | ${jadwal.kelas}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Mata Pelajaran
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = kelasItem.mataPelajaran,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guru Asli
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Guru Asli: ${kelasItem.namaGuru ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (kelasItem.kodeGuru != null) {
                        Text(
                            text = kelasItem.kodeGuru,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Guru Pengganti (jika ada)
            if (hasReplacement) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Diganti Oleh:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = kelasItem.namaGuruPengganti ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (kelasItem.kodeGuruPengganti != null) {
                                Text(
                                    text = kelasItem.kodeGuruPengganti,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Jam Kehadiran
            if (!kelasItem.jamMasuk.isNullOrBlank() || !kelasItem.jamKeluar.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!kelasItem.jamMasuk.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Masuk: ${kelasItem.jamMasuk}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (!kelasItem.jamKeluar.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keluar: ${kelasItem.jamKeluar}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Keterangan (jika ada)
            if (!kelasItem.keterangan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = kelasItem.keterangan,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Ruangan (jika ada dari jadwal)
            kelasItem.jadwal?.let { jadwal ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GantiGuruKelasKosongDialog(
    kelasKosong: KelasKosongItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onGantiGuru: (GuruItem) -> Unit // ✅ UBAH: Kirim GuruItem, bukan String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val guruRepository = remember { GuruRepository() }

    // ✅ TAMBAHKAN: State untuk guru list
    var selectedGuru by remember { mutableStateOf<GuruItem?>(null) }
    var expandedGuru by remember { mutableStateOf(false) }
    var guruList by remember { mutableStateOf<List<GuruItem>>(emptyList()) }
    var isLoadingGuru by remember { mutableStateOf(false) }
    var errorLoadGuru by remember { mutableStateOf<String?>(null) }

    // ✅ FETCH guru saat dialog dibuka
    LaunchedEffect(Unit) {
        isLoadingGuru = true
        errorLoadGuru = null

        coroutineScope.launch {
            guruRepository.getGuru(status = "Aktif")
                .onSuccess { data ->
                    guruList = data
                    isLoadingGuru = false

                    if (AppConfig.isDebugMode()) {
                        android.util.Log.d("GantiGuruDialog", "✓ Loaded ${data.size} guru")
                    }
                }
                .onFailure { exception ->
                    errorLoadGuru = exception.message
                    isLoadingGuru = false

                    android.util.Log.e("GantiGuruDialog", "✗ Error loading guru: ${exception.message}")
                }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading && !isLoadingGuru) onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ganti Guru Pengganti",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pilih guru pengganti untuk kelas kosong",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Info Kelas Kosong
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Kelas Kosong",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mata Pelajaran
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = kelasKosong.mataPelajaran,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Jadwal Info
                        kelasKosong.jadwal?.let { jadwal ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${jadwal.hari}, ${jadwal.jam}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kelas ${jadwal.kelas} • ${jadwal.ruangan}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Guru Tidak Hadir
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Guru Tidak Hadir:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = kelasKosong.namaGuru ?: "Tidak ada guru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Keterangan
                        if (!kelasKosong.keterangan.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keterangan: ${kelasKosong.keterangan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                // ✅ PERBAIKAN: Pilih Guru dari Database
                Text(
                    text = "Pilih Guru Pengganti",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // ✅ Tampilkan error jika gagal load guru
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
                                    text = "Gagal memuat guru",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
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

                // Dropdown Guru dari Database
                ExposedDropdownMenuBox(
                    expanded = expandedGuru,
                    onExpandedChange = {
                        expandedGuru = !expandedGuru && !isLoading && !isLoadingGuru && guruList.isNotEmpty()
                    }
                ) {
                    OutlinedTextField(
                        value = selectedGuru?.nama ?: "Pilih Guru Pengganti",
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
                                else if (guruList.isEmpty()) "Tidak ada guru"
                                else "Nama Guru Pengganti"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Teacher Icon"
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        supportingText = {
                            when {
                                isLoadingGuru -> Text("Sedang memuat data guru...")
                                errorLoadGuru != null -> Text(
                                    "Error: $errorLoadGuru",
                                    color = MaterialTheme.colorScheme.error
                                )
                                guruList.isEmpty() -> Text("Tidak ada guru aktif")
                                else -> Text("Pilih guru dari daftar")
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
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && !isLoadingGuru,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            selectedGuru?.let { onGantiGuru(it) } // ✅ Kirim GuruItem
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && !isLoadingGuru && selectedGuru != null,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onTertiary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KelasKosongCard(
    kelasKosong: KelasKosongItem,
    nomor: Int,
    onGantiGuru: () -> Unit = {} // ✅ TAMBAHKAN callback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(kelasKosong.status) {
                "Telat" -> MaterialTheme.colorScheme.tertiaryContainer
                "Tidak Hadir" -> MaterialTheme.colorScheme.errorContainer
                "Izin" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Nomor + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = "$nomor",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = when(kelasKosong.status) {
                        "Telat" -> MaterialTheme.colorScheme.tertiary
                        "Tidak Hadir" -> MaterialTheme.colorScheme.error
                        "Izin" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(kelasKosong.status) {
                                "Telat" -> Icons.Default.Schedule
                                "Tidak Hadir" -> Icons.Default.Error
                                "Izin" -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = kelasKosong.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Jadwal Info (jika ada)
            kelasKosong.jadwal?.let { jadwal ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${jadwal.hari}, ${jadwal.jam} | ${jadwal.kelas}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Mata Pelajaran
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = kelasKosong.mataPelajaran,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guru Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = kelasKosong.namaGuru ?: "Tidak ada guru",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (kelasKosong.kodeGuru != null) {
                        Text(
                            text = "Kode: ${kelasKosong.kodeGuru}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Keterangan (jika ada)
            if (!kelasKosong.keterangan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = kelasKosong.keterangan,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Ruangan (jika ada dari jadwal)
            kelasKosong.jadwal?.let { jadwal ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
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

            // ✅ PERBAIKAN AKHIR: SELALU tampilkan tombol, tidak perlu cek jadwalId
            // Karena backend auto-detect akan mengisi jadwal_id otomatis
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGantiGuru,
                modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Ganti Guru",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cari Guru Pengganti",
                        fontWeight = FontWeight.Bold
                    )
                }
            // ✅ HAPUS: Tidak perlu tutup blok if lagi
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// Di dalam file KurikulumActivity.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListKurikulumPage(modifier: Modifier = Modifier) {
    val guruRepository = remember { GuruRepository() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Guru states
    var guruList by remember { mutableStateOf<List<GuruItem>>(emptyList()) }
    var isLoadingGuru by remember { mutableStateOf(false) }
    var errorMessageGuru by remember { mutableStateOf<String?>(null) }

    // Filter states
    var selectedStatus by remember { mutableStateOf("Semua Status") }
    var selectedMataPelajaran by remember { mutableStateOf("Semua Mapel") }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedMapel by remember { mutableStateOf(false) }

    val statusOptions = listOf("Semua Status", "Aktif", "Nonaktif")

    // Extract unique mata pelajaran dari guru list
    val mapelOptions = remember(guruList) {
        val allMapel = guruList.map { it.mataPelajaran }.distinct().sorted()
        listOf("Semua Mapel") + allMapel
    }

    // Sample history data (bisa diganti dengan data real dari backend)
    val historyPergantian = listOf(
        PergantianGuru("2024-01-15", "07:00-08:30", "XI RPL 1", "Matematika", "Dr. Ahmad Wijaya", "Rudi Hartono", "Guru sakit"),
        PergantianGuru("2024-01-14", "10:15-11:45", "XII RPL 2", "Pemrograman Web", "Sari Purnama", "Budi Santoso", "Dinas luar"),
        PergantianGuru("2024-01-13", "13:00-14:30", "X RPL 1", "Basis Data", "Maya Sari", "Dedi Kurniawan", "Izin keluarga"),
        PergantianGuru("2024-01-12", "08:30-10:00", "XI RPL 2", "Algoritma", "Budi Santoso", "Sari Purnama", "Pelatihan")
    )

    // Fetch guru saat page dibuka atau filter berubah
    LaunchedEffect(selectedStatus, selectedMataPelajaran) {
        isLoadingGuru = true
        errorMessageGuru = null

        coroutineScope.launch {
            guruRepository.getGuru(
                status = if (selectedStatus == "Semua Status") null else selectedStatus,
                mataPelajaran = if (selectedMataPelajaran == "Semua Mapel") null else selectedMataPelajaran
            ).onSuccess { data ->
                guruList = data
                isLoadingGuru = false

                if (AppConfig.isDebugMode()) {
                    android.util.Log.d("ListKurikulum", "✓ Loaded ${data.size} guru")
                }
            }.onFailure { exception ->
                errorMessageGuru = exception.message
                isLoadingGuru = false

                android.util.Log.e("ListKurikulum", "✗ Error: ${exception.message}")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                        imageVector = Icons.Default.List,
                        contentDescription = "List Icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Data Kurikulum",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelola data guru dan riwayat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (!isLoadingGuru && selectedTab == 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${guruList.size} Guru Terdaftar",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Semua Guru",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Teachers Icon"
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "History Pergantian",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History Icon"
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Section (hanya untuk tab Guru)
            if (selectedTab == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filter Data Guru",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Status Filter
                            ExposedDropdownMenuBox(
                                expanded = expandedStatus,
                                onExpandedChange = { expandedStatus = !expandedStatus },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Status") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                                    },
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
                                            text = { Text(status) },
                                            onClick = {
                                                selectedStatus = status
                                                expandedStatus = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Mata Pelajaran Filter
                            ExposedDropdownMenuBox(
                                expanded = expandedMapel,
                                onExpandedChange = { expandedMapel = !expandedMapel },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedMataPelajaran,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Mata Pelajaran") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMapel)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedMapel,
                                    onDismissRequest = { expandedMapel = false }
                                ) {
                                    mapelOptions.forEach { mapel ->
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
                        }

                        // Active filter indicator
                        if (selectedStatus != "Semua Status" || selectedMataPelajaran != "Semua Mapel") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
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
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Filter: ${if (selectedStatus != "Semua Status") selectedStatus else ""} ${if (selectedMataPelajaran != "Semua Mapel") "• $selectedMataPelajaran" else ""}".trim(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content
            when (selectedTab) {
                0 -> GuruListWithState(
                    guruList = guruList,
                    isLoading = isLoadingGuru,
                    errorMessage = errorMessageGuru,
                    onRetry = {
                        coroutineScope.launch {
                            isLoadingGuru = true
                            errorMessageGuru = null
                            guruRepository.getGuru(
                                status = if (selectedStatus == "Semua Status") null else selectedStatus,
                                mataPelajaran = if (selectedMataPelajaran == "Semua Mapel") null else selectedMataPelajaran
                            ).onSuccess { data ->
                                guruList = data
                                isLoadingGuru = false
                            }.onFailure { exception ->
                                errorMessageGuru = exception.message
                                isLoadingGuru = false
                            }
                        }
                    }
                )
                1 -> HistoryPergantianList(historyList = historyPergantian)
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

// ✅ TAMBAHKAN: GuruListWithState untuk handle loading/error/empty
@Composable
fun GuruListWithState(
    guruList: List<GuruItem>,
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat data guru...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error State
        errorMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gagal Memuat Data Guru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
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
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "Retry Icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            }
        }

        // Empty State
        guruList.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                        imageVector = Icons.Default.Person,
                        contentDescription = "No Data Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak Ada Data Guru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada guru terdaftar dalam sistem",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Success State - Display Data
        else -> {
            GuruList(guruList = guruList)
        }
    }
}

// ✅ UPDATE: GuruList untuk handle GuruItem dari database
@Composable
fun GuruList(guruList: List<GuruItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        guruList.forEachIndexed { index, guru ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when(guru.status) {
                        "Aktif" -> MaterialTheme.colorScheme.surfaceVariant
                        "Nonaktif" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = when(guru.status) {
                            "Aktif" -> MaterialTheme.colorScheme.primary
                            "Nonaktif" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Teacher Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Info Guru
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Nama Guru
                        Text(
                            text = guru.nama,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Kode Guru
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = guru.kodeGuru,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Mata Pelajaran
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = guru.mataPelajaran,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Email (jika ada)
                        if (!guru.email.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = guru.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        color = when(guru.status) {
                            "Aktif" -> MaterialTheme.colorScheme.primaryContainer
                            "Nonaktif" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = guru.status,
                            style = MaterialTheme.typography.labelMedium,
                            color = when(guru.status) {
                                "Aktif" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Nonaktif" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryPergantianList(historyList: List<PergantianGuru>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        historyList.forEachIndexed { index, pergantian ->
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
                    // Date indicator
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // History info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Date and Time
                        Text(
                            text = "${pergantian.tanggal} | ${pergantian.jam}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${pergantian.kelas} - ${pergantian.mataPelajaran}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Pergantian detail
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pergantian.guruAsli,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )

                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(horizontal = 4.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = pergantian.guruPengganti,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Alasan: ${pergantian.alasan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "List Kurikulum Page Preview")
@Composable
fun ListKurikulumPagePreview() {
    AplikasiMonitoringKelasTheme {
        ListKurikulumPage()
    }
}

@Preview(showBackground = true, name = "Kurikulum Screen - Full")
@Composable
fun KurikulumScreenPreview() {
    AplikasiMonitoringKelasTheme {
        KurikulumScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Kurikulum Screen - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun KurikulumScreenDarkPreview() {
    AplikasiMonitoringKelasTheme {
        KurikulumScreen()
    }
}