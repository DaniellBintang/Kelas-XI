<?php

namespace App\Filament\Imports;

use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class UserImport
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

            // Normalize: convert 'name' to 'nama' for validation and insert
            $normalizedData = [
                'nama' => $data['name'] ?? $data['nama'] ?? '',
                'email' => $data['email'] ?? '',
                'password' => $data['password'] ?? 'password',
                'role' => $data['role'] ?? 'siswa',
                'kelas' => $data['kelas'] ?? null,
            ];

            // Validation
            $validator = Validator::make($normalizedData, [
                'email' => 'required|email|unique:users,email',
                'nama' => 'required|string|max:255',
                'role' => 'required|in:admin,kurikulum,kepala_sekolah,siswa',
            ]);

            if ($validator->fails()) {
                $errors[] = "Row [{$normalizedData['email']}]: " . $validator->errors()->first();
                continue;
            }

            try {
                User::create([
                    'nama'      => $normalizedData['nama'],
                    'email'     => $normalizedData['email'],
                    'password'  => Hash::make($normalizedData['password']),
                    'role'      => $normalizedData['role'],
                    'kelas'     => $normalizedData['kelas'],
                ]);
                $imported++;
            } catch (\Exception $e) {
                $errors[] = "Error importing [{$normalizedData['email']}]: " . $e->getMessage();
            }
        }

        fclose($file);

        return [
            'imported' => $imported,
            'errors' => $errors
        ];
    }
}
