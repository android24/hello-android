package com.helloandroid.process

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SlowBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 12_000L)
        Log.w(TAG, "Slow broadcast starts on ${currentThreadName()}, delayMs=$delayMs")
        ProcessLabStore.recordLifecycle(
            source = "SlowBroadcast",
            phase = "onReceive-start",
            detail = "delay=${delayMs}ms, process=${context.currentProcessName()}"
        )
        Thread.sleep(delayMs)
        ProcessLabStore.recordLifecycle(
            source = "SlowBroadcast",
            phase = "onReceive-end",
            detail = "finished after ${delayMs}ms"
        )
        Log.w(TAG, "Slow broadcast finished")
    }

    companion object {
        const val EXTRA_DELAY_MS = "delay_ms"
        private const val TAG = "ProcessZygoteLab"
    }
}
