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
        Schema::create('guru', function (Blueprint $table) {
            $table->id();
            $table->string('kode_guru', 50)->unique();
            $table->string('nama', 100);
            $table->string('mata_pelajaran', 100);
            $table->string('email', 100)->nullable();
            $table->string('no_telepon', 20)->nullable();
            $table->text('alamat')->nullable();
            $table->enum('status', ['Aktif', 'Cuti', 'Nonaktif'])->default('Aktif');
            $table->timestamps();

            // Index untuk pencarian cepat
            $table->index('kode_guru');
            $table->index('nama');
            $table->index('status');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('guru');
    }
};
