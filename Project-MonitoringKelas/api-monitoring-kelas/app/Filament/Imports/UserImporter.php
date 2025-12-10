<?php

namespace App\Filament\Imports;

use App\Models\User;
use Filament\Actions\Imports\ImportColumn;
use Filament\Actions\Imports\Importer;
use Filament\Actions\Imports\Models\Import;
use Illuminate\Support\Facades\Hash;

class UserImporter extends Importer
{
    protected static ?string $model = User::class;

    public static function getColumns(): array
    {
        return [
            ImportColumn::make('nama')
                ->label('Nama Lengkap')
                ->requiredMapping()
                ->rules(['required', 'max:255']),

            ImportColumn::make('email')
                ->label('Email')
                ->requiredMapping()
                ->rules(['required', 'email', 'unique:users,email']),

            ImportColumn::make('password')
                ->label('Password')
                ->requiredMapping()
                ->rules(['required', 'min:8'])
                ->mutateBeforeCreate(function ($state) {
                    return Hash::make($state);
                }),

            ImportColumn::make('role')
                ->label('Role')
                ->requiredMapping()
                ->rules(['required', 'in:siswa,kurikulum,kepala_sekolah,admin']),

            ImportColumn::make('kelas')
                ->label('Kelas')
                ->rules(['nullable', 'max:50']),

            ImportColumn::make('status')
                ->label('Status')
                ->rules(['required', 'in:aktif,nonaktif'])
                ->default('aktif'),
        ];
    }

    public function resolveRecord(): ?User
    {
        return User::firstOrNew([
            'email' => $this->data['email'],
        ]);
    }

    public static function getCompletedNotificationBody(Import $import): string
    {
        $body = 'Import user berhasil. ' . number_format($import->successful_rows) . ' user di-import.';

        if ($failedRowsCount = $import->getFailedRowsCount()) {
            $body .= ' ' . number_format($failedRowsCount) . ' user gagal di-import.';
        }

        return $body;
    }
}
