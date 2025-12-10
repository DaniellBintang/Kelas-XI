<?php

namespace App\Filament\Exports;

use App\Models\User;
use Filament\Actions\Exports\ExportColumn;
use Filament\Actions\Exports\Exporter;
use Filament\Actions\Exports\Models\Export;

class UserExporter extends Exporter
{
    protected static ?string $model = User::class;

    public static function getColumns(): array
    {
        return [
            ExportColumn::make('id')
                ->label('ID'),

            ExportColumn::make('nama')
                ->label('Nama'),

            ExportColumn::make('email')
                ->label('Email'),

            ExportColumn::make('role')
                ->label('Role'),

            ExportColumn::make('kelas')
                ->label('Kelas'),

            ExportColumn::make('status')
                ->label('Status'),

            ExportColumn::make('created_at')
                ->label('Dibuat Pada'),

            ExportColumn::make('updated_at')
                ->label('Diupdate Pada'),
        ];
    }

    public static function getCompletedNotificationBody(Export $export): string
    {
        $body = 'Export user telah selesai. ' . number_format($export->successful_rows) . ' ' . str('baris')->plural($export->successful_rows) . ' berhasil diexport.';

        return $body;
    }
}
