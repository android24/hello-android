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
import android.util.Log

class RemoteInspectorService : Service() {
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_PING -> {
                reply(msg, remoteBundle("pong", "remote process is alive"))
                true
            }
            MSG_MUTATE_SINGLETON -> {
                ProcessSingleton.value += 1
                reply(msg, remoteBundle("mutated", "remote singleton=${ProcessSingleton.value}"))
                true
            }
            MSG_BLOCK_REMOTE -> {
                val delayMs = msg.data.getLong(KEY_DELAY_MS, 8_000L)
                Log.w(TAG, "Remote main thread blocking for $delayMs ms")
                Thread.sleep(delayMs)
                reply(msg, remoteBundle("blocked", "remote blocked ${delayMs}ms"))
                true
            }
            MSG_CRASH_REMOTE -> {
                reply(msg, remoteBundle("crash", "remote will kill itself"))
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
            source = "RemoteService",
            phase = "onCreate",
            detail = "process=${currentProcessName()}, pid=${Process.myPid()}"
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        ProcessLabStore.recordLifecycle(
            source = "RemoteService",
            phase = "onBind",
            detail = "thread=${currentThreadName()}, singleton=${ProcessSingleton.value}"
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

    private fun remoteBundle(status: String, note: String): Bundle {
        return Bundle().apply {
            putString("status", status)
            putString("note", note)
            putString("processName", currentProcessName())
            putInt("pid", Process.myPid())
            putInt("uid", Process.myUid())
            putString("threadName", currentThreadName())
            putString("singleton", "${ProcessSingleton.value} createdAt=${ProcessSingleton.createdAt}")
            putString("oom", readProcValue("/proc/${Process.myPid()}/oom_score_adj"))
        }
    }

    companion object {
        const val MSG_PING = 1
        const val MSG_MUTATE_SINGLETON = 2
        const val MSG_BLOCK_REMOTE = 3
        const val MSG_CRASH_REMOTE = 4
        const val MSG_REPLY = 100
        const val KEY_DELAY_MS = "delay_ms"
        private const val TAG = "ProcessZygoteLab"
    }
}
