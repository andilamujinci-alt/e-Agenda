package com.example.suratapp.data.models

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
data class Pegawai(
    val id: String? = null,
    val nip: String,
    val nama: String,
    val role: String, // "admin" atau "user"
    val email: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) : Parcelable
data class LoginResponse(
    val success: Boolean,
    val pegawai: Pegawai? = null,
    val message: String? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
data class SuratMasuk(
    val id: Int? = null,
    val pengirim: String = "",
    @SerialName("nomor_surat")
    val nomorSurat: String = "",
    @SerialName("tanggal_surat")
    val tanggalSurat: String = "",
    @SerialName("nomor_agenda")
    val nomorAgenda: String = "",
    @SerialName("tanggal_diterima")
    val tanggalDiterima: String = "",
    val perihal: String = "",
    @SerialName("status_surat")
    val statusSurat: String = "",
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("nama_penerima")
    val namaPenerima: String? = null,
    @SerialName("tanda_tangan")
    val tandaTangan: String? = null,
    @SerialName("timestamp_disposisi")
    val timestampDisposisi: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Parcelable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
data class SuratKeluar(
    val id: Int? = null,
    val pengirim: String = "",
    @SerialName("nomor_surat")
    val nomorSurat: String = "",
    @SerialName("tanggal_surat")
    val tanggalSurat: String = "",
    @SerialName("nomor_agenda")
    val nomorAgenda: String = "",
    @SerialName("tanggal_diterima")
    val tanggalDiterima: String = "",
    val perihal: String = "",
    @SerialName("status_surat")
    val statusSurat: String = "",
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("nama_penerima")
    val namaPenerima: String? = null,
    @SerialName("tanda_tangan")
    val tandaTangan: String? = null,
    @SerialName("timestamp_disposisi")
    val timestampDisposisi: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Parcelable