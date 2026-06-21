package com.helloandroid.async.data.repository

import com.helloandroid.async.data.model.SyncLesson
import com.helloandroid.async.data.preferences.SyncPreferences

data class SyncDashboardData(
    val lessons: List<SyncLesson>,
    val preferences: SyncPreferences
)
