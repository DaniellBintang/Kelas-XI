package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Jadwal Item
 * Response dari endpoint GET /api/jadwal dan GET /api/jadwal/dengan-status-guru
 */
data class JadwalItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("hari")
    val hari: String,

    @SerializedName("kelas")
    val kelas: String,

    @SerializedName("jam")
    val jam: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("kode_guru")
    val kodeGuru: String? = null, // Made nullable

    @SerializedName("nama_guru")
    val namaGuru: String? = null, // Made nullable (this was missing!)

    @SerializedName("ruangan")
    val ruangan: String,

    // ✅ BARU: Status guru (normal/izin)
    @SerializedName("status_guru")
    val statusGuru: String? = "normal",

    // ✅ BARU: Info izin guru
    @SerializedName("info_izin")
    val infoIzin: InfoIzinGuru? = null,

    // ✅ BARU: Info guru pengganti
    @SerializedName("guru_pengganti")
    val guruPengganti: GuruPenggantiInfo? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * ✅ BARU: Info Izin Guru
 */
data class InfoIzinGuru(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("keterangan")
    val keterangan: String? = null,

    @SerializedName("tanggal_mulai")
    val tanggalMulai: String? = null,

    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String? = null,

    @SerializedName("durasi_hari")
    val durasiHari: Int? = null
)

/**
 * ✅ BARU: Info Guru Pengganti
 */
data class GuruPenggantiInfo(
    @SerializedName("kode_guru")
    val kodeGuru: String? = null,

    @SerializedName("nama_guru")
    val namaGuru: String? = null,

    @SerializedName("keterangan")
    val keterangan: String? = null
)