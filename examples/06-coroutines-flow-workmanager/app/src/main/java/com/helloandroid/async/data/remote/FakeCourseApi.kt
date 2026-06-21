package com.helloandroid.async.data.remote

import com.helloandroid.async.data.model.SyncLesson
import java.io.IOException
import kotlinx.coroutines.delay

class FakeCourseApi {
    suspend fun fetchLessons(shouldFail: Boolean): List<SyncLesson> {
        delay(900)

        if (shouldFail) {
            throw IOException("模拟网络失败：缓存仍然保留，错误进入事件流")
        }

        return listOf(
            SyncLesson(
                id = 1,
                title = "Thread 与线程池",
                description = "理解 Java 异步模型解决过什么，又留下了哪些成本。",
                source = "Thread",
                isSynced = true
            ),
            SyncLesson(
                id = 2,
                title = "Coroutines",
                description = "用顺序代码表达异步任务，让刷新逻辑更像一条直路。",
                source = "Coroutine",
                isSynced = true
            ),
            SyncLesson(
                id = 3,
                title = "Flow 与 StateFlow",
                description = "让缓存、配置和 UI 状态像数据流一样持续更新。",
                source = "Flow",
                isSynced = false
            ),
            SyncLesson(
                id = 4,
                title = "WorkManager",
                description = "把不急但要可靠完成的任务交给系统调度。",
                source = "Worker",
                isSynced = false
            )
        )
    }
}
