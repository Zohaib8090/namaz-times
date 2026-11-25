package com.example

import com.example.data.local.PrayerDao
import com.example.data.repository.CalculatedPrayerDay
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PrayerTimeItem
import com.example.data.repository.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDate

class PrayerWindowTest {

    private val repo = PrayerRepository(
        Proxy.newProxyInstance(
            PrayerDao::class.java.classLoader,
            arrayOf(PrayerDao::class.java)
        ) { _, _, _ -> null } as PrayerDao
    )

    private val h = 3_600_000L
    // Fajr 05:00, sunrise 06:30, Dhuhr 12:00, Asr 16:00, Maghrib 18:30, Isha 20:00 (hours from midnight)
    private val day = CalculatedPrayerDay(
        date = LocalDate.of(2026, 1, 1), dateKey = "2026-01-01",
        gregorianFormatted = "", hijriFormatted = "",
        fajrMillis = 5 * h, sunriseMillis = 6 * h + h / 2, dhuhrMillis = 12 * h,
        asrMillis = 16 * h, asrShafiiMillis = 15 * h, maghribMillis = 18 * h + h / 2, ishaMillis = 20 * h,
        prayers = listOf(
            PrayerTimeItem(PrayerType.FAJR, 5 * h),
            PrayerTimeItem(PrayerType.DHUHR, 12 * h),
            PrayerTimeItem(PrayerType.ASR, 16 * h),
            PrayerTimeItem(PrayerType.MAGHRIB, 18 * h + h / 2),
            PrayerTimeItem(PrayerType.ISHA, 20 * h)
        )
    )
    private val tomorrowFajr = 24 * h + 5 * h
    private val yesterdayIsha = -4 * h

    @Test
    fun fajrEndsAtSunrise() {
        val cd = repo.calculateCountdown(6 * h, day, tomorrowFajr, yesterdayIsha)
        assertEquals(PrayerType.FAJR, cd.currentPrayer?.type)
        assertEquals(30 * 60L, cd.currentRemainingSeconds)
        assertEquals(PrayerType.DHUHR, cd.nextPrayer.type)
    }

    @Test
    fun noCurrentPrayerBetweenSunriseAndDhuhr() {
        val cd = repo.calculateCountdown(9 * h, day, tomorrowFajr, yesterdayIsha)
        assertNull(cd.currentPrayer)
        assertEquals(3 * 3600L, cd.remainingSeconds)
    }

    @Test
    fun asrEndsWhenMaghribStarts() {
        val cd = repo.calculateCountdown(17 * h, day, tomorrowFajr, yesterdayIsha)
        assertEquals(PrayerType.ASR, cd.currentPrayer?.type)
        assertEquals(90 * 60L, cd.currentRemainingSeconds)
        assertEquals(PrayerType.MAGHRIB, cd.nextPrayer.type)
    }

    @Test
    fun ishaRunsUntilNextFajr() {
        val late = repo.calculateCountdown(22 * h, day, tomorrowFajr, yesterdayIsha)
        assertEquals(PrayerType.ISHA, late.currentPrayer?.type)
        assertEquals(7 * 3600L, late.currentRemainingSeconds)

        val early = repo.calculateCountdown(2 * h, day, tomorrowFajr, yesterdayIsha)
        assertEquals(PrayerType.ISHA, early.currentPrayer?.type)
        assertEquals(3 * 3600L, early.currentRemainingSeconds)
        assertEquals(PrayerType.FAJR, early.nextPrayer.type)
    }
}
