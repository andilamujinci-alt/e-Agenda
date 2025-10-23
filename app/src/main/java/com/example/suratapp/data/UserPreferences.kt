// UserPreferences.kt
package com.example.suratapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val NIP_KEY = stringPreferencesKey("nip")
        private val NAMA_KEY = stringPreferencesKey("nama")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    val userFlow: Flow<UserData?> = context.dataStore.data.map { preferences ->
        val nip = preferences[NIP_KEY]
        val nama = preferences[NAMA_KEY]
        val role = preferences[ROLE_KEY]
        val email = preferences[EMAIL_KEY]

        if (nip != null && nama != null && role != null) {
            UserData(nip, nama, role, email)
        } else {
            null
        }
    }

    suspend fun saveUser(nip: String, nama: String, role: String, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[NIP_KEY] = nip
            preferences[NAMA_KEY] = nama
            preferences[ROLE_KEY] = role
            if (email != null) {
                preferences[EMAIL_KEY] = email
            }
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class UserData(
    val nip: String,
    val nama: String,
    val role: String,
    val email: String?
)