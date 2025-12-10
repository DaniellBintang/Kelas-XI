<?php

namespace App\Filament\Resources\KelasResource\Pages;

use App\Filament\Resources\KelasResource;
use App\Filament\Imports\KelasImport;
use Filament\Actions;
use Filament\Resources\Pages\ListRecords;
use Filament\Notifications\Notification;
use Illuminate\Support\Facades\Storage;

class ListKelas extends ListRecords
{
    protected static string $resource = KelasResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Actions\Action::make('download_template')
                ->label('Download Template')
                ->icon('heroicon-o-arrow-down-tray')
                ->color('success')
                ->action(function () {
                    // Generate CSV template dengan format baru
                    $filename = 'template_kelas_' . date('Ymd') . '.csv';
                    $headers = [
                        'Content-Type' => 'text/csv',
                        'Content-Disposition' => "attachment; filename=\"{$filename}\"",
                    ];

                    $callback = function () {
                        $file = fopen('php://output', 'w');

                        // Header CSV
                        fputcsv($file, ['Nama Kelas', 'Wali Kelas', 'Ruangan', 'Kapasitas', 'Jumlah Siswa', 'Status']);

                        // Contoh data (user tinggal edit/hapus)
                        fputcsv($file, ['X TKJ 1', 'Budi Santoso S.Kom', 'Lab Komputer 1', '36', '32', 'Aktif']);
                        fputcsv($file, ['X TKJ 2', 'Siti Nurhaliza S.Pd', 'Lab Komputer 2', '36', '30', 'Aktif']);
                        fputcsv($file, ['X RPL 1', 'Ahmad Dahlan S.Kom', 'Lab Programming 1', '36', '34', 'Aktif']);
                        fputcsv($file, ['X RPL 2', 'Dewi Lestari S.T', 'Lab Programming 2', '36', '33', 'Aktif']);
                        fputcsv($file, ['XI TKJ 1', 'Eko Prasetyo S.Kom', 'Lab Komputer 3', '36', '28', 'Aktif']);
                        fputcsv($file, ['XI TKJ 2', 'Rina Wati S.Pd', 'Lab Komputer 4', '36', '29', 'Aktif']);
                        fputcsv($file, ['XI RPL 1', 'Hendra Gunawan S.Kom', 'Lab Programming 3', '36', '31', 'Aktif']);
                        fputcsv($file, ['XI RPL 2', 'Linda Sari S.T', 'Lab Programming 4', '36', '30', 'Aktif']);
                        fputcsv($file, ['XII TKJ 1', 'Agus Salim S.Kom', 'Lab Komputer 5', '36', '25', 'Aktif']);
                        fputcsv($file, ['XII TKJ 2', 'Fitri Handayani S.Pd', 'Lab Komputer 6', '36', '27', 'Aktif']);
                        fputcsv($file, ['XII RPL 1', 'Rizki Pratama S.Kom', 'Lab Programming 5', '36', '26', 'Aktif']);
                        fputcsv($file, ['XII RPL 2', 'Maya Anggraini S.T', 'Lab Programming 6', '36', '28', 'Aktif']);

                        fclose($file);
                    };

                    return response()->stream($callback, 200, $headers);
                }),

            Actions\Action::make('import')
                ->label('Import Excel')
                ->icon('heroicon-o-arrow-up-tray')
                ->color('warning')
                ->form([
                    \Filament\Forms\Components\FileUpload::make('file')
                        ->label('File Excel/CSV')
                        ->required()
                        ->acceptedFileTypes(['text/csv', 'text/plain'])
                        ->storeFiles(false)
                        ->helperText('Upload file CSV')
                ])
                ->action(function (array $data) {
                    try {
                        $file = $data['file'];

                        // Get the uploaded file's real path
                        $filePath = $file->getRealPath();

                        $result = KelasImport::import($filePath);

                        Notification::make()
                            ->title('Import Berhasil!')
                            ->success()
                            ->body("{$result['imported']} kelas berhasil diimport.")
                            ->send();

                        if (!empty($result['errors'])) {
                            Notification::make()
                                ->title('Beberapa Error Ditemukan')
                                ->warning()
                                ->body(implode("\n", array_slice($result['errors'], 0, 5)))
                                ->send();
                        }
                    } catch (\Exception $e) {
                        Notification::make()
                            ->title('Import Gagal!')
                            ->danger()
                            ->body('Error: ' . $e->getMessage())
                            ->send();
                    }
                }),

            Actions\CreateAction::make(),
        ];
    }
}
