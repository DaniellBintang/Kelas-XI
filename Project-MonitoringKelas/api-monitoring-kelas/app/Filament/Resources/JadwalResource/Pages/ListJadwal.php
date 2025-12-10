<?php

namespace App\Filament\Resources\JadwalResource\Pages;

use App\Filament\Resources\JadwalResource;
use App\Filament\Imports\JadwalImport;
use Filament\Actions;
use Filament\Resources\Pages\ListRecords;
use Filament\Notifications\Notification;
use Illuminate\Support\Facades\Storage;

class ListJadwal extends ListRecords
{
    protected static string $resource = JadwalResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Actions\Action::make('download_template')
                ->label('Download Template')
                ->icon('heroicon-o-arrow-down-tray')
                ->color('success')
                ->action(function () {
                    return response()->download(
                        storage_path('app/public/templates/template_jadwal.csv'),
                        'template_jadwal.csv'
                    );
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

                        $result = JadwalImport::import($filePath);

                        Notification::make()
                            ->title('Import Berhasil!')
                            ->success()
                            ->body("{$result['imported']} jadwal berhasil diimport.")
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
