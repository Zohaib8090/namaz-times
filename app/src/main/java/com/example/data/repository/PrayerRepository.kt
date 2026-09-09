package com.example.data.repository

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.example.data.local.PrayerDao
import com.example.data.local.PrayerEntity
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PrayerRepository(
    private val prayerDao: PrayerDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getParametersForMethod(methodName: String, isHanafi: Boolean): CalculationParameters {
        val params = when (methodName.uppercase()) {
            "KARACHI" -> CalculationMethod.KARACHI.parameters
            "MUSLIM_WORLD_LEAGUE", "MWL" -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA.parameters
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN.parameters
            "NORTH_AMERICA", "ISNA" -> CalculationMethod.NORTH_AMERICA.parameters
            "DUBAI" -> CalculationMethod.DUBAI.parameters
            "MOON_SIGHTING" -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
            else -> CalculationMethod.KARACHI.parameters
        }
        params.madhab = if (isHanafi) Madhab.HANAFI else Madhab.SHAFI
        return params
    }

    suspend fun getPrayerTimesForDate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        isHanafi: Boolean,
        methodName: String = "KARACHI"
    ): CalculatedPrayerDay = withContext(Dispatchers.Default) {
        val dateKey = date.format(dateFormatter)
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)

        // Calculate Hanafi prayer times
        val hanafiParams = getParametersForMethod(methodName, isHanafi = true)
        val hanafiPrayers = PrayerTimes(coordinates, dateComponents, hanafiParams)

        // Calculate Shafi'i prayer times (to get Shafi'i Asr)
        val shafiParams = getParametersForMethod(methodName, isHanafi = false)
        val shafiPrayers = PrayerTimes(coordinates, dateComponents, shafiParams)

        val fajrMillis = hanafiPrayers.fajr.time
        val dhuhrMillis = hanafiPrayers.dhuhr.time
        val asrHanafiMillis = hanafiPrayers.asr.time
        val asrShafiMillis = shafiPrayers.asr.time
        val maghribMillis = hanafiPrayers.maghrib.time
        val ishaMillis = hanafiPrayers.isha.time

        // Cache in Room
        val entity = PrayerEntity(
            date = dateKey,
            fajrMillis = fajrMillis,
            dhuhrMillis = dhuhrMillis,
            asrMillis = asrHanafiMillis,
            asrShafiiMillis = asrShafiMillis,
            maghribMillis = maghribMillis,
            ishaMillis = ishaMillis,
            latitude = latitude,
            longitude = longitude,
            calculationMethod = methodName
        )
        withContext(Dispatchers.IO) {
            prayerDao.insertPrayer(entity)
        }

        buildCalculatedDay(date, dateKey, entity, isHanafi)
    }

    suspend fun getWeekPrayerTimes(
        startDate: LocalDate,
        latitude: Double,
        longitude: Double,
        isHanafi: Boolean,
        methodName: String = "KARACHI"
    ): List<CalculatedPrayerDay> = withContext(Dispatchers.Default) {
        val days = mutableListOf<CalculatedPrayerDay>()
        for (i in 0 until 7) {
            val date = startDate.plusDays(i.toLong())
            val prayerDay = getPrayerTimesForDate(date, latitude, longitude, isHanafi, methodName)
            days.add(prayerDay)
        }
        days
    }

    suspend fun cacheMonthPrayerTimes(
        startDate: LocalDate,
        latitude: Double,
        longitude: Double,
        methodName: String = "KARACHI"
    ) = withContext(Dispatchers.IO) {
        val entities = mutableListOf<PrayerEntity>()
        val coordinates = Coordinates(latitude, longitude)
        val hanafiParams = getParametersForMethod(methodName, isHanafi = true)
        val shafiParams = getParametersForMethod(methodName, isHanafi = false)

        for (i in 0 until 30) {
            val date = startDate.plusDays(i.toLong())
            val dateKey = date.format(dateFormatter)
            val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)

            val hanafiPrayers = PrayerTimes(coordinates, dateComponents, hanafiParams)
            val shafiPrayers = PrayerTimes(coordinates, dateComponents, shafiParams)

            entities.add(
                PrayerEntity(
                    date = dateKey,
                    fajrMillis = hanafiPrayers.fajr.time,
                    dhuhrMillis = hanafiPrayers.dhuhr.time,
                    asrMillis = hanafiPrayers.asr.time,
                    asrShafiiMillis = shafiPrayers.asr.time,
                    maghribMillis = hanafiPrayers.maghrib.time,
                    ishaMillis = hanafiPrayers.isha.time,
                    latitude = latitude,
                    longitude = longitude,
                    calculationMethod = methodName
                )
            )
        }
        prayerDao.insertPrayers(entities)
    }

    private fun buildCalculatedDay(
        date: LocalDate,
        dateKey: String,
        entity: PrayerEntity,
        isHanafi: Boolean
    ): CalculatedPrayerDay {
        val activeAsrMillis = if (isHanafi) entity.asrMillis else entity.asrShafiiMillis

        val items = listOf(
            PrayerTimeItem(PrayerType.FAJR, entity.fajrMillis),
            PrayerTimeItem(PrayerType.DHUHR, entity.dhuhrMillis),
            PrayerTimeItem(PrayerType.ASR, activeAsrMillis, isHanafi = isHanafi),
            PrayerTimeItem(PrayerType.MAGHRIB, entity.maghribMillis),
            PrayerTimeItem(PrayerType.ISHA, entity.ishaMillis)
        )

        return CalculatedPrayerDay(
            date = date,
            dateKey = dateKey,
            gregorianFormatted = DateUtils.formatGregorianDate(date),
            hijriFormatted = DateUtils.formatHijriDate(date),
            fajrMillis = entity.fajrMillis,
            dhuhrMillis = entity.dhuhrMillis,
            asrMillis = entity.asrMillis,
            asrShafiiMillis = entity.asrShafiiMillis,
            maghribMillis = entity.maghribMillis,
            ishaMillis = entity.ishaMillis,
            prayers = items
        )
    }

    fun calculateCountdown(
        currentTimeMillis: Long,
        todayPrayers: CalculatedPrayerDay,
        tomorrowFajrMillis: Long
    ): NextPrayerCountdown {
        val prayers = todayPrayers.prayers

        var currentPrayer: PrayerTimeItem? = null
        var nextPrayer: PrayerTimeItem? = null
        var previousPrayerMillis = 0L

        // Find current and next prayer
        for (i in prayers.indices) {
            val prayer = prayers[i]
            if (currentTimeMillis < prayer.timeMillis) {
                nextPrayer = prayer
                currentPrayer = if (i > 0) prayers[i - 1] else null
                previousPrayerMillis = if (i > 0) prayers[i - 1].timeMillis else todayPrayers.fajrMillis - (6 * 3600 * 1000L)
                break
            }
        }

        // If all prayers passed today, next is tomorrow's Fajr
        if (nextPrayer == null) {
            currentPrayer = prayers.last() // Isha
            nextPrayer = PrayerTimeItem(PrayerType.FAJR, tomorrowFajrMillis)
            previousPrayerMillis = prayers.last().timeMillis
        }

        val remainingMillis = (nextPrayer.timeMillis - currentTimeMillis).coerceAtLeast(0L)
        val remainingSeconds = remainingMillis / 1000L

        val totalInterval = (nextPrayer.timeMillis - previousPrayerMillis).coerceAtLeast(1L)
        val elapsed = (currentTimeMillis - previousPrayerMillis).coerceAtLeast(0L)
        val progress = (elapsed.toFloat() / totalInterval.toFloat()).coerceIn(0f, 1f)

        return NextPrayerCountdown(
            nextPrayer = nextPrayer,
            currentPrayer = currentPrayer,
            remainingSeconds = remainingSeconds,
            remainingFormatted = DateUtils.formatCountdown(remainingSeconds),
            progress = progress
        )
    }

    fun calculateQibla(latitude: Double, longitude: Double): Double {
        return try {
            val coordinates = Coordinates(latitude, longitude)
            val qibla = Qibla(coordinates)
            qibla.direction
        } catch (e: Exception) {
            // Karachi fallback: 267.3°
            267.3
        }
    }

    fun calculateDistanceToMakkah(lat: Double, lng: Double): Double {
        // Makkah Kaaba coordinates: 21.4225 N, 39.8262 E
        val makkahLat = 21.4225
        val makkahLng = 39.8262
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(makkahLat - lat)
        val dLng = Math.toRadians(makkahLng - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(makkahLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
