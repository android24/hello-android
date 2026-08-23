package com.helloandroid.storage

import android.app.Application

class StorageAccessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        StorageLabStore.record("App", "ON_CREATE", "application started in ${currentProcessName(this)}")
    }
}
