package com.focusguard.app.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguard.app.ui.components.DottedMountainClimbCanvas
import com.focusguard.app.ui.components.DottedShieldPlateauCanvas
import com.focusguard.app.ui.components.GuardyMascotHead
import com.focusguard.app.ui.theme.AmoledBlack
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.GuardRed
import com.focusguard.app.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler(enabled = uiState.step > 0) {
        viewModel.previousStep()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val backgroundBrush = when (uiState.step) {
        5, 6 -> Brush.verticalGradient(
            listOf(Color(0xFF060707), Color(0xFF1A0808), Color(0xFF280B0B))
        )
        7 -> Brush.verticalGradient(
            listOf(Color(0xFF071B05), Color(0xFF0E2C08), Color(0xFF0B2406))
        )
        else -> Brush.verticalGradient(listOf(AmoledBlack, AmoledBlack))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingStep"
        ) { step ->
            when (step) {
                0 -> IntroMascotStep(onContinue = { viewModel.nextStep() })
                1 -> QuizStepLayout(
                    progress = 0.22f,
                    question = "Are you preparing for any exam?",
                    canContinue = uiState.isPreparingExam != null,
                    onContinue = { viewModel.nextStep() }
                ) {
                    QuizOptionCard(
                        label = "Yes",
                        selected = uiState.isPreparingExam == true,
                        onClick = { viewModel.setPreparingExam(true) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    QuizOptionCard(
                        label = "No",
                        selected = uiState.isPreparingExam == false,
                        onClick = { viewModel.setPreparingExam(false) }
                    )
                }
                2 -> {
                    val filteredExams = remember(uiState.examSearchQuery) {
                        if (uiState.examSearchQuery.isBlank()) viewModel.availableExams
                        else viewModel.availableExams.filter {
                            it.contains(uiState.examSearchQuery, ignoreCase = true)
                        }
                    }
                    QuizStepLayout(
                        progress = 0.42f,
                        question = "Which exam are you preparing for?",
                        canContinue = uiState.selectedExam.isNotBlank(),
                        onContinue = { viewModel.nextStep() }
                    ) {
                        OutlinedTextField(
                            value = uiState.examSearchQuery,
                            onValueChange = { viewModel.setExamSearchQuery(it) },
                            placeholder = { Text("Search exams (UPSC, NEET, Banking...)", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = GuardGreen,
                                unfocusedBorderColor = Color(0xFF232724),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredExams) { exam ->
                                QuizOptionCard(
                                    label = exam,
                                    selected = uiState.selectedExam == exam,
                                    onClick = { viewModel.setSelectedExam(exam) }
                                )
                            }
                        }
                    }
                }
                3 -> QuizStepLayout(
                    progress = 0.65f,
                    question = "How often do you study?",
                    canContinue = uiState.studyFrequency.isNotBlank(),
                    onContinue = { viewModel.nextStep() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.studyFrequencies) { freq ->
                            QuizOptionCard(
                                label = freq,
                                selected = uiState.studyFrequency == freq,
                                onClick = { viewModel.setStudyFrequency(freq) }
                            )
                        }
                    }
                }
                4 -> QuizStepLayout(
                    progress = 0.88f,
                    question = "When do you scroll the most?",
                    canContinue = uiState.scrollHabit.isNotBlank(),
                    onContinue = { viewModel.nextStep() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.scrollHabits) { habit ->
                            QuizOptionCard(
                                label = habit,
                                selected = uiState.scrollHabit == habit,
                                onClick = { viewModel.setScrollHabit(habit) }
                            )
                        }
                    }
                }
                5 -> ClimbStoryStep(
                    onContinue = { viewModel.nextStep() }
                )
                6 -> {
                    val (highlight, subtitle) = viewModel.examStatisticHeadline()
                    ExamStatStoryStep(
                        highlight = highlight,
                        subtitle = subtitle,
                        onContinue = { viewModel.nextStep() }
                    )
                }
                7 -> ShieldStoryStep(
                    onContinue = { viewModel.nextStep() }
                )
                8 -> SetupPermissionsStep(
                    userName = uiState.userName,
                    onNameChange = { viewModel.updateName(it) },
                    hasUsageAccess = uiState.hasUsageAccess,
                    hasAccessibilityAccess = uiState.hasAccessibilityAccess,
                    onGrantUsage = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    onGrantAccessibility = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onFinish = { viewModel.completeOnboarding(onFinished) }
                )
            }
        }
    }
}

@Composable
private fun IntroMascotStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Hi, I’m Guardy. I’ll help you stay\nfocused on your studies!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    lineHeight = 30.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(36.dp))

            GuardyMascotHead(
                modifier = Modifier.size(300.dp),
                showHalo = true
            )
        }

        WhitePillButton(
            text = "Continue",
            enabled = true,
            onClick = onContinue
        )
    }
}

