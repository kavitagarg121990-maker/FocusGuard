package com.focusguard.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.app.ui.theme.EmeraldPrimaryLight
import com.focusguard.app.ui.theme.ProgressAmber
import com.focusguard.app.ui.theme.ProgressGreen

@Composable
fun UsageBarChart(
    dailyData: List<Pair<String, Float>>, // Day label to minutes
    goalMinutes: Float = 300f,
    modifier: Modifier = Modifier,
    heightDp: Int = 180
) {
    if (dailyData.isEmpty()) return

    val maxVal = (dailyData.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(goalMinutes * 1.15f)
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "BarChartAnimation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .padding(horizontal = 8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height - 30f // Leave space for bottom baseline
            val barCount = dailyData.size
            val barSlotWidth = canvasWidth / barCount
            val barWidth = (barSlotWidth * 0.45f).coerceAtMost(36f)

            // Draw Goal Reference Line
            val goalY = canvasHeight - (goalMinutes / maxVal) * canvasHeight
            drawLine(
                color = ProgressAmber.copy(alpha = 0.6f),
                start = Offset(0f, goalY),
                end = Offset(canvasWidth, goalY),
                strokeWidth = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            dailyData.forEachIndexed { index, pair ->
                val minutes = pair.second
                val barHeight = ((minutes / maxVal) * canvasHeight * animatedProgress).coerceAtLeast(6f)
                val x = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                val y = canvasHeight - barHeight

                // Draw background bar track
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.35f),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, canvasHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                // Draw filled bar
                val barColor = if (minutes > goalMinutes) ProgressAmber else primaryColor
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }

        // Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dailyData.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = labelColor
                )
            }
        }
    }
}
