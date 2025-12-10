package com.example.aplikasimonitoringkelas.data.repository

import android.util.Log
import com.example.aplikasimonitoringkelas.data.dummy.DummyData
import com.example.aplikasimonitoringkelas.data.remote.ApiConfig
import com.example.aplikasimonitoringkelas.data.remote.request.JadwalRequest
import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongResponse
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.delay

/**
 * Jadwal Repository
 *
 * Handles data source switching antara Dummy Data dan Real API
 * Implements Repository Pattern untuk centralize data access logic
 *
 * Features:
 * - GET: getJadwal(), getJadwalById()
 * - CREATE: createJadwal() (Admin only)
 * - UPDATE: updateJadwal() (Admin only)
 * - DELETE: deleteJadwal() (Admin only)
 * - MONITORING: getKelasKosong(), getStatistikKelasKosong() (Kepala Sekolah)
 *
 * Usage:
 * ```
 * val repository = JadwalRepository()
 * val result = repository.getJadwal(hari = "Senin", kelas = "XI RPL")
 *
 * result.onSuccess { jadwalList ->
 *     // Handle success
 * }.onFailure { exception ->
 *     // Handle error
 * }
 * ```
 */
class JadwalRepository {

    companion object {
        private const val TAG = "JadwalRepository"
        private const val FALLBACK_TO_DUMMY = true // Auto fallback ke dummy jika API error
    }

    private val apiService = ApiConfig.getApiService()

    // ==================== READ OPERATIONS ====================

