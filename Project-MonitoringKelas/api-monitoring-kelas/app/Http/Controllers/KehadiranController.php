<?php

namespace App\Http\Controllers;

use App\Models\Kehadiran;
use App\Models\Jadwal;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;

class KehadiranController extends Controller
{
    /**
     * Display a listing of kehadiran guru
     * GET /api/kehadiran?jadwal_id=1&tanggal=2025-11-12
     */
    public function index(Request $request)
    {
        $query = Kehadiran::with('jadwal');

        // Filter berdasarkan jadwal_id jika ada
        if ($request->has('jadwal_id') && !empty($request->jadwal_id)) {
            $query->where('jadwal_id', $request->jadwal_id);
        }

        // Filter berdasarkan tanggal jika ada
        if ($request->has('tanggal') && !empty($request->tanggal)) {
            $query->whereDate('tanggal', $request->tanggal);
        }

        // Filter berdasarkan status jika ada
        if ($request->has('status') && !empty($request->status)) {
            $query->where('status', $request->status);
        }

        // Order by tanggal DESC (terbaru dulu)
        $kehadirans = $query->orderBy('tanggal', 'desc')
            ->orderBy('created_at', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'message' => 'Data kehadiran guru berhasil diambil',
            'data' => $kehadirans
        ], 200);
    }

    /**
     * Store a newly created kehadiran guru
     * POST /api/kehadiran
     * Bisa dari laporan siswa (tanpa jadwal_id) atau dari admin (dengan jadwal_id)
     */
    public function store(Request $request)
    {
        // ✅ PERBAIKAN: Tambah validasi kelas
        $validator = Validator::make($request->all(), [
            'jadwal_id' => 'nullable|exists:jadwal,id',
            'tanggal' => 'required|date_format:Y-m-d',
            'jam_masuk' => 'nullable|date_format:H:i',
            'jam_keluar' => 'nullable|date_format:H:i',
            'mata_pelajaran' => 'required|string|max:100',
            'kelas' => 'required|string|max:50', // ✅ TAMBAHKAN
            'nama_guru' => 'required|string|max:100',
            'kode_guru' => 'required|string|max:50',
            'status' => 'required|in:Hadir,Telat,Tidak Hadir,Izin',
            'keterangan' => 'nullable|string|max:500'
        ], [
            'tanggal.required' => 'Tanggal wajib diisi',
            'tanggal.date_format' => 'Format tanggal harus YYYY-MM-DD (contoh: 2025-11-13)',
            'jam_masuk.date_format' => 'Format jam masuk harus HH:mm (contoh: 07:00)',
            'jam_keluar.date_format' => 'Format jam keluar harus HH:mm (contoh: 08:30)',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'kelas.required' => 'Kelas wajib diisi', // ✅ TAMBAHKAN
            'nama_guru.required' => 'Nama guru wajib diisi',
            'kode_guru.required' => 'Kode guru wajib diisi',
            'status.required' => 'Status kehadiran wajib diisi',
            'status.in' => 'Status harus: Hadir, Telat, Tidak Hadir, atau Izin'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $guru = \App\Models\Guru::where('kode_guru', $request->kode_guru)->first();

            $jadwalId = $request->jadwal_id;

            // ✅ PERBAIKAN: Auto-detect jadwal_id jika tidak ada
            if (!$jadwalId) {
                // Konversi hari dari tanggal ke format Indonesia dengan capitalize
                $hariRaw = \Carbon\Carbon::parse($request->tanggal)->locale('id')->dayName;
                $hari = ucfirst(strtolower($hariRaw)); // "senin" → "Senin"

                Log::info("🔍 Auto-detecting jadwal_id", [
                    'tanggal' => $request->tanggal,
                    'hari_detected' => $hari,
                    'kode_guru' => $request->kode_guru,
                    'mata_pelajaran' => $request->mata_pelajaran,
                    'kelas' => $request->kelas
                ]);

                // ✅ PERBAIKAN: Cari dengan LIKE untuk mengatasi perbedaan minor
                $jadwal = \App\Models\Jadwal::where('kode_guru', $request->kode_guru)
                    ->where(function ($query) use ($request) {
                        $query->where('mata_pelajaran', $request->mata_pelajaran)
                            ->orWhere('mata_pelajaran', 'LIKE', '%' . $request->mata_pelajaran . '%');
                    })
                    ->where('hari', $hari)
                    ->where('kelas', $request->kelas)
                    ->first();

                if ($jadwal) {
                    $jadwalId = $jadwal->id;
                    Log::info("✓ Jadwal found", [
                        'jadwal_id' => $jadwalId,
                        'jam' => $jadwal->jam,
                        'mata_pelajaran_matched' => $jadwal->mata_pelajaran
                    ]);
                } else {
                    // Debug: Cek apakah ada jadwal untuk guru ini di hari tersebut
                    $debugJadwal = \App\Models\Jadwal::where('kode_guru', $request->kode_guru)
                        ->where('hari', $hari)
                        ->where('kelas', $request->kelas)
                        ->get(['id', 'mata_pelajaran', 'jam']);

                    Log::warning("✗ Jadwal not found", [
                        'checked_hari' => $hari,
                        'checked_kode_guru' => $request->kode_guru,
                        'checked_mata_pelajaran' => $request->mata_pelajaran,
                        'checked_kelas' => $request->kelas,
                        'available_jadwal_for_this_guru_hari_kelas' => $debugJadwal->toArray()
                    ]);
                }
            }

            // ✅ TAMBAHKAN kelas di create
            $kehadiran = \App\Models\Kehadiran::create([
                'jadwal_id' => $jadwalId,
                'guru_id' => $guru ? $guru->id : null,
                'tanggal' => $request->tanggal,
                'jam_masuk' => $request->jam_masuk,
                'jam_keluar' => $request->jam_keluar,
                'mata_pelajaran' => $request->mata_pelajaran,
                'kelas' => $request->kelas,
                'nama_guru' => $request->nama_guru,
                'kode_guru' => $request->kode_guru,
                'status' => $request->status,
                'keterangan' => $request->keterangan
            ]);

            $kehadiran->load(['jadwal', 'guru']);

            Log::info("✓ Kehadiran created", [
                'id' => $kehadiran->id,
                'jadwal_id' => $kehadiran->jadwal_id,
                'has_jadwal' => $kehadiran->jadwal ? 'YES' : 'NO'
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Kehadiran guru berhasil ditambahkan',
                'data' => $kehadiran
            ], 201);
        } catch (\Exception $e) {
            Log::error('KehadiranController@store error: ' . $e->getMessage());
            Log::error('Request data: ' . json_encode($request->all()));
            Log::error('Stack trace: ' . $e->getTraceAsString());

            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menyimpan data',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Display the specified kehadiran
     * GET /api/kehadiran/{id}
     */
    public function show($id)
    {
        $kehadiran = Kehadiran::with('jadwal')->find($id);

        if (!$kehadiran) {
            return response()->json([
                'success' => false,
                'message' => 'Kehadiran tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Data kehadiran berhasil diambil',
            'data' => $kehadiran
        ], 200);
    }

    /**
     * Update the specified kehadiran
     * PUT/PATCH /api/kehadiran/{id}
     */
    public function update(Request $request, $id)
    {
        $kehadiran = Kehadiran::find($id);

        if (!$kehadiran) {
            return response()->json([
                'success' => false,
                'message' => 'Kehadiran tidak ditemukan'
            ], 404);
        }

        // Validasi input
        $validator = Validator::make($request->all(), [
            'jadwal_id' => 'required|exists:jadwal,id',
            'tanggal' => 'required|date',
            'jam_masuk' => 'nullable|date_format:H:i',
            'jam_keluar' => 'nullable|date_format:H:i',
            'mata_pelajaran' => 'required|string',
            'nama_guru' => 'nullable|string',
            'kode_guru' => 'nullable|string',
            'status' => 'required|in:Hadir,Telat,Tidak Hadir,Izin',
            'keterangan' => 'nullable|string'
        ], [
            'jadwal_id.required' => 'Jadwal ID wajib diisi',
            'jadwal_id.exists' => 'Jadwal tidak ditemukan',
            'tanggal.required' => 'Tanggal wajib diisi',
            'tanggal.date' => 'Format tanggal tidak valid',
            'jam_masuk.date_format' => 'Format jam masuk harus HH:MM',
            'jam_keluar.date_format' => 'Format jam keluar harus HH:MM',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'status.required' => 'Status kehadiran wajib diisi',
            'status.in' => 'Status harus: Hadir, Telat, Tidak Hadir, atau Izin'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Update kehadiran
        $kehadiran->update([
            'jadwal_id' => $request->jadwal_id,
            'tanggal' => $request->tanggal,
            'jam_masuk' => $request->jam_masuk,
            'jam_keluar' => $request->jam_keluar,
            'mata_pelajaran' => $request->mata_pelajaran,
            'nama_guru' => $request->nama_guru,
            'kode_guru' => $request->kode_guru,
            'status' => $request->status,
            'keterangan' => $request->keterangan
        ]);

        // Load relasi jadwal
        $kehadiran->load('jadwal');

        return response()->json([
            'success' => true,
            'message' => 'Kehadiran berhasil diupdate',
            'data' => $kehadiran
        ], 200);
    }

    /**
     * Remove the specified kehadiran
     * DELETE /api/kehadiran/{id}
     */
    public function destroy($id)
    {
        $kehadiran = Kehadiran::find($id);

        if (!$kehadiran) {
            return response()->json([
                'success' => false,
                'message' => 'Kehadiran tidak ditemukan'
            ], 404);
        }

        $kehadiran->delete();

        return response()->json([
            'success' => true,
            'message' => 'Kehadiran berhasil dihapus'
        ], 200);
    }

    /**
     * Get laporan kehadiran guru harian
     * GET /api/kehadiran/laporan/harian?tanggal=2025-11-12
     */
    public function laporanHarian(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tanggal' => 'required|date'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $tanggal = $request->tanggal;

        $kehadirans = Kehadiran::with(['jadwal', 'guru'])
            ->whereDate('tanggal', $tanggal)
            ->orderBy('jam_masuk', 'asc')
            ->get();

        // Statistik
        $totalKehadiran = $kehadirans->count();
        $hadir = $kehadirans->where('status', 'Hadir')->count();
        $telat = $kehadirans->where('status', 'Telat')->count();
        $tidakHadir = $kehadirans->where('status', 'Tidak Hadir')->count();
        $izin = $kehadirans->where('status', 'Izin')->count();

        return response()->json([
            'success' => true,
            'message' => 'Laporan kehadiran harian berhasil diambil',
            'data' => [
                'tanggal' => $tanggal,
                'statistik' => [
                    'total' => $totalKehadiran,
                    'hadir' => $hadir,
                    'telat' => $telat,
                    'tidak_hadir' => $tidakHadir,
                    'izin' => $izin,
                    'persentase_hadir' => $totalKehadiran > 0 ? round(($hadir / $totalKehadiran) * 100, 2) : 0,
                    'persentase_telat' => $totalKehadiran > 0 ? round(($telat / $totalKehadiran) * 100, 2) : 0,
                ],
                'kehadiran' => $kehadirans
            ]
        ], 200);
    }

    /**
     * Get laporan kehadiran guru mingguan
     * GET /api/kehadiran/laporan/mingguan?tanggal_mulai=2025-11-11&tanggal_selesai=2025-11-17
     */
    public function laporanMingguan(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tanggal_mulai' => 'required|date',
            'tanggal_selesai' => 'required|date|after_or_equal:tanggal_mulai'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $tanggalMulai = $request->tanggal_mulai;
        $tanggalSelesai = $request->tanggal_selesai;

        $kehadirans = Kehadiran::with(['jadwal', 'guru'])
            ->whereBetween('tanggal', [$tanggalMulai, $tanggalSelesai])
            ->orderBy('tanggal', 'desc')
            ->orderBy('jam_masuk', 'asc')
            ->get();

        // Statistik
        $totalKehadiran = $kehadirans->count();
        $hadir = $kehadirans->where('status', 'Hadir')->count();
        $telat = $kehadirans->where('status', 'Telat')->count();
        $tidakHadir = $kehadirans->where('status', 'Tidak Hadir')->count();
        $izin = $kehadirans->where('status', 'Izin')->count();

        // Group by tanggal
        $perHari = $kehadirans->groupBy(function ($item) {
            return $item->tanggal->format('Y-m-d');
        })->map(function ($items, $tanggal) {
            return [
                'tanggal' => $tanggal,
                'total' => $items->count(),
                'hadir' => $items->where('status', 'Hadir')->count(),
                'telat' => $items->where('status', 'Telat')->count(),
                'tidak_hadir' => $items->where('status', 'Tidak Hadir')->count(),
                'izin' => $items->where('status', 'Izin')->count(),
            ];
        })->values();

        return response()->json([
            'success' => true,
            'message' => 'Laporan kehadiran mingguan berhasil diambil',
            'data' => [
                'periode' => [
                    'tanggal_mulai' => $tanggalMulai,
                    'tanggal_selesai' => $tanggalSelesai
                ],
                'statistik' => [
                    'total' => $totalKehadiran,
                    'hadir' => $hadir,
                    'telat' => $telat,
                    'tidak_hadir' => $tidakHadir,
                    'izin' => $izin,
                    'persentase_hadir' => $totalKehadiran > 0 ? round(($hadir / $totalKehadiran) * 100, 2) : 0,
                    'persentase_telat' => $totalKehadiran > 0 ? round(($telat / $totalKehadiran) * 100, 2) : 0,
                ],
                'per_hari' => $perHari,
                'kehadiran' => $kehadirans
            ]
        ], 200);
    }

    /**
     * Get laporan kehadiran guru bulanan
     * GET /api/kehadiran/laporan/bulanan?bulan=11&tahun=2025
     */
    public function laporanBulanan(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'bulan' => 'required|integer|min:1|max:12',
            'tahun' => 'required|integer|min:2000'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $bulan = $request->bulan;
        $tahun = $request->tahun;

        $kehadirans = Kehadiran::with(['jadwal', 'guru'])
            ->whereMonth('tanggal', $bulan)
            ->whereYear('tanggal', $tahun)
            ->orderBy('tanggal', 'desc')
            ->orderBy('jam_masuk', 'asc')
            ->get();

        // Statistik
        $totalKehadiran = $kehadirans->count();
        $hadir = $kehadirans->where('status', 'Hadir')->count();
        $telat = $kehadirans->where('status', 'Telat')->count();
        $tidakHadir = $kehadirans->where('status', 'Tidak Hadir')->count();
        $izin = $kehadirans->where('status', 'Izin')->count();

        // Group by guru
        $perGuru = $kehadirans->groupBy('kode_guru')->map(function ($items, $kodeGuru) {
            return [
                'kode_guru' => $kodeGuru,
                'nama_guru' => $items->first()->nama_guru,
                'total' => $items->count(),
                'hadir' => $items->where('status', 'Hadir')->count(),
                'telat' => $items->where('status', 'Telat')->count(),
                'tidak_hadir' => $items->where('status', 'Tidak Hadir')->count(),
                'izin' => $items->where('status', 'Izin')->count(),
                'persentase_kehadiran' => $items->count() > 0 ?
                    round((($items->where('status', 'Hadir')->count() + $items->where('status', 'Telat')->count()) / $items->count()) * 100, 2) : 0
            ];
        })->values();

        return response()->json([
            'success' => true,
            'message' => 'Laporan kehadiran bulanan berhasil diambil',
            'data' => [
                'periode' => [
                    'bulan' => $bulan,
                    'tahun' => $tahun,
                    'nama_bulan' => date('F', mktime(0, 0, 0, $bulan, 1))
                ],
                'statistik' => [
                    'total' => $totalKehadiran,
                    'hadir' => $hadir,
                    'telat' => $telat,
                    'tidak_hadir' => $tidakHadir,
                    'izin' => $izin,
                    'persentase_hadir' => $totalKehadiran > 0 ? round(($hadir / $totalKehadiran) * 100, 2) : 0,
                    'persentase_telat' => $totalKehadiran > 0 ? round(($telat / $totalKehadiran) * 100, 2) : 0,
                ],
                'per_guru' => $perGuru,
                'kehadiran' => $kehadirans
            ]
        ], 200);
    }

    /**
     * Get laporan kehadiran per guru
     * GET /api/kehadiran/laporan/per-guru/{guru_id}?bulan=11&tahun=2025
     */
    public function laporanPerGuru(Request $request, $guruId)
    {
        $validator = Validator::make($request->all(), [
            'bulan' => 'nullable|integer|min:1|max:12',
            'tahun' => 'nullable|integer|min:2000'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $query = Kehadiran::with(['jadwal', 'guru'])
            ->where('guru_id', $guruId);

        if ($request->has('bulan') && $request->has('tahun')) {
            $query->whereMonth('tanggal', $request->bulan)
                ->whereYear('tanggal', $request->tahun);
        }

        $kehadirans = $query->orderBy('tanggal', 'desc')->get();

        if ($kehadirans->isEmpty()) {
            return response()->json([
                'success' => false,
                'message' => 'Data kehadiran guru tidak ditemukan'
            ], 404);
        }

        // Statistik
        $totalKehadiran = $kehadirans->count();
        $hadir = $kehadirans->where('status', 'Hadir')->count();
        $telat = $kehadirans->where('status', 'Telat')->count();
        $tidakHadir = $kehadirans->where('status', 'Tidak Hadir')->count();
        $izin = $kehadirans->where('status', 'Izin')->count();

        return response()->json([
            'success' => true,
            'message' => 'Laporan kehadiran guru berhasil diambil',
            'data' => [
                'guru' => $kehadirans->first()->guru,
                'periode' => [
                    'bulan' => $request->bulan,
                    'tahun' => $request->tahun
                ],
                'statistik' => [
                    'total' => $totalKehadiran,
                    'hadir' => $hadir,
                    'telat' => $telat,
                    'tidak_hadir' => $tidakHadir,
                    'izin' => $izin,
                    'persentase_kehadiran' => $totalKehadiran > 0 ?
                        round((($hadir + $telat) / $totalKehadiran) * 100, 2) : 0
                ],
                'kehadiran' => $kehadirans
            ]
        ], 200);
    }

    // ✅ TAMBAHKAN METHOD BARU di dalam class KehadiranController

    /**
     * Assign guru pengganti untuk kehadiran yang bermasalah
     * PUT /api/kehadiran/{id}/assign-pengganti
     * 
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function assignGuruPengganti(Request $request, $id)
    {
        // Validasi input
        $validator = Validator::make($request->all(), [
            'nama_guru_pengganti' => 'required|string|max:100',
            'kode_guru_pengganti' => 'required|string|max:50',
            'keterangan_pengganti' => 'nullable|string|max:500'
        ], [
            'nama_guru_pengganti.required' => 'Nama guru pengganti wajib diisi',
            'kode_guru_pengganti.required' => 'Kode guru pengganti wajib diisi'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // Cari kehadiran berdasarkan ID
            $kehadiran = Kehadiran::find($id);

            if (!$kehadiran) {
                return response()->json([
                    'success' => false,
                    'message' => 'Data kehadiran tidak ditemukan'
                ], 404);
            }

            // Validasi: Hanya bisa assign pengganti jika status bukan "Hadir"
            if ($kehadiran->status === 'Hadir') {
                return response()->json([
                    'success' => false,
                    'message' => 'Tidak bisa assign guru pengganti untuk guru yang hadir'
                ], 400);
            }

            // Update dengan guru pengganti
            $kehadiran->update([
                'nama_guru_pengganti' => $request->nama_guru_pengganti,
                'kode_guru_pengganti' => $request->kode_guru_pengganti,
                'keterangan_pengganti' => $request->keterangan_pengganti ??
                    "Guru pengganti ditugaskan karena {$kehadiran->nama_guru} {$kehadiran->status}",
                'waktu_assign_pengganti' => now()
            ]);

            // Load relasi
            $kehadiran->load(['jadwal', 'guru', 'guruPengganti']);

            return response()->json([
                'success' => true,
                'message' => "Guru pengganti '{$request->nama_guru_pengganti}' berhasil ditugaskan",
                'data' => $kehadiran
            ], 200);
        } catch (\Exception $e) {
            Log::error('KehadiranController@assignGuruPengganti error: ' . $e->getMessage());

            return response()->json([
                'success' => false,
                'message' => 'Gagal menugaskan guru pengganti',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Hapus guru pengganti (batalkan assignment)
     * DELETE /api/kehadiran/{id}/hapus-pengganti
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function hapusGuruPengganti($id)
    {
        try {
            $kehadiran = Kehadiran::find($id);

            if (!$kehadiran) {
                return response()->json([
                    'success' => false,
                    'message' => 'Data kehadiran tidak ditemukan'
                ], 404);
            }

            // Reset guru pengganti
            $kehadiran->update([
                'nama_guru_pengganti' => null,
                'kode_guru_pengganti' => null,
                'keterangan_pengganti' => null,
                'waktu_assign_pengganti' => null
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Guru pengganti berhasil dibatalkan'
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal membatalkan guru pengganti',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get kelas kosong berdasarkan kehadiran guru
     * GET /api/kehadiran/kelas-kosong?tanggal=2025-11-15&status=Telat
     */
    public function getByKelas($kelas)
    {
        try {
            // Decode URL-encoded kelas (misal: XI%20RPL%201 -> XI RPL 1)
            $kelas = urldecode($kelas);

            // Get laporan guru berdasarkan kelas, sorted by tanggal desc
            $laporanGuru = Kehadiran::with(['jadwal', 'guru'])
                ->where('kelas', $kelas)
                ->orderBy('tanggal', 'desc')
                ->orderBy('jam_masuk', 'desc')
                ->get();

            $jumlah = $laporanGuru->count();

            return response()->json([
                'success' => true,
                'message' => "Ditemukan {$jumlah} laporan untuk kelas {$kelas}",
                'data' => $laporanGuru,
                'meta' => [
                    'total' => $jumlah,
                    'kelas' => $kelas
                ]
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data laporan: ' . $e->getMessage(),
                'data' => []
            ], 500);
        }
    }

    /**
     * Get daftar kelas kosong (guru Telat/Tidak Hadir/Izin)
     * GET /api/kehadiran/kelas-kosong?tanggal=2024-11-20&kelas=XI RPL 1&status=Telat
     */
    public function kelasKosong(Request $request)
    {
        try {
            // ✅ PERBAIKAN: Tambah validasi kelas
            $validator = Validator::make($request->all(), [
                'tanggal' => 'nullable|date_format:Y-m-d',
                'kelas' => 'nullable|string|max:50',
                'status' => 'nullable|string|in:Hadir,Telat,Tidak Hadir,Izin,Guru Pengganti' // ✅ TAMBAH Hadir & Guru Pengganti
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Default tanggal adalah hari ini
            $tanggal = $request->tanggal ?? now()->format('Y-m-d');

            // ✅ PERBAIKAN: Query kehadiran dengan logic yang lebih fleksibel
            $query = Kehadiran::with(['jadwal', 'guru'])
                ->whereDate('tanggal', $tanggal);

            // ✅ TAMBAHKAN: Filter by kelas if provided
            if ($request->has('kelas') && !empty($request->kelas)) {
                $query->where('kelas', $request->kelas);
            }

            // ✅ PERBAIKAN: Filter by specific status if provided
            if ($request->has('status') && !empty($request->status)) {
                // ✅ SPECIAL CASE: Jika status="Hadir", ambil SEMUA data (untuk page Kelas Aktif)
                // Karena page Kelas Aktif butuh semua data untuk filter client-side
                if ($request->status !== 'Hadir') {
                    // Filter status spesifik (Telat, Tidak Hadir, Izin, Guru Pengganti)
                    $query->where('status', $request->status);
                }
                // Jika status="Hadir", tidak ada filter (ambil semua)
            } else {
                // ✅ DEFAULT: Tanpa filter, hanya tampilkan kelas bermasalah (Izin, Tidak Hadir, Telat)
                // Kelas Kosong = guru tidak hadir, telat, atau izin
                $query->whereIn('status', ['Izin', 'Tidak Hadir', 'Telat']);
            }

            // Order by jam_masuk
            $kelasKosong = $query->orderByRaw("
            CASE 
                WHEN jam_masuk IS NULL THEN 1 
                ELSE 0 
            END
        ")
                ->orderBy('jam_masuk', 'asc')
                ->get();

            $jumlahKelasKosong = $kelasKosong->count();

            // ✅ TAMBAHKAN: Info kelas di response
            $filterInfo = [
                'tanggal' => $tanggal,
                'kelas' => $request->kelas ?? 'Semua Kelas',
                'status' => $request->status ?? 'Semua Status'
            ];

            return response()->json([
                'success' => true,
                'message' => $jumlahKelasKosong > 0
                    ? "Ditemukan {$jumlahKelasKosong} kelas kosong pada tanggal {$tanggal}" .
                    ($request->kelas ? " untuk kelas {$request->kelas}" : "")
                    : "Tidak ada kelas kosong pada tanggal {$tanggal}" .
                    ($request->kelas ? " untuk kelas {$request->kelas}" : ""),
                'jumlah_kelas_kosong' => $jumlahKelasKosong,
                'filter' => $filterInfo,
                'data' => $kelasKosong
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data kelas kosong',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
