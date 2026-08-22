package com.helloandroid.background

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun now(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

fun currentProcessName(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return ApplicationProcessNameHolder.name ?: ApplicationProcessNameHolder.resolve()
    }
    val pid = Process.myPid()
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.runningAppProcesses
        ?.firstOrNull { it.pid == pid }
        ?.processName
        ?: context.packageName
}

object ApplicationProcessNameHolder {
    var name: String? = null

    fun resolve(): String {
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            "-"
        }
        name = resolved
        return resolved
    }
}

fun runtimeSnapshot(context: Context): RuntimeSnapshot {
    return RuntimeSnapshot(
        packageName = context.packageName,
        processName = currentProcessName(context),
        pid = Process.myPid().toString(),
        uid = Process.myUid().toString(),
        threadName = Thread.currentThread().name,
        sdk = Build.VERSION.SDK_INT.toString()
    )
}
