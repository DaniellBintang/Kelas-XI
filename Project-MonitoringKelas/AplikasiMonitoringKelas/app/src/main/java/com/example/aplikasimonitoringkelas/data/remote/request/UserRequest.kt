package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for User Management (Admin)
 * Used for creating and updating users via POST /api/users and PUT /api/users/{id}
 *
 * @property nama User's full name (required)
 * @property email User's email address (required, must be unique)
 * @property password User's password (required for create, optional for update)
 * @property role User's role - siswa, kurikulum, kepala_sekolah, admin (required)
 * @property kelas User's class - only for role "siswa" (optional)
 * @property status User's status - aktif or nonaktif (optional, default: aktif)
 *
 * Example usage:
 * ```
 * val request = UserRequest(
 *     nama = "Ahmad Siswa",
 *     email = "ahmad@sekolah.com",
 *     password = "password123",
 *     role = "siswa",
 *     kelas = "X RPL 1",
 *     status = "aktif"
 * )
 * ```
 */
data class UserRequest(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String? = null, // Optional for update, required for create

    @SerializedName("role")
    val role: String, // siswa, kurikulum, kepala_sekolah, admin

    @SerializedName("kelas")
    val kelas: String? = null, // Optional, khusus untuk role "siswa"

    @SerializedName("status")
    val status: String? = "aktif" // aktif or nonaktif (default: aktif)
)