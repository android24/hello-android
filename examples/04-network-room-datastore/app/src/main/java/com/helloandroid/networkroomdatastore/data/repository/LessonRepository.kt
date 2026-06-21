package com.helloandroid.networkroomdatastore.data.repository

import com.helloandroid.networkroomdatastore.data.local.LessonDao
import com.helloandroid.networkroomdatastore.data.preferences.UserPreferences
import com.helloandroid.networkroomdatastore.data.preferences.UserPreferencesDataSource
import com.helloandroid.networkroomdatastore.data.remote.FakeLessonApi
import com.helloandroid.networkroomdatastore.ui.LessonUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepository(
    private val api: FakeLessonApi,
    private val lessonDao: LessonDao,
    private val preferencesDataSource: UserPreferencesDataSource
) {
    val lessons: Flow<List<LessonUiModel>> = lessonDao.observeLessons()
        .map { entities -> entities.map { it.toUiModel() } }

    val preferences: Flow<UserPreferences> = preferencesDataSource.preferences

    suspend fun refreshLessons(shouldFail: Boolean) {
        val now = System.currentTimeMillis()
        val remoteLessons = api.getLessons(shouldFail = shouldFail)

        lessonDao.clearLessons()
        lessonDao.insertLessons(remoteLessons.map { lesson -> lesson.toEntity(updatedAtMillis = now) })
        preferencesDataSource.setLastSyncMillis(now)
    }

    suspend fun setShowCompleted(showCompleted: Boolean) {
        preferencesDataSource.setShowCompleted(showCompleted)
    }
}
