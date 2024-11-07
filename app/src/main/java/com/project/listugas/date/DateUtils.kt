package com.project.listugas.date

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {

    // Format tanggal yang digunakan dalam aplikasi
    private const val DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"

    // Formatter untuk tanggal dalam database (string ke objek)
    private val dbFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DB_DATE_FORMAT)
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT)

    // Mengonversi tanggal dari string ke objek LocalDate
    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, dbFormatter)
    }

    // Mengonversi LocalDate ke string untuk ditampilkan
    fun formatDisplayDate(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    // Mendapatkan tanggal saat ini sebagai string
    fun getCurrentDate(): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault()).format(Date())
    }
}