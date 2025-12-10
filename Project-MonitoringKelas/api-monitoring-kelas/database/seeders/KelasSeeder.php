<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class KelasSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Hapus data kelas yang sudah ada
        DB::table('kelas')->truncate();

        $now = Carbon::now();
        $kelas = [];

        // Ambil data guru untuk dijadikan wali kelas
        $guruList = DB::table('guru')->orderBy('kode_guru')->get();
        $guruIndex = 0;

        // Data kelas untuk setiap tingkat (X, XI, XII) dan jurusan RPL
        $tingkatList = [
            ['tingkat' => 'X', 'label' => 'X'],
            ['tingkat' => 'XI', 'label' => 'XI'],
            ['tingkat' => 'XII', 'label' => 'XII'],
        ];

        foreach ($tingkatList as $tingkat) {
            for ($i = 1; $i <= 3; $i++) {
                // Ambil guru untuk wali kelas secara berurutan
                $waliKelas = $guruIndex < count($guruList) ? $guruList[$guruIndex]->nama : null;
                $guruIndex++;

                $kelas[] = [
                    'nama_kelas' => (string)$i,
                    'tingkat' => $tingkat['tingkat'],
                    'jurusan' => 'RPL',
                    'wali_kelas' => $waliKelas,
                    'ruangan' => 'R.' . $tingkat['label'] . '0' . $i,
                    'kapasitas' => 36,
                    'jumlah_siswa' => 0,
                    'keterangan' => 'Kelas ' . $tingkat['label'] . ' RPL ' . $i,
                    'status' => 'Aktif',
                    'created_at' => $now,
                    'updated_at' => $now,
                ];
            }
        }

        // Insert semua data kelas
        DB::table('kelas')->insert($kelas);

        $this->command->info('Berhasil membuat ' . count($kelas) . ' data kelas (X RPL 1-3, XI RPL 1-3, XII RPL 1-3)');
    }
}
