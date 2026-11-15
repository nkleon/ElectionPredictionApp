package com.example.electionpredictionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.electionpredictionapp.routes.*
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElectionPredictionAppTheme {
                AppNavigation(this)
            }
        }
    }
}