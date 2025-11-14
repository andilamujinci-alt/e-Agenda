// InputSuratActivity.kt - Updated with auto compress camera
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
import android.view.View
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
import com.example.suratapp.utils.Constants
import com.example.suratapp.utils.DateUtils
import com.example.suratapp.utils.FileUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoFile?.let { file ->
                    val fileSize = file.length()
                    val twoMB = 2 * 1024 * 1024 // 2MB dalam bytes

                    if (fileSize > twoMB) {
                        // Auto compress jika lebih dari 2MB
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvFileName.text = "Mengkompress foto..."

                        mainScope.launch {
                            try {
                                val compressedFile = FileUtils.compressImageIfNeeded(
                                    this@InputSuratActivity,
                                    Uri.fromFile(file)
                                )

                                if (compressedFile != null) {
                                    selectedFileUri = Uri.fromFile(compressedFile)
                                    selectedFileName = compressedFile.name
                                    selectedFileType = "image"

                                    val compressedSize = compressedFile.length()
                                    binding.tvFileName.text =
                                        "📷 ${compressedFile.name} (${FileUtils.formatFileSize(compressedSize)})"
                                    Toast.makeText(
                                        this@InputSuratActivity,
                                        "✅ Foto otomatis dikompress dari ${FileUtils.formatFileSize(fileSize)} ke ${FileUtils.formatFileSize(compressedSize)}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Delete file asli
                                    file.delete()
                                } else {
                                    binding.tvFileName.text = "Belum ada file dipilih"
                                    Toast.makeText(
                                        this@InputSuratActivity,
                                        "❌ Gagal mengkompress foto",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    file.delete()
                                }
                            } catch (e: Exception) {
                                binding.tvFileName.text = "Belum ada file dipilih"
                                Toast.makeText(
                                    this@InputSuratActivity,
                                    "❌ Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                file.delete()
                            } finally {
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    } else {
                        // Ukuran sudah dibawah 2MB, langsung gunakan
                        selectedFileUri = Uri.fromFile(file)
                        selectedFileName = file.name
                        selectedFileType = "image"

                        binding.tvFileName.text =
                            "📷 ${file.name} (${FileUtils.formatFileSize(fileSize)})"
                        Toast.makeText(this, "✅ Foto berhasil diambil", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private val pdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Validasi ukuran file
                val fileSize = FileUtils.getFileSize(this, it)

                if (!FileUtils.isFileSizeValid(fileSize)) {
                    val maxSizeMB = Constants.MAX_FILE_SIZE_MB
                    Toast.makeText(
                        this,
                        "❌ Ukuran file terlalu besar (${FileUtils.formatFileSize(fileSize)}). Maksimal $maxSizeMB MB",
                        Toast.LENGTH_LONG
                    ).show()
                    return@registerForActivityResult
                }

                selectedFileUri = it
                selectedFileName = getFileName(it)
                selectedFileType = "pdf"

                binding.tvFileName.text =
                    "📄 $selectedFileName (${FileUtils.formatFileSize(fileSize)})"
                Toast.makeText(this, "✅ File PDF dipilih", Toast.LENGTH_SHORT).show()
            }
        }

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Validasi ukuran file
                val fileSize = FileUtils.getFileSize(this, it)

                if (!FileUtils.isFileSizeValid(fileSize)) {
                    val maxSizeMB = Constants.MAX_FILE_SIZE_MB

                    // Tanya user apakah mau compress
                    AlertDialog.Builder(this)
                        .setTitle("File Terlalu Besar")
                        .setMessage("Ukuran file ${FileUtils.formatFileSize(fileSize)}, melebihi batas $maxSizeMB MB.\n\nApakah Anda ingin mengkompress gambar?")
                        .setPositiveButton("Compress") { dialog, _ ->
                            compressAndSelectImage(it)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()

                    return@registerForActivityResult
                }

                selectedFileUri = it
                selectedFileName = getFileName(it)
                selectedFileType = "image"

                binding.tvFileName.text =
                    "🖼️ $selectedFileName (${FileUtils.formatFileSize(fileSize)})"
                Toast.makeText(this, "✅ Gambar dipilih", Toast.LENGTH_SHORT).show()
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
        supportActionBar?.title =
            if (suratType == "masuk") "Tambah Surat Masuk" else "Tambah Surat Keluar"
    }

    private fun updateLabels() {
        if (suratType == "masuk") {
            binding.tilPengirim.hint = "Pengirim"
            binding.tilTanggalDiterima.hint = "Tanggal Diterima"
        } else {
            binding.tilPengirim.hint = "Penerima"
            binding.tilTanggalDiterima.hint = "Tanggal Dikirim"
        }
    }

    private fun setupStatusSpinner() {
        val statusOptions = if (suratType == "masuk") {
            arrayOf(
                "Sub Bagian Umum",
                "Sekretaris",
                "Kepala",
                "Ekonomi",
                "Sarpras",
                "Sosbud",
                "Litbang",
                "Program",
                "Keuangan"
            )
        } else {
            arrayOf(
                "Sub Bagian Umum",
                "Sekretaris",
                "Kepala",
                "Ekonomi",
                "Sarpras",
                "Sosbud",
                "Litbang",
                "Program",
                "Keuangan",
                "Penerima"
            )
        }

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
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
                        setText(
                            DateUtils.formatToDisplay(
                                DateUtils.fromDatePicker(
                                    year,
                                    month,
                                    day
                                )
                            )
                        )
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
                        setText(
                            DateUtils.formatToDisplay(
                                DateUtils.fromDatePicker(
                                    year,
                                    month,
                                    day
                                )
                            )
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun setupDuplicateValidation() {
        binding.etNomorAgenda.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                checkDuplicateJob?.cancel()
                if (!s.isNullOrEmpty()) {
                    checkDuplicateJob = mainScope.launch {
                        delay(400)
                        viewModel.checkDuplicateNomorAgenda(s.toString())
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
            .setTitle("Pilih Jenis File (Max ${Constants.MAX_FILE_SIZE_MB}MB)")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> imageLauncher.launch("image/*")
                    1 -> pdfLauncher.launch("application/pdf")
                }
            }
            .show()
    }

    private fun compressAndSelectImage(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvFileName.text = "Mengkompress gambar..."

        mainScope.launch {
            try {
                val compressedFile = FileUtils.compressImageIfNeeded(this@InputSuratActivity, uri)

                if (compressedFile != null) {
                    selectedFileUri = Uri.fromFile(compressedFile)
                    selectedFileName = compressedFile.name
                    selectedFileType = "image"

                    val fileSize = compressedFile.length()
                    binding.tvFileName.text =
                        "🖼️ ${compressedFile.name} (${FileUtils.formatFileSize(fileSize)})"
                    Toast.makeText(
                        this@InputSuratActivity,
                        "✅ Gambar berhasil dikompress",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.tvFileName.text = "Belum ada file dipilih"
                    Toast.makeText(
                        this@InputSuratActivity,
                        "❌ Gagal mengkompress gambar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.tvFileName.text = "Belum ada file dipilih"
                Toast.makeText(this@InputSuratActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
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
            val labelPengirim = if (suratType == "masuk") "Pengirim" else "Penerima"
            binding.tilPengirim.error = "$labelPengirim harus diisi"
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
            val labelTanggal = if (suratType == "masuk") "Tanggal diterima" else "Tanggal dikirim"
            binding.tilTanggalDiterima.error = "$labelTanggal harus diisi"
            return false
        }
        if (binding.etPerihal.text.isNullOrEmpty()) {
            binding.tilPerihal.error = "Perihal harus diisi"
            return false
        }

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
        viewModel.duplicateCheck.observe(this) { result ->
            if (result.error != null) return@observe
            binding.tilNomorAgenda.error = if (result.isDuplicateNomorAgenda) {
                "⚠️ Nomor agenda sudah terdaftar!"
            } else null
        }

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
        mainScope.cancel()
    }
}