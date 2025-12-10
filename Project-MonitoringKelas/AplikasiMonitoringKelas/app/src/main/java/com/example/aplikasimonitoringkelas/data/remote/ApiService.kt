package com.example.aplikasimonitoringkelas.data.remote

import com.example.aplikasimonitoringkelas.data.remote.request.JadwalRequest
import com.example.aplikasimonitoringkelas.data.remote.request.KehadiranRequest
import com.example.aplikasimonitoringkelas.data.remote.request.LaporanGuruRequest
import com.example.aplikasimonitoringkelas.data.remote.request.LoginRequest
import com.example.aplikasimonitoringkelas.data.remote.request.TugasRequest
import com.example.aplikasimonitoringkelas.data.remote.request.UserRequest
import com.example.aplikasimonitoringkelas.data.remote.response.ApiResponse
import com.example.aplikasimonitoringkelas.data.remote.response.GuruItem
import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.remote.response.KehadiranItem
import com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongKehadiranResponse
import com.example.aplikasimonitoringkelas.data.remote.response.KelasKosongResponse
import com.example.aplikasimonitoringkelas.data.remote.response.LoginResponse
import com.example.aplikasimonitoringkelas.data.remote.response.TugasItem
import com.example.aplikasimonitoringkelas.data.remote.response.UserItem
import retrofit2.Response
import retrofit2.http.*
import com.example.aplikasimonitoringkelas.data.remote.request.*
import com.example.aplikasimonitoringkelas.data.remote.response.*
import retrofit2.http.*

/**
 * Retrofit API Service interface
 * Defines all API endpoints for the application
 */
interface ApiService {

    // ============================================
    // AUTH ENDPOINTS
    // ============================================

    /**
     * Login user
     * POST /api/login
     */
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Register new user
     * POST /api/register
     */
    @POST("register")
    suspend fun register(
        @Body request: UserRequest
    ): Response<ApiResponse<UserItem>>


    /**
     * Logout user
     * POST /api/logout
     */
    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    /**
     * Get current authenticated user
     * GET /api/user
     */
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserItem>>


    // ============================================
    // JADWAL ENDPOINTS
    // ============================================

    /**
     * Get jadwal pelajaran with optional filters
     * GET /api/jadwal
     */
    @GET("jadwal")
    suspend fun getJadwal(
        @Query("hari") hari: String? = null,
        @Query("kelas") kelas: String? = null,
        @Query("guru") guru: String? = null,
        @Query("mata_pelajaran") mataPelajaran: String? = null
    ): Response<ApiResponse<List<JadwalItem>>>

    /**
     * Get Kelas Kosong berdasarkan Kehadiran Guru
     * GET /api/kehadiran/kelas-kosong
     *
     * Menampilkan jadwal dimana guru status: Telat, Tidak Hadir, atau Izin
     *
     * @param tanggal Filter by date (YYYY-MM-DD format)
     * @param status Filter by status (Telat, Tidak Hadir, Izin)
     * @return Response with list of kelas kosong
     */
    @GET("kehadiran/kelas-kosong")
    suspend fun getKelasKosongKehadiran(
        @Query("tanggal") tanggal: String? = null,
        @Query("kelas") kelas: String? = null, // ✅ TAMBAHKAN
        @Query("status") status: String? = null
    ): Response<KelasKosongKehadiranResponse>

    /**
     * Get jadwal by ID
     * GET /api/jadwal/{id}
     */
    @GET("jadwal/{id}")
    suspend fun getJadwalById(
        @Path("id") id: Int
    ): Response<ApiResponse<JadwalItem>>

    /**
     * Create new jadwal (Admin only)
     * POST /api/jadwal
     */
    @POST("jadwal")
    suspend fun createJadwal(
        @Body request: JadwalRequest
    ): Response<ApiResponse<JadwalItem>>

    /**
     * Update existing jadwal (Admin only)
     * PUT /api/jadwal/{id}
     */
    @PUT("jadwal/{id}")
    suspend fun updateJadwal(
        @Path("id") id: Int,
        @Body request: JadwalRequest
    ): Response<ApiResponse<JadwalItem>>

    /**
     * Delete jadwal (Admin only)
     * DELETE /api/jadwal/{id}
     */
    @DELETE("jadwal/{id}")
    suspend fun deleteJadwal(
        @Path("id") id: Int
    ): Response<ApiResponse<Any>>

