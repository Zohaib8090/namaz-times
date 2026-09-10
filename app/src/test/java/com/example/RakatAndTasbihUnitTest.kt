package com.example

import com.example.ui.rakats.RakatRepository
import com.example.ui.tasbih.AdhkarList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RakatAndTasbihUnitTest {

    @Test
    fun testDailyPrayersTotalRakats() {
        val daily = RakatRepository.dailyPrayers
        assertEquals(5, daily.size)

        val fajr = daily.find { it.id == "fajr" }
        assertNotNull(fajr)
        assertEquals(4, fajr!!.totalRakats)

        val dhuhr = daily.find { it.id == "dhuhr" }
        assertNotNull(dhuhr)
        assertEquals(12, dhuhr!!.totalRakats)

        val asr = daily.find { it.id == "asr" }
        assertNotNull(asr)
        assertEquals(8, asr!!.totalRakats)

        val maghrib = daily.find { it.id == "maghrib" }
        assertNotNull(maghrib)
        assertEquals(7, maghrib!!.totalRakats)

        val isha = daily.find { it.id == "isha" }
        assertNotNull(isha)
        assertEquals(17, isha!!.totalRakats)

        val totalDaily = daily.sumOf { it.totalRakats }
        assertEquals(48, totalDaily)
    }

    @Test
    fun testSpecialPrayersExist() {
        val special = RakatRepository.specialPrayers
        assertTrue(special.isNotEmpty())

        val jummah = special.find { it.id == "jummah" }
        assertNotNull(jummah)
        assertEquals(14, jummah!!.totalRakats)

        val tahajjud = special.find { it.id == "tahajjud" }
        assertNotNull(tahajjud)
    }

    @Test
    fun testPredefinedAdhkar() {
        val adhkar = AdhkarList.predefinedDhikr
        assertTrue(adhkar.size >= 5)

        val subhanAllah = AdhkarList.getById("subhanallah")
        assertEquals(33, subhanAllah.defaultTarget)
        assertEquals("SubhanAllah", subhanAllah.transliteration)

        val alhamdulillah = AdhkarList.getById("alhamdulillah")
        assertEquals(33, alhamdulillah.defaultTarget)

        val allahuAkbar = AdhkarList.getById("allahu_akbar")
        assertEquals(34, allahuAkbar.defaultTarget)
    }
}
