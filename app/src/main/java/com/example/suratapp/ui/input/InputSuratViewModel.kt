package com.example.suratapp.ui.input

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.suratapp.data.models.SuratKeluar
import com.example.suratapp.data.models.SuratMasuk
import com.example.suratapp.data.repository.SuratRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class InputSuratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SuratRepository()
    private val context = application

    private val _saveResult = MutableLiveData<Pair<Boolean, String?>>()
    val saveResult: LiveData<Pair<Boolean, String?>> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _duplicateCheck = MutableLiveData<DuplicateCheckResult>()
    val duplicateCheck: LiveData<DuplicateCheckResult> = _duplicateCheck

    // 🔍 Cek duplikat berdasarkan nomorAgenda saja, lintas surat masuk & keluar
    fun checkDuplicateNomorAgenda(nomorAgenda: String) {
        viewModelScope.launch {
            try {
                val isDuplicate = repository.checkDuplicateNomorAgendaAll(nomorAgenda)
                _duplicateCheck.value = DuplicateCheckResult(isDuplicateNomorAgenda = isDuplicate)
            } catch (e: Exception) {
                _duplicateCheck.value = DuplicateCheckResult(error = e.message)
            }
        }
    }

    fun saveSurat(
        type: String,
        pengirim: String,
        nomorSurat: String,
        tanggalSurat: String,
        nomorAgenda: String,
        tanggalDiterima: String,
        perihal: String,
        statusSurat: String,
        fileUri: Uri?,
        fileType: String? = null
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Cek duplikat nomorAgenda di semua tabel
                val isDuplicateNomorAgenda = repository.checkDuplicateNomorAgendaAll(nomorAgenda)
                if (isDuplicateNomorAgenda) {
                    _saveResult.value = Pair(false, "Nomor Agenda sudah terdaftar!")
                    _isLoading.value = false
                    return@launch
                }

                var fileUrl: String? = null
                if (fileUri != null) {
                    val file = uriToFile(fileUri, fileType)
                    fileUrl = repository.uploadFile(file, "surat_files")

                    if (fileUrl == null) {
                        _saveResult.value = Pair(false, "Gagal upload file")
                        _isLoading.value = false
                        return@launch
                    }
                }

                val result = if (type == "masuk") {
                    val surat = SuratMasuk(
                        pengirim = pengirim,
                        nomorSurat = nomorSurat,
                        tanggalSurat = tanggalSurat,
                        nomorAgenda = nomorAgenda,
                        tanggalDiterima = tanggalDiterima,
                        perihal = perihal,
                        statusSurat = statusSurat,
                        fileUrl = fileUrl
                    )
                    repository.insertSuratMasuk(surat)
                } else {
                    val surat = SuratKeluar(
                        pengirim = pengirim,
                        nomorSurat = nomorSurat,
                        tanggalSurat = tanggalSurat,
                        nomorAgenda = nomorAgenda,
                        tanggalDiterima = tanggalDiterima,
                        perihal = perihal,
                        statusSurat = statusSurat,
                        fileUrl = fileUrl
                    )
                    repository.insertSuratKeluar(surat)
                }

                _saveResult.value = if (result != null) {
                    Pair(true, null)
                } else {
                    Pair(false, "Gagal menyimpan data ke database")
                }
            } catch (e: Exception) {
                _saveResult.value = Pair(false, e.message ?: "Terjadi kesalahan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun uriToFile(uri: Uri, fileType: String?): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val extension = when (fileType) {
            "image" -> ".jpg"
            "pdf" -> ".pdf"
            else -> {
                val mimeType = context.contentResolver.getType(uri)
                when {
                    mimeType?.startsWith("image/") == true -> ".jpg"
                    mimeType == "application/pdf" -> ".pdf"
                    else -> ".dat"
                }
            }
        }

        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { output -> inputStream?.copyTo(output) }
        inputStream?.close()
        return file
    }
}

data class DuplicateCheckResult(
    val isDuplicateNomorAgenda: Boolean = false,
    val error: String? = null
)
