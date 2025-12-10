<?php

namespace App\Http\Controllers;

use App\Models\Guru;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class GuruController extends Controller
{
    /**
     * Display a listing of guru
     * GET /api/guru?status=Aktif&mata_pelajaran=Matematika
     */
    public function index(Request $request)
    {
        $query = Guru::query();

        // Filter berdasarkan status jika ada
        if ($request->has('status') && !empty($request->status)) {
            $query->where('status', $request->status);
        }

        // Filter berdasarkan mata pelajaran jika ada
        if ($request->has('mata_pelajaran') && !empty($request->mata_pelajaran)) {
            $query->where('mata_pelajaran', 'like', '%' . $request->mata_pelajaran . '%');
        }

        // Search berdasarkan nama jika ada
        if ($request->has('search') && !empty($request->search)) {
            $query->where('nama', 'like', '%' . $request->search . '%');
        }

        // Order by nama ASC
        $gurus = $query->orderBy('nama', 'asc')->get();

        return response()->json([
            'success' => true,
            'message' => 'Data guru berhasil diambil',
            'data' => $gurus
        ], 200);
    }

    /**
     * Store a newly created guru
     * POST /api/guru
     */
    public function store(Request $request)
    {
        // Validasi input
        $validator = Validator::make($request->all(), [
            'kode_guru' => 'required|string|max:50|unique:guru,kode_guru',
            'nama' => 'required|string|max:100',
            'mata_pelajaran' => 'required|string|max:100',
            'email' => 'nullable|email|max:100',
            'no_telepon' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'status' => 'required|in:Aktif,Cuti,Nonaktif'
        ], [
            'kode_guru.required' => 'Kode guru wajib diisi',
            'kode_guru.unique' => 'Kode guru sudah terdaftar',
            'nama.required' => 'Nama guru wajib diisi',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'email.email' => 'Format email tidak valid',
            'status.required' => 'Status wajib diisi',
            'status.in' => 'Status harus: Aktif, Cuti, atau Nonaktif'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Simpan data guru
        $guru = Guru::create([
            'kode_guru' => $request->kode_guru,
            'nama' => $request->nama,
            'mata_pelajaran' => $request->mata_pelajaran,
            'email' => $request->email,
            'no_telepon' => $request->no_telepon,
            'alamat' => $request->alamat,
            'status' => $request->status
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Data guru berhasil ditambahkan',
            'data' => $guru
        ], 201);
    }

    /**
     * Display the specified guru
     * GET /api/guru/{id}
     */
    public function show($id)
    {
        $guru = Guru::with(['kehadiran', 'jadwal'])->find($id);

        if (!$guru) {
            return response()->json([
                'success' => false,
                'message' => 'Guru tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Data guru berhasil diambil',
            'data' => $guru
        ], 200);
    }

    /**
     * Update the specified guru
     * PUT/PATCH /api/guru/{id}
     */
    public function update(Request $request, $id)
    {
        $guru = Guru::find($id);

        if (!$guru) {
            return response()->json([
                'success' => false,
                'message' => 'Guru tidak ditemukan'
            ], 404);
        }

        // Validasi input
        $validator = Validator::make($request->all(), [
            'kode_guru' => 'required|string|max:50|unique:guru,kode_guru,' . $id,
            'nama' => 'required|string|max:100',
            'mata_pelajaran' => 'required|string|max:100',
            'email' => 'nullable|email|max:100',
            'no_telepon' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'status' => 'required|in:Aktif,Cuti,Nonaktif'
        ], [
            'kode_guru.required' => 'Kode guru wajib diisi',
            'kode_guru.unique' => 'Kode guru sudah terdaftar',
            'nama.required' => 'Nama guru wajib diisi',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'email.email' => 'Format email tidak valid',
            'status.required' => 'Status wajib diisi',
            'status.in' => 'Status harus: Aktif, Cuti, atau Nonaktif'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Update data guru
        $guru->update([
            'kode_guru' => $request->kode_guru,
            'nama' => $request->nama,
            'mata_pelajaran' => $request->mata_pelajaran,
            'email' => $request->email,
            'no_telepon' => $request->no_telepon,
            'alamat' => $request->alamat,
            'status' => $request->status
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Data guru berhasil diupdate',
            'data' => $guru
        ], 200);
    }

    /**
     * Remove the specified guru
     * DELETE /api/guru/{id}
     */
    public function destroy($id)
    {
        $guru = Guru::find($id);

        if (!$guru) {
            return response()->json([
                'success' => false,
                'message' => 'Guru tidak ditemukan'
            ], 404);
        }

        $guru->delete();

        return response()->json([
            'success' => true,
            'message' => 'Data guru berhasil dihapus'
        ], 200);
    }

    /**
     * Get statistics guru
     * GET /api/guru/statistics
     */
    public function statistics()
    {
        $totalGuru = Guru::count();
        $guruAktif = Guru::where('status', 'Aktif')->count();
        $guruCuti = Guru::where('status', 'Cuti')->count();
        $guruNonaktif = Guru::where('status', 'Nonaktif')->count();

        // Guru per mata pelajaran
        $guruPerMataPelajaran = Guru::select('mata_pelajaran')
            ->selectRaw('COUNT(*) as total')
            ->groupBy('mata_pelajaran')
            ->orderBy('total', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'message' => 'Statistik guru berhasil diambil',
            'data' => [
                'total_guru' => $totalGuru,
                'guru_aktif' => $guruAktif,
                'guru_cuti' => $guruCuti,
                'guru_nonaktif' => $guruNonaktif,
                'guru_per_mata_pelajaran' => $guruPerMataPelajaran
            ]
        ], 200);
    }
}
