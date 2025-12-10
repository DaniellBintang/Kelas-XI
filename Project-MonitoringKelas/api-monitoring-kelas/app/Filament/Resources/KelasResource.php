<?php

namespace App\Filament\Resources;

use App\Filament\Resources\KelasResource\Pages;
use App\Models\Kelas;
use Filament\Schemas\Components\Section;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Schemas\Schema;
use Filament\Resources\Resource;
use Filament\Actions\EditAction;
use Filament\Actions\DeleteAction;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\BulkActionGroup;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class KelasResource extends Resource
{
    protected static string | \BackedEnum | null $navigationIcon = 'heroicon-o-academic-cap';

    protected static ?string $model = Kelas::class;

    protected static ?string $navigationLabel = 'Kelas';

    protected static ?int $navigationSort = 3;

    public static function form(Schema $schema): Schema
    {
        return $schema
            ->schema([
                Section::make('Informasi Kelas')
                    ->schema([
                        TextInput::make('nama_kelas')
                            ->label('Nama Kelas')
                            ->required()
                            ->maxLength(50)
                            ->placeholder('Contoh: 1, 2, A, B'),

                        Select::make('tingkat')
                            ->label('Tingkat')
                            ->options([
                                'X' => 'Kelas X',
                                'XI' => 'Kelas XI',
                                'XII' => 'Kelas XII',
                            ])
                            ->required()
                            ->native(false),

                        TextInput::make('jurusan')
                            ->label('Jurusan')
                            ->required()
                            ->maxLength(50)
                            ->placeholder('Contoh: IPA, IPS, TKJ, RPL'),

                        Select::make('wali_kelas')
                            ->label('Wali Kelas')
                            ->options(
                                \App\Models\Guru::query()
                                    ->orderBy('nama')
                                    ->pluck('nama', 'nama')
                            )
                            ->searchable()
                            ->placeholder('Pilih wali kelas')
                            ->native(false),
                    ])
                    ->columns(2),

                Section::make('Detail Kelas')
                    ->schema([
                        TextInput::make('ruangan')
                            ->label('Ruangan')
                            ->maxLength(50)
                            ->placeholder('Contoh: R.301, Lab Komputer 1'),

                        TextInput::make('kapasitas')
                            ->label('Kapasitas')
                            ->numeric()
                            ->default(36)
                            ->minValue(1)
                            ->maxValue(50)
                            ->suffix('siswa'),

                        TextInput::make('jumlah_siswa')
                            ->label('Jumlah Siswa')
                            ->numeric()
                            ->default(0)
                            ->minValue(0)
                            ->suffix('siswa')
                            ->helperText('Jumlah siswa saat ini di kelas'),

                        Select::make('status')
                            ->label('Status')
                            ->options([
                                'Aktif' => 'Aktif',
                                'Tidak Aktif' => 'Tidak Aktif',
                            ])
                            ->default('Aktif')
                            ->required()
                            ->native(false),
                    ])
                    ->columns(2),

                Section::make('Keterangan')
                    ->schema([
                        Textarea::make('keterangan')
                            ->label('Keterangan')
                            ->rows(3)
                            ->maxLength(500)
                            ->placeholder('Catatan tambahan tentang kelas'),
                    ]),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('nama_lengkap')
                    ->label('Nama Kelas')
                    ->searchable(['nama_kelas', 'tingkat', 'jurusan'])
                    ->sortable(['tingkat', 'jurusan', 'nama_kelas'])
                    ->weight('bold')
                    ->formatStateUsing(
                        fn(Kelas $record) =>
                        "{$record->tingkat} {$record->jurusan} {$record->nama_kelas}"
                    ),

                TextColumn::make('wali_kelas')
                    ->label('Wali Kelas')
                    ->searchable()
                    ->sortable()
                    ->placeholder('Belum ditentukan'),

                TextColumn::make('ruangan')
                    ->label('Ruangan')
                    ->searchable()
                    ->placeholder('-'),

                TextColumn::make('kapasitas')
                    ->label('Kapasitas')
                    ->alignCenter()
                    ->sortable()
                    ->suffix(' siswa'),

                TextColumn::make('jumlah_siswa')
                    ->label('Jumlah Siswa')
                    ->alignCenter()
                    ->sortable()
                    ->badge()
                    ->color(
                        fn($state, Kelas $record) =>
                        $state > $record->kapasitas ? 'danger' : 'success'
                    ),

                TextColumn::make('status')
                    ->label('Status')
                    ->badge()
                    ->color(fn(string $state): string => match ($state) {
                        'Aktif' => 'success',
                        'Tidak Aktif' => 'danger',
                    })
                    ->sortable(),

                TextColumn::make('created_at')
                    ->label('Dibuat')
                    ->dateTime('d M Y H:i')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                SelectFilter::make('tingkat')
                    ->label('Tingkat')
                    ->options([
                        'X' => 'Kelas X',
                        'XI' => 'Kelas XI',
                        'XII' => 'Kelas XII',
                    ])
                    ->native(false),

                SelectFilter::make('status')
                    ->label('Status')
                    ->options([
                        'Aktif' => 'Aktif',
                        'Tidak Aktif' => 'Tidak Aktif',
                    ])
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
            ->defaultSort('tingkat', 'asc');
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
            'index' => Pages\ListKelas::route('/'),
            'create' => Pages\CreateKelas::route('/create'),
            'edit' => Pages\EditKelas::route('/{record}/edit'),
        ];
    }
}
