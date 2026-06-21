package com.helloandroid.networkroomdatastore.data.remote

import java.io.IOException
import kotlinx.coroutines.delay

class FakeLessonApi {
    suspend fun getLessons(shouldFail: Boolean): List<LessonDto> {
        delay(900)

        if (shouldFail) {
            throw IOException("模拟网络失败：请检查缓存是否仍然展示")
        }

        return listOf(
            LessonDto(
                id = 1,
                title = "网络请求基础",
                description = "理解接口、JSON、DTO 和加载状态。",
                level = "基础",
                isCompleted = true
            ),
            LessonDto(
                id = 2,
                title = "Room 本地数据库",
                description = "用 Entity、Dao、Database 保存结构化缓存。",
                level = "核心",
                isCompleted = false
            ),
            LessonDto(
                id = 3,
                title = "DataStore 配置存储",
                description = "保存轻量偏好，例如是否显示已完成课程。",
                level = "核心",
                isCompleted = false
            ),
            LessonDto(
                id = 4,
                title = "离线可用课程列表",
                description = "组合网络、Room、DataStore，完成一条真实数据链路。",
                level = "综合",
                isCompleted = false
            )
        )
    }
}
