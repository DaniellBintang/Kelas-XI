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
            // Add only guru_pengganti_id since kode_guru_pengganti and nama_guru_pengganti already exist
            $table->unsignedBigInteger('guru_pengganti_id')->nullable()->after('nama_guru_pengganti');

            // Add foreign key constraint
            $table->foreign('guru_pengganti_id')->references('id')->on('guru')->onDelete('set null');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            $table->dropForeign(['guru_pengganti_id']);
            $table->dropColumn(['guru_pengganti_id']);
        });
    }
};
