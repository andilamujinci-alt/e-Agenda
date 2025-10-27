// DetailSuratViewModel.kt
package com.example.suratapp.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.suratapp.data.UserData
import com.example.suratapp.data.UserPreferences
import com.example.suratapp.data.models.SuratKeluar
import com.example.suratapp.data.models.SuratMasuk
import com.example.suratapp.data.repository.SuratRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DetailSuratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SuratRepository()
    private val userPreferences = UserPreferences(application)

    private val _updateResult = MutableLiveData<Pair<Boolean, String?>>()
    val updateResult: LiveData<Pair<Boolean, String?>> = _updateResult

    private val _deleteResult = MutableLiveData<Pair<Boolean, String?>>()
    val deleteResult: LiveData<Pair<Boolean, String?>> = _deleteResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk refreshed data (BARU)
    private val _refreshedSuratMasuk = MutableLiveData<SuratMasuk?>()
    val refreshedSuratMasuk: LiveData<SuratMasuk?> = _refreshedSuratMasuk

    private val _refreshedSuratKeluar = MutableLiveData<SuratKeluar?>()
    val refreshedSuratKeluar: LiveData<SuratKeluar?> = _refreshedSuratKeluar

    fun updateSuratMasuk(id: Int, surat: SuratMasuk) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateSuratMasuk(id, surat)
                if (result != null) {
                    _updateResult.value = Pair(true, null)
                } else {
                    _updateResult.value = Pair(false, "Gagal memperbarui data")
                }
            } catch (e: Exception) {
                _updateResult.value = Pair(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSuratKeluar(id: Int, surat: SuratKeluar) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateSuratKeluar(id, surat)
                if (result != null) {
                    _updateResult.value = Pair(true, null)
                } else {
                    _updateResult.value = Pair(false, "Gagal memperbarui data")
                }
            } catch (e: Exception) {
                _updateResult.value = Pair(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi baru untuk refresh data dari database (BARU)
    fun refreshSuratMasuk(id: Int) {
        viewModelScope.launch {
            try {
                val surat = repository.getSuratMasukById(id)
                _refreshedSuratMasuk.value = surat
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshSuratKeluar(id: Int) {
        viewModelScope.launch {
            try {
                val surat = repository.getSuratKeluarById(id)
                _refreshedSuratKeluar.value = surat
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSuratMasuk(id: Int, fileUrl: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Delete file dari storage jika ada
                if (!fileUrl.isNullOrEmpty()) {
                    repository.deleteFile(fileUrl, "surat_files")
                }

                // Delete data dari database
                val success = repository.deleteSuratMasuk(id)
                if (success) {
                    _deleteResult.value = Pair(true, null)
                } else {
                    _deleteResult.value = Pair(false, "Gagal menghapus data")
                }
            } catch (e: Exception) {
                _deleteResult.value = Pair(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSuratKeluar(id: Int, fileUrl: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Delete file dari storage jika ada
                if (!fileUrl.isNullOrEmpty()) {
                    repository.deleteFile(fileUrl, "surat_files")
                }

                // Delete data dari database
                val success = repository.deleteSuratKeluar(id)
                if (success) {
                    _deleteResult.value = Pair(true, null)
                } else {
                    _deleteResult.value = Pair(false, "Gagal menghapus data")
                }
            } catch (e: Exception) {
                _deleteResult.value = Pair(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserInfo(): Flow<UserData?> {
        return userPreferences.userFlow
    }
}