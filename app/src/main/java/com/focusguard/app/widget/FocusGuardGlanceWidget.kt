package com.focusguard.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.focusguard.app.MainActivity
import com.focusguard.app.service.AppBlockerForegroundService

class FocusGuardGlanceWidget : GlanceAppWidget() {

    private val primaryColorProvider = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFF83D5C5),
        night = androidx.compose.ui.graphics.Color(0xFF83D5C5)
    )
    private val whiteColorProvider = ColorProvider(
        day = androidx.compose.ui.graphics.Color.White,
        night = androidx.compose.ui.graphics.Color.White
    )
    private val subtextColorProvider = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFFA0B3B0),
        night = androidx.compose.ui.graphics.Color(0xFFA0B3B0)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(0xFF1A2120))
                        .cornerRadius(16.dp)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ FocusGuard",
                            style = TextStyle(
                                color = primaryColorProvider,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Text(
                        text = "3h 42m",
                        style = TextStyle(
                            color = whiteColorProvider,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "of 5h daily limit",
                        style = TextStyle(
                            color = subtextColorProvider,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(androidx.compose.ui.graphics.Color(0xFF1D6A5D))
                            .cornerRadius(8.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open FocusGuard",
                            style = TextStyle(
                                color = whiteColorProvider,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

class FocusGuardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusGuardGlanceWidget()
}
