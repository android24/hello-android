package com.helloandroid.architecture.mvc

import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.CourseLesson
import com.helloandroid.architecture.common.SampleLessonSource

class CourseCatalogModel(
    private val source: SampleLessonSource
) {
    suspend fun loadLessons(): List<CourseLesson> {
        return source.loadLessons(ArchitectureMode.MVC)
    }
}
