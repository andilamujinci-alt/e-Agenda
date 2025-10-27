// FileUtils.kt
package com.example.suratapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Get file size dari URI
     * @return size in bytes
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * Format file size untuk display
     */
    fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Check apakah file size melebihi limit
     */
    fun isFileSizeValid(sizeInBytes: Long, maxSizeInBytes: Long = Constants.MAX_FILE_SIZE_BYTES): Boolean {
        return sizeInBytes <= maxSizeInBytes
    }

    /**
     * Compress image jika terlalu besar
     */
    fun compressImageIfNeeded(context: Context, uri: Uri, maxSizeInBytes: Long = Constants.MAX_FILE_SIZE_BYTES): File? {
        return try {
            val originalFile = uriToTempFile(context, uri)
            val originalSize = originalFile.length()

            // Jika ukuran sudah OK, langsung return
            if (originalSize <= maxSizeInBytes) {
                return originalFile
            }

            // Compress image
            val bitmap = BitmapFactory.decodeFile(originalFile.path)
            var quality = 90
            var compressedFile: File

            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                FileOutputStream(compressedFile).use { fos ->
                    fos.write(outputStream.toByteArray())
                }

                quality -= 10
            } while (compressedFile.length() > maxSizeInBytes && quality > 0)

            // Clean up original temp file
            originalFile.delete()

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert URI to temporary File
     */
    private fun uriToTempFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")

        FileOutputStream(tempFile).use { output ->
            inputStream?.copyTo(output)
        }

        inputStream?.close()
        return tempFile
    }

    /**
     * Get MIME type dari URI
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        }
    }

    /**
     * Check apakah file adalah gambar
     */
    fun isImageFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("image/") == true
    }

    /**
     * Check apakah file adalah PDF
     */
    fun isPdfFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType == "application/pdf"
    }
}