<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use Illuminate\Support\Facades\DB;

echo "=== Checking Kelas Data ===\n\n";

// Check Kelas dengan wali kelas
$kelas = DB::table('kelas')->select('tingkat', 'jurusan', 'nama_kelas', 'wali_kelas', 'ruangan')->get();
foreach ($kelas as $k) {
    $namaKelas = "{$k->tingkat} {$k->jurusan} {$k->nama_kelas}";
    $waliKelas = $k->wali_kelas ?? 'Belum ditentukan';
    echo "  {$namaKelas}\n";
    echo "    - Wali Kelas: {$waliKelas}\n";
    echo "    - Ruangan: {$k->ruangan}\n\n";
}

// Check guru list
echo "\n=== Checking Guru List ===\n";
$guruList = DB::table('guru')->select('kode_guru', 'nama')->orderBy('kode_guru')->get();
foreach ($guruList as $g) {
    echo "  {$g->kode_guru}: {$g->nama}\n";
}

echo "\nDone!\n";