    // ✅ ADD THIS NEW ENDPOINT
    /**
     * Get kelas kosong (Empty classes monitoring - Kepala Sekolah feature)
     * GET /api/jadwal/kelas-kosong
     *
     * Returns list of schedules where teacher (kode_guru or nama_guru) is null/empty
     * Special endpoint for school principal to monitor classes without teachers
     *
     * @param hari Filter by day (Senin, Selasa, Rabu, Kamis, Jumat, Sabtu)
     * @param tanggal Filter by specific date (YYYY-MM-DD format)
     * @param kelas Filter by specific class (e.g., "X RPL 1", "XI RPL 2")
     * @return KelasKosongResponse with jumlah_kelas_kosong and list of empty schedules
     *
     * Example response:
     * ```json
     * {
     *   "success": true,
     *   "message": "Ditemukan 3 kelas kosong pada hari Senin",
     *   "jumlah_kelas_kosong": 3,
     *   "data": [...]
     * }
     * ```
     */
    @GET("jadwal/kelas-kosong")
    suspend fun getKelasKosong(
        @Query("hari") hari: String? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("kelas") kelas: String? = null
    ): Response<KelasKosongResponse>

    /**
     * Get statistik kelas kosong (Kepala Sekolah dashboard)
     * GET /api/jadwal/statistik/kelas-kosong
     *
     * Returns aggregated statistics about empty classes
     * Useful for dashboard overview
     */
    @GET("jadwal/statistik/kelas-kosong")
    suspend fun getStatistikKelasKosong(): Response<ApiResponse<Any>>

    /**
     * ✅ BARU: Get jadwal dengan status guru (izin/pengganti)
     * GET /api/jadwal/dengan-status-guru
     *
     * Endpoint khusus untuk Siswa Activity - Jadwal Pelajaran
     * Menampilkan jadwal dengan info:
     * - status_guru: "normal" atau "izin"
     * - info_izin: Detail izin guru (keterangan, durasi, dll)
     * - guru_pengganti: Info guru pengganti jika ada
     *
     * @param hari Filter by day (Senin-Minggu)
     * @param kelas Filter by class (XI RPL 1, etc)
     * @param tanggal Optional date filter (default: hari ini)
     * @return Response with list of JadwalItem with extended guru status info
     */
    @GET("jadwal/dengan-status-guru")
    suspend fun getJadwalDenganStatusGuru(
        @Query("hari") hari: String? = null,
        @Query("kelas") kelas: String? = null,
        @Query("tanggal") tanggal: String? = null
    ): Response<ApiResponse<List<JadwalItem>>>


    // ============================================
    // KEHADIRAN ENDPOINTS (Siswa)
    // ============================================

