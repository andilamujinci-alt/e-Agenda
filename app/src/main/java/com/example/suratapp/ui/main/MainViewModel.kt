package com.example.suratapp.ui.main

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
import com.example.suratapp.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SuratRepository()
    private val userPreferences = UserPreferences(application)

    private val _suratMasukList = MutableLiveData<List<SuratMasuk>>()
    val suratMasukList: LiveData<List<SuratMasuk>> = _suratMasukList

    private val _suratKeluarList = MutableLiveData<List<SuratKeluar>>()
    val suratKeluarList: LiveData<List<SuratKeluar>> = _suratKeluarList



    // MainViewModel.kt - Update loadData untuk count disposisi berdasarkan timestamp
    fun loadData() {
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayDb()

                // Load surat masuk
                val suratMasuk = repository.getAllSuratMasuk()
                _suratMasukList.value = suratMasuk

                // Count surat masuk hari ini

                // Load surat keluar
                val suratKeluar = repository.getAllSuratKeluar()
                _suratKeluarList.value = suratKeluar

            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun getUserInfo(): Flow<UserData?> {
        return userPreferences.userFlow
    }

    suspend fun logout() {
        repository.logout()
        userPreferences.clearUser()
    }

}