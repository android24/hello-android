package com.helloandroid.data.course.remote

import com.helloandroid.core.model.CourseLesson

interface CourseRemoteDataSource {
    suspend fun fetchLessons(): List<CourseLesson>
}
