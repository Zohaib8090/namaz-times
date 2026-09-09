package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.PrayerRepository
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import com.example.worker.DailyUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class KarachiNamazApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val prayerRepository by lazy { PrayerRepository(database.prayerDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()

        // 1. Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // 2. Schedule Daily Periodic WorkManager
        try {
            DailyUpdateWorker.enqueuePeriodicWork(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Pre-calculate and setup alarms for today
        applicationScope.launch {
            try {
                val prefs = userPreferencesRepository.userPreferencesFlow.first()
                val today = LocalDate.now()
                val todayPrayers = prayerRepository.getPrayerTimesForDate(
                    today,
                    prefs.latitude,
                    prefs.longitude,
                    prefs.isHanafi,
                    prefs.calculationMethod
                )
                val tomorrowPrayers = prayerRepository.getPrayerTimesForDate(
                    today.plusDays(1),
                    prefs.latitude,
                    prefs.longitude,
                    prefs.isHanafi,
                    prefs.calculationMethod
                )
                AlarmScheduler.scheduleDailyAlarms(this@KarachiNamazApp, todayPrayers, tomorrowPrayers)
                prayerRepository.cacheMonthPrayerTimes(today, prefs.latitude, prefs.longitude, prefs.calculationMethod)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
