package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import com.example.data.repository.PrayerType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceGlass
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AlarmScheduler
import com.example.util.NotificationSound
import com.example.util.SoundPreviewPlayer

@Composable
fun SettingsScreen(
    userPrefs: UserPreferences,
    onSetHanafi: (Boolean) -> Unit,
    onSetCalculationMethod: (String) -> Unit,
    onSetSoundEnabled: (Boolean) -> Unit,
    onSetVibrationEnabled: (Boolean) -> Unit,
    onSet24Hour: (Boolean) -> Unit,
    onSetPrayerSound: (String, NotificationSound) -> Unit,
    onSetAllPrayerSounds: (NotificationSound) -> Unit,
    onRefreshLocation: () -> Unit,
    onResetKarachi: () -> Unit,
    onTestNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMethodDropdownOpen by remember { mutableStateOf(false) }

    // Dialog state for selecting prayer sound: null = closed, "ALL" = all prayers, or "Fajr", "Dhuhr", etc.
    var prayerSoundTarget by remember { mutableStateOf<String?>(null) }
    var selectedTestPrayer by remember { mutableStateOf(PrayerType.FAJR) }

    // Clean up audio preview when navigating away
    DisposableEffect(Unit) {
        onDispose {
            SoundPreviewPlayer.stopSound()
        }
    }

    val calculationMethods = listOf(
        "KARACHI" to "University of Islamic Sciences, Karachi",
        "MUSLIM_WORLD_LEAGUE" to "Muslim World League (MWL)",
        "UMM_AL_QURA" to "Umm Al-Qura University, Makkah",
        "EGYPTIAN" to "Egyptian General Authority of Survey",
        "NORTH_AMERICA" to "ISNA (Islamic Society of North America)",
        "DUBAI" to "Dubai Awqaf"
    )

    val currentMethodLabel = calculationMethods.find { it.first.equals(userPrefs.calculationMethod, ignoreCase = true) }?.second
        ?: "University of Islamic Sciences, Karachi"

    val dailyPrayers = listOf(
        PrayerType.FAJR to userPrefs.getSoundForPrayer("Fajr"),
        PrayerType.DHUHR to userPrefs.getSoundForPrayer("Dhuhr"),
        PrayerType.ASR to userPrefs.getSoundForPrayer("Asr"),
        PrayerType.MAGHRIB to userPrefs.getSoundForPrayer("Maghrib"),
        PrayerType.ISHA to userPrefs.getSoundForPrayer("Isha")
    )

    // Sound Selection Modal Dialog
    prayerSoundTarget?.let { target ->
        val currentSound = if (target == "ALL") {
            userPrefs.getSoundForPrayer("Dhuhr")
        } else {
            userPrefs.getSoundForPrayer(target)
        }

        SoundSelectionDialog(
            prayerName = if (target == "ALL") "All Prayers" else "$target Namaz",
            currentSound = currentSound,
            onDismiss = {
                SoundPreviewPlayer.stopSound()
                prayerSoundTarget = null
            },
            onSelectSound = { sound ->
                SoundPreviewPlayer.stopSound()
                if (target == "ALL") {
                    onSetAllPrayerSounds(sound)
                } else {
                    onSetPrayerSound(target, sound)
                }
                prayerSoundTarget = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Customize prayer calculation, notification tones & location",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        // Section 1: Jurisprudence & Calculation Method
        item {
            SettingsCard(title = "Calculation & Madhab") {
                // Hanafi / Shafi'i Asr Toggle
                SettingsSwitchRow(
                    icon = Icons.Default.Schedule,
                    title = "Asr Calculation: Hanafi",
                    subtitle = if (userPrefs.isHanafi) "Hanafi: Shadow = 2x object length (Standard in Pakistan)" else "Shafi'i: Shadow = 1x object length",
                    checked = userPrefs.isHanafi,
                    onCheckedChange = { onSetHanafi(it) },
                    testTag = "toggle_hanafi"
                )

                HorizontalDivider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Calculation Method Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Calculation Authority",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentMethodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedButton(
                            onClick = { isMethodDropdownOpen = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_method_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change Method (${userPrefs.calculationMethod})")
                        }

                        DropdownMenu(
                            expanded = isMethodDropdownOpen,
                            onDismissRequest = { isMethodDropdownOpen = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            calculationMethods.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (key.equals(userPrefs.calculationMethod, ignoreCase = true)) GoldLight else TextPrimary,
                                            fontWeight = if (key.equals(userPrefs.calculationMethod, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSetCalculationMethod(key)
                                        isMethodDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Notifications & Reminders with Pre-bundled Sounds
        item {
            SettingsCard(title = "Azan & Notification Sounds") {
                // Master Sound Toggle
                SettingsSwitchRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Namaz Reminder Sound",
                    subtitle = "Play audio reminder at exact prayer time",
                    checked = userPrefs.soundEnabled,
                    onCheckedChange = { onSetSoundEnabled(it) },
                    testTag = "toggle_sound"
                )

                if (userPrefs.soundEnabled) {
                    HorizontalDivider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                    // Header for per-prayer sounds with "Apply to All" button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prayer Audio Tones",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Select distinct sounds for each namaz",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                prayerSoundTarget = "ALL"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GoldLight
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("apply_all_sounds_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = GoldLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply to All", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5 Daily Prayer Rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dailyPrayers.forEachIndexed { index, (prayer, sound) ->
                            val isPlaying = SoundPreviewPlayer.currentlyPlayingSoundId == sound.id

                            PrayerSoundRow(
                                prayer = prayer,
                                sound = sound,
                                isPlaying = isPlaying,
                                onTogglePlay = {
                                    SoundPreviewPlayer.playSound(context, sound)
                                },
                                onChangeSound = {
                                    prayerSoundTarget = prayer.displayName
                                }
                            )

                            if (index < dailyPrayers.size - 1) {
                                HorizontalDivider(color = DarkSurfaceBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Vibration Toggle
                SettingsSwitchRow(
                    icon = Icons.Default.Vibration,
                    title = "Vibration",
                    subtitle = "Vibrate phone when alarm triggers",
                    checked = userPrefs.vibrationEnabled,
                    onCheckedChange = { onSetVibrationEnabled(it) },
                    testTag = "toggle_vibration"
                )

                HorizontalDivider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Test Alarm Notification with prayer choice
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Reminder Notification",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hear the selected sound for ${selectedTestPrayer.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Button(
                            onClick = { onTestNotification(selectedTestPrayer) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldContainer, contentColor = EmeraldLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_notification_button")
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prayer Selection Filter Chips for Testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA).forEach { prayer ->
                            val isSelected = selectedTestPrayer == prayer
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTestPrayer = prayer },
                                label = { Text(prayer.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldContainer,
                                    selectedLabelColor = GoldLight,
                                    containerColor = DarkSurfaceElevated,
                                    labelColor = TextMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("test_prayer_chip_${prayer.displayName.lowercase()}")
                            )
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val canSchedule = AlarmScheduler.canScheduleExactAlarms(context)
                    if (!canSchedule) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33EF4444),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exact alarm permission required for precision reminders.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFCA5A5),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Text("Enable", color = GoldLight)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Time Format
        item {
            SettingsCard(title = "Display") {
                SettingsSwitchRow(
                    icon = Icons.Default.Alarm,
                    title = "24-Hour Time Format",
                    subtitle = if (userPrefs.is24Hour) "Showing e.g. 16:57" else "Showing e.g. 4:57 PM",
                    checked = userPrefs.is24Hour,
                    onCheckedChange = { onSet24Hour(it) },
                    testTag = "toggle_24_hour"
                )
            }
        }

        // Section 4: Location Settings
        item {
            SettingsCard(title = "Location") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userPrefs.locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${userPrefs.latitude.format(4)}° N, ${userPrefs.longitude.format(4)}° E",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRefreshLocation,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gps_detect_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Detect GPS")
                    }

                    OutlinedButton(
                        onClick = onResetKarachi,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_karachi_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Karachi Default")
                    }
                }
            }
        }

        // Section 5: Offline Notice & Reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceGlass),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Offline & Private",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Namaz Timing Reminder and Qibla Detector calculates all timings directly on your device using precise astronomical algorithms. No internet or data connection is required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGlass),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkSurfaceBorder, GoldBorder)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (checked) EmeraldContainer else DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) GoldLight else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = GoldLight,
                checkedTrackColor = EmeraldPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun PrayerSoundRow(
    prayer: PrayerType,
    sound: NotificationSound,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onChangeSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChangeSound() }
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag("sound_row_${prayer.displayName.lowercase()}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(EmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = GoldLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = prayer.arabicName,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sound.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Preview play/stop button
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("preview_button_${prayer.displayName.lowercase()}")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                    tint = if (isPlaying) Color(0xFFEF4444) else GoldLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedButton(
                onClick = onChangeSound,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("change_sound_${prayer.displayName.lowercase()}")
            ) {
                Text("Change", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SoundSelectionDialog(
    prayerName: String,
    currentSound: NotificationSound,
    onDismiss: () -> Unit,
    onSelectSound: (NotificationSound) -> Unit
) {
    val context = LocalContext.current
    var selectedSound by remember { mutableStateOf(currentSound) }

    AlertDialog(
        onDismissRequest = {
            SoundPreviewPlayer.stopSound()
            onDismiss()
        },
        containerColor = DarkSurfaceElevated,
        title = {
            Column {
                Text(
                    text = "Notification Sound",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = "Select sound for $prayerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(NotificationSound.entries.size) { index ->
                    val sound = NotificationSound.entries[index]
                    val isSelected = selectedSound == sound
                    val isPlaying = SoundPreviewPlayer.currentlyPlayingSoundId == sound.id

                    Surface(
                        onClick = {
                            selectedSound = sound
                            SoundPreviewPlayer.playSound(context, sound)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldContainer.copy(alpha = 0.5f) else DarkSurfaceGlass,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sound_option_${sound.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedSound = sound
                                    SoundPreviewPlayer.playSound(context, sound)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GoldAccent,
                                    unselectedColor = TextMuted
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sound.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) GoldLight else TextPrimary
                                )
                                Text(
                                    text = sound.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    SoundPreviewPlayer.playSound(context, sound)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop" else "Play",
                                    tint = if (isPlaying) Color(0xFFEF4444) else GoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundPreviewPlayer.stopSound()
                    onSelectSound(selectedSound)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldContainer, contentColor = EmeraldLight),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_sound_button")
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    SoundPreviewPlayer.stopSound()
                    onDismiss()
                }
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
