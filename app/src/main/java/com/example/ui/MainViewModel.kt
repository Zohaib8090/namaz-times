package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KarachiNamazApp
import com.example.data.preferences.UserPreferences
import com.example.data.repository.CalculatedPrayerDay
import com.example.data.repository.NextPrayerCountdown
import com.example.data.repository.PrayerType
import com.example.ui.qibla.CompassSensorManager
import com.example.util.AlarmScheduler
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KarachiNamazApp
    private val prayerRepo = app.prayerRepository
    private val prefsRepo = app.userPreferencesRepository
    private val compassSensorManager = CompassSensorManager(application)

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _todayPrayers = MutableStateFlow<CalculatedPrayerDay?>(null)
    val todayPrayers: StateFlow<CalculatedPrayerDay?> = _todayPrayers.asStateFlow()

    private val _weekPrayers = MutableStateFlow<List<CalculatedPrayerDay>>(emptyList())
    val weekPrayers: StateFlow<List<CalculatedPrayerDay>> = _weekPrayers.asStateFlow()

    private val _countdown = MutableStateFlow<NextPrayerCountdown?>(null)
    val countdown: StateFlow<NextPrayerCountdown?> = _countdown.asStateFlow()

    private val _qiblaAngle = MutableStateFlow(267.3)
    val qiblaAngle: StateFlow<Double> = _qiblaAngle.asStateFlow()

    private val _distanceToMakkah = MutableStateFlow(3300.0)
    val distanceToMakkah: StateFlow<Double> = _distanceToMakkah.asStateFlow()

    val compassAzimuth: StateFlow<Float> = compassSensorManager.getAzimuthFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1500),
            initialValue = 0f
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var tickerJob: Job? = null
    private var tomorrowFajrMillis: Long = 0L

    init {
        // Observe preferences and recalculate on IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.userPreferencesFlow.collectLatest { prefs ->
                _userPreferences.value = prefs
                loadPrayerData(prefs)
            }
        }

        startLiveCountdownTicker()
    }

    private fun startLiveCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val currentToday = _todayPrayers.value
                if (currentToday != null) {
                    val now = System.currentTimeMillis()
                    val cd = prayerRepo.calculateCountdown(now, currentToday, tomorrowFajrMillis)
                    _countdown.value = cd
                }
                delay(1000)
            }
        }
    }

    private suspend fun loadPrayerData(prefs: UserPreferences) = withContext(Dispatchers.Default) {
        val today = LocalDate.now()
        val lat = prefs.latitude
        val lng = prefs.longitude
        val isHanafi = prefs.isHanafi
        val method = prefs.calculationMethod

        // Qibla calculations
        val qibla = prayerRepo.calculateQibla(lat, lng)
        val dist = prayerRepo.calculateDistanceToMakkah(lat, lng)
        _qiblaAngle.value = qibla
        _distanceToMakkah.value = dist

        // Calculate today
        val todayDay = prayerRepo.getPrayerTimesForDate(today, lat, lng, isHanafi, method)
        _todayPrayers.value = todayDay

        // Calculate tomorrow to get tomorrow's Fajr
        val tomorrowDay = prayerRepo.getPrayerTimesForDate(today.plusDays(1), lat, lng, isHanafi, method)
        tomorrowFajrMillis = tomorrowDay.fajrMillis

        // Calculate 7 days
        val weekList = prayerRepo.getWeekPrayerTimes(today, lat, lng, isHanafi, method)
        _weekPrayers.value = weekList

        // Trigger immediate countdown update
        val now = System.currentTimeMillis()
        _countdown.value = prayerRepo.calculateCountdown(now, todayDay, tomorrowFajrMillis)

        // Reschedule alarms off the main thread
        withContext(Dispatchers.IO) {
            AlarmScheduler.scheduleDailyAlarms(getApplication(), todayDay, tomorrowDay)
        }
    }

    fun refreshLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                val detected = LocationHelper.getCurrentLocation(getApplication())
                prefsRepo.setLocation(detected.latitude, detected.longitude, detected.name)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setHanafi(isHanafi: Boolean) {
        viewModelScope.launch {
            prefsRepo.setHanafi(isHanafi)
        }
    }

    fun setCalculationMethod(method: String) {
        viewModelScope.launch {
            prefsRepo.setCalculationMethod(method)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setVibrationEnabled(enabled)
        }
    }

    fun set24Hour(is24Hour: Boolean) {
        viewModelScope.launch {
            prefsRepo.set24Hour(is24Hour)
        }
    }

    fun setManualCoordinates(lat: Double, lng: Double, name: String) {
        viewModelScope.launch {
            prefsRepo.setLocation(lat, lng, name)
        }
    }

    fun resetToKarachi() {
        viewModelScope.launch {
            prefsRepo.setLocation(
                LocationHelper.KARACHI_DEFAULT.latitude,
                LocationHelper.KARACHI_DEFAULT.longitude,
                LocationHelper.KARACHI_DEFAULT.name
            )
        }
    }

    fun setPrayerSound(prayerName: String, sound: com.example.util.NotificationSound) {
        viewModelScope.launch {
            prefsRepo.setPrayerSound(prayerName, sound.id)
            NotificationHelper.createOrUpdatePrayerChannel(getApplication(), prayerName, sound)
        }
    }

    fun setAllPrayerSounds(sound: com.example.util.NotificationSound) {
        viewModelScope.launch {
            prefsRepo.setAllPrayerSounds(sound.id)
            listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { name ->
                NotificationHelper.createOrUpdatePrayerChannel(getApplication(), name, sound)
            }
        }
    }

    fun testPrayerNotification(prayerType: PrayerType, overrideSound: com.example.util.NotificationSound? = null) {
        val prefs = _userPreferences.value
        val sound = overrideSound ?: prefs.getSoundForPrayer(prayerType.displayName)
        NotificationHelper.showPrayerNotification(
            context = getApplication(),
            prayerName = prayerType.displayName,
            notificationId = prayerType.id,
            sound = sound,
            soundEnabled = prefs.soundEnabled,
            vibrationEnabled = prefs.vibrationEnabled
        )
    }

    fun isFacingQibla(currentAzimuth: Float, targetQibla: Double): Boolean {
        var diff = abs(currentAzimuth - targetQibla.toFloat())
        if (diff > 180f) diff = 360f - diff
        return diff <= 4.0f
    }
}
