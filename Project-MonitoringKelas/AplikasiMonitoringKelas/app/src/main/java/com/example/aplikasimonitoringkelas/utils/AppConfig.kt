package com.example.aplikasimonitoringkelas.utils

/**
 * Application Configuration
 * Central configuration untuk manage app settings, API endpoints, dan feature flags
 *
 * Usage:
 * ```
 * if (AppConfig.USE_DUMMY_DATA) {
 *     // Load dummy data dari local
 * } else {
 *     // Fetch data dari Laravel API
 * }
 * ```
 */
object AppConfig {

    // ==================== FEATURE FLAGS ====================

    /**
     * Toggle antara Dummy Data dan Real API
     *
     * - true: Gunakan dummy data untuk development/testing tanpa backend
     * - false: Gunakan real API dari Laravel backend
     *
     * RECOMMENDED:
     * - Development: true (testing UI tanpa backend)
     * - Production: false (connect ke real API)
     */
    const val USE_DUMMY_DATA: Boolean = false

    /**
     * Debug Mode Flag
     *
     * - true: Enable logging, error details, debug features
     * - false: Disable logging untuk production
     *
     * Affects:
     * - HTTP logging interceptor level
     * - Error message verbosity
     * - Debug UI elements
     */
    const val DEBUG_MODE: Boolean = true


    // ==================== API CONFIGURATION ====================

    /**
     * Base URL untuk Laravel API Backend
     *
     * IMPORTANT - Choose based on your testing environment:
     *
     * 1. Android Emulator:
     *    - Use: "http://10.0.2.2:8000/api/"
     *    - 10.0.2.2 adalah alias untuk localhost di emulator
     *
     * 2. Real Android Device (sama WiFi dengan komputer):
     *    - Use: "http://192.168.x.x:8000/api/"
     *    - Ganti dengan IP address komputer Anda
     *    - Cara cek IP:
     *      Windows: ipconfig
     *      Mac/Linux: ifconfig
     *
     * 3. Production Server:
     *    - Use: "https://your-domain.com/api/"
     */

    /**
     * Base URL untuk Laravel API Backend
     *
     * PILIH SESUAI DEVICE:
     * - EMULATOR: http://10.0.2.2:8000/api/
     * - REAL DEVICE: http://192.168.1.4:8000/api/ (IP komputer kamu)
     * 
     * Untuk real device, server Laravel harus: php artisan serve --host=0.0.0.0 --port=8000
     * Untuk emulator, server Laravel: php artisan serve (biasa)
     */
    const val BASE_URL: String = "http://192.168.40.15:8000/api/"  // ✅ Untuk Real Device

    /**
     * Alternative Base URLs untuk berbagai environment
     */
    object ApiUrl {
        const val EMULATOR = "http://10.0.2.2:8000/api/"           // ✅ Untuk emulator
        const val LOCALHOST = "http://127.0.0.1:8000/api/"          // Untuk testing
        const val LOCAL_NETWORK = "http://192.168.1.7:8000/api/"   // Untuk real device
        const val PRODUCTION = "https://api.monitoringkelas.com/api/"
    }

    /**
     * HTTP Request Timeout Configuration (in seconds)
     *
     * - Connect Timeout: waktu untuk establish connection
     * - Read Timeout: waktu untuk receive response
     * - Write Timeout: waktu untuk send request
     */
    const val TIMEOUT_SECONDS: Long = 30


    // ==================== APP INFORMATION ====================

    /**
     * Application Version
     * Format: Major.Minor.Patch (Semantic Versioning)
     */
    const val APP_VERSION: String = "1.0.0"

    /**
     * Application Name
     */
    const val APP_NAME: String = "Aplikasi Monitoring Kelas"

    /**
     * Minimum required API level
     */
    const val MIN_API_LEVEL: Int = 29 // Android 10


    // ==================== PAGINATION & LIMITS ====================

    /**
     * Default page size untuk pagination
     */
    const val DEFAULT_PAGE_SIZE: Int = 20

    /**
     * Maximum items to cache locally
     */
    const val MAX_CACHE_SIZE: Int = 100


    // ==================== DATE & TIME FORMATS ====================

    /**
     * Standard date format untuk API communication
     * Format: YYYY-MM-DD (ISO 8601)
     */
    const val API_DATE_FORMAT: String = "yyyy-MM-dd"

    /**
     * Display date format untuk UI
     * Format: DD/MM/YYYY
     */
    const val DISPLAY_DATE_FORMAT: String = "dd/MM/yyyy"

    /**
     * Display time format untuk UI
     * Format: HH:mm
     */
    const val DISPLAY_TIME_FORMAT: String = "HH:mm"

