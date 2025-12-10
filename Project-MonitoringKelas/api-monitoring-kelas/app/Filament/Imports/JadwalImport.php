<?php

namespace App\Filament\Imports;

use App\Models\Jadwal;
use App\Models\Guru;
use Illuminate\Support\Facades\Validator;

class JadwalImport
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

            // Validation
            $validator = Validator::make($data, [
                'hari' => 'required|in:Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu',
                'jam' => 'required|string',
                'kelas' => 'required|string|max:50',
                'ruangan' => 'required|string|max:50',
                'mata_pelajaran' => 'required|string|max:255',
                'kode_guru' => 'nullable|exists:guru,kode_guru',
            ]);

            if ($validator->fails()) {
                $errors[] = "Row: " . implode(', ', $data) . " - Errors: " . $validator->errors()->first();
                continue;
            }

            try {
                // Find guru by kode_guru or nama
                $namaGuru = null;
                if (!empty($data['kode_guru'])) {
                    $guru = Guru::where('kode_guru', $data['kode_guru'])->first();
                    $namaGuru = $guru ? $guru->nama : null;
                } elseif (!empty($data['nama_guru'])) {
                    $namaGuru = $data['nama_guru'];
                }

                Jadwal::create([
                    'hari'              => $data['hari'],
                    'jam'               => $data['jam'],
                    'kelas'             => $data['kelas'],
                    'ruangan'           => $data['ruangan'],
                    'mata_pelajaran'    => $data['mata_pelajaran'] ?? $data['mapel'] ?? '',
                    'kode_guru'         => $data['kode_guru'] ?? null,
                    'nama_guru'         => $namaGuru,
                ]);
                $imported++;
            } catch (\Exception $e) {
                $errors[] = "Error importing: " . $e->getMessage();
            }
        }

        fclose($file);

        return [
            'imported' => $imported,
            'errors' => $errors
        ];
    }
}
