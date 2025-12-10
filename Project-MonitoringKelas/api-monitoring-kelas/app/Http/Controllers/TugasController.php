<?php

namespace App\Http\Controllers;

use App\Models\Tugas;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class TugasController extends Controller
{
    /**
     * Display a listing of tugas
     * GET /api/tugas?user_id=1
     */
    public function index(Request $request)
    {
        $query = Tugas::with('user:id,nama,email');

        // Filter berdasarkan user_id jika ada
        if ($request->has('user_id') && !empty($request->user_id)) {
            $query->where('user_id', $request->user_id);
        }

        // Order by tanggal DESC (terbaru dulu)
        $tugas = $query->orderBy('tanggal', 'desc')
            ->orderBy('created_at', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'message' => 'Data tugas berhasil diambil',
            'data' => $tugas
        ], 200);
    }

    /**
     * Store a newly created tugas
     * POST /api/tugas
     */
    public function store(Request $request)
    {
        // Validasi input
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|exists:users,id',
            'tanggal' => 'required|date',
            'mata_pelajaran' => 'required|string',
            'judul_tugas' => 'required|string',
            'status' => 'required|in:Selesai,Belum Selesai,Terlambat'
        ], [
            'user_id.required' => 'User ID wajib diisi',
            'user_id.exists' => 'User tidak ditemukan',
            'tanggal.required' => 'Tanggal wajib diisi',
            'tanggal.date' => 'Format tanggal tidak valid',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'judul_tugas.required' => 'Judul tugas wajib diisi',
            'status.required' => 'Status tugas wajib diisi',
            'status.in' => 'Status harus: Selesai, Belum Selesai, atau Terlambat'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Simpan tugas
        $tugas = Tugas::create([
            'user_id' => $request->user_id,
            'tanggal' => $request->tanggal,
            'mata_pelajaran' => $request->mata_pelajaran,
            'judul_tugas' => $request->judul_tugas,
            'status' => $request->status
        ]);

        // Load relasi user
        $tugas->load('user:id,nama,email');

        return response()->json([
            'success' => true,
            'message' => 'Tugas berhasil ditambahkan',
            'data' => $tugas
        ], 201);
    }

    /**
     * Display the specified tugas
     * GET /api/tugas/{id}
     */
    public function show($id)
    {
        $tugas = Tugas::with('user:id,nama,email,role')->find($id);

        if (!$tugas) {
            return response()->json([
                'success' => false,
                'message' => 'Tugas tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Data tugas berhasil diambil',
            'data' => $tugas
        ], 200);
    }

    /**
     * Update the specified tugas
     * PUT/PATCH /api/tugas/{id}
     */
    public function update(Request $request, $id)
    {
        $tugas = Tugas::find($id);

        if (!$tugas) {
            return response()->json([
                'success' => false,
                'message' => 'Tugas tidak ditemukan'
            ], 404);
        }

        // Validasi input
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|exists:users,id',
            'tanggal' => 'required|date',
            'mata_pelajaran' => 'required|string',
            'judul_tugas' => 'required|string',
            'status' => 'required|in:Selesai,Belum Selesai,Terlambat'
        ], [
            'user_id.required' => 'User ID wajib diisi',
            'user_id.exists' => 'User tidak ditemukan',
            'tanggal.required' => 'Tanggal wajib diisi',
            'tanggal.date' => 'Format tanggal tidak valid',
            'mata_pelajaran.required' => 'Mata pelajaran wajib diisi',
            'judul_tugas.required' => 'Judul tugas wajib diisi',
            'status.required' => 'Status tugas wajib diisi',
            'status.in' => 'Status harus: Selesai, Belum Selesai, atau Terlambat'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Update tugas
        $tugas->update([
            'user_id' => $request->user_id,
            'tanggal' => $request->tanggal,
            'mata_pelajaran' => $request->mata_pelajaran,
            'judul_tugas' => $request->judul_tugas,
            'status' => $request->status
        ]);

        // Load relasi user
        $tugas->load('user:id,nama,email');

        return response()->json([
            'success' => true,
            'message' => 'Tugas berhasil diupdate',
            'data' => $tugas
        ], 200);
    }

    /**
     * Remove the specified tugas
     * DELETE /api/tugas/{id}
     */
    public function destroy($id)
    {
        $tugas = Tugas::find($id);

        if (!$tugas) {
            return response()->json([
                'success' => false,
                'message' => 'Tugas tidak ditemukan'
            ], 404);
        }

        $tugas->delete();

        return response()->json([
            'success' => true,
            'message' => 'Tugas berhasil dihapus'
        ], 200);
    }
}