    /**
     * Full datetime display format
     * Format: DD/MM/YYYY HH:mm
     */
    const val DISPLAY_DATETIME_FORMAT: String = "dd/MM/yyyy HH:mm"


    // ==================== VALIDATION RULES ====================

    /**
     * Minimum password length
     */
    const val MIN_PASSWORD_LENGTH: Int = 8

    /**
     * Email regex pattern
     */
    const val EMAIL_PATTERN: String = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"


    // ==================== SHARED PREFERENCES KEYS ====================

    /**
     * SharedPreferences file name
     */
    const val PREFS_NAME: String = "monitoring_kelas_prefs"

    /**
     * Keys untuk menyimpan data di SharedPreferences
     */
    object PrefsKeys {
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        const val IS_LOGGED_IN = "is_logged_in"
        const val LAST_LOGIN = "last_login"
    }


    // ==================== USER ROLES ====================

    /**
     * Available user roles
     */
    object UserRole {
        const val ADMIN = "admin"
        const val KEPALA_SEKOLAH = "kepala_sekolah"
        const val KURIKULUM = "kurikulum"
        const val SISWA = "siswa"
    }


    // ==================== STATUS VALUES ====================

    /**
     * Status Kehadiran
     */
    object StatusKehadiran {
        const val HADIR = "Hadir"
        const val SAKIT = "Sakit"
        const val IZIN = "Izin"
        const val ALPHA = "Alpha"

        fun getAll(): List<String> = listOf(HADIR, SAKIT, IZIN, ALPHA)
    }

    /**
     * Status Tugas
     */
    object StatusTugas {
        const val SELESAI = "Selesai"
        const val BELUM_SELESAI = "Belum Selesai"
        const val TERLAMBAT = "Terlambat"

        fun getAll(): List<String> = listOf(SELESAI, BELUM_SELESAI, TERLAMBAT)
    }

    /**
     * Hari dalam seminggu
     */
    object Hari {
        const val SENIN = "Senin"
        const val SELASA = "Selasa"
        const val RABU = "Rabu"
        const val KAMIS = "Kamis"
        const val JUMAT = "Jumat"
        const val SABTU = "Sabtu"
        const val MINGGU = "Minggu"

        fun getAll(): List<String> = listOf(
            SENIN, SELASA, RABU, KAMIS, JUMAT, SABTU, MINGGU
        )

        fun getWeekdays(): List<String> = listOf(
            SENIN, SELASA, RABU, KAMIS, JUMAT
        )
    }


    // ==================== UTILITY FUNCTIONS ====================

    /**
     * Check if app is using dummy data mode
     *
     * @return true if using dummy data, false if using real API
     */
    fun isDummyMode(): Boolean = USE_DUMMY_DATA

    /**
     * Check if app is in debug mode
     *
     * @return true if debug mode enabled
     */
    fun isDebugMode(): Boolean = DEBUG_MODE

    /**
     * Get current base URL
     * @return Base URL yang sedang digunakan
     */
    fun getBaseUrl(): String {
        return BASE_URL
    }

    /**
     * Get current base URL based on environment
     *
     * @return Current base URL string
     */
    fun getCurrentBaseUrl(): String = BASE_URL

    /**
     * Check if user role is admin
     *
     * @param role User role string
     * @return true if role is admin
     */
    fun isAdmin(role: String): Boolean = role == UserRole.ADMIN

    /**
     * Check if user role is kepala sekolah
     *
     * @param role User role string
     * @return true if role is kepala sekolah
     */
    fun isKepalaSekolah(role: String): Boolean = role == UserRole.KEPALA_SEKOLAH

    /**
     * Check if user role is kurikulum
     *
     * @param role User role string
     * @return true if role is kurikulum
     */
    fun isKurikulum(role: String): Boolean = role == UserRole.KURIKULUM

    /**
     * Check if user role is siswa
     *
     * @param role User role string
     * @return true if role is siswa
     */
    fun isSiswa(role: String): Boolean = role == UserRole.SISWA

    /**
     * Get app version with prefix
     *
     * @return Formatted version string (e.g., "v1.0.0")
     */
    fun getFormattedVersion(): String = "v$APP_VERSION"

    /**
     * Log configuration info (for debugging)
     */
    fun logConfig() {
        if (DEBUG_MODE) {
            println("========================================")
            println("APP CONFIGURATION")
            println("========================================")
            println("App Name: $APP_NAME")
            println("Version: $APP_VERSION")
            println("Dummy Mode: $USE_DUMMY_DATA")
            println("Debug Mode: $DEBUG_MODE")
            println("Base URL: $BASE_URL")
            println("Timeout: ${TIMEOUT_SECONDS}s")
            println("========================================")
        }
    }
}