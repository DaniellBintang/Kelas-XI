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
        Schema::create('kelas', function (Blueprint $table) {
            $table->id();
            $table->string('nama_kelas', 50); // Contoh: 1, 2, 3
            $table->string('tingkat', 10); // X, XI, XII
            $table->string('jurusan', 50); // RPL, TKJ, MM, dll
            $table->string('wali_kelas', 100)->nullable();
            $table->string('ruangan', 50)->nullable();
            $table->integer('kapasitas')->default(36);
            $table->integer('jumlah_siswa')->default(0);
            $table->text('keterangan')->nullable();
            $table->enum('status', ['Aktif', 'Nonaktif'])->default('Aktif');
            $table->timestamps();

            // Indexes
            $table->index('nama_kelas');
            $table->index('tingkat');
            $table->index('jurusan');
            $table->index('status');

            // Unique constraint: kombinasi tingkat, jurusan, dan nama_kelas harus unik
            $table->unique(['tingkat', 'jurusan', 'nama_kelas']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('kelas');
    }
};