    /**
     * Get Jadwal with optional filters
     *
     * @param hari Optional filter by hari (Senin-Minggu)
     * @param kelas Optional filter by kelas (XI RPL, XII RPL, X RPL)
     * @param guru Optional filter by nama guru
     * @param mataPelajaran Optional filter by mata pelajaran
     * @return Result<List<JadwalItem>> Success dengan data atau Failure dengan exception
     */
    suspend fun getJadwal(
        hari: String? = null,
        kelas: String? = null,
        guru: String? = null,
        mataPelajaran: String? = null
    ): Result<List<JadwalItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // MODE: Dummy Data
                getJadwalFromDummy(hari, kelas)
            } else {
                // MODE: Real API
                getJadwalFromApi(hari, kelas, guru, mataPelajaran)
            }
        } catch (e: Exception) {
            logError("getJadwal", e)

            // Fallback to dummy if API fails
            if (FALLBACK_TO_DUMMY && !AppConfig.isDummyMode()) {
                logInfo("Fallback to dummy data")
                getJadwalFromDummy(hari, kelas)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get Jadwal by ID
     *
     * @param id Jadwal ID
     * @return Result<JadwalItem> Single jadwal item
     */
    suspend fun getJadwalById(id: Int): Result<JadwalItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode
                val allJadwal = DummyData.getDummyJadwal()
                val jadwal = allJadwal.find { it.id == id }

                if (jadwal != null) {
                    logSuccess("getJadwalById from Dummy: ID=$id")
                    Result.success(jadwal)
                } else {
                    logError("getJadwalById", Exception("Jadwal with ID=$id not found"))
                    Result.failure(Exception("Jadwal tidak ditemukan"))
                }
            } else {
                // Real API
                logInfo("Fetching jadwal by ID: $id")
                val response = apiService.getJadwalById(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("getJadwalById from API: ID=$id")
                        Result.success(apiResponse.data)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Jadwal tidak ditemukan"
                        logError("getJadwalById", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("getJadwalById", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("getJadwalById", e)
            Result.failure(e)
        }
    }

    // ==================== CREATE OPERATION ====================

    /**
     * Create new Jadwal (Admin only)
     *
     * NOTE: Dummy mode tidak support create, akan return error
     *
     * @param request JadwalRequest dengan data jadwal baru
     * @return Result<JadwalItem> Created jadwal item
     */
    suspend fun createJadwal(request: JadwalRequest): Result<JadwalItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode tidak support create
                logInfo("Create blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur CREATE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                // Create via API
                logInfo("Creating jadwal: ${request.mataPelajaran} - ${request.kelas}")
                val response = apiService.createJadwal(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("createJadwal: ${apiResponse.data.mataPelajaran} berhasil dibuat")
                        Result.success(apiResponse.data)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Gagal membuat jadwal"
                        logError("createJadwal", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("createJadwal", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("createJadwal", e)
            Result.failure(e)
        }
    }

    // ==================== UPDATE OPERATION ====================

    /**
     * Update existing Jadwal (Admin only)
     *
     * NOTE: Dummy mode tidak support update, akan return error
     *
     * @param id Jadwal ID to update
     * @param request JadwalRequest dengan data update
     * @return Result<JadwalItem> Updated jadwal item
     */
    suspend fun updateJadwal(id: Int, request: JadwalRequest): Result<JadwalItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode tidak support update
                logInfo("Update blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur UPDATE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                // Update via API
                logInfo("Updating jadwal ID $id: ${request.mataPelajaran}")
                val response = apiService.updateJadwal(id, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("updateJadwal: ID $id - ${apiResponse.data.mataPelajaran} berhasil diupdate")
                        Result.success(apiResponse.data)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Gagal update jadwal"
                        logError("updateJadwal", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("updateJadwal", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("updateJadwal", e)
            Result.failure(e)
        }
    }

    // ==================== DELETE OPERATION ====================

    /**
     * Delete Jadwal (Admin only)
     *
     * NOTE: Dummy mode tidak support delete, akan return error
     *
     * @param id Jadwal ID to delete
     * @return Result<String> Success message jika berhasil
     */
    suspend fun deleteJadwal(id: Int): Result<String> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode tidak support delete
                logInfo("Delete blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur DELETE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                // Delete via API
                logInfo("Deleting jadwal ID $id")
                val response = apiService.deleteJadwal(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val message = apiResponse.message ?: "Jadwal berhasil dihapus"
                        logSuccess("deleteJadwal: ID $id - $message")
                        Result.success(message)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Gagal hapus jadwal"
                        logError("deleteJadwal", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("deleteJadwal", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("deleteJadwal", e)
            Result.failure(e)
        }
    }

    // ==================== MONITORING OPERATIONS (Kepala Sekolah) ====================

    // ✅ ADD THIS NEW METHOD
    /**
     * Get kelas kosong (Empty classes)
     * Special feature for Kepala Sekolah to monitor classes without teachers
     *
     * @param hari Filter by day (optional)
     * @param tanggal Filter by date (optional, format: YYYY-MM-DD)
     * @param kelas Filter by class (optional)
     * @return Result<KelasKosongResponse> with count and list of empty classes
     */
    suspend fun getKelasKosong(
        hari: String? = null,
        tanggal: String? = null,
        kelas: String? = null
    ): Result<KelasKosongResponse> {
        return try {
            if (AppConfig.isDummyMode()) {
                // MODE: Dummy Data
                getKelasKosongFromDummy(hari, kelas)
            } else {
                // MODE: Real API
                getKelasKosongFromApi(hari, tanggal, kelas)
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ getKelasKosong failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get kelas kosong from Dummy Data
     */
    private suspend fun getKelasKosongFromDummy(
        hari: String?,
        kelas: String?
    ): Result<KelasKosongResponse> {
        delay(500) // Simulate network delay

        var jadwalList = DummyData.getDummyJadwal()

        // Filter by hari
        if (hari != null) {
            jadwalList = jadwalList.filter { it.hari.equals(hari, ignoreCase = true) }
        }

        // Filter by kelas
        if (kelas != null) {
            jadwalList = jadwalList.filter { it.kelas.equals(kelas, ignoreCase = true) }
        }

        // Filter only kelas kosong (no teacher assigned)
        val kelasKosong = jadwalList.filter {
            it.namaGuru.isNullOrBlank() || it.kodeGuru.isNullOrBlank()
        }

        val response = KelasKosongResponse(
            success = true,
            message = if (kelasKosong.isEmpty()) {
                "Tidak ada kelas kosong"
            } else {
                "Ditemukan ${kelasKosong.size} kelas kosong${if (hari != null) " pada hari $hari" else ""}"
            },
            jumlahKelasKosong = kelasKosong.size,
            data = kelasKosong
        )

        Log.d(TAG, "✓ getKelasKosong from Dummy: ${kelasKosong.size} empty classes found")
        return Result.success(response)
    }

    /**
     * Get kelas kosong from Real API
     */
    private suspend fun getKelasKosongFromApi(
        hari: String?,
        tanggal: String?,
        kelas: String?
    ): Result<KelasKosongResponse> {
        val response = apiService.getKelasKosong(
            hari = hari,
            tanggal = tanggal,
            kelas = kelas
        )

        return if (response.isSuccessful && response.body() != null) {
            val kelasKosongResponse = response.body()!!

            if (kelasKosongResponse.success) {
                Log.d(TAG, "✓ getKelasKosong from API: ${kelasKosongResponse.jumlahKelasKosong} empty classes found")
                Result.success(kelasKosongResponse)
            } else {
                val errorMsg = kelasKosongResponse.message ?: "Gagal mengambil data kelas kosong"
                Log.e(TAG, "✗ getKelasKosong API error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } else {
            val errorMsg = "Gagal mengambil data kelas kosong: ${response.code()}"
            Log.e(TAG, "✗ getKelasKosong HTTP error: $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Get Statistik Kelas Kosong
     * Dashboard untuk Kepala Sekolah
     *
     * @return Result<Any> Statistical data about empty classes
     */
    suspend fun getStatistikKelasKosong(): Result<Map<String, Any>> {
        return try {
            if (AppConfig.isDummyMode()) {
                delay(500) // Simulate network delay

                val allJadwal = DummyData.jadwalMap.values.flatten()
                val kelasKosong = allJadwal.filter {
                    it.namaGuru.isNullOrEmpty() || it.namaGuru == "-"
                }

                val stats = mapOf(
                    "total_kelas_kosong" to kelasKosong.size,
                    "kelas_kosong_hari_ini" to kelasKosong.filter { it.hari == "Senin" }.size,
                    "hari_ini" to "Senin",
                    "message" to "Data statistik dummy untuk testing"
                )

                logSuccess("getStatistikKelasKosong from Dummy: ${kelasKosong.size} total")
                Result.success(stats)
            } else {
                // Fetch from API
                val response = apiService.getStatistikKelasKosong()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Convert Any to Map if needed
                        val stats = when (val data = apiResponse.data) {
                            is Map<*, *> -> data as Map<String, Any>
                            else -> mapOf("data" to data)
                        }
                        logSuccess("getStatistikKelasKosong from API")
                        Result.success(stats)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal mengambil statistik"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("getStatistikKelasKosong", e)
            Result.failure(e)
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Get Jadwal from Dummy Data
     */
    private suspend fun getJadwalFromDummy(
        hari: String?,
        kelas: String?
    ): Result<List<JadwalItem>> {
        // Simulate network delay
        delay(500)

        val jadwalList = DummyData.getJadwalByHariKelas(hari, kelas)
        logInfo("Loaded ${jadwalList.size} items from Dummy Data (hari=$hari, kelas=$kelas)")

        return Result.success(jadwalList)
    }

    /**
     * Get Jadwal from Real API
     */
    private suspend fun getJadwalFromApi(
        hari: String?,
        kelas: String?,
        guru: String?,
        mataPelajaran: String?
    ): Result<List<JadwalItem>> {
        val response = apiService.getJadwal(hari, kelas, guru, mataPelajaran)

        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                val jadwalList = apiResponse.data ?: emptyList()
                logSuccess("Loaded ${jadwalList.size} items from API (hari=$hari, kelas=$kelas)")
                Result.success(jadwalList)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Data tidak tersedia"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
        }
    }

    // ==================== LOGGING HELPERS ====================

    private fun logInfo(message: String) {
        if (AppConfig.isDebugMode()) {
            Log.i(TAG, message)
        }
    }

    private fun logSuccess(message: String) {
        if (AppConfig.isDebugMode()) {
            Log.d(TAG, "✓ $message")
        }
    }

    private fun logError(method: String, exception: Exception) {
        if (AppConfig.isDebugMode()) {
            Log.e(TAG, "✗ $method failed: ${exception.message}", exception)
        }
    }

    // ==================== JADWAL DENGAN STATUS GURU ====================

    /**
     * ✅ BARU: Get Jadwal dengan Info Status Guru (Izin/Pengganti)
     * Endpoint khusus untuk Siswa Activity - menampilkan jadwal dengan info:
     * - Guru yang sedang izin
     * - Guru pengganti yang ditunjuk
     *
     * @param hari Filter by hari (Senin-Minggu)
     * @param kelas Filter by kelas (XI RPL 1, etc)
     * @param tanggal Optional tanggal (default: hari ini)
     * @return Result<List<JadwalItem>> dengan info statusGuru, infoIzin, guruPengganti
     */
    suspend fun getJadwalDenganStatusGuru(
        hari: String? = null,
        kelas: String? = null,
        tanggal: String? = null
    ): Result<List<JadwalItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode: Return jadwal biasa (tanpa info izin)
                logInfo("getJadwalDenganStatusGuru: Dummy mode - returning normal jadwal")
                getJadwalFromDummy(hari, kelas)
            } else {
                // Real API
                logInfo("Fetching jadwal dengan status guru: hari=$hari, kelas=$kelas, tanggal=$tanggal")
                val response = apiService.getJadwalDenganStatusGuru(hari, kelas, tanggal)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val jadwalList = apiResponse.data ?: emptyList()
                        logSuccess("getJadwalDenganStatusGuru: Loaded ${jadwalList.size} items")
                        
                        // Log statistik jika debug mode
                        if (AppConfig.isDebugMode()) {
                            val guruIzinCount = jadwalList.count { it.statusGuru == "izin" }
                            val adaPenggantiCount = jadwalList.count { it.guruPengganti != null }
                            Log.d(TAG, "  → Guru izin: $guruIzinCount, Ada pengganti: $adaPenggantiCount")
                        }
                        
                        Result.success(jadwalList)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Gagal mengambil jadwal"
                        logError("getJadwalDenganStatusGuru", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("getJadwalDenganStatusGuru", Exception(errorMsg))
                    
                    // Fallback ke jadwal biasa jika endpoint baru error
                    if (FALLBACK_TO_DUMMY) {
                        logInfo("Fallback to normal getJadwal")
                        getJadwalFromApi(hari, kelas, null, null)
                    } else {
                        Result.failure(Exception(errorMsg))
                    }
                }
            }
        } catch (e: Exception) {
            logError("getJadwalDenganStatusGuru", e)
            
            // Fallback ke jadwal biasa
            if (FALLBACK_TO_DUMMY) {
                logInfo("Exception fallback to normal getJadwal")
                getJadwal(hari, kelas)
            } else {
                Result.failure(e)
            }
        }
    }
}