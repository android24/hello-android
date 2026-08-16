package com.helloandroid.stability

import android.app.Application
import android.util.Log

class StabilityLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("StabilityLab", "Uncaught on ${thread.name}: ${throwable::class.java.simpleName}", throwable)
            StabilityLabStore.record(
                source = "Crash",
                signal = "UNCAUGHT",
                detail = "${thread.name}: ${throwable::class.java.simpleName}"
            )
            previousHandler?.uncaughtException(thread, throwable)
        }
        StabilityLabStore.record("Application", "ON_CREATE", "process=${currentProcessName()}")
    }
}
