package com.helloandroid.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LaunchTraceStore.recordScreen("DetailActivity")
        LaunchTraceStore.record("DetailActivity.onCreate", "standard 模式详情页创建")
        setContent {
            LaunchLabApp(
                screenTitle = "DetailActivity",
                screenRole = "普通详情页。每次普通启动通常会创建新实例。",
                primaryAction = "再开一个详情页" to { openDetail() },
                secondaryAction = "打开 singleTop 页面" to { openSingleTop("DetailActivity") },
                tertiaryAction = "CLEAR_TOP 回首页" to { clearTopToMain("DetailActivity") }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        LaunchTraceStore.record("DetailActivity.onStart", "详情页可见")
    }

    override fun onResume() {
        super.onResume()
        LaunchTraceStore.recordScreen("DetailActivity")
        LaunchTraceStore.record("DetailActivity.onResume", "详情页可交互")
    }

    override fun onPause() {
        LaunchTraceStore.record("DetailActivity.onPause", "详情页即将失去前台")
        super.onPause()
    }

    override fun onDestroy() {
        LaunchTraceStore.record("DetailActivity.onDestroy", "详情页销毁")
        super.onDestroy()
    }
}
