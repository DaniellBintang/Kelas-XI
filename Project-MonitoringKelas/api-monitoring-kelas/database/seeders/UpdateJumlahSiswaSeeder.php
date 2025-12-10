<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use App\Models\Kelas;
use App\Models\User;

class UpdateJumlahSiswaSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $kelas = Kelas::all();

        foreach ($kelas as $k) {
            // Format nama kelas lengkap: "XI RPL 1"
            $namaKelasLengkap = "{$k->tingkat} {$k->jurusan} {$k->nama_kelas}";

            // Hitung siswa dengan kelas yang sesuai
            $jumlahSiswa = User::where('role', 'siswa')
                ->where('kelas', $namaKelasLengkap)
                ->where('status', 'aktif')
                ->count();

            // Update jumlah siswa
            $k->jumlah_siswa = $jumlahSiswa;
            $k->save();

            $this->command->info("Kelas {$namaKelasLengkap}: {$jumlahSiswa} siswa");
        }

        $this->command->info('✓ Jumlah siswa berhasil diupdate');
    }
}
