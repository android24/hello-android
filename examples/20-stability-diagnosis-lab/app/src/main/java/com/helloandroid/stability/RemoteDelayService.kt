package com.helloandroid.stability

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger

class RemoteDelayService : Service() {
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_BLOCK -> {
                val delayMs = msg.data.getLong(KEY_DELAY_MS, 8_000L)
                StabilityLabStore.record("RemoteService", "BLOCK_START", "delay=${delayMs}ms")
                Thread.sleep(delayMs)
                reply(msg, "remote delayed ${delayMs}ms")
                StabilityLabStore.record("RemoteService", "BLOCK_END", "finished")
                true
            }
            else -> false
        }
    }
    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder {
        StabilityLabStore.record("RemoteService", "ON_BIND", "process=${currentProcessName()}")
        return messenger.binder
    }

    private fun reply(request: Message, note: String) {
        val replyTo = request.replyTo ?: return
        val response = Message.obtain(null, MSG_REPLY).apply {
            data = Bundle().apply { putString("note", note) }
        }
        runCatching { replyTo.send(response) }
    }

    companion object {
        const val MSG_BLOCK = 1
        const val MSG_REPLY = 100
        const val KEY_DELAY_MS = "delay_ms"
    }
}
