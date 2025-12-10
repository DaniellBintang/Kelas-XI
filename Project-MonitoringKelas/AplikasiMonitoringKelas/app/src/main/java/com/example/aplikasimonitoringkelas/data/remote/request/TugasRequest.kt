package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Tugas Request Body
 * Untuk endpoint POST /api/tugas dan PUT /api/tugas/{id}
 */
data class TugasRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("tanggal")
    val tanggal: String, // Format: YYYY-MM-DD (2024-01-15)

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("judul_tugas")
    val judulTugas: String,

    @SerializedName("status")
    val status: String // Selesai, Belum Selesai, Terlambat
)

/**
 * Enum untuk Status Tugas
 */
enum class StatusTugas(val value: String, val displayName: String) {
    SELESAI("Selesai", "Selesai"),
    BELUM_SELESAI("Belum Selesai", "Belum Selesai"),
    TERLAMBAT("Terlambat", "Terlambat");

    companion object {
        fun fromValue(value: String): StatusTugas? {
            return values().find { it.value == value }
        }

        fun getAllValues(): List<String> {
            return values().map { it.value }
        }
    }
}

/**
 * Helper function untuk validasi status
 */
fun TugasRequest.isValidStatus(): Boolean {
    return StatusTugas.fromValue(status) != null
}