package com.helloandroid.async.data.preferences

data class SyncPreferences(
    val wifiOnly: Boolean = false,
    val lastSyncMillis: Long? = null
)
