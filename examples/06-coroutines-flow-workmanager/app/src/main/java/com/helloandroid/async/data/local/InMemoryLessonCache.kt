package com.helloandroid.async.data.local

import com.helloandroid.async.data.model.SyncLesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemoryLessonCache {
    private val lessons = MutableStateFlow<List<SyncLesson>>(emptyList())

    fun observeLessons(): StateFlow<List<SyncLesson>> = lessons

    fun replaceLessons(newLessons: List<SyncLesson>) {
        lessons.value = newLessons
    }
}
