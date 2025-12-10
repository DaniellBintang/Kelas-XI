<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

// ✅ TAMBAHKAN: Route untuk Filament (agar tidak error)
Route::redirect('/login', '/admin/login')->name('login');
