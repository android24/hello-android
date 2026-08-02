package com.helloandroid.packagemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PackageLabStore.recordExternalEntry("PackageChangedReceiver", intent.action.orEmpty())
    }
}
