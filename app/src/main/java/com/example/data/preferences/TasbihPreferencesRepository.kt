package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.tasbih.AdhkarList
import com.example.ui.tasbih.DhikrItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tasbihDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasbih_preferences")

data class TasbihData(
    val count: Int = 0,
    val target: Int = 33,
    val lapCount: Int = 0,
    val lifetimeCount: Int = 0,
    val selectedDhikrId: String = "subhanallah",
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true
) {
    val selectedDhikr: DhikrItem
        get() = AdhkarList.getById(selectedDhikrId)

    val progress: Float
        get() = if (target > 0) (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
}

class TasbihPreferencesRepository(private val context: Context) {

    private object Keys {
        val COUNT = intPreferencesKey("count")
        val TARGET = intPreferencesKey("target")
        val LAP_COUNT = intPreferencesKey("lap_count")
        val LIFETIME_COUNT = intPreferencesKey("lifetime_count")
        val SELECTED_DHIKR_ID = stringPreferencesKey("selected_dhikr_id")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val tasbihFlow: Flow<TasbihData> = context.tasbihDataStore.data.map { prefs ->
        TasbihData(
            count = prefs[Keys.COUNT] ?: 0,
            target = prefs[Keys.TARGET] ?: 33,
            lapCount = prefs[Keys.LAP_COUNT] ?: 0,
            lifetimeCount = prefs[Keys.LIFETIME_COUNT] ?: 0,
            selectedDhikrId = prefs[Keys.SELECTED_DHIKR_ID] ?: "subhanallah",
            hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true
        )
    }

    suspend fun increment(): Boolean {
        var reachedTarget = false
        context.tasbihDataStore.edit { prefs ->
            val current = prefs[Keys.COUNT] ?: 0
            val target = prefs[Keys.TARGET] ?: 33
            val currentLaps = prefs[Keys.LAP_COUNT] ?: 0
            val currentLifetime = prefs[Keys.LIFETIME_COUNT] ?: 0

            val next = current + 1
            prefs[Keys.LIFETIME_COUNT] = currentLifetime + 1

            if (target > 0 && next >= target) {
                // Completed one full cycle/lap
                prefs[Keys.COUNT] = 0
                prefs[Keys.LAP_COUNT] = currentLaps + 1
                reachedTarget = true
            } else {
                prefs[Keys.COUNT] = next
            }
        }
        return reachedTarget
    }

    suspend fun decrement() {
        context.tasbihDataStore.edit { prefs ->
            val current = prefs[Keys.COUNT] ?: 0
            if (current > 0) {
                prefs[Keys.COUNT] = current - 1
                val currentLifetime = prefs[Keys.LIFETIME_COUNT] ?: 0
                if (currentLifetime > 0) {
                    prefs[Keys.LIFETIME_COUNT] = currentLifetime - 1
                }
            }
        }
    }

    suspend fun resetCurrent() {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.COUNT] = 0
        }
    }

    suspend fun resetAll() {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.COUNT] = 0
            prefs[Keys.LAP_COUNT] = 0
        }
    }

    suspend fun setTarget(target: Int) {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.TARGET] = target
        }
    }

    suspend fun selectDhikr(dhikrId: String, defaultTarget: Int) {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.SELECTED_DHIKR_ID] = dhikrId
            prefs[Keys.TARGET] = defaultTarget
            prefs[Keys.COUNT] = 0
        }
    }

    suspend fun setHaptic(enabled: Boolean) {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setSound(enabled: Boolean) {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }
}
