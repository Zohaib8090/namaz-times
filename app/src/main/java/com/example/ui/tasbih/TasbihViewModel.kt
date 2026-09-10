package com.example.ui.tasbih

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.TasbihData
import com.example.data.preferences.TasbihPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TasbihEvent {
    data class CycleCompleted(val completedDhikr: String, val target: Int) : TasbihEvent()
}

class TasbihViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TasbihPreferencesRepository(application)

    private val _tasbihState = MutableStateFlow(TasbihData())
    val tasbihState: StateFlow<TasbihData> = _tasbihState.asStateFlow()

    private val _events = MutableSharedFlow<TasbihEvent>()
    val events: SharedFlow<TasbihEvent> = _events.asSharedFlow()

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.tasbihFlow.collect { data ->
                _tasbihState.value = data
            }
        }
    }

    fun onCountTap() {
        val current = _tasbihState.value
        if (current.hapticEnabled) {
            triggerTapVibration()
        }
        if (current.soundEnabled) {
            playTapSound()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val reachedTarget = repository.increment()
            if (reachedTarget) {
                if (current.hapticEnabled) {
                    triggerTargetCelebrationVibration()
                }
                _events.emit(
                    TasbihEvent.CycleCompleted(
                        completedDhikr = current.selectedDhikr.transliteration,
                        target = current.target
                    )
                )
            }
        }
    }

    fun onDecrement() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.decrement()
        }
        if (_tasbihState.value.hapticEnabled) {
            triggerTapVibration()
        }
    }

    fun onResetCurrent() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetCurrent()
        }
    }

    fun onResetAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAll()
        }
    }

    fun onSelectTarget(target: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setTarget(target)
        }
    }

    fun onSelectDhikr(item: DhikrItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.selectDhikr(item.id, item.defaultTarget)
        }
    }

    fun onToggleHaptic() {
        val next = !_tasbihState.value.hapticEnabled
        viewModelScope.launch(Dispatchers.IO) {
            repository.setHaptic(next)
        }
    }

    fun onToggleSound() {
        val next = !_tasbihState.value.soundEnabled
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSound(next)
        }
    }

    private fun playTapSound() {
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.6f)
        } catch (_: Exception) {
            // Ignore if audio manager fails
        }
    }

    private fun triggerTapVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun triggerTargetCelebrationVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 80, 60, 100, 60, 150)
                    val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 80, 60, 100, 60, 150), -1)
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }
}
