<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     * 
     * Mengubah kolom 'kelas' dari string ke TEXT untuk menyimpan
     * daftar kelas terpengaruh dalam format JSON
     */
    public function up(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            // Ubah kolom kelas menjadi TEXT untuk menyimpan JSON kelas terpengaruh
            $table->text('kelas_terpengaruh')->nullable()->after('kelas');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            $table->dropColumn('kelas_terpengaruh');
        });
    }
};
