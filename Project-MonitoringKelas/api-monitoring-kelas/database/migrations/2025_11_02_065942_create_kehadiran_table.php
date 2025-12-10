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
        Schema::create('kehadiran', function (Blueprint $table) {
            $table->id();

            // ✅ jadwal_id nullable (siswa tidak tahu jadwal_id)
            $table->unsignedBigInteger('jadwal_id')->nullable();

            // ✅ guru_id untuk relasi ke tabel guru
            $table->unsignedBigInteger('guru_id')->nullable();

            // Data kehadiran
            $table->date('tanggal');
            $table->time('jam_masuk')->nullable();
            $table->time('jam_keluar')->nullable();
            $table->string('mata_pelajaran', 100);

            // ✅ PERBAIKAN: nama_guru NULLABLE (untuk handle kelas kosong)
            $table->string('nama_guru', 100)->nullable();

            $table->string('kode_guru', 50)->nullable(); // ✅ Juga nullable

            $table->enum('status', ['Hadir', 'Telat', 'Tidak Hadir', 'Izin'])->default('Hadir');

            $table->text('keterangan')->nullable();
            $table->timestamps();

            // Foreign key constraint untuk jadwal_id (nullable)
            $table->foreign('jadwal_id')
                ->references('id')
                ->on('jadwal')
                ->onDelete('set null');

            // Foreign key constraint untuk guru_id (nullable)
            $table->foreign('guru_id')
                ->references('id')
                ->on('guru')
                ->onDelete('set null');

            // Index untuk filtering cepat
            $table->index('jadwal_id');
            $table->index('guru_id');
            $table->index('tanggal');
            $table->index('kode_guru');
            $table->index(['tanggal', 'kode_guru']); // Composite index
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('kehadiran');
    }
};
