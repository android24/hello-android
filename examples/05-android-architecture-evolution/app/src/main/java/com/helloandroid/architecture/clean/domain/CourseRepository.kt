package com.helloandroid.architecture.clean.domain

interface CourseRepository {
    suspend fun getLessons(): List<CleanCourseLesson>
}
