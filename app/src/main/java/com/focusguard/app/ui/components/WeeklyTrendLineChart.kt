package com.focusguard.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WeeklyTrendLineChart(
    points: List<Float>, // Screen time values (e.g. minutes)
    modifier: Modifier = Modifier,
    heightDp: Int = 160
) {
    if (points.size < 2) return

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "LineChartAnim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(60f) * 1.15f
    val minVal = (points.minOrNull() ?: 0f) * 0.85f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val coordinates = points.mapIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minVal) / (maxVal - minVal)
            val y = height - (normalizedY * height * animatedProgress).coerceAtMost(height)
            Offset(x, y)
        }

        // Build smooth cubic Bézier path
        val path = Path().apply {
            moveTo(coordinates.first().x, coordinates.first().y)
            for (i in 0 until coordinates.size - 1) {
                val p0 = coordinates[i]
                val p1 = coordinates[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }
        }

        // Fill under the curve with translucent gradient
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.0f)
                )
            )
        )

        // Draw primary line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw data points circles
        coordinates.forEach { point ->
            drawCircle(
                color = MaterialTheme.colorScheme.surface,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = primaryColor,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}
