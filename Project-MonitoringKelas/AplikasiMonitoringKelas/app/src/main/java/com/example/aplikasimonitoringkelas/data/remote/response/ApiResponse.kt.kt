package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 * Digunakan untuk semua response dari Laravel API
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)