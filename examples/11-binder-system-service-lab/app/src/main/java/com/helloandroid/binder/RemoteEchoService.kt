package com.helloandroid.binder

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.SystemClock
import android.util.Log

class RemoteEchoService : Service() {
    private val incomingMessenger = Messenger(IncomingHandler())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RemoteEchoService.onCreate pid=${Process.myPid()}, thread=${Thread.currentThread().name}")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "RemoteEchoService.onBind pid=${Process.myPid()}")
        return incomingMessenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "RemoteEchoService.onUnbind pid=${Process.myPid()}")
        return super.onUnbind(intent)
    }

    private class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what != MSG_ECHO_REQUEST) {
                super.handleMessage(msg)
                return
            }

            val requestId = msg.data.getInt(KEY_REQUEST_ID)
            val payload = msg.data.getString(KEY_PAYLOAD).orEmpty()
            val sentAt = msg.data.getLong(KEY_SENT_AT)
            val handledAt = SystemClock.uptimeMillis()
            val reply = Message.obtain(null, MSG_ECHO_REPLY).apply {
                data = Bundle().apply {
                    putInt(KEY_REQUEST_ID, requestId)
                    putString(KEY_PAYLOAD, payload)
                    putInt(KEY_REMOTE_PID, Process.myPid())
                    putString(KEY_REMOTE_THREAD, Thread.currentThread().name)
                    putLong(KEY_SENT_AT, sentAt)
                    putLong(KEY_HANDLED_AT, handledAt)
                }
            }

            Log.d(TAG, "RemoteEchoService.handleMessage requestId=$requestId, payload=$payload")
            msg.replyTo?.send(reply)
        }
    }

    companion object {
        const val MSG_ECHO_REQUEST = 1001
        const val MSG_ECHO_REPLY = 1002
        const val KEY_REQUEST_ID = "request_id"
        const val KEY_PAYLOAD = "payload"
        const val KEY_SENT_AT = "sent_at"
        const val KEY_HANDLED_AT = "handled_at"
        const val KEY_REMOTE_PID = "remote_pid"
        const val KEY_REMOTE_THREAD = "remote_thread"
    }
}
