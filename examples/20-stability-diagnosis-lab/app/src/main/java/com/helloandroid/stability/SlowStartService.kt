package com.helloandroid.stability

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SlowStartService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delayMs = intent?.getLongExtra(EXTRA_DELAY_MS, 16_000L) ?: 16_000L
        StabilityLabStore.record("Service", "START_COMMAND_BEGIN", "delay=${delayMs}ms")
        Thread.sleep(delayMs)
        StabilityLabStore.record("Service", "START_COMMAND_END", "finished")
        stopSelf(startId)
        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_DELAY_MS = "delay_ms"
    }
}
