package com.example.aplikasimonitoringkelas.data.repository

import android.util.Log
import android.util.Patterns
import com.example.aplikasimonitoringkelas.data.dummy.DummyData
import com.example.aplikasimonitoringkelas.data.remote.ApiConfig
import com.example.aplikasimonitoringkelas.data.remote.request.UserRequest
import com.example.aplikasimonitoringkelas.data.remote.response.UserItem
import com.example.aplikasimonitoringkelas.utils.AppConfig
import kotlinx.coroutines.delay

/**
 * Repository for User Management (Admin features)
 * Handles user CRUD operations with validation and error handling
 *
 * Features:
 * - GET: getUsers(), getUserById()
 * - CREATE: createUser() with client-side validation
 * - UPDATE: updateUser() with validation
 * - DELETE: deleteUser(), bulkDeleteUsers()
 * - ADMIN: changeUserStatus(), getUserStatistics()
 *
 * Usage:
 * ```
 * val repository = UserRepository()
 * val result = repository.getUsers(role = "siswa")
 *
 * result.onSuccess { users ->
 *     // Handle success
 * }.onFailure { exception ->
 *     // Handle error
 * }
 * ```
 */
class UserRepository {

    companion object {
        private const val TAG = "UserRepository"
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_NAME_LENGTH = 255
        private const val MAX_EMAIL_LENGTH = 255
    }

    private val apiService = ApiConfig.getApiService()

    // ==================== READ OPERATIONS ====================

