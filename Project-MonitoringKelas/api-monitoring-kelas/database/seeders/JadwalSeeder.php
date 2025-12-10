<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Jadwal;
use App\Models\Kelas;
use App\Models\Guru;

class JadwalSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Ambil semua kelas yang aktif
        $kelasList = Kelas::where('status', 'Aktif')->get();

        $totalJadwal = 0;

        foreach ($kelasList as $kelas) {
            $namaKelasLengkap = "{$kelas->tingkat} {$kelas->jurusan} {$kelas->nama_kelas}";
            $ruanganKelas = $kelas->ruangan; // Ambil ruangan dari data kelas

            // Template jadwal berdasarkan tingkat
            $jadwalTemplate = $this->getJadwalByTingkat($kelas->tingkat, $namaKelasLengkap, $ruanganKelas);

            foreach ($jadwalTemplate as $jadwal) {
                Jadwal::create($jadwal);
                $totalJadwal++;
            }

            $this->command->info("✓ Jadwal {$namaKelasLengkap}: " . count($jadwalTemplate) . " jadwal created");
        }

        $this->command->info("✓ Total {$totalJadwal} jadwal created for all classes");
    }

    private function getJadwalByTingkat($tingkat, $namaKelas, $ruangan)
    {
        if ($tingkat === 'X') {
            return $this->getJadwalKelasX($namaKelas, $ruangan);
        } elseif ($tingkat === 'XI') {
            return $this->getJadwalKelasXI($namaKelas, $ruangan);
        } elseif ($tingkat === 'XII') {
            return $this->getJadwalKelasXII($namaKelas, $ruangan);
        }

        return [];
    }

    private function getJadwalKelasX($namaKelas, $ruangan)
    {
        return [
            // SENIN
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G001', 'nama_guru' => 'Dr. Ahmad Wijaya, S.Pd., M.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Pemrograman Dasar', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Komputer 1'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Basis Data Dasar', 'kode_guru' => 'G015', 'nama_guru' => 'Maya Sari, S.Kom.', 'ruangan' => 'Lab Komputer 2'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],

            // SELASA
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Komputer dan Jaringan Dasar', 'kode_guru' => 'G017', 'nama_guru' => 'Firman Hidayat, S.Kom.', 'ruangan' => 'Lab Jaringan'],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Sistem Komputer', 'kode_guru' => 'G003', 'nama_guru' => 'Budi Santoso, S.Kom., M.T.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Pendidikan Agama Islam', 'kode_guru' => 'G019', 'nama_guru' => 'Yusuf Hidayat, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G002', 'nama_guru' => 'Siti Nurhaliza, S.Pd.', 'ruangan' => $ruangan],

            // RABU
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Seni Budaya', 'kode_guru' => 'G010', 'nama_guru' => 'Ani Suryani, S.Sn.', 'ruangan' => 'Lab Desain'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Pemrograman Dasar', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Komputer 1'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Fisika', 'kode_guru' => 'G004', 'nama_guru' => 'Dewi Lestari, S.Si.', 'ruangan' => 'Lab IPA'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G011', 'nama_guru' => 'Rudi Hartono, S.Pd., M.Si.', 'ruangan' => $ruangan],

            // KAMIS
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Kewarganegaraan', 'kode_guru' => 'G020', 'nama_guru' => 'Lisa Maharani, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Basis Data Dasar', 'kode_guru' => 'G016', 'nama_guru' => 'Dedi Kurniawan, S.T.', 'ruangan' => 'Lab Komputer 2'],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],

            // JUMAT
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Jasmani', 'kode_guru' => 'G009', 'nama_guru' => 'Muhammad Rizki, S.Pd.', 'ruangan' => 'Lapangan'],
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G012', 'nama_guru' => 'Bambang Sutrisno, S.Pd.', 'ruangan' => $ruangan],
        ];
    }

    private function getJadwalKelasXI($namaKelas, $ruangan)
    {
        return [
            // SENIN
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G001', 'nama_guru' => 'Dr. Ahmad Wijaya, S.Pd., M.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Pemrograman Web', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Komputer 1'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Basis Data', 'kode_guru' => 'G015', 'nama_guru' => 'Maya Sari, S.Kom.', 'ruangan' => 'Lab Komputer 2'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Jaringan Komputer', 'kode_guru' => 'G017', 'nama_guru' => 'Firman Hidayat, S.Kom.', 'ruangan' => 'Lab Jaringan'],

            // SELASA
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pemrograman Mobile', 'kode_guru' => 'G014', 'nama_guru' => 'Agus Wijaya, S.T.', 'ruangan' => 'Lab Android'],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Informatika', 'kode_guru' => 'G003', 'nama_guru' => 'Budi Santoso, S.Kom., M.T.', 'ruangan' => 'Lab Komputer 1'],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G011', 'nama_guru' => 'Rudi Hartono, S.Pd., M.Si.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Pendidikan Agama Islam', 'kode_guru' => 'G019', 'nama_guru' => 'Yusuf Hidayat, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G002', 'nama_guru' => 'Siti Nurhaliza, S.Pd.', 'ruangan' => $ruangan],

            // RABU
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Seni Budaya', 'kode_guru' => 'G010', 'nama_guru' => 'Ani Suryani, S.Sn.', 'ruangan' => 'Lab Desain'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Pemrograman Web', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Server'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Basis Data', 'kode_guru' => 'G016', 'nama_guru' => 'Dedi Kurniawan, S.T.', 'ruangan' => 'Lab Komputer 2'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Fisika', 'kode_guru' => 'G004', 'nama_guru' => 'Dewi Lestari, S.Si.', 'ruangan' => 'Lab IPA'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Jaringan Komputer', 'kode_guru' => 'G018', 'nama_guru' => 'Rina Wati, S.Kom.', 'ruangan' => 'Lab Jaringan'],

            // KAMIS
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Kewarganegaraan', 'kode_guru' => 'G020', 'nama_guru' => 'Lisa Maharani, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Pemrograman Mobile', 'kode_guru' => 'G014', 'nama_guru' => 'Agus Wijaya, S.T.', 'ruangan' => 'Lab Android'],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Informatika', 'kode_guru' => 'G003', 'nama_guru' => 'Budi Santoso, S.Kom., M.T.', 'ruangan' => 'Lab Komputer 1'],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Sejarah', 'kode_guru' => 'G007', 'nama_guru' => 'Hendra Gunawan, S.Pd.', 'ruangan' => $ruangan],

            // JUMAT
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Jasmani', 'kode_guru' => 'G009', 'nama_guru' => 'Muhammad Rizki, S.Pd.', 'ruangan' => 'Lapangan'],
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G001', 'nama_guru' => 'Dr. Ahmad Wijaya, S.Pd., M.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G012', 'nama_guru' => 'Bambang Sutrisno, S.Pd.', 'ruangan' => $ruangan],
        ];
    }

    private function getJadwalKelasXII($namaKelas, $ruangan)
    {
        return [
            // SENIN
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pemrograman Web Lanjut', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Server'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Basis Data Lanjut', 'kode_guru' => 'G015', 'nama_guru' => 'Maya Sari, S.Kom.', 'ruangan' => 'Lab Database'],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G001', 'nama_guru' => 'Dr. Ahmad Wijaya, S.Pd., M.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Senin', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Keamanan Jaringan', 'kode_guru' => 'G017', 'nama_guru' => 'Firman Hidayat, S.Kom.', 'ruangan' => 'Lab Keamanan'],

            // SELASA
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Mobile App Development', 'kode_guru' => 'G014', 'nama_guru' => 'Agus Wijaya, S.T.', 'ruangan' => 'Lab Mobile'],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Software Engineering', 'kode_guru' => 'G003', 'nama_guru' => 'Budi Santoso, S.Kom., M.T.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Cloud Computing', 'kode_guru' => 'G016', 'nama_guru' => 'Dedi Kurniawan, S.T.', 'ruangan' => 'Lab Cloud'],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Pendidikan Agama Islam', 'kode_guru' => 'G019', 'nama_guru' => 'Yusuf Hidayat, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Selasa', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G002', 'nama_guru' => 'Siti Nurhaliza, S.Pd.', 'ruangan' => $ruangan],

            // RABU
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'UI/UX Design Lanjut', 'kode_guru' => 'G010', 'nama_guru' => 'Ani Suryani, S.Sn.', 'ruangan' => 'Lab Desain Pro'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Web Service & API', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Server'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Matematika', 'kode_guru' => 'G011', 'nama_guru' => 'Rudi Hartono, S.Pd., M.Si.', 'ruangan' => $ruangan],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Pemrograman Web Lanjut', 'kode_guru' => 'G013', 'nama_guru' => 'Sari Purnama, S.Kom.', 'ruangan' => 'Lab Server'],
            ['hari' => 'Rabu', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Basis Data Lanjut', 'kode_guru' => 'G015', 'nama_guru' => 'Maya Sari, S.Kom.', 'ruangan' => 'Lab Database'],

            // KAMIS
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Kewarganegaraan', 'kode_guru' => 'G020', 'nama_guru' => 'Lisa Maharani, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Mobile App Development', 'kode_guru' => 'G014', 'nama_guru' => 'Agus Wijaya, S.T.', 'ruangan' => 'Lab Mobile'],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Keamanan Jaringan', 'kode_guru' => 'G017', 'nama_guru' => 'Firman Hidayat, S.Kom.', 'ruangan' => 'Lab Keamanan'],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '12:30-14:00', 'mata_pelajaran' => 'Bahasa Inggris', 'kode_guru' => 'G005', 'nama_guru' => 'Eko Prasetyo, S.Pd.', 'ruangan' => $ruangan],
            ['hari' => 'Kamis', 'kelas' => $namaKelas, 'jam' => '14:00-15:30', 'mata_pelajaran' => 'Software Engineering', 'kode_guru' => 'G003', 'nama_guru' => 'Budi Santoso, S.Kom., M.T.', 'ruangan' => $ruangan],

            // JUMAT
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '07:00-08:30', 'mata_pelajaran' => 'Pendidikan Jasmani', 'kode_guru' => 'G009', 'nama_guru' => 'Muhammad Rizki, S.Pd.', 'ruangan' => 'Lapangan'],
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '08:30-10:00', 'mata_pelajaran' => 'Cloud Computing', 'kode_guru' => 'G016', 'nama_guru' => 'Dedi Kurniawan, S.T.', 'ruangan' => 'Lab Cloud'],
            ['hari' => 'Jumat', 'kelas' => $namaKelas, 'jam' => '10:15-11:45', 'mata_pelajaran' => 'Bahasa Indonesia', 'kode_guru' => 'G012', 'nama_guru' => 'Bambang Sutrisno, S.Pd.', 'ruangan' => $ruangan],
        ];
    }
}
