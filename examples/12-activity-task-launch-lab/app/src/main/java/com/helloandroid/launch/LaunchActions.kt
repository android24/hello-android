package com.helloandroid.launch

import android.app.Activity
import android.content.Intent

fun Activity.openDetail() {
    LaunchTraceStore.recordStandardLaunch(source = javaClass.simpleName)
    startActivity(Intent(this, DetailActivity::class.java))
}

fun Activity.openSingleTop(source: String) {
    LaunchTraceStore.recordSingleTopLaunch(source = source)
    startActivity(Intent(this, SingleTopActivity::class.java))
}

fun Activity.clearTopToMain(source: String) {
    LaunchTraceStore.recordClearTop(source = source)
    val intent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    startActivity(intent)
}
