package com.helloandroid.domain.course

import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeDashboard(): Flow<CourseDashboard>

    suspend fun refreshCourses(): RefreshReport
}
