package com.example.aplikasimonitoringkelas.data.remote

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client Singleton
 * Provides instance of ApiService untuk komunikasi dengan Laravel API Backend
 *
 * Base URL: http://10.0.2.2:8000/api/
 * - 10.0.2.2 adalah localhost untuk Android Emulator
 * - Untuk real device, ganti dengan IP komputer (contoh: http://192.168.1.100:8000/api/)
 */
object RetrofitClient {

    // ✅ DYNAMIC BASE URL: Bisa diubah tanpa rebuild APK
    private var applicationContext: Context? = null
    
    /**
     * Initialize RetrofitClient dengan Application Context
     * HARUS dipanggil di Application.onCreate() atau MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    
    /**
     * Get SharedPreferences untuk menyimpan BASE_URL
     */
    private fun getPreferences(): SharedPreferences? {
        return applicationContext?.getSharedPreferences("MonitoringKelasPrefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Default BASE_URL (fallback jika belum di-set di SharedPreferences)
     *
     * IMPORTANT:
     * - Emulator: gunakan http://10.0.2.2:8000/api/
     * - Real Device (sama WiFi): gunakan http://192.168.x.x:8000/api/
     *
     * Cara cek IP komputer:
     * - Windows: ipconfig
     * - Mac/Linux: ifconfig
     */
    private const val DEFAULT_BASE_URL = "http://192.168.40.15:8000/api/"
    
    /**
     * Get current BASE_URL (dari SharedPreferences atau default)
     */
    fun getBaseUrl(): String {
        return getPreferences()?.getString("BASE_URL", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }
    
    /**
     * Set BASE_URL baru (disimpan di SharedPreferences)
     * Setelah set, HARUS restart aplikasi atau refresh Retrofit instance
     */
    fun setBaseUrl(newBaseUrl: String) {
        getPreferences()?.edit()?.putString("BASE_URL", newBaseUrl)?.apply()
        // Force recreate retrofit instance
        _apiService = null
    }

    // ✅ Solusi sementara jika BuildConfig belum ter-generate
    private const val IS_DEBUG = true // Set false untuk production

    /**
     * Logging Interceptor untuk debug HTTP request/response
     * Level BODY akan menampilkan:
     * - Request URL, headers, body
     * - Response status code, headers, body
     *
     * Untuk production, ganti level ke NONE
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (IS_DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * OkHttpClient Configuration
     *
     * - Logging Interceptor: untuk debug network calls
     * - Connect Timeout: 30 detik untuk establish connection
     * - Read Timeout: 30 detik untuk receive response
     * - Write Timeout: 30 detik untuk send request
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Retrofit Instance (dinamis, dibangun ulang setiap kali BASE_URL berubah)
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API Service Instance (cached, tapi bisa di-recreate)
     */
    private var _apiService: ApiService? = null
    
    val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = createRetrofit().create(ApiService::class.java)
            }
            return _apiService!!
        }

    /**
     * Helper function untuk update base URL secara dinamis
     * Berguna jika ingin switch antara emulator dan real device
     *
     * @param newBaseUrl URL baru untuk API endpoint
     * @return ApiService dengan base URL yang baru
     */
    fun createService(newBaseUrl: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}