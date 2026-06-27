package com.helloandroid.performance

import android.app.Application
import android.os.SystemClock

class PerformanceLabApplication : Application() {
    val processCreateElapsedRealtime: Long = SystemClock.elapsedRealtime()
}
