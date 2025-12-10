package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Kehadiran Request Body
 * Format SESUAI dengan Postman yang berhasil
 */
data class KehadiranRequest(
    @SerializedName("jadwal_id")
    val jadwalId: Int? = null,

    @SerializedName("tanggal")
    val tanggal: String, // ✅ Required: Format yyyy-MM-dd (2025-11-13)

    @SerializedName("jam_masuk")
    val jamMasuk: String? = null,

    @SerializedName("jam_keluar")
    val jamKeluar: String? = null,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("kelas")
    val kelas: String, // ✅ TAMBAHKAN: XI RPL 1, XII RPL 2, dll

    @SerializedName("nama_guru")
    val namaGuru: String,

    @SerializedName("kode_guru")
    val kodeGuru: String,

    @SerializedName("status")
    val status: String, // ✅ Required: Hadir, Telat, Tidak Hadir, Izin

    @SerializedName("keterangan")
    val keterangan: String? = null
)