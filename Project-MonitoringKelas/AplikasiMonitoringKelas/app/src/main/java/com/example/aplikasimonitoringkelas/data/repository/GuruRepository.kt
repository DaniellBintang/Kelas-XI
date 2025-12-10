package com.example.aplikasimonitoringkelas.data.repository

import android.util.Log
import com.example.aplikasimonitoringkelas.data.remote.ApiConfig
import com.example.aplikasimonitoringkelas.data.remote.request.LaporanGuruRequest
import com.example.aplikasimonitoringkelas.data.remote.response.GuruItem
import com.example.aplikasimonitoringkelas.data.remote.response.KehadiranItem
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.delay

/**
 * Repository for Guru operations
 *
 * Handles guru data fetching and laporan guru dari siswa
 *
 * Features:
 * - GET: getGuru(), getGuruById()
 * - REPORT: laporkanGuru() (Siswa melaporkan kehadiran guru)
 *
 * Usage:
 * ```
 * val repository = GuruRepository()
 * val result = repository.getGuru(status = "Aktif")
 *
 * result.onSuccess { guruList ->
 *     // Handle success
 * }.onFailure { exception ->
 *     // Handle error
 * }
 * ```
 */
class GuruRepository {

    companion object {
        private const val TAG = "GuruRepository"
    }

    private val apiService = ApiConfig.getApiService()

    // ==================== READ OPERATIONS ====================

    /**
     * Get all guru with optional filters
     *
     * @param status Filter by status (Aktif, Cuti, Nonaktif)
     * @param mataPelajaran Filter by mata pelajaran
     * @param search Search by name
     * @return Result<List<GuruItem>> Success dengan data atau Failure dengan exception
     */
    suspend fun getGuru(
        status: String? = null,
        mataPelajaran: String? = null,
        search: String? = null
    ): Result<List<GuruItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // MODE: Dummy Data
                delay(500) // Simulate network delay
                val guruList = getDummyGuru()
                logSuccess("getGuru from Dummy: ${guruList.size} items")
                Result.success(guruList)
            } else {
                // MODE: Real API
                logInfo("Fetching guru (status=$status, mataPelajaran=$mataPelajaran, search=$search)")
                val response = apiService.getGuru(status, mataPelajaran, search)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val guruList = apiResponse.data ?: emptyList()
                        logSuccess("getGuru from API: ${guruList.size} items")
                        Result.success(guruList)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Data tidak tersedia"
                        logError("getGuru", Exception(errorMsg))
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    logError("getGuru", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("getGuru", e)
            Result.failure(e)
        }
    }

    /**
     * Get guru by ID
     *
     * @param id Guru ID
     * @return Result<GuruItem> Single guru item
     */
    suspend fun getGuruById(id: Int): Result<GuruItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                delay(500)
                val guru = getDummyGuru().find { it.id == id }
                if (guru != null) {
                    logSuccess("getGuruById from Dummy: ${guru.nama}")
                    Result.success(guru)
                } else {
                    Result.failure(Exception("Guru dengan ID $id tidak ditemukan"))
                }
            } else {
                logInfo("Fetching guru ID $id")
                val response = apiService.getGuruById(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("getGuruById from API: ${apiResponse.data.nama}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Data tidak ditemukan"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("getGuruById", e)
            Result.failure(e)
        }
    }

    // ==================== REPORT OPERATION (Siswa) ====================

    /**
     * Siswa melaporkan kehadiran guru (telat/tidak hadir)
     *
     * NOTE: Dummy mode tidak support create, akan return error
     *
     * @param request LaporanGuruRequest dengan data laporan
     * @return Result<KehadiranItem> Created kehadiran item
     */
    suspend fun laporkanGuru(request: LaporanGuruRequest): Result<KehadiranItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Laporan tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                logInfo("Melaporkan guru: ${request.namaGuru} - ${request.status}")
                val response = apiService.laporkanGuru(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("laporkanGuru: ${apiResponse.data.mataPelajaran} - ${apiResponse.data.status}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal mengirim laporan"))
                    }
                } else {
                    // Handle validation errors
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "HTTP ${response.code()}: ${errorBody ?: response.message()}"
                    logError("laporkanGuru", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("laporkanGuru", e)
            Result.failure(e)
        }
    }

    // ==================== DUMMY DATA ====================

    /**
     * Dummy data for testing
     */
    private fun getDummyGuru(): List<GuruItem> {
        return listOf(
            GuruItem(
                id = 1,
                kodeGuru = "G001",
                nama = "Dr. Ahmad Wijaya, S.Pd., M.Pd.",
                mataPelajaran = "Matematika",
                email = "ahmad.wijaya@school.com",
                noTelepon = "081234567890",
                alamat = "Jl. Pendidikan No. 123, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 2,
                kodeGuru = "G002",
                nama = "Siti Nurhaliza, S.Pd.",
                mataPelajaran = "Bahasa Indonesia",
                email = "siti.nurhaliza@school.com",
                noTelepon = "081234567891",
                alamat = "Jl. Merdeka No. 45, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 3,
                kodeGuru = "G003",
                nama = "Budi Santoso, S.Kom., M.T.",
                mataPelajaran = "Informatika",
                email = "budi.santoso@school.com",
                noTelepon = "081234567892",
                alamat = "Jl. Teknologi No. 67, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 4,
                kodeGuru = "G004",
                nama = "Dewi Lestari, S.Si.",
                mataPelajaran = "Fisika",
                email = "dewi.lestari@school.com",
                noTelepon = "081234567893",
                alamat = "Jl. Sains No. 89, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 5,
                kodeGuru = "G005",
                nama = "Eko Prasetyo, S.Pd.",
                mataPelajaran = "Bahasa Inggris",
                email = "eko.prasetyo@school.com",
                noTelepon = "081234567894",
                alamat = "Jl. Internasional No. 12, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 6,
                kodeGuru = "G006",
                nama = "Farah Diba, S.Pd.",
                mataPelajaran = "Kimia",
                email = "farah.diba@school.com",
                noTelepon = "081234567895",
                alamat = "Jl. Laboratorium No. 34, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 7,
                kodeGuru = "G007",
                nama = "Hendra Gunawan, S.Pd.",
                mataPelajaran = "Sejarah",
                email = "hendra.gunawan@school.com",
                noTelepon = "081234567896",
                alamat = "Jl. Nusantara No. 56, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 8,
                kodeGuru = "G008",
                nama = "Linda Wati, S.Pd.",
                mataPelajaran = "Biologi",
                email = "linda.wati@school.com",
                noTelepon = "081234567897",
                alamat = "Jl. Alam No. 78, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 9,
                kodeGuru = "G009",
                nama = "Muhammad Rizki, S.Pd.",
                mataPelajaran = "Pendidikan Jasmani",
                email = "muhammad.rizki@school.com",
                noTelepon = "081234567898",
                alamat = "Jl. Olahraga No. 90, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            ),
            GuruItem(
                id = 10,
                kodeGuru = "G010",
                nama = "Ani Suryani, S.Sn.",
                mataPelajaran = "Seni Budaya",
                email = "ani.suryani@school.com",
                noTelepon = "081234567899",
                alamat = "Jl. Seni No. 11, Jakarta",
                status = "Aktif",
                createdAt = "2025-11-12T10:00:00.000000Z",
                updatedAt = "2025-11-12T10:00:00.000000Z"
            )
        )
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