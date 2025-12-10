<?php

namespace App\Filament\Resources\Users\Schemas;

use Filament\Schemas\Components\Section;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Schema;
use Illuminate\Support\Facades\Hash;

class UserForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->schema([
                Section::make('Informasi User')
                    ->description('Data lengkap user untuk sistem monitoring kelas')
                    ->schema([
                        TextInput::make('nama')
                            ->label('Nama Lengkap')
                            ->required()
                            ->maxLength(255)
                            ->placeholder('Contoh: Ahmad Siswa')
                            ->helperText('Nama lengkap user'),

                        TextInput::make('email')
                            ->label('Email')
                            ->email()
                            ->required()
                            ->unique(ignoreRecord: true)
                            ->maxLength(255)
                            ->placeholder('Contoh: ahmad@sekolah.com')
                            ->helperText('Email harus unik'),

                        TextInput::make('password')
                            ->label('Password')
                            ->password()
                            ->required(fn($livewire) => $livewire instanceof \Filament\Resources\Pages\CreateRecord)
                            ->dehydrateStateUsing(fn($state) => filled($state) ? Hash::make($state) : null)
                            ->dehydrated(fn($state) => filled($state))
                            ->minLength(8)
                            ->placeholder('Minimal 8 karakter')
                            ->helperText('Password minimal 8 karakter (kosongkan jika tidak ingin mengubah)'),

                        Select::make('role')
                            ->label('Role')
                            ->required()
                            ->options([
                                'siswa' => 'Siswa',
                                'kurikulum' => 'Kurikulum',
                                'kepala_sekolah' => 'Kepala Sekolah',
                                'admin' => 'Admin',
                            ])
                            ->default('siswa')
                            ->reactive()
                            ->helperText('Pilih role user'),

                        Select::make('kelas')
                            ->label('Kelas')
                            ->options([
                                'X RPL 1' => 'X RPL 1',
                                'X RPL 2' => 'X RPL 2',
                                'XI RPL 1' => 'XI RPL 1',
                                'XI RPL 2' => 'XI RPL 2',
                                'XII RPL 1' => 'XII RPL 1',
                                'XII RPL 2' => 'XII RPL 2',
                                'XII RPL 3' => 'XII RPL 3',
                            ])
                            ->visible(fn($get) => $get('role') === 'siswa')
                            ->required(fn($get) => $get('role') === 'siswa')
                            ->helperText('Kelas hanya untuk role Siswa'),

                        Select::make('status')
                            ->label('Status')
                            ->required()
                            ->options([
                                'aktif' => 'Aktif',
                                'nonaktif' => 'Non-Aktif (Banned)',
                            ])
                            ->default('aktif')
                            ->helperText('Status aktif/non-aktif user'),
                    ])
                    ->columns(2),
            ]);
    }
}
