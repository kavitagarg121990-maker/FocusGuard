package com.focusguard.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.app.ui.theme.ProgressAmber
import com.focusguard.app.ui.theme.ProgressGreen
import com.focusguard.app.ui.theme.ProgressRed

@Composable
fun CircularProgressRing(
    spentMinutes: Long,
    goalMinutes: Long,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 18.dp
) {
    val ratio = if (goalMinutes > 0) (spentMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1.25f) else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "RingProgress"
    )

    val targetColor = when {
        animatedRatio < 0.70f -> ProgressGreen
        animatedRatio < 0.92f -> ProgressAmber
        else -> ProgressRed
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 600),
        label = "RingColor"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track Arc (270 degrees)
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Filled Progress Arc
            val sweep = (animatedRatio.coerceAtMost(1f) * 270f)
            if (sweep > 0f) {
                drawArc(
                    color = animatedColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content Readout
        val hours = spentMinutes / 60
        val mins = spentMinutes % 60
        val goalHours = goalMinutes / 60
        val goalMins = goalMinutes % 60

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${hours}h ${mins}m",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "of ${goalHours}h${if (goalMins > 0) " ${goalMins}m" else ""} daily limit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val percent = (ratio * 100).toInt()
            Text(
                text = "$percent% used",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = animatedColor
            )
        }
    }
}
