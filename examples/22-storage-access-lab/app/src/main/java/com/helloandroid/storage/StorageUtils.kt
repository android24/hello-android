package com.helloandroid.storage

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun now(): String {
    return SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
}

fun currentProcessName(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        "${context.packageName}:pid-${Process.myPid()}"
    }
}

fun runtimeSnapshot(context: Context): RuntimeSnapshot {
    val appInfo = context.applicationInfo
    return RuntimeSnapshot(
        packageName = context.packageName,
        targetSdk = appInfo.targetSdkVersion.toString(),
        sdk = Build.VERSION.SDK_INT.toString(),
        filesDir = context.filesDir.absolutePath,
        cacheDir = context.cacheDir.absolutePath,
        processName = currentProcessName(context)
    )
}
