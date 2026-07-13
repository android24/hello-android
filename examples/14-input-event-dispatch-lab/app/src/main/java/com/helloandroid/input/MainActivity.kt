package com.helloandroid.input

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InputDispatchLabApp(
                onMainThreadBusy = {
                    val durationMs = 180L
                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() - start < durationMs) {
                        // Intentionally busy for a short input-latency experiment.
                    }
                    InputLabStore.recordMainThreadBusy(durationMs)
                }
            )
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        InputLabStore.recordActivityEvent(event)
        return super.dispatchTouchEvent(event)
    }
}
