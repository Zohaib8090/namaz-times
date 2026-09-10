package com.example.ui.tasbih

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceGlass
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TasbihScreen(
    modifier: Modifier = Modifier,
    viewModel: TasbihViewModel = viewModel(),
    snackbarHostState: SnackbarHostState? = null
) {
    val state by viewModel.tasbihState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TasbihEvent.CycleCompleted -> {
                    snackbarHostState?.showSnackbar(
                        "Alhamdulillah! Completed ${event.target}x ${event.completedDhikr}"
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Tasbih Counter",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Would you like to reset the current count or reset both count and completed cycles?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onResetCurrent()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Reset Current Count")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.onResetAll()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset All Cycles", color = GoldAccent)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        TasbihHeader(
            isHapticEnabled = state.hapticEnabled,
            isSoundEnabled = state.soundEnabled,
            onToggleHaptic = { viewModel.onToggleHaptic() },
            onToggleSound = { viewModel.onToggleSound() },
            onResetClick = { showResetDialog = true }
        )

        // Adhkar Selection Carousel
        AdhkarSelectorRow(
            selectedId = state.selectedDhikrId,
            onSelect = { viewModel.onSelectDhikr(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Dhikr Display Card
        ActiveDhikrCard(item = state.selectedDhikr)

        Spacer(modifier = Modifier.height(20.dp))

        // Main Counter Dial Button
        InteractiveCounterDial(
            count = state.count,
            target = state.target,
            progress = state.progress,
            lapCount = state.lapCount,
            totalLifetime = state.lifetimeCount,
            onTap = { viewModel.onCountTap() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Target Selector Chips
        TargetSelectorRow(
            currentTarget = state.target,
            onSelectTarget = { viewModel.onSelectTarget(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Actions (Undo, Reset, Lap summary)
        CounterControlActions(
            count = state.count,
            lapCount = state.lapCount,
            totalLifetime = state.lifetimeCount,
            onDecrement = { viewModel.onDecrement() },
            onReset = { showResetDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Virtue Card
        if (state.selectedDhikr.virtue.isNotEmpty()) {
            VirtueCard(virtue = state.selectedDhikr.virtue)
        }
    }
}

@Composable
private fun TasbihHeader(
    isHapticEnabled: Boolean,
    isSoundEnabled: Boolean,
    onToggleHaptic: () -> Unit,
    onToggleSound: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurfaceElevated, DarkBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Tasbih Counter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Digital Subha & Dhikr Tracker",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Haptic feedback toggle
            IconButton(
                onClick = onToggleHaptic,
                modifier = Modifier.testTag("tasbih_toggle_haptic")
            ) {
                Icon(
                    imageVector = if (isHapticEnabled) Icons.Filled.Vibration else Icons.Outlined.Vibration,
                    contentDescription = "Haptic Feedback",
                    tint = if (isHapticEnabled) GoldAccent else TextMuted
                )
            }

            // Sound feedback toggle
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier.testTag("tasbih_toggle_sound")
            ) {
                Icon(
                    imageVector = if (isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                    contentDescription = "Sound Click",
                    tint = if (isSoundEnabled) EmeraldLight else TextMuted
                )
            }

            // Reset icon
            IconButton(
                onClick = onResetClick,
                modifier = Modifier.testTag("tasbih_button_reset_header")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset Counter",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AdhkarSelectorRow(
    selectedId: String,
    onSelect: (DhikrItem) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tasbih_adhkar_carousel"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AdhkarList.predefinedDhikr, key = { it.id }) { item ->
            val isSelected = item.id == selectedId
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item) },
                label = {
                    Text(
                        text = item.transliteration,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = DarkSurface,
                    selectedContainerColor = EmeraldContainer,
                    labelColor = TextSecondary,
                    selectedLabelColor = GoldAccent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = DarkSurfaceBorder,
                    selectedBorderColor = GoldBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("adhkar_chip_${item.id}")
            )
        }
    }
}

@Composable
private fun ActiveDhikrCard(item: DhikrItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("tasbih_active_dhikr_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, GoldBorder.copy(alpha = 0.5f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Arabic Text
            Text(
                text = item.arabic,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Transliteration
            Text(
                text = item.transliteration,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            // Meaning
            Text(
                text = item.translation,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InteractiveCounterDial(
    count: Int,
    target: Int,
    progress: Float,
    lapCount: Int,
    totalLifetime: Int,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "dial_scale"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = 300f),
        label = "dial_progress"
    )

    Box(
        modifier = Modifier
            .size(260.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(DarkSurface)
            .border(2.dp, DarkSurfaceBorder, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            )
            .testTag("tasbih_counter_dial"),
        contentAlignment = Alignment.Center
    ) {
        // Circular Progress Ring
        Canvas(modifier = Modifier.size(244.dp)) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

            // Background Track Ring
            drawCircle(
                color = Color(0xFF0F2620),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Foreground Progress Arc
            if (target > 0 && animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            EmeraldLight,
                            GoldAccent,
                            GoldLight,
                            EmeraldLight
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inner Circle Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Lap / Cycle indicator badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (lapCount > 0) "Cycle $lapCount" else "Cycle 1",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnEmeraldContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Big Count Number with animated transition
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                    }
                },
                label = "count_number"
            ) { displayedCount ->
                Text(
                    text = "$displayedCount",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )
            }

            // Target or Free Count indicator
            Text(
                text = if (target > 0) "of $target" else "Free Count",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tap hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "TAP TO COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TargetSelectorRow(
    currentTarget: Int,
    onSelectTarget: (Int) -> Unit
) {
    val targets = listOf(
        33 to "33",
        99 to "99",
        100 to "100",
        0 to "∞ Free"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        targets.forEach { (targetValue, label) ->
            val isSelected = currentTarget == targetValue
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) EmeraldContainer else DarkSurface)
                    .border(
                        1.dp,
                        if (isSelected) GoldAccent else DarkSurfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectTarget(targetValue) }
                    .padding(vertical = 10.dp)
                    .testTag("target_chip_$targetValue"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) GoldAccent else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CounterControlActions(
    count: Int,
    lapCount: Int,
    totalLifetime: Int,
    onDecrement: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minus 1 / Undo Button
        OutlinedButton(
            onClick = onDecrement,
            enabled = count > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary,
                disabledContentColor = TextMuted
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("tasbih_button_decrement")
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Minus 1",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Undo (-1)", fontSize = 12.sp)
        }

        // Lifetime total display badge
        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalLifetime",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = "Total Count",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Reset Button
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("tasbih_button_reset")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Reset",
                modifier = Modifier.size(16.dp),
                tint = GoldAccent
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", fontSize = 12.sp)
        }
    }
}

@Composable
private fun VirtueCard(virtue: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("tasbih_virtue_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = EmeraldLight,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Virtue & Benefit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = virtue,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
