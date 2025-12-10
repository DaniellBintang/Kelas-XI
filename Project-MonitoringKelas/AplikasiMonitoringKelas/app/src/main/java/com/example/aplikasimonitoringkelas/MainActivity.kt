package com.example.aplikasimonitoringkelas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.aplikasimonitoringkelas.data.remote.ApiConfig
import com.example.aplikasimonitoringkelas.data.remote.request.LoginRequest
import com.example.aplikasimonitoringkelas.data.remote.RetrofitClient
import com.example.aplikasimonitoringkelas.ui.theme.AplikasiMonitoringKelasTheme
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ INITIALIZE RetrofitClient dengan Context untuk dynamic BASE_URL
        RetrofitClient.initialize(this)

        // Log app config pada startup
        AppConfig.logConfig()

        // ✅ PERBAIKAN: Check jika dari logout, skip auto-login
        val fromLogout = intent.getBooleanExtra("FROM_LOGOUT", false)

        setContent {
            AplikasiMonitoringKelasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        skipAutoLogin = fromLogout // ✅ Pass flag ke LoginScreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier, skipAutoLogin: Boolean = false) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // SharedPreferences for session management
    val sharedPreferences = remember {
        context.getSharedPreferences("MonitoringKelasPrefs", Context.MODE_PRIVATE)
    }

    // State variables
    var selectedRole by remember { mutableStateOf("Siswa") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    // API states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val roles = listOf("Siswa", "Kurikulum", "Kepala Sekolah")

    // Email validation function
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Save user session to SharedPreferences
    fun saveUserSession(
        userId: Int,
        userName: String,
        userEmail: String,
        userRole: String,
        userKelas: String? = null, // ✅ TAMBAHKAN parameter
        token: String? = null
    ) {
        sharedPreferences.edit().apply {
            putInt("USER_ID", userId)
            putString("USER_NAME", userName)
            putString("USER_EMAIL", userEmail)
            putString("USER_ROLE", userRole)
            userKelas?.let { putString("USER_KELAS", it) } // ✅ SIMPAN KELAS
            putBoolean("IS_LOGGED_IN", true)
            token?.let { putString("AUTH_TOKEN", it) }
            putLong("LOGIN_TIMESTAMP", System.currentTimeMillis())
            apply()
        }

        // ✅ TAMBAHKAN LOG untuk debugging
        if (AppConfig.isDebugMode()) {
            android.util.Log.d("MainActivity", "=== SESSION SAVED ===")
            android.util.Log.d("MainActivity", "  USER_ID: $userId")
            android.util.Log.d("MainActivity", "  USER_NAME: $userName")
            android.util.Log.d("MainActivity", "  USER_EMAIL: $userEmail")
            android.util.Log.d("MainActivity", "  USER_ROLE: $userRole")
            android.util.Log.d("MainActivity", "  USER_KELAS: $userKelas") // ✅ LOG KELAS
            android.util.Log.d("MainActivity", "  AUTH_TOKEN: ${token?.take(10)}...")
        }
    }

    // Clear user session
    fun clearUserSession() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    // Navigation function with session save
    fun navigateToRoleActivity(
        userId: Int,
        userName: String,
        userEmail: String,
        userRole: String,
        userKelas: String? = null,
        token: String? = null
    ) {
        // ✅ PERBAIKAN: Pass userKelas ke saveUserSession
        saveUserSession(userId, userName, userEmail, userRole, userKelas, token) // ← TAMBAHKAN userKelas

        // ✅ TAMBAHKAN LOG sebelum navigate
        if (AppConfig.isDebugMode()) {
            android.util.Log.d("MainActivity", "=== NAVIGATING TO ACTIVITY ===")
            android.util.Log.d("MainActivity", "  userRole: $userRole")
            android.util.Log.d("MainActivity", "  userKelas: $userKelas") // ✅ LOG KELAS
        }

        val roleNormalized = userRole.lowercase()

        val intent = when {
            roleNormalized == "siswa" -> Intent(context, SiswaActivity::class.java)
            roleNormalized == "kurikulum" -> Intent(context, KurikulumActivity::class.java)
            roleNormalized == "kepala_sekolah" || roleNormalized == "kepala sekolah" ->
                Intent(context, KepalaSekolahActivity::class.java)
            roleNormalized == "admin" -> Intent(context, AdminActivity::class.java)
            else -> null
        }

        intent?.let {
            // Pass user data via Intent extras
            it.putExtra("USER_ID", userId)
            it.putExtra("USER_NAME", userName)
            it.putExtra("USER_EMAIL", userEmail)
            it.putExtra("USER_ROLE", userRole)
            userKelas?.let { kelas ->
                it.putExtra("USER_KELAS", kelas)
                // ✅ TAMBAHKAN LOG
                android.util.Log.d("MainActivity", "  Intent Extra USER_KELAS: $kelas")
            }
            token?.let { authToken -> it.putExtra("AUTH_TOKEN", authToken) }

            context.startActivity(it)

            // Finish MainActivity after navigate
            if (context is MainActivity) {
                context.finish()
            }
        } ?: run {
            Toast.makeText(context, "Role tidak dikenali: $userRole", Toast.LENGTH_SHORT).show()
        }
    }

    // Login function with API call (ENHANCED)
    fun performLogin() {
        // Clear previous errors
        errorMessage = null

        // Validasi form
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.isNotBlank()

        if (!isEmailValid || !isPasswordValid) {
            val errorMsg = when {
                !isEmailValid && password.isBlank() -> "Email tidak valid dan password tidak boleh kosong"
                !isEmailValid -> "Format email tidak valid"
                password.isBlank() -> "Password tidak boleh kosong"
                else -> "Lengkapi data dengan benar"
            }
            errorMessage = errorMsg
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            return
        }

        // Check if using Dummy Mode
        if (AppConfig.isDummyMode()) {
            // Dummy Mode: Bypass API, navigate directly
            navigateToRoleActivity(
                userId = 999,
                userName = "User Demo ($selectedRole)",
                userEmail = email,
                userRole = selectedRole.lowercase().replace(" ", "_"),
                userKelas = "XI RPL", // Default kelas untuk dummy
                token = "dummy_token_12345"
            )
            return
        }

        // Real API Mode
        isLoading = true

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(
                    email = email,
                    password = password
                )

                val apiService = ApiConfig.getApiService()
                val response = apiService.login(loginRequest)

                withContext(Dispatchers.Main) {
                    isLoading = false

                    if (response.isSuccessful) {
                        val loginResponse = response.body()

                        if (loginResponse?.success == true && loginResponse.data != null) {
                            val userData = loginResponse.data
                            val authToken = loginResponse.token

                            // ✅ TAMBAHKAN LOG untuk melihat data dari API
                            if (AppConfig.isDebugMode()) {
                                android.util.Log.d("MainActivity", "=== LOGIN RESPONSE ===")
                                android.util.Log.d("MainActivity", "  User ID: ${userData.id}")
                                android.util.Log.d("MainActivity", "  Nama: ${userData.nama}")
                                android.util.Log.d("MainActivity", "  Email: ${userData.email}")
                                android.util.Log.d("MainActivity", "  Role: ${userData.role}")
                                android.util.Log.d("MainActivity", "  Kelas: ${userData.kelas}") // ✅ CEK INI
                                android.util.Log.d("MainActivity", "  Status: ${userData.status}")
                                android.util.Log.d("MainActivity", "  Token: ${authToken?.take(20)}...")
                            }

                            // Validasi role sesuai dengan yang dipilih
                            val loginRole = userData.role.lowercase()
                            val selectedRoleNormalized = selectedRole.lowercase().replace(" ", "_")

                            // Allow login if role matches or if role is flexible
                            val isRoleValid = when {
                                loginRole == selectedRoleNormalized -> true
                                loginRole == "admin" -> true // Admin can access all roles
                                selectedRoleNormalized == "siswa" && loginRole == "siswa" -> true
                                else -> false
                            }

                            if (isRoleValid) {
                                // Login berhasil
                                Toast.makeText(
                                    context,
                                    "Login berhasil! Selamat datang ${userData.nama}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // ✅ PERBAIKAN: Pastikan kelas ter-pass
                                val userKelas = userData.kelas ?: "XI RPL 1" // Default fallback

                                // ✅ LOG sebelum navigate
                                if (AppConfig.isDebugMode()) {
                                    android.util.Log.d("MainActivity", "=== BEFORE NAVIGATE ===")
                                    android.util.Log.d("MainActivity", "  Passing userKelas: $userKelas")
                                }

                                // Navigate to activity based on actual role from API
                                navigateToRoleActivity(
                                    userId = userData.id,
                                    userName = userData.nama,
                                    userEmail = userData.email,
                                    userRole = userData.role,
                                    userKelas = userKelas, // ✅ Pass kelas yang sudah divalidasi
                                    token = authToken
                                )
                            } else {
                                errorMessage = "Role tidak sesuai. Anda login sebagai ${userData.role}, bukan $selectedRole"
                                showErrorDialog = true
                            }
                        } else {
                            errorMessage = loginResponse?.message ?: "Login gagal. Periksa email dan password Anda."
                            showErrorDialog = true
                        }
                    } else {
                        // HTTP error
                        val errorBody = response.errorBody()?.string()
                        errorMessage = when (response.code()) {
                            401 -> "Email atau password salah"
                            404 -> "Endpoint tidak ditemukan. Periksa BASE_URL di AppConfig"
                            500 -> "Server error. Silakan coba lagi nanti"
                            else -> "Error ${response.code()}: ${errorBody ?: response.message()}"
                        }
                        showErrorDialog = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false

                    errorMessage = when {
                        e.message?.contains("Unable to resolve host") == true ->
                            "Tidak dapat terhubung ke server. Periksa koneksi internet dan BASE_URL"
                        e.message?.contains("timeout") == true ->
                            "Koneksi timeout. Periksa koneksi internet Anda"
                        e is java.net.ConnectException ->
                            "Tidak dapat terhubung ke server ${AppConfig.getBaseUrl()}"
                        else ->
                            "Error: ${e.message}"
                    }
                    showErrorDialog = true
                }
            }
        }
    }

    // ✅ PERBAIKAN: Auto-login hanya jika TIDAK dari logout
    LaunchedEffect(Unit) {
        if (!skipAutoLogin) { // ← CHECK FLAG
            val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
            val savedUserId = sharedPreferences.getInt("USER_ID", -1)
            val savedUserName = sharedPreferences.getString("USER_NAME", "") ?: ""
            val savedUserEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""
            val savedUserRole = sharedPreferences.getString("USER_ROLE", "") ?: ""
            val savedUserKelas = sharedPreferences.getString("USER_KELAS", null)
            val savedToken = sharedPreferences.getString("AUTH_TOKEN", null)

            val loginTimestamp = sharedPreferences.getLong("LOGIN_TIMESTAMP", 0)
            val currentTime = System.currentTimeMillis()
            val sessionDuration = currentTime - loginTimestamp
            val maxSessionDuration = 24 * 60 * 60 * 1000 // 24 hours

            if (isLoggedIn && savedUserId != -1 && sessionDuration < maxSessionDuration) {
                Toast.makeText(
                    context,
                    "Melanjutkan sesi sebagai $savedUserName",
                    Toast.LENGTH_SHORT
                ).show()

                navigateToRoleActivity(
                    userId = savedUserId,
                    userName = savedUserName,
                    userEmail = savedUserEmail,
                    userRole = savedUserRole,
                    userKelas = savedUserKelas,
                    token = savedToken
                )
            } else if (isLoggedIn && sessionDuration >= maxSessionDuration) {
                // Session expired
                clearUserSession()
                Toast.makeText(
                    context,
                    "Sesi telah berakhir. Silakan login kembali",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // ✅ Jika dari logout, clear session sekali lagi (safety)
            sharedPreferences.edit().clear().apply()
        }
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
                    text = "Login Gagal",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(errorMessage ?: "")

                    if (AppConfig.isDebugMode()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "BASE_URL: ${AppConfig.getBaseUrl()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                    contentDescription = "Error Icon",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
// ✅ SESUDAH (FIX):
        Image(
            painter = painterResource(id = R.drawable.logo_sekolah), // ← Langsung pakai PNG
            contentDescription = "Logo Sekolah",
            modifier = Modifier.size(150.dp)
            // ❌ HAPUS colorFilter karena logo sudah berwarna
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Spinner
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && !isLoading },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                enabled = !isLoading,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                label = { Text("Role") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Role Icon"
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            selectedRole = role
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email TextField
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = it.isNotEmpty() && !isValidEmail(it)
            },
            label = { Text("Email") },
            placeholder = { Text("Masukkan email Anda") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError,
            enabled = !isLoading,
            supportingText = {
                if (emailError) {
                    Text(
                        text = "Format email tidak valid",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Masukkan password Anda") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading,
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button with Loading State
        Button(
            onClick = { performLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
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
                        text = "Loading...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = "LOGIN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info card untuk role & mode
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Role dipilih: $selectedRole",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                // Tampilkan mode (Dummy/API)
                if (AppConfig.isDebugMode()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mode: ${if (AppConfig.isDummyMode()) "Dummy Data" else "Real API"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Server: ${AppConfig.getBaseUrl()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Error message display (alternative to dialog)
        if (!showErrorDialog && errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen - Light")
@Composable
fun LoginScreenPreview() {
    AplikasiMonitoringKelasTheme {
        LoginScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Login Screen - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenDarkPreview() {
    AplikasiMonitoringKelasTheme {
        LoginScreen()
    }
}