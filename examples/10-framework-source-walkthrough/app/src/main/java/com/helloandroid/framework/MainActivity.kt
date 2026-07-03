package com.helloandroid.framework

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val viewModel: FrameworkWalkthroughViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as FrameworkWalkthroughApplication
        val elapsedFromProcessStart = SystemClock.elapsedRealtime() - app.processCreateElapsedRealtime

        viewModel.recordProcessSnapshot(
            elapsedFromProcessStart = elapsedFromProcessStart,
            activityContextName = this::class.java.simpleName,
            applicationContextName = applicationContext::class.java.simpleName
        )
        viewModel.recordLifecycle(
            name = "Activity.onCreate",
            detail = "savedInstanceState=${savedInstanceState != null}, processAge=${elapsedFromProcessStart}ms"
        )

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1F7A68),
                    secondary = Color(0xFF7D5A2A),
                    tertiary = Color(0xFF315F8C),
                    surface = Color.White,
                    background = Color(0xFFF6F8F7)
                )
            ) {
                FrameworkWalkthroughRoute(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.recordLifecycle("Activity.onStart", "Activity 已进入可见阶段")
    }

    override fun onResume() {
        super.onResume()
        viewModel.recordLifecycle("Activity.onResume", "Activity 已进入前台可交互阶段")
    }

    override fun onPause() {
        viewModel.recordLifecycle("Activity.onPause", "Activity 即将失去前台交互")
        super.onPause()
    }

    override fun onStop() {
        viewModel.recordLifecycle("Activity.onStop", "Activity 不再可见")
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.recordLifecycle("Activity.onDestroy", "Activity 即将销毁")
        super.onDestroy()
    }
}
