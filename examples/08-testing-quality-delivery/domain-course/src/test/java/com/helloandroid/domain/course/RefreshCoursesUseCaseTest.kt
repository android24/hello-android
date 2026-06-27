package com.helloandroid.domain.course

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RefreshCoursesUseCaseTest {
    @Test
    fun refreshCourses_returnsReport_whenRepositorySucceeds() = runTest {
        val repository = FakeCourseRepository()
        val useCase = RefreshCoursesUseCase(repository)

        val report = useCase()

        assertTrue(repository.refreshCalled)
        assertEquals(3, report.changedCount)
        assertEquals("fake", report.source)
    }

    @Test
    fun refreshCourses_propagatesError_whenRepositoryFails() = runTest {
        val repository = FakeCourseRepository(shouldFail = true)
        val useCase = RefreshCoursesUseCase(repository)

        try {
            useCase()
            fail("Expected refreshCourses to throw an error.")
        } catch (error: IllegalStateException) {
            assertEquals("network error", error.message)
        }
    }

    private class FakeCourseRepository(
        private val shouldFail: Boolean = false
    ) : CourseRepository {
        var refreshCalled = false

        override fun observeDashboard(): Flow<CourseDashboard> {
            return flowOf(
                CourseDashboard(
                    lessons = listOf(
                        CourseLesson(
                            id = "testing",
                            title = "测试保护业务规则",
                            summary = "UseCase 测试不需要真实网络。",
                            difficulty = LessonDifficulty.Intermediate,
                            isSynced = true
                        )
                    ),
                    networkConfig = NetworkConfig(
                        baseUrl = "https://fake.local/",
                        environmentName = "test",
                        verboseLogEnabled = true
                    ),
                    refreshCount = 0
                )
            )
        }

        override suspend fun refreshCourses(): RefreshReport {
            refreshCalled = true
            if (shouldFail) {
                error("network error")
            }
            return RefreshReport(
                changedCount = 3,
                source = "fake"
            )
        }
    }
}
