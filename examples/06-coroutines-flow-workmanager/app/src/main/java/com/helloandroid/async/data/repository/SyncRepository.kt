package com.helloandroid.async.data.repository

import com.helloandroid.async.data.local.InMemoryLessonCache
import com.helloandroid.async.data.preferences.InMemorySyncPreferences
import com.helloandroid.async.data.remote.FakeCourseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class SyncRepository(
    private val api: FakeCourseApi,
    private val cache: InMemoryLessonCache,
    private val preferences: InMemorySyncPreferences
) {
    val dashboardData: Flow<SyncDashboardData> = combine(
        cache.observeLessons(),
        preferences.observePreferences()
    ) { lessons, preferences ->
        SyncDashboardData(
            lessons = lessons,
            preferences = preferences
        )
    }

    suspend fun refreshLessons(shouldFail: Boolean) = withContext(Dispatchers.IO) {
        val lessons = api.fetchLessons(shouldFail = shouldFail)
        cache.replaceLessons(lessons)
        preferences.markSyncedAt(System.currentTimeMillis())
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        preferences.setWifiOnly(wifiOnly)
    }
}
