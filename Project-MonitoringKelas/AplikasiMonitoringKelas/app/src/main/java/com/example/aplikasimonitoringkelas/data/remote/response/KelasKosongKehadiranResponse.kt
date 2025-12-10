package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response dari endpoint GET /api/kehadiran/kelas-kosong
 * Digunakan oleh Kurikulum untuk monitoring guru yang tidak hadir
 */
data class KelasKosongKehadiranResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("jumlah_kelas_kosong")
    val jumlahKelasKosong: Int = 0,

    @SerializedName("filter")
    val filter: FilterInfo? = null,

    @SerializedName("data")
    val data: List<KelasKosongItem> = emptyList()
)

data class FilterInfo(
    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("kelas")
    val kelas: String,

    @SerializedName("status")
    val status: String
)

/**
 * Item Kelas Kosong dari Kehadiran Guru
 */
data class KelasKosongItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("jadwal_id")
    val jadwalId: Int?,

    @SerializedName("guru_id")
    val guruId: Int?,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("jam_masuk")
    val jamMasuk: String?,

    @SerializedName("jam_keluar")
    val jamKeluar: String?,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    // ✅ TAMBAHKAN property kelas
    @SerializedName("kelas")
    val kelas: String? = null,

    @SerializedName("nama_guru")
    val namaGuru: String?,

    @SerializedName("kode_guru")
    val kodeGuru: String?,

    @SerializedName("status")
    val status: String, // Telat, Tidak Hadir, Izin

    @SerializedName("keterangan")
    val keterangan: String?,

    // ✅ TAMBAHKAN: Guru Pengganti Fields
    @SerializedName("nama_guru_pengganti")
    val namaGuruPengganti: String? = null,

    @SerializedName("kode_guru_pengganti")
    val kodeGuruPengganti: String? = null,

    @SerializedName("keterangan_pengganti")
    val keteranganPengganti: String? = null,

    @SerializedName("waktu_assign_pengganti")
    val waktuAssignPengganti: String? = null,

    // ✅ TAMBAHKAN: Durasi Izin Fields
    @SerializedName("tanggal_mulai_izin")
    val tanggalMulaiIzin: String? = null,

    @SerializedName("tanggal_selesai_izin")
    val tanggalSelesaiIzin: String? = null,

    @SerializedName("durasi_izin_hari")
    val durasiIzinHari: Int? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("jadwal")
    val jadwal: JadwalInfo?,

    @SerializedName("guru")
    val guru: GuruInfo?
)

/**
 * Informasi Jadwal (nested object)
 */
data class JadwalInfo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("hari")
    val hari: String,

    @SerializedName("jam")
    val jam: String,

    @SerializedName("kelas")
    val kelas: String,

    @SerializedName("ruangan")
    val ruangan: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String? = null
)

/**
 * Informasi Guru (nested object)
 */
data class GuruInfo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode_guru")
    val kodeGuru: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("no_telepon")
    val noTelepon: String? = null,

    @SerializedName("alamat")
    val alamat: String? = null,

    @SerializedName("status")
    val status: String
)