package com.helloandroid.process

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLabStore.recordLifecycle(
            source = "Activity",
            phase = "onCreate",
            detail = "savedState=${savedInstanceState != null}, process=${currentProcessName()}"
        )
        setContent {
            ProcessZygoteLabApp()
        }
    }
}
