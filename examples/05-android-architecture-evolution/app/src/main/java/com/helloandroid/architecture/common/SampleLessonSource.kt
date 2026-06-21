package com.helloandroid.architecture.common

import kotlinx.coroutines.delay

class SampleLessonSource {
    suspend fun loadLessons(mode: ArchitectureMode): List<CourseLesson> {
        delay(450)

        return listOf(
            CourseLesson(
                id = 1,
                title = "为什么需要架构",
                description = "先看见 Activity 变胖的问题，再理解拆分的意义。",
                owner = mode.label,
                isCompleted = true
            ),
            CourseLesson(
                id = 2,
                title = "${mode.label} 课程列表",
                description = "同一个功能在 ${mode.label} 中的代码组织方式。",
                owner = mode.label,
                isCompleted = false
            ),
            CourseLesson(
                id = 3,
                title = "状态、事件与边界",
                description = "观察刷新、过滤、展示分别属于哪一层。",
                owner = mode.label,
                isCompleted = false
            )
        )
    }
}
