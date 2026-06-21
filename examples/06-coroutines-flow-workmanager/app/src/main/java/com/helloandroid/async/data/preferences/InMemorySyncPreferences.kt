package com.helloandroid.async.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemorySyncPreferences {
    private val preferences = MutableStateFlow(SyncPreferences())

    fun observePreferences(): StateFlow<SyncPreferences> = preferences

    fun setWifiOnly(wifiOnly: Boolean) {
        preferences.update { it.copy(wifiOnly = wifiOnly) }
    }

    fun markSyncedAt(timeMillis: Long) {
        preferences.update { it.copy(lastSyncMillis = timeMillis) }
    }
}
