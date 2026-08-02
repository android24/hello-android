package com.helloandroid.packagemanager

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class DeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PackageLabStore.recordExternalEntry("DeepLinkActivity", intent?.toUri(0).orEmpty())
        setContentView(
            TextView(this).apply {
                text = "DeepLinkActivity 已被 PMS 匹配并启动"
                textSize = 18f
                setPadding(36, 48, 36, 48)
            }
        )
    }
}
