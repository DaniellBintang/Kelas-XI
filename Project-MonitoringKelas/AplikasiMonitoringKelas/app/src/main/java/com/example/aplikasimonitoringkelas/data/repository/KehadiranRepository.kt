package com.example.aplikasimonitoringkelas.data.repository

import android.util.Log
import com.example.aplikasimonitoringkelas.data.dummy.DummyData
import com.example.aplikasimonitoringkelas.data.remote.RetrofitClient
import com.example.aplikasimonitoringkelas.data.remote.RetrofitClient.apiService
import com.example.aplikasimonitoringkelas.data.remote.request.KehadiranRequest
import com.example.aplikasimonitoringkelas.data.remote.response.KehadiranItem
import com.example.aplikasimonitoringkelas.data.remote.request.AssignGuruPenggantiRequest
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.delay

/**
 * Kehadiran Repository
 *
 * Handles data source switching antara Dummy Data dan Real API
 * Implements Repository Pattern untuk centralize data access logic
 *
 * Usage:
 * ```
 * val repository = KehadiranRepository()
 * val result = repository.getKehadiran(userId = 4)
 *
 * result.onSuccess { kehadiranList ->
 *     // Handle success
 * }.onFailure { exception ->
 *     // Handle error
 * }
 * ```
 */
class KehadiranRepository {

    companion object {
        private const val TAG = "KehadiranRepository"
        private const val FALLBACK_TO_DUMMY = true // Auto fallback ke dummy jika API error
    }

    /**
     * Get Kehadiran by User ID
     *
     * @param userId User ID untuk filter kehadiran (optional, null = semua kehadiran)
     * @return Result<List<KehadiranItem>> Success dengan data atau Failure dengan exception
     */

