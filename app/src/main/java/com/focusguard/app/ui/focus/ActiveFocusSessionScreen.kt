package com.focusguard.app.ui.focus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguard.app.data.model.FocusSoundType
import com.focusguard.app.ui.components.AuroraSessionBackground
import com.focusguard.app.ui.components.GuardyMascotHead
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGold
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFocusSessionScreen(
    onOpenGroups: () -> Unit,
    onExitSession: () -> Unit,
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTagMenu by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    BackHandler {
        // Minimize back to dashboard while timer keeps running in FocusTimerService
        onExitSession()
    }

    val totalSec = if (uiState.timerMode == FocusTimerMode.STOPWATCH) {
        uiState.stopwatchSecondsElapsed
    } else {
        uiState.secondsRemaining
    }
    val timeText = "%02d:%02d".format(totalSec / 60, totalSec % 60)

    val tags = listOf("Untagged", "Study", "Deep Work", "Exam Prep", "Coding", "Reading")
    val quotes = remember {
        listOf(
            "Today’s focus session is going to be\namazing. Let’s do this!",
            "Stay locked in. Every minute builds\nyour future.",
            "Ignore the noise — Guardy is keeping\ndistractions away."
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraSessionBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Group Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0x660B1E14),
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.clickable { onOpenGroups() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF283A2F),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF9FB8A9),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.activeGroupName ?: "Create group with friends!",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Center Circular Countdown Ring
            Surface(
                shape = CircleShape,
                color = Color(0xAA0C1D11),
                border = BorderStroke(3.dp, Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.size(258.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showTagMenu = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = uiState.activeTag,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showTagMenu,
                            onDismissRequest = { showTagMenu = false },
                            containerColor = CardDark
                        ) {
                            tags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag, color = Color.White) },
                                    onClick = {
                                        viewModel.setSessionTag(tag)
                                        showTagMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (uiState.timerMode) {
                                FocusTimerMode.TIMER -> "Timer"
                                FocusTimerMode.STOPWATCH -> "Stopwatch"
                                FocusTimerMode.POMODORO -> "Pomodoro"
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0x44FFFFFF),
                        modifier = Modifier.clickable { viewModel.togglePause() }
                    ) {
                        Text(
                            text = if (uiState.isPaused) "Resume" else "Take a break",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // 3. Bottom Motivation Card + Controls + Pause/Finish CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x880B1C12),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = quotes[(uiState.completedSessionsCount) % quotes.size],
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Menu / Mascot / Gold Lock Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x660B1E14),
                        border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { showOptionsSheet = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Session Controls",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF102016)),
                        contentAlignment = Alignment.Center
                    ) {
                        GuardyMascotHead(modifier = Modifier.size(54.dp), showHalo = false)
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (uiState.isUltraFocus) Color(0x994A3B08) else Color(0x660B1E14),
                        border = BorderStroke(1.dp, GuardGold.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { viewModel.toggleUltraFocus() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Ultra Focus Lock",
                                tint = GuardGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (uiState.isPaused) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.togglePause() },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GuardGreen,
                                contentColor = Color(0xFF0A1607)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Text("Resume", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.finishAndBankTime()
                                onExitSession()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x8816231C),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color(0x55FFFFFF)),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Text("Finish & Save", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0x770D1D14),
                        border = BorderStroke(1.dp, Color(0x55FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clickable { viewModel.togglePause() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pause",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOptionsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            sheetState = sheetState,
            containerColor = CardDark,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Session Controls",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Ambient Focus Sound", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FocusSoundType.entries.forEach { sound ->
                        val selected = uiState.selectedSound == sound
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) GuardGreen else Color(0xFF1D221F),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectSound(sound) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (selected) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sound.title.substringBefore(" "),
                                    color = if (selected) Color.Black else Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.addFiveMinutes()
                            showOptionsSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2521)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("+5 mins", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            showOptionsSheet = false
                            viewModel.finishAndBankTime()
                            onExitSession()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1616)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Finish Session", color = Color(0xFFFF8A8A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
