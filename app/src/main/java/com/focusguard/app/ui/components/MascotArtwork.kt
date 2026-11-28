package com.focusguard.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.app.ui.theme.GuardGold
import com.focusguard.app.ui.theme.GuardGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProBadgePill(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = GuardGold,
        modifier = modifier
    ) {
        Text(
            text = "PRO",
            color = Color(0xFF10120E),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/**
 * FocusGuard Mascot ("Guardy") — 3D-shaded cute lime-green bowl-headed study companion
 * with optional concentric dotted green halo rings.
 */
@Composable
fun GuardyMascotHead(
    modifier: Modifier = Modifier,
    showHalo: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MascotPulse")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val minDim = size.minDimension

        if (showHalo) {
            // Outer dotted green ring
            val outerR = minDim * 0.46f * haloScale
            val dotCount = 110
            for (i in 0 until dotCount) {
                val angle = (2.0 * PI * i) / dotCount
                val dx = cx + (outerR * cos(angle)).toFloat()
                val dy = cy + (outerR * sin(angle)).toFloat()
                drawCircle(
                    color = GuardGreen.copy(alpha = 0.78f),
                    radius = minDim * 0.006f,
                    center = Offset(dx, dy)
                )
            }
            // Inner thin dark-gray circle around avatar
            drawCircle(
                color = Color(0xFF2B302C),
                radius = minDim * 0.27f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.6.dp.toPx())
            )
        }

        // Dark circular backdrop inside avatar ring
        val faceScale = if (showHalo) 0.42f else 0.85f
        val r = (minDim / 2f) * faceScale

        drawCircle(
            color = Color(0xFF0A0D0A),
            radius = r * 1.15f,
            center = Offset(cx, cy)
        )

        // Subtle green shoulders at bottom
        drawArc(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF7BE82E), Color(0xFF3E9A13)),
                startY = cy + r * 0.45f,
                endY = cy + r * 1.1f
            ),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - r * 0.56f, cy + r * 0.50f),
            size = Size(r * 1.12f, r * 0.95f)
        )

        // Bowl-shaped glowing lime-green head (flat slightly curved top, rounded chin)
        val headPath = Path().apply {
            moveTo(cx - r * 0.82f, cy - r * 0.25f)
            quadraticTo(cx, cy - r * 0.34f, cx + r * 0.82f, cy - r * 0.25f)
            cubicTo(
                cx + r * 0.84f, cy + r * 0.42f,
                cx + r * 0.40f, cy + r * 0.78f,
                cx, cy + r * 0.78f
            )
            cubicTo(
                cx - r * 0.40f, cy + r * 0.78f,
                cx - r * 0.84f, cy + r * 0.42f,
                cx - r * 0.82f, cy - r * 0.25f
            )
            close()
        }
        drawPath(
            path = headPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC8FF56), Color(0xFF83E524), Color(0xFF4CA814)),
                center = Offset(cx, cy + r * 0.1f),
                radius = r * 1.15f
            )
        )

        // Inner top rim shadow for depth
        drawLine(
            color = Color(0xFF469A12).copy(alpha = 0.55f),
            start = Offset(cx - r * 0.72f, cy - r * 0.24f),
            end = Offset(cx + r * 0.72f, cy - r * 0.24f),
            strokeWidth = r * 0.05f,
            cap = StrokeCap.Round
        )

        // Rosy cheeks
        drawCircle(
            color = Color(0xFFFF7A85).copy(alpha = 0.55f),
            radius = r * 0.14f,
            center = Offset(cx - r * 0.48f, cy + r * 0.34f)
        )
        drawCircle(
            color = Color(0xFFFF7A85).copy(alpha = 0.55f),
            radius = r * 0.14f,
            center = Offset(cx + r * 0.48f, cy + r * 0.34f)
        )

        // Big expressive eyes
        val eyeY = cy + r * 0.14f
        val eyeOffsetX = r * 0.29f
        val eyeR = r * 0.21f
        listOf(-eyeOffsetX, eyeOffsetX).forEach { ox ->
            drawCircle(Color.White, radius = eyeR, center = Offset(cx + ox, eyeY))
            drawCircle(Color(0xFF141814), radius = eyeR * 0.70f, center = Offset(cx + ox, eyeY + eyeR * 0.05f))
            drawCircle(Color.White, radius = eyeR * 0.24f, center = Offset(cx + ox - eyeR * 0.22f, eyeY - eyeR * 0.22f))
        }

        // Eyebrows
        val browY = cy - r * 0.12f
        listOf(-1f, 1f).forEach { dir ->
            val startX = cx + dir * r * 0.17f
            val endX = cx + dir * r * 0.41f
            drawPath(
                path = Path().apply {
                    moveTo(startX, browY)
                    quadraticTo((startX + endX) / 2f, browY - r * 0.08f, endX, browY + r * 0.03f)
                },
                color = Color(0xFF20580A),
                style = Stroke(width = r * 0.055f, cap = StrokeCap.Round)
            )
        }

        // Cute gentle smile
        drawPath(
            path = Path().apply {
                moveTo(cx - r * 0.14f, cy + r * 0.43f)
                quadraticTo(cx, cy + r * 0.54f, cx + r * 0.14f, cy + r * 0.43f)
            },
            color = Color(0xFF1F540A),
            style = Stroke(width = r * 0.05f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Full-body Guardy mascot pointing proudly for Profile Pro Card.
 */
@Composable
fun GuardyMascotFull(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width * 0.55f
        val cy = size.height * 0.46f
        val r = size.minDimension * 0.28f

        // Legs
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF7BE82E), Color(0xFF439A14))),
            topLeft = Offset(cx - r * 0.42f, cy + r * 1.15f),
            size = Size(r * 0.34f, r * 0.52f),
            cornerRadius = CornerRadius(r * 0.16f)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF7BE82E), Color(0xFF439A14))),
            topLeft = Offset(cx + r * 0.08f, cy + r * 1.15f),
            size = Size(r * 0.34f, r * 0.52f),
            cornerRadius = CornerRadius(r * 0.16f)
        )

        // Torso
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFAEF848), Color(0xFF5DC41E)),
                center = Offset(cx, cy + r * 0.9f),
                radius = r
            ),
            topLeft = Offset(cx - r * 0.50f, cy + r * 0.45f),
            size = Size(r * 1.0f, r * 0.82f),
            cornerRadius = CornerRadius(r * 0.38f)
        )

        // Pointing arms (left)
        drawLine(
            color = Color(0xFF7CE82F),
            start = Offset(cx - r * 0.38f, cy + r * 0.72f),
            end = Offset(cx - r * 1.05f, cy + r * 0.62f),
            strokeWidth = r * 0.22f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF6CD626),
            start = Offset(cx + r * 0.25f, cy + r * 0.78f),
            end = Offset(cx - r * 0.45f, cy + r * 0.92f),
            strokeWidth = r * 0.22f,
            cap = StrokeCap.Round
        )

        // Head bowl
        val headPath = Path().apply {
            moveTo(cx - r * 0.85f, cy - r * 0.35f)
            quadraticTo(cx, cy - r * 0.44f, cx + r * 0.85f, cy - r * 0.35f)
            cubicTo(
                cx + r * 0.88f, cy + r * 0.35f,
                cx + r * 0.42f, cy + r * 0.65f,
                cx, cy + r * 0.65f
            )
            cubicTo(
                cx - r * 0.42f, cy + r * 0.65f,
                cx - r * 0.88f, cy + r * 0.35f,
                cx - r * 0.85f, cy - r * 0.35f
            )
            close()
        }
        drawPath(
            path = headPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC8FF56), Color(0xFF83E524), Color(0xFF4CA814)),
                center = Offset(cx, cy),
                radius = r * 1.15f
            )
        )

        // Eyes & smile
        val eyeY = cy + r * 0.03f
        listOf(-r * 0.28f, r * 0.28f).forEach { ox ->
            drawCircle(Color.White, radius = r * 0.19f, center = Offset(cx + ox, eyeY))
            drawCircle(Color(0xFF141814), radius = r * 0.13f, center = Offset(cx + ox - r * 0.03f, eyeY))
            drawCircle(Color.White, radius = r * 0.045f, center = Offset(cx + ox - r * 0.07f, eyeY - r * 0.04f))
        }
        drawCircle(Color(0xFFFF7A85).copy(alpha = 0.55f), radius = r * 0.12f, center = Offset(cx - r * 0.48f, cy + r * 0.24f))
        drawCircle(Color(0xFFFF7A85).copy(alpha = 0.55f), radius = r * 0.12f, center = Offset(cx + r * 0.48f, cy + r * 0.24f))
        drawPath(
            path = Path().apply {
                moveTo(cx - r * 0.14f, cy + r * 0.32f)
                quadraticTo(cx, cy + r * 0.42f, cx + r * 0.14f, cy + r * 0.32f)
            },
            color = Color(0xFF1F540A),
            style = Stroke(width = r * 0.055f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Animated particle mountain spiral representing "Your dream is a long climb".
 */
@Composable
fun DottedMountainClimbCanvas(
    modifier: Modifier = Modifier,
    showPeakFlag: Boolean = true,
    showClimber: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MountainGlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StarPulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val peakX = w * 0.58f
        val peakY = h * 0.18f
        val baseY = h * 0.94f

        // Mountain silhouette dots (left & right slopes + internal spiral path)
        val rows = 68
        for (i in 0..rows) {
            val t = i / rows.toFloat()
            val y = peakY + (baseY - peakY) * t
            val halfSpan = (w * 0.44f) * t
            val leftX = peakX - halfSpan * 1.12f
            val rightX = peakX + halfSpan * 0.85f

            val dotAlpha = 0.25f + 0.55f * (1f - t * 0.4f)
            drawCircle(
                color = Color.White.copy(alpha = dotAlpha * 0.5f),
                radius = 1.6.dp.toPx(),
                center = Offset(leftX, y)
            )
            drawCircle(
                color = Color.White.copy(alpha = dotAlpha * 0.5f),
                radius = 1.6.dp.toPx(),
                center = Offset(rightX, y)
            )
        }

        // Winding spiral trail up the mountain
        val trailSteps = 140
        for (i in 0..trailSteps) {
            val t = i / trailSteps.toFloat()
            val y = peakY + (baseY - peakY) * t
            val halfSpan = (w * 0.39f) * t
            val wave = sin(t * 7.5f * PI).toFloat()
            val x = peakX - (w * 0.05f * t) + wave * halfSpan
            val shimmer = 0.45f + 0.55f * sin((t + pulse) * 2f * PI).toFloat().coerceIn(0f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = (0.35f + 0.55f * shimmer).coerceIn(0.2f, 0.95f)),
                radius = 2.0.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Ambient floating stars/dots
        val ambientCount = 45
        for (i in 0 until ambientCount) {
            val frac = i / ambientCount.toFloat()
            val y = peakY + (baseY - peakY) * frac
            val span = (w * 0.35f) * frac
            val x = peakX + cos(i * 2.3f) * span * 0.9f
            drawCircle(
                color = Color.White.copy(alpha = 0.32f),
                radius = (1.2f + (i % 3) * 0.6f).dp.toPx(),
                center = Offset(x, y)
            )
        }

        if (showPeakFlag) {
            // Dotted flag at peak
            val fx = peakX
            val fy = peakY - 28.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(fx, peakY),
                end = Offset(fx, fy),
                strokeWidth = 2.dp.toPx()
            )
            for (r in 0..4) {
                for (c in 0..7) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 1.3.dp.toPx(),
                        center = Offset(fx + 3.dp.toPx() + c * 3.dp.toPx(), fy + r * 3.dp.toPx())
                    )
                }
            }
        }

        if (showClimber) {
            // Pixel / dotted character "You" near bottom
            val ux = w * 0.40f
            val uy = h * 0.79f
            val p = 2.4.dp.toPx()
            val pts = listOf(
                0f to -4f, -0.5f to -3f, 0.5f to -3f,
                -1f to -2f, 0f to -2f, 1f to -2f,
                -1f to -1f, 0f to -1f, 1f to -1f,
                0f to 0f, -0.7f to 1f, 0.7f to 1f,
                -0.8f to 2f, 0.8f to 2f
            )
            pts.forEach { (dx, dy) ->
                drawCircle(
                    color = Color.White,
                    radius = 1.9.dp.toPx(),
                    center = Offset(ux + dx * p, uy + dy * p)
                )
            }
        }
    }
}

