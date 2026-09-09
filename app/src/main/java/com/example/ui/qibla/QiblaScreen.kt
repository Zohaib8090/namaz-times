package com.example.ui.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    currentAzimuth: Float,
    targetQiblaAngle: Double,
    distanceToMakkahKm: Double,
    locationName: String,
    modifier: Modifier = Modifier
) {
    // Check if device is facing Qibla (within ±4 degrees)
    var angleDiff = abs(currentAzimuth - targetQiblaAngle.toFloat())
    if (angleDiff > 180f) angleDiff = 360f - angleDiff
    val isFacingQibla = angleDiff <= 4.0f

    // Animated dial rotation
    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(150, easing = LinearEasing),
        label = "azimuth_anim"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (isFacingQibla) GoldAccent else DarkSurfaceBorder,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "border_color_anim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title & Location
        Text(
            text = "Qibla Direction",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = EmeraldLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = locationName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Facing Qibla Status Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isFacingQibla) EmeraldContainer else DarkSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (isFacingQibla) listOf(GoldAccent, EmeraldLight) else listOf(DarkSurfaceBorder, DarkSurfaceBorder)
                )
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFacingQibla) Icons.Default.CheckCircle else Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (isFacingQibla) GoldLight else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFacingQibla) "Facing the Holy Kaaba (Qibla)!" else "Rotate your phone to align with Qibla",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isFacingQibla) FontWeight.Bold else FontWeight.Medium,
                    color = if (isFacingQibla) GoldLight else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Compass Visual Box
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(2.5.dp, dialBorderColor, CircleShape)
                .shadow(if (isFacingQibla) 16.dp else 4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Background Compass Ticks & Cardinal Directions (Rotates with azimuth)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = -animatedAzimuth }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 16.dp.toPx()

                // Draw outer ring
                drawCircle(
                    color = Color(0x332DD4BF),
                    radius = radius,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Draw tick marks every 30 degrees
                for (angle in 0 until 360 step 15) {
                    val angleRad = Math.toRadians((angle - 90).toDouble())
                    val isMajor = angle % 90 == 0
                    val isMedium = angle % 30 == 0
                    val tickLen = when {
                        isMajor -> 14.dp.toPx()
                        isMedium -> 9.dp.toPx()
                        else -> 5.dp.toPx()
                    }

                    val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
                    val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()

                    val tickColor = when {
                        isMajor -> GoldAccent
                        isMedium -> EmeraldLight
                        else -> Color(0x4499BDB5)
                    }

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.5f else 1.5f
                    )
                }

                // Draw Qibla marker on the compass ring
                val qiblaAngleRad = Math.toRadians((targetQiblaAngle - 90))
                val qiblaX = center.x + (radius - 22.dp.toPx()) * cos(qiblaAngleRad).toFloat()
                val qiblaY = center.y + (radius - 22.dp.toPx()) * sin(qiblaAngleRad).toFloat()

                drawCircle(
                    color = GoldAccent,
                    radius = 8.dp.toPx(),
                    center = Offset(qiblaX, qiblaY)
                )
            }

            // Fixed Cardinal labels overlay (so text is readable)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Text("N", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isFacingQibla) GoldLight else EmeraldLight, modifier = Modifier.padding(top = 8.dp))
            }

            // Center Qibla Needle (Points toward Qibla relative to device heading)
            val needleRotation = (targetQiblaAngle.toFloat() - animatedAzimuth + 360f) % 360f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = needleRotation },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // Top pointer (pointing to Kaaba) in gold
                    val pathTop = Path().apply {
                        moveTo(cx, 16.dp.toPx())
                        lineTo(cx - 10.dp.toPx(), cy)
                        lineTo(cx, cy - 4.dp.toPx())
                        lineTo(cx + 10.dp.toPx(), cy)
                        close()
                    }
                    drawPath(pathTop, color = if (isFacingQibla) GoldLight else GoldAccent)

                    // Bottom pointer in emerald/muted
                    val pathBottom = Path().apply {
                        moveTo(cx, h - 16.dp.toPx())
                        lineTo(cx - 8.dp.toPx(), cy)
                        lineTo(cx, cy + 4.dp.toPx())
                        lineTo(cx + 8.dp.toPx(), cy)
                        close()
                    }
                    drawPath(pathBottom, color = EmeraldPrimary)

                    // Center pivot circle
                    drawCircle(color = DarkBackground, radius = 12.dp.toPx(), center = Offset(cx, cy))
                    drawCircle(color = GoldAccent, radius = 6.dp.toPx(), center = Offset(cx, cy))
                }
            }

            // Small Kaaba Icon badge in the center
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, GoldAccent, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Kaaba Direction",
                    tint = GoldAccent,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(needleRotation)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Numerical Details Card
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
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Target Qibla Angle", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = String.format(Locale.ENGLISH, "%.1f° West", targetQiblaAngle),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "Karachi standard: 267.3° W",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Current Heading", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = String.format(Locale.ENGLISH, "%.0f°", currentAzimuth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isFacingQibla) EmeraldLight else TextPrimary
                        )
                        Text(
                            text = if (isFacingQibla) "Aligned with Qibla" else "Off by ${String.format(Locale.ENGLISH, "%.0f°", angleDiff)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFacingQibla) GoldAccent else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Distance to Makkah (Holy Kaaba)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.ENGLISH, "%,.0f km", distanceToMakkahKm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer
                    ) {
                        Text(
                            text = "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
