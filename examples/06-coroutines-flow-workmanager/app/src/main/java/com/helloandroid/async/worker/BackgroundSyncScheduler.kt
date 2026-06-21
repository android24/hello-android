package com.helloandroid.async.worker

import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val UNIQUE_SYNC_WORK_NAME = "course_sync_once"

class BackgroundSyncScheduler(
    private val workManager: WorkManager
) {
    fun enqueueOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncCourseWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun observeWorkState(): Flow<String> {
        return workManager.getWorkInfosForUniqueWorkLiveData(UNIQUE_SYNC_WORK_NAME)
            .asFlow()
            .map { infos ->
                infos.firstOrNull()?.state.toDisplayText()
            }
    }

    private fun WorkInfo.State?.toDisplayText(): String {
        return when (this) {
            WorkInfo.State.ENQUEUED -> "后台同步：等待网络与系统调度"
            WorkInfo.State.RUNNING -> "后台同步：Worker 正在执行"
            WorkInfo.State.SUCCEEDED -> "后台同步：已完成"
            WorkInfo.State.FAILED -> "后台同步：失败"
            WorkInfo.State.BLOCKED -> "后台同步：等待前置任务"
            WorkInfo.State.CANCELLED -> "后台同步：已取消"
            null -> "后台同步：尚未入队"
        }
    }
}
