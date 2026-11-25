package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import com.example.data.repository.CalculatedPrayerDay
import com.example.data.repository.NextPrayerCountdown
import com.example.data.repository.PrayerTimeItem
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
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    todayPrayers: CalculatedPrayerDay?,
    weekPrayers: List<CalculatedPrayerDay>,
    countdown: NextPrayerCountdown?,
    userPrefs: UserPreferences,
    isRefreshingLocation: Boolean,
    onRefreshLocation: () -> Unit,
    onPrayerClick: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showWeekTable by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Location, Gregorian & Hijri Dates
        item {
            HeaderSection(
                todayPrayers = todayPrayers,
                locationName = userPrefs.locationName,
                isRefreshingLocation = isRefreshingLocation,
                onRefreshLocation = onRefreshLocation
            )
        }

        // 2. Next Prayer Countdown Hero Card
        item {
            CountdownHeroCard(
                countdown = countdown,
                todayPrayers = todayPrayers,
                is24Hour = userPrefs.is24Hour
            )
        }

        // 3. Section Title for 5 Daily Prayers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Namaz Timings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (userPrefs.isHanafi) "Hanafi Asr" else "Shafi'i Asr",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4. 5 Daily Prayer Cards
        if (todayPrayers != null) {
            val now = System.currentTimeMillis()
            val nextPrayerType = countdown?.nextPrayer?.type
            val currentPrayerType = countdown?.currentPrayer?.type

            items(todayPrayers.prayers, key = { it.type.name }) { item ->
                val isNext = item.type == nextPrayerType
                val isCurrent = item.type == currentPrayerType
                val isPassed = now > item.timeMillis && !isCurrent

                PrayerCard(
                    item = item,
                    todayDay = todayPrayers,
                    isNext = isNext,
                    isCurrent = isCurrent,
                    isPassed = isPassed,
                    is24Hour = userPrefs.is24Hour,
                    isHanafi = userPrefs.isHanafi,
                    onClick = { onPrayerClick(item.type) }
                )
            }
        }

        // 5. 7-Day Timetable Toggle Button
        item {
            WeeklyTableSection(
                weekPrayers = weekPrayers,
                todayKey = todayPrayers?.dateKey ?: "",
                is24Hour = userPrefs.is24Hour,
                isHanafi = userPrefs.isHanafi,
                isExpanded = showWeekTable,
                onToggleExpand = { showWeekTable = !showWeekTable }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeaderSection(
    todayPrayers: CalculatedPrayerDay?,
    locationName: String,
    isRefreshingLocation: Boolean,
    onRefreshLocation: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        val timeZoneCode = java.util.TimeZone.getTimeZone(DateUtils.displayZone).getDisplayName(false, java.util.TimeZone.SHORT)
                        Text(
                            text = "Offline Calculations • $timeZoneCode",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier.testTag("refresh_location_button")
                ) {
                    if (isRefreshingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EmeraldLight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS Location",
                            tint = GoldAccent
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = DarkSurfaceBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = todayPrayers?.gregorianFormatted ?: "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todayPrayers?.hijriFormatted ?: "Islamic Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldContainer,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldLight, GoldAccent)))
                ) {
                    Text(
                        text = "100% Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownHeroCard(
    countdown: NextPrayerCountdown?,
    todayPrayers: CalculatedPrayerDay?,
    is24Hour: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // A prayer is "current" while its time window is open (Fajr until sunrise, others until the next prayer)
    val current = countdown?.currentPrayer
    val next = countdown?.nextPrayer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(GoldAccent.copy(alpha = 0.6f), EmeraldPrimary.copy(alpha = 0.4f))
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x330F766E), Color(0x1A042F2E))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: label + Arabic name of the prayer shown in the big box
                val heroType = (current ?: next)?.type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .graphicsLayer { alpha = pulseAlphaState.value }
                                .clip(CircleShape)
                                .background(GoldAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (current != null) "CURRENT NAMAZ" else "UPCOMING NAMAZ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = heroType?.arabicName ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = heroType?.displayName ?: "Fajr",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                if (countdown != null) {
                    val subtitle = if (current != null) {
                        "${DateUtils.formatPrayerTime(countdown.currentStartMillis, is24Hour)} – " +
                            DateUtils.formatPrayerTime(countdown.currentEndMillis, is24Hour)
                    } else {
                        "at ${DateUtils.formatPrayerTime(countdown.nextPrayer.timeMillis, is24Hour)}"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big live countdown: time left in the current prayer, or time until the upcoming one
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkBackground.copy(alpha = 0.85f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldPrimary, GoldAccent))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (current != null) "TIME LEFT IN ${current.type.displayName.uppercase()}" else "STARTS IN",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (current != null) {
                                countdown?.currentRemainingFormatted ?: "00:00:00"
                            } else {
                                countdown?.remainingFormatted ?: "00:00:00"
                            },
                            modifier = Modifier.testTag("current_prayer_countdown"),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HOURS   :   MINUTES   :   SECONDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress: how much of the current prayer window has passed (or of the wait for the next one)
                LinearProgressIndicator(
                    progress = { if (current != null) (countdown?.currentProgress ?: 0f) else (countdown?.progress ?: 0.5f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GoldAccent,
                    trackColor = DarkSurfaceBorder
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Small section: the next prayer and when it starts
                if (countdown != null && next != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkBackground.copy(alpha = 0.6f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (current == null && countdown.sunriseMillis > 0L && next.type == PrayerType.DHUHR) {
                                Text(
                                    text = "Sunrise at ${DateUtils.formatPrayerTime(countdown.sunriseMillis, is24Hour)} • no Namaz time until Dhuhr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NEXT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${next.type.displayName} at ${DateUtils.formatPrayerTime(next.timeMillis, is24Hour)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "STARTS IN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = countdown.remainingFormatted,
                                        modifier = Modifier.testTag("next_prayer_countdown"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerCard(
    item: PrayerTimeItem,
    todayDay: CalculatedPrayerDay,
    isNext: Boolean,
    isCurrent: Boolean,
    isPassed: Boolean,
    is24Hour: Boolean,
    isHanafi: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isNext -> GoldAccent
        isCurrent -> EmeraldLight
        else -> DarkSurfaceBorder
    }

    val backgroundColor = when {
        isNext -> Color(0xFF1B3830)
        isCurrent -> Color(0xFF0F362F)
        isPassed -> Color(0xFF0A1B18)
        else -> DarkSurfaceGlass
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isNext || isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("prayer_card_${item.type.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Name + Arabic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isNext || isCurrent) EmeraldContainer else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Passed",
                            tint = EmeraldLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alarm Active",
                            tint = if (isNext) GoldAccent else EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPassed) TextSecondary else TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.type.arabicName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // Special detail for Asr Hanafi vs Shafi'i
                    if (item.type == PrayerType.ASR) {
                        val hanafiTime = DateUtils.formatPrayerTime(todayDay.asrMillis, is24Hour)
                        val shafiTime = DateUtils.formatPrayerTime(todayDay.asrShafiiMillis, is24Hour)
                        Text(
                            text = if (isHanafi) "Hanafi: $hanafiTime (Shafi'i: $shafiTime)" else "Shafi'i: $shafiTime (Hanafi: $hanafiTime)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldMuted,
                            fontSize = 11.sp
                        )
                    } else if (isNext) {
                        Text(
                            text = "Next scheduled Namaz",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                    } else if (isCurrent) {
                        Text(
                            text = "Current Namaz window",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight
                        )
                    }
                }
            }

            // Right: Time + Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatPrayerTime(item.timeMillis, is24Hour),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        isNext -> GoldLight
                        isCurrent -> EmeraldLight
                        isPassed -> TextMuted
                        else -> TextPrimary
                    }
                )

                if (isNext) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, GoldLight)))
                    ) {
                        Text(
                            text = "UPCOMING",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldContainer,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldLight, EmeraldLight)))
                    ) {
                        Text(
                            text = "NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTableSection(
    weekPrayers: List<CalculatedPrayerDay>,
    todayKey: String,
    is24Hour: Boolean,
    isHanafi: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGlass),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkSurfaceBorder, GoldBorder)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header button to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .testTag("toggle_weekly_table_button"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "7-Day Prayer Timetable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Calculated daily via University of Islamic Sciences, Karachi algorithm",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Day", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall, color = GoldAccent, fontWeight = FontWeight.Bold)
                        Text("Fajr", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Text("Dhuhr", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Text("Asr", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Text("Magh", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Text("Isha", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                    }

                    HorizontalDivider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 4.dp))

                    // 7 Rows
                    weekPrayers.forEach { day ->
                        val isToday = day.dateKey == todayKey
                        val activeAsr = if (isHanafi) day.asrMillis else day.asrShafiiMillis

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isToday) EmeraldContainer.copy(alpha = 0.5f) else Color.Transparent)
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isToday) "Today" else DateUtils.formatShortDate(day.date),
                                modifier = Modifier.weight(1.3f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) GoldLight else TextPrimary
                            )
                            Text(
                                text = DateUtils.formatPrayerTime(day.fajrMillis, is24Hour).replace(" AM", "").replace(" PM", ""),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isToday) TextPrimary else TextSecondary
                            )
                            Text(
                                text = DateUtils.formatPrayerTime(day.dhuhrMillis, is24Hour).replace(" AM", "").replace(" PM", ""),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isToday) TextPrimary else TextSecondary
                            )
                            Text(
                                text = DateUtils.formatPrayerTime(activeAsr, is24Hour).replace(" AM", "").replace(" PM", ""),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isToday) GoldAccent else TextSecondary
                            )
                            Text(
                                text = DateUtils.formatPrayerTime(day.maghribMillis, is24Hour).replace(" AM", "").replace(" PM", ""),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isToday) TextPrimary else TextSecondary
                            )
                            Text(
                                text = DateUtils.formatPrayerTime(day.ishaMillis, is24Hour).replace(" AM", "").replace(" PM", ""),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isToday) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
