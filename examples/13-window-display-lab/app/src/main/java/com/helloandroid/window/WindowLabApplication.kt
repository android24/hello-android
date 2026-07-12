package com.helloandroid.window

import android.app.Application
import android.os.Process
import android.util.Log

class WindowLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application.onCreate pid=${Process.myPid()}, thread=${Thread.currentThread().name}")
    }
}

const val TAG = "WindowDisplayLab"
