<?php

namespace App\Filament\Imports;

use App\Models\Guru;
use Illuminate\Support\Facades\Validator;

class GuruImport
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
                'kode_guru' => 'required|string|unique:guru,kode_guru|max:10',
                'nama' => 'required|string|max:255',
                'mata_pelajaran' => 'required|string|max:255',
                'email' => 'nullable|email',
                'status' => 'nullable|in:Aktif,Cuti,Nonaktif',
            ]);

            if ($validator->fails()) {
                $errors[] = "Row: " . implode(', ', $data) . " - Errors: " . $validator->errors()->first();
                continue;
            }

            try {
                Guru::create([
                    'kode_guru'         => $data['kode_guru'],
                    'nama'              => $data['nama'],
                    'mata_pelajaran'    => $data['mata_pelajaran'],
                    'email'             => $data['email'] ?? null,
                    'no_telepon'        => $data['no_telepon'] ?? $data['telepon'] ?? null,
                    'alamat'            => $data['alamat'] ?? null,
                    'status'            => $data['status'] ?? 'Aktif',
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
