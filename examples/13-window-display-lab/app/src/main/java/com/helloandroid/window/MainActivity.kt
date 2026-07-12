package com.helloandroid.window

import android.os.Bundle
import android.view.Choreographer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(Unit) {
                recordWindowSnapshot()
            }
            WindowDisplayLabApp(
                onRefreshWindowInfo = { recordWindowSnapshot() },
                onShowToast = {
                    WindowLabStore.recordToastShown()
                    Toast.makeText(this, "Toast 是系统短提示，不适合复杂交互", Toast.LENGTH_SHORT).show()
                },
                onRequestFrame = {
                    Choreographer.getInstance().postFrameCallback { frameTimeNanos ->
                        WindowLabStore.recordFrame(frameTimeNanos)
                    }
                },
                onMainThreadBusy = {
                    val durationMs = 180L
                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() - start < durationMs) {
                        // Intentionally busy for a short lab experiment.
                    }
                    WindowLabStore.recordMainThreadBusy(durationMs)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.post { recordWindowSnapshot() }
    }

    private fun recordWindowSnapshot() {
        val decor = window.decorView
        WindowLabStore.recordWindowInfo(
            WindowInfo(
                activityName = javaClass.simpleName,
                windowClass = window.javaClass.simpleName,
                decorViewClass = decor.javaClass.simpleName,
                decorSize = "${decor.width}x${decor.height}",
                rootViewClass = "ComposeView in DecorView content",
                softInputMode = "adjustResize"
            )
        )
    }
}
