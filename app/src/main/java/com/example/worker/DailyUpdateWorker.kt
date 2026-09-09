package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.KarachiNamazApp
import com.example.util.AlarmScheduler
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DailyUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "DailyUpdateWorker"
        const val WORK_NAME = "karachi_namaz_daily_update_worker"

        fun enqueuePeriodicWork(context: Context) {
            val pktZone = try {
                ZoneId.of("Asia/Karachi")
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }

            val now = ZonedDateTime.now(pktZone)
            var targetTime = now.withHour(0).withMinute(5).withSecond(0).withNano(0)
            if (now.isAfter(targetTime)) {
                targetTime = targetTime.plusDays(1)
            }

            val initialDelayMinutes = Duration.between(now, targetTime).toMinutes().coerceAtLeast(1L)
            Log.d(TAG, "Scheduling DailyUpdateWorker: initial delay $initialDelayMinutes minutes to 00:05 AM PKT")

            val constraints = Constraints.Builder()
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DailyUpdateWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "DailyUpdateWorker executing daily recalculation and alarm reschedule...")
        return try {
            val app = applicationContext as? KarachiNamazApp ?: return Result.failure()
            val prefs = app.userPreferencesRepository.userPreferencesFlow.first()
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            val lat = prefs.latitude
            val lng = prefs.longitude
            val isHanafi = prefs.isHanafi
            val method = prefs.calculationMethod

            // Calculate and cache today and tomorrow
            val todayPrayers = app.prayerRepository.getPrayerTimesForDate(today, lat, lng, isHanafi, method)
            val tomorrowPrayers = app.prayerRepository.getPrayerTimesForDate(tomorrow, lat, lng, isHanafi, method)

            // Cache 30 days ahead into Room
            app.prayerRepository.cacheMonthPrayerTimes(today, lat, lng, method)

            // Cancel old pending intents & set 5 new exact alarms
            AlarmScheduler.cancelAllAlarms(applicationContext)
            AlarmScheduler.scheduleDailyAlarms(applicationContext, todayPrayers, tomorrowPrayers)

            Log.d(TAG, "DailyUpdateWorker completed successfully: 5 alarms scheduled for $today")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyUpdateWorker.doWork()", e)
            Result.retry()
        }
    }
}
