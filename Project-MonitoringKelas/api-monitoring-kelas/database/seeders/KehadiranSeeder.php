<?php

namespace Database\Seeders;

use App\Models\Kehadiran;
use App\Models\Jadwal;
use App\Models\Guru;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Carbon\Carbon;

class KehadiranSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Ambil beberapa jadwal dan guru untuk sample data
        $jadwals = Jadwal::take(10)->get();
        $gurus = Guru::all();

        if ($jadwals->isEmpty() || $gurus->isEmpty()) {
            $this->command->warn('Tidak ada data jadwal atau guru. Pastikan JadwalSeeder dan GuruSeeder sudah dijalankan.');
            return;
        }

        $statuses = ['Hadir', 'Telat', 'Tidak Hadir', 'Izin'];
        $keterangans = [
            'Hadir' => ['Tepat waktu', 'Masuk normal', null],
            'Telat' => ['Terlambat 15 menit karena macet', 'Terlambat 30 menit', 'Terlambat karena urusan keluarga'],
            'Tidak Hadir' => ['Sakit', 'Ada keperluan mendadak', 'Tanpa keterangan'],
            'Izin' => ['Mengikuti seminar', 'Keperluan keluarga', 'Dinas luar kota'],
        ];

        // Generate data kehadiran untuk 30 hari terakhir
        for ($i = 0; $i < 30; $i++) {
            $tanggal = Carbon::now()->subDays($i);

            // Hanya generate untuk hari Senin - Jumat
            if ($tanggal->isWeekend()) {
                continue;
            }

            foreach ($jadwals as $jadwal) {
                // Random: 70% hadir, 15% telat, 10% izin, 5% tidak hadir
                $rand = rand(1, 100);
                if ($rand <= 70) {
                    $status = 'Hadir';
                    $jamMasuk = '07:' . str_pad(rand(0, 30), 2, '0', STR_PAD_LEFT);
                    $jamKeluar = '14:' . str_pad(rand(0, 59), 2, '0', STR_PAD_LEFT);
                } elseif ($rand <= 85) {
                    $status = 'Telat';
                    $jamMasuk = '07:' . str_pad(rand(31, 59), 2, '0', STR_PAD_LEFT);
                    $jamKeluar = '14:' . str_pad(rand(0, 59), 2, '0', STR_PAD_LEFT);
                } elseif ($rand <= 95) {
                    $status = 'Izin';
                    $jamMasuk = null;
                    $jamKeluar = null;
                } else {
                    $status = 'Tidak Hadir';
                    $jamMasuk = null;
                    $jamKeluar = null;
                }

                // Cari guru berdasarkan kode_guru di jadwal
                $guru = $gurus->where('kode_guru', $jadwal->kode_guru)->first();

                Kehadiran::create([
                    'jadwal_id' => $jadwal->id,
                    'guru_id' => $guru ? $guru->id : null,
                    'tanggal' => $tanggal->format('Y-m-d'),
                    'jam_masuk' => $jamMasuk,
                    'jam_keluar' => $jamKeluar,
                    'mata_pelajaran' => $jadwal->mata_pelajaran,
                    'nama_guru' => $jadwal->nama_guru,
                    'kode_guru' => $jadwal->kode_guru,
                    'status' => $status,
                    'keterangan' => $keterangans[$status][array_rand($keterangans[$status])],
                ]);
            }
        }

        $this->command->info('Data kehadiran guru berhasil ditambahkan.');
    }
}
