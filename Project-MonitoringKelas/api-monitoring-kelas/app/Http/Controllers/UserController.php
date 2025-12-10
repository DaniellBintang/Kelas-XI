<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

class UserController extends Controller
{
    /**
     * Display a listing of users with pagination
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            // Validasi query parameters (optional)
            $validator = Validator::make($request->all(), [
                'role' => 'nullable|string|in:siswa,kurikulum,kepala_sekolah,admin',
                'status' => 'nullable|string|in:aktif,nonaktif',
                'search' => 'nullable|string|max:255',
                'per_page' => 'nullable|integer|min:1|max:100'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Query builder
            $query = User::query();

            // Filter by role
            if ($request->has('role') && $request->role != null) {
                $query->where('role', $request->role);
            }

            // Filter by status (if you have status field)
            if ($request->has('status') && $request->status != null) {
                $query->where('status', $request->status);
            }

            // Search by name or email
            if ($request->has('search') && $request->search != null) {
                $searchTerm = $request->search;
                $query->where(function ($q) use ($searchTerm) {
                    $q->where('nama', 'like', '%' . $searchTerm . '%')
                        ->orWhere('email', 'like', '%' . $searchTerm . '%');
                });
            }

            // Order by created_at desc (newest first)
            $query->orderBy('created_at', 'desc');

            // Pagination
            $perPage = $request->input('per_page', 10);
            $users = $query->paginate($perPage);

            // Remove password from response
            $users->getCollection()->transform(function ($user) {
                return [
                    'id' => $user->id,
                    'nama' => $user->nama,
                    'email' => $user->email,
                    'role' => $user->role,
                    'kelas' => $user->kelas ?? null,
                    'status' => $user->status ?? 'aktif',
                    'created_at' => $user->created_at,
                    'updated_at' => $user->updated_at
                ];
            });

            // Summary statistics
            $summary = [
                'total_users' => User::count(),
                'total_siswa' => User::where('role', 'siswa')->count(),
                'total_kurikulum' => User::where('role', 'kurikulum')->count(),
                'total_kepala_sekolah' => User::where('role', 'kepala_sekolah')->count(),
                'total_admin' => User::where('role', 'admin')->count()
            ];

            return response()->json([
                'success' => true,
                'message' => 'Data user berhasil diambil',
                'summary' => $summary,
                'data' => $users->items(),
                'pagination' => [
                    'current_page' => $users->currentPage(),
                    'per_page' => $users->perPage(),
                    'total' => $users->total(),
                    'last_page' => $users->lastPage(),
                    'from' => $users->firstItem(),
                    'to' => $users->lastItem()
                ]
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil data user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Store a newly created user
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        try {
            // Validasi input
            $validator = Validator::make($request->all(), [
                'nama' => 'required|string|max:255',
                'email' => 'required|email|unique:users,email|max:255',
                'password' => 'required|string|min:8|max:255',
                'role' => 'required|string|in:siswa,kurikulum,kepala_sekolah,admin',
                'kelas' => 'nullable|string|max:50', // Optional, khusus untuk siswa
                'status' => 'nullable|string|in:aktif,nonaktif'
            ], [
                'nama.required' => 'Nama wajib diisi',
                'nama.max' => 'Nama maksimal 255 karakter',
                'email.required' => 'Email wajib diisi',
                'email.email' => 'Format email tidak valid',
                'email.unique' => 'Email sudah terdaftar',
                'password.required' => 'Password wajib diisi',
                'password.min' => 'Password minimal 8 karakter',
                'role.required' => 'Role wajib diisi',
                'role.in' => 'Role harus salah satu dari: siswa, kurikulum, kepala_sekolah, admin'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Create user baru
            $user = User::create([
                'nama' => $request->nama,
                'email' => $request->email,
                'password' => Hash::make($request->password), // Hash password dengan bcrypt
                'role' => $request->role,
                'kelas' => $request->kelas,
                'status' => $request->status ?? 'aktif'
            ]);

            // Response tanpa password
            $userData = [
                'id' => $user->id,
                'nama' => $user->nama,
                'email' => $user->email,
                'role' => $user->role,
                'kelas' => $user->kelas,
                'status' => $user->status,
                'created_at' => $user->created_at,
                'updated_at' => $user->updated_at
            ];

            return response()->json([
                'success' => true,
                'message' => 'User berhasil dibuat',
                'data' => $userData
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal membuat user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Display the specified user
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        try {
            // Cari user by ID
            $user = User::find($id);

            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'User tidak ditemukan'
                ], 404);
            }

            // Response tanpa password
            $userData = [
                'id' => $user->id,
                'nama' => $user->nama,
                'email' => $user->email,
                'role' => $user->role,
                'kelas' => $user->kelas,
                'status' => $user->status ?? 'aktif',
                'created_at' => $user->created_at,
                'updated_at' => $user->updated_at
            ];

            return response()->json([
                'success' => true,
                'message' => 'Detail user berhasil diambil',
                'data' => $userData
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil detail user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Update the specified user
     * 
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        try {
            // Cari user by ID
            $user = User::find($id);

            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'User tidak ditemukan'
                ], 404);
            }

            // Validasi input
            $validator = Validator::make($request->all(), [
                'nama' => 'required|string|max:255',
                'email' => [
                    'required',
                    'email',
                    'max:255',
                    Rule::unique('users', 'email')->ignore($id) // Unique kecuali untuk user ini
                ],
                'password' => 'nullable|string|min:8|max:255', // Optional, jika diisi maka update password
                'role' => 'required|string|in:siswa,kurikulum,kepala_sekolah,admin',
                'kelas' => 'nullable|string|max:50',
                'status' => 'nullable|string|in:aktif,nonaktif'
            ], [
                'nama.required' => 'Nama wajib diisi',
                'nama.max' => 'Nama maksimal 255 karakter',
                'email.required' => 'Email wajib diisi',
                'email.email' => 'Format email tidak valid',
                'email.unique' => 'Email sudah terdaftar',
                'password.min' => 'Password minimal 8 karakter',
                'role.required' => 'Role wajib diisi',
                'role.in' => 'Role harus salah satu dari: siswa, kurikulum, kepala_sekolah, admin'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Update data user
            $user->nama = $request->nama;
            $user->email = $request->email;
            $user->role = $request->role;
            $user->kelas = $request->kelas;
            $user->status = $request->status ?? $user->status ?? 'aktif';

            // Update password hanya jika diisi
            if ($request->filled('password')) {
                $user->password = Hash::make($request->password);
            }

            $user->save();

            // Response tanpa password
            $userData = [
                'id' => $user->id,
                'nama' => $user->nama,
                'email' => $user->email,
                'role' => $user->role,
                'kelas' => $user->kelas,
                'status' => $user->status,
                'created_at' => $user->created_at,
                'updated_at' => $user->updated_at
            ];

            return response()->json([
                'success' => true,
                'message' => 'User berhasil diupdate',
                'data' => $userData
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengupdate user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Remove the specified user
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        try {
            // Cari user by ID
            $user = User::find($id);

            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'User tidak ditemukan'
                ], 404);
            }

            // Simpan data user sebelum dihapus (untuk response)
            $userName = $user->nama;
            $userEmail = $user->email;
            $userRole = $user->role;

            // Hapus user
            $user->delete();

            return response()->json([
                'success' => true,
                'message' => "User {$userName} ({$userRole}) berhasil dihapus",
                'deleted_user' => [
                    'id' => $id,
                    'nama' => $userName,
                    'email' => $userEmail,
                    'role' => $userRole
                ]
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal menghapus user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get user statistics (for admin dashboard)
     * 
     * @return \Illuminate\Http\JsonResponse
     */
    public function statistics()
    {
        try {
            $stats = [
                'total_users' => User::count(),
                'users_by_role' => [
                    'siswa' => User::where('role', 'siswa')->count(),
                    'kurikulum' => User::where('role', 'kurikulum')->count(),
                    'kepala_sekolah' => User::where('role', 'kepala_sekolah')->count(),
                    'admin' => User::where('role', 'admin')->count()
                ],
                'users_by_status' => [
                    'aktif' => User::where('status', 'aktif')->count(),
                    'nonaktif' => User::where('status', 'nonaktif')->count()
                ],
                'recent_users' => User::orderBy('created_at', 'desc')
                    ->limit(5)
                    ->get()
                    ->map(function ($user) {
                        return [
                            'id' => $user->id,
                            'nama' => $user->nama,
                            'email' => $user->email,
                            'role' => $user->role,
                            'created_at' => $user->created_at
                        ];
                    }),
                'users_by_kelas' => User::where('role', 'siswa')
                    ->whereNotNull('kelas')
                    ->selectRaw('kelas, COUNT(*) as jumlah')
                    ->groupBy('kelas')
                    ->orderBy('kelas', 'asc')
                    ->get()
            ];

            return response()->json([
                'success' => true,
                'message' => 'Statistik user berhasil diambil',
                'data' => $stats
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengambil statistik user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Bulk delete users (admin only)
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function bulkDelete(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'user_ids' => 'required|array|min:1',
                'user_ids.*' => 'required|integer|exists:users,id'
            ], [
                'user_ids.required' => 'User IDs wajib diisi',
                'user_ids.array' => 'User IDs harus berupa array',
                'user_ids.min' => 'Minimal 1 user harus dipilih',
                'user_ids.*.exists' => 'Salah satu user tidak ditemukan'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            $userIds = $request->user_ids;

            // Get user data before delete
            $users = User::whereIn('id', $userIds)->get();

            // Delete users
            $deletedCount = User::whereIn('id', $userIds)->delete();

            return response()->json([
                'success' => true,
                'message' => "{$deletedCount} user berhasil dihapus",
                'deleted_count' => $deletedCount,
                'deleted_users' => $users->map(function ($user) {
                    return [
                        'id' => $user->id,
                        'nama' => $user->nama,
                        'email' => $user->email,
                        'role' => $user->role
                    ];
                })
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal menghapus user',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Change user status (activate/deactivate)
     * 
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function changeStatus(Request $request, $id)
    {
        try {
            $user = User::find($id);

            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'User tidak ditemukan'
                ], 404);
            }

            $validator = Validator::make($request->all(), [
                'status' => 'required|string|in:aktif,nonaktif'
            ], [
                'status.required' => 'Status wajib diisi',
                'status.in' => 'Status harus aktif atau nonaktif'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validasi gagal',
                    'errors' => $validator->errors()
                ], 422);
            }

            $oldStatus = $user->status ?? 'aktif';
            $user->status = $request->status;
            $user->save();

            return response()->json([
                'success' => true,
                'message' => "Status user berhasil diubah dari {$oldStatus} menjadi {$request->status}",
                'data' => [
                    'id' => $user->id,
                    'nama' => $user->nama,
                    'email' => $user->email,
                    'role' => $user->role,
                    'status' => $user->status,
                    'updated_at' => $user->updated_at
                ]
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal mengubah status user',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
