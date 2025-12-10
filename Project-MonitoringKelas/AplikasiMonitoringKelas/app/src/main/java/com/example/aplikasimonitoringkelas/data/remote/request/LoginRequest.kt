package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Login Request Body
 * Untuk endpoint POST /api/login
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)