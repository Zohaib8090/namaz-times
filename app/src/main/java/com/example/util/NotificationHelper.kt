package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.PrayerType

object NotificationHelper {

    fun getChannelId(prayerName: String, sound: NotificationSound = NotificationSound.AZAN_MAKKAH): String {
        return "channel_namaz_${prayerName.lowercase()}_${sound.id}"
    }

    fun createOrUpdatePrayerChannel(
        context: Context,
        prayerName: String,
        sound: NotificationSound = NotificationSound.AZAN_MAKKAH
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channelId = getChannelId(prayerName, sound)

            val soundUri = sound.getSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                channelId,
                "$prayerName Namaz (${sound.title})",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification reminder for $prayerName prayer with ${sound.title}"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val prayers = listOf(
                PrayerType.FAJR to NotificationSound.FAJR_DAWN,
                PrayerType.DHUHR to NotificationSound.AZAN_MAKKAH,
                PrayerType.ASR to NotificationSound.AZAN_MADINAH,
                PrayerType.MAGHRIB to NotificationSound.AZAN_MAKKAH,
                PrayerType.ISHA to NotificationSound.TAKBIR_CHIME
            )

            for ((prayer, defaultSound) in prayers) {
                createOrUpdatePrayerChannel(context, prayer.displayName, defaultSound)
            }
        }
    }

    fun showPrayerNotification(
        context: Context,
        prayerName: String,
        notificationId: Int,
        sound: NotificationSound = NotificationSound.AZAN_MAKKAH,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Ensure channel exists with current sound configuration
        createOrUpdatePrayerChannel(context, prayerName, sound)

        val channelId = getChannelId(prayerName, sound)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = sound.getSoundUri(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$prayerName time now")
            .setContentText("Time for $prayerName Namaz - حي على الصلاة (${sound.title})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (soundEnabled) {
            builder.setSound(soundUri)
        } else {
            builder.setSound(null)
        }

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 250, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
