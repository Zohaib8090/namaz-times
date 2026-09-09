package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.KarachiNamazApp
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "PrayerAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
        val prayerId = intent.getIntExtra(AlarmScheduler.EXTRA_PRAYER_ID, 100)

        Log.d(TAG, "Alarm received for $prayerName (ID: $prayerId)")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? KarachiNamazApp
                val prefsRepo = app?.userPreferencesRepository
                val userPrefs = prefsRepo?.userPreferencesFlow?.first()

                val soundEnabled = userPrefs?.soundEnabled ?: true
                val vibrationEnabled = userPrefs?.vibrationEnabled ?: true
                val prayerSound = userPrefs?.getSoundForPrayer(prayerName) ?: com.example.util.NotificationSound.getDefaultSoundForPrayer(prayerName)

                NotificationHelper.showPrayerNotification(
                    context = context,
                    prayerName = prayerName,
                    notificationId = prayerId,
                    sound = prayerSound,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled
                )

                // Re-sync / ensure future alarms are scheduled
                if (app != null) {
                    val today = java.time.LocalDate.now()
                    val lat = userPrefs?.latitude ?: 24.8607
                    val lng = userPrefs?.longitude ?: 67.0011
                    val isHanafi = userPrefs?.isHanafi ?: true
                    val method = userPrefs?.calculationMethod ?: "KARACHI"

                    val todayPrayers = app.prayerRepository.getPrayerTimesForDate(today, lat, lng, isHanafi, method)
                    val tomorrowPrayers = app.prayerRepository.getPrayerTimesForDate(today.plusDays(1), lat, lng, isHanafi, method)

                    AlarmScheduler.scheduleDailyAlarms(context, todayPrayers, tomorrowPrayers)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing prayer alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
