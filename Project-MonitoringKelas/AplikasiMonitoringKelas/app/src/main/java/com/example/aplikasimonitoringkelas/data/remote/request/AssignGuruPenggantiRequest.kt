package com.example.aplikasimonitoringkelas.data.remote.request

import com.google.gson.annotations.SerializedName

data class AssignGuruPenggantiRequest(
    @SerializedName("nama_guru_pengganti")
    val namaGuruPengganti: String,

    @SerializedName("kode_guru_pengganti")
    val kodeGuruPengganti: String,

    @SerializedName("keterangan_pengganti")
    val keteranganPengganti: String? = null
)
