<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            // ✅ Ubah jadwal_id menjadi nullable
            $table->unsignedBigInteger('jadwal_id')->nullable()->change();
        });
    }

    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            // Rollback: Jadwal_id kembali NOT NULL
            $table->unsignedBigInteger('jadwal_id')->nullable(false)->change();
        });
    }
};
