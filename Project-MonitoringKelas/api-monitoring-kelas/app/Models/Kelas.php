<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Kelas extends Model
{
    use HasFactory;

    protected $table = 'kelas';

    protected $fillable = [
        'nama_kelas',
        'tingkat',
        'jurusan',
        'wali_kelas',
        'ruangan',
        'kapasitas',
        'jumlah_siswa',
        'keterangan',
        'status'
    ];

    protected $casts = [
        'kapasitas' => 'integer',
        'jumlah_siswa' => 'integer',
    ];

    /**
     * Get siswa untuk kelas ini
     */
    public function siswa()
    {
        return $this->hasMany(User::class, 'kelas', 'nama_kelas')
            ->where('role', 'siswa');
    }

    /**
     * Get jadwal untuk kelas ini
     */
    public function jadwal()
    {
        return $this->hasMany(Jadwal::class, 'kelas', 'nama_kelas');
    }

    /**
     * Scope untuk kelas aktif
     */
    public function scopeAktif($query)
    {
        return $query->where('status', 'Aktif');
    }

    /**
     * Accessor untuk nama lengkap kelas
     */
    public function getNamaLengkapAttribute()
    {
        return "{$this->tingkat} {$this->jurusan} {$this->nama_kelas}";
    }
}
