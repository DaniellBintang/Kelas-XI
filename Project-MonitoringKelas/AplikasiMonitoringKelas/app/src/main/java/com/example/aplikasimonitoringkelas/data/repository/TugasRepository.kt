package com.example.aplikasimonitoringkelas.data.repository

import android.util.Log
import com.example.aplikasimonitoringkelas.data.dummy.DummyData
import com.example.aplikasimonitoringkelas.data.remote.RetrofitClient
import com.example.aplikasimonitoringkelas.data.remote.request.TugasRequest
import com.example.aplikasimonitoringkelas.data.remote.response.TugasItem
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.delay

/**
 * Tugas Repository
 *
 * Handles data source switching antara Dummy Data dan Real API
 * Implements Repository Pattern untuk centralize data access logic
 *
 * Usage:
 * ```
 * val repository = TugasRepository()
 * val result = repository.getTugas(userId = 4)
 *
 * result.onSuccess { tugasList ->
 *     // Handle success
 * }.onFailure { exception ->
 *     // Handle error
 * }
 * ```
 */
class TugasRepository {

    companion object {
        private const val TAG = "TugasRepository"
        private const val FALLBACK_TO_DUMMY = true
    }

    /**
     * Get Tugas by User ID
     *
     * @param userId User ID untuk filter tugas (optional, null = semua tugas)
     * @return Result<List<TugasItem>> Success dengan data atau Failure dengan exception
     */
    suspend fun getTugas(userId: Int? = null): Result<List<TugasItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                getTugasFromDummy(userId)
            } else {
                getTugasFromApi(userId)
            }
        } catch (e: Exception) {
            logError("getTugas", e)

            if (FALLBACK_TO_DUMMY && !AppConfig.isDummyMode()) {
                logInfo("Fallback to dummy data")
                getTugasFromDummy(userId)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get Tugas by ID
     */
    suspend fun getTugasById(id: Int): Result<TugasItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                val tugas = DummyData.tugasList.find { it.id == id }

                if (tugas != null) {
                    delay(300)
                    Result.success(tugas)
                } else {
                    Result.failure(Exception("Tugas dengan ID $id tidak ditemukan"))
                }
            } else {
                val response = RetrofitClient.apiService.getTugasById(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("getTugasById: ${apiResponse.data.judulTugas}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Data tidak ditemukan"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("getTugasById", e)
            Result.failure(e)
        }
    }

    /**
     * Create new Tugas
     */
    suspend fun createTugas(request: TugasRequest): Result<TugasItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Create tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                val response = RetrofitClient.apiService.createTugas(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("createTugas: ${apiResponse.data.judulTugas}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal membuat tugas"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: ${errorBody ?: response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("createTugas", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing Tugas
     */
    suspend fun updateTugas(id: Int, request: TugasRequest): Result<TugasItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Update tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                val response = RetrofitClient.apiService.updateTugas(id, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        logSuccess("updateTugas: ${apiResponse.data.judulTugas}")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal update tugas"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: ${errorBody ?: response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("updateTugas", e)
            Result.failure(e)
        }
    }

    /**
     * Delete Tugas
     */
    suspend fun deleteTugas(id: Int): Result<Boolean> {
        return try {
            if (AppConfig.isDummyMode()) {
                Result.failure(
                    Exception("Delete tidak tersedia di Dummy Mode. Aktifkan Real API di AppConfig.")
                )
            } else {
                val response = RetrofitClient.apiService.deleteTugas(id)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        logSuccess("deleteTugas: ID $id")
                        Result.success(true)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Gagal hapus tugas"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            logError("deleteTugas", e)
            Result.failure(e)
        }
    }

    /**
     * Get Tugas Summary (Count by Status)
     */
    suspend fun getTugasSummary(userId: Int): Result<Map<String, Int>> {
        return try {
            getTugas(userId).fold(
                onSuccess = { tugasList ->
                    val summary = tugasList.groupBy { it.status }
                        .mapValues { it.value.size }
                    Result.success(summary)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            logError("getTugasSummary", e)
            Result.failure(e)
        }
    }


    // ==================== PRIVATE HELPER METHODS ====================

    private suspend fun getTugasFromDummy(userId: Int?): Result<List<TugasItem>> {
        delay(500)

        val tugasList = DummyData.getTugasByUserId(userId)
        logInfo("Loaded ${tugasList.size} items from Dummy Data (userId=$userId)")

        return Result.success(tugasList)
    }

    private suspend fun getTugasFromApi(userId: Int?): Result<List<TugasItem>> {
        val response = RetrofitClient.apiService.getTugas(userId)

        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                val tugasList = apiResponse.data ?: emptyList()
                logSuccess("Loaded ${tugasList.size} items from API (userId=$userId)")
                Result.success(tugasList)
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