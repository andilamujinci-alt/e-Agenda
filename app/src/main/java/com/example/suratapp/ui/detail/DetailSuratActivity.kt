// DetailSuratActivity.kt
package com.example.suratapp.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.suratapp.data.models.SuratKeluar
import com.example.suratapp.data.models.SuratMasuk
import com.example.suratapp.databinding.ActivityDetailSuratBinding
import com.example.suratapp.databinding.DialogSignatureBinding
import com.example.suratapp.utils.DateUtils
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class DetailSuratActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSuratBinding
    private val viewModel: DetailSuratViewModel by viewModels()
    private var suratType: String = "masuk"
    private var suratMasuk: SuratMasuk? = null
    private var suratKeluar: SuratKeluar? = null
    private var userRole: String = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSuratBinding.inflate(layoutInflater)
        setContentView(binding.root)

        suratType = intent.getStringExtra("TYPE") ?: "masuk"
        suratMasuk = intent.getParcelableExtra("SURAT_MASUK")
        suratKeluar = intent.getParcelableExtra("SURAT_KELUAR")

        setupToolbar()
        getUserRole()
        loadData()
        setupStatusSpinner()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (suratType == "masuk") "Detail Surat Masuk" else "Detail Surat Keluar"
    }

    private fun getUserRole() {
        lifecycleScope.launch {
            viewModel.getUserInfo().collect { userData ->
                if (userData != null) {
                    userRole = userData.role

                    // Hide edit buttons if user is not admin
                    if (userData.role != "admin") {
                        binding.btnUpdateStatus.visibility = View.GONE
                        binding.btnDisposisi.visibility = View.GONE
                        binding.btnDelete.visibility = View.GONE
                        binding.spinnerStatus.isEnabled = false
                    }
                }
            }
        }
    }

    private fun loadData() {
        if (suratType == "masuk" && suratMasuk != null) {
            binding.apply {
                // Update label
                tvLabelPengirim.text = "Pengirim"

                tvPengirim.text = suratMasuk?.pengirim
                tvNomorSurat.text = suratMasuk?.nomorSurat
                tvTanggalSurat.text = DateUtils.formatToDisplay(suratMasuk?.tanggalSurat)
                tvNomorAgenda.text = suratMasuk?.nomorAgenda
                tvTanggalDiterima.text = DateUtils.formatToDisplay(suratMasuk?.tanggalDiterima)
                tvPerihal.text = suratMasuk?.perihal

                // Show signature if exists
                if (!suratMasuk?.namaPenerima.isNullOrEmpty()) {
                    cardTandaTangan.visibility = View.VISIBLE
                    tvNamaPenerima.text = suratMasuk?.namaPenerima

                    // Tampilkan timestamp disposisi
                    if (!suratMasuk?.timestampDisposisi.isNullOrEmpty()) {
                        tvTimestampDisposisi.visibility = View.VISIBLE
                        tvTimestampDisposisi.text = DateUtils.formatTimestampToDisplay(suratMasuk?.timestampDisposisi)
                    } else {
                        tvTimestampDisposisi.visibility = View.GONE
                    }

                    suratMasuk?.tandaTangan?.let { base64 ->
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivTandaTangan.setImageBitmap(bitmap)
                    }
                } else {
                    cardTandaTangan.visibility = View.GONE
                }
            }
        } else if (suratType == "keluar" && suratKeluar != null) {
            binding.apply {
                // Update label untuk surat keluar
                tvLabelPengirim.text = "Penerima"

                tvPengirim.text = suratKeluar?.pengirim
                tvNomorSurat.text = suratKeluar?.nomorSurat
                tvTanggalSurat.text = DateUtils.formatToDisplay(suratKeluar?.tanggalSurat)
                tvNomorAgenda.text = suratKeluar?.nomorAgenda
                tvTanggalDiterima.text = DateUtils.formatToDisplay(suratKeluar?.tanggalDiterima)
                tvPerihal.text = suratKeluar?.perihal

                // Show signature if exists
                if (!suratKeluar?.namaPenerima.isNullOrEmpty()) {
                    cardTandaTangan.visibility = View.VISIBLE
                    tvNamaPenerima.text = suratKeluar?.namaPenerima

                    // Tampilkan timestamp disposisi
                    if (!suratKeluar?.timestampDisposisi.isNullOrEmpty()) {
                        tvTimestampDisposisi.visibility = View.VISIBLE
                        tvTimestampDisposisi.text = DateUtils.formatTimestampToDisplay(suratKeluar?.timestampDisposisi)
                    } else {
                        tvTimestampDisposisi.visibility = View.GONE
                    }

                    suratKeluar?.tandaTangan?.let { base64 ->
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivTandaTangan.setImageBitmap(bitmap)
                    }
                } else {
                    cardTandaTangan.visibility = View.GONE
                }
            }
        }
    }

    private fun setupStatusSpinner() {
        val statusOptions = if (suratType == "masuk") {
            arrayOf("Sub Bagian Umum", "Sekretaris", "Kepala", "Ekonomi", "Sarpras", "Sosbud", "Litbang", "Program", "Keuangan")
        } else {
            arrayOf("Ekonomi", "Sarpras", "Sosbud", "Litbang", "Program", "Keuangan", "Sekretaris", "Kepala", "Penerima")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.spinnerStatus.adapter = adapter

        // Set current status
        val currentStatus = if (suratType == "masuk") suratMasuk?.statusSurat else suratKeluar?.statusSurat
        val position = statusOptions.indexOf(currentStatus)
        if (position >= 0) {
            binding.spinnerStatus.setSelection(position)
        }
    }

    private fun setupButtons() {
        // Update text button disposisi sesuai tipe surat
        if (suratType == "masuk") {
            binding.btnDisposisi.text = "TTD Penerima Disposisi"
        } else {
            binding.btnDisposisi.text = "TTD Penerima Surat"
        }

        binding.btnUpdateStatus.setOnClickListener {
            if (userRole == "admin") {
                updateStatus()
            }
        }

        binding.btnDisposisi.setOnClickListener {
            if (userRole == "admin") {
                showSignatureDialog()
            }
        }

        binding.btnDelete.setOnClickListener {
            if (userRole == "admin") {
                showDeleteConfirmationDialog()
            }
        }

        binding.btnViewFile.setOnClickListener {
            val fileUrl = if (suratType == "masuk") suratMasuk?.fileUrl else suratKeluar?.fileUrl
            if (!fileUrl.isNullOrEmpty()) {
                openFile(fileUrl)
            } else {
                Toast.makeText(this, "File tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDownload.setOnClickListener {
            val fileUrl = if (suratType == "masuk") suratMasuk?.fileUrl else suratKeluar?.fileUrl
            if (!fileUrl.isNullOrEmpty()) {
                openFile(fileUrl)
                Toast.makeText(this, "Membuka file untuk diunduh", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "File tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatus() {
        val newStatus = binding.spinnerStatus.selectedItem.toString()

        if (suratType == "masuk" && suratMasuk != null) {
            val updatedSurat = suratMasuk!!.copy(statusSurat = newStatus)
            viewModel.updateSuratMasuk(suratMasuk!!.id!!, updatedSurat)
        } else if (suratType == "keluar" && suratKeluar != null) {
            val updatedSurat = suratKeluar!!.copy(statusSurat = newStatus)
            viewModel.updateSuratKeluar(suratKeluar!!.id!!, updatedSurat)
        }
    }

    private fun showSignatureDialog() {
        val dialogBinding = DialogSignatureBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Tanda Tangan Penerima")
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val nama = dialogBinding.etNamaPenerima.text.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dialogBinding.signaturePad.isEmpty) {
                    Toast.makeText(this, "Tanda tangan harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val signatureBitmap = dialogBinding.signaturePad.signatureBitmap
                val base64Signature = bitmapToBase64(signatureBitmap)

                saveDisposisi(nama, base64Signature)
                dialog.dismiss()
            }
        }

        dialogBinding.btnClear.setOnClickListener {
            dialogBinding.signaturePad.clear()
        }

        dialog.show()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveDisposisi(nama: String, tandaTangan: String) {
        val timestamp = DateUtils.getCurrentTimestamp()

        if (suratType == "masuk" && suratMasuk != null) {
            val updatedSurat = suratMasuk!!.copy(
                namaPenerima = nama,
                tandaTangan = tandaTangan,
                timestampDisposisi = timestamp
            )
            viewModel.updateSuratMasuk(suratMasuk!!.id!!, updatedSurat)
        } else if (suratType == "keluar" && suratKeluar != null) {
            val updatedSurat = suratKeluar!!.copy(
                namaPenerima = nama,
                tandaTangan = tandaTangan,
                timestampDisposisi = timestamp
            )
            viewModel.updateSuratKeluar(suratKeluar!!.id!!, updatedSurat)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Surat")
            .setMessage("Apakah Anda yakin ingin menghapus surat ini? Data yang dihapus tidak dapat dikembalikan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteSurat()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteSurat() {
        if (suratType == "masuk" && suratMasuk != null) {
            viewModel.deleteSuratMasuk(suratMasuk!!.id!!, suratMasuk!!.fileUrl)
        } else if (suratType == "keluar" && suratKeluar != null) {
            viewModel.deleteSuratKeluar(suratKeluar!!.id!!, suratKeluar!!.fileUrl)
        }
    }

    private fun openFile(fileUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(fileUrl), getMimeType(fileUrl))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: buka di browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            startActivity(browserIntent)
        }
    }

    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            url.endsWith(".png", ignoreCase = true) -> "image/png"
            else -> "*/*"
        }
    }

    // PERUBAHAN UTAMA DI SINI: Refresh data setelah update berhasil
    private fun observeViewModel() {
        viewModel.updateResult.observe(this) { result ->
            if (result.first) {
                Toast.makeText(this, "✅ Data berhasil diperbarui", Toast.LENGTH_SHORT).show()

                // Refresh data dari database
                refreshData()

                // HAPUS finish() agar tetap di activity ini
                // finish() // ← COMMENT ATAU HAPUS BARIS INI
            } else {
                Toast.makeText(this, "❌ Gagal memperbarui data: ${result.second}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe delete result - tetap finish karena data sudah dihapus
        viewModel.deleteResult.observe(this) { result ->
            if (result.first) {
                Toast.makeText(this, "✅ Surat berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke MainActivity karena data sudah dihapus
            } else {
                Toast.makeText(this, "❌ Gagal menghapus surat: ${result.second}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnUpdateStatus.isEnabled = !isLoading
            binding.btnDisposisi.isEnabled = !isLoading
            binding.btnDelete.isEnabled = !isLoading

            if (isLoading) {
                binding.btnUpdateStatus.text = "Menyimpan..."
                binding.btnDisposisi.text = "Menyimpan..."
                binding.btnDelete.text = "Menghapus..."
            } else {
                binding.btnUpdateStatus.text = "Update Status"
                binding.btnDisposisi.text = if (suratType == "masuk") "TTD Penerima Disposisi" else "TTD Penerima Surat"
                binding.btnDelete.text = "Hapus Surat"
            }
        }

        // Observe refreshed data (BARU)
        viewModel.refreshedSuratMasuk.observe(this) { surat ->
            surat?.let {
                suratMasuk = it
                loadData()
            }
        }

        viewModel.refreshedSuratKeluar.observe(this) { surat ->
            surat?.let {
                suratKeluar = it
                loadData()
            }
        }
    }

    // Fungsi baru untuk refresh data dari database
    private fun refreshData() {
        if (suratType == "masuk" && suratMasuk != null) {
            viewModel.refreshSuratMasuk(suratMasuk!!.id!!)
        } else if (suratType == "keluar" && suratKeluar != null) {
            viewModel.refreshSuratKeluar(suratKeluar!!.id!!)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}