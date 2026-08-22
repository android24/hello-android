package com.helloandroid.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class CourseDownloadForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    private val progressTask = object : Runnable {
        override fun run() {
            progress += 20
            BackgroundLabStore.record("FGS", "PROGRESS", "offline course download $progress%")
            if (progress < 100) {
                handler.postDelayed(this, 1_000L)
            } else {
                BackgroundLabStore.record("FGS", "DONE", "download finished, stop foreground service")
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForeground(NOTIFICATION_ID, notification("离线课程下载中", "正在观察前台服务生命周期"))
        BackgroundLabStore.record("FGS", "ON_CREATE", "startForeground called")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        progress = 0
        BackgroundLabStore.record("FGS", "ON_START_COMMAND", "startId=$startId")
        handler.removeCallbacks(progressTask)
        handler.post(progressTask)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressTask)
        BackgroundLabStore.record("FGS", "ON_DESTROY", "foreground service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "后台调度实验",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun notification(title: String, text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "background_scheduling_lab"
        private const val NOTIFICATION_ID = 2101
    }
}
