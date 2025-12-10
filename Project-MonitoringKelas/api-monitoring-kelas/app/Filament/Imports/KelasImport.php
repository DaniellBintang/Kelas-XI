<?php

namespace App\Filament\Imports;

use App\Models\Kelas;
use App\Models\Guru;
use Illuminate\Support\Facades\Validator;

class KelasImport
{
    public static function import($filePath)
    {
        $file = fopen($filePath, 'r');
        $headers = fgetcsv($file); // Read header row
        $imported = 0;
        $errors = [];

        while (($row = fgetcsv($file)) !== false) {
            if (empty(array_filter($row))) continue; // Skip empty rows

            $data = array_combine($headers, $row);

            // Normalize field names (lowercase dan replace spasi dengan underscore)
            $normalizedData = [];
            foreach ($data as $key => $value) {
                // Convert "Nama Kelas" -> "nama_kelas"
                $normalizedKey = strtolower(str_replace(' ', '_', trim($key)));
                $normalizedData[$normalizedKey] = $value;
            }

            // Validation rules
            $validator = Validator::make($normalizedData, [
                'nama_kelas' => 'required|string|max:50',
                'wali_kelas' => 'nullable|string|max:100',
                'ruangan' => 'nullable|string|max:50',
                'kapasitas' => 'nullable|integer|min:1',
                'jumlah_siswa' => 'nullable|integer|min:0',
                'status' => 'nullable|in:Aktif,Nonaktif',
            ]);

            if ($validator->fails()) {
                $errors[] = "Row: " . implode(', ', $data) . " - Errors: " . $validator->errors()->first();
                continue;
            }

            try {
                // ✅ PERBAIKAN: Ambil nama_kelas langsung (sudah format lengkap: "X TKJ 1")
                $namaKelas = trim($normalizedData['nama_kelas']);

                // Parse nama_kelas untuk extract tingkat, jurusan, nomor
                // Format: "X TKJ 1" -> tingkat=X, jurusan=TKJ, nomor=1
                $parts = explode(' ', $namaKelas);

                $tingkat = $parts[0] ?? 'X'; // X, XI, XII
                $jurusan = $parts[1] ?? 'RPL'; // TKJ, RPL, MM, dll
                $nomor = $parts[2] ?? '1'; // 1, 2, 3, dst

                // Get wali kelas
                $waliKelas = !empty($normalizedData['wali_kelas'])
                    ? trim($normalizedData['wali_kelas'])
                    : null;

                // Get other fields with defaults
                $ruangan = !empty($normalizedData['ruangan'])
                    ? trim($normalizedData['ruangan'])
                    : null;

                $kapasitas = !empty($normalizedData['kapasitas'])
                    ? (int)$normalizedData['kapasitas']
                    : 36;

                $jumlahSiswa = !empty($normalizedData['jumlah_siswa'])
                    ? (int)$normalizedData['jumlah_siswa']
                    : 0;

                $status = !empty($normalizedData['status'])
                    ? $normalizedData['status']
                    : 'Aktif';

                // ✅ PERBAIKAN: Simpan hanya NOMOR kelas di nama_kelas (bukan nama lengkap)
                // Database: nama_kelas = "1", tingkat = "X", jurusan = "TKJ"
                // Accessor getNamaLengkapAttribute() akan gabung jadi "X TKJ 1"
                Kelas::create([
                    'nama_kelas'    => $nomor,          // "1" (BUKAN "X TKJ 1")
                    'tingkat'       => $tingkat,        // "X"
                    'jurusan'       => $jurusan,        // "TKJ"
                    'wali_kelas'    => $waliKelas,
                    'ruangan'       => $ruangan,
                    'kapasitas'     => $kapasitas,
                    'jumlah_siswa'  => $jumlahSiswa,
                    'status'        => $status,
                ]);
                $imported++;
            } catch (\Exception $e) {
                $errors[] = "Error importing row: " . $e->getMessage();
            }
        }

        fclose($file);

        return [
            'imported' => $imported,
            'errors' => $errors
        ];
    }
}
