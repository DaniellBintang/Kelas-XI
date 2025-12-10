<?php
// filepath: database/migrations/2025_11_15_add_kelas_to_kehadiran_table.php

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
        Schema::table('kehadiran', function (Blueprint $table) {
            // Tambah kolom kelas setelah mata_pelajaran
            $table->string('kelas', 50)->nullable()->after('mata_pelajaran');

            // Tambah index untuk filtering
            $table->index('kelas');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            $table->dropIndex(['kelas']);
            $table->dropColumn('kelas');
        });
    }
};
