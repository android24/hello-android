package com.helloandroid.performance

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: PerformanceLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as PerformanceLabApplication
        viewModel.recordFirstActivityCreate(
            elapsedFromProcessStart = SystemClock.elapsedRealtime() - app.processCreateElapsedRealtime,
            buildProfile = BuildConfig.BUILD_PROFILE,
            verboseLogEnabled = BuildConfig.ENABLE_VERBOSE_LOG
        )

        setContent {
            PerformanceLabApp(viewModel = viewModel)
        }
    }
}
