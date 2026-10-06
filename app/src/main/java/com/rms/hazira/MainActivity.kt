package com.rms.hazira

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rms.hazira.ui.HaziraNavigation
import com.rms.hazira.ui.theme.HaziraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HaziraTheme {
                HaziraNavigation()
            }
        }
    }
}
