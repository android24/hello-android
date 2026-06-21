package com.helloandroid.async.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.helloandroid.async.data.legacy.LegacyAsyncPlayground
import com.helloandroid.async.data.repository.SyncRepository
import com.helloandroid.async.worker.BackgroundSyncScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SyncCenterViewModel(
    private val repository: SyncRepository,
    private val legacyAsyncPlayground: LegacyAsyncPlayground,
    private val backgroundSyncScheduler: BackgroundSyncScheduler
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)
    private val threadLog = MutableStateFlow("Thread：等待执行")
    private val executorLog = MutableStateFlow("线程池：等待执行")
    private val workStateText = backgroundSyncScheduler.observeWorkState()

    private val _events = MutableSharedFlow<SyncEvent>()
    val events: SharedFlow<SyncEvent> = _events

    val uiState: StateFlow<SyncCenterUiState> = combine(
        repository.dashboardData,
        isRefreshing,
        threadLog,
        executorLog,
        workStateText
    ) { dashboardData, refreshing, threadMessage, executorMessage, workMessage ->
        SyncCenterUiState(
            isRefreshing = refreshing,
            lessons = dashboardData.lessons,
            wifiOnly = dashboardData.preferences.wifiOnly,
            lastSyncText = dashboardData.preferences.lastSyncMillis.toSyncText(),
            threadLog = threadMessage,
            executorLog = executorMessage,
            workStateText = workMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncCenterUiState()
    )

    fun runThreadDemo() {
        threadLog.value = "Thread：后台线程正在工作..."
        legacyAsyncPlayground.runThreadDemo { message ->
            threadLog.value = message
        }
    }

    fun runExecutorDemo() {
        executorLog.value = "线程池：3 个任务已提交"
        legacyAsyncPlayground.runExecutorDemo { message ->
            executorLog.update { old -> "$old\n$message" }
        }
    }

    fun refreshLessons(shouldFail: Boolean = false) {
        viewModelScope.launch {
            isRefreshing.value = true

            runCatching {
                withTimeout(2_500) {
                    repository.refreshLessons(shouldFail = shouldFail)
                }
            }.onSuccess {
                _events.emit(SyncEvent.ShowMessage("协程刷新完成，Flow 已推送新状态"))
            }.onFailure { throwable ->
                _events.emit(
                    SyncEvent.ShowMessage(
                        throwable.message ?: "刷新失败，旧缓存仍然保留"
                    )
                )
            }

            isRefreshing.value = false
        }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        repository.setWifiOnly(wifiOnly)
        viewModelScope.launch {
            _events.emit(
                SyncEvent.ShowMessage(
                    if (wifiOnly) {
                        "配置已切换：只在 Wi-Fi 下同步"
                    } else {
                        "配置已切换：允许任意网络同步"
                    }
                )
            )
        }
    }

    fun startBackgroundSync() {
        backgroundSyncScheduler.enqueueOneTimeSync()
        viewModelScope.launch {
            _events.emit(SyncEvent.ShowMessage("WorkManager 任务已入队"))
        }
    }

    private fun Long?.toSyncText(): String {
        if (this == null) {
            return "尚未同步"
        }

        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return "最近同步：${formatter.format(Date(this))}"
    }
}

class SyncCenterViewModelFactory(
    private val repository: SyncRepository,
    private val legacyAsyncPlayground: LegacyAsyncPlayground,
    private val backgroundSyncScheduler: BackgroundSyncScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncCenterViewModel(
            repository = repository,
            legacyAsyncPlayground = legacyAsyncPlayground,
            backgroundSyncScheduler = backgroundSyncScheduler
        ) as T
    }
}
