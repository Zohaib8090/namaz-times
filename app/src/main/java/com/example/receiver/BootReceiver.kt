package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.KarachiNamazApp
import com.example.util.AlarmScheduler
import com.example.worker.DailyUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received broadcast action: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? KarachiNamazApp
                if (app != null) {
                    val prefs = app.userPreferencesRepository.userPreferencesFlow.first()
                    val today = LocalDate.now()
                    val lat = prefs.latitude
                    val lng = prefs.longitude
                    val isHanafi = prefs.isHanafi
                    val method = prefs.calculationMethod

                    val todayPrayers = app.prayerRepository.getPrayerTimesForDate(today, lat, lng, isHanafi, method)
                    val tomorrowPrayers = app.prayerRepository.getPrayerTimesForDate(today.plusDays(1), lat, lng, isHanafi, method)

                    AlarmScheduler.scheduleDailyAlarms(context, todayPrayers, tomorrowPrayers)
                    DailyUpdateWorker.enqueuePeriodicWork(context)
                    Log.d(TAG, "Successfully rescheduled alarms on boot/time change")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms on boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
