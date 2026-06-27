package com.helloandroid.feature.course

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.domain.course.CourseDashboard
import com.helloandroid.domain.course.CourseRepository
import com.helloandroid.domain.course.GetCourseDashboardUseCase
import com.helloandroid.domain.course.RefreshCoursesUseCase
import com.helloandroid.domain.course.RefreshReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CourseEngineeringViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_observesDashboardFromRepository() = runTest {
        val repository = FakeCourseRepository()
        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value

        assertEquals(1, state.dashboard?.lessons?.size)
        assertTrue(state.message.contains("Hilt 已装配 Repository"))
    }

    @Test
    fun refresh_updatesMessageAndDashboard_whenRepositorySucceeds() = runTest {
        val repository = FakeCourseRepository()
        val viewModel = createViewModel(repository)

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(2, state.dashboard?.lessons?.size)
        assertTrue(state.message.contains("刷新完成"))
    }

    @Test
    fun refresh_showsErrorMessage_whenRepositoryFails() = runTest {
        val repository = FakeCourseRepository(shouldFail = true)
        val viewModel = createViewModel(repository)

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("network error", state.message)
    }

    private fun createViewModel(
        repository: CourseRepository
    ): CourseEngineeringViewModel {
        return CourseEngineeringViewModel(
            getCourseDashboard = GetCourseDashboardUseCase(repository),
            refreshCourses = RefreshCoursesUseCase(repository)
        )
    }

    private class FakeCourseRepository(
        private val shouldFail: Boolean = false
    ) : CourseRepository {
        private val dashboard = MutableStateFlow(
            CourseDashboard(
                lessons = listOf(seedLesson),
                networkConfig = NetworkConfig(
                    baseUrl = "https://test.local/",
                    environmentName = "test",
                    verboseLogEnabled = true
                ),
                refreshCount = 0
            )
        )

        override fun observeDashboard(): Flow<CourseDashboard> = dashboard

        override suspend fun refreshCourses(): RefreshReport {
            if (shouldFail) {
                error("network error")
            }

            dashboard.value = dashboard.value.copy(
                lessons = listOf(seedLesson, refreshedLesson),
                refreshCount = dashboard.value.refreshCount + 1
            )

            return RefreshReport(
                changedCount = 2,
                source = "fake"
            )
        }
    }

    private companion object {
        val seedLesson = CourseLesson(
            id = "seed",
            title = "初始课程",
            summary = "测试开始前已经存在的课程。",
            difficulty = LessonDifficulty.Beginner,
            isSynced = true
        )

        val refreshedLesson = CourseLesson(
            id = "refreshed",
            title = "刷新后的课程",
            summary = "点击刷新后由 Fake Repository 推入。",
            difficulty = LessonDifficulty.Advanced,
            isSynced = true
        )
    }
}
