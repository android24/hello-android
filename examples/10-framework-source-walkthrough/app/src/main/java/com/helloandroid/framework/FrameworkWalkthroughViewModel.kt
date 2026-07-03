package com.helloandroid.framework

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FrameworkWalkthroughViewModel : ViewModel() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _uiState = MutableStateFlow(FrameworkWalkthroughState())
    val uiState: StateFlow<FrameworkWalkthroughState> = _uiState

    fun recordProcessSnapshot(
        elapsedFromProcessStart: Long,
        activityContextName: String,
        applicationContextName: String
    ) {
        _uiState.update { state ->
            state.copy(
                processInfo = ProcessInfo(
                    pid = Process.myPid(),
                    threadName = Thread.currentThread().name,
                    mainLooperThread = Looper.getMainLooper().thread.name,
                    isMainLooper = Looper.myLooper() == Looper.getMainLooper(),
                    sdkInt = Build.VERSION.SDK_INT,
                    elapsedFromProcessStart = elapsedFromProcessStart,
                    activityContextName = activityContextName,
                    applicationContextName = applicationContextName
                )
            )
        }
    }

    fun recordLifecycle(name: String, detail: String) {
        addTrace(title = name, detail = detail)
    }

    fun sendHandlerMessage() {
        val postedAt = SystemClock.uptimeMillis()
        addTrace(
            title = "Handler.post",
            detail = "Message 已投递到主线程队列，等待 Looper 取出"
        )

        mainHandler.post {
            val waitTime = SystemClock.uptimeMillis() - postedAt
            val report = buildString {
                append("Runnable 在 ${Thread.currentThread().name} 线程执行")
                append("，等待 ${waitTime}ms")
                append("，当前 Looper 是主 Looper：${Looper.myLooper() == Looper.getMainLooper()}")
            }

            _uiState.update { state ->
                state.copy(handlerReport = report)
            }
            addTrace(title = "Handler dispatch", detail = report)
        }
    }

    fun clearTrace() {
        _uiState.update {
            it.copy(
                lifecycleEvents = emptyList(),
                handlerReport = "轨迹已清空。再次点击按钮，重新观察 Handler 消息。"
            )
        }
        Log.d(TAG, "Trace cleared")
    }

    private fun addTrace(title: String, detail: String) {
        val event = TraceEvent(
            title = title,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        Log.d(TAG, "$title: $detail")
        _uiState.update { state ->
            state.copy(lifecycleEvents = (listOf(event) + state.lifecycleEvents).take(12))
        }
    }
}
