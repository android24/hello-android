package com.helloandroid.architecture.clean.data

import com.helloandroid.architecture.clean.domain.CleanCourseLesson
import com.helloandroid.architecture.clean.domain.CourseRepository
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.SampleLessonSource

class SampleCourseRepository(
    private val source: SampleLessonSource
) : CourseRepository {
    override suspend fun getLessons(): List<CleanCourseLesson> {
        return source.loadLessons(ArchitectureMode.CLEAN).map { lesson ->
            CleanCourseLesson(
                id = lesson.id,
                title = lesson.title,
                description = lesson.description,
                isCompleted = lesson.isCompleted
            )
        }
    }
}
