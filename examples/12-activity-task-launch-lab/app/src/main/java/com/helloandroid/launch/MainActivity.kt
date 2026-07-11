package com.helloandroid.launch

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as LaunchLabApplication
        LaunchTraceStore.recordProcess(SystemClock.elapsedRealtime() - app.processCreateElapsedRealtime)
        LaunchTraceStore.recordScreen("MainActivity")
        LaunchTraceStore.record("MainActivity.onCreate", "savedInstanceState=${savedInstanceState != null}")
        setContent {
            LaunchLabApp(
                screenTitle = "MainActivity",
                screenRole = "任务栈入口。适合观察普通启动、singleTop 和 CLEAR_TOP 回首页。",
                primaryAction = "普通启动详情页" to { openDetail() },
                secondaryAction = "打开 singleTop 页面" to { openSingleTop("MainActivity") },
                tertiaryAction = "清空轨迹" to { LaunchTraceStore.clearTrace() }
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LaunchTraceStore.recordScreen("MainActivity")
        LaunchTraceStore.recordNewIntent("MainActivity", intent?.flags ?: 0)
    }

    override fun onStart() {
        super.onStart()
        LaunchTraceStore.record("MainActivity.onStart", "首页可见")
    }

    override fun onResume() {
        super.onResume()
        LaunchTraceStore.recordScreen("MainActivity")
        LaunchTraceStore.record("MainActivity.onResume", "首页可交互")
    }

    override fun onPause() {
        LaunchTraceStore.record("MainActivity.onPause", "首页即将失去前台")
        super.onPause()
    }

    override fun onDestroy() {
        LaunchTraceStore.record("MainActivity.onDestroy", "首页销毁")
        super.onDestroy()
    }
}
