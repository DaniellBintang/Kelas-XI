<?php
// filepath: database/migrations/2025_11_18_add_guru_pengganti_to_kehadiran_table.php

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
            // Tambah kolom guru pengganti setelah nama_guru
            $table->string('nama_guru_pengganti', 100)->nullable()->after('nama_guru');
            $table->string('kode_guru_pengganti', 50)->nullable()->after('nama_guru_pengganti');

            // Tambah keterangan untuk guru pengganti
            $table->text('keterangan_pengganti')->nullable()->after('keterangan');

            // Tambah timestamp kapan guru pengganti di-assign
            $table->timestamp('waktu_assign_pengganti')->nullable()->after('keterangan_pengganti');

            // Tambah index untuk searching
            $table->index('kode_guru_pengganti');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('kehadiran', function (Blueprint $table) {
            $table->dropIndex(['kode_guru_pengganti']);
            $table->dropColumn([
                'nama_guru_pengganti',
                'kode_guru_pengganti',
                'keterangan_pengganti',
                'waktu_assign_pengganti'
            ]);
        });
    }
};
