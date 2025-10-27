// Repository.kt
package com.example.suratapp.data.repository

import com.example.suratapp.data.SupabaseClient
import com.example.suratapp.data.models.LoginResponse
import com.example.suratapp.data.models.Pegawai
import com.example.suratapp.data.models.SuratKeluar
import com.example.suratapp.data.models.SuratMasuk
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.io.File

class SuratRepository {

    private val client = SupabaseClient.client

    // Login dengan NIP dan Password menggunakan Supabase Auth
    suspend fun login(nip: String, password: String): LoginResponse {
        return try {
            // 1. Cari pegawai berdasarkan NIP
            val pegawaiList = client.from("pegawai")
                .select {
                    filter {
                        eq("nip", nip)
                    }
                }
                .decodeList<Pegawai>()

            if (pegawaiList.isEmpty()) {
                return LoginResponse(
                    success = false,
                    message = "NIP tidak ditemukan"
                )
            }

            val pegawai = pegawaiList.first()

            // 2. Login ke Supabase Auth menggunakan email
            try {
                client.auth.signInWith(Email) {
                    email = pegawai.email
                    this.password = password
                }

                // 3. Login berhasil
                LoginResponse(
                    success = true,
                    pegawai = pegawai,
                    message = "Login berhasil"
                )
            } catch (authError: Exception) {
                authError.printStackTrace()
                LoginResponse(
                    success = false,
                    message = "NIP atau Password salah"
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            LoginResponse(
                success = false,
                message = "Terjadi kesalahan: ${e.message}"
            )
        }
    }

    // Logout
    suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get current user session
    suspend fun getCurrentUser(): Pegawai? {
        return try {
            val session = client.auth.currentSessionOrNull()
            if (session != null) {
                // Ambil data pegawai dari email yang login
                val email = session.user?.email ?: return null
                val pegawaiList = client.from("pegawai")
                    .select {
                        filter {
                            eq("email", email)
                        }
                    }
                    .decodeList<Pegawai>()

                pegawaiList.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Check if user is logged in
    suspend fun isLoggedIn(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    // Surat Masuk - Sort by tanggal_diterima DESC (terbaru di atas)
    suspend fun getAllSuratMasuk(): List<SuratMasuk> {
        return try {
            client.from("surat_masuk")
                .select {
                    order(column = "tanggal_diterima", order = Order.DESCENDING)
                }
                .decodeList<SuratMasuk>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Check duplicate Surat Masuk by nomor surat
    suspend fun checkDuplicateSuratMasukByNomorSurat(nomorSurat: String): Boolean {
        return try {
            val result = client.from("surat_masuk")
                .select {
                    filter {
                        eq("nomor_surat", nomorSurat)
                    }
                }
                .decodeList<SuratMasuk>()
            result.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Check duplicate Surat Masuk by nomor agenda
    suspend fun checkDuplicateSuratMasukByNomorAgenda(nomorAgenda: String): Boolean {
        return try {
            val result = client.from("surat_masuk")
                .select {
                    filter {
                        eq("nomor_agenda", nomorAgenda)
                    }
                }
                .decodeList<SuratMasuk>()
            result.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun insertSuratMasuk(surat: SuratMasuk): SuratMasuk? {
        return try {
            client.from("surat_masuk")
                .insert(surat) {
                    select()
                }
                .decodeSingle<SuratMasuk>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateSuratMasuk(id: Int, surat: SuratMasuk): SuratMasuk? {
        return try {
            client.from("surat_masuk")
                .update(surat) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<SuratMasuk>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Surat Keluar
    suspend fun getAllSuratKeluar(): List<SuratKeluar> {
        return try {
            client.from("surat_keluar")
                .select {
                    order(column = "tanggal_diterima", order = Order.DESCENDING)
                }
                .decodeList<SuratKeluar>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    // Check duplicate Surat Keluar by nomor surat
    suspend fun checkDuplicateSuratKeluarByNomorSurat(nomorSurat: String): Boolean {
        return try {
            val result = client.from("surat_keluar")
                .select {
                    filter {
                        eq("nomor_surat", nomorSurat)
                    }
                }
                .decodeList<SuratKeluar>()
            result.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Check duplicate Surat Keluar by nomor agenda
    suspend fun checkDuplicateSuratKeluarByNomorAgenda(nomorAgenda: String): Boolean {
        return try {
            val result = client.from("surat_keluar")
                .select {
                    filter {
                        eq("nomor_agenda", nomorAgenda)
                    }
                }
                .decodeList<SuratKeluar>()
            result.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun insertSuratKeluar(surat: SuratKeluar): SuratKeluar? {
        return try {
            client.from("surat_keluar")
                .insert(surat) {
                    select()
                }
                .decodeSingle<SuratKeluar>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateSuratKeluar(id: Int, surat: SuratKeluar): SuratKeluar? {
        return try {
            client.from("surat_keluar")
                .update(surat) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<SuratKeluar>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Upload file to Supabase Storage
    suspend fun uploadFile(file: File, bucket: String): String? {
        return try {
            val fileName = "${System.currentTimeMillis()}_${file.name}"
            val bytes = file.readBytes()

            client.storage.from(bucket).upload(fileName, bytes)

            client.storage.from(bucket).publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }



    }

    suspend fun deleteSuratMasuk(id: Int): Boolean {
        return try {
            client.from("surat_masuk")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Delete Surat Keluar
    suspend fun deleteSuratKeluar(id: Int): Boolean {
        return try {
            client.from("surat_keluar")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Delete file from storage (optional, untuk cleanup)
    suspend fun deleteFile(fileUrl: String, bucket: String): Boolean {
        return try {
            // Extract filename from URL
            val fileName = fileUrl.substringAfterLast("/")
            client.storage.from(bucket).delete(fileName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get Surat Masuk by ID (BARU)
    suspend fun getSuratMasukById(id: Int): SuratMasuk? {
        return try {
            val result = client.from("surat_masuk")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<SuratMasuk>()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get Surat Keluar by ID (BARU)
    suspend fun getSuratKeluarById(id: Int): SuratKeluar? {
        return try {
            val result = client.from("surat_keluar")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<SuratKeluar>()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkDuplicateNomorAgendaAll(nomorAgenda: String): Boolean {
        val masuk = checkDuplicateSuratMasukByNomorAgenda(nomorAgenda)
        val keluar = checkDuplicateSuratKeluarByNomorAgenda(nomorAgenda)
        return masuk || keluar
    }

}