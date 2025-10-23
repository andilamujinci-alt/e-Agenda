// LoginActivity.kt
package com.example.suratapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.suratapp.databinding.ActivityLoginBinding
import com.example.suratapp.ui.main.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLoginStatus()
        setupListeners()
        observeViewModel()
    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            // Check local storage dan Supabase session
            val hasLocalData = viewModel.checkLoginStatus().first { it }
            val hasSupabaseSession = viewModel.checkSupabaseSession()

            if (hasLocalData && hasSupabaseSession) {
                navigateToMain()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val nip = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (nip.isEmpty()) {
                binding.tilUsername.error = "NIP harus diisi"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password harus diisi"
                return@setOnClickListener
            }

            // Clear errors
            binding.tilUsername.error = null
            binding.tilPassword.error = null

            viewModel.login(nip, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            if (result.success) {
                Toast.makeText(
                    this,
                    "Selamat datang, ${result.pegawai?.nama}",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMain()
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnLogin.text = if (isLoading) "Loading..." else "Login"
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}