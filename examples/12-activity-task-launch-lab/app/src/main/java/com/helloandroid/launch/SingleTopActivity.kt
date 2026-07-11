package com.helloandroid.launch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SingleTopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LaunchTraceStore.recordScreen("SingleTopActivity")
        LaunchTraceStore.record("SingleTopActivity.onCreate", "singleTop 页面创建")
        setContent {
            LaunchLabApp(
                screenTitle = "SingleTopActivity",
                screenRole = "singleTop 观察页。再次启动栈顶自己时，应重点观察 onNewIntent。",
                primaryAction = "再次启动自己" to { openSingleTop("SingleTopActivity") },
                secondaryAction = "普通启动详情页" to { openDetail() },
                tertiaryAction = "CLEAR_TOP 回首页" to { clearTopToMain("SingleTopActivity") }
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LaunchTraceStore.recordScreen("SingleTopActivity")
        LaunchTraceStore.recordNewIntent("SingleTopActivity", intent?.flags ?: 0)
    }

    override fun onStart() {
        super.onStart()
        LaunchTraceStore.record("SingleTopActivity.onStart", "singleTop 页面可见")
    }

    override fun onResume() {
        super.onResume()
        LaunchTraceStore.recordScreen("SingleTopActivity")
        LaunchTraceStore.record("SingleTopActivity.onResume", "singleTop 页面可交互")
    }

    override fun onPause() {
        LaunchTraceStore.record("SingleTopActivity.onPause", "singleTop 页面即将失去前台")
        super.onPause()
    }

    override fun onDestroy() {
        LaunchTraceStore.record("SingleTopActivity.onDestroy", "singleTop 页面销毁")
        super.onDestroy()
    }
}
