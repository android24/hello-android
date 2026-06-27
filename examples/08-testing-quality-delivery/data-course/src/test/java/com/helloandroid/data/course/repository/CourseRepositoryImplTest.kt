package com.helloandroid.data.course.repository

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.data.course.local.CourseLocalDataSource
import com.helloandroid.data.course.remote.CourseRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseRepositoryImplTest {
    @Test
    fun observeDashboard_emitsSeedLessons_beforeRefresh() = runTest {
        val repository = createRepository(
            remoteDataSource = ScriptedCourseRemoteDataSource(remoteLessons)
        )

        val dashboard = repository.observeDashboard().first()

        assertEquals(3, dashboard.lessons.size)
        assertEquals("test", dashboard.networkConfig.environmentName)
        assertEquals(0, dashboard.refreshCount)
    }

    @Test
    fun refreshCourses_replacesLocalLessonsAndIncrementsRefreshCount() = runTest {
        val repository = createRepository(
            remoteDataSource = ScriptedCourseRemoteDataSource(remoteLessons)
        )

        val report = repository.refreshCourses()
        val dashboard = repository.observeDashboard().first()

        assertEquals(2, report.changedCount)
        assertEquals("https://test.local/", report.source)
        assertEquals(2, dashboard.lessons.size)
        assertEquals(1, dashboard.refreshCount)
        assertTrue(dashboard.lessons.all { it.isSynced })
    }

    private fun createRepository(
        remoteDataSource: CourseRemoteDataSource
    ): CourseRepositoryImpl {
        return CourseRepositoryImpl(
            localDataSource = CourseLocalDataSource(),
            remoteDataSource = remoteDataSource,
            networkConfig = NetworkConfig(
                baseUrl = "https://test.local/",
                environmentName = "test",
                verboseLogEnabled = true
            )
        )
    }

    private class ScriptedCourseRemoteDataSource(
        private val lessons: List<CourseLesson>
    ) : CourseRemoteDataSource {
        override suspend fun fetchLessons(): List<CourseLesson> = lessons
    }

    private companion object {
        val remoteLessons = listOf(
            CourseLesson(
                id = "unit-test",
                title = "单元测试",
                summary = "用稳定输入保护业务规则。",
                difficulty = LessonDifficulty.Beginner,
                isSynced = true
            ),
            CourseLesson(
                id = "viewmodel-test",
                title = "ViewModel 状态测试",
                summary = "观察刷新前后的 UI State。",
                difficulty = LessonDifficulty.Intermediate,
                isSynced = true
            )
        )
    }
}
