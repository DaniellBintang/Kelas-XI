<?php

namespace App\Filament\Resources;

use App\Filament\Resources\GuruResource\Pages;
use App\Models\Guru;
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

class GuruResource extends Resource
{
    protected static string | \BackedEnum | null $navigationIcon = 'heroicon-o-user';

    protected static ?string $model = Guru::class;

    protected static ?string $navigationLabel = 'Data Guru';

    protected static ?string $modelLabel = 'Guru';

    protected static ?string $pluralModelLabel = 'Guru';

    protected static ?int $navigationSort = 2;

    public static function form(Schema $schema): Schema
    {
        return $schema
            ->schema([
                Section::make('Informasi Guru')
                    ->description('Data lengkap guru untuk sistem monitoring kelas')
                    ->schema([
                        TextInput::make('kode_guru')
                            ->label('Kode Guru')
                            ->required()
                            ->unique(ignoreRecord: true)
                            ->maxLength(10)
                            ->placeholder('Contoh: G001')
                            ->helperText('Kode unik guru'),

                        TextInput::make('nama')
                            ->label('Nama Lengkap')
                            ->required()
                            ->maxLength(255)
                            ->placeholder('Contoh: Dr. Ahmad Wijaya, S.Pd., M.Pd.'),

                        TextInput::make('mata_pelajaran')
                            ->label('Mata Pelajaran')
                            ->required()
                            ->maxLength(100)
                            ->placeholder('Contoh: Matematika'),

                        TextInput::make('email')
                            ->label('Email')
                            ->email()
                            ->required()
                            ->unique(ignoreRecord: true)
                            ->maxLength(255)
                            ->placeholder('Contoh: ahmad.wijaya@school.com'),

                        TextInput::make('no_telepon')
                            ->label('No. Telepon')
                            ->tel()
                            ->maxLength(15)
                            ->placeholder('Contoh: 081234567890'),

                        Textarea::make('alamat')
                            ->label('Alamat')
                            ->maxLength(500)
                            ->rows(3)
                            ->placeholder('Alamat lengkap guru'),

                        Select::make('status')
                            ->label('Status')
                            ->required()
                            ->options([
                                'Aktif' => 'Aktif',
                                'Cuti' => 'Cuti',
                                'Resign' => 'Resign',
                            ])
                            ->default('Aktif'),
                    ])
                    ->columns(2),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('kode_guru')
                    ->label('Kode Guru')
                    ->sortable()
                    ->searchable()
                    ->weight('bold'),

                TextColumn::make('nama')
                    ->label('Nama Lengkap')
                    ->sortable()
                    ->searchable(),

                TextColumn::make('mata_pelajaran')
                    ->label('Mata Pelajaran')
                    ->sortable()
                    ->searchable()
                    ->badge()
                    ->color('info'),

                TextColumn::make('email')
                    ->label('Email')
                    ->sortable()
                    ->searchable()
                    ->copyable()
                    ->icon('heroicon-o-envelope'),

                TextColumn::make('no_telepon')
                    ->label('No. Telepon')
                    ->searchable(),

                TextColumn::make('status')
                    ->label('Status')
                    ->colors([
                        'success' => 'Aktif',
                        'warning' => 'Cuti',
                        'danger' => 'Resign',
                    ]),

                TextColumn::make('created_at')
                    ->label('Dibuat Pada')
                    ->dateTime('d M Y H:i')
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Filter Status')
                    ->options([
                        'Aktif' => 'Aktif',
                        'Cuti' => 'Cuti',
                        'Resign' => 'Resign',
                    ]),

                SelectFilter::make('mata_pelajaran')
                    ->label('Filter Mata Pelajaran')
                    ->options(function () {
                        return Guru::query()
                            ->distinct()
                            ->pluck('mata_pelajaran', 'mata_pelajaran')
                            ->toArray();
                    }),
            ])
            ->actions([
                EditAction::make()
                    ->label('Edit')
                    ->icon('heroicon-o-pencil'),
                DeleteAction::make()
                    ->label('Hapus')
                    ->icon('heroicon-o-trash')
                    ->requiresConfirmation(),
            ])
            ->bulkActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make()
                        ->label('Hapus Terpilih')
                        ->requiresConfirmation(),
                ]),
            ])
            ->defaultSort('created_at', 'desc')
            ->striped()
            ->paginated([10, 25, 50, 100]);
    }

    public static function getRelations(): array
    {
        return [];
    }

    public static function getPages(): array
    {
        return [
            'index' => Pages\ListGurus::route('/'),
            'create' => Pages\CreateGuru::route('/create'),
            'edit' => Pages\EditGuru::route('/{record}/edit'),
        ];
    }
}
