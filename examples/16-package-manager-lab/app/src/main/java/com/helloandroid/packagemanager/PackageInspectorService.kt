package com.helloandroid.packagemanager

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PackageInspectorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
