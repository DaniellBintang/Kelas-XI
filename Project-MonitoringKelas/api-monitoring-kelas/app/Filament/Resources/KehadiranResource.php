<?php

namespace App\Filament\Resources;

use App\Filament\Resources\KehadiranResource\Pages;
use App\Models\Kehadiran;
use App\Models\Guru;
use App\Models\Jadwal;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Components\Grid;
use Filament\Forms\Components\DatePicker;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TimePicker;
use Filament\Forms\Components\Hidden;
use Filament\Forms\Components\Placeholder;
use Filament\Forms\Components\ViewField;
use Filament\Schemas\Schema;
use Filament\Resources\Resource;
use Filament\Actions\EditAction;
use Filament\Actions\DeleteAction;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\BulkActionGroup;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Filters\Filter;
use Filament\Tables\Table;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\HtmlString;

class KehadiranResource extends Resource
{
    protected static string | \BackedEnum | null $navigationIcon = 'heroicon-o-clipboard-document-check';

    protected static ?string $model = Kehadiran::class;

    protected static ?string $navigationLabel = 'Izin Guru';

    protected static ?string $modelLabel = 'Izin Guru';

    protected static ?string $pluralModelLabel = 'Izin Guru';

    protected static ?int $navigationSort = 3;

    public static function form(Schema $schema): Schema
    {
        return $schema
            ->schema([
                Section::make('Informasi Izin Guru')
                    ->description('Masukkan data izin guru')
                    ->schema([
                        // Tanggal Input
                        DatePicker::make('tanggal')
                            ->label('Tanggal Input')
                            ->required()
                            ->default(now())
                            ->displayFormat('d/m/Y')
                            ->columnSpanFull(),

                        Grid::make(2)
                            ->schema([
                                // Guru yang Izin
                                Select::make('kode_guru')
                                    ->label('Guru yang Izin')
                                    ->required()
                                    ->options(function (): array {
                                        // Tampilkan semua guru aktif
                                        return Guru::where('status', 'Aktif')
                                            ->orderBy('nama')
                                            ->pluck('nama', 'kode_guru')
                                            ->toArray();
                                    })
                                    ->searchable()
                                    ->reactive()
                                    ->afterStateUpdated(function ($set, $state) {
                                        if (!$state) {
                                            $set('mata_pelajaran', null);
                                            $set('nama_guru', null);
                                            $set('guru_id', null);
                                            return;
                                        }

                                        // Ambil data guru
                                        $guru = Guru::where('kode_guru', $state)->first();
                                        if ($guru) {
                                            $set('mata_pelajaran', $guru->mata_pelajaran);
                                            $set('nama_guru', $guru->nama);
                                            $set('guru_id', $guru->id);
                                        }
                                    })
                                    ->helperText('Pilih guru yang akan mengajukan izin'),

                                // Mata Pelajaran
                                TextInput::make('mata_pelajaran')
                                    ->label('Mata Pelajaran')
                                    ->required()
                                    ->readOnly()
                                    ->helperText('Otomatis terisi berdasarkan data guru'),
                            ]),

                        Hidden::make('jadwal_id'),
                        Hidden::make('nama_guru'),
                        Hidden::make('guru_id'),
                        Hidden::make('kelas_terpengaruh'), // ✅ BARU: Untuk menyimpan array kelas

                        // Status Hidden - Selalu "Izin"
                        Hidden::make('status')
                            ->default('Izin'),

                        // Keterangan Izin
                        Textarea::make('keterangan')
                            ->label('Alasan / Keterangan Izin')
                            ->placeholder('Contoh: Sakit, Keperluan keluarga, Dinas luar, dll.')
                            ->rows(3)
                            ->maxLength(500)
                            ->required()
                            ->helperText('Jelaskan alasan guru mengajukan izin'),
                    ]),

                // Durasi Izin Section - Selalu Tampil
                Section::make('Durasi Izin')
                    ->description('Tentukan periode izin guru')
                    ->icon('heroicon-o-calendar-days')
                    ->schema([
                        Grid::make(3)
                            ->schema([
                                DatePicker::make('tanggal_mulai_izin')
                                    ->label('Tanggal Mulai Izin')
                                    ->required()
                                    ->displayFormat('d/m/Y')
                                    ->default(now())
                                    ->reactive()
                                    ->afterStateUpdated(function ($set, $state, $get) {
                                        $tanggalSelesai = $get('tanggal_selesai_izin');
                                        if ($state && $tanggalSelesai) {
                                            $mulai = \Carbon\Carbon::parse($state);
                                            $selesai = \Carbon\Carbon::parse($tanggalSelesai);
                                            $durasi = $mulai->diffInDays($selesai) + 1;
                                            $set('durasi_izin_hari', $durasi);
                                        }
                                    }),

                                DatePicker::make('tanggal_selesai_izin')
                                    ->label('Tanggal Selesai Izin')
                                    ->required()
                                    ->displayFormat('d/m/Y')
                                    ->reactive()
                                    ->afterStateUpdated(function ($set, $state, $get) {
                                        $tanggalMulai = $get('tanggal_mulai_izin');
                                        if ($tanggalMulai && $state) {
                                            $mulai = \Carbon\Carbon::parse($tanggalMulai);
                                            $selesai = \Carbon\Carbon::parse($state);
                                            $durasi = $mulai->diffInDays($selesai) + 1;
                                            $set('durasi_izin_hari', $durasi);
                                        }
                                    })
                                    ->helperText('Tanggal guru bertugas kembali'),

                                TextInput::make('durasi_izin_hari')
                                    ->label('Durasi (Hari)')
                                    ->numeric()
                                    ->readOnly()
                                    ->suffix('hari')
                                    ->helperText('Otomatis dihitung'),
                            ]),
                    ])
                    ->collapsed(false),

                // ✅ BARU: Section Kelas Terpengaruh - Preview kelas yang akan kosong
                Section::make('Kelas Terpengaruh')
                    ->description('Daftar kelas yang akan kosong selama guru izin')
                    ->icon('heroicon-o-exclamation-triangle')
                    ->schema([
                        Placeholder::make('preview_kelas_terpengaruh')
                            ->label('')
                            ->content(function ($get) {
                                $kodeGuru = $get('kode_guru');
                                $tanggalMulai = $get('tanggal_mulai_izin');
                                $tanggalSelesai = $get('tanggal_selesai_izin');

                                if (!$kodeGuru || !$tanggalMulai || !$tanggalSelesai) {
                                    return new HtmlString('
                                        <div class="p-4 bg-gray-50 rounded-lg border border-gray-200">
                                            <p class="text-gray-500 text-sm">
                                                📋 Pilih guru dan tentukan periode izin untuk melihat kelas yang terpengaruh.
                                            </p>
                                        </div>
                                    ');
                                }

                                $kelasTerpengaruh = Kehadiran::getKelasTerpengaruh($kodeGuru, $tanggalMulai, $tanggalSelesai);

                                if (empty($kelasTerpengaruh)) {
                                    return new HtmlString('
                                        <div class="p-4 bg-green-50 rounded-lg border border-green-200">
                                            <p class="text-green-700 text-sm font-medium">
                                                ✓ Tidak ada jadwal mengajar di periode ini.
                                            </p>
                                        </div>
                                    ');
                                }

                                // Generate HTML
                                $html = '<div class="space-y-3">';

                                $totalKelas = 0;
                                foreach ($kelasTerpengaruh as $item) {
                                    $totalKelas += $item['jumlah_kelas'];

                                    $html .= '<div class="border rounded-lg p-3 bg-amber-50 border-amber-200">';
                                    $html .= '<div class="flex items-center justify-between mb-2">';
                                    $html .= '<span class="font-semibold text-amber-800">' . $item['hari'] . ', ' . $item['tanggal_formatted'] . '</span>';
                                    $html .= '<span class="text-xs bg-amber-200 text-amber-800 px-2 py-1 rounded-full font-medium">' . $item['jumlah_kelas'] . ' kelas</span>';
                                    $html .= '</div>';
                                    $html .= '<div class="space-y-1">';

                                    foreach ($item['kelas_list'] as $kelas) {
                                        $html .= '<div class="flex items-center text-sm bg-white rounded px-2 py-1">';
                                        $html .= '<span class="w-24 font-mono text-amber-700">' . $kelas['jam'] . '</span>';
                                        $html .= '<span class="flex-1 font-medium text-gray-800">' . $kelas['kelas'] . '</span>';
                                        $html .= '<span class="text-xs text-gray-500">' . $kelas['mata_pelajaran'] . '</span>';
                                        $html .= '</div>';
                                    }

                                    $html .= '</div>';
                                    $html .= '</div>';
                                }

                                $html .= '<div class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">';
                                $html .= '<div class="flex items-center">';
                                $html .= '<svg class="w-5 h-5 text-red-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">';
                                $html .= '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>';
                                $html .= '</svg>';
                                $html .= '<p class="text-red-700 font-semibold">Total: ' . $totalKelas . ' kelas akan kosong selama ' . count($kelasTerpengaruh) . ' hari</p>';
                                $html .= '</div>';
                                $html .= '<p class="text-red-600 text-sm mt-1">Pertimbangkan untuk menunjuk guru pengganti untuk mengatasi kelas kosong.</p>';
                                $html .= '</div>';
                                $html .= '</div>';

                                return new HtmlString($html);
                            }),
                    ])
                    ->visible(fn($get) => $get('kode_guru') && $get('tanggal_mulai_izin') && $get('tanggal_selesai_izin'))
                    ->collapsed(false),

                // Guru Pengganti Section - Selalu Tampil
                Section::make('Guru Pengganti')
                    ->description('Pilih guru pengganti untuk mengatasi kelas kosong selama guru izin')
                    ->icon('heroicon-o-user-plus')
                    ->schema([
                        Grid::make(2)
                            ->schema([
                                Select::make('kode_guru_pengganti')
                                    ->label('Guru Pengganti')
                                    ->options(function ($get): array {
                                        // Ambil semua guru aktif KECUALI guru yang sedang izin
                                        $kodeGuruAsli = $get('kode_guru');
                                        return Guru::where('status', 'Aktif')
                                            ->when($kodeGuruAsli, fn($query) => $query->where('kode_guru', '!=', $kodeGuruAsli))
                                            ->orderBy('nama')
                                            ->pluck('nama', 'kode_guru')
                                            ->toArray();
                                    })
                                    ->searchable()
                                    ->reactive()
                                    ->afterStateUpdated(function ($set, $state) {
                                        if (!$state) {
                                            $set('nama_guru_pengganti', null);
                                            $set('guru_pengganti_id', null);
                                            return;
                                        }

                                        // Auto-fill nama guru pengganti dan ID
                                        $guru = Guru::where('kode_guru', $state)->first();
                                        if ($guru) {
                                            $set('nama_guru_pengganti', $guru->nama);
                                            $set('guru_pengganti_id', $guru->id);
                                        }
                                    })
                                    ->helperText('Opsional - Pilih guru untuk menggantikan selama izin'),

                                TextInput::make('nama_guru_pengganti')
                                    ->label('Nama Guru Pengganti')
                                    ->readOnly()
                                    ->helperText('Otomatis terisi saat memilih guru'),
                            ]),

                        Hidden::make('guru_pengganti_id'),
                    ])
                    ->collapsed(false),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('tanggal')
                    ->label('Tanggal')
                    ->date('d/m/Y')
                    ->sortable()
                    ->searchable(),

                // ✅ UBAH: Tampilkan jumlah kelas terpengaruh, hover untuk detail
                TextColumn::make('kelas_terpengaruh_display')
                    ->label('Kelas Terpengaruh')
                    ->getStateUsing(function ($record) {
                        $kelas = $record->kelas_terpengaruh;
                        if (empty($kelas)) {
                            return '-';
                        }
                        $kelasArray = is_array($kelas) ? $kelas : json_decode($kelas, true);
                        if (empty($kelasArray)) {
                            return '-';
                        }
                        // Tampilkan jumlah kelas saja
                        $count = count($kelasArray);
                        return $count . ' Kelas';
                    })
                    ->tooltip(function ($record) {
                        $kelas = $record->kelas_terpengaruh;
                        if (empty($kelas)) return null;
                        $kelasArray = is_array($kelas) ? $kelas : json_decode($kelas, true);
                        if (empty($kelasArray)) return null;
                        // Format: "Kelas: X RPL 1, XI RPL 2, XII RPL 1"
                        return "Kelas terpengaruh:\n• " . implode("\n• ", $kelasArray);
                    }),

                TextColumn::make('guru.nama')
                    ->label('Nama Guru')
                    ->searchable()
                    ->sortable(),

                TextColumn::make('mata_pelajaran')
                    ->label('Mata Pelajaran')
                    ->searchable()
                    ->wrap()
                    ->toggleable(isToggledHiddenByDefault: true),

                TextColumn::make('status')
                    ->label('Status')
                    ->badge()
                    ->color(fn(string $state): string => match ($state) {
                        'Hadir' => 'success',
                        'Telat' => 'warning',
                        'Tidak Hadir' => 'danger',
                        'Izin' => 'info',
                        default => 'gray',
                    })
                    ->formatStateUsing(fn(string $state): string => match ($state) {
                        'Hadir' => '✓ Hadir',
                        'Telat' => '⏰ Telat',
                        'Tidak Hadir' => '✗ Tidak Hadir',
                        'Izin' => '📋 Izin',
                        default => $state,
                    })
                    ->sortable(),

                TextColumn::make('jam_masuk')
                    ->label('Jam Masuk')
                    ->time('H:i')
                    ->placeholder('-')
                    ->sortable(),

                TextColumn::make('jam_keluar')
                    ->label('Jam Keluar')
                    ->time('H:i')
                    ->placeholder('-')
                    ->sortable(),

                TextColumn::make('keterangan')
                    ->label('Keterangan')
                    ->limit(30)
                    ->placeholder('-')
                    ->tooltip(function (TextColumn $column): ?string {
                        $state = $column->getState();
                        if (strlen($state) <= 30) {
                            return null;
                        }
                        return $state;
                    }),

                TextColumn::make('nama_guru_pengganti')
                    ->label('Guru Pengganti')
                    ->searchable()
                    ->placeholder('-')
                    ->formatStateUsing(function ($state, $record) {
                        if (!$state) {
                            return '-';
                        }
                        $kode = $record->kode_guru_pengganti ?? '';
                        return $state . ($kode ? " ({$kode})" : '');
                    })
                    ->badge()
                    ->color(fn($state) => $state ? 'success' : 'gray')
                    ->sortable(),

                TextColumn::make('tanggal_mulai_izin')
                    ->label('Mulai Izin')
                    ->date('d/m/Y')
                    ->placeholder('-')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),

                TextColumn::make('tanggal_selesai_izin')
                    ->label('Selesai Izin')
                    ->date('d/m/Y')
                    ->placeholder('-')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),

                TextColumn::make('durasi_izin_hari')
                    ->label('Durasi Izin')
                    ->formatStateUsing(fn($state) => $state ? $state . ' hari' : '-')
                    ->placeholder('-')
                    ->sortable()
                    ->badge()
                    ->color(fn($state) => $state ? 'warning' : 'gray')
                    ->toggleable(isToggledHiddenByDefault: false),

                TextColumn::make('created_at')
                    ->label('Dibuat')
                    ->dateTime('d/m/Y H:i')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Status')
                    ->options([
                        'Hadir' => 'Hadir',
                        'Telat' => 'Telat',
                        'Tidak Hadir' => 'Tidak Hadir',
                        'Izin' => 'Izin',
                    ])
                    ->multiple(),

                // ✅ UBAH: Filter kelas terpengaruh (search dalam JSON)
                SelectFilter::make('kelas_filter')
                    ->label('Kelas Terpengaruh')
                    ->options([
                        'X RPL 1' => 'X RPL 1',
                        'X RPL 2' => 'X RPL 2',
                        'X RPL 3' => 'X RPL 3',
                        'XI RPL 1' => 'XI RPL 1',
                        'XI RPL 2' => 'XI RPL 2',
                        'XI RPL 3' => 'XI RPL 3',
                        'XII RPL 1' => 'XII RPL 1',
                        'XII RPL 2' => 'XII RPL 2',
                        'XII RPL 3' => 'XII RPL 3',
                    ])
                    ->multiple()
                    ->query(function (Builder $query, array $data): Builder {
                        if (empty($data['values'])) {
                            return $query;
                        }
                        return $query->where(function ($q) use ($data) {
                            foreach ($data['values'] as $kelas) {
                                $q->orWhereRaw('JSON_CONTAINS(kelas_terpengaruh, ?)', ['"' . $kelas . '"']);
                            }
                        });
                    }),

                Filter::make('tanggal')
                    ->form([
                        DatePicker::make('dari_tanggal')
                            ->label('Dari Tanggal'),
                        DatePicker::make('sampai_tanggal')
                            ->label('Sampai Tanggal'),
                    ])
                    ->query(function (Builder $query, array $data): Builder {
                        return $query
                            ->when(
                                $data['dari_tanggal'],
                                fn(Builder $query, $date): Builder => $query->whereDate('tanggal', '>=', $date),
                            )
                            ->when(
                                $data['sampai_tanggal'],
                                fn(Builder $query, $date): Builder => $query->whereDate('tanggal', '<=', $date),
                            );
                    }),
            ])
            ->actions([
                ViewAction::make(),
                EditAction::make(),
                DeleteAction::make(),
            ])
            ->bulkActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ])
            ->defaultSort('tanggal', 'desc');
    }

    public static function getRelations(): array
    {
        return [
            //
        ];
    }

    public static function getPages(): array
    {
        return [
            'index' => Pages\ListKehadirans::route('/'),
            'create' => Pages\CreateKehadiran::route('/create'),
            'edit' => Pages\EditKehadiran::route('/{record}/edit'),
        ];
    }
}
