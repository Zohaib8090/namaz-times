package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.repository.CalculatedPrayerDay
import com.example.data.repository.PrayerType
import com.example.receiver.PrayerAlarmReceiver

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"
    const val EXTRA_PRAYER_TIME = "extra_prayer_time"

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun scheduleDailyAlarms(
        context: Context,
        todayPrayers: CalculatedPrayerDay,
        tomorrowPrayers: CalculatedPrayerDay? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        val prayersToSchedule = listOf(
            Triple(PrayerType.FAJR, todayPrayers.fajrMillis, tomorrowPrayers?.fajrMillis),
            Triple(PrayerType.DHUHR, todayPrayers.dhuhrMillis, tomorrowPrayers?.dhuhrMillis),
            Triple(
                PrayerType.ASR,
                todayPrayers.prayers.firstOrNull { it.type == PrayerType.ASR }?.timeMillis ?: todayPrayers.asrMillis,
                tomorrowPrayers?.prayers?.firstOrNull { it.type == PrayerType.ASR }?.timeMillis
            ),
            Triple(PrayerType.MAGHRIB, todayPrayers.maghribMillis, tomorrowPrayers?.maghribMillis),
            Triple(PrayerType.ISHA, todayPrayers.ishaMillis, tomorrowPrayers?.ishaMillis)
        )

        for ((type, todayMillis, tomorrowMillis) in prayersToSchedule) {
            // Determine target trigger time: if today's time hasn't passed, schedule today; else schedule tomorrow
            val targetMillis = if (todayMillis > now + 5000) {
                todayMillis
            } else if (tomorrowMillis != null && tomorrowMillis > now) {
                tomorrowMillis
            } else {
                // If tomorrow's calculated time not provided, add 24 hours approximately
                todayMillis + (24 * 60 * 60 * 1000L)
            }

            scheduleExactAlarm(context, alarmManager, type, targetMillis)
        }
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayerType: PrayerType,
        triggerAtMillis: Long
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerType.displayName)
            putExtra(EXTRA_PRAYER_ID, prayerType.id)
            putExtra(EXTRA_PRAYER_TIME, triggerAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerType.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Cancel previous alarm if any
            alarmManager.cancel(pendingIntent)

            val canScheduleExact = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
            } catch (e: Exception) {
                false
            }

            if (canScheduleExact) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                    Log.d(TAG, "Scheduled exact alarm for ${prayerType.displayName} at $triggerAtMillis")
                    return
                } catch (e: SecurityException) {
                    Log.w(TAG, "Exact alarm permission not granted, falling back to standard idle alarm")
                }
            }

            // Fallback for devices without exact alarm permission or Android M+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled alarm for ${prayerType.displayName} at $triggerAtMillis")
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled standard alarm for ${prayerType.displayName} at $triggerAtMillis")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to schedule alarm for ${prayerType.displayName}: ${e.message}")
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (type in PrayerType.values()) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                type.id,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
