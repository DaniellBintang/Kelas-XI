<?php

namespace App\Filament\Resources;

use App\Filament\Resources\JadwalResource\Pages;
use App\Models\Jadwal;
use App\Models\Kelas;
use App\Models\Guru;
use Filament\Schemas\Components\Section;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Schema;
use Filament\Resources\Resource;
use Filament\Actions\EditAction;
use Filament\Actions\DeleteAction;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\BulkActionGroup;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class JadwalResource extends Resource
{
    protected static string | \BackedEnum | null $navigationIcon = 'heroicon-o-calendar-days';

    protected static ?string $model = Jadwal::class;

    protected static ?string $navigationLabel = 'Jadwal Pelajaran';

    protected static ?int $navigationSort = 4;

    public static function form(Schema $schema): Schema
    {
        return $schema
            ->schema([
                Section::make('Informasi Jadwal')
                    ->schema([
                        Select::make('hari')
                            ->label('Hari')
                            ->options([
                                'Senin' => 'Senin',
                                'Selasa' => 'Selasa',
                                'Rabu' => 'Rabu',
                                'Kamis' => 'Kamis',
                                'Jumat' => 'Jumat',
                                'Sabtu' => 'Sabtu',
                            ])
                            ->required()
                            ->native(false),

                        Select::make('kelas')
                            ->label('Kelas')
                            ->options(function () {
                                return Kelas::where('status', 'Aktif')
                                    ->get()
                                    ->mapWithKeys(function ($kelas) {
                                        $namaLengkap = "{$kelas->tingkat} {$kelas->jurusan} {$kelas->nama_kelas}";
                                        return [$namaLengkap => $namaLengkap];
                                    });
                            })
                            ->required()
                            ->searchable()
                            ->native(false),

                        TextInput::make('jam')
                            ->label('Jam Pelajaran')
                            ->required()
                            ->placeholder('Contoh: 07:00-08:30'),

                        TextInput::make('mata_pelajaran')
                            ->label('Mata Pelajaran')
                            ->required()
                            ->maxLength(100),
                    ])
                    ->columns(2),

                Section::make('Data Pengajar')
                    ->schema([
                        Select::make('kode_guru')
                            ->label('Guru Pengajar')
                            ->options(function () {
                                return Guru::all()
                                    ->mapWithKeys(function ($guru) {
                                        return [$guru->kode_guru => "{$guru->nama} ({$guru->kode_guru})"];
                                    });
                            })
                            ->required()
                            ->searchable()
                            ->native(false)
                            ->reactive()
                            ->afterStateUpdated(function ($state, callable $set) {
                                $guru = Guru::where('kode_guru', $state)->first();
                                if ($guru) {
                                    $set('nama_guru', $guru->nama);
                                }
                            }),

                        TextInput::make('nama_guru')
                            ->label('Nama Guru')
                            ->required()
                            ->maxLength(255)
                            ->disabled()
                            ->dehydrated(),

                        TextInput::make('ruangan')
                            ->label('Ruangan')
                            ->maxLength(50)
                            ->placeholder('Contoh: R.301, Lab Komputer'),
                    ])
                    ->columns(2),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('hari')
                    ->label('Hari')
                    ->badge()
                    ->color(fn(string $state): string => match ($state) {
                        'Senin' => 'info',
                        'Selasa' => 'success',
                        'Rabu' => 'warning',
                        'Kamis' => 'danger',
                        'Jumat' => 'primary',
                        'Sabtu' => 'gray',
                        default => 'gray',
                    })
                    ->sortable(),

                TextColumn::make('jam')
                    ->label('Jam')
                    ->searchable()
                    ->sortable(),

                TextColumn::make('kelas')
                    ->label('Kelas')
                    ->searchable()
                    ->sortable()
                    ->weight('bold'),

                TextColumn::make('mata_pelajaran')
                    ->label('Mata Pelajaran')
                    ->searchable()
                    ->sortable(),

                TextColumn::make('nama_guru')
                    ->label('Guru Pengajar')
                    ->searchable(['nama_guru', 'kode_guru'])
                    ->sortable(),

                TextColumn::make('ruangan')
                    ->label('Ruangan')
                    ->searchable()
                    ->placeholder('-'),

                TextColumn::make('created_at')
                    ->label('Dibuat')
                    ->dateTime('d M Y H:i')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                SelectFilter::make('hari')
                    ->label('Hari')
                    ->options([
                        'Senin' => 'Senin',
                        'Selasa' => 'Selasa',
                        'Rabu' => 'Rabu',
                        'Kamis' => 'Kamis',
                        'Jumat' => 'Jumat',
                        'Sabtu' => 'Sabtu',
                    ])
                    ->native(false),

                SelectFilter::make('kelas')
                    ->label('Kelas')
                    ->options(function () {
                        return Kelas::where('status', 'Aktif')
                            ->get()
                            ->mapWithKeys(function ($kelas) {
                                $namaLengkap = "{$kelas->tingkat} {$kelas->jurusan} {$kelas->nama_kelas}";
                                return [$namaLengkap => $namaLengkap];
                            });
                    })
                    ->searchable()
                    ->native(false),
            ])
            ->actions([
                EditAction::make(),
                DeleteAction::make(),
            ])
            ->bulkActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ])
            ->defaultSort('hari', 'asc');
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
            'index' => Pages\ListJadwal::route('/'),
            'create' => Pages\CreateJadwal::route('/create'),
            'edit' => Pages\EditJadwal::route('/{record}/edit'),
        ];
    }
}
