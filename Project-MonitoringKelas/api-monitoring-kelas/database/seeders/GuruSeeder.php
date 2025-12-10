<?php
// filepath: database/seeders/GuruSeeder.php

namespace Database\Seeders;

use App\Models\Guru;
use Illuminate\Database\Seeder;

class GuruSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $gurus = [
            // ============================================
            // GURU MATEMATIKA (2 guru)
            // ============================================
            [
                'kode_guru' => 'G001',
                'nama' => 'Dr. Ahmad Wijaya, S.Pd., M.Pd.',
                'mata_pelajaran' => 'Matematika',
                'email' => 'ahmad.wijaya@school.com',
                'no_telepon' => '081234567890',
                'alamat' => 'Jl. Pendidikan No. 123, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G011',
                'nama' => 'Rudi Hartono, S.Pd., M.Si.',
                'mata_pelajaran' => 'Matematika',
                'email' => 'rudi.hartono@school.com',
                'no_telepon' => '081234567811',
                'alamat' => 'Jl. Angka No. 88, Jakarta',
                'status' => 'Aktif',
            ],

            // ============================================
            // GURU BAHASA INDONESIA (2 guru)
            // ============================================
            [
                'kode_guru' => 'G002',
                'nama' => 'Siti Nurhaliza, S.Pd.',
                'mata_pelajaran' => 'Bahasa Indonesia',
                'email' => 'siti.nurhaliza@school.com',
                'no_telepon' => '081234567891',
                'alamat' => 'Jl. Merdeka No. 45, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G012',
                'nama' => 'Bambang Sutrisno, S.Pd.',
                'mata_pelajaran' => 'Bahasa Indonesia',
                'email' => 'bambang.sutrisno@school.com',
                'no_telepon' => '081234567812',
                'alamat' => 'Jl. Sastra No. 22, Jakarta',
                'status' => 'Aktif',
            ],

            // ============================================
            // GURU INFORMATIKA / PEMROGRAMAN (3 guru)
            // ============================================
            [
                'kode_guru' => 'G003',
                'nama' => 'Budi Santoso, S.Kom., M.T.',
                'mata_pelajaran' => 'Informatika',
                'email' => 'budi.santoso@school.com',
                'no_telepon' => '081234567892',
                'alamat' => 'Jl. Teknologi No. 67, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G013',
                'nama' => 'Sari Purnama, S.Kom.',
                'mata_pelajaran' => 'Pemrograman Web',
                'email' => 'sari.purnama@school.com',
                'no_telepon' => '081234567813',
                'alamat' => 'Jl. Koding No. 101, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G014',
                'nama' => 'Agus Wijaya, S.T.',
                'mata_pelajaran' => 'Pemrograman Mobile',
                'email' => 'agus.wijaya@school.com',
                'no_telepon' => '081234567814',
                'alamat' => 'Jl. Android No. 77, Jakarta',
                'status' => 'Aktif',
            ],

            // ============================================
            // GURU BASIS DATA (2 guru)
            // ============================================
            [
                'kode_guru' => 'G015',
                'nama' => 'Maya Sari, S.Kom.',
                'mata_pelajaran' => 'Basis Data',
                'email' => 'maya.sari@school.com',
                'no_telepon' => '081234567815',
                'alamat' => 'Jl. Database No. 55, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G016',
                'nama' => 'Dedi Kurniawan, S.T.',
                'mata_pelajaran' => 'Basis Data',
                'email' => 'dedi.kurniawan@school.com',
                'no_telepon' => '081234567816',
                'alamat' => 'Jl. SQL No. 33, Jakarta',
                'status' => 'Aktif',
            ],

            // ============================================
            // GURU JARINGAN KOMPUTER (2 guru)
            // ============================================
            [
                'kode_guru' => 'G017',
                'nama' => 'Firman Hidayat, S.Kom.',
                'mata_pelajaran' => 'Jaringan Komputer',
                'email' => 'firman.hidayat@school.com',
                'no_telepon' => '081234567817',
                'alamat' => 'Jl. Network No. 44, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G018',
                'nama' => 'Rina Wati, S.T.',
                'mata_pelajaran' => 'Jaringan Komputer',
                'email' => 'rina.wati@school.com',
                'no_telepon' => '081234567818',
                'alamat' => 'Jl. LAN No. 66, Jakarta',
                'status' => 'Aktif',
            ],

            // ============================================
            // GURU LAINNYA (existing)
            // ============================================
            [
                'kode_guru' => 'G004',
                'nama' => 'Dewi Lestari, S.Si.',
                'mata_pelajaran' => 'Fisika',
                'email' => 'dewi.lestari@school.com',
                'no_telepon' => '081234567893',
                'alamat' => 'Jl. Sains No. 89, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G005',
                'nama' => 'Eko Prasetyo, S.Pd.',
                'mata_pelajaran' => 'Bahasa Inggris',
                'email' => 'eko.prasetyo@school.com',
                'no_telepon' => '081234567894',
                'alamat' => 'Jl. Internasional No. 12, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G006',
                'nama' => 'Ratna Sari, S.Pd.',
                'mata_pelajaran' => 'Kimia',
                'email' => 'ratna.sari@school.com',
                'no_telepon' => '081234567895',
                'alamat' => 'Jl. Laboratorium No. 34, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G007',
                'nama' => 'Hendra Gunawan, S.Pd.',
                'mata_pelajaran' => 'Sejarah',
                'email' => 'hendra.gunawan@school.com',
                'no_telepon' => '081234567896',
                'alamat' => 'Jl. Nusantara No. 56, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G008',
                'nama' => 'Linda Wati, S.Pd.',
                'mata_pelajaran' => 'Biologi',
                'email' => 'linda.wati@school.com',
                'no_telepon' => '081234567897',
                'alamat' => 'Jl. Alam No. 78, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G009',
                'nama' => 'Muhammad Rizki, S.Pd.',
                'mata_pelajaran' => 'Pendidikan Jasmani',
                'email' => 'muhammad.rizki@school.com',
                'no_telepon' => '081234567898',
                'alamat' => 'Jl. Olahraga No. 90, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G010',
                'nama' => 'Ani Suryani, S.Sn.',
                'mata_pelajaran' => 'Seni Budaya',
                'email' => 'ani.suryani@school.com',
                'no_telepon' => '081234567899',
                'alamat' => 'Jl. Seni No. 11, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G019',
                'nama' => 'Yusuf Hidayat, S.Pd.',
                'mata_pelajaran' => 'Pendidikan Agama Islam',
                'email' => 'yusuf.hidayat@school.com',
                'no_telepon' => '081234567819',
                'alamat' => 'Jl. Iman No. 99, Jakarta',
                'status' => 'Aktif',
            ],
            [
                'kode_guru' => 'G020',
                'nama' => 'Lisa Maharani, S.Pd.',
                'mata_pelajaran' => 'Pendidikan Kewarganegaraan',
                'email' => 'lisa.maharani@school.com',
                'no_telepon' => '081234567820',
                'alamat' => 'Jl. Pancasila No. 17, Jakarta',
                'status' => 'Aktif',
            ],
        ];

        foreach ($gurus as $guru) {
            Guru::create($guru);
        }

        $this->command->info('✓ 20 Guru created successfully');
    }
}
