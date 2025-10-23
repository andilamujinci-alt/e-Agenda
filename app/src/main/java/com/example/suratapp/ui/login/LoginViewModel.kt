// LoginViewModel.kt
package com.example.suratapp.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.suratapp.data.UserPreferences
import com.example.suratapp.data.models.LoginResponse
import com.example.suratapp.data.repository.SuratRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SuratRepository()
    private val userPreferences = UserPreferences(application)

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(nip: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.login(nip, password)
                _loginResult.value = result

                if (result.success && result.pegawai != null) {
                    // Simpan data user ke local storage
                    userPreferences.saveUser(
                        nip = result.pegawai.nip,
                        nama = result.pegawai.nama,
                        role = result.pegawai.role,
                        email = result.pegawai.email
                    )
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResponse(
                    success = false,
                    message = "Terjadi kesalahan: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkLoginStatus(): Flow<Boolean> {
        return userPreferences.userFlow.map { it != null }
    }

    suspend fun checkSupabaseSession(): Boolean {
        return repository.isLoggedIn()
    }
}