package com.focusguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.focusguard.app.ui.navigation.FocusNavGraph
import com.focusguard.app.ui.theme.FocusGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FocusGuardApp

        setContent {
            FocusGuardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusNavGraph(
                        preferencesRepository = app.preferencesRepository
                    )
                }
            }
        }
    }
}
