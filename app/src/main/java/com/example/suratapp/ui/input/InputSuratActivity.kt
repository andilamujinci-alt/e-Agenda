// InputSuratActivity.kt - Tambahkan validasi real-time
package com.example.suratapp.ui.input

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.suratapp.R
import com.example.suratapp.databinding.ActivityInputSuratBinding
import com.example.suratapp.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InputSuratActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputSuratBinding
    private val viewModel: InputSuratViewModel by viewModels()
    private var suratType: String = "masuk"
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var selectedFileType: String? = null
    private var photoFile: File? = null

    private var checkDuplicateJob: Job? = null
    private val mainScope = MainScope()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoFile?.let {
                selectedFileUri = Uri.fromFile(it)
                selectedFileName = it.name
                selectedFileType = "image"
                binding.tvFileName.text = "📷 ${it.name}"
                Toast.makeText(this, "Foto berhasil diambil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = getFileName(it)
            selectedFileType = "pdf"
            binding.tvFileName.text = "📄 $selectedFileName"
            Toast.makeText(this, "File PDF dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = getFileName(it)
            selectedFileType = "image"
            binding.tvFileName.text = "🖼️ $selectedFileName"
            Toast.makeText(this, "Gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputSuratBinding.inflate(layoutInflater)
        setContentView(binding.root)

        suratType = intent.getStringExtra("TYPE") ?: "masuk"

        setupToolbar()
        updateLabels()
        setupStatusSpinner()
        setupDatePickers()
        setupFileButtons()
        setupSaveButton()
        setupDuplicateValidation()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (suratType == "masuk") "Tambah Surat Masuk" else "Tambah Surat Keluar"

        // Update label berdasarkan tipe surat
        updateLabels()
    }

    private fun updateLabels() {
        if (suratType == "masuk") {
            binding.tilPengirim.hint = "Pengirim"
        } else {
            binding.tilPengirim.hint = "Penerima"
        }
    }

    private fun setupStatusSpinner() {
        val statusOptions = if (suratType == "masuk") {
            arrayOf("Sub Bagian Umum", "Sekretaris", "Kepala", "Ekonomi", "Sarpras", "Sosbud", "Litbang", "Program", "Keuangan")
        } else {
            arrayOf("Sub Bagian Umum", "Sekretaris", "Kepala", "Ekonomi", "Sarpras", "Sosbud", "Litbang", "Program", "Keuangan", "Penerima")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.spinnerStatus.adapter = adapter
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.etTanggalSurat.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    this@InputSuratActivity,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        // Tampilkan dalam format Indonesia
                        setText(DateUtils.formatToDisplay(DateUtils.fromDatePicker(year, month, day)))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        binding.etTanggalDiterima.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    this@InputSuratActivity,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        // Tampilkan dalam format Indonesia
                        setText(DateUtils.formatToDisplay(DateUtils.fromDatePicker(year, month, day)))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun setupDuplicateValidation() {
        // Real-time check untuk nomor surat
        binding.etNomorSurat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkDuplicateJob?.cancel()
                if (s.toString().isNotEmpty()) {
                    checkDuplicateJob = mainScope.launch {
                        delay(500) // Debounce 500ms
                        val nomorAgenda = binding.etNomorAgenda.text.toString()
                        if (nomorAgenda.isNotEmpty()) {
                            viewModel.checkDuplicate(suratType, s.toString(), nomorAgenda)
                        }
                    }
                } else {
                    binding.tilNomorSurat.error = null
                }
            }
        })

        // Real-time check untuk nomor agenda
        binding.etNomorAgenda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkDuplicateJob?.cancel()
                if (s.toString().isNotEmpty()) {
                    checkDuplicateJob = mainScope.launch {
                        delay(500) // Debounce 500ms
                        val nomorSurat = binding.etNomorSurat.text.toString()
                        if (nomorSurat.isNotEmpty()) {
                            viewModel.checkDuplicate(suratType, nomorSurat, s.toString())
                        }
                    }
                } else {
                    binding.tilNomorAgenda.error = null
                }
            }
        })
    }

    private fun setupFileButtons() {
        binding.btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnUploadFile.setOnClickListener {
            showFilePickerDialog()
        }
    }

    private fun showFilePickerDialog() {
        val options = arrayOf("Pilih Gambar", "Pilih PDF")
        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis File")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> imageLauncher.launch("image/*")
                    1 -> pdfLauncher.launch("application/pdf")
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        this.photoFile = photoFile

        val photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveData()
            }
        }
    }

    private fun validateInput(): Boolean {
        if (binding.etPengirim.text.isNullOrEmpty()) {
            binding.tilPengirim.error = "Pengirim harus diisi"
            return false
        }
        if (binding.etNomorSurat.text.isNullOrEmpty()) {
            binding.tilNomorSurat.error = "Nomor surat harus diisi"
            return false
        }
        if (binding.etTanggalSurat.text.isNullOrEmpty()) {
            binding.tilTanggalSurat.error = "Tanggal surat harus diisi"
            return false
        }
        if (binding.etNomorAgenda.text.isNullOrEmpty()) {
            binding.tilNomorAgenda.error = "Nomor agenda harus diisi"
            return false
        }
        if (binding.etTanggalDiterima.text.isNullOrEmpty()) {
            binding.tilTanggalDiterima.error = "Tanggal diterima harus diisi"
            return false
        }
        if (binding.etPerihal.text.isNullOrEmpty()) {
            binding.tilPerihal.error = "Perihal harus diisi"
            return false
        }

        // Check if there's duplicate error
        if (binding.tilNomorSurat.error != null || binding.tilNomorAgenda.error != null) {
            Toast.makeText(this, "Perbaiki error terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveData() {
        val pengirim = binding.etPengirim.text.toString()
        val nomorSurat = binding.etNomorSurat.text.toString()
        val tanggalSurat = DateUtils.formatToDatabase(binding.etTanggalSurat.text.toString())
        val nomorAgenda = binding.etNomorAgenda.text.toString()
        val tanggalDiterima = DateUtils.formatToDatabase(binding.etTanggalDiterima.text.toString())
        val perihal = binding.etPerihal.text.toString()
        val statusSurat = binding.spinnerStatus.selectedItem.toString()

        viewModel.saveSurat(
            type = suratType,
            pengirim = pengirim,
            nomorSurat = nomorSurat,
            tanggalSurat = tanggalSurat,
            nomorAgenda = nomorAgenda,
            tanggalDiterima = tanggalDiterima,
            perihal = perihal,
            statusSurat = statusSurat,
            fileUri = selectedFileUri,
            fileType = selectedFileType
        )
    }

    private fun observeViewModel() {
        // Observe duplicate check result
        viewModel.duplicateCheck.observe(this) { result ->
            if (result.error != null) {
                // Handle error
                return@observe
            }

            if (result.isDuplicateNomorSurat) {
                binding.tilNomorSurat.error = "⚠️ Nomor surat sudah terdaftar!"
            } else {
                binding.tilNomorSurat.error = null
            }

            if (result.isDuplicateNomorAgenda) {
                binding.tilNomorAgenda.error = "⚠️ Nomor agenda sudah terdaftar!"
            } else {
                binding.tilNomorAgenda.error = null
            }
        }

        // Observe save result
        viewModel.saveResult.observe(this) { result ->
            if (result.first) {
                Toast.makeText(this, "✅ Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "❌ ${result.second}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSave.text = if (isLoading) "Menyimpan..." else "Simpan"
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = "file.pdf"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
        return result
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        checkDuplicateJob?.cancel()
    }
}