<?php
// filepath: e:\api-monitoring-kelas\app\Http\Controllers\JadwalController.php

namespace App\Http\Controllers;

use App\Models\Jadwal;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class JadwalController extends Controller
{
    /**
     * Display a listing of jadwal
     * GET /api/jadwal
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            $query = Jadwal::query();

            // Filter berdasarkan hari jika ada
            if ($request->has('hari') && !empty($request->hari)) {
                $query->where('hari', $request->hari);
            }

            // Filter berdasarkan kelas jika ada
            if ($request->has('kelas') && !empty($request->kelas)) {
                $query->where('kelas', $request->kelas);
            }

            // Filter berdasarkan guru jika ada
            if ($request->has('guru') && !empty($request->guru)) {
                $query->where('nama_guru', 'like', '%' . $request->guru . '%');
            }

            // Filter berdasarkan mata_pelajaran jika ada
            if ($request->has('mata_pelajaran') && !empty($request->mata_pelajaran)) {
                $query->where('mata_pelajaran', 'like', '%' . $request->mata_pelajaran . '%');
            }

            // Order by hari dan jam
            $query->orderByRaw("FIELD(hari, 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu')")
                ->orderBy('jam', 'asc');

            $jadwal = $query->get();

            return response()->json([
                'success' => true,
                'message' => 'Data jadwal berhasil diambil',
                'data' => $jadwal
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data jadwal',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Store a newly created jadwal
     * POST /api/jadwal
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        // Validasi input
        $validator = Validator::make($request->all(), [
            'hari' => 'required|in:Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu',
            'kelas' => 'required|string',
            'jam' => 'required|string',
            'mata_pelajaran' => 'required|string',
            'kode_guru' => 'nullable|string',
            'nama_guru' => 'nullable|string',
            'ruangan' => 'required|string'
        ], [
            'hari.required' => 'Hari wajib diisi',
            'hari.in' => 'Hari tidak valid',
            'kelas.required' => 'Kelas wajib diisi',
            'jam.required' => 'Jam wajib diisi',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'ruangan.required' => 'Ruangan wajib diisi'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // Simpan jadwal
            $jadwal = Jadwal::create([
                'hari' => $request->hari,
                'kelas' => $request->kelas,
                'jam' => $request->jam,
                'mata_pelajaran' => $request->mata_pelajaran,
                'kode_guru' => $request->kode_guru,
                'nama_guru' => $request->nama_guru,
                'ruangan' => $request->ruangan
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Jadwal berhasil ditambahkan',
                'data' => $jadwal
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal menambahkan jadwal',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Display the specified jadwal
     * GET /api/jadwal/{id}
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $jadwal = Jadwal::find($id);

        if (!$jadwal) {
            return response()->json([
                'success' => false,
                'message' => 'Jadwal tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Data jadwal berhasil diambil',
            'data' => $jadwal
        ], 200);
    }

    /**
     * Update the specified jadwal
     * PUT/PATCH /api/jadwal/{id}
     * 
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        $jadwal = Jadwal::find($id);

        if (!$jadwal) {
            return response()->json([
                'success' => false,
                'message' => 'Jadwal tidak ditemukan'
            ], 404);
        }

        // Validasi input
        $validator = Validator::make($request->all(), [
            'hari' => 'required|in:Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu',
            'kelas' => 'required|string',
            'jam' => 'required|string',
            'mata_pelajaran' => 'required|string',
            'kode_guru' => 'nullable|string',
            'nama_guru' => 'nullable|string',
            'ruangan' => 'required|string'
        ], [
            'hari.required' => 'Hari wajib diisi',
            'hari.in' => 'Hari tidak valid',
            'kelas.required' => 'Kelas wajib diisi',
            'jam.required' => 'Jam wajib diisi',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'ruangan.required' => 'Ruangan wajib diisi'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // Update jadwal
            $jadwal->update([
                'hari' => $request->hari,
                'kelas' => $request->kelas,
                'jam' => $request->jam,
                'mata_pelajaran' => $request->mata_pelajaran,
                'kode_guru' => $request->kode_guru,
                'nama_guru' => $request->nama_guru,
                'ruangan' => $request->ruangan
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Jadwal berhasil diupdate',
                'data' => $jadwal
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengupdate jadwal',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Remove the specified jadwal
     * DELETE /api/jadwal/{id}
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        $jadwal = Jadwal::find($id);

        if (!$jadwal) {
            return response()->json([
                'success' => false,
                'message' => 'Jadwal tidak ditemukan'
            ], 404);
        }

        try {
            $jadwal->delete();

            return response()->json([
                'success' => true,
                'message' => 'Jadwal berhasil dihapus'
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal menghapus jadwal',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get kelas kosong (jadwal dengan guru kosong/NULL)
     * Endpoint khusus untuk Kepala Sekolah monitoring
     * GET /api/jadwal/kelas-kosong
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function kelasKosong(Request $request)
    {
        try {
            // Validasi query parameters (optional)
            $validator = Validator::make($request->all(), [
                'hari' => 'nullable|string|in:Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu',
                'tanggal' => 'nullable|date_format:Y-m-d',
                'kelas' => 'nullable|string'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Query builder untuk jadwal kosong
            $query = Jadwal::query()
                ->where(function ($q) {
                    $q->whereNull('nama_guru')
                        ->orWhere('nama_guru', '')
                        ->orWhere('nama_guru', '-');
                });

            // Filter berdasarkan hari jika ada parameter
            if ($request->has('hari') && $request->hari != null) {
                $query->where('hari', $request->hari);
            }

            // Filter berdasarkan tanggal jika ada parameter
            if ($request->has('tanggal') && $request->tanggal != null) {
                $query->whereDate('created_at', $request->tanggal);
            }

            // Filter berdasarkan kelas jika ada parameter
            if ($request->has('kelas') && $request->kelas != null) {
                $query->where('kelas', 'like', '%' . $request->kelas . '%');
            }

            // Order by jam (paling pagi duluan)
            $query->orderBy('jam', 'asc');

            // Get data
            $kelasKosong = $query->get();

            // Hitung jumlah kelas kosong
            $jumlahKelasKosong = $kelasKosong->count();

            // Grouping berdasarkan hari untuk summary
            $summary = $kelasKosong->groupBy('hari')->map(function ($items, $hari) {
                return [
                    'hari' => $hari,
                    'jumlah' => $items->count(),
                    'jam_kosong' => $items->pluck('jam')->toArray()
                ];
            })->values();

            // Grouping berdasarkan kelas untuk detail
            $detailPerKelas = $kelasKosong->groupBy('kelas')->map(function ($items, $kelas) {
                return [
                    'kelas' => $kelas,
                    'jumlah_jam_kosong' => $items->count(),
                    'jadwal_kosong' => $items->map(function ($item) {
                        return [
                            'id' => $item->id,
                            'hari' => $item->hari,
                            'jam' => $item->jam,
                            'mata_pelajaran' => $item->mata_pelajaran,
                            'kode_guru' => $item->kode_guru ?? '-',
                            'ruangan' => $item->ruangan
                        ];
                    })
                ];
            })->values();

            // Response dengan informasi lengkap
            return response()->json([
                'success' => true,
                'message' => $jumlahKelasKosong > 0
                    ? "Ditemukan {$jumlahKelasKosong} kelas kosong"
                    : "Tidak ada kelas kosong",
                'jumlah_kelas_kosong' => $jumlahKelasKosong,
                'filter' => [
                    'hari' => $request->hari ?? 'Semua',
                    'tanggal' => $request->tanggal ?? 'Semua',
                    'kelas' => $request->kelas ?? 'Semua'
                ],
                'summary_per_hari' => $summary,
                'detail_per_kelas' => $detailPerKelas,
                'data' => $kelasKosong->map(function ($item) {
                    return [
                        'id' => $item->id,
                        'hari' => $item->hari,
                        'jam' => $item->jam,
                        'mata_pelajaran' => $item->mata_pelajaran,
                        'kode_guru' => $item->kode_guru ?? '-',
                        'nama_guru' => $item->nama_guru ?? 'Tidak ada guru',
                        'ruangan' => $item->ruangan,
                        'kelas' => $item->kelas,
                        'status' => 'Kosong',
                        'prioritas' => $this->calculatePriority($item->jam),
                        'created_at' => $item->created_at,
                        'updated_at' => $item->updated_at
                    ];
                })
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data kelas kosong',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Calculate priority level based on jam (waktu)
     * Pagi = High priority, Siang/Sore = Medium/Low priority
     * 
     * @param string $jam
     * @return string
     */
    private function calculatePriority($jam)
    {
        // Extract jam awal dari format "07:00-08:30"
        $jamAwal = explode('-', $jam)[0];
        $hour = (int) explode(':', $jamAwal)[0];

        if ($hour >= 7 && $hour < 10) {
            return 'High'; // Jam pagi (07:00-09:59)
        } elseif ($hour >= 10 && $hour < 13) {
            return 'Medium'; // Jam siang (10:00-12:59)
        } else {
            return 'Low'; // Jam sore (13:00+)
        }
    }

    /**
     * Get statistics kelas kosong (summary untuk dashboard Kepala Sekolah)
     * GET /api/jadwal/statistik/kelas-kosong
     * 
     * @return \Illuminate\Http\JsonResponse
     */
    public function statistikKelasKosong()
    {
        try {
            // Total kelas kosong
            $totalKelasKosong = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })->count();

            // Kelas kosong hari ini (berdasarkan hari dalam seminggu)
            $hariIni = now()->locale('id')->dayName;
            $kelasKosongHariIni = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })->where('hari', $hariIni)->count();

            // Kelas kosong per hari
            $kelasKosongPerHari = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })
                ->selectRaw('hari, COUNT(*) as jumlah')
                ->groupBy('hari')
                ->orderByRaw("FIELD(hari, 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu')")
                ->get();

            // Kelas paling sering kosong
            $kelasPalingSering = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })
                ->selectRaw('kelas, COUNT(*) as jumlah')
                ->groupBy('kelas')
                ->orderBy('jumlah', 'desc')
                ->limit(5)
                ->get();

            // Jam yang paling sering kosong
            $jamPalingSering = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })
                ->selectRaw('jam, COUNT(*) as jumlah')
                ->groupBy('jam')
                ->orderBy('jumlah', 'desc')
                ->limit(5)
                ->get();

            // Mata pelajaran yang paling sering kosong
            $mapelPalingSering = Jadwal::where(function ($q) {
                $q->whereNull('nama_guru')
                    ->orWhere('nama_guru', '')
                    ->orWhere('nama_guru', '-');
            })
                ->selectRaw('mata_pelajaran, COUNT(*) as jumlah')
                ->groupBy('mata_pelajaran')
                ->orderBy('jumlah', 'desc')
                ->limit(5)
                ->get();

            return response()->json([
                'success' => true,
                'message' => 'Statistik kelas kosong berhasil diambil',
                'data' => [
                    'total_kelas_kosong' => $totalKelasKosong,
                    'kelas_kosong_hari_ini' => $kelasKosongHariIni,
                    'hari_ini' => $hariIni,
                    'per_hari' => $kelasKosongPerHari,
                    'kelas_paling_sering_kosong' => $kelasPalingSering,
                    'jam_paling_sering_kosong' => $jamPalingSering,
                    'mata_pelajaran_paling_sering_kosong' => $mapelPalingSering,
                    'tingkat_keparahan' => $this->getTingkatKeparahan($totalKelasKosong)
                ]
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil statistik kelas kosong',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get tingkat keparahan berdasarkan jumlah kelas kosong
     * 
     * @param int $jumlah
     * @return array
     */
    private function getTingkatKeparahan($jumlah)
    {
        if ($jumlah == 0) {
            return [
                'level' => 'Aman',
                'color' => 'green',
                'message' => 'Tidak ada kelas kosong'
            ];
        } elseif ($jumlah <= 5) {
            return [
                'level' => 'Rendah',
                'color' => 'blue',
                'message' => 'Jumlah kelas kosong masih terkendali'
            ];
        } elseif ($jumlah <= 10) {
            return [
                'level' => 'Sedang',
                'color' => 'yellow',
                'message' => 'Perlu perhatian untuk mencari guru pengganti'
            ];
        } else {
            return [
                'level' => 'Tinggi',
                'color' => 'red',
                'message' => 'Diperlukan tindakan segera!'
            ];
        }
    }

    /**
     * ✅ BARU: Get jadwal dengan info guru izin dan guru pengganti
     * Endpoint untuk Siswa Activity - Jadwal Pelajaran
     * GET /api/jadwal/dengan-status-guru
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function jadwalDenganStatusGuru(Request $request)
    {
        try {
            // Validasi query parameters
            $validator = Validator::make($request->all(), [
                'hari' => 'nullable|string|in:Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu',
                'kelas' => 'nullable|string',
                'tanggal' => 'nullable|date_format:Y-m-d'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Tentukan tanggal dan hari
            $tanggal = $request->tanggal ?? now()->format('Y-m-d');

            // Jika hari tidak disediakan, ambil dari tanggal
            if ($request->has('hari') && !empty($request->hari)) {
                $hari = $request->hari;
            } else {
                $hariMap = [
                    'Monday' => 'Senin',
                    'Tuesday' => 'Selasa',
                    'Wednesday' => 'Rabu',
                    'Thursday' => 'Kamis',
                    'Friday' => 'Jumat',
                    'Saturday' => 'Sabtu',
                    'Sunday' => 'Minggu',
                ];
                $hariInggris = \Carbon\Carbon::parse($tanggal)->format('l');
                $hari = $hariMap[$hariInggris] ?? 'Senin';
            }

            // Query jadwal
            $query = Jadwal::query()->where('hari', $hari);

            if ($request->has('kelas') && !empty($request->kelas)) {
                $query->where('kelas', $request->kelas);
            }

            $query->orderBy('jam', 'asc');

            $jadwalList = $query->get();

            // Ambil semua kehadiran guru izin untuk tanggal ini
            $kehadiranIzin = \App\Models\Kehadiran::where('status', 'Izin')
                ->where(function ($q) use ($tanggal) {
                    $q->where('tanggal', $tanggal)
                        ->orWhere(function ($q2) use ($tanggal) {
                            // Cek juga izin multi-hari
                            $q2->whereNotNull('tanggal_mulai_izin')
                                ->whereNotNull('tanggal_selesai_izin')
                                ->whereDate('tanggal_mulai_izin', '<=', $tanggal)
                                ->whereDate('tanggal_selesai_izin', '>=', $tanggal);
                        });
                })
                ->get()
                ->keyBy('kode_guru');

            // ✅ BARU: Ambil semua kehadiran guru yang dilaporkan "Tidak Hadir" oleh siswa untuk tanggal ini
            $kehadiranTidakHadir = \App\Models\Kehadiran::where('status', 'Tidak Hadir')
                ->where('tanggal', $tanggal)
                ->get()
                ->keyBy('kode_guru');

            // Map jadwal dengan info guru izin atau tidak hadir
            $result = $jadwalList->map(function ($jadwal) use ($kehadiranIzin, $kehadiranTidakHadir, $tanggal) {
                $kodeGuru = $jadwal->kode_guru;
                $guruIzin = $kehadiranIzin->get($kodeGuru);
                $guruTidakHadir = $kehadiranTidakHadir->get($kodeGuru);

                $statusGuru = 'normal'; // Default: guru normal/hadir
                $infoIzin = null;
                $guruPengganti = null;

                // Prioritas: Tidak Hadir > Izin (jika siswa melaporkan tidak hadir, tampilkan status tidak_hadir)
                if ($guruTidakHadir) {
                    $statusGuru = 'tidak_hadir';
                    $infoIzin = [
                        'id' => $guruTidakHadir->id,
                        'keterangan' => $guruTidakHadir->keterangan ?? 'Guru tidak hadir (dilaporkan oleh siswa)',
                        'tanggal_mulai' => $tanggal,
                        'tanggal_selesai' => $tanggal,
                        'durasi_hari' => 1,
                    ];

                    // Cek apakah ada guru pengganti
                    if ($guruTidakHadir->kode_guru_pengganti) {
                        $guruPengganti = [
                            'kode_guru' => $guruTidakHadir->kode_guru_pengganti,
                            'nama_guru' => $guruTidakHadir->nama_guru_pengganti,
                            'keterangan' => $guruTidakHadir->keterangan_pengganti,
                        ];
                    }
                } elseif ($guruIzin) {
                    $statusGuru = 'izin';
                    $infoIzin = [
                        'id' => $guruIzin->id,
                        'keterangan' => $guruIzin->keterangan,
                        'tanggal_mulai' => $guruIzin->tanggal_mulai_izin?->format('Y-m-d'),
                        'tanggal_selesai' => $guruIzin->tanggal_selesai_izin?->format('Y-m-d'),
                        'durasi_hari' => $guruIzin->durasi_izin_hari,
                    ];

                    // Cek apakah ada guru pengganti
                    if ($guruIzin->kode_guru_pengganti) {
                        $guruPengganti = [
                            'kode_guru' => $guruIzin->kode_guru_pengganti,
                            'nama_guru' => $guruIzin->nama_guru_pengganti,
                            'keterangan' => $guruIzin->keterangan_pengganti,
                        ];
                    }
                }

                return [
                    'id' => $jadwal->id,
                    'hari' => $jadwal->hari,
                    'kelas' => $jadwal->kelas,
                    'jam' => $jadwal->jam,
                    'mata_pelajaran' => $jadwal->mata_pelajaran,
                    'kode_guru' => $jadwal->kode_guru,
                    'nama_guru' => $jadwal->nama_guru,
                    'ruangan' => $jadwal->ruangan,
                    'status_guru' => $statusGuru,
                    'info_izin' => $infoIzin,
                    'guru_pengganti' => $guruPengganti,
                ];
            });

            // Hitung statistik
            $totalJadwal = $result->count();
            $totalGuruIzin = $result->where('status_guru', 'izin')->count();
            $totalGuruTidakHadir = $result->where('status_guru', 'tidak_hadir')->count();
            $totalAdaPengganti = $result->filter(fn($j) => $j['guru_pengganti'] !== null)->count();

            return response()->json([
                'success' => true,
                'message' => 'Data jadwal dengan status guru berhasil diambil',
                'filter' => [
                    'hari' => $hari,
                    'kelas' => $request->kelas ?? 'Semua',
                    'tanggal' => $tanggal,
                ],
                'statistik' => [
                    'total_jadwal' => $totalJadwal,
                    'guru_izin' => $totalGuruIzin,
                    'guru_tidak_hadir' => $totalGuruTidakHadir,
                    'ada_pengganti' => $totalAdaPengganti,
                    'tanpa_pengganti' => ($totalGuruIzin + $totalGuruTidakHadir) - $totalAdaPengganti,
                ],
                'data' => $result->values()
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data jadwal',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
