package com.example.aplikasimonitoringkelas.data.dummy

import com.example.aplikasimonitoringkelas.data.remote.response.JadwalItem
import com.example.aplikasimonitoringkelas.data.remote.response.KehadiranItem
import com.example.aplikasimonitoringkelas.data.remote.response.TugasItem
import com.example.aplikasimonitoringkelas.data.remote.response.UserData
import com.example.aplikasimonitoringkelas.data.remote.response.UserItem

/**
 * Dummy Data Object
 * Centralized dummy data untuk development/testing tanpa backend
 */
object DummyData {

    // ==================== JADWAL DUMMY DATA ====================

    /**
     * Dummy Jadwal Data
     * Organized by Hari (day of week)
     */
    val jadwalMap = mapOf(
        "Senin" to listOf(
            JadwalItem(
                id = 1,
                hari = "Senin",
                kelas = "XI RPL",
                jam = "07:00-08:30",
                mataPelajaran = "Matematika",
                kodeGuru = "MT001",
                namaGuru = "Pak Ahmad Yusuf",
                ruangan = "Lab 1",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 2,
                hari = "Senin",
                kelas = "XI RPL",
                jam = "08:30-10:00",
                mataPelajaran = "Pemrograman Web",
                kodeGuru = "PW002",
                namaGuru = "Bu Sari Dewi",
                ruangan = "Lab Komputer",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 3,
                hari = "Senin",
                kelas = "XI RPL",
                jam = "10:15-11:45",
                mataPelajaran = "Basis Data",
                kodeGuru = "BD003",
                namaGuru = "Pak Budi Santoso",
                ruangan = "Lab 2",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 4,
                hari = "Senin",
                kelas = "XI RPL",
                jam = "13:00-14:30",
                mataPelajaran = "Bahasa Inggris",
                kodeGuru = "EN004",
                namaGuru = "Bu Maya Sari",
                ruangan = "Kelas 11A",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            )
        ),
        "Selasa" to listOf(
            JadwalItem(
                id = 5,
                hari = "Selasa",
                kelas = "XI RPL",
                jam = "07:00-08:30",
                mataPelajaran = "Pemrograman Mobile",
                kodeGuru = "PM005",
                namaGuru = "Pak Dedi Kurniawan",
                ruangan = "Lab Android",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 6,
                hari = "Selasa",
                kelas = "XI RPL",
                jam = "08:30-10:00",
                mataPelajaran = "Algoritma",
                kodeGuru = "AL006",
                namaGuru = "Bu Rina Wati",
                ruangan = "Kelas 11A",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 7,
                hari = "Selasa",
                kelas = "XI RPL",
                jam = "10:15-11:45",
                mataPelajaran = "Jaringan Komputer",
                kodeGuru = "JK007",
                namaGuru = "Pak Eko Prasetyo",
                ruangan = "Lab Jaringan",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 8,
                hari = "Selasa",
                kelas = "XI RPL",
                jam = "13:00-14:30",
                mataPelajaran = "PKN",
                kodeGuru = "PK008",
                namaGuru = "Bu Lisa Maharani",
                ruangan = "Kelas 11A",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            )
        ),
        "Rabu" to listOf(
            JadwalItem(
                id = 9,
                hari = "Rabu",
                kelas = "XI RPL",
                jam = "07:00-08:30",
                mataPelajaran = "Sistem Operasi",
                kodeGuru = "SO009",
                namaGuru = "Pak Hendra Wijaya",
                ruangan = "Lab Komputer",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 10,
                hari = "Rabu",
                kelas = "XI RPL",
                jam = "08:30-10:00",
                mataPelajaran = "Grafik Komputer",
                kodeGuru = "GK010",
                namaGuru = "Bu Nina Safitri",
                ruangan = "Lab Design",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            ),
            JadwalItem(
                id = 11,
                hari = "Rabu",
                kelas = "XI RPL",
                jam = "10:15-11:45",
                mataPelajaran = "Pemrograman Java",
                kodeGuru = "PJ011",
                namaGuru = "Pak Rudi Hermawan",
                ruangan = "Lab Java",
                createdAt = "2024-01-15T10:00:00.000000Z",
                updatedAt = "2024-01-15T10:00:00.000000Z"
            )
        )
    )

    /**
     * Get jadwal by hari and kelas
     */
    fun getJadwalByHariKelas(hari: String?, kelas: String?): List<JadwalItem> {
        return if (hari != null) {
            jadwalMap[hari]?.filter {
                kelas == null || it.kelas == kelas
            } ?: emptyList()
        } else {
            jadwalMap.values.flatten().filter {
                kelas == null || it.kelas == kelas
            }
        }
    }

