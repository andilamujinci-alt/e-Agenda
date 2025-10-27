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
    const val STATUS_EKONOMI = "Ekonomi"
    const val STATUS_SARPRAS = "Sarpras"
    const val STATUS_SOSBUD = "Sosbud"
    const val STATUS_LITBANG = "Litbang"
    const val STATUS_PROGRAM = "Program"
    const val STATUS_KEUANGAN = "Keuangan"

    // Status Surat Keluar
    const val STATUS_PENERIMA = "Penerima"

    // Request Codes
    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_STORAGE_PERMISSION = 101

    // File
    const val MAX_FILE_SIZE_MB = 2 // UBAH KE 2MB
    const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L // 2MB in bytes
    const val SUPABASE_BUCKET = "surat_files"
}
