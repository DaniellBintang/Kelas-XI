package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Guru Response
 * GET /api/guru
 */
data class GuruResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<GuruItem>
)

/**
 * Single Guru Response
 * GET /api/guru/{id}
 * POST /api/guru
 */
data class SingleGuruResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: GuruItem
)

/**
 * Guru Item
 */
data class GuruItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode_guru")
    val kodeGuru: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("no_telepon")
    val noTelepon: String?,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("status")
    val status: String, // Aktif, Cuti, Nonaktif

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)
