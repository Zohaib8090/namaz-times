package com.example.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.R

/**
 * Pre-bundled local notification sounds available for prayer alerts.
 */
enum class NotificationSound(
    val id: String,
    val title: String,
    val subtitle: String,
    @RawRes val rawResId: Int?
) {
    AZAN_MAKKAH(
        id = "azan_makkah",
        title = "Makkah Azan Chime",
        subtitle = "Majestic melodic harmonic call",
        rawResId = R.raw.sound_azan_makkah
    ),
    AZAN_MADINAH(
        id = "azan_madinah",
        title = "Madinah Soft Tone",
        subtitle = "Serene and warm modal chime",
        rawResId = R.raw.sound_azan_madinah
    ),
    FAJR_DAWN(
        id = "fajr_dawn",
        title = "Fajr Dawn Awakening",
        subtitle = "Gentle uplifting morning motif",
        rawResId = R.raw.sound_fajr_dawn
    ),
    TAKBIR_CHIME(
        id = "takbir_chime",
        title = "Takbir Melody",
        subtitle = "Four-tone harmonic cadence",
        rawResId = R.raw.sound_takbir_chime
    ),
    SERENE_BELL(
        id = "serene_bell",
        title = "Serene Singing Bell",
        subtitle = "Soft acoustic resonant tone",
        rawResId = R.raw.sound_soft_bell
    ),
    SYSTEM_DEFAULT(
        id = "system_default",
        title = "Device Default Tone",
        subtitle = "Standard system notification sound",
        rawResId = null
    );

    fun getSoundUri(context: Context): Uri {
        return if (rawResId != null) {
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$rawResId")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    companion object {
        fun fromId(id: String?): NotificationSound {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: AZAN_MAKKAH
        }

        fun getDefaultSoundForPrayer(prayerName: String): NotificationSound {
            return when (prayerName.lowercase()) {
                "fajr" -> FAJR_DAWN
                "dhuhr" -> AZAN_MAKKAH
                "asr" -> AZAN_MADINAH
                "maghrib" -> AZAN_MAKKAH
                "isha" -> TAKBIR_CHIME
                else -> AZAN_MAKKAH
            }
        }
    }
}

/**
 * Singleton player to preview notification tones within the settings UI.
 */
object SoundPreviewPlayer {
    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingSoundId by mutableStateOf<String?>(null)
        private set

    fun playSound(context: Context, sound: NotificationSound) {
        if (currentlyPlayingSoundId == sound.id) {
            stopSound()
            return
        }

        stopSound()
        try {
            val uri = sound.getSoundUri(context)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener {
                    stopSound()
                }
                prepare()
                start()
            }
            currentlyPlayingSoundId = sound.id
        } catch (e: Exception) {
            e.printStackTrace()
            stopSound()
        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            currentlyPlayingSoundId = null
        }
    }
}
