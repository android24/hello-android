package com.helloandroid.data.course.remote

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import javax.inject.Inject
import kotlinx.coroutines.delay

class FakeCourseApi @Inject constructor(
    private val networkConfig: NetworkConfig
) {
    suspend fun requestLessons(): List<CourseLesson> {
        delay(450)

        return listOf(
            CourseLesson(
                id = "butter-knife",
                title = "Butter Knife 历史支线",
                summary = "读懂老项目里的 View 绑定注解，也知道新项目为什么转向 View Binding 与 Compose。",
                difficulty = LessonDifficulty.Beginner,
                isSynced = true
            ),
            CourseLesson(
                id = "hilt-chain",
                title = "Hilt 注入链路",
                summary = "从 ViewModel 到 UseCase、Repository、DataSource，观察对象如何被自动装配。",
                difficulty = LessonDifficulty.Intermediate,
                isSynced = true
            ),
            CourseLesson(
                id = "modules",
                title = "模块边界体检",
                summary = "把通用模型、网络配置、业务接口、数据实现和页面拆到合适模块。",
                difficulty = LessonDifficulty.Advanced,
                isSynced = networkConfig.environmentName == "prod"
            ),
            CourseLesson(
                id = "flavors",
                title = "Gradle 环境配置",
                summary = "当前数据来自 ${networkConfig.badge}，baseUrl 为 ${networkConfig.baseUrl}。",
                difficulty = LessonDifficulty.Advanced,
                isSynced = networkConfig.verboseLogEnabled
            )
        )
    }
}
