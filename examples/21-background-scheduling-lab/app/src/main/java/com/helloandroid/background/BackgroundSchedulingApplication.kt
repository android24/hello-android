package com.helloandroid.background

import android.app.Application

class BackgroundSchedulingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BackgroundLabStore.record("App", "ON_CREATE", "application started in ${currentProcessName(this)}")
    }
}
