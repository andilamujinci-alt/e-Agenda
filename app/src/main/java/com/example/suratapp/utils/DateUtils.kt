// DateUtils.kt - Tambahkan fungsi untuk timestamp
package com.example.suratapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    // Format untuk display (Indonesia)
    private val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))

    // Format untuk database (tetap yyyy-MM-dd untuk sorting)
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Format untuk display dengan nama bulan
    private val displayFormatLong = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    // Format untuk timestamp (ISO 8601 untuk database)
    private val timestampDbFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // Format untuk display timestamp (Indonesia)
    private val timestampDisplayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).apply {
        timeZone = TimeZone.getDefault() // Otomatis pakai timezone device
    }

    private val timestampParseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Convert dari database format (yyyy-MM-dd) ke display format (dd-MM-yyyy)
     */
    fun formatToDisplay(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val date = dbFormat.parse(dateString)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Convert dari display format (dd-MM-yyyy) ke database format (yyyy-MM-dd)
     */
    fun formatToDatabase(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val date = displayFormat.parse(dateString)
            dbFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }


    /**
     * Get timestamp saat ini dalam format database (ISO 8601)
     */
    fun getCurrentTimestamp(): String {
        val localTime = Date()
        return timestampDbFormat.format(localTime)
    }

    /**
     * Format timestamp dari database ke display format (15 Jan 2025, 14:30)
     */
    fun formatTimestampToDisplay(timestampString: String?): String {
        if (timestampString.isNullOrEmpty()) return ""

        return try {
            val date = parseTimestamp(timestampString)

            if (date != null) {
                timestampDisplayFormat.format(date)
            } else {
                timestampString
            }
        } catch (e: Exception) {
            e.printStackTrace()
            timestampString
        }
    }

    private fun parseTimestamp(timestampString: String): Date? {
        val formats = listOf(
            timestampParseFormat, // yyyy-MM-dd'T'HH:mm:ss
        )

        for (format in formats) {
            try {
                return format.parse(timestampString)
            } catch (e: Exception) {
                // Continue to next format
            }
        }

        return null
    }

    /**
     * Get tanggal hari ini dalam format database (yyyy-MM-dd)
     */
    fun getTodayDb(): String {
        return dbFormat.format(Date())
    }

    /**
     * Get tanggal hari ini dalam format display (dd-MM-yyyy)
     */
    fun getTodayDisplay(): String {
        return displayFormat.format(Date())
    }

    /**
     * Get tanggal minggu ini (awal minggu) dalam format database
     */
    fun getWeekStartDb(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return dbFormat.format(calendar.time)
    }

    /**
     * Get tanggal bulan ini (awal bulan) dalam format database
     */
    fun getMonthStartDb(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return dbFormat.format(calendar.time)
    }

    /**
     * Check apakah tanggal adalah hari ini
     */
    fun isToday(dateString: String?): Boolean {
        return dateString == getTodayDb()
    }

    /**
     * Parse date dari DatePicker dan return dalam format database
     */
    fun fromDatePicker(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return dbFormat.format(calendar.time)
    }
}