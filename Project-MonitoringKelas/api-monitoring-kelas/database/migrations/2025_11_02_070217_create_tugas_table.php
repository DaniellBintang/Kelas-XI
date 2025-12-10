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
        Schema::create('tugas', function (Blueprint $table) {
            $table->id(); // bigInteger, primary key, auto increment
            $table->unsignedBigInteger('user_id');
            $table->date('tanggal');
            $table->string('mata_pelajaran', 100);
            $table->string('judul_tugas', 255);
            $table->enum('status', ['Selesai', 'Belum Selesai', 'Terlambat'])->default('Belum Selesai');
            $table->timestamps(); // created_at, updated_at

            // Foreign key constraint
            $table->foreign('user_id')
                ->references('id')
                ->on('users')
                ->onDelete('cascade');

            // Index untuk filtering cepat
            $table->index('user_id');
            $table->index('tanggal');
            $table->index(['user_id', 'tanggal']); // Composite index untuk query kombinasi
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tugas');
    }
};
