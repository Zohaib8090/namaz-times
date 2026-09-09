package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.example.util.NotificationSound

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val isHanafi: Boolean = true,
    val calculationMethod: String = "KARACHI",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val is24Hour: Boolean = false,
    val latitude: Double = 24.8607,
    val longitude: Double = 67.0011,
    val locationName: String = "Karachi, Pakistan",
    val autoLocation: Boolean = true,
    val fajrSound: String = NotificationSound.FAJR_DAWN.id,
    val dhuhrSound: String = NotificationSound.AZAN_MAKKAH.id,
    val asrSound: String = NotificationSound.AZAN_MADINAH.id,
    val maghribSound: String = NotificationSound.AZAN_MAKKAH.id,
    val ishaSound: String = NotificationSound.TAKBIR_CHIME.id
) {
    fun getSoundForPrayer(prayerName: String): NotificationSound {
        val soundId = when (prayerName.lowercase()) {
            "fajr" -> fajrSound
            "dhuhr" -> dhuhrSound
            "asr" -> asrSound
            "maghrib" -> maghribSound
            "isha" -> ishaSound
            else -> dhuhrSound
        }
        return NotificationSound.fromId(soundId)
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_HANAFI = booleanPreferencesKey("is_hanafi")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val AUTO_LOCATION = booleanPreferencesKey("auto_location")
        val FAJR_SOUND = stringPreferencesKey("fajr_sound")
        val DHUHR_SOUND = stringPreferencesKey("dhuhr_sound")
        val ASR_SOUND = stringPreferencesKey("asr_sound")
        val MAGHRIB_SOUND = stringPreferencesKey("maghrib_sound")
        val ISHA_SOUND = stringPreferencesKey("isha_sound")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            isHanafi = preferences[PreferencesKeys.IS_HANAFI] ?: true,
            calculationMethod = preferences[PreferencesKeys.CALCULATION_METHOD] ?: "KARACHI",
            soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            is24Hour = preferences[PreferencesKeys.IS_24_HOUR] ?: false,
            latitude = preferences[PreferencesKeys.LATITUDE] ?: 24.8607,
            longitude = preferences[PreferencesKeys.LONGITUDE] ?: 67.0011,
            locationName = preferences[PreferencesKeys.LOCATION_NAME] ?: "Karachi, Pakistan",
            autoLocation = preferences[PreferencesKeys.AUTO_LOCATION] ?: true,
            fajrSound = preferences[PreferencesKeys.FAJR_SOUND] ?: NotificationSound.FAJR_DAWN.id,
            dhuhrSound = preferences[PreferencesKeys.DHUHR_SOUND] ?: NotificationSound.AZAN_MAKKAH.id,
            asrSound = preferences[PreferencesKeys.ASR_SOUND] ?: NotificationSound.AZAN_MADINAH.id,
            maghribSound = preferences[PreferencesKeys.MAGHRIB_SOUND] ?: NotificationSound.AZAN_MAKKAH.id,
            ishaSound = preferences[PreferencesKeys.ISHA_SOUND] ?: NotificationSound.TAKBIR_CHIME.id
        )
    }

    suspend fun setPrayerSound(prayerName: String, soundId: String) {
        context.dataStore.edit { preferences ->
            when (prayerName.lowercase()) {
                "fajr" -> preferences[PreferencesKeys.FAJR_SOUND] = soundId
                "dhuhr" -> preferences[PreferencesKeys.DHUHR_SOUND] = soundId
                "asr" -> preferences[PreferencesKeys.ASR_SOUND] = soundId
                "maghrib" -> preferences[PreferencesKeys.MAGHRIB_SOUND] = soundId
                "isha" -> preferences[PreferencesKeys.ISHA_SOUND] = soundId
            }
        }
    }

    suspend fun setAllPrayerSounds(soundId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAJR_SOUND] = soundId
            preferences[PreferencesKeys.DHUHR_SOUND] = soundId
            preferences[PreferencesKeys.ASR_SOUND] = soundId
            preferences[PreferencesKeys.MAGHRIB_SOUND] = soundId
            preferences[PreferencesKeys.ISHA_SOUND] = soundId
        }
    }

    suspend fun setHanafi(isHanafi: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_HANAFI] = isHanafi
        }
    }

    suspend fun setCalculationMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALCULATION_METHOD] = method
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun set24Hour(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_24_HOUR] = is24Hour
        }
    }

    suspend fun setLocation(lat: Double, lng: Double, name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LATITUDE] = lat
            preferences[PreferencesKeys.LONGITUDE] = lng
            preferences[PreferencesKeys.LOCATION_NAME] = name
        }
    }

    suspend fun setAutoLocation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCATION] = enabled
        }
    }
}
