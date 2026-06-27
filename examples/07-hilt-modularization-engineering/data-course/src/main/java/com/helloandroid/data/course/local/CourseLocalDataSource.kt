package com.helloandroid.data.course.local

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class CourseLocalDataSource @Inject constructor() {
    private val lessons = MutableStateFlow(seedLessons)

    fun observeLessons(): StateFlow<List<CourseLesson>> = lessons.asStateFlow()

    fun replaceLessons(nextLessons: List<CourseLesson>) {
        lessons.value = nextLessons
    }

    private companion object {
        val seedLessons = listOf(
            CourseLesson(
                id = "architecture",
                title = "架构演进复盘",
                summary = "从 MVC、MVP、MVVM 到 MVI，理解职责为什么要分层。",
                difficulty = LessonDifficulty.Intermediate,
                isSynced = true
            ),
            CourseLesson(
                id = "async",
                title = "协程与 Flow 同步中心",
                summary = "把刷新、错误、状态流和后台任务放回同一条数据链路。",
                difficulty = LessonDifficulty.Advanced,
                isSynced = true
            ),
            CourseLesson(
                id = "engineering",
                title = "工程化升级入口",
                summary = "本地种子数据，等待 Hilt 注入远端数据源后刷新。",
                difficulty = LessonDifficulty.Advanced,
                isSynced = false
            )
        )
    }
}
