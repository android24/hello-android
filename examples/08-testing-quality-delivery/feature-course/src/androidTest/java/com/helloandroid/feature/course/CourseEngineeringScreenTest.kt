package com.helloandroid.feature.course

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.domain.course.CourseDashboard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CourseEngineeringScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screenShowsQualityDashboardAndRefreshButton() {
        var refreshClicked = false

        composeRule.setContent {
            CourseEngineeringScreen(
                uiState = fakeUiState,
                onRefresh = { refreshClicked = true }
            )
        }

        composeRule.onNodeWithText("第 8 章质量保障演练").assertIsDisplayed()
        composeRule.onNodeWithText("通过 Hilt 刷新课程").assertIsDisplayed()
        composeRule.onNodeWithText("测试金字塔").assertIsDisplayed()

        composeRule.onNodeWithText("通过 Hilt 刷新课程").performClick()

        assertTrue(refreshClicked)
    }

    private companion object {
        val fakeUiState = CourseEngineeringUiState(
            dashboard = CourseDashboard(
                lessons = listOf(
                    CourseLesson(
                        id = "quality",
                        title = "质量体检",
                        summary = "用 Compose UI 测试保护关键用户路径。",
                        difficulty = LessonDifficulty.Intermediate,
                        isSynced = true
                    )
                ),
                networkConfig = NetworkConfig(
                    baseUrl = "https://test.local/",
                    environmentName = "test",
                    verboseLogEnabled = true
                ),
                refreshCount = 1
            ),
            message = "测试数据已经准备完成"
        )
    }
}
