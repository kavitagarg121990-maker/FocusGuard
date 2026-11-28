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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguard.app.ui.components.ProBadgePill
import com.focusguard.app.ui.theme.AmoledBlack
import com.focusguard.app.ui.theme.CardBorderDark
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGold
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenBlocks: () -> Unit,
    onLaunchActiveSession: () -> Unit,
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var showStartSheet by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
                viewModel.refreshScreenTime()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Subtle radial green aura at top center matching screenshot 11
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1D3E12).copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(540f, 180f),
                        radius = 750f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Bar: Initial Avatar (left) + Gold Hourglass Badge (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF282C29),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onOpenProfile() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD5B7FC),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.userName.take(1).uppercase(),
                                    color = Color(0xFF18141E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2C2610),
                    border = BorderStroke(1.dp, Color(0xFF4C3F12)),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onOpenSettings() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Settings & Permissions",
                            tint = GuardGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Hero Circular Focus Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(238.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = uiState.goalProgressFraction.coerceAtLeast(0.02f),
                    animationSpec = tween(600),
                    label = "GoalRing"
                )

                Canvas(modifier = Modifier.size(214.dp)) {
                    val strokePx = 24.dp.toPx()
                    val arcSize = size.width - strokePx
                    val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                    drawArc(
                        color = Color(0xFF262A26),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    if (uiState.todayFocusMinutes > 0) {
                        drawArc(
                            color = GuardGreen,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Top circular green arrow button on the ring
                Surface(
                    shape = CircleShape,
                    color = GuardGreen,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .size(34.dp)
                        .clickable { onOpenStats() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Reports",
                            tint = Color(0xFF0A1705),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showEditGoalDialog = true }
                ) {
                    Text(
                        text = uiState.todayFocusFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Focus goal ${uiState.goalFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            // Live Session Resume Pill if active in background
            if (uiState.isRunning) {
                val liveTime = if (uiState.timerMode == FocusTimerMode.STOPWATCH) {
                    val s = uiState.stopwatchSecondsElapsed
                    "%02d:%02d".format(s / 60, s % 60)
                } else {
                    val s = uiState.secondsRemaining
                    "%02d:%02d".format(s / 60, s % 60)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1A3A15),
                    border = BorderStroke(1.dp, GuardGreen),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 14.dp)
                        .clickable { onLaunchActiveSession() }
                ) {
                    Text(
                        text = "Session running • $liveTime  →",
                        color = GuardGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. PRO Shield Status Banner (Unlocked Free)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3E330B),
                border = BorderStroke(1.dp, Color(0xFF5D4D12)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!uiState.isAccessibilityEnabled) {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } else {
                            onOpenBlocks()
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProBadgePill()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.isAccessibilityEnabled) {
                            "All Pro shields unlocked"
                        } else {
                            "Enable App Guard shield"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2B240A)
                    ) {
                        Text(
                            text = if (uiState.isAccessibilityEnabled) "ACTIVE" else "FIX",
                            color = GuardGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Screen Time Card with multi-color segmented progress bar
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!uiState.hasUsagePermission) {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        } else {
                            onOpenStats()
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (uiState.hasUsagePermission) uiState.todayScreenTimeFormatted else "Grant Access",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.hasUsagePermission) "Screen Time" else "Tap to track daily screen time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-color segmented bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2B2E2B))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            val segments = uiState.screenTimeSegments
                            if (segments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.18f)
                                        .clip(RoundedCornerShape(50))
                                        .background(GuardGreen)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.06f)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFF9800))
                                )
                            } else {
                                segments.forEach { seg ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(seg.fraction.coerceAtLeast(0.04f))
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(seg.colorHex))
                                    )
                                }
                                Spacer(modifier = Modifier.weight(0.35f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Upcoming Schedule Section
            Text(
                text = "Upcoming schedule",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            val schedule = uiState.upcomingSchedule
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSchedule() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF282414),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = GuardGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule?.title ?: "Afternoon Study",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val timeRange = if (schedule != null) {
                            formatScheduleRange(
                                schedule.startHour,
                                schedule.startMinute,
                                schedule.endHour,
                                schedule.endMinute
                            )
                        } else {
                            "Tap to configure study blocks"
                        }
                        Text(
                            text = timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF222623)
                    ) {
                        Text(
                            text = if (schedule != null && schedule.isEnabled) "Active" else "Add",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // 6. Floating "Start focus timer" White Pill CTA above bottom bar
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth()
                .height(62.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (uiState.isRunning) {
                        onLaunchActiveSession()
                    } else {
                        showStartSheet = true
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (uiState.isRunning) "Open active session" else "Start focus timer",
                    color = Color(0xFF0A0D0A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFA6FF6E), GuardGreen, Color(0xFF42AF1F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Start Focus Timer Bottom Sheet (Screenshot 18)
    if (showStartSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showDurationPicker by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { showStartSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF141715),
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                // Segmented Mode Switcher: Timer | Stopwatch | Pomodoro
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F2320),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        listOf(
                            FocusTimerMode.TIMER to "Timer",
                            FocusTimerMode.STOPWATCH to "Stopwatch",
                            FocusTimerMode.POMODORO to "Pomodoro"
                        ).forEach { (mode, label) ->
                            val selected = uiState.timerMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) Color(0xFF283E1C) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val mins = if (mode == FocusTimerMode.STOPWATCH) 0 else uiState.workDurationMinutes.coerceAtLeast(15)
                                        viewModel.setTimerModeAndDuration(mode, mins)
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) GuardGreen else Color.White,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Focus time & Break card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1D201E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDurationPicker = !showDurationPicker }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Focus time",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.timerMode == FocusTimerMode.STOPWATCH) "Count up" else "${uiState.workDurationMinutes} mins",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (showDurationPicker && uiState.timerMode != FocusTimerMode.STOPWATCH) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 25, 45, 60, 90).forEach { mins ->
                                    val sel = uiState.workDurationMinutes == mins
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (sel) GuardGreen else Color(0xFF2A2E2B),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.setTimerModeAndDuration(uiState.timerMode, mins)
                                            }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "${mins}m",
                                                color = if (sel) Color.Black else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF2A2E2B))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nextBreaks = if (uiState.breakCount >= 3) 1 else uiState.breakCount + 1
                                    viewModel.setBreakConfig(nextBreaks, nextBreaks * 5)
                                }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Break",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${uiState.breakCount} break (${uiState.breakDurationMinutes}m)",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Side-by-side Blocked Apps & Ultra Focus cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF152613),
                        border = BorderStroke(1.dp, Color(0xFF223C1E)),
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp)
                            .clickable {
                                showStartSheet = false
                                onOpenBlocks()
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GuardGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF223B1E)
                                ) {
                                    Text(
                                        text = "${uiState.blockedAppsCount} apps",
                                        color = Color(0xFFACDF92),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Blocked apps",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Ultra Focus Gold Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GuardGold,
                        border = BorderStroke(
                            width = if (uiState.isUltraFocus) 2.5.dp else 0.dp,
                            color = if (uiState.isUltraFocus) Color.White else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp)
                            .clickable { viewModel.toggleUltraFocus() }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFE7BA19),
                                modifier = Modifier
                                    .size(110.dp)
                                    .align(Alignment.BottomEnd)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF10120E)
                                    ) {
                                        Text(
                                            text = if (uiState.isUltraFocus) "ON" else "PRO",
                                            color = GuardGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Ultra focus",
                                        color = Color(0xFF10120E),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Achieve non stop focus",
                                        color = Color(0xFF3E3510),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        showStartSheet = false
                        viewModel.startSession()
                        onLaunchActiveSession()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0A0D0A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Edit Daily Goal Dialog
    if (showEditGoalDialog) {
        var tempGoalHours by remember { mutableIntStateOf((uiState.goalMinutes / 60).coerceIn(1, 10)) }
        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            containerColor = CardDark,
            title = {
                Text("Edit Daily Focus Goal", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Target: ${tempGoalHours}h 00m per day",
                        color = GuardGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = tempGoalHours.toFloat(),
                        onValueChange = { tempGoalHours = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = GuardGreen,
                            activeTrackColor = GuardGreen
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDailyGoal(tempGoalHours, 0)
                    showEditGoalDialog = false
                }) {
                    Text("Save", color = GuardGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

fun formatScheduleRange(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): String {
    fun fmt(h: Int, m: Int): String {
        val amPm = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "%02d:%02d %s".format(h12, m, amPm)
    }
    return "${fmt(startHour, startMinute)} - ${fmt(endHour, endMinute)}"
}
