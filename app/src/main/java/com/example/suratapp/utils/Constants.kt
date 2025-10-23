package com.example.suratapp.utils

object Constants {
    const val USER_ADMIN = "admin"
    const val USER_REGULAR = "user"

    const val SURAT_TYPE_MASUK = "masuk"
    const val SURAT_TYPE_KELUAR = "keluar"

    // Status Surat Masuk
    const val STATUS_SUB_BAGIAN_UMUM = "Sub Bagian Umum"
    const val STATUS_SEKRETARIS = "Sekretaris"
    const val STATUS_KEPALA = "Kepala"
    const val STATUS_DISPOSISI = "Disposisi ke Bidang"

    // Status Surat Keluar
    const val STATUS_BIDANG = "Bidang"
    const val STATUS_PENERIMA = "Penerima"

    // Request Codes
    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_STORAGE_PERMISSION = 101

    // File
    const val MAX_FILE_SIZE_MB = 10
    const val SUPABASE_BUCKET = "surat_files"
}
