package com.helloandroid.networkroomdatastore.data.preferences

data class UserPreferences(
    val showCompleted: Boolean = true,
    val lastSyncMillis: Long? = null
)
