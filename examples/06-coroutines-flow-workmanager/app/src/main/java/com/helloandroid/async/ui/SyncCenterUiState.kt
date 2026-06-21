package com.helloandroid.async.ui

import com.helloandroid.async.data.model.SyncLesson

data class SyncCenterUiState(
    val isRefreshing: Boolean = false,
    val lessons: List<SyncLesson> = emptyList(),
    val wifiOnly: Boolean = false,
    val lastSyncText: String = "尚未同步",
    val threadLog: String = "Thread：等待执行",
    val executorLog: String = "线程池：等待执行",
    val workStateText: String = "后台同步：尚未入队"
)