@Composable
private fun QuizStepLayout(
    progress: Float,
    question: String,
    canContinue: Boolean,
    onContinue: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Top Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF232724))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Guardy + Chat Bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF121513)),
                contentAlignment = Alignment.Center
            ) {
                GuardyMascotHead(modifier = Modifier.size(66.dp), showHalo = false)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1D1B),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 23.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WhitePillButton(
            text = "Continue",
            enabled = canContinue,
            onClick = onContinue
        )
    }
}

@Composable
private fun QuizOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFF172714) else CardDark,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) GuardGreen else Color(0xFF222623)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = GuardGreen,
                    unselectedColor = Color(0xFF7B817D)
                )
            )
        }
    }
}

@Composable
private fun ClimbStoryStep(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = buildAnnotatedString {
                    append("Your ")
                    withStyle(SpanStyle(color = GuardGreen)) { append("dream") }
                    append("\nis a long climb")
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 31.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DottedMountainClimbCanvas(
                    modifier = Modifier.fillMaxSize(),
                    showPeakFlag = true,
                    showClimber = true
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 44.dp, end = 48.dp)
                ) {
                    Text(
                        text = "95%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "You",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 118.dp, bottom = 54.dp)
                )
            }

            WhitePillButton(text = "Continue", enabled = true, onClick = onContinue)
        }
    }
}

@Composable
private fun ExamStatStoryStep(
    highlight: String,
    subtitle: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = GuardRed)) { append(highlight) }
                append(" ")
                append(subtitle)
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                lineHeight = 30.sp
            ),
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            DottedMountainClimbCanvas(
                modifier = Modifier.fillMaxSize(),
                showPeakFlag = true,
                showClimber = true
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 48.dp)
            ) {
                Text(
                    text = "95%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "You",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 118.dp, bottom = 54.dp)
            )
        }

        WhitePillButton(text = "Continue", enabled = true, onClick = onContinue)
    }
}

@Composable
private fun ShieldStoryStep(onContinue: () -> Unit) {
    var lockedPhase by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1700)
        lockedPhase = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = if (!lockedPhase) {
                buildAnnotatedString {
                    append("But don’t worry, ")
                    withStyle(SpanStyle(color = GuardGreen)) { append("FocusGuard") }
                    append("\nhas helped ")
                    withStyle(SpanStyle(color = GuardGreen)) { append("50 lakh") }
                    append(" students")
                }
            } else {
                buildAnnotatedString {
                    append("It ")
                    withStyle(SpanStyle(color = GuardGreen)) { append("blocks the apps") }
                    append("\nthat pull you down")
                }
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                lineHeight = 30.sp
            ),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            DottedShieldPlateauCanvas(
                modifier = Modifier.fillMaxSize(),
                showPadlocks = lockedPhase
            )
            Text(
                text = "You",
                color = GuardGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 110.dp)
            )
        }

        WhitePillButton(
            text = "Continue",
            enabled = true,
            onClick = {
                if (!lockedPhase) lockedPhase = true else onContinue()
            }
        )
    }
}

@Composable
private fun SetupPermissionsStep(
    userName: String,
    onNameChange: (String) -> Unit,
    hasUsageAccess: Boolean,
    hasAccessibilityAccess: Boolean,
    onGrantUsage: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GuardyMascotHead(modifier = Modifier.size(56.dp), showHalo = false)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Almost ready!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "100% private & offline — everything stays on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Your Name",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = onNameChange,
                placeholder = { Text("Enter your name (e.g. Aryan)", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedBorderColor = GuardGreen,
                    unfocusedBorderColor = Color(0xFF232724),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionSetupCard(
                title = "Usage Access",
                description = "Track your daily screen time and enforce app time limits.",
                granted = hasUsageAccess,
                onGrant = onGrantUsage
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionSetupCard(
                title = "Accessibility Service",
                description = "Instantly block distracting apps, YouTube Shorts, and Reels.",
                granted = hasAccessibilityAccess,
                onGrant = onGrantAccessibility
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        WhitePillButton(
            text = if (hasUsageAccess) "Start Focusing" else "Continue to FocusGuard",
            enabled = true,
            onClick = onFinish
        )
    }
}

@Composable
private fun PermissionSetupCard(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, if (granted) GuardGreen else Color(0xFF232724)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!granted) onGrant() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (granted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = GuardGreen,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GuardGreen
                ) {
                    Text(
                        text = "Allow",
                        color = Color(0xFF0A1607),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WhitePillButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF080A09),
            disabledContainerColor = Color(0xFF2B2E2C),
            disabledContentColor = Color(0xFF7D837F)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
