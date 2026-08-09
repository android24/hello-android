package com.helloandroid.process

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProcessSingleton {
    val createdAt: String = now()
    var value: Int = (System.nanoTime() % 10000).toInt()
}

object ProcessSessionDraft {
    var memoryDraft: Int = 0
}

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

fun readUidLine(): String {
    return runCatching {
        File("/proc/${Process.myPid()}/status")
            .readLines()
            .firstOrNull { it.startsWith("Uid:") }
            .orEmpty()
    }.getOrElse { "unavailable: ${it::class.java.simpleName}" }
}

fun readThreadCountLine(): String {
    return runCatching {
        File("/proc/${Process.myPid()}/status")
            .readLines()
            .firstOrNull { it.startsWith("Threads:") }
            .orEmpty()
    }.getOrElse { "unavailable: ${it::class.java.simpleName}" }
}
