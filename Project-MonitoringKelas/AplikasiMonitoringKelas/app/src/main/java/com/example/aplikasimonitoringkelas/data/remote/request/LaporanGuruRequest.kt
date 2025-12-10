package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Laporan Guru Request Body
 * Untuk siswa melaporkan kehadiran guru
 * Endpoint POST /api/kehadiran
 */
data class LaporanGuruRequest(
    @SerializedName("jadwal_id")
    val jadwalId: Int? = null, // Optional, null jika laporan dari siswa

    @SerializedName("tanggal")
    val tanggal: String, // Format: YYYY-MM-DD (2025-11-12)

    @SerializedName("jam_masuk")
    val jamMasuk: String? = null, // Format: HH:mm (07:30)

    @SerializedName("jam_keluar")
    val jamKeluar: String? = null, // Format: HH:mm (14:00)

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("nama_guru")
    val namaGuru: String,

    @SerializedName("kode_guru")
    val kodeGuru: String,

    @SerializedName("status")
    val status: String, // Hadir, Telat, Tidak Hadir, Izin

    @SerializedName("keterangan")
    val keterangan: String? = null
)

/**
 * Enum untuk Status Kehadiran Guru
 */
enum class StatusKehadiranGuru(val value: String, val displayName: String) {
    HADIR("Hadir", "Hadir"),
    TELAT("Telat", "Terlambat"),
    TIDAK_HADIR("Tidak Hadir", "Tidak Hadir"),
    IZIN("Izin", "Izin");

    companion object {
        fun fromValue(value: String): StatusKehadiranGuru? {
            return values().find { it.value == value }
        }
    }
}
