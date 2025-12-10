<?php

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
            $table->date('tanggal_mulai_izin')->nullable()->after('keterangan');
            $table->date('tanggal_selesai_izin')->nullable()->after('tanggal_mulai_izin');
            $table->integer('durasi_izin_hari')->nullable()->after('tanggal_selesai_izin')->comment('Durasi izin dalam hari');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            $table->dropColumn(['tanggal_mulai_izin', 'tanggal_selesai_izin', 'durasi_izin_hari']);
        });
    }
};
