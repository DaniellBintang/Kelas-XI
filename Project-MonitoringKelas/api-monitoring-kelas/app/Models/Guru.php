<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Guru extends Model
{
    use HasFactory;

    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'guru';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'kode_guru',
        'nama',
        'mata_pelajaran',
        'email',
        'no_telepon',
        'alamat',
        'status', // Aktif, Cuti, Nonaktif
    ];

    /**
     * Get the kehadiran records for the guru.
     */
    public function kehadiran()
    {
        return $this->hasMany(Kehadiran::class, 'guru_id');
    }

    /**
     * Get the jadwal for the guru.
     */
    public function jadwal()
    {
        return $this->hasMany(Jadwal::class, 'kode_guru', 'kode_guru');
    }
}
