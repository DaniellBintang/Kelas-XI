<?php

namespace App\Http\Controllers;

use App\Models\Kehadiran;
use App\Models\Guru;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class KelasTerpengaruhController extends Controller
{
    /**
     * Get kelas yang terpengaruh berdasarkan izin guru
     * 
     * @param Request $request
     * @return JsonResponse
     * 
     * Query params:
     * - kode_guru: string (required)
     * - tanggal_mulai: Y-m-d (required)
     * - tanggal_selesai: Y-m-d (required)
     */
    public function getKelasTerpengaruh(Request $request): JsonResponse
    {
        $request->validate([
            'kode_guru' => 'required|string',
            'tanggal_mulai' => 'required|date',
            'tanggal_selesai' => 'required|date|after_or_equal:tanggal_mulai',
        ]);

        $kodeGuru = $request->input('kode_guru');
        $tanggalMulai = $request->input('tanggal_mulai');
        $tanggalSelesai = $request->input('tanggal_selesai');

        // Get guru info
        $guru = Guru::where('kode_guru', $kodeGuru)->first();

        if (!$guru) {
            return response()->json([
                'success' => false,
                'message' => 'Guru tidak ditemukan',
                'data' => null
            ], 404);
        }

        // Get ringkasan kelas terpengaruh
        $ringkasan = Kehadiran::getRingkasanKelasTerpengaruh($kodeGuru, $tanggalMulai, $tanggalSelesai);

        return response()->json([
            'success' => true,
            'message' => 'Data kelas terpengaruh berhasil diambil',
            'data' => [
                'guru' => [
                    'kode_guru' => $guru->kode_guru,
                    'nama' => $guru->nama,
                    'mata_pelajaran' => $guru->mata_pelajaran,
                ],
                'periode_izin' => [
                    'tanggal_mulai' => $tanggalMulai,
                    'tanggal_selesai' => $tanggalSelesai,
                ],
                'ringkasan' => [
                    'total_hari_terpengaruh' => $ringkasan['total_hari_terpengaruh'],
                    'total_kelas_terpengaruh' => $ringkasan['total_kelas_terpengaruh'],
                    'kelas_per_hari' => $ringkasan['kelas_by_hari'],
                ],
                'detail_per_tanggal' => $ringkasan['detail'],
            ]
        ]);
    }

    /**
     * Get preview kelas terpengaruh (untuk form Filament via AJAX)
     */
    public function previewKelasTerpengaruh(Request $request): JsonResponse
    {
        $kodeGuru = $request->input('kode_guru');
        $tanggalMulai = $request->input('tanggal_mulai');
        $tanggalSelesai = $request->input('tanggal_selesai');

        if (!$kodeGuru || !$tanggalMulai || !$tanggalSelesai) {
            return response()->json([
                'success' => false,
                'message' => 'Parameter tidak lengkap',
                'html' => '<p class="text-gray-500 text-sm">Pilih guru dan tentukan periode izin untuk melihat kelas yang terpengaruh.</p>'
            ]);
        }

        $kelasTerpengaruh = Kehadiran::getKelasTerpengaruh($kodeGuru, $tanggalMulai, $tanggalSelesai);

        if (empty($kelasTerpengaruh)) {
            return response()->json([
                'success' => true,
                'message' => 'Tidak ada kelas terpengaruh',
                'html' => '<p class="text-green-600 text-sm font-medium">✓ Tidak ada jadwal mengajar di periode ini.</p>',
                'data' => []
            ]);
        }

        // Generate HTML untuk preview
        $html = '<div class="space-y-4">';

        $totalKelas = 0;
        foreach ($kelasTerpengaruh as $item) {
            $totalKelas += $item['jumlah_kelas'];
            $html .= '<div class="border rounded-lg p-3 bg-amber-50 border-amber-200">';
            $html .= '<div class="flex items-center justify-between mb-2">';
            $html .= '<span class="font-semibold text-amber-800">' . $item['hari'] . ', ' . $item['tanggal_formatted'] . '</span>';
            $html .= '<span class="text-xs bg-amber-200 text-amber-800 px-2 py-1 rounded">' . $item['jumlah_kelas'] . ' kelas</span>';
            $html .= '</div>';
            $html .= '<div class="space-y-1">';

            foreach ($item['kelas_list'] as $kelas) {
                $html .= '<div class="flex items-center text-sm text-amber-700">';
                $html .= '<span class="w-24 font-medium">' . $kelas['jam'] . '</span>';
                $html .= '<span class="flex-1">' . $kelas['kelas'] . '</span>';
                $html .= '<span class="text-xs text-amber-600">' . $kelas['mata_pelajaran'] . '</span>';
                $html .= '</div>';
            }

            $html .= '</div>';
            $html .= '</div>';
        }

        $html .= '<div class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">';
        $html .= '<p class="text-red-700 font-semibold">⚠️ Total: ' . $totalKelas . ' kelas akan kosong selama ' . count($kelasTerpengaruh) . ' hari</p>';
        $html .= '</div>';
        $html .= '</div>';

        return response()->json([
            'success' => true,
            'message' => 'Data kelas terpengaruh berhasil diambil',
            'html' => $html,
            'data' => $kelasTerpengaruh,
            'summary' => [
                'total_hari' => count($kelasTerpengaruh),
                'total_kelas' => $totalKelas
            ]
        ]);
    }
}
