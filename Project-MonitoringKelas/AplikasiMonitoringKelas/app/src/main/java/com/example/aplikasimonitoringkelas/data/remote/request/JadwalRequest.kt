package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Jadwal Request Body
 * Untuk endpoint POST /api/jadwal dan PUT /api/jadwal/{id}
 */
data class JadwalRequest(
    @SerializedName("hari")
    val hari: String, // Senin, Selasa, Rabu, Kamis, Jumat, Sabtu, Minggu

    @SerializedName("kelas")
    val kelas: String, // XI RPL, XII RPL, X RPL

    @SerializedName("jam")
    val jam: String, // 07:00-08:30

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("kode_guru")
    val kodeGuru: String,

    @SerializedName("nama_guru")
    val namaGuru: String,

    @SerializedName("ruangan")
    val ruangan: String
)

/**
 * Helper function untuk validasi hari
 */
fun JadwalRequest.isValidHari(): Boolean {
    val validHari = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    return hari in validHari
}