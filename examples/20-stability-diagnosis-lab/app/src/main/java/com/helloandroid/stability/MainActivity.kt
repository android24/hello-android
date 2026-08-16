package com.helloandroid.stability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StabilityLabStore.record("Activity", "ON_CREATE", "savedState=${savedInstanceState != null}")
        setContent {
            StabilityDiagnosisLabApp()
        }
    }
}
