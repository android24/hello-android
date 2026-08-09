package com.helloandroid.process

import android.app.Application

class ProcessLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLabStore.recordLifecycle(
            source = "Application",
            phase = "onCreate",
            detail = "process=${currentProcessName()}, singleton=${ProcessSingleton.value}"
        )
    }
}
