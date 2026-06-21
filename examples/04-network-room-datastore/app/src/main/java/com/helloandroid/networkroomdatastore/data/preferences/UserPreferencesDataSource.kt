package com.helloandroid.networkroomdatastore.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(
    private val context: Context
) {
    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { preferences ->
        UserPreferences(
            showCompleted = preferences[SHOW_COMPLETED_KEY] ?: true,
            lastSyncMillis = preferences[LAST_SYNC_MILLIS_KEY]
        )
    }

    suspend fun setShowCompleted(showCompleted: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SHOW_COMPLETED_KEY] = showCompleted
        }
    }

    suspend fun setLastSyncMillis(lastSyncMillis: Long) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[LAST_SYNC_MILLIS_KEY] = lastSyncMillis
        }
    }

    private companion object {
        val SHOW_COMPLETED_KEY = booleanPreferencesKey("show_completed")
        val LAST_SYNC_MILLIS_KEY = longPreferencesKey("last_sync_millis")
    }
}