    // Add this function to DummyData object:

    fun getDummyUsers(): List<UserItem> {
        return listOf(
            UserItem(
                id = 1,
                nama = "Ahmad Rizki",
                email = "ahmad.rizki@sekolah.com",
                role = "siswa",
                kelas = "X RPL 1",
                status = "aktif",
                createdAt = "2024-01-10T08:00:00.000000Z",
                updatedAt = "2024-01-10T08:00:00.000000Z"
            ),
            UserItem(
                id = 2,
                nama = "Siti Nurhaliza",
                email = "siti.nur@sekolah.com",
                role = "siswa",
                kelas = "XI RPL 2",
                status = "aktif",
                createdAt = "2024-01-10T08:15:00.000000Z",
                updatedAt = "2024-01-10T08:15:00.000000Z"
            ),
            UserItem(
                id = 3,
                nama = "Budi Santoso",
                email = "budi.santoso@sekolah.com",
                role = "kurikulum",
                kelas = null,
                status = "aktif",
                createdAt = "2024-01-08T09:00:00.000000Z",
                updatedAt = "2024-01-08T09:00:00.000000Z"
            ),
            UserItem(
                id = 4,
                nama = "Dr. Sari Wijaya",
                email = "sari.wijaya@sekolah.com",
                role = "kepala_sekolah",
                kelas = null,
                status = "aktif",
                createdAt = "2024-01-05T07:30:00.000000Z",
                updatedAt = "2024-01-05T07:30:00.000000Z"
            ),
            UserItem(
                id = 5,
                nama = "Maya Anggraini",
                email = "maya.admin@sekolah.com",
                role = "admin",
                kelas = null,
                status = "aktif",
                createdAt = "2024-01-01T08:00:00.000000Z",
                updatedAt = "2024-01-01T08:00:00.000000Z"
            ),
            UserItem(
                id = 6,
                nama = "Dedi Kurniawan",
                email = "dedi.k@sekolah.com",
                role = "siswa",
                kelas = "XII RPL 1",
                status = "nonaktif",
                createdAt = "2024-01-12T10:00:00.000000Z",
                updatedAt = "2024-01-15T14:30:00.000000Z"
            ),
            UserItem(
                id = 7,
                nama = "Rina Wulandari",
                email = "rina.wulan@sekolah.com",
                role = "siswa",
                kelas = "X RPL 2",
                status = "aktif",
                createdAt = "2024-01-11T08:30:00.000000Z",
                updatedAt = "2024-01-11T08:30:00.000000Z"
            ),
            UserItem(
                id = 8,
                nama = "Eko Prasetyo",
                email = "eko.p@sekolah.com",
                role = "siswa",
                kelas = "XI RPL 1",
                status = "aktif",
                createdAt = "2024-01-09T09:15:00.000000Z",
                updatedAt = "2024-01-09T09:15:00.000000Z"
            )
        )
    }


    // ==================== KEHADIRAN DUMMY DATA ====================

    private val dummyUser = UserData(
        id = 4,
        nama = "Andi Siswa",
        email = "andi@sekolah.com",
        role = "siswa"
    )

    val kehadiranList = listOf(
        KehadiranItem(
            id = 1,
            userId = 4,
            tanggal = "2024-01-15",
            mataPelajaran = "Matematika",
            status = "Hadir",
            keterangan = null,
            createdAt = "2024-01-15T10:00:00.000000Z",
            updatedAt = "2024-01-15T10:00:00.000000Z",
            user = dummyUser
        ),
        KehadiranItem(
            id = 2,
            userId = 4,
            tanggal = "2024-01-15",
            mataPelajaran = "Pemrograman Web",
            status = "Hadir",
            keterangan = null,
            createdAt = "2024-01-15T11:00:00.000000Z",
            updatedAt = "2024-01-15T11:00:00.000000Z",
            user = dummyUser
        ),
        KehadiranItem(
            id = 3,
            userId = 4,
            tanggal = "2024-01-14",
            mataPelajaran = "Basis Data",
            status = "Sakit",
            keterangan = "Demam tinggi",
            createdAt = "2024-01-14T10:00:00.000000Z",
            updatedAt = "2024-01-14T10:00:00.000000Z",
            user = dummyUser
        ),
        KehadiranItem(
            id = 4,
            userId = 4,
            tanggal = "2024-01-14",
            mataPelajaran = "Bahasa Inggris",
            status = "Izin",
            keterangan = "Keperluan keluarga",
            createdAt = "2024-01-14T13:00:00.000000Z",
            updatedAt = "2024-01-14T13:00:00.000000Z",
            user = dummyUser
        )
    )

