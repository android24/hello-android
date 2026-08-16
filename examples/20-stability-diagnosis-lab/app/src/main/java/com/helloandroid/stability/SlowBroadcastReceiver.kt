package com.helloandroid.stability

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SlowBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 12_000L)
        StabilityLabStore.record("Broadcast", "ON_RECEIVE_START", "delay=${delayMs}ms")
        Thread.sleep(delayMs)
        StabilityLabStore.record("Broadcast", "ON_RECEIVE_END", "finished")
    }

    companion object {
        const val EXTRA_DELAY_MS = "delay_ms"
    }
}
