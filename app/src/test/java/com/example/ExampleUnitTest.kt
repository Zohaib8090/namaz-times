package com.example

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ExampleUnitTest {
  @Test
  fun testKarachiPrayerTimesReference_Sep9_2026() {
    val coordinates = Coordinates(24.8607, 67.0011)
    val dateComponents = DateComponents(2026, 9, 9)

    val paramsHanafi = CalculationMethod.KARACHI.parameters
    paramsHanafi.madhab = Madhab.HANAFI
    val prayersHanafi = PrayerTimes(coordinates, dateComponents, paramsHanafi)

    val paramsShafi = CalculationMethod.KARACHI.parameters
    paramsShafi.madhab = Madhab.SHAFI
    val prayersShafi = PrayerTimes(coordinates, dateComponents, paramsShafi)

    val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH).apply {
      timeZone = TimeZone.getTimeZone("Asia/Karachi")
    }

    val fajrStr = sdf.format(prayersHanafi.fajr)
    val dhuhrStr = sdf.format(prayersHanafi.dhuhr)
    val asrHanafiStr = sdf.format(prayersHanafi.asr)
    val asrShafiStr = sdf.format(prayersShafi.asr)
    val maghribStr = sdf.format(prayersHanafi.maghrib)
    val ishaStr = sdf.format(prayersHanafi.isha)

    println("Sep 9 2026 Karachi Times: Fajr=$fajrStr, Dhuhr=$dhuhrStr, AsrHanafi=$asrHanafiStr, AsrShafi=$asrShafiStr, Maghrib=$maghribStr, Isha=$ishaStr")

    // Verify times match expected Karachi calculations for Sep 9
    assertTrue(fajrStr.startsWith("4:5"))
    assertTrue(dhuhrStr.startsWith("12:2") || dhuhrStr.startsWith("12:3"))
    assertTrue(asrHanafiStr.startsWith("4:5"))
    assertTrue(asrShafiStr.startsWith("3:5"))
    assertTrue(maghribStr.startsWith("6:4"))
    assertTrue(ishaStr.startsWith("7:5") || ishaStr.startsWith("8:0"))

    // Test Qibla direction for Karachi
    val qibla = Qibla(coordinates)
    assertEquals(267.3, qibla.direction, 0.5)
  }

  @Test
  fun testNotificationSoundResolution() {
    val soundMakkah = com.example.util.NotificationSound.fromId("azan_makkah")
    assertEquals(com.example.util.NotificationSound.AZAN_MAKKAH, soundMakkah)

    val soundDawn = com.example.util.NotificationSound.fromId("fajr_dawn")
    assertEquals(com.example.util.NotificationSound.FAJR_DAWN, soundDawn)

    // Fallback on unknown sound ID
    val fallback = com.example.util.NotificationSound.fromId("non_existent_id")
    assertEquals(com.example.util.NotificationSound.AZAN_MAKKAH, fallback)

    // Test default sound per prayer
    assertEquals(com.example.util.NotificationSound.FAJR_DAWN, com.example.util.NotificationSound.getDefaultSoundForPrayer("Fajr"))
    assertEquals(com.example.util.NotificationSound.AZAN_MAKKAH, com.example.util.NotificationSound.getDefaultSoundForPrayer("Dhuhr"))
    assertEquals(com.example.util.NotificationSound.AZAN_MADINAH, com.example.util.NotificationSound.getDefaultSoundForPrayer("Asr"))
    assertEquals(com.example.util.NotificationSound.AZAN_MAKKAH, com.example.util.NotificationSound.getDefaultSoundForPrayer("Maghrib"))
    assertEquals(com.example.util.NotificationSound.TAKBIR_CHIME, com.example.util.NotificationSound.getDefaultSoundForPrayer("Isha"))
  }
}
