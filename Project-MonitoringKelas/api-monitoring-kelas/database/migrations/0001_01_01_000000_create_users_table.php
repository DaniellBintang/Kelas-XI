<?php
// filepath: e:\api-monitoring-kelas\database\migrations\0001_01_01_000000_create_users_table.php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('users', function (Blueprint $table) {
            $table->id(); // bigInteger, primary key, auto increment
            $table->string('nama', 255)->nullable(false);
            $table->string('email', 255)->unique();
            $table->string('password', 255);
            $table->enum('role', ['siswa', 'kurikulum', 'kepala_sekolah', 'admin'])
                ->default('siswa');
            $table->string('kelas', 50)->nullable(); // Add kelas field (nullable, khusus untuk siswa)
            $table->enum('status', ['aktif', 'nonaktif'])
                ->default('aktif'); // Add status field
            $table->timestamps(); // created_at, updated_at
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('users');
    }
};
