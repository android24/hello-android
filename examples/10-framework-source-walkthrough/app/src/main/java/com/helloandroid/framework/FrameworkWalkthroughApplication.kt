package com.helloandroid.framework

import android.app.Application
import android.os.SystemClock
import android.util.Log

class FrameworkWalkthroughApplication : Application() {
    val processCreateElapsedRealtime: Long = SystemClock.elapsedRealtime()

    override fun onCreate() {
        super.onCreate()
        Log.d(
            TAG,
            "Application.onCreate thread=${Thread.currentThread().name}, processAge=0ms"
        )
    }
}

const val TAG = "FrameworkWalkthrough"
