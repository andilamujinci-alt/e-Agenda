package com.example.suratapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.suratapp.R
import com.example.suratapp.databinding.ActivityMainBinding
import com.example.suratapp.ui.input.InputSuratActivity
import com.example.suratapp.ui.login.LoginActivity
import com.example.suratapp.utils.DateUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var userRole: String ="user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        getUserInfo()
        setupViewPager()
        setupFab()
        observeViewModel()

        viewModel.loadData()
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            viewModel.getUserInfo().collect { userData ->
                if (userData != null) {
                    userRole = userData.role
                    // Tampilkan nama pegawai
                    binding.tvWelcome.text = "Selamat Datang, ${userData.nama}"
                    binding.tvUserInfo.text = "NIP. ${userData.nip}"

                    // Hide FAB if user is not admin
                    if (userData.role != "admin") {
                        binding.fabAdd.hide()
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        val adapter = SuratPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Surat Masuk"
                1 -> "Surat Keluar"
                else -> ""
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            if (userRole == "admin") {
                showSuratTypeDialog()
            }
        }
    }

    private fun showSuratTypeDialog() {
        val items = arrayOf("Surat Masuk", "Surat Keluar")
        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Surat")
            .setItems(items) { dialog, which ->
                val intent = Intent(this, InputSuratActivity::class.java)
                intent.putExtra("TYPE", if (which == 0) "masuk" else "keluar")
                startActivity(intent)
            }
            .show()
    }

    // MainActivity.kt - Update observeViewModel
    private fun observeViewModel() {
        viewModel.suratMasukList.observe(this) { list ->
            val today = DateUtils.getTodayDb()
            val todayCount = list.count { it.tanggalDiterima == today }
            binding.tvSuratMasukCount.text = todayCount.toString()
        }

        viewModel.suratKeluarList.observe(this) { list ->
            val today = DateUtils.getTodayDb()
            val todayCount = list.count { it.tanggalDiterima == today }
            binding.tvSuratKeluarCount.text = todayCount.toString()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            viewModel.logout()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}