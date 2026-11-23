package com.focusguard.app.ui.tips

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TipsScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "BreatheAnim")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathScale"
    )

    val primaryGreen = Color(0xFF00E676)
    val cardBg = Color(0xFF16221E)
    val cardBorder = Color(0xFF1E312B)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1412))
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF15382B),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Focus Wisdom",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "40+ science-backed digital wellness strategies",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9E99)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 1. Mindful Breathing Exercise Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mindful Pause (Box Breathing)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inhale slowly as the ring expands. Reset your nervous system.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9E99)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryGreen.copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, primaryGreen),
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(breathScale)
                        ) {}
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Inhale • Hold • Exhale",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = primaryGreen
                    )
                }
            }
        }

        // 2. Curated Focus Tips
        val tipsList = listOf(
            TipItem(
                title = "The 20-Second Friction Rule",
                desc = "Add 20 seconds of friction (like FocusGuard's delay timer) between you and an addictive app. Your impulse brain gives up 80% of the time.",
                tag = "Habits"
            ),
            TipItem(
                title = "Dopamine Fasting in the Morning",
                desc = "Never check Reels or Shorts in the first 60 minutes after waking up. Your morning dopamine baseline sets your attention span for the whole day.",
                tag = "Neuroscience"
            ),
            TipItem(
                title = "Batched Notifications",
                desc = "Turn off real-time notifications for social apps. Schedule two 15-minute windows at 1 PM and 7 PM to reply to non-urgent messages.",
                tag = "Productivity"
            ),
            TipItem(
                title = "Study Desk Phone Quarantine",
                desc = "Place your phone in another room or keep FocusGuard's desk mode active. Visual presence of a phone reduces cognitive working memory.",
                tag = "Focus"
            ),
            TipItem(
                title = "The 5-Minute Emergency Delay",
                desc = "Whenever you feel an uncontrollable urge to open a blocked app, wait 5 full minutes. In 9 out of 10 cases, the craving disappears.",
                tag = "Willpower"
            ),
            TipItem(
                title = "Night Rest & Circadian Clock",
                desc = "Activate FocusGuard's recurring night schedule from 10 PM to 7 AM. Blue light after 10 PM delays melatonin secretion by 90 minutes.",
                tag = "Sleep"
            )
        )

        items(tipsList.size) { index ->
            val tip = tipsList[index]
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tip.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF183B2D)
                        ) {
                            Text(
                                text = tip.tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tip.desc,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = Color(0xFFB0BEBA)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

data class TipItem(
    val title: String,
    val desc: String,
    val tag: String
)