/**
 * Green dotted cliff plateau with pixel "You" and floating distraction icons + optional padlocks.
 */
@Composable
fun DottedShieldPlateauCanvas(
    modifier: Modifier = Modifier,
    showPadlocks: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlateauAnim")
    val bob by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatBob"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Hourglass / plateau outline in glowing lime-green dots
        val topY = h * 0.42f
        val midY = h * 0.64f
        val botY = h * 0.88f
        val topW = w * 0.34f
        val midW = w * 0.18f
        val botW = w * 0.33f

        // Top edge
        for (i in 0..42) {
            val t = i / 42f
            val x = (cx - topW) + (2 * topW * t)
            drawCircle(GuardGreen, 1.8.dp.toPx(), Offset(x, topY))
        }
        // Bottom edge
        for (i in 0..42) {
            val t = i / 42f
            val x = (cx - botW) + (2 * botW * t)
            drawCircle(GuardGreen, 1.8.dp.toPx(), Offset(x, botY))
        }
        // Left & right concave walls
        for (i in 0..38) {
            val t = i / 38f
            val y = topY + (botY - topY) * t
            val pinch = sin(t * PI).toFloat()
            val span = topW - (topW - midW) * pinch
            drawCircle(GuardGreen, 1.8.dp.toPx(), Offset(cx - span, y))
            drawCircle(GuardGreen, 1.8.dp.toPx(), Offset(cx + span, y))
        }

        // Pixel "You" standing on top of the plateau
        val ux = cx
        val uy = topY - 12.dp.toPx()
        val p = 2.6.dp.toPx()
        val youPts = listOf(
            0f to -4f, -0.5f to -3f, 0.5f to -3f,
            -1f to -2f, 0f to -2f, 1f to -2f,
            -1f to -1f, 0f to -1f, 1f to -1f,
            0f to 0f, -0.7f to 1f, 0.7f to 1f,
            -0.8f to 2f, 0.8f to 2f
        )
        youPts.forEach { (dx, dy) ->
            drawCircle(GuardGreen, 2.0.dp.toPx(), Offset(ux + dx * p, uy + dy * p))
        }

        // Floating distraction app clusters around the plateau
        val clusters = listOf(
            Offset(w * 0.25f, h * 0.12f + bob),
            Offset(w * 0.70f, h * 0.14f - bob),
            Offset(w * 0.16f, h * 0.36f - bob),
            Offset(w * 0.84f, h * 0.45f + bob),
            Offset(w * 0.16f, h * 0.53f + bob),
            Offset(w * 0.16f, h * 0.72f - bob),
            Offset(w * 0.84f, h * 0.72f + bob)
        )

        clusters.forEach { center ->
            val badgeColor = if (showPadlocks) Color(0xFF7C8A7C) else Color(0xFFE5484D)
            for (r in -2..2) {
                for (c in -2..2) {
                    if (kotlin.math.abs(r) + kotlin.math.abs(c) <= 3) {
                        drawCircle(
                            color = badgeColor.copy(alpha = if (showPadlocks) 0.45f else 0.85f),
                            radius = 1.6.dp.toPx(),
                            center = Offset(center.x + c * 4.dp.toPx(), center.y + r * 4.dp.toPx())
                        )
                    }
                }
            }
            // Play triangle inside
            drawCircle(Color.White.copy(alpha = 0.8f), 2.2.dp.toPx(), center)

            if (showPadlocks) {
                // Dotted green padlock overlapping cluster
                val lx = center.x + 10.dp.toPx()
                val ly = center.y + 8.dp.toPx()
                drawArc(
                    color = GuardGreen,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(lx - 6.dp.toPx(), ly - 12.dp.toPx()),
                    size = Size(12.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawRoundRect(
                    color = GuardGreen,
                    topLeft = Offset(lx - 8.dp.toPx(), ly - 6.dp.toPx()),
                    size = Size(16.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
        }
    }
}

/**
 * Fullscreen Aurora Borealis & Starry Night background for Active Focus Session (Screenshot 19).
 */
@Composable
fun AuroraSessionBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraAnim")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuroraShift"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep night sky base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF04140E),
                    Color(0xFF093822),
                    Color(0xFF0C5B2A),
                    Color(0xFF082D1E),
                    Color(0xFF040D0A)
                )
            )
        )

        // Glowing Aurora Curtains
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF47FF5B).copy(alpha = 0.42f),
                    Color(0xFF12B954).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(w * (0.28f + 0.12f * shift), h * 0.48f),
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(w * (0.28f + 0.12f * shift), h * 0.48f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF28F0A8).copy(alpha = 0.30f),
                    Color(0xFF0B6E3A).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(w * (0.72f - 0.10f * shift), h * 0.34f),
                radius = w * 0.78f
            ),
            radius = w * 0.78f,
            center = Offset(w * (0.72f - 0.10f * shift), h * 0.34f)
        )

        // Subtle mountain horizon at bottom
        val horizonPath = Path().apply {
            moveTo(0f, h * 0.78f)
            lineTo(w * 0.25f, h * 0.73f)
            lineTo(w * 0.52f, h * 0.77f)
            lineTo(w * 0.78f, h * 0.71f)
            lineTo(w, h * 0.76f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = horizonPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0B2A20).copy(alpha = 0.85f), Color(0xFF040A08)),
                startY = h * 0.71f,
                endY = h
            )
        )

        // Starry night dots
        for (i in 0 until 75) {
            val sx = ((i * 137.5f) % 100f) / 100f * w
            val sy = ((i * 53.7f) % 72f) / 100f * h
            val starAlpha = if (i % 3 == 0) 0.35f + 0.5f * shift else 0.45f
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = (0.8f + (i % 3) * 0.45f).dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}
