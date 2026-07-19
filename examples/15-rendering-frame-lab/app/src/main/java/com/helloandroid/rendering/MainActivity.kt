package com.helloandroid.rendering

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RenderingFrameLabApp(
                onMainThreadBusy = {
                    val durationMs = 140L
                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() - start < durationMs) {
                        // Intentionally busy for a short frame-budget experiment.
                    }
                    RenderingLabStore.recordMainThreadBusy(durationMs)
                }
            )
        }
    }
}
