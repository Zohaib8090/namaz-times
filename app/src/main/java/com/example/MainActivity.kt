package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.qibla.QiblaScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceGlass
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.KarachiNamazTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Namaz",
        activeIcon = Icons.Filled.AccessTime,
        inactiveIcon = Icons.Outlined.AccessTime
    )

    object Qibla : Screen(
        route = "qibla",
        title = "Qibla",
        activeIcon = Icons.Filled.CompassCalibration,
        inactiveIcon = Icons.Outlined.CompassCalibration
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        activeIcon = Icons.Filled.Settings,
        inactiveIcon = Icons.Outlined.Settings
    )
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KarachiNamazTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher for Location and Android 13+ Notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    val todayPrayers by viewModel.todayPrayers.collectAsStateWithLifecycle()
    val weekPrayers by viewModel.weekPrayers.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isRefreshingLocation by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val qiblaAngle by viewModel.qiblaAngle.collectAsStateWithLifecycle()
    val distanceToMakkah by viewModel.distanceToMakkah.collectAsStateWithLifecycle()

    val screens = listOf(Screen.Home, Screen.Qibla, Screen.Settings)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = DarkSurfaceGlass,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (selected) GoldAccent else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            indicatorColor = EmeraldContainer,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        todayPrayers = todayPrayers,
                        weekPrayers = weekPrayers,
                        countdown = countdown,
                        userPrefs = userPrefs,
                        isRefreshingLocation = isRefreshingLocation,
                        onRefreshLocation = {
                            viewModel.refreshLocation()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Detecting GPS location...")
                            }
                        },
                        onPrayerClick = { prayerType ->
                            viewModel.testPrayerNotification(prayerType)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Alarm active for ${prayerType.displayName} Namaz")
                            }
                        }
                    )
                }

                is Screen.Qibla -> {
                    val currentAzimuth by viewModel.compassAzimuth.collectAsStateWithLifecycle()
                    QiblaScreen(
                        currentAzimuth = currentAzimuth,
                        targetQiblaAngle = qiblaAngle,
                        distanceToMakkahKm = distanceToMakkah,
                        locationName = userPrefs.locationName
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        userPrefs = userPrefs,
                        onSetHanafi = { viewModel.setHanafi(it) },
                        onSetCalculationMethod = { viewModel.setCalculationMethod(it) },
                        onSetSoundEnabled = { viewModel.setSoundEnabled(it) },
                        onSetVibrationEnabled = { viewModel.setVibrationEnabled(it) },
                        onSet24Hour = { viewModel.set24Hour(it) },
                        onSetPrayerSound = { prayer, sound -> viewModel.setPrayerSound(prayer, sound) },
                        onSetAllPrayerSounds = { sound -> viewModel.setAllPrayerSounds(sound) },
                        onRefreshLocation = {
                            viewModel.refreshLocation()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Updating location from GPS...")
                            }
                        },
                        onResetKarachi = {
                            viewModel.resetToKarachi()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Reset location to Karachi (24.8607° N, 67.0011° E)")
                            }
                        },
                        onTestNotification = { prayerType ->
                            viewModel.testPrayerNotification(prayerType)
                            coroutineScope.launch {
                                val sound = userPrefs.getSoundForPrayer(prayerType.displayName)
                                snackbarHostState.showSnackbar("Test alert triggered for ${prayerType.displayName} (${sound.title})")
                            }
                        }
                    )
                }
            }
        }
    }
}
