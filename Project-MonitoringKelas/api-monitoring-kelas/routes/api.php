<?php

use App\Http\Controllers\JadwalController;
use App\Http\Controllers\KehadiranController;
use App\Http\Controllers\TugasController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\GuruController;
use App\Http\Controllers\KelasTerpengaruhController;
use Illuminate\Support\Facades\Route;

// Auth routes
Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);

// Protected routes
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/user', [AuthController::class, 'user']);
});

// Jadwal routes - SPECIFIC ROUTES FIRST (before {id} routes)
Route::get('/jadwal/kelas-kosong', [JadwalController::class, 'kelasKosong']);
Route::get('/jadwal/statistik/kelas-kosong', [JadwalController::class, 'statistikKelasKosong']);
Route::get('/jadwal/dengan-status-guru', [JadwalController::class, 'jadwalDenganStatusGuru']); // ✅ BARU: Jadwal + Info Guru Izin

// Jadwal CRUD routes - GENERIC ROUTES AFTER
Route::get('/jadwal', [JadwalController::class, 'index']);
Route::post('/jadwal', [JadwalController::class, 'store']);
Route::get('/jadwal/{id}', [JadwalController::class, 'show']);
Route::put('/jadwal/{id}', [JadwalController::class, 'update']);
Route::delete('/jadwal/{id}', [JadwalController::class, 'destroy']);

// ✅ TAMBAHKAN route baru untuk assign guru pengganti
Route::put('/kehadiran/{id}/assign-pengganti', [KehadiranController::class, 'assignGuruPengganti']);
Route::delete('/kehadiran/{id}/hapus-pengganti', [KehadiranController::class, 'hapusGuruPengganti']);


// Kehadiran routes - SPECIFIC ROUTES FIRST
Route::get('/kehadiran/kelas-kosong', [KehadiranController::class, 'kelasKosong']); // ✅ BARU
Route::get('/kehadiran/kelas/{kelas}', [KehadiranController::class, 'getByKelas']); // ✅ BARU untuk siswa
Route::get('/kehadiran/laporan/harian', [KehadiranController::class, 'laporanHarian']);
Route::get('/kehadiran/laporan/mingguan', [KehadiranController::class, 'laporanMingguan']);
Route::get('/kehadiran/laporan/bulanan', [KehadiranController::class, 'laporanBulanan']);
Route::get('/kehadiran/laporan/per-guru/{guru_id}', [KehadiranController::class, 'laporanPerGuru']);

// ✅ BARU: Kelas Terpengaruh routes
Route::get('/kelas-terpengaruh', [KelasTerpengaruhController::class, 'getKelasTerpengaruh']);
Route::get('/kelas-terpengaruh/preview', [KelasTerpengaruhController::class, 'previewKelasTerpengaruh']);

// Kehadiran CRUD routes - GENERIC ROUTES AFTER
Route::get('/kehadiran', [KehadiranController::class, 'index']);
Route::post('/kehadiran', [KehadiranController::class, 'store']);
Route::get('/kehadiran/{id}', [KehadiranController::class, 'show']);
Route::put('/kehadiran/{id}', [KehadiranController::class, 'update']);
Route::delete('/kehadiran/{id}', [KehadiranController::class, 'destroy']);

// Guru routes - SPECIFIC ROUTES FIRST
Route::get('/guru/statistics', [GuruController::class, 'statistics']);

// Guru CRUD routes - GENERIC ROUTES AFTER
Route::get('/guru', [GuruController::class, 'index']);
Route::post('/guru', [GuruController::class, 'store']);
Route::get('/guru/{id}', [GuruController::class, 'show']);
Route::put('/guru/{id}', [GuruController::class, 'update']);
Route::delete('/guru/{id}', [GuruController::class, 'destroy']);

// Tugas routes
Route::get('/tugas', [TugasController::class, 'index']);
Route::post('/tugas', [TugasController::class, 'store']);
Route::get('/tugas/{id}', [TugasController::class, 'show']);
Route::put('/tugas/{id}', [TugasController::class, 'update']);
Route::delete('/tugas/{id}', [TugasController::class, 'destroy']);

// User Management routes - ADMIN ONLY
// Specific routes first
Route::get('/users/statistics', [UserController::class, 'statistics']);
Route::post('/users/bulk-delete', [UserController::class, 'bulkDelete']);
Route::patch('/users/{id}/status', [UserController::class, 'changeStatus']);

// CRUD routes
Route::get('/users', [UserController::class, 'index']);
Route::post('/users', [UserController::class, 'store']);
Route::get('/users/{id}', [UserController::class, 'show']);
Route::put('/users/{id}', [UserController::class, 'update']);
Route::delete('/users/{id}', [UserController::class, 'destroy']);
Route::apiResource('users', UserController::class);
