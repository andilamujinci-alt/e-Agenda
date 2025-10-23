package com.example.suratapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object CameraUtils {

    fun compressImage(file: File, maxSize: Int = 1024): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val correctedBitmap = rotateImageIfRequired(bitmap, file.path)

        var quality = 100
        var streamLength: Int

        do {
            val bmpStream = FileOutputStream(file)
            correctedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bmpStream)
            bmpStream.flush()
            bmpStream.close()
            streamLength = file.length().toInt()
            quality -= 5
        } while (streamLength > maxSize * 1024 && quality > 0)

        return file
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, path: String): Bitmap {
        val ei = ExifInterface(path)
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}