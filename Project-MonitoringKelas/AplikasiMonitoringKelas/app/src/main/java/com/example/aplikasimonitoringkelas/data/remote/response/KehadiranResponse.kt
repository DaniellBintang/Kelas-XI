package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

data class KehadiranItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("jadwal_id")
    val jadwalId: Int? = null,

    @SerializedName("guru_id")
    val guruId: Int? = null,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("jam_masuk")
    val jamMasuk: String? = null,

    @SerializedName("jam_keluar")
    val jamKeluar: String? = null,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("kelas")
    val kelas: String? = null,

    @SerializedName("nama_guru")
    val namaGuru: String? = null,

    @SerializedName("kode_guru")
    val kodeGuru: String? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("keterangan")
    val keterangan: String? = null,

    // ✅ TAMBAHKAN: Guru Pengganti
    @SerializedName("nama_guru_pengganti")
    val namaGuruPengganti: String? = null,

    @SerializedName("kode_guru_pengganti")
    val kodeGuruPengganti: String? = null,

    @SerializedName("keterangan_pengganti")
    val keteranganPengganti: String? = null,

    @SerializedName("waktu_assign_pengganti")
    val waktuAssignPengganti: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("jadwal")
    val jadwal: JadwalData? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("user")
    val user: UserData? = null
)

// ✅ TAMBAHKAN: JadwalData class untuk nested jadwal object
data class JadwalData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("hari")
    val hari: String,

    @SerializedName("jam")
    val jam: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("kode_guru")
    val kodeGuru: String? = null,

    @SerializedName("nama_guru")
    val namaGuru: String,

    @SerializedName("ruangan")
    val ruangan: String,

    @SerializedName("kelas")
    val kelas: String,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// ✅ UserData untuk nested user object
data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("kelas")
    val kelas: String? = null
)

// ✅ Response untuk GET /api/kehadiran
data class KehadiranListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<KehadiranItem>
)

// ✅ Response untuk POST/PUT /api/kehadiran
data class KehadiranResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: KehadiranItem
)

// ✅ Response untuk DELETE /api/kehadiran/{id}
data class KehadiranDeleteResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)