    /**
     * Get all users with optional filters
     *
     * @param role Filter by role (siswa, kurikulum, kepala_sekolah, admin)
     * @param status Filter by status (aktif, nonaktif)
     * @param search Search by name or email
     * @param perPage Number of items per page (default: 10)
     * @param page Page number (default: 1)
     * @return Result with list of UserItem
     */
    suspend fun getUsers(
        role: String? = null,
        status: String? = null,
        search: String? = null,
        perPage: Int? = 10,
        page: Int? = 1
    ): Result<List<UserItem>> {
        return try {
            if (AppConfig.isDummyMode()) {
                // MODE: Dummy Data
                getUsersFromDummy(role, status, search)
            } else {
                // MODE: Real API
                getUsersFromApi(role, status, search, perPage, page)
            }
        } catch (e: Exception) {
            logError("getUsers", e)
            Result.failure(e)
        }
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return Result with UserItem
     */
    suspend fun getUserById(id: Int): Result<UserItem> {
        return try {
            if (AppConfig.isDummyMode()) {
                delay(300) // Simulate network delay
                
                val user = DummyData.getDummyUsers().find { it.id == id }
                if (user != null) {
                    logSuccess("getUserById from Dummy: ${user.nama}")
                    Result.success(user)
                } else {
                    logError("getUserById", Exception("User ID $id tidak ditemukan"))
                    Result.failure(Exception("User tidak ditemukan"))
                }
            } else {
                val response = apiService.getUserById(id)

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        logSuccess("getUserById from API: ${user.nama}")
                        Result.success(user)
                    } else {
                        Result.failure(Exception("User tidak ditemukan"))
                    }
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Gagal mengambil data user"))
                }
            }
        } catch (e: Exception) {
            logError("getUserById", e)
            Result.failure(e)
        }
    }

    // ==================== CREATE OPERATION ====================

    /**
     * Create new user (Admin only)
     * With client-side validation
     *
     * @param request UserRequest with user data
     * @return Result with created UserItem
     */
    suspend fun createUser(request: UserRequest): Result<UserItem> {
        return try {
            // Client-side validation
            val validationError = validateUserRequest(request, isUpdate = false)
            if (validationError != null) {
                logError("createUser - Validation", Exception(validationError))
                return Result.failure(Exception(validationError))
            }

            if (AppConfig.isDummyMode()) {
                logInfo("Create blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur CREATE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                logInfo("Creating user: ${request.nama} (${request.role})")
                val response = apiService.createUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        logSuccess("createUser: ${user.nama} berhasil dibuat")
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Gagal membuat user"))
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal membuat user"
                    logError("createUser", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("createUser", e)
            Result.failure(e)
        }
    }

    // ==================== UPDATE OPERATION ====================

    /**
     * Update existing user (Admin only)
     * With client-side validation
     *
     * @param id User ID to update
     * @param request UserRequest with updated data
     * @return Result with updated UserItem
     */
    suspend fun updateUser(id: Int, request: UserRequest): Result<UserItem> {
        return try {
            // Client-side validation (password optional for update)
            val validationError = validateUserRequest(request, isUpdate = true)
            if (validationError != null) {
                logError("updateUser - Validation", Exception(validationError))
                return Result.failure(Exception(validationError))
            }

            if (AppConfig.isDummyMode()) {
                logInfo("Update blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur UPDATE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                logInfo("Updating user ID $id: ${request.nama}")
                val response = apiService.updateUser(id, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        logSuccess("updateUser: ID $id - ${user.nama} berhasil diupdate")
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Gagal mengupdate user"))
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengupdate user"
                    logError("updateUser", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("updateUser", e)
            Result.failure(e)
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete user (Admin only)
     *
     * @param id User ID to delete
     * @return Result with success message
     */
    suspend fun deleteUser(id: Int): Result<String> {
        return try {
            if (AppConfig.isDummyMode()) {
                logInfo("Delete blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur DELETE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                logInfo("Deleting user ID $id")
                val response = apiService.deleteUser(id)

                if (response.isSuccessful && response.body()?.success == true) {
                    val message = response.body()?.message ?: "User berhasil dihapus"
                    logSuccess("deleteUser: ID $id - $message")
                    Result.success(message)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal menghapus user"
                    logError("deleteUser", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("deleteUser", e)
            Result.failure(e)
        }
    }

    /**
     * Bulk delete users (Admin only)
     *
     * @param userIds List of user IDs to delete
     * @return Result with success message
     */
    suspend fun bulkDeleteUsers(userIds: List<Int>): Result<String> {
        return try {
            if (userIds.isEmpty()) {
                return Result.failure(Exception("Tidak ada user yang dipilih"))
            }

            if (AppConfig.isDummyMode()) {
                logInfo("Bulk delete blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur BULK DELETE hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                logInfo("Bulk deleting ${userIds.size} users: $userIds")
                val response = apiService.bulkDeleteUsers(mapOf("user_ids" to userIds))

                if (response.isSuccessful && response.body()?.success == true) {
                    val message = response.body()?.message ?: "${userIds.size} user berhasil dihapus"
                    logSuccess("bulkDeleteUsers: $message")
                    Result.success(message)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal menghapus user"
                    logError("bulkDeleteUsers", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("bulkDeleteUsers", e)
            Result.failure(e)
        }
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Change user status (Admin only)
     *
     * @param id User ID
     * @param status New status (aktif or nonaktif)
     * @return Result with updated UserItem
     */
    suspend fun changeUserStatus(id: Int, status: String): Result<UserItem> {
        return try {
            // Validate status
            if (status != "aktif" && status != "nonaktif") {
                return Result.failure(Exception("Status harus 'aktif' atau 'nonaktif'"))
            }

            if (AppConfig.isDummyMode()) {
                logInfo("Change status blocked in Dummy Mode")
                Result.failure(
                    Exception("Fitur CHANGE STATUS hanya tersedia di API Mode. Ubah AppConfig.USE_DUMMY_DATA = false")
                )
            } else {
                logInfo("Changing user ID $id status to: $status")
                val response = apiService.changeUserStatus(id, mapOf("status" to status))

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        logSuccess("changeUserStatus: ID $id - Status changed to $status")
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Gagal mengubah status user"))
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengubah status user"
                    logError("changeUserStatus", Exception(errorMsg))
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            logError("changeUserStatus", e)
            Result.failure(e)
        }
    }

    /**
     * Get user statistics (Admin dashboard)
     *
     * @return Result with statistics data
     */
    suspend fun getUserStatistics(): Result<Map<String, Any>> {
        return try {
            if (AppConfig.isDummyMode()) {
                delay(500) // Simulate network delay
                
                val users = DummyData.getDummyUsers()
                val stats = mapOf(
                    "total_users" to users.size,
                    "total_siswa" to users.count { it.role == "siswa" },
                    "total_kurikulum" to users.count { it.role == "kurikulum" },
                    "total_kepala_sekolah" to users.count { it.role == "kepala_sekolah" },
                    "total_admin" to users.count { it.role == "admin" },
                    "users_aktif" to users.count { it.status == "aktif" },
                    "users_nonaktif" to users.count { it.status == "nonaktif" }
                )
                
                logSuccess("getUserStatistics from Dummy: ${users.size} total users")
                Result.success(stats)
            } else {
                val response = apiService.getUserStatistics()

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val stats = when (data) {
                        is Map<*, *> -> data as Map<String, Any>
                        else -> mapOf("data" to (data ?: emptyMap<String, Any>()))
                    }
                    logSuccess("getUserStatistics from API")
                    Result.success(stats)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Gagal mengambil statistik"))
                }
            }
        } catch (e: Exception) {
            logError("getUserStatistics", e)
            Result.failure(e)
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Get users from Dummy Data with filters
     */
    private suspend fun getUsersFromDummy(
        role: String?,
        status: String?,
        search: String?
    ): Result<List<UserItem>> {
        delay(400) // Simulate network delay
        
        var users = DummyData.getDummyUsers()

        // Apply filters
        if (role != null) {
            users = users.filter { it.role.equals(role, ignoreCase = true) }
        }

        if (status != null) {
            users = users.filter { it.status?.equals(status, ignoreCase = true) == true }
        }

        if (search != null && search.isNotBlank()) {
            users = users.filter {
                it.nama.contains(search, ignoreCase = true) ||
                        it.email.contains(search, ignoreCase = true)
            }
        }

        logSuccess("Loaded ${users.size} users from Dummy (role=$role, status=$status, search=$search)")
        return Result.success(users)
    }

    /**
     * Get users from Real API with filters
     */
    private suspend fun getUsersFromApi(
        role: String?,
        status: String?,
        search: String?,
        perPage: Int?,
        page: Int?
    ): Result<List<UserItem>> {
        val response = apiService.getUsers(
            role = role,
            status = status,
            search = search,
            perPage = perPage,
            page = page
        )

        return if (response.isSuccessful && response.body()?.success == true) {
            val users = response.body()?.data ?: emptyList()
            logSuccess("Loaded ${users.size} users from API (role=$role, status=$status)")
            Result.success(users)
        } else {
            val errorMsg = response.body()?.message ?: "Gagal mengambil data user"
            logError("getUsersFromApi", Exception(errorMsg))
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Validate UserRequest before sending to API
     * Client-side validation
     *
     * @param request UserRequest to validate
     * @param isUpdate True if this is an update operation (password optional)
     * @return Error message if validation fails, null if valid
     */
    private fun validateUserRequest(request: UserRequest, isUpdate: Boolean): String? {
        // Validate nama
        if (request.nama.isBlank()) {
            return "Nama tidak boleh kosong"
        }
        if (request.nama.length > MAX_NAME_LENGTH) {
            return "Nama maksimal $MAX_NAME_LENGTH karakter"
        }

        // Validate email
        if (request.email.isBlank()) {
            return "Email tidak boleh kosong"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
            return "Format email tidak valid"
        }
        if (request.email.length > MAX_EMAIL_LENGTH) {
            return "Email maksimal $MAX_EMAIL_LENGTH karakter"
        }

        // Validate password (required for create, optional for update)
        if (!isUpdate) {
            // For create operation, password is required
            if (request.password.isNullOrBlank()) {
                return "Password tidak boleh kosong"
            }
            if (request.password.length < MIN_PASSWORD_LENGTH) {
                return "Password minimal $MIN_PASSWORD_LENGTH karakter"
            }
        } else {
            // For update operation, validate only if password is provided
            if (!request.password.isNullOrBlank() && request.password.length < MIN_PASSWORD_LENGTH) {
                return "Password minimal $MIN_PASSWORD_LENGTH karakter"
            }
        }

        // Validate role
        val validRoles = listOf("siswa", "kurikulum", "kepala_sekolah", "admin")
        if (request.role !in validRoles) {
            return "Role harus salah satu dari: ${validRoles.joinToString(", ")}"
        }

        // Validate status (if provided)
        if (request.status != null && request.status !in listOf("aktif", "nonaktif")) {
            return "Status harus 'aktif' atau 'nonaktif'"
        }

        // All validations passed
        return null
    }

    // ==================== LOGGING HELPERS ====================

    private fun logInfo(message: String) {
        if (AppConfig.isDebugMode()) {
            Log.i(TAG, message)
        }
    }

    private fun logSuccess(message: String) {
        if (AppConfig.isDebugMode()) {
            Log.d(TAG, "✓ $message")
        }
    }

    private fun logError(method: String, exception: Exception) {
        if (AppConfig.isDebugMode()) {
            Log.e(TAG, "✗ $method failed: ${exception.message}", exception)
        }
    }
}