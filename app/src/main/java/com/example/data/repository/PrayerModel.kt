package com.example.data.repository

import java.time.LocalDate

enum class PrayerType(val displayName: String, val arabicName: String, val id: Int) {
    FAJR("Fajr", "الفجر", 101),
    DHUHR("Dhuhr", "الظهر", 102),
    ASR("Asr", "العصر", 103),
    MAGHRIB("Maghrib", "المغرب", 104),
    ISHA("Isha", "العشاء", 105)
}

data class PrayerTimeItem(
    val type: PrayerType,
    val timeMillis: Long,
    val isHanafi: Boolean = true,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val isPassed: Boolean = false
)

data class CalculatedPrayerDay(
    val date: LocalDate,
    val dateKey: String, // YYYY-MM-DD
    val gregorianFormatted: String,
    val hijriFormatted: String,
    val fajrMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long,
    val asrShafiiMillis: Long,
    val maghribMillis: Long,
    val ishaMillis: Long,
    val prayers: List<PrayerTimeItem>
)

data class NextPrayerCountdown(
    val nextPrayer: PrayerTimeItem,
    val currentPrayer: PrayerTimeItem?,
    val remainingSeconds: Long,
    val remainingFormatted: String,
    val progress: Float
)
