package com.helloandroid.process

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SlowStartService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delayMs = intent?.getLongExtra(EXTRA_DELAY_MS, 22_000L) ?: 22_000L
        Log.w(TAG, "Slow service starts on ${currentThreadName()}, delayMs=$delayMs")
        ProcessLabStore.recordLifecycle(
            source = "SlowService",
            phase = "onStartCommand-start",
            detail = "delay=${delayMs}ms, process=${currentProcessName()}"
        )
        Thread.sleep(delayMs)
        ProcessLabStore.recordLifecycle(
            source = "SlowService",
            phase = "onStartCommand-end",
            detail = "finished after ${delayMs}ms"
        )
        stopSelf(startId)
        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_DELAY_MS = "delay_ms"
        private const val TAG = "ProcessZygoteLab"
    }
}