    suspend fun getKehadiran(userId: Int? = null): Result<List<KehadiranItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // MODE: Dummy Data
                getKehadiranFromDummy(userId)
            } else {
                // MODE: Real API
                getKehadiranFromApi(userId)
            }
        } catch (e: Exception) {
            logError("getKehadiran", e)

            // Fallback to dummy if API fails
            if (FALLBACK_TO_DUMMY && !AppConfig.isDummyMode()) {
                logInfo("Fallback to dummy data")
                getKehadiranFromDummy(userId)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get Kehadiran/Laporan Guru by Kelas
     *
     * @param kelas Nama kelas untuk filter laporan (contoh: "XI RPL 1")
     * @return Result<List<KehadiranItem>> List laporan guru untuk kelas tertentu
     */
    suspend fun getKehadiranByKelas(kelas: String): Result<List<KehadiranItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Filter dummy data by kelas
                delay(500)
                val filtered = DummyData.kehadiranList.filter { it.kelas == kelas }
                logSuccess("getKehadiranByKelas: Found ${filtered.size} records for kelas $kelas")
                Result.success(filtered)
            } else {
                // Fetch from API with kelas filter
                val response = RetrofitClient.apiService.getKehadiranByKelas(kelas)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("getKehadiranByKelas: ${apiResponse.data.size} records for $kelas")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Data tidak ditemukan"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("getKehadiranByKelas", e)
            
            // Fallback to dummy if API fails
            if (FALLBACK_TO_DUMMY && !AppConfig.isDummyMode()) {
                delay(500)
                val filtered = DummyData.kehadiranList.filter { it.kelas == kelas }
                logInfo("Fallback to dummy data: ${filtered.size} records")
                Result.success(filtered)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get Kehadiran by ID
     *
     * @param id Kehadiran ID
     * @return Result<KehadiranItem> Single kehadiran item
     */
    suspend fun getKehadiranById(id: Int): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Find from dummy data
                val kehadiran = DummyData.kehadiranList.find { it.id == id }

                if (kehadiran != null) {
                    delay(300) // Simulate network delay
                    Result.success(kehadiran)
                } else {
                    Result.failure(Exception("Kehadiran dengan ID $id tidak ditemukan"))
                }
            } else {
                // Fetch from API
                val response = RetrofitClient.apiService.getKehadiranById(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("getKehadiranById: ${apiResponse.data.mataPelajaran} - ${apiResponse.data.status}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Data tidak ditemukan"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("getKehadiranById", e)
            Result.failure(e)
        }
    }

    /**
     * Create new Kehadiran
     *
     * NOTE: Dummy mode tidak support create, akan return error
     *
     * @param request KehadiranRequest dengan data kehadiran baru
     * @return Result<KehadiranItem> Created kehadiran item
     */
    suspend fun createKehadiran(request: KehadiranRequest): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode tidak support create
                Result.failure(
                    Exception("Create tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                // Create via API
                val response = RetrofitClient.apiService.createKehadiran(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("createKehadiran: ${apiResponse.data.mataPelajaran} - ${apiResponse.data.status}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal membuat kehadiran"))
                    }
                } else {
                    // Handle validation errors
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: ${errorBody ?: response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("createKehadiran", e)
            Result.failure(e)
        }
    }

    /**
     * Get Kelas Kosong berdasarkan Kehadiran Guru
     * Digunakan oleh Kepala Sekolah dan Kurikulum untuk monitoring
     *
     * @param tanggal Filter by tanggal (YYYY-MM-DD format)
     * @param kelas Filter by kelas (optional)
     * @param status Filter by status (Telat, Tidak Hadir, Izin)
     * @return Result<KelasKosongKehadiranResponse>
     */
    suspend fun getKelasKosongKehadiran(
        tanggal: String? = null,
        kelas: String? = null,
        status: String? = null
    ): Result<com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongKehadiranResponse> {
        return try {
            if (AppConfig.isDummyMode()) {
                // Dummy mode - return empty atau mock data
                delay(500)
                val mockResponse = com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongKehadiranResponse(
                    success = true,
                    message = "Mock data - Dummy mode active",
                    jumlahKelasKosong = 0,
                    filter = null,
                    data = emptyList()
                )
                logInfo("getKelasKosongKehadiran from Dummy: 0 kelas kosong")
                Result.success(mockResponse)
            } else {
                // Real API mode
                logInfo("Fetching kelas kosong: tanggal=$tanggal, kelas=$kelas, status=$status")

                val response = apiService.getKelasKosongKehadiran(
                    tanggal = tanggal,
                    kelas = kelas,
                    status = status
                )

                if (response.isSuccessful && response.body() != null) {
                    val kelasKosongResponse = response.body()!!

                    if (kelasKosongResponse.success) {
                        logSuccess("getKelasKosongKehadiran: ${kelasKosongResponse.jumlahKelasKosong} kelas kosong")
                        Result.success(kelasKosongResponse)
                    } else {
                        val errorMsg = kelasKosongResponse.message ?: "Gagal mengambil data kelas kosong"
                        logError("getKelasKosongKehadiran", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("getKelasKosongKehadiran", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("getKelasKosongKehadiran", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing Kehadiran
     *
     * @param id Kehadiran ID to update
     * @param request KehadiranRequest dengan data update
     * @return Result<KehadiranItem> Updated kehadiran item
     */
    suspend fun updateKehadiran(id: Int, request: KehadiranRequest): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Update tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                val response = RetrofitClient.apiService.updateKehadiran(id, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("updateKehadiran: ${apiResponse.data.mataPelajaran} - ${apiResponse.data.status}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal update kehadiran"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: ${errorBody ?: response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("updateKehadiran", e)
            Result.failure(e)
        }
    }

    /**
     * Delete Kehadiran
     *
     * @param id Kehadiran ID to delete
     * @return Result<Boolean> True if success
     */
    suspend fun deleteKehadiran(id: Int): Result<Boolean> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Delete tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                val response = RetrofitClient.apiService.deleteKehadiran(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        logSuccess("deleteKehadiran: ID $id")
                        Result.success(true)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal hapus kehadiran"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("deleteKehadiran", e)
            Result.failure(e)
        }
    }

    /**
     * Assign guru pengganti untuk kehadiran
     * PUT /api/kehadiran/{id}/assign-pengganti
     */
    suspend fun assignGuruPengganti(
        kehadiranId: Int,
        request: AssignGuruPenggantiRequest
    ): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Assign guru pengganti tidak tersedia di Dummy Mode. Aktifkan Real API.")
                )
            } else {
                logInfo("Assigning guru pengganti untuk kehadiran ID: $kehadiranId")
                val response = apiService.assignGuruPengganti(kehadiranId, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("assignGuruPengganti: ${apiResponse.data.namaGuruPengganti}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal assign guru pengganti"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()}: ${errorBody ?: response.message()}"
                    logError("assignGuruPengganti", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("assignGuruPengganti", e)
            Result.failure(e)
        }
    }

    /**
     * Hapus guru pengganti (batalkan assignment)
     * DELETE /api/kehadiran/{id}/hapus-pengganti
     */
    suspend fun hapusGuruPengganti(kehadiranId: Int): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Hapus guru pengganti tidak tersedia di Dummy Mode.")
                )
            } else {
                logInfo("Menghapus guru pengganti untuk kehadiran ID: $kehadiranId")
                val response = apiService.hapusGuruPengganti(kehadiranId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("hapusGuruPengganti: Success")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal hapus guru pengganti"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()}: ${errorBody ?: response.message()}"
                    logError("hapusGuruPengganti", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("hapusGuruPengganti", e)
            Result.failure(e)
        }
    }

    /**
     * Get Kehadiran Summary (Count by Status)
     * Helper function untuk statistik kehadiran
     *
     * @param userId User ID
     * @return Map<String, Int> Status count (Hadir: 10, Sakit: 2, etc.)
     */
    suspend fun getKehadiranSummary(userId: Int): Result<Map<String, Int>> {
        return try {
            getKehadiran(userId).fold(
                onSuccess = { kehadiranList ->
                    val summary = kehadiranList.groupBy { it.status }
                        .mapValues { it.value.size }
                    Result.success(summary)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            logError("getKehadiranSummary", e)
            Result.failure(e)
        }
    }


    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Get Kehadiran from Dummy Data
     */
    private suspend fun getKehadiranFromDummy(userId: Int?): Result<List<KehadiranItem>> {
        // Simulate network delay
        delay(500)

        val kehadiranList = DummyData.getKehadiranByUserId(userId)
        logInfo("Loaded ${kehadiranList.size} items from Dummy Data (userId=$userId)")

        return Result.success(kehadiranList)
    }

    /**
     * Get Kehadiran from Real API
     */
    private suspend fun getKehadiranFromApi(userId: Int?): Result<List<KehadiranItem>> {
        val response = RetrofitClient.apiService.getKehadiran(userId)

        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                val kehadiranList = apiResponse.data ?: emptyList()
                logSuccess("Loaded ${kehadiranList.size} items from API (userId=$userId)")
                Result.success(kehadiranList)
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
}