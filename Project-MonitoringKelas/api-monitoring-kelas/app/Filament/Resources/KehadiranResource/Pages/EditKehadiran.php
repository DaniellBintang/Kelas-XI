<?php

namespace App\Filament\Resources\KehadiranResource\Pages;

use App\Filament\Resources\KehadiranResource;
use App\Models\Guru;
use App\Models\Kehadiran;
use Filament\Actions;
use Filament\Resources\Pages\EditRecord;
use Filament\Notifications\Notification;

class EditKehadiran extends EditRecord
{
    protected static string $resource = KehadiranResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Actions\DeleteAction::make(),
        ];
    }

    protected function getRedirectUrl(): string
    {
        return $this->getResource()::getUrl('index');
    }

    protected function getSavedNotification(): ?Notification
    {
        return Notification::make()
            ->success()
            ->title('Izin Guru Berhasil Diperbarui')
            ->body('Data izin guru telah berhasil diperbarui.');
    }

    protected function mutateFormDataBeforeSave(array $data): array
    {
        // Status selalu "Izin" untuk form ini
        $data['status'] = 'Izin';

        // Jam masuk dan keluar tidak diperlukan untuk izin
        $data['jam_masuk'] = null;
        $data['jam_keluar'] = null;

        // Ensure tanggal is in correct format
        if (isset($data['tanggal'])) {
            $data['tanggal'] = \Carbon\Carbon::parse($data['tanggal'])->format('Y-m-d');
        }

        // Fill nama_guru and guru_id from kode_guru
        if (isset($data['kode_guru'])) {
            $guru = Guru::where('kode_guru', $data['kode_guru'])->first();
            if ($guru) {
                $data['nama_guru'] = $guru->nama;
                $data['guru_id'] = $guru->id;
            }
        }

        // Fill nama_guru_pengganti and guru_pengganti_id from kode_guru_pengganti
        if (isset($data['kode_guru_pengganti']) && !empty($data['kode_guru_pengganti'])) {
            $guruPengganti = Guru::where('kode_guru', $data['kode_guru_pengganti'])->first();
            if ($guruPengganti) {
                $data['nama_guru_pengganti'] = $guruPengganti->nama;
                $data['guru_pengganti_id'] = $guruPengganti->id;
            }
        } else {
            // Clear guru pengganti jika tidak dipilih
            $data['kode_guru_pengganti'] = null;
            $data['nama_guru_pengganti'] = null;
            $data['guru_pengganti_id'] = null;
        }

        // Auto-calculate durasi izin if tanggal mulai and selesai are set
        if (isset($data['tanggal_mulai_izin']) && isset($data['tanggal_selesai_izin'])) {
            $data['tanggal_mulai_izin'] = \Carbon\Carbon::parse($data['tanggal_mulai_izin'])->format('Y-m-d');
            $data['tanggal_selesai_izin'] = \Carbon\Carbon::parse($data['tanggal_selesai_izin'])->format('Y-m-d');

            $mulai = \Carbon\Carbon::parse($data['tanggal_mulai_izin']);
            $selesai = \Carbon\Carbon::parse($data['tanggal_selesai_izin']);
            $data['durasi_izin_hari'] = $mulai->diffInDays($selesai) + 1;

            // ✅ BARU: Auto-fill kelas_terpengaruh dari jadwal guru
            if (isset($data['kode_guru'])) {
                $kelasTerpengaruh = Kehadiran::generateKelasTerpengaruhArray(
                    $data['kode_guru'],
                    $data['tanggal_mulai_izin'],
                    $data['tanggal_selesai_izin']
                );
                $data['kelas_terpengaruh'] = $kelasTerpengaruh;

                // ✅ Set kelas pertama sebagai kelas utama (untuk backward compatibility)
                if (!empty($kelasTerpengaruh)) {
                    $data['kelas'] = $kelasTerpengaruh[0];
                }
            }
        }

        return $data;
    }
}
