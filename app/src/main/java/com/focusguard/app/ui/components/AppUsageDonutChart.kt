package com.focusguard.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.app.data.model.AppUsageInfo
import com.focusguard.app.ui.theme.AccentBlue
import com.focusguard.app.ui.theme.AccentOrange
import com.focusguard.app.ui.theme.AccentPurple
import com.focusguard.app.ui.theme.EmeraldPrimaryLight
import com.focusguard.app.ui.theme.ProgressAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppUsageDonutChart(
    appUsages: List<AppUsageInfo>,
    modifier: Modifier = Modifier
) {
    if (appUsages.isEmpty()) return

    val topApps = appUsages.take(5)
    val totalTime = topApps.sumOf { it.totalTimeInForegroundMillis }.coerceAtLeast(1L)
    val colors = listOf(
        EmeraldPrimaryLight,
        AccentBlue,
        AccentPurple,
        ProgressAmber,
        AccentOrange
    )

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "DonutAnim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokePx = 22.dp.toPx()
                val arcSize = size.width - strokePx
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                var currentAngle = -90f

                topApps.forEachIndexed { index, app ->
                    val sweep = (app.totalTimeInForegroundMillis.toFloat() / totalTime) * 360f * animatedProgress
                    val color = colors[index % colors.size]

                    drawArc(
                        color = color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                    )
                    currentAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Top Apps",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${topApps.size}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend items
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topApps.forEachIndexed { index, app ->
                val color = colors[index % colors.size]
                val percent = ((app.totalTimeInForegroundMillis.toFloat() / totalTime) * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${app.appName} ($percent%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
