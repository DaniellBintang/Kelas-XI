package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Tugas Item
 * Response dari endpoint GET /api/tugas
 */
data class TugasItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("mata_pelajaran")
    val mataPelajaran: String,

    @SerializedName("judul_tugas")
    val judulTugas: String,

    @SerializedName("status")
    val status: String, // Selesai, Belum Selesai, Terlambat

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("user")
    val user: UserData? = null
)