package com.focusguard.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val primaryGreen = Color(0xFF00E676)
    val cardBg = Color(0xFF131F1B)
    val cardBorder = Color(0xFF1E362E)
    val redColor = Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E0C))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090E0C))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Focus & Timer Settings Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "FOCUS & TIMER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Daily Screen Time Goal
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = primaryGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily Focus Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${uiState.dailyGoalMinutes / 60}h ${uiState.dailyGoalMinutes % 60}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = uiState.dailyGoalMinutes.toFloat(),
                            onValueChange = { viewModel.setDailyGoalMinutes(it.toInt()) },
                            valueRange = 30f..480f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryGreen,
                                activeTrackColor = primaryGreen,
                                inactiveTrackColor = Color(0xFF22362F)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("30m", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9E99))
                            Text("2h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9E99))
                            Text("4h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9E99))
                            Text("8h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9E99))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pomodoro Duration Presets
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Pomodoro Work Duration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Default length for focused work intervals",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B9E99)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(15 to "15m", 25 to "25m", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                                val selected = uiState.pomodoroWorkMinutes == mins
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) Color(0xFF1B4D3E) else Color(0xFF172520),
                                    border = BorderStroke(1.dp, if (selected) primaryGreen else Color(0xFF233830)),
                                    modifier = Modifier.clickable { viewModel.setPomodoroWorkMinutes(mins) }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (selected) primaryGreen else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Audio & Haptics Header
            item {
                Text(
                    text = "FEEDBACK & SOUNDS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Haptics & Chimes Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Haptic Feedback Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = primaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Haptic Feedback", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Gentle vibration on button taps", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
                                }
                            }
                            Switch(
                                checked = uiState.hapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryGreen,
                                    checkedTrackColor = Color(0xFF1B4D3E)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Completion Chime Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = primaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Completion Alert", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Vibrate & alert when focus ends", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
                                }
                            }
                            Switch(
                                checked = uiState.soundChimeEnabled,
                                onCheckedChange = { viewModel.setSoundChimeEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryGreen,
                                    checkedTrackColor = Color(0xFF1B4D3E)
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Strict Mode Header
            item {
                Text(
                    text = "PROTECTION & STRICT MODE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Strict Mode Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = primaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Strict Mode Enforcement", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Prevents cancelling focus early", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
                                }
                            }
                            Switch(
                                checked = uiState.isStrictModeEnabled,
                                onCheckedChange = { viewModel.setStrictMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryGreen,
                                    checkedTrackColor = Color(0xFF1B4D3E)
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 4. Permissions Doctor Header
            item {
                Text(
                    text = "SYSTEM PERMISSIONS DOCTOR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ensure all permissions are active for flawless 0ms blocking",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B9E99)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Permission Items
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // 1. Accessibility
                        PermissionRow(
                            name = "Accessibility Service",
                            desc = "Required for instant app & reels blocking",
                            isGranted = uiState.isAccessibilityGranted,
                            onGrant = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Usage Stats
                        PermissionRow(
                            name = "Usage Access",
                            desc = "Required to track daily app limits",
                            isGranted = uiState.isUsageStatsGranted,
                            onGrant = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Display Over Other Apps
                        PermissionRow(
                            name = "Display Over Other Apps",
                            desc = "Required to show distraction overlay scrim",
                            isGranted = uiState.isOverlayGranted,
                            onGrant = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Notifications
                        PermissionRow(
                            name = "Notification Permission",
                            desc = "Required for live timer in background",
                            isGranted = uiState.isNotificationGranted,
                            onGrant = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. Battery Optimization Exemption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.BatterySaver, contentDescription = null, tint = primaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Battery Killer Exemption", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Prevents MIUI/Samsung from killing blocker", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
                                }
                            }
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))
                            ) {
                                Text("Whitelist", color = primaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 5. 100% Offline & Privacy Guarantee Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11261F)),
                    border = BorderStroke(1.dp, Color(0xFF1E483A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15382B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "100% Offline & Certified Private",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "FocusGuard does NOT have the internet permission. All app usage data and blocking configurations remain exclusively in your phone's encrypted SQLite database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9ECEBE)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun PermissionRow(
    name: String,
    desc: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    val primaryGreen = Color(0xFF00E676)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = primaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9E99))
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF17382A)
            ) {
                Text(
                    text = "Active",
                    color = primaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        } else {
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Enable", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
