package com.example.aplikasimonitoringkelas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplikasimonitoringkelas.data.remote.request.JadwalRequest
import com.example.aplikasimonitoringkelas.data.remote.request.UserRequest
import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.remote.response.UserItem
import com.example.aplikasimonitoringkelas.data.repository.JadwalRepository
import com.example.aplikasimonitoringkelas.data.repository.UserRepository
import com.example.aplikasimonitoringkelas.ui.theme.AplikasiMonitoringKelasTheme
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.launch

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Receive user data from Intent - SUDAH ADA ✅
        val userId = intent.getIntExtra("USER_ID", 0)
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: ""
        val authToken = intent.getStringExtra("AUTH_TOKEN")

        setContent {
            AplikasiMonitoringKelasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        AdminTopBar(userName = userName, userRole = userRole)
                        AdminScreen(
                            userId = userId,
                            userName = userName,
                            userEmail = userEmail,
                            userRole = userRole,
                            authToken = authToken,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(userName: String, userRole: String) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Dashboard Admin",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Login sebagai: $userName ($userRole)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userId: Int = -1,
    userName: String = "Admin",
    userEmail: String = "",
    userRole: String = "admin",
    authToken: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf(
        BottomNavigationItemAdmin("Entri Jadwal", Icons.Default.Add),
        BottomNavigationItemAdmin("Ubah Jadwal", Icons.Default.Edit),
        BottomNavigationItemAdmin("Entri User", Icons.Default.PersonAdd),
        BottomNavigationItemAdmin("List", Icons.Default.List)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
    ) { innerPadding ->
        when (selectedItem) {
            0 -> EntriJadwalPage(modifier = Modifier.padding(innerPadding))
            1 -> UbahJadwalPage(modifier = Modifier.padding(innerPadding))
            2 -> EntriUserPage(modifier = Modifier.padding(innerPadding))
            3 -> ListPageAdmin(modifier = Modifier.padding(innerPadding))
        }
    }
}

data class BottomNavigationItemAdmin(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class JadwalAdmin(
    val id: Int,
    val hari: String,
    val kelas: String,
    val mataPelajaran: String,
    val guru: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String,
    val semester: String,
    val tahunAjaran: String
)

data class UserAdmin(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String,
    val kelas: String?, // Khusus untuk siswa
    val status: String, // "Aktif", "Nonaktif"
    val tanggalDibuat: String
)

// ➕ MENU 1: ENTRI JADWAL (CREATE - WITH API INTEGRATION!)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriJadwalPage(modifier: Modifier = Modifier) {
    // Form state
    var selectedHari by remember { mutableStateOf("Senin") }
    var selectedKelas by remember { mutableStateOf("X RPL 1") }
    var selectedMataPelajaran by remember { mutableStateOf("Matematika") }
    var selectedGuru by remember { mutableStateOf("Pak Ahmad Yusuf") }
    var kodeGuru by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("07:00") }
    var jamSelesai by remember { mutableStateOf("08:30") }
    var selectedRuangan by remember { mutableStateOf("Lab 1") }

    // Dropdown states
    var expandedHari by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }
    var expandedMataPelajaran by remember { mutableStateOf(false) }
    var expandedGuru by remember { mutableStateOf(false) }
    var expandedRuangan by remember { mutableStateOf(false) }

    // Validation error states
    var jamMulaiError by remember { mutableStateOf(false) }
    var jamSelesaiError by remember { mutableStateOf(false) }
    var kodeGuruError by remember { mutableStateOf(false) }

    // API state management
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Jadwal list from API
    val jadwalList = remember { mutableStateListOf< JadwalItem>() }

    // Repository
    val repository = remember { JadwalRepository() }

    // Coroutine scope
    val scope = rememberCoroutineScope()

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Dropdown options - Data Master Lengkap
    val hariList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val kelasList = listOf("X RPL 1", "X RPL 2", "XI RPL 1", "XI RPL 2", "XII RPL 1", "XII RPL 2")
    val mataPelajaranList = listOf(
        "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "PKn", "Agama",
        "Pemrograman Dasar", "Pemrograman Web", "Basis Data", "Sistem Operasi",
        "Jaringan Komputer", "Pemrograman Mobile", "Algoritma", "Grafik Komputer"
    )
    val guruList = listOf(
        "Pak Ahmad Yusuf", "Bu Sari Dewi", "Pak Budi Santoso", "Bu Maya Sari",
        "Pak Dedi Kurniawan", "Bu Rina Wati", "Pak Eko Prasetyo", "Bu Lisa Maharani",
        "Pak Hendra Wijaya", "Bu Nina Safitri"
    )
    val ruanganList = listOf(
        "Lab 1", "Lab 2", "Lab Komputer", "Lab Android", "Lab Design", "Lab Jaringan",
        "Kelas 10A", "Kelas 10B", "Kelas 11A", "Kelas 11B", "Kelas 12A", "Kelas 12B"
    )

    // Load jadwal list saat page dibuka
    LaunchedEffect(Unit) {
        isFetching = true
        repository.getJadwal().onSuccess { jadwalData ->
            jadwalList.clear()
            jadwalList.addAll(jadwalData)
            isFetching = false
        }.onFailure { error ->
            errorMessage = "Gagal memuat jadwal: ${error.message}"
            isFetching = false
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        kodeGuruError = kodeGuru.isBlank()
        jamMulaiError = jamMulai.isBlank()
        jamSelesaiError = jamSelesai.isBlank()

        return !kodeGuruError && !jamMulaiError && !jamSelesaiError
    }

    // Save jadwal function with API call
    fun saveJadwal() {
        // Validate form
        if (!validateForm()) {
            errorMessage = "Mohon lengkapi semua field yang wajib diisi"
            return
        }

        // Clear previous messages
        errorMessage = null
        successMessage = null

        // Set loading state
        isLoading = true

        // Create request object
        val request = JadwalRequest(
            hari = selectedHari,
            jam = "$jamMulai-$jamSelesai",
            mataPelajaran = selectedMataPelajaran,
            kodeGuru = kodeGuru,
            namaGuru = selectedGuru,
            ruangan = selectedRuangan,
            kelas = selectedKelas
        )

        // Call API
        scope.launch {
            repository.createJadwal(request)
                .onSuccess { newJadwal ->
                    // Add to list
                    jadwalList.add(0, newJadwal) // Add to top of list

                    // Show success message
                    successMessage = "Jadwal ${newJadwal.mataPelajaran} berhasil disimpan!"

                    // Show snackbar
                    snackbarHostState.showSnackbar(
                        message = "✓ Jadwal berhasil disimpan",
                        duration = SnackbarDuration.Short
                    )

                    // Clear form
                    kodeGuru = ""
                    jamMulai = ""
                    jamSelesai = ""
                    selectedHari = "Senin"
                    selectedKelas = "X RPL 1"
                    selectedMataPelajaran = "Matematika"
                    selectedGuru = "Pak Ahmad Yusuf"
                    selectedRuangan = "Lab 1"

                    // Reset loading
                    isLoading = false
                }
                .onFailure { error ->
                    // Show error message
                    errorMessage = error.message ?: "Gagal menyimpan jadwal"

                    // Show snackbar
                    snackbarHostState.showSnackbar(
                        message = "✗ ${error.message}",
                        duration = SnackbarDuration.Long
                    )

                    // Reset loading
                    isLoading = false
                }
        }
    }

    // Refresh jadwal list
    fun refreshJadwalList() {
        isFetching = true
        scope.launch {
            repository.getJadwal().onSuccess { jadwalData ->
                jadwalList.clear()
                jadwalList.addAll(jadwalData)
                isFetching = false
            }.onFailure { error ->
                errorMessage = "Gagal memuat jadwal: ${error.message}"
                isFetching = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // Header Admin
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
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = "Admin Icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Entri Jadwal Pelajaran",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Login sebagai: Admin Sekolah",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (AppConfig.isDummyMode()) "Mode: DUMMY DATA (READ ONLY)" else "Mode: API LIVE - Full CRUD",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (AppConfig.isDummyMode())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error Message Card
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                            imageVector = Icons.Default.Delete,
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
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Success Message Card
            if (successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { successMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Form Jadwal Baru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Row 1: Hari dan Kelas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dropdown Hari
                        ExposedDropdownMenuBox(
                            expanded = expandedHari,
                            onExpandedChange = { expandedHari = !expandedHari },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedHari,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHari) },
                                label = { Text("Hari") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Date Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHari,
                                onDismissRequest = { expandedHari = false }
                            ) {
                                hariList.forEach { hari ->
                                    DropdownMenuItem(
                                        text = { Text(hari) },
                                        onClick = {
                                            selectedHari = hari
                                            expandedHari = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Kelas
                        ExposedDropdownMenuBox(
                            expanded = expandedKelas,
                            onExpandedChange = { expandedKelas = !expandedKelas },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedKelas,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                label = { Text("Kelas") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Class Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedKelas,
                                onDismissRequest = { expandedKelas = false }
                            ) {
                                kelasList.forEach { kelas ->
                                    DropdownMenuItem(
                                        text = { Text(kelas) },
                                        onClick = {
                                            selectedKelas = kelas
                                            expandedKelas = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Mata Pelajaran dan Guru
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dropdown Mata Pelajaran
                        ExposedDropdownMenuBox(
                            expanded = expandedMataPelajaran,
                            onExpandedChange = { expandedMataPelajaran = !expandedMataPelajaran },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedMataPelajaran,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMataPelajaran) },
                                label = { Text("Mata Pelajaran") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Subject,
                                        contentDescription = "Subject Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMataPelajaran,
                                onDismissRequest = { expandedMataPelajaran = false }
                            ) {
                                mataPelajaranList.forEach { matpel ->
                                    DropdownMenuItem(
                                        text = { Text(matpel, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedMataPelajaran = matpel
                                            expandedMataPelajaran = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Guru
                        ExposedDropdownMenuBox(
                            expanded = expandedGuru,
                            onExpandedChange = { expandedGuru = !expandedGuru },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedGuru,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGuru) },
                                label = { Text("Guru") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Teacher Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGuru,
                                onDismissRequest = { expandedGuru = false }
                            ) {
                                guruList.forEach { guru ->
                                    DropdownMenuItem(
                                        text = { Text(guru, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedGuru = guru
                                            expandedGuru = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kode Guru (Required Field)
                    OutlinedTextField(
                        value = kodeGuru,
                        onValueChange = {
                            kodeGuru = it
                            kodeGuruError = it.isBlank()
                        },
                        label = { Text("Kode Guru *") },
                        placeholder = { Text("Contoh: GR001") },
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = kodeGuruError,
                        supportingText = {
                            if (kodeGuruError) {
                                Text(
                                    text = "Kode guru wajib diisi",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Row 3: Jam Mulai dan Jam Selesai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Jam Mulai
                        OutlinedTextField(
                            value = jamMulai,
                            onValueChange = {
                                jamMulai = it
                                jamMulaiError = it.isBlank()
                            },
                            label = { Text("Jam Mulai *") },
                            placeholder = { Text("07:00") },
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time Icon"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = jamMulaiError,
                            supportingText = {
                                if (jamMulaiError) {
                                    Text(
                                        text = "Jam mulai wajib diisi",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Jam Selesai
                        OutlinedTextField(
                            value = jamSelesai,
                            onValueChange = {
                                jamSelesai = it
                                jamSelesaiError = it.isBlank()
                            },
                            label = { Text("Jam Selesai *") },
                            placeholder = { Text("08:30") },
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time Icon"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = jamSelesaiError,
                            supportingText = {
                                if (jamSelesaiError) {
                                    Text(
                                        text = "Jam selesai wajib diisi",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Dropdown Ruangan
                    ExposedDropdownMenuBox(
                        expanded = expandedRuangan,
                        onExpandedChange = { expandedRuangan = !expandedRuangan },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRuangan,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isLoading,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuangan) },
                            label = { Text("Ruangan") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRuangan,
                            onDismissRequest = { expandedRuangan = false }
                        ) {
                            ruanganList.forEach { ruangan ->
                                DropdownMenuItem(
                                    text = { Text(ruangan) },
                                    onClick = {
                                        selectedRuangan = ruangan
                                        expandedRuangan = false
                                    }
                                )
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { saveJadwal() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading && !AppConfig.isDummyMode()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (AppConfig.isDummyMode()) "Mode Dummy - Read Only" else "Simpan Jadwal",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            // Jadwal List Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal Tersimpan (${jadwalList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { refreshJadwalList() },
                    enabled = !isFetching
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isFetching && jadwalList.isEmpty()) {
                // Loading state
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat jadwal...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (jadwalList.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(jadwalList) { index, jadwal ->
                        JadwalItemCard(
                            jadwalData = jadwal,
                            index = index + 1
                        )
                    }
                }
            } else {
                // Empty state
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
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "No Schedule",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada jadwal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Mulai dengan mengisi form di atas",
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

@Composable
fun JadwalItemCard(
    jadwalData: JadwalItem,
    index: Int
) {
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
            // Number indicator
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Jadwal info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${jadwalData.hari} | ${jadwalData.jam}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = jadwalData.kelas,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = jadwalData.mataPelajaran,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Teacher Icon",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${jadwalData.namaGuru ?: "-"} | ${jadwalData.ruangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ✏️ MENU 2: UBAH JADWAL (UPDATE & DELETE WITH API INTEGRATION!)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahJadwalPage(modifier: Modifier = Modifier) {
    // Filter state
    var selectedFilterHari by remember { mutableStateOf("Semua Hari") }
    var selectedFilterKelas by remember { mutableStateOf("Semua Kelas") }
    var expandedFilterHari by remember { mutableStateOf(false) }
    var expandedFilterKelas by remember { mutableStateOf(false) }

    // Jadwal list from API
    val jadwalList = remember { mutableStateListOf<JadwalItem>() }

    // Selected jadwal for edit/delete
    var selectedJadwal by remember { mutableStateOf<JadwalItem?>(null) }

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Loading states
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    // Message states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Repository
    val repository = remember { JadwalRepository() }

    // Coroutine scope
    val scope = rememberCoroutineScope()

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter options
    val hariFilterOptions = listOf("Semua Hari", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val kelasFilterOptions = listOf("Semua Kelas", "X RPL 1", "X RPL 2", "XI RPL 1", "XI RPL 2", "XII RPL 1", "XII RPL 2")

    // Load jadwal list saat page dibuka
    LaunchedEffect(selectedFilterHari, selectedFilterKelas) {
        isFetching = true
        val hari = if (selectedFilterHari == "Semua Hari") null else selectedFilterHari
        val kelas = if (selectedFilterKelas == "Semua Kelas") null else selectedFilterKelas

        repository.getJadwal(hari = hari, kelas = kelas).onSuccess { jadwalData ->
            jadwalList.clear()
            jadwalList.addAll(jadwalData)
            isFetching = false
        }.onFailure { error ->
            errorMessage = "Gagal memuat jadwal: ${error.message}"
            isFetching = false
        }
    }

    // Delete jadwal function
    fun deleteJadwal(jadwal: JadwalItem) {
        isDeleting = true
        errorMessage = null
        successMessage = null

        scope.launch {
            repository.deleteJadwal(jadwal.id)
                .onSuccess { message ->
                    // Remove from list
                    jadwalList.remove(jadwal)

                    // Show success message
                    successMessage = message
                    snackbarHostState.showSnackbar(
                        message = "✓ $message",
                        duration = SnackbarDuration.Short
                    )

                    // Close dialog
                    showDeleteDialog = false
                    selectedJadwal = null
                    isDeleting = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Gagal menghapus jadwal"
                    snackbarHostState.showSnackbar(
                        message = "✗ ${error.message}",
                        duration = SnackbarDuration.Long
                    )
                    isDeleting = false
                }
        }
    }

    // Refresh jadwal list
    fun refreshJadwalList() {
        isFetching = true
        val hari = if (selectedFilterHari == "Semua Hari") null else selectedFilterHari
        val kelas = if (selectedFilterKelas == "Semua Kelas") null else selectedFilterKelas

        scope.launch {
            repository.getJadwal(hari = hari, kelas = kelas).onSuccess { jadwalData ->
                jadwalList.clear()
                jadwalList.addAll(jadwalData)
                isFetching = false
            }.onFailure { error ->
                errorMessage = "Gagal memuat jadwal: ${error.message}"
                isFetching = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kelola Jadwal Pelajaran",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Edit dan Hapus jadwal yang sudah ada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (AppConfig.isDummyMode()) "Mode: DUMMY (READ ONLY)" else "Total: ${jadwalList.size} jadwal",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (AppConfig.isDummyMode())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error Message Card
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                            imageVector = Icons.Default.Delete,
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
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Success Message Card
            if (successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { successMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                            text = "Filter Jadwal",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = { refreshJadwalList() },
                            enabled = !isFetching
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Filter Hari
                        ExposedDropdownMenuBox(
                            expanded = expandedFilterHari,
                            onExpandedChange = { expandedFilterHari = !expandedFilterHari },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedFilterHari,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isFetching,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilterHari) },
                                label = { Text("Hari") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFilterHari,
                                onDismissRequest = { expandedFilterHari = false }
                            ) {
                                hariFilterOptions.forEach { hari ->
                                    DropdownMenuItem(
                                        text = { Text(hari) },
                                        onClick = {
                                            selectedFilterHari = hari
                                            expandedFilterHari = false
                                        }
                                    )
                                }
                            }
                        }

                        // Filter Kelas
                        ExposedDropdownMenuBox(
                            expanded = expandedFilterKelas,
                            onExpandedChange = { expandedFilterKelas = !expandedFilterKelas },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedFilterKelas,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isFetching,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilterKelas) },
                                label = { Text("Kelas") },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFilterKelas,
                                onDismissRequest = { expandedFilterKelas = false }
                            ) {
                                kelasFilterOptions.forEach { kelas ->
                                    DropdownMenuItem(
                                        text = { Text(kelas) },
                                        onClick = {
                                            selectedFilterKelas = kelas
                                            expandedFilterKelas = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Jadwal List
            if (isFetching && jadwalList.isEmpty()) {
                // Loading state
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat jadwal...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (jadwalList.isNotEmpty()) {
                Text(
                    text = "Ditemukan ${jadwalList.size} jadwal",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(jadwalList) { index, jadwal ->
                        EditableJadwalCard(
                            jadwalData = jadwal,
                            index = index + 1,
                            isLoading = isDeleting || isUpdating,
                            onEdit = {
                                selectedJadwal = jadwal
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedJadwal = jadwal
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            } else {
                // Empty state
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
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No Results",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ditemukan jadwal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Coba ubah filter atau tambah jadwal baru",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedJadwal != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                    selectedJadwal = null
                }
            },
            title = {
                Text(
                    text = "Konfirmasi Hapus",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus jadwal ini?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = selectedJadwal!!.mataPelajaran,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${selectedJadwal!!.hari} | ${selectedJadwal!!.jam}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Kelas: ${selectedJadwal!!.kelas}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data yang sudah dihapus tidak dapat dikembalikan!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { deleteJadwal(selectedJadwal!!) },
                    enabled = !isDeleting && !AppConfig.isDummyMode(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menghapus...")
                    } else {
                        Text(if (AppConfig.isDummyMode()) "Mode Dummy" else "Hapus")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedJadwal = null
                    },
                    enabled = !isDeleting
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedJadwal != null) {
        EditJadwalDialog(
            jadwal = selectedJadwal!!,
            isLoading = isUpdating,
            onDismiss = {
                if (!isUpdating) {
                    showEditDialog = false
                    selectedJadwal = null
                }
            },
            onUpdate = { updatedRequest ->
                isUpdating = true
                errorMessage = null
                successMessage = null

                scope.launch {
                    repository.updateJadwal(selectedJadwal!!.id, updatedRequest)
                        .onSuccess { updatedJadwal ->
                            // Update in list
                            val index = jadwalList.indexOfFirst { it.id == updatedJadwal.id }
                            if (index != -1) {
                                jadwalList[index] = updatedJadwal
                            }

                            // Show success message
                            successMessage = "Jadwal berhasil diupdate!"
                            snackbarHostState.showSnackbar(
                                message = "✓ Jadwal berhasil diupdate",
                                duration = SnackbarDuration.Short
                            )

                            // Close dialog
                            showEditDialog = false
                            selectedJadwal = null
                            isUpdating = false
                        }
                        .onFailure { error ->
                            errorMessage = error.message ?: "Gagal mengupdate jadwal"
                            snackbarHostState.showSnackbar(
                                message = "✗ ${error.message}",
                                duration = SnackbarDuration.Long
                            )
                            isUpdating = false
                        }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJadwalDialog(
    jadwal: JadwalItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (JadwalRequest) -> Unit
) {
    // Form state (pre-filled with jadwal data)
    var selectedHari by remember { mutableStateOf(jadwal.hari) }
    var selectedKelas by remember { mutableStateOf(jadwal.kelas) }
    var selectedMataPelajaran by remember { mutableStateOf(jadwal.mataPelajaran) }
    var selectedGuru by remember { mutableStateOf(jadwal.namaGuru ?: "") }
    var kodeGuru by remember { mutableStateOf(jadwal.kodeGuru ?: "") }
    var selectedRuangan by remember { mutableStateOf(jadwal.ruangan) }

    // Extract jam mulai dan selesai dari format "07:00-08:30"
    val jamParts = jadwal.jam.split("-")
    var jamMulai by remember { mutableStateOf(jamParts.getOrNull(0)?.trim() ?: "") }
    var jamSelesai by remember { mutableStateOf(jamParts.getOrNull(1)?.trim() ?: "") }

    // Dropdown states
    var expandedHari by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }
    var expandedMataPelajaran by remember { mutableStateOf(false) }
    var expandedGuru by remember { mutableStateOf(false) }
    var expandedRuangan by remember { mutableStateOf(false) }

    // Dropdown options
    val hariList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val kelasList = listOf("X RPL 1", "X RPL 2", "XI RPL 1", "XI RPL 2", "XII RPL 1", "XII RPL 2")
    val mataPelajaranList = listOf(
        "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "PKn", "Agama",
        "Pemrograman Dasar", "Pemrograman Web", "Basis Data", "Sistem Operasi",
        "Jaringan Komputer", "Pemrograman Mobile", "Algoritma", "Grafik Komputer"
    )
    val guruList = listOf(
        "Pak Ahmad Yusuf", "Bu Sari Dewi", "Pak Budi Santoso", "Bu Maya Sari",
        "Pak Dedi Kurniawan", "Bu Rina Wati", "Pak Eko Prasetyo", "Bu Lisa Maharani",
        "Pak Hendra Wijaya", "Bu Nina Safitri"
    )
    val ruanganList = listOf(
        "Lab 1", "Lab 2", "Lab Komputer", "Lab Android", "Lab Design", "Lab Jaringan",
        "Kelas 10A", "Kelas 10B", "Kelas 11A", "Kelas 11B", "Kelas 12A", "Kelas 12B"
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = Modifier.fillMaxWidth()
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Jadwal",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isLoading) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Divider()

                // Form fields in scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hari dan Kelas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedHari,
                            onExpandedChange = { expandedHari = !expandedHari },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedHari,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHari) },
                                label = { Text("Hari", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHari,
                                onDismissRequest = { expandedHari = false }
                            ) {
                                hariList.forEach { hari ->
                                    DropdownMenuItem(
                                        text = { Text(hari, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedHari = hari
                                            expandedHari = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedKelas,
                            onExpandedChange = { expandedKelas = !expandedKelas },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedKelas,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                label = { Text("Kelas", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = expandedKelas,
                                onDismissRequest = { expandedKelas = false }
                            ) {
                                kelasList.forEach { kelas ->
                                    DropdownMenuItem(
                                        text = { Text(kelas, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedKelas = kelas
                                            expandedKelas = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Mata Pelajaran
                    ExposedDropdownMenuBox(
                        expanded = expandedMataPelajaran,
                        onExpandedChange = { expandedMataPelajaran = !expandedMataPelajaran }
                    ) {
                        OutlinedTextField(
                            value = selectedMataPelajaran,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isLoading,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMataPelajaran) },
                            label = { Text("Mata Pelajaran", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMataPelajaran,
                            onDismissRequest = { expandedMataPelajaran = false }
                        ) {
                            mataPelajaranList.forEach { matpel ->
                                DropdownMenuItem(
                                    text = { Text(matpel, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        selectedMataPelajaran = matpel
                                        expandedMataPelajaran = false
                                    }
                                )
                            }
                        }
                    }

                    // Guru dan Kode Guru
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedGuru,
                            onExpandedChange = { expandedGuru = !expandedGuru },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            OutlinedTextField(
                                value = selectedGuru,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGuru) },
                                label = { Text("Guru", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGuru,
                                onDismissRequest = { expandedGuru = false }
                            ) {
                                guruList.forEach { guru ->
                                    DropdownMenuItem(
                                        text = { Text(guru, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedGuru = guru
                                            expandedGuru = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = kodeGuru,
                            onValueChange = { kodeGuru = it },
                            label = { Text("Kode", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Jam Mulai dan Jam Selesai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = jamMulai,
                            onValueChange = { jamMulai = it },
                            label = { Text("Jam Mulai", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("07:00") },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )

                        OutlinedTextField(
                            value = jamSelesai,
                            onValueChange = { jamSelesai = it },
                            label = { Text("Jam Selesai", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("08:30") },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Ruangan
                    ExposedDropdownMenuBox(
                        expanded = expandedRuangan,
                        onExpandedChange = { expandedRuangan = !expandedRuangan }
                    ) {
                        OutlinedTextField(
                            value = selectedRuangan,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isLoading,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuangan) },
                            label = { Text("Ruangan", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRuangan,
                            onDismissRequest = { expandedRuangan = false }
                        ) {
                            ruanganList.forEach { ruangan ->
                                DropdownMenuItem(
                                    text = { Text(ruangan, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        selectedRuangan = ruangan
                                        expandedRuangan = false
                                    }
                                )
                            }
                        }
                    }
                }

                Divider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val request = JadwalRequest(
                                hari = selectedHari,
                                jam = "$jamMulai-$jamSelesai",
                                mataPelajaran = selectedMataPelajaran,
                                kodeGuru = kodeGuru,
                                namaGuru = selectedGuru,
                                ruangan = selectedRuangan,
                                kelas = selectedKelas
                            )
                            onUpdate(request)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Update",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditableJadwalCard(
    jadwalData: JadwalItem,
    index: Int,
    isLoading: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            // Number indicator
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Jadwal info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${jadwalData.hari} | ${jadwalData.jam}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = jadwalData.kelas,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = jadwalData.mataPelajaran,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Teacher Icon",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${jadwalData.namaGuru ?: "-"} | ${jadwalData.ruangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp),
                    enabled = !isLoading && !AppConfig.isDummyMode()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = if (AppConfig.isDummyMode())
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    enabled = !isLoading && !AppConfig.isDummyMode()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (AppConfig.isDummyMode())
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}



// 👥 MENU TAMBAHAN: ENTRI USER (CREATE USER - WITH API INTEGRATION!)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriUserPage(modifier: Modifier = Modifier) {
    // Form state
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("siswa") }
    var selectedKelas by remember { mutableStateOf("X RPL 1") } // Khusus siswa
    var selectedStatus by remember { mutableStateOf("aktif") }

    // Dropdown states
    var expandedRole by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // Password visibility
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation error states
    var namaError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var namaErrorMessage by remember { mutableStateOf("") }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }

    // API state management
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // User list from API
    val userList = remember { mutableStateListOf<UserItem>() }

    // Repository
    val repository = remember { UserRepository() }

    // Coroutine scope
    val scope = rememberCoroutineScope()

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Dropdown options
    val roleList = listOf(
        "siswa" to "Siswa",
        "kurikulum" to "Kurikulum",
        "kepala_sekolah" to "Kepala Sekolah",
        "admin" to "Administrator"
    )
    val kelasList = listOf("X RPL 1", "X RPL 2", "XI RPL 1", "XI RPL 2", "XII RPL 1", "XII RPL 2")
    val statusList = listOf("aktif" to "Aktif", "nonaktif" to "Nonaktif")

    // Load user list saat page dibuka
    LaunchedEffect(Unit) {
        isFetching = true
        repository.getUsers().onSuccess { userData ->
            userList.clear()
            userList.addAll(userData)
            isFetching = false
        }.onFailure { error ->
            errorMessage = "Gagal memuat user: ${error.message}"
            isFetching = false
        }
    }

    // Email validation function
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        // Validate nama
        if (nama.isBlank()) {
            namaError = true
            namaErrorMessage = "Nama tidak boleh kosong"
            isValid = false
        } else {
            namaError = false
            namaErrorMessage = ""
        }

        // Validate email
        if (email.isBlank()) {
            emailError = true
            emailErrorMessage = "Email tidak boleh kosong"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = true
            emailErrorMessage = "Format email tidak valid"
            isValid = false
        } else {
            emailError = false
            emailErrorMessage = ""
        }

        // Validate password
        if (password.isBlank()) {
            passwordError = true
            passwordErrorMessage = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 8) {
            passwordError = true
            passwordErrorMessage = "Password minimal 8 karakter"
            isValid = false
        } else {
            passwordError = false
            passwordErrorMessage = ""
        }

        return isValid
    }

    // Save user function with API call
    fun saveUser() {
        // Validate form
        if (!validateForm()) {
            errorMessage = "Mohon perbaiki input yang tidak valid"
            return
        }

        // Clear previous messages
        errorMessage = null
        successMessage = null

        // Set loading state
        isLoading = true

        // Create request object
        val request = UserRequest(
            nama = nama,
            email = email,
            password = password,
            role = selectedRole,
            kelas = if (selectedRole == "siswa") selectedKelas else null,
            status = selectedStatus
        )

        // Call API
        scope.launch {
            repository.createUser(request)
                .onSuccess { newUser ->
                    // Add to list
                    userList.add(0, newUser) // Add to top of list

                    // Show success message
                    successMessage = "User ${newUser.nama} berhasil dibuat!"

                    // Show snackbar
                    snackbarHostState.showSnackbar(
                        message = "✓ User berhasil dibuat",
                        duration = SnackbarDuration.Short
                    )

                    // Clear form
                    nama = ""
                    email = ""
                    password = ""
                    selectedRole = "siswa"
                    selectedKelas = "X RPL 1"
                    selectedStatus = "aktif"

                    // Reset validation errors
                    namaError = false
                    emailError = false
                    passwordError = false

                    // Reset loading
                    isLoading = false
                }
                .onFailure { error ->
                    // Show error message
                    errorMessage = error.message ?: "Gagal membuat user"

                    // Show snackbar
                    snackbarHostState.showSnackbar(
                        message = "✗ ${error.message}",
                        duration = SnackbarDuration.Long
                    )

                    // Reset loading
                    isLoading = false
                }
        }
    }

    // Refresh user list
    fun refreshUserList() {
        isFetching = true
        scope.launch {
            repository.getUsers().onSuccess { userData ->
                userList.clear()
                userList.addAll(userData)
                isFetching = false
            }.onFailure { error ->
                errorMessage = "Gagal memuat user: ${error.message}"
                isFetching = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // Header Admin
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add User Icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Manajemen User",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tambah dan kelola pengguna sistem",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = if (AppConfig.isDummyMode()) "Mode: DUMMY DATA (READ ONLY)" else "Mode: API LIVE - Full CRUD",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (AppConfig.isDummyMode())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error Message Card
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                            imageVector = Icons.Default.Delete,
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
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Success Message Card
            if (successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { successMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Form User Baru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Nama
                    OutlinedTextField(
                        value = nama,
                        onValueChange = {
                            nama = it
                            namaError = it.isBlank()
                            namaErrorMessage = if (it.isBlank()) "Nama tidak boleh kosong" else ""
                        },
                        label = { Text("Nama Lengkap *") },
                        placeholder = { Text("Masukkan nama lengkap") },
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name Icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = namaError,
                        supportingText = {
                            if (namaError) {
                                Text(
                                    text = namaErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = it.isBlank() || !isValidEmail(it)
                            emailErrorMessage = when {
                                it.isBlank() -> "Email tidak boleh kosong"
                                !isValidEmail(it) -> "Format email tidak valid"
                                else -> ""
                            }
                        },
                        label = { Text("Email *") },
                        placeholder = { Text("contoh@sekolah.com") },
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text(
                                    text = emailErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = it.length < 8
                            passwordErrorMessage = when {
                                it.isBlank() -> "Password tidak boleh kosong"
                                it.length < 8 -> "Password minimal 8 karakter"
                                else -> ""
                            }
                        },
                        label = { Text("Password *") },
                        placeholder = { Text("Minimal 8 karakter") },
                        enabled = !isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) {
                                Text(
                                    text = passwordErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Row: Role dan Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Role Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedRole,
                            onExpandedChange = { expandedRole = !expandedRole },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = roleList.find { it.first == selectedRole }?.second
                                    ?: "Siswa",
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                                label = { Text("Role *") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SupervisorAccount,
                                        contentDescription = "Role Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedRole,
                                onDismissRequest = { expandedRole = false }
                            ) {
                                roleList.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedRole = value
                                            expandedRole = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = statusList.find { it.first == selectedStatus }?.second
                                    ?: "Aktif",
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                label = { Text("Status") },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                statusList.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedStatus = value
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kelas Dropdown (hanya untuk Siswa)
                    if (selectedRole == "siswa") {
                        ExposedDropdownMenuBox(
                            expanded = expandedKelas,
                            onExpandedChange = { expandedKelas = !expandedKelas },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedKelas,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                label = { Text("Kelas (Khusus Siswa)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Class Icon"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedKelas,
                                onDismissRequest = { expandedKelas = false }
                            ) {
                                kelasList.forEach { kelas ->
                                    DropdownMenuItem(
                                        text = { Text(kelas) },
                                        onClick = {
                                            selectedKelas = kelas
                                            expandedKelas = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Save User Button
                    Button(
                        onClick = { saveUser() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading && !AppConfig.isDummyMode()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add User Icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (AppConfig.isDummyMode()) "Mode Dummy - Read Only" else "Simpan User",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            // User List Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar User (${userList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { refreshUserList() },
                    enabled = !isFetching
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isFetching && userList.isEmpty()) {
                // Loading state
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat user...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (userList.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(userList) { index, user ->
                        UserItemCard(
                            userData = user,
                            index = index + 1
                        )
                    }
                }
            } else {
                // Empty state
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
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "No Users",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada user",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Mulai dengan mengisi form di atas",
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

@Composable
fun UserItemCard(
    userData: UserItem,
    index: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(userData.status) {
                "aktif" -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User indicator
            Surface(
                modifier = Modifier.size(40.dp),
                color = when(userData.role.lowercase()) {
                    "admin" -> MaterialTheme.colorScheme.error
                    "kepala_sekolah" -> MaterialTheme.colorScheme.primary
                    "kurikulum" -> MaterialTheme.colorScheme.secondary
                    "siswa" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userData.nama,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = userData.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when(userData.role.lowercase()) {
                            "admin" -> MaterialTheme.colorScheme.errorContainer
                            "kepala_sekolah" -> MaterialTheme.colorScheme.primaryContainer
                            "kurikulum" -> MaterialTheme.colorScheme.secondaryContainer
                            "siswa" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = userData.getRoleDisplayName() + if (userData.kelas != null) " - ${userData.kelas}" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = when(userData.role.lowercase()) {
                                "admin" -> MaterialTheme.colorScheme.onErrorContainer
                                "kepala_sekolah" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "kurikulum" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "siswa" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        color = if (userData.isActive()) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (userData.isActive()) "Aktif" else "Nonaktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (userData.isActive()) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (userData.createdAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dibuat: ${userData.getFormattedCreatedAt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 📋 MENU 3: LIST (OVERVIEW SEMUA DATA + HISTORY)
@Composable
fun ListPageAdmin(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Sample comprehensive data
    val semuaJadwal = listOf(
        JadwalAdmin(1, "Senin", "X RPL 1", "Matematika", "Pak Ahmad Yusuf", "07:00", "08:30", "Lab 1", "Ganjil", "2024/2025"),
        JadwalAdmin(2, "Senin", "X RPL 2", "Bahasa Indonesia", "Bu Sari Dewi", "08:30", "10:00", "Kelas 10A", "Ganjil", "2024/2025"),
        JadwalAdmin(3, "Selasa", "XI RPL 1", "Pemrograman Web", "Pak Budi Santoso", "07:00", "08:30", "Lab Komputer", "Ganjil", "2024/2025"),
        JadwalAdmin(4, "Rabu", "XII RPL 1", "Basis Data", "Bu Maya Sari", "10:15", "11:45", "Lab 2", "Ganjil", "2024/2025"),
        JadwalAdmin(5, "Kamis", "XI RPL 2", "Algoritma", "Bu Rina Wati", "13:00", "14:30", "Kelas 11A", "Ganjil", "2024/2025"),
        JadwalAdmin(6, "Jumat", "XII RPL 2", "Pemrograman Mobile", "Pak Eko Prasetyo", "07:00", "08:30", "Lab Android", "Ganjil", "2024/2025")
    )

    val semuaUser = listOf(
        UserAdmin(1, "Ahmad Siswa", "ahmad@sekolah.com", "Siswa", "X RPL 1", "Aktif", "2024-01-10"),
        UserAdmin(2, "Sari Kurikulum", "sari@sekolah.com", "Kurikulum", null, "Aktif", "2024-01-08"),
        UserAdmin(3, "Budi Kepala", "budi@sekolah.com", "Kepala Sekolah", null, "Aktif", "2024-01-05"),
        UserAdmin(4, "Maya Admin", "maya@sekolah.com", "Admin", null, "Aktif", "2024-01-01"),
        UserAdmin(5, "Dedi Siswa", "dedi@sekolah.com", "Siswa", "XI RPL 2", "Nonaktif", "2024-01-12")
    )

    val historyPerubahan = listOf(
        "2024-01-15 10:30 - Admin Maya menambah jadwal Matematika X RPL 1",
        "2024-01-15 09:45 - Admin Maya mengubah jam Pemrograman Web dari 08:00 ke 07:00",
        "2024-01-14 14:20 - Admin Maya menghapus jadwal PKn XII RPL 2",
        "2024-01-14 11:15 - Admin Maya menambah user baru: Ahmad Siswa",
        "2024-01-13 16:40 - Admin Maya mengubah status user Dedi Siswa menjadi Nonaktif",
        "2024-01-13 13:25 - Admin Maya menambah jadwal Basis Data XI RPL 1"
    )

    // Filter data berdasarkan search
    val filteredJadwal = semuaJadwal.filter { jadwal ->
        searchQuery.isEmpty() ||
                jadwal.mataPelajaran.contains(searchQuery, ignoreCase = true) ||
                jadwal.guru.contains(searchQuery, ignoreCase = true) ||
                jadwal.kelas.contains(searchQuery, ignoreCase = true)
    }

    val filteredUser = semuaUser.filter { user ->
        searchQuery.isEmpty() ||
                user.nama.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.role.contains(searchQuery, ignoreCase = true)
    }

    val filteredHistory = historyPerubahan.filter { history ->
        searchQuery.isEmpty() || history.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Overview
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
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SupervisorAccount,
                    contentDescription = "Admin Dashboard Icon",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Admin Dashboard - Overview Sistem",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kelola semua data master sistem sekolah",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Total: ${semuaJadwal.size} jadwal, ${semuaUser.size} user",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Search Bar Global
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Global Search") },
            placeholder = { Text("Cari jadwal, user, atau riwayat perubahan...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatCard(
                title = "Total Jadwal",
                count = semuaJadwal.size,
                icon = Icons.Default.Schedule,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Total User",
                count = semuaUser.size,
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "User Aktif",
                count = semuaUser.count { it.status == "Aktif" },
                icon = Icons.Default.PersonAdd,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
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
                        text = "Semua Jadwal",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule Icon"
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Semua User",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Icon"
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = "History Icon"
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AdminJadwalOverview(filteredJadwal)
            1 -> AdminUserOverview(filteredUser)
            2 -> AdminHistoryOverview(filteredHistory)
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
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
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdminJadwalOverview(jadwalList: List<JadwalAdmin>) {
    if (jadwalList.isNotEmpty()) {
        Text(
            text = "Ditemukan ${jadwalList.size} jadwal",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(jadwalList) { index, jadwal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${jadwal.hari} | ${jadwal.jamMulai}-${jadwal.jamSelesai}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = jadwal.kelas,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = jadwal.mataPelajaran,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${jadwal.guru} | ${jadwal.ruangan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        EmptyStateCard("Tidak ditemukan jadwal", "Coba ubah kata kunci pencarian")
    }
}

@Composable
fun AdminUserOverview(userList: List<UserAdmin>) {
    if (userList.isNotEmpty()) {
        Text(
            text = "Ditemukan ${userList.size} user",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(userList) { index, user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when(user.status) {
                            "Aktif" -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = when(user.role) {
                                "Admin" -> MaterialTheme.colorScheme.error
                                "Kepala Sekolah" -> MaterialTheme.colorScheme.primary
                                "Kurikulum" -> MaterialTheme.colorScheme.secondary
                                "Siswa" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = user.nama,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = when(user.role) {
                                        "Admin" -> MaterialTheme.colorScheme.errorContainer
                                        "Kepala Sekolah" -> MaterialTheme.colorScheme.primaryContainer
                                        "Kurikulum" -> MaterialTheme.colorScheme.secondaryContainer
                                        "Siswa" -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = user.role + if (user.kelas != null) " - ${user.kelas}" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when(user.role) {
                                            "Admin" -> MaterialTheme.colorScheme.onErrorContainer
                                            "Kepala Sekolah" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "Kurikulum" -> MaterialTheme.colorScheme.onSecondaryContainer
                                            "Siswa" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Surface(
                                    color = if (user.status == "Aktif") MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = user.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (user.status == "Aktif") MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        EmptyStateCard("Tidak ditemukan user", "Coba ubah kata kunci pencarian")
    }
}

@Composable
fun AdminHistoryOverview(historyList: List<String>) {
    if (historyList.isNotEmpty()) {
        Text(
            text = "Riwayat Perubahan (${historyList.size} aktivitas)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(historyList) { index, history ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = history,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    } else {
        EmptyStateCard("Tidak ditemukan riwayat", "Coba ubah kata kunci pencarian")
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String
) {
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
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "No Results",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Previews - UPDATED dengan parameter default
@Preview(showBackground = true, name = "Entri Jadwal Page Preview")
@Composable
fun EntriJadwalPagePreview() {
    AplikasiMonitoringKelasTheme {
        EntriJadwalPage()
    }
}

@Preview(showBackground = true, name = "Ubah Jadwal Page Preview")
@Composable
fun UbahJadwalPagePreview() {
    AplikasiMonitoringKelasTheme {
        UbahJadwalPage()
    }
}

@Preview(showBackground = true, name = "List Admin Page Preview")
@Composable
fun ListAdminPagePreview() {
    AplikasiMonitoringKelasTheme {
        ListPageAdmin()
    }
}

@Preview(showBackground = true, name = "Admin Screen - Full")
@Composable
fun AdminScreenPreview() {
    AplikasiMonitoringKelasTheme {
        AdminScreen(
            userId = 1,
            userName = "Admin Preview",
            userEmail = "admin@sekolah.com",
            userRole = "admin",
            authToken = null
        )
    }
}

@Preview(
    showBackground = true,
    name = "Admin Screen - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AdminScreenDarkPreview() {
    AplikasiMonitoringKelasTheme {
        AdminScreen(
            userId = 1,
            userName = "Admin Dark Preview",
            userEmail = "admin@sekolah.com",
            userRole = "admin",
            authToken = null
        )
    }
}