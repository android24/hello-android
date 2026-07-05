package com.helloandroid.binder

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

class BinderLabViewModel : ViewModel() {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _uiState = MutableStateFlow(BinderLabState())
    val uiState: StateFlow<BinderLabState> = _uiState

    fun recordProcessSnapshot(processAgeMs: Long, packageName: String) {
        _uiState.update { state ->
            state.copy(
                processInfo = BinderProcessInfo(
                    pid = Process.myPid(),
                    threadName = Thread.currentThread().name,
                    processAgeMs = processAgeMs,
                    packageName = packageName
                )
            )
        }
    }

    fun onRemoteServiceBound() {
        _uiState.update {
            it.copy(
                isRemoteServiceBound = true,
                remoteStatus = "远程 Service 已绑定，Binder 通道已建立。"
            )
        }
        addEvent("Service connected", "RemoteEchoService 已连接")
    }

    fun onRemoteServiceDisconnected() {
        _uiState.update {
            it.copy(
                isRemoteServiceBound = false,
                remoteStatus = "远程 Service 已断开。"
            )
        }
        addEvent("Service disconnected", "RemoteEchoService 连接断开")
    }

    fun onBindRequested() {
        addEvent("Bind requested", "App 请求绑定 :binder 进程中的远程 Service")
    }

    fun onUnbindRequested() {
        _uiState.update {
            it.copy(
                isRemoteServiceBound = false,
                remoteStatus = "已主动解绑远程 Service。"
            )
        }
        addEvent("Unbind requested", "App 主动解绑远程 Service")
    }

    fun nextRequestId(): Int {
        var next = 0
        _uiState.update { state ->
            next = state.requestCount + 1
            state.copy(requestCount = next)
        }
        return next
    }

    fun onMessageSent(requestId: Int, payload: String) {
        addEvent("Message sent", "requestId=$requestId, payload=$payload")
    }

    fun onRemoteReply(
        requestId: Int,
        payload: String,
        remotePid: Int,
        remoteThread: String,
        sentAt: Long,
        handledAt: Long
    ) {
        val now = SystemClock.uptimeMillis()
        val roundTrip = now - sentAt
        val remoteCost = handledAt - sentAt
        val reply = "requestId=$requestId, remotePid=$remotePid, remoteThread=$remoteThread, roundTrip=${roundTrip}ms"

        _uiState.update { it.copy(lastReply = reply) }
        addEvent(
            title = "Remote reply",
            detail = "$reply, remoteCost=${remoteCost}ms, payload=$payload"
        )
    }

    fun onRemoteError(message: String) {
        addEvent("Remote error", message)
    }

    fun clearEvents() {
        _uiState.update {
            it.copy(
                callEvents = emptyList(),
                lastReply = "轨迹已清空。再次发送 Binder 消息，观察新的往返结果。"
            )
        }
    }

    private fun addEvent(title: String, detail: String) {
        Log.d(TAG, "$title: $detail")
        val event = BinderCallEvent(
            title = title,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _uiState.update { state ->
            state.copy(callEvents = (listOf(event) + state.callEvents).take(12))
        }
    }
}
