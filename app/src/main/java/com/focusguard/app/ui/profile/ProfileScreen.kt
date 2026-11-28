package com.focusguard.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.ui.components.GuardyMascotFull
import com.focusguard.app.ui.components.ProBadgePill
import com.focusguard.app.ui.theme.AmoledBlack
import com.focusguard.app.ui.theme.CardBorderDark
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class AchievementBadge(
    val title: String,
    val subtitle: String,
    val currentMinutes: Int,
    val targetMinutes: Int
)

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onStartFocus: () -> Unit
) {
    val app = LocalContext.current.applicationContext as FocusGuardApp
    val prefsRepo = app.preferencesRepository
    val scope = rememberCoroutineScope()

    val userName by prefsRepo.userName.collectAsState(initial = "Aryan")
    val selectedExam by prefsRepo.selectedExam.collectAsState(initial = "Class 12")
    val joinedSince by prefsRepo.joinedSinceLabel.collectAsState(initial = "Focusing since Nov, 2026")
    val totalMinutesSaved by prefsRepo.totalMinutesSaved.collectAsState(initial = 0)
    val streakDays by prefsRepo.currentStreakDays.collectAsState(initial = 1)
    val treesPlanted by prefsRepo.totalTreesPlanted.collectAsState(initial = 0)

    var showEditNameDialog by remember { mutableStateOf(false) }

    val totalFormatted = remember(totalMinutesSaved) {
        if (totalMinutesSaved <= 0) "-" else {
            val h = totalMinutesSaved / 60
            val m = totalMinutesSaved % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        }
    }

    val badges = remember(totalMinutesSaved, treesPlanted, streakDays) {
        listOf(
            AchievementBadge("1H FOCUS", "${totalMinutesSaved.coerceAtMost(60)}m / 1h focused", totalMinutesSaved, 60),
            AchievementBadge("5H FOCUS", "${totalMinutesSaved.coerceAtMost(300)}m / 5h focused", totalMinutesSaved, 300),
            AchievementBadge("3D STREAK", "$streakDays / 3 days", streakDays, 3),
            AchievementBadge("5 TREES", "$treesPlanted / 5 trees", treesPlanted, 5)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        // 1. Bright Green Header Banner with Avatar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF5DC42C))
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF426728).copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x5512280B),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onNavigateBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0x5512280B),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onOpenSettings() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. Profile Identity + PRO Mascot Card
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showEditNameDialog = true }
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal: $selectedExam • 100% Offline",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = joinedSince,
                    color = TextMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // FocusGuard PRO Card with Full-body Guardy
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FocusGuard",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ProBadgePill()
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All Pro Shields, Shorts Blocker & Ultra Focus are unlocked for you.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Manage Permissions",
                                color = GuardGreen,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { onOpenSettings() }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GuardyMascotFull(modifier = Modifier.size(104.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Overview
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartFocus() }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1A2A15),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = GuardGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Total Time Focused",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = totalFormatted,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A2913))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = if (totalMinutesSaved == 0) {
                                    "Start your first focus session now"
                                } else {
                                    "$treesPlanted focus trees planted • $streakDays-day streak"
                                },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Achievements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Achievements",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "View all",
                        color = GuardGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onOpenStats() }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(badges) { badge ->
                    val unlocked = badge.currentMinutes >= badge.targetMinutes
                    val progress = (badge.currentMinutes.toFloat() / badge.targetMinutes.toFloat()).coerceIn(0f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(114.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(92.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val r = size.minDimension * 0.45f
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val hexPath = Path().apply {
                                    for (i in 0..5) {
                                        val angle = PI / 3.0 * i - PI / 6.0
                                        val px = cx + (r * cos(angle)).toFloat()
                                        val py = cy + (r * sin(angle)).toFloat()
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                    close()
                                }
                                drawPath(
                                    path = hexPath,
                                    color = if (unlocked) Color(0xFF224219) else Color(0xFF262A27)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = if (unlocked) GuardGreen else Color(0xFF474C48),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = badge.title,
                                    color = if (unlocked) Color.White else Color(0xFF6B706C),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF252926))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(GuardGreen)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = badge.subtitle,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Weekly Reports",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStats() }
                ) {
                    Text(
                        text = "Tap to view daily screen time charts, app donut breakdown & CSV export",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }

    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = CardDark,
            title = { Text("Edit Profile Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GuardGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        prefsRepo.setUserProfile(
                            name = tempName.ifBlank { "Aryan" },
                            usageLevel = "4-6 hours",
                            goal = selectedExam,
                            dailyGoalMinutes = 240
                        )
                    }
                    showEditNameDialog = false
                }) {
                    Text("Save", color = GuardGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
