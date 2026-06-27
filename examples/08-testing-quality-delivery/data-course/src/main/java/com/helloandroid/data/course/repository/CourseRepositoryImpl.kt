package com.helloandroid.data.course.repository

import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.data.course.local.CourseLocalDataSource
import com.helloandroid.data.course.remote.CourseRemoteDataSource
import com.helloandroid.domain.course.CourseDashboard
import com.helloandroid.domain.course.CourseRepository
import com.helloandroid.domain.course.RefreshReport
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val localDataSource: CourseLocalDataSource,
    private val remoteDataSource: CourseRemoteDataSource,
    private val networkConfig: NetworkConfig
) : CourseRepository {
    private val refreshCount = MutableStateFlow(0)

    override fun observeDashboard() = combine(
        localDataSource.observeLessons(),
        refreshCount
    ) { lessons, count ->
        CourseDashboard(
            lessons = lessons,
            networkConfig = networkConfig,
            refreshCount = count
        )
    }

    override suspend fun refreshCourses(): RefreshReport {
        val remoteLessons = remoteDataSource.fetchLessons()
        localDataSource.replaceLessons(remoteLessons)
        refreshCount.value = refreshCount.value + 1
        return RefreshReport(
            changedCount = remoteLessons.size,
            source = networkConfig.baseUrl
        )
    }
}
