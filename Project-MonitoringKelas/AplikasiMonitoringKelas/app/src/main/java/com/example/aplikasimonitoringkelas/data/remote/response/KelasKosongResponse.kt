package com.example.aplikasimonitoringkelas.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Kelas Kosong Response
 * Response khusus untuk endpoint GET /api/jadwal/kelas-kosong
 *
 * Digunakan oleh Kepala Sekolah untuk monitoring kelas kosong
 *
 * Response format dari Laravel backend:
 * ```json
 * {
 *   "success": true,
 *   "message": "Ditemukan 3 kelas kosong pada hari Senin",
 *   "jumlah_kelas_kosong": 3,
 *   "data": [
 *     {
 *       "id": 1,
 *       "hari": "Senin",
 *       "jam": "07:00-08:30",
 *       "mata_pelajaran": "Matematika",
 *       "kelas": "X RPL 1",
 *       "ruangan": "Lab 1",
 *       "kode_guru": null,
 *       "nama_guru": null,
 *       "status": "kosong"
 *     }
 *   ]
 * }
 * ```
 */
data class KelasKosongResponse(
    /**
     * Indicates whether the request was successful
     */
    @SerializedName("success")
    val success: Boolean,

    /**
     * Response message from server
     * Example: "Ditemukan 3 kelas kosong pada hari Senin"
     */
    @SerializedName("message")
    val message: String? = null,

    /**
     * Total count of empty classes found
     * Used for quick summary in dashboard
     */
    @SerializedName("jumlah_kelas_kosong")
    val jumlahKelasKosong: Int = 0,

    /**
     * List of empty class schedules
     * Contains JadwalItem objects with null or empty guru fields
     */
    @SerializedName("data")
    val data: List<JadwalItem> = emptyList(),

    /**
     * Optional: Timestamp when data was fetched
     */
    @SerializedName("timestamp")
    val timestamp: String? = null,

    /**
     * Optional: Filter parameters used
     */
    @SerializedName("filters")
    val filters: KelasKosongFilters? = null
)

/**
 * Filter parameters used in the kelas kosong request
 * Useful for displaying what filters were applied
 */
data class KelasKosongFilters(
    @SerializedName("hari")
    val hari: String? = null,

    @SerializedName("tanggal")
    val tanggal: String? = null,

    @SerializedName("kelas")
    val kelas: String? = null
)

/**
 * Extension function to check if there are empty classes
 */
fun KelasKosongResponse.hasEmptyClasses(): Boolean {
    return jumlahKelasKosong > 0 && data.isNotEmpty()
}

/**
 * Extension function to get summary message
 */
fun KelasKosongResponse.getSummaryMessage(): String {
    return when {
        jumlahKelasKosong == 0 -> "Tidak ada kelas kosong"
        jumlahKelasKosong == 1 -> "Ditemukan 1 kelas kosong"
        else -> "Ditemukan $jumlahKelasKosong kelas kosong"
    }
}

/**
 * Extension function to categorize by priority
 * Priority based on time (earlier classes = higher priority)
 */
fun KelasKosongResponse.getByPriority(): List<JadwalItem> {
    return data.sortedBy { jadwal ->
        // Extract start time from "07:00-08:30" format
        val startTime = jadwal.jam.split("-").firstOrNull() ?: "99:99"
        startTime
    }
}

/**
 * Extension function to group by kelas
 */
fun KelasKosongResponse.groupByKelas(): Map<String, List<JadwalItem>> {
    return data.groupBy { it.kelas }
}

/**
 * Extension function to get kelas with most empty periods
 */
fun KelasKosongResponse.getKelasMostAffected(): Pair<String, Int>? {
    val grouped = groupByKelas()
    return grouped.maxByOrNull { it.value.size }?.let {
        it.key to it.value.size
    }
}