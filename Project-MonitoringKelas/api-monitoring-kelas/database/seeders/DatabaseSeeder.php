<?php

namespace Database\Seeders;

use App\Models\User;
use App\Models\Jadwal;
use App\Models\Kehadiran;
use App\Models\Tugas;
use App\Models\Guru;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\DB;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // Disable foreign key checks
        DB::statement('SET FOREIGN_KEY_CHECKS=0;');

        // Clear existing data
        User::truncate();
        Jadwal::truncate();
        Guru::truncate();
        Kehadiran::truncate();
        Tugas::truncate();

        // Enable foreign key checks
        DB::statement('SET FOREIGN_KEY_CHECKS=1;');

        // ============================================
        // 1. SEED USERS (7 users dengan kelas & status)
        // ============================================

        // ✅ FIX: Admin dengan nama lengkap
        $admin = User::create([
            'nama' => 'Admin Sekolah', // ← TAMBAHKAN INI
            'email' => 'admin@sekolah.com',
            'password' => Hash::make('admin123'),
            'role' => 'admin',
            'kelas' => null,
            'status' => 'aktif'
        ]);

        // Kepala Sekolah
        $kepsek = User::create([
            'nama' => 'Dr. Suparman, M.Pd.', // ← TAMBAHKAN INI
            'email' => 'kepsek@sekolah.com',
            'password' => Hash::make('kepsek123'),
            'role' => 'kepala_sekolah',
            'kelas' => null,
            'status' => 'aktif'
        ]);

        // Kurikulum
        $kurikulum = User::create([
            'nama' => 'Dra. Siti Aminah, M.Pd.', // ← TAMBAHKAN INI
            'email' => 'kurikulum@sekolah.com',
            'password' => Hash::make('kurikulum123'),
            'role' => 'kurikulum',
            'kelas' => null,
            'status' => 'aktif'
        ]);

        // Siswa 1 - XI RPL 1
        $andi = User::create([
            'nama' => 'Andi Pratama', // ← TAMBAHKAN INI
            'email' => 'andi@siswa.com',
            'password' => Hash::make('siswa123'),
            'role' => 'siswa',
            'kelas' => 'XI RPL 1',
            'status' => 'aktif'
        ]);

        // Siswa 2 - XI RPL 1
        $budi = User::create([
            'nama' => 'Budi Santoso', // ← TAMBAHKAN INI
            'email' => 'budi@siswa.com',
            'password' => Hash::make('siswa123'),
            'role' => 'siswa',
            'kelas' => 'XI RPL 1',
            'status' => 'aktif'
        ]);

        // Siswa 3 - X RPL 2
        $citra = User::create([
            'nama' => 'Citra Dewi', // ← TAMBAHKAN INI
            'email' => 'citra@siswa.com',
            'password' => Hash::make('siswa123'),
            'role' => 'siswa',
            'kelas' => 'X RPL 2',
            'status' => 'aktif'
        ]);

        // Siswa 4 - XII RPL 3 (Non-Aktif)
        $dimas = User::create([
            'nama' => 'Dimas Prasetyo', // ← TAMBAHKAN INI
            'email' => 'dimas@siswa.com',
            'password' => Hash::make('siswa123'),
            'role' => 'siswa',
            'kelas' => 'XII RPL 3',
            'status' => 'aktif'
        ]);

        $this->command->info('✓ 7 Users created successfully (with kelas & status)');

        // ============================================
        // 2. SEED GURU DATA (Harus sebelum jadwal)
        // ============================================
        $this->call(GuruSeeder::class);
        $this->command->info('✓ Guru data seeded successfully');

        // ============================================
        // 3. SEED KELAS DATA
        // ============================================
        $this->call(KelasSeeder::class);
        $this->command->info('✓ Kelas data seeded successfully');

        // ============================================
        // 4. SEED JADWAL LENGKAP (Senin - Jumat)
        // ============================================
        $this->call(JadwalSeeder::class);
        $this->command->info('✓ Jadwal data seeded successfully');

        // ============================================
        // 5. SEED KEHADIRAN GURU (Sample Data)
        // ============================================

        // Ambil beberapa jadwal untuk sample kehadiran
        $jadwalSamples = Jadwal::whereNotNull('nama_guru')
            ->where('nama_guru', '!=', '')
            ->where('nama_guru', '!=', '-')
            ->limit(10)
            ->get();

        // Di bagian seeding kehadiran
        foreach ($jadwalSamples as $index => $jadwal) {
            $guru = Guru::where('kode_guru', $jadwal->kode_guru)->first();

            $statusOptions = ['Hadir', 'Telat', 'Tidak Hadir', 'Izin'];
            $status = $statusOptions[$index % 4];

            Kehadiran::create([
                'jadwal_id' => $jadwal->id,
                'guru_id' => $guru?->id,
                'tanggal' => now()->subDays(rand(0, 7))->format('Y-m-d'),
                'jam_masuk' => $status === 'Hadir' ? '07:00' : ($status === 'Telat' ? '07:15' : null),
                'jam_keluar' => in_array($status, ['Hadir', 'Telat']) ? '08:30' : null,
                'mata_pelajaran' => $jadwal->mata_pelajaran,
                'kelas' => $jadwal->kelas, // ✅ TAMBAHKAN
                'nama_guru' => $jadwal->nama_guru,
                'kode_guru' => $jadwal->kode_guru,
                'status' => $status,
                'keterangan' => match ($status) {
                    'Hadir' => 'Guru hadir tepat waktu',
                    'Telat' => 'Terlambat 15 menit',
                    'Tidak Hadir' => 'Tidak ada keterangan',
                    'Izin' => 'Sakit dengan surat dokter',
                }
            ]);
        }

        $this->command->info('✓ 10 sample Kehadiran Guru created');

        // ============================================
        // 5. SEED TUGAS (untuk Siswa)
        // ============================================

        Tugas::create([
            'user_id' => $andi->id,
            'tanggal' => now()->subDays(3)->format('Y-m-d'),
            'mata_pelajaran' => 'Matematika',
            'judul_tugas' => 'Integral dan Turunan',
            'status' => 'Selesai'
        ]);

        Tugas::create([
            'user_id' => $andi->id,
            'tanggal' => now()->subDays(2)->format('Y-m-d'),
            'mata_pelajaran' => 'Pemrograman Web',
            'judul_tugas' => 'Website Portfolio',
            'status' => 'Belum Selesai'
        ]);

        Tugas::create([
            'user_id' => $budi->id,
            'tanggal' => now()->subDays(1)->format('Y-m-d'),
            'mata_pelajaran' => 'Basis Data',
            'judul_tugas' => 'ERD Sistem Perpustakaan',
            'status' => 'Selesai'
        ]);

        Tugas::create([
            'user_id' => $citra->id,
            'tanggal' => now()->format('Y-m-d'),
            'mata_pelajaran' => 'Pemrograman Dasar',
            'judul_tugas' => 'Aplikasi Kalkulator',
            'status' => 'Belum Selesai'
        ]);

        $this->command->info('✓ 4 Tugas created successfully');

        // ============================================
        // 6. UPDATE JUMLAH SISWA DI KELAS
        // ============================================
        $this->call(UpdateJumlahSiswaSeeder::class);
        $this->command->info('✓ Jumlah siswa updated successfully');

        // ============================================
        // SUMMARY
        // ============================================
        $this->command->info('');
        $this->command->info('========================================');
        $this->command->info('Database seeded successfully!');
        $this->command->info('========================================');
        $this->command->info('Users: 7 users created');
        $this->command->info('  - Admin: admin@sekolah.com');
        $this->command->info('  - Kepala Sekolah: kepsek@sekolah.com');
        $this->command->info('  - Kurikulum: kurikulum@sekolah.com');
        $this->command->info('  - Siswa XI RPL 1: andi@sekolah.com, budi@sekolah.com');
        $this->command->info('  - Siswa X RPL 2: citra@sekolah.com');
        $this->command->info('  - Siswa XII RPL 3: dimas@sekolah.com (nonaktif)');
        $this->command->info('  Password: password123');
        $this->command->info('');
        $this->command->info('Jadwal: Dynamic jadwal for all classes');
        $this->command->info('  - Automatically created for all active classes');
        $this->command->info('  - Based on tingkat (X, XI, XII)');
        $this->command->info('');
        $this->command->info('Guru: 10 guru with various subjects');
        $this->command->info('Kelas: 9 kelas RPL (X, XI, XII)');
        $this->command->info('Kehadiran Guru: 10 sample records');
        $this->command->info('Tugas: 4 assignments');
        $this->command->info('========================================');
    }
}
