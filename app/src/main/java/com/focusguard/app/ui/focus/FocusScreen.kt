package com.focusguard.app.ui.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguard.app.data.model.FocusSoundType
import com.focusguard.app.data.model.FocusTheme
import com.focusguard.app.ui.theme.ProgressAmber

@Composable
fun FocusScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val primaryThemeColor = Color(uiState.selectedTheme.primaryColorHex)
    val backgroundThemeColor = Color(uiState.selectedTheme.backgroundColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundThemeColor)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Header: Streaks & Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Focus Mode",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Distracting apps are blocked during focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Streak Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ProgressAmber.copy(alpha = 0.15f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = ProgressAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.currentStreakDays} day streak",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ProgressAmber
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Mode Selector (Pomodoro vs Stopwatch)
        PrimaryTabRow(
            selectedTabIndex = if (uiState.timerMode == FocusTimerMode.POMODORO) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = primaryThemeColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = uiState.timerMode == FocusTimerMode.POMODORO,
                onClick = { viewModel.setTimerMode(FocusTimerMode.POMODORO, onNavigateToPremium) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pomodoro", fontWeight = FontWeight.SemiBold)
                        if (!uiState.isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Star, contentDescription = "PRO", tint = ProgressAmber, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            )
            Tab(
                selected = uiState.timerMode == FocusTimerMode.STOPWATCH,
                onClick = { viewModel.setTimerMode(FocusTimerMode.STOPWATCH, onNavigateToPremium) },
                text = { Text("Stopwatch (Free)", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big Circular Countdown / Elapsed Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            val progressFraction = if (uiState.timerMode == FocusTimerMode.POMODORO) {
                if (uiState.totalPhaseDurationSeconds > 0) {
                    uiState.secondsRemaining.toFloat() / uiState.totalPhaseDurationSeconds.toFloat()
                } else 0f
            } else {
                ((uiState.stopwatchSecondsElapsed % 3600).toFloat() / 3600f)
            }

            val animatedProgress by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = tween(durationMillis = 600),
                label = "TimerRingAnim"
            )

            Canvas(modifier = Modifier.size(240.dp)) {
                val strokePx = 14.dp.toPx()
                val arcSize = size.width - strokePx
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                // Track
                drawArc(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Progress
                drawArc(
                    color = primaryThemeColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Readout in center
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val timeString = if (uiState.timerMode == FocusTimerMode.POMODORO) {
                    val m = uiState.secondsRemaining / 60
                    val s = uiState.secondsRemaining % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    val m = uiState.stopwatchSecondsElapsed / 60
                    val s = uiState.stopwatchSecondsElapsed % 60
                    String.format("%02d:%02d", m, s)
                }

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                val subtitle = if (uiState.timerMode == FocusTimerMode.POMODORO) {
                    if (uiState.pomodoroPhase == PomodoroPhase.WORK) "Deep Focus" else "Break Time"
                } else {
                    "Stopwatch Session"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = primaryThemeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Play / Pause / Reset Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.stopTimer() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Button(
                onClick = {
                    if (uiState.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryThemeColor)
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Ambient Focus Sound Player (Procedural audio)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = primaryThemeColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ambient Sound Generator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!uiState.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ProgressAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ProgressAmber
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FocusSoundType.entries.forEach { sound ->
                        val isSelected = uiState.selectedSound == sound
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primaryThemeColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clickable { viewModel.selectSound(sound, onNavigateToPremium) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sound.title.substringBefore(" "),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Themes Selector
        Text(
            text = "Focus Themes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FocusTheme.entries) { theme ->
                val isSelected = uiState.selectedTheme == theme
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(theme.backgroundColorHex)),
                    modifier = Modifier
                        .size(width = 110.dp, height = 70.dp)
                        .clickable { viewModel.selectTheme(theme, onNavigateToPremium) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.primaryColorHex))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
