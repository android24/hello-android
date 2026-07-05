package com.helloandroid.binder

import android.app.Application
import android.os.Process
import android.os.SystemClock
import android.util.Log

class BinderLabApplication : Application() {
    val processCreateElapsedRealtime: Long = SystemClock.elapsedRealtime()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application.onCreate pid=${Process.myPid()}, thread=${Thread.currentThread().name}")
    }
}

const val TAG = "BinderSystemLab"