    /**
     * Get kehadiran by user ID
     */
    fun getKehadiranByUserId(userId: Int?): List<KehadiranItem> {
        return if (userId != null) {
            kehadiranList.filter { it.userId == userId }
        } else {
            kehadiranList
        }
    }


    // ==================== TUGAS DUMMY DATA ====================

    val tugasList = listOf(
        TugasItem(
            id = 1,
            userId = 4,
            tanggal = "2024-01-15",
            mataPelajaran = "Matematika",
            judulTugas = "Integral dan Turunan",
            status = "Selesai",
            createdAt = "2024-01-15T10:00:00.000000Z",
            updatedAt = "2024-01-15T10:00:00.000000Z",
            user = dummyUser
        ),
        TugasItem(
            id = 2,
            userId = 4,
            tanggal = "2024-01-14",
            mataPelajaran = "Pemrograman Web",
            judulTugas = "Membuat Website Portfolio",
            status = "Belum Selesai",
            createdAt = "2024-01-14T10:00:00.000000Z",
            updatedAt = "2024-01-14T10:00:00.000000Z",
            user = dummyUser
        ),
        TugasItem(
            id = 3,
            userId = 4,
            tanggal = "2024-01-13",
            mataPelajaran = "Basis Data",
            judulTugas = "ERD Sistem Perpustakaan",
            status = "Selesai",
            createdAt = "2024-01-13T10:00:00.000000Z",
            updatedAt = "2024-01-13T10:00:00.000000Z",
            user = dummyUser
        ),
        TugasItem(
            id = 4,
            userId = 4,
            tanggal = "2024-01-12",
            mataPelajaran = "Bahasa Inggris",
            judulTugas = "Essay About Technology",
            status = "Terlambat",
            createdAt = "2024-01-12T10:00:00.000000Z",
            updatedAt = "2024-01-12T10:00:00.000000Z",
            user = dummyUser
        )
    )

    fun getDummyJadwal(): List<JadwalItem> {
        return listOf(
            // Normal schedules with teachers
            JadwalItem(
                id = 1,
                hari = "Senin",
                kelas = "X RPL 1",
                jam = "07:00-08:30",
                mataPelajaran = "Matematika",
                kodeGuru = "MT001",
                namaGuru = "Pak Ahmad Yusuf",
                ruangan = "Lab 1"
            ),
            JadwalItem(
                id = 2,
                hari = "Senin",
                kelas = "XI RPL 1",
                jam = "08:30-10:00",
                mataPelajaran = "Pemrograman Web",
                kodeGuru = "PW002",
                namaGuru = "Bu Sari Dewi",
                ruangan = "Lab Komputer"
            ),

            // ✅ KELAS KOSONG - No teacher assigned
            JadwalItem(
                id = 3,
                hari = "Senin",
                kelas = "X RPL 1",
                jam = "10:15-11:45",
                mataPelajaran = "Pemrograman Dasar",
                kodeGuru = null, // ← No teacher code
                namaGuru = null, // ← No teacher name (KOSONG!)
                ruangan = "Lab Komputer"
            ),
            JadwalItem(
                id = 4,
                hari = "Senin",
                kelas = "XII RPL 1",
                jam = "08:30-10:00",
                mataPelajaran = "Sistem Operasi",
                kodeGuru = null,
                namaGuru = null, // ← KOSONG!
                ruangan = "Lab 2"
            ),

            // Normal schedule
            JadwalItem(
                id = 5,
                hari = "Selasa",
                kelas = "X RPL 1",
                jam = "07:00-08:30",
                mataPelajaran = "Bahasa Indonesia",
                kodeGuru = "BI003",
                namaGuru = "Bu Maya Sari",
                ruangan = "Kelas 10A"
            ),

            // ✅ KELAS KOSONG
            JadwalItem(
                id = 6,
                hari = "Selasa",
                kelas = "XI RPL 2",
                jam = "10:15-11:45",
                mataPelajaran = "Basis Data",
                kodeGuru = null,
                namaGuru = null, // ← KOSONG!
                ruangan = "Lab 1"
            )
        )
    }

    /**
     * Get tugas by user ID
     */
    fun getTugasByUserId(userId: Int?): List<TugasItem> {
        return if (userId != null) {
            tugasList.filter { it.userId == userId }
        } else {
            tugasList
        }
    }
}