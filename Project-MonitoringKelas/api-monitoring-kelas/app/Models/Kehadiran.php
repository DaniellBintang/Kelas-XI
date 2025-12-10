<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Carbon\Carbon;

class Kehadiran extends Model
{
    use HasFactory;

    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'kehadiran';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'jadwal_id',
        'guru_id',
        'tanggal',
        'jam_masuk',
        'jam_keluar',
        'mata_pelajaran',
        'nama_guru',
        'kode_guru',
        'status', // Hadir, Telat, Tidak Hadir, Izin
        'kelas', // ✅ Legacy - single kelas
        'kelas_terpengaruh', // ✅ BARU - JSON array kelas yang terpengaruh
        'keterangan',
        'nama_guru_pengganti',      // ✅ TAMBAHKAN
        'kode_guru_pengganti',      // ✅ TAMBAHKAN
        'keterangan_pengganti',     // ✅ TAMBAHKAN
        'waktu_assign_pengganti',
        'tanggal_mulai_izin',       // ✅ TAMBAHKAN
        'tanggal_selesai_izin',     // ✅ TAMBAHKAN
        'durasi_izin_hari',         // ✅ TAMBAHKAN
        'guru_pengganti_id',        // ✅ TAMBAHKAN
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'tanggal' => 'date',
            'jam_masuk' => 'datetime:H:i',
            'jam_keluar' => 'datetime:H:i',
            'tanggal_mulai_izin' => 'date',
            'tanggal_selesai_izin' => 'date',
            'kelas_terpengaruh' => 'array', // ✅ BARU - Cast ke array
        ];
    }

    /**
     * Get the jadwal that owns the kehadiran.
     */
    public function jadwal()
    {
        return $this->belongsTo(Jadwal::class, 'jadwal_id');
    }


    /**
     * Get the guru that owns the kehadiran.
     */
    public function guru()
    {
        return $this->belongsTo(Guru::class, 'guru_id');
    }

    /**
     * ✅ TAMBAHKAN: Relasi ke guru pengganti
     */
    public function guruPengganti()
    {
        return $this->belongsTo(Guru::class, 'guru_pengganti_id');
    }

    /**
     * ✅ BARU: Get kelas yang terpengaruh selama periode izin guru
     * 
     * @param string|null $kodeGuru - Kode guru yang izin
     * @param string|null $tanggalMulai - Tanggal mulai izin (Y-m-d)
     * @param string|null $tanggalSelesai - Tanggal selesai izin (Y-m-d)
     * @return array - Array of affected classes grouped by date
     */
    public static function getKelasTerpengaruh(?string $kodeGuru, ?string $tanggalMulai, ?string $tanggalSelesai): array
    {
        if (!$kodeGuru || !$tanggalMulai || !$tanggalSelesai) {
            return [];
        }

        $result = [];
        $hariMap = [
            'Monday' => 'Senin',
            'Tuesday' => 'Selasa',
            'Wednesday' => 'Rabu',
            'Thursday' => 'Kamis',
            'Friday' => 'Jumat',
            'Saturday' => 'Sabtu',
            'Sunday' => 'Minggu',
        ];

        // Parse tanggal
        $mulai = Carbon::parse($tanggalMulai);
        $selesai = Carbon::parse($tanggalSelesai);

        // Loop setiap hari dalam periode izin
        $currentDate = $mulai->copy();
        while ($currentDate <= $selesai) {
            $hariInggris = $currentDate->format('l');
            $hariIndonesia = $hariMap[$hariInggris] ?? null;

            if ($hariIndonesia) {
                // Cari jadwal guru di hari tersebut
                $jadwalHariIni = Jadwal::where('kode_guru', $kodeGuru)
                    ->where('hari', $hariIndonesia)
                    ->orderBy('jam')
                    ->get();

                if ($jadwalHariIni->count() > 0) {
                    $kelasList = [];
                    foreach ($jadwalHariIni as $jadwal) {
                        $kelasList[] = [
                            'jadwal_id' => $jadwal->id,
                            'kelas' => $jadwal->kelas,
                            'jam' => $jadwal->jam,
                            'mata_pelajaran' => $jadwal->mata_pelajaran,
                            'ruangan' => $jadwal->ruangan,
                        ];
                    }

                    $result[] = [
                        'tanggal' => $currentDate->format('Y-m-d'),
                        'tanggal_formatted' => $currentDate->format('d/m/Y'),
                        'hari' => $hariIndonesia,
                        'jumlah_kelas' => count($kelasList),
                        'kelas_list' => $kelasList,
                    ];
                }
            }

            $currentDate->addDay();
        }

        return $result;
    }

    /**
     * ✅ BARU: Get ringkasan kelas terpengaruh (untuk display singkat)
     */
    public static function getRingkasanKelasTerpengaruh(?string $kodeGuru, ?string $tanggalMulai, ?string $tanggalSelesai): array
    {
        $kelasTerpengaruh = self::getKelasTerpengaruh($kodeGuru, $tanggalMulai, $tanggalSelesai);

        $totalKelas = 0;
        $kelasByHari = [];

        foreach ($kelasTerpengaruh as $item) {
            $totalKelas += $item['jumlah_kelas'];
            $kelasByHari[$item['hari']] = ($kelasByHari[$item['hari']] ?? 0) + $item['jumlah_kelas'];
        }

        return [
            'total_hari_terpengaruh' => count($kelasTerpengaruh),
            'total_kelas_terpengaruh' => $totalKelas,
            'kelas_by_hari' => $kelasByHari,
            'detail' => $kelasTerpengaruh,
        ];
    }

    /**
     * ✅ BARU: Generate array kelas terpengaruh untuk disimpan ke database
     * Format: Array of unique class names yang terpengaruh
     * 
     * @param string|null $kodeGuru
     * @param string|null $tanggalMulai  
     * @param string|null $tanggalSelesai
     * @return array - Array kelas unik yang terpengaruh
     */
    public static function generateKelasTerpengaruhArray(?string $kodeGuru, ?string $tanggalMulai, ?string $tanggalSelesai): array
    {
        $kelasTerpengaruh = self::getKelasTerpengaruh($kodeGuru, $tanggalMulai, $tanggalSelesai);

        $kelasUnik = [];
        foreach ($kelasTerpengaruh as $item) {
            foreach ($item['kelas_list'] as $kelas) {
                if (!in_array($kelas['kelas'], $kelasUnik)) {
                    $kelasUnik[] = $kelas['kelas'];
                }
            }
        }

        // Sort untuk konsistensi
        sort($kelasUnik);

        return $kelasUnik;
    }

    /**
     * ✅ BARU: Get kelas terpengaruh as comma-separated string (untuk display)
     */
    public function getKelasTerpengaruhDisplayAttribute(): string
    {
        if (empty($this->kelas_terpengaruh)) {
            return '-';
        }

        $kelas = is_array($this->kelas_terpengaruh)
            ? $this->kelas_terpengaruh
            : json_decode($this->kelas_terpengaruh, true);

        return implode(', ', $kelas ?: []);
    }
}
