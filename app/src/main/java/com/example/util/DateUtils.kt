package com.example.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

object DateUtils {

    private val ISLAMIC_MONTHS = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun formatHijriDate(date: LocalDate): String {
        return try {
            val hijrahDate = HijrahDate.from(date)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(ChronoField.YEAR_OF_ERA)
            val monthName = if (month in 1..12) ISLAMIC_MONTHS[month - 1] else "Month $month"
            "$day $monthName $year AH"
        } catch (e: Exception) {
            "1448 AH"
        }
    }

    fun formatGregorianDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        return date.format(formatter)
    }

    fun formatShortDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
        return date.format(formatter)
    }

    fun formatPrayerTime(millis: Long, is24Hour: Boolean = false): String {
        val pattern = if (is24Hour) "HH:mm" else "h:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
        return sdf.format(Date(millis))
    }

    fun formatCountdown(seconds: Long): String {
        if (seconds <= 0) return "00:00:00"
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hrs, mins, secs)
    }

    fun toLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
