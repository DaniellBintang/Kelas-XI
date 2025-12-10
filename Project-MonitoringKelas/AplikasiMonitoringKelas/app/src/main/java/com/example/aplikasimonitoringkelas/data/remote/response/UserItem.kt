package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * User data item from API response
 * Represents a single user object returned from the API
 *
 * @property id User's unique identifier
 * @property nama User's full name
 * @property email User's email address
 * @property role User's role (siswa, kurikulum, kepala_sekolah, admin)
 * @property kelas User's class (only for students)
 * @property status User's status (aktif, nonaktif)
 * @property createdAt Timestamp when user was created
 * @property updatedAt Timestamp when user was last updated
 *
 * Example JSON response:
 * ```json
 * {
 *   "id": 1,
 *   "nama": "Ahmad Siswa",
 *   "email": "ahmad@sekolah.com",
 *   "role": "siswa",
 *   "kelas": "X RPL 1",
 *   "status": "aktif",
 *   "created_at": "2024-01-10T08:00:00.000000Z",
 *   "updated_at": "2024-01-10T08:00:00.000000Z"
 * }
 * ```
 */
data class UserItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String, // siswa, kurikulum, kepala_sekolah, admin

    @SerializedName("kelas")
    val kelas: String? = null, // Only for students

    @SerializedName("status")
    val status: String? = "aktif", // aktif or nonaktif

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Get formatted role name in Bahasa Indonesia
     */
    fun getRoleDisplayName(): String {
        return when (role.lowercase()) {
            "siswa" -> "Siswa"
            "kurikulum" -> "Kurikulum"
            "kepala_sekolah" -> "Kepala Sekolah"
            "admin" -> "Administrator"
            else -> role
        }
    }

    /**
     * Get role color for UI display
     */
    fun getRoleColor(): String {
        return when (role.lowercase()) {
            "admin" -> "#EF4444" // Red
            "kepala_sekolah" -> "#3B82F6" // Blue
            "kurikulum" -> "#10B981" // Green
            "siswa" -> "#F59E0B" // Orange
            else -> "#6B7280" // Gray
        }
    }

    /**
     * Check if user is active
     */
    fun isActive(): Boolean {
        return status?.equals("aktif", ignoreCase = true) == true
    }

    /**
     * Check if user is a student
     */
    fun isSiswa(): Boolean {
        return role.equals("siswa", ignoreCase = true)
    }

    /**
     * Check if user is an admin
     */
    fun isAdmin(): Boolean {
        return role.equals("admin", ignoreCase = true)
    }

    /**
     * Check if user is a kurikulum
     */
    fun isKurikulum(): Boolean {
        return role.equals("kurikulum", ignoreCase = true)
    }

    /**
     * Check if user is a kepala sekolah
     */
    fun isKepalaSekolah(): Boolean {
        return role.equals("kepala_sekolah", ignoreCase = true)
    }

    /**
     * Get formatted created date
     * Converts ISO 8601 format to readable format
     */
    fun getFormattedCreatedAt(): String {
        return createdAt?.let {
            try {
                // Convert "2024-01-10T08:00:00.000000Z" to "10 Jan 2024"
                val date = java.time.LocalDateTime.parse(
                    it.substringBefore("."),
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                date.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                )
            } catch (e: Exception) {
                it.substringBefore("T") // Fallback to date only
            }
        } ?: "-"
    }

    /**
     * Get user display name with role
     * Example: "Ahmad Siswa (Siswa - X RPL 1)"
     */
    fun getDisplayNameWithRole(): String {
        val roleText = getRoleDisplayName()
        return if (isSiswa() && kelas != null) {
            "$nama ($roleText - $kelas)"
        } else {
            "$nama ($roleText)"
        }
    }
}