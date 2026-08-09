package com.helloandroid.process

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process

class IsolatedInspectorService : Service() {
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_PING -> {
                reply(msg, isolatedBundle("pong", "isolated process is alive"))
                true
            }
            MSG_CRASH -> {
                reply(msg, isolatedBundle("crash", "isolated process will kill itself"))
                Process.killProcess(Process.myPid())
                true
            }
            else -> false
        }
    }

    private val messenger = Messenger(handler)

    override fun onCreate() {
        super.onCreate()
        ProcessLabStore.recordLifecycle(
            source = "IsolatedService",
            phase = "onCreate",
            detail = "process=${currentProcessName()}, pid=${Process.myPid()}, uid=${Process.myUid()}"
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        ProcessLabStore.recordLifecycle(
            source = "IsolatedService",
            phase = "onBind",
            detail = "thread=${currentThreadName()}"
        )
        return messenger.binder
    }

    private fun reply(request: Message, data: Bundle) {
        val replyTo = request.replyTo ?: return
        val response = Message.obtain(null, MSG_REPLY).apply {
            this.data = data
        }
        runCatching { replyTo.send(response) }
    }

    private fun isolatedBundle(status: String, note: String): Bundle {
        return Bundle().apply {
            putString("status", status)
            putString("note", note)
            putString("processName", currentProcessName())
            putInt("pid", Process.myPid())
            putInt("uid", Process.myUid())
            putString("threadName", currentThreadName())
            putString("oom", readProcValue("/proc/${Process.myPid()}/oom_score_adj"))
        }
    }

    companion object {
        const val MSG_PING = 1
        const val MSG_CRASH = 2
        const val MSG_REPLY = 100
    }
}
