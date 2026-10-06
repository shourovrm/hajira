package com.rms.hazira

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rms.hazira.ui.HaziraNavigation
import com.rms.hazira.ui.theme.HaziraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is light-only. Without this, a phone in dark mode gets white status bar
        // icons on the pale page and they cannot be read.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            HaziraTheme {
                HaziraNavigation()
            }
        }
    }
}
