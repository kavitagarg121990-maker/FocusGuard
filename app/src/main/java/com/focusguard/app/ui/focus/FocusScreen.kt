package com.focusguard.app.ui.focus

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAccessibility()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val primaryGreen = Color(0xFF00E676)
    val cardBg = Color(0xFF131F1B)
    val cardBorder = Color(0xFF1E362E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E0C))
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Header (Hourglass icon, "Focus", "X app blocked")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF15382B),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "${uiState.fullyBlockedCount} app blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B9E99)
                )
            }
        }

        // 2. Warning Card if Accessibility / App Guard is OFF
        if (!uiState.isAccessibilityEnabled) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C0E)),
                border = BorderStroke(1.dp, Color(0xFF6E401A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "App guard is off — blocking will be slow and reels cannot be closed.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = Color(0xFFE5D5C5),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = primaryGreen)
                    ) {
                        Text("Fix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // 3. Daily Focus Goal Card (Clickable to Edit)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showEditGoalDialog = true
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color(0xFF8B9E99),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily focus goal",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B2E27))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${uiState.todayFocusHoursMinutes} / ${uiState.goalHours}h",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goal",
                            tint = primaryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Glowing Neon Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22362F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(uiState.goalProgressFraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF10B981),
                                        Color(0xFF34D399)
                                    )
                                )
                            )
                    )
                }
            }
        }

        // 4. Circular Timer Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(240.dp)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val progressFraction = if (uiState.timerMode == FocusTimerMode.STOPWATCH) {
                ((uiState.stopwatchSecondsElapsed % 3600).toFloat() / 3600f)
            } else {
                if (uiState.totalPhaseDurationSeconds > 0) {
                    ((uiState.totalPhaseDurationSeconds - uiState.secondsRemaining).toFloat() / uiState.totalPhaseDurationSeconds.toFloat())
                } else 0f
            }

            val animatedProgress by animateFloatAsState(
                targetValue = if (progressFraction > 0f) progressFraction else 0.08f,
                animationSpec = tween(durationMillis = 500),
                label = "TimerRingAnim"
            )

            Canvas(modifier = Modifier.size(210.dp)) {
                val strokePx = 16.dp.toPx()
                val arcSize = size.width - strokePx
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                // Dark Outer Segmented Track
                drawArc(
                    color = Color(0xFF1B2924),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glowing Bright Neon Green Arc
                drawArc(
                    color = primaryGreen,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Center Content: Mode Pill + Big Digits + Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF13362A),
                    border = BorderStroke(1.dp, Color(0xFF22C55E)),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showModeDialog = true
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = uiState.timerMode.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = primaryGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                val totalSec = if (uiState.timerMode == FocusTimerMode.STOPWATCH) {
                    uiState.stopwatchSecondsElapsed
                } else {
                    uiState.secondsRemaining
                }
                val m = totalSec / 60
                val s = totalSec % 60
                val timeString = String.format("%02d:%02d", m, s)

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                val statusText = when {
                    uiState.isPaused -> "Paused"
                    uiState.isRunning && uiState.timerMode == FocusTimerMode.STOPWATCH -> "Counting up"
                    uiState.isRunning -> "Focusing..."
                    else -> "Ready"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (uiState.isPaused) Color(0xFFF59E0B) else primaryGreen
                )
            }
        }

        // 5. Session Info Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF122820)),
            border = BorderStroke(1.dp, Color(0xFF1A3B2F)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val sessionDesc = if (uiState.timerMode == FocusTimerMode.STOPWATCH) {
                    "Stopwatch. Blocked apps stay shut until you finish."
                } else {
                    "Pomodoro: ${uiState.workDurationMinutes}m focus. Distractions completely blocked."
                }
                Text(
                    text = sessionDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBFEADB)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 6. Action Controls: "Pause" + "+ 5 min"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (uiState.isRunning) {
                        viewModel.togglePause()
                    } else {
                        viewModel.startTimer()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2B25)),
                border = BorderStroke(1.dp, Color(0xFF283D35)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isRunning && !uiState.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val label = when {
                        uiState.isPaused -> "Resume"
                        uiState.isRunning -> "Pause (${uiState.remainingPauses})"
                        else -> "Start Focus"
                    }
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.addFiveMinutes()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2B25)),
                border = BorderStroke(1.dp, Color(0xFF283D35)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "+ 5 min",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7. Wide Action Button: "✓ Finish and bank the time"
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.finishAndBankTime()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF143B2C)),
            border = BorderStroke(1.5.dp, primaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Finish and bank the time",
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 8. Blocking Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Blocking Protection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.fullyBlockedCount} apps blocked completely, ${uiState.reelsOnlyCount} feeds blocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9E99)
                    )
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Dialog: Edit Daily Focus Goal
    if (showEditGoalDialog) {
        var selectedHours by remember { mutableIntStateOf(uiState.goalHours) }
        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            title = {
                Text(
                    text = "Edit Daily Focus Goal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Set your personal target hours per day:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9E99)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (selectedHours > 1) selectedHours-- }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = primaryGreen)
                        }
                        Text(
                            text = "${selectedHours} Hours",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryGreen,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { if (selectedHours < 16) selectedHours++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = primaryGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Quick Goal Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(1, 2, 3, 4, 6).forEach { h ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedHours == h) primaryGreen else Color(0xFF1B2E27),
                                modifier = Modifier.clickable { selectedHours = h }
                            ) {
                                Text(
                                    text = "${h}h",
                                    color = if (selectedHours == h) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateDailyGoal(selectedHours, 0)
                        showEditGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text("Save Goal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel", color = Color(0xFF8B9E99))
                }
            },
            containerColor = Color(0xFF131F1B)
        )
    }

    // Dialog: Select Timer Mode & Duration
    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = {
                Text(
                    text = "Timer Mode & Duration",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose your preferred focus pacing style:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9E99)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stopwatch Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.timerMode == FocusTimerMode.STOPWATCH) Color(0xFF1B4D3E) else Color(0xFF172520),
                        border = BorderStroke(1.dp, if (uiState.timerMode == FocusTimerMode.STOPWATCH) primaryGreen else Color(0xFF233830)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setTimerModeAndDuration(FocusTimerMode.STOPWATCH, 0)
                                showModeDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = primaryGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Stopwatch Mode", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Count up indefinitely until you finish", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pomodoro Presets
                    Text("Pomodoro Countdown Presets:", style = MaterialTheme.typography.labelMedium, color = primaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(15 to "15 min (Quick Sprint)", 25 to "25 min (Standard Pomodoro)", 45 to "45 min (Deep Work)", 60 to "60 min (Mastery)").forEach { (mins, label) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.timerMode == FocusTimerMode.POMODORO && uiState.workDurationMinutes == mins) Color(0xFF1B4D3E) else Color(0xFF172520),
                            border = BorderStroke(1.dp, if (uiState.timerMode == FocusTimerMode.POMODORO && uiState.workDurationMinutes == mins) primaryGreen else Color(0xFF233830)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setTimerModeAndDuration(FocusTimerMode.POMODORO, mins)
                                    showModeDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showModeDialog = false }) {
                    Text("Close", color = Color(0xFF8B9E99))
                }
            },
            containerColor = Color(0xFF131F1B)
        )
    }
}
