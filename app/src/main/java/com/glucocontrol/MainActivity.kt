package com.glucocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.glucocontrol.presentation.navigation.GlucoApp
import com.glucocontrol.presentation.theme.GlucoControlTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // extiende el contenido bajo la status bar
        setContent {
            GlucoControlTheme {
                GlucoApp()
            }
        }
    }
}
