package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Login Response wrapper
 * Response dari endpoint POST /api/login
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: LoginData? = null,

    @SerializedName("token")
    val token: String? = null
)

/**
 * Login Data - berisi informasi user yang login
 */
data class LoginData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("kelas")
    val kelas: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)