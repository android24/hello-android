package com.helloandroid.stability

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.currentProcessName(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return Application.getProcessName()
    }
    return runCatching {
        File("/proc/self/cmdline").readText().trim { it <= ' ' }
    }.getOrElse { packageName }
}

fun currentThreadName(): String = Thread.currentThread().name

fun now(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

fun readProcValue(path: String): String {
    return runCatching { File(path).readText().trim() }.getOrElse { "unavailable: ${it::class.java.simpleName}" }
}

fun runtimeIdentity(context: Context): RuntimeIdentity {
    val pid = Process.myPid()
    return RuntimeIdentity(
        packageName = context.packageName,
        processName = context.currentProcessName(),
        pid = pid.toString(),
        uid = Process.myUid().toString(),
        threadName = currentThreadName(),
        oomScoreAdj = readProcValue("/proc/$pid/oom_score_adj")
    )
}
