package com.helloandroid.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class CourseProgressSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        BackgroundLabStore.updateWorkState(
            WorkInfo.State.RUNNING,
            "Worker 开始执行：模拟上传学习进度。"
        )
        delay(1_500L)
        BackgroundLabStore.updateWorkState(
            WorkInfo.State.SUCCEEDED,
            "Worker 执行成功：可靠任务完成，下一步看报告和回归。"
        )
        return Result.success()
    }
}