    /**
     * Get kehadiran data with filters
     * GET /api/kehadiran
     */
    @GET("kehadiran")
    suspend fun getKehadiran(
        @Query("user_id") userId: Int? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("mata_pelajaran") mataPelajaran: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<KehadiranItem>>>

    /**
     * Get kehadiran/laporan by kelas
     * GET /api/kehadiran/kelas/{kelas}
     */
    @GET("kehadiran/kelas/{kelas}")
    suspend fun getKehadiranByKelas(
        @Path("kelas") kelas: String
    ): Response<ApiResponse<List<KehadiranItem>>>

    /**
     * Get kehadiran by ID
     * GET /api/kehadiran/{id}
     */
    @GET("kehadiran/{id}")
    suspend fun getKehadiranById(
        @Path("id") id: Int
    ): Response<ApiResponse<KehadiranItem>>

    /**
     * Create new kehadiran entry
     * POST /api/kehadiran
     */
    @POST("kehadiran")
    suspend fun createKehadiran(
        @Body request: KehadiranRequest
    ): Response<ApiResponse<KehadiranItem>>

    /**
     * Update kehadiran
     * PUT /api/kehadiran/{id}
     */
    @PUT("kehadiran/{id}")
    suspend fun updateKehadiran(
        @Path("id") id: Int,
        @Body request: KehadiranRequest
    ): Response<ApiResponse<KehadiranItem>>

    /**
     * Delete kehadiran
     * DELETE /api/kehadiran/{id}
     */
    @DELETE("kehadiran/{id}")
    suspend fun deleteKehadiran(
        @Path("id") id: Int
    ): Response<ApiResponse<Any>>


    // ============================================
    // TUGAS ENDPOINTS (Siswa)
    // ============================================

    /**
     * Get tugas data with filters
     * GET /api/tugas
     */
    @GET("tugas")
    suspend fun getTugas(
        @Query("user_id") userId: Int? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("mata_pelajaran") mataPelajaran: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<TugasItem>>>

    /**
     * Get tugas by ID
     * GET /api/tugas/{id}
     */
    @GET("tugas/{id}")
    suspend fun getTugasById(
        @Path("id") id: Int
    ): Response<ApiResponse<TugasItem>>

    /**
     * Create new tugas entry
     * POST /api/tugas
     */
    @POST("tugas")
    suspend fun createTugas(
        @Body request: TugasRequest
    ): Response<ApiResponse<TugasItem>>

    /**
     * Update tugas
     * PUT /api/tugas/{id}
     */
    @PUT("tugas/{id}")
    suspend fun updateTugas(
        @Path("id") id: Int,
        @Body request: TugasRequest
    ): Response<ApiResponse<TugasItem>>

    /**
     * Delete tugas
     * DELETE /api/tugas/{id}
     */
    @DELETE("tugas/{id}")
    suspend fun deleteTugas(
        @Path("id") id: Int
    ): Response<ApiResponse<Any>>


    // ============================================
    // USER MANAGEMENT ENDPOINTS (Admin only)
    // ============================================

    /**
     * Get all users with optional filters and pagination
     * GET /api/users
     */
    @GET("users")
    suspend fun getUsers(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): Response<ApiResponse<List<UserItem>>>

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<ApiResponse<UserItem>>

    /**
     * Create new user (Admin only)
     * POST /api/users
     */
    @POST("users")
    suspend fun createUser(
        @Body request: UserRequest
    ): Response<ApiResponse<UserItem>>

    /**
     * Update existing user (Admin only)
     * PUT /api/users/{id}
     */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UserRequest
    ): Response<ApiResponse<UserItem>>

    /**
     * Delete user (Admin only)
     * DELETE /api/users/{id}
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): Response<ApiResponse<Any>>

    /**
     * Get user statistics (Admin dashboard)
     * GET /api/users/statistics
     */
    @GET("users/statistics")
    suspend fun getUserStatistics(): Response<ApiResponse<Any>>

    /**
     * Bulk delete users (Admin only)
     * POST /api/users/bulk-delete
     */
    @POST("users/bulk-delete")
    suspend fun bulkDeleteUsers(
        @Body userIds: Map<String, List<Int>>
    ): Response<ApiResponse<Any>>

    /**
     * Change user status (Admin only)
     * PATCH /api/users/{id}/status
     */
    @PATCH("users/{id}/status")
    suspend fun changeUserStatus(
        @Path("id") id: Int,
        @Body status: Map<String, String>
    ): Response<ApiResponse<UserItem>>


    // ============================================
    // GURU ENDPOINTS
    // ============================================

    /**
     * Get all guru with optional filters
     * GET /api/guru
     */
    @GET("guru")
    suspend fun getGuru(
        @Query("status") status: String? = null,
        @Query("mata_pelajaran") mataPelajaran: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<GuruItem>>>

    /**
     * Get guru by ID
     * GET /api/guru/{id}
     */
    @GET("guru/{id}")
    suspend fun getGuruById(
        @Path("id") id: Int
    ): Response<ApiResponse<GuruItem>>

    /**
     * Assign Guru Pengganti untuk Kehadiran
     * PUT /api/kehadiran/{id}/assign-pengganti
     */
    @PUT("kehadiran/{id}/assign-pengganti")
    suspend fun assignGuruPengganti(
        @Path("id") kehadiranId: Int,
        @Body request: AssignGuruPenggantiRequest
    ): Response<ApiResponse<KehadiranItem>>

    /**
     * Hapus Guru Pengganti
     * DELETE /api/kehadiran/{id}/hapus-pengganti
     */
    @DELETE("kehadiran/{id}/hapus-pengganti")
    suspend fun hapusGuruPengganti(
        @Path("id") kehadiranId: Int
    ): Response<ApiResponse<KehadiranItem>>


    // ============================================
    // LAPORAN GURU ENDPOINTS (Siswa)
    // ============================================

    /**
     * Siswa melaporkan kehadiran guru (telat/tidak hadir)
     * POST /api/kehadiran
     */
    @POST("kehadiran")
    suspend fun laporkanGuru(
        @Body request: LaporanGuruRequest
    ): Response<ApiResponse<KehadiranItem>>
}