package com.helloandroid.launch

import android.os.Process
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LaunchTraceStore {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(LaunchLabState())

    val state: StateFlow<LaunchLabState> = _state

    fun recordProcess(processAgeMs: Long) {
        _state.update { state ->
            state.copy(
                processInfo = LaunchProcessInfo(
                    pid = Process.myPid(),
                    threadName = Thread.currentThread().name,
                    processAgeMs = processAgeMs
                )
            )
        }
    }

    fun recordScreen(screen: String) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun recordStandardLaunch(source: String) {
        _state.update { state ->
            val nextDetailIndex = state.stackSnapshot.count { it.label.startsWith("DetailActivity") } + 1
            state.copy(
                standardLaunchCount = state.standardLaunchCount + 1,
                duplicateDetailCount = if (source == "DetailActivity") {
                    state.duplicateDetailCount + 1
                } else {
                    state.duplicateDetailCount
                },
                expectedActual = LaunchExpectedActual(
                    operation = "$source 普通启动 DetailActivity",
                    expected = "standard 模式通常会创建一个新的 DetailActivity 实例，并把它压到当前任务栈顶部。",
                    actual = "等待 DetailActivity.onCreate 出现；如果连续从 DetailActivity 启动自己，返回时会逐层退出。",
                    verdict = "等待生命周期证据。"
                ),
                stackSnapshot = (state.stackSnapshot + LaunchStackNode(
                    label = "DetailActivity #$nextDetailIndex",
                    note = "standard 新实例"
                )).takeLast(MAX_STACK_SNAPSHOT)
            )
        }
        record("startActivity", "$source 普通启动 DetailActivity")
    }

    fun recordSingleTopLaunch(source: String) {
        _state.update { state ->
            val isLaunchingSelfOnTop = source == "SingleTopActivity" &&
                state.stackSnapshot.lastOrNull()?.label == "SingleTopActivity"
            state.copy(
                singleTopLaunchCount = state.singleTopLaunchCount + 1,
                expectedActual = LaunchExpectedActual(
                    operation = "$source 启动 SingleTopActivity",
                    expected = if (isLaunchingSelfOnTop) {
                        "目标已经位于栈顶，singleTop 应复用当前实例，并触发 onNewIntent。"
                    } else {
                        "目标不在栈顶，系统会创建或调度 SingleTopActivity 进入前台。"
                    },
                    actual = "等待 SingleTopActivity.onCreate 或 SingleTopActivity.onNewIntent 出现。",
                    verdict = "等待生命周期证据。"
                ),
                stackSnapshot = if (isLaunchingSelfOnTop) {
                    state.stackSnapshot
                } else {
                    (state.stackSnapshot + LaunchStackNode(
                        label = "SingleTopActivity",
                        note = "singleTop 观察页"
                    )).takeLast(MAX_STACK_SNAPSHOT)
                }
            )
        }
        record("startActivity", "$source 启动 SingleTopActivity")
    }

    fun recordNewIntent(screen: String, flags: Int) {
        _state.update {
            it.copy(
                newIntentCount = it.newIntentCount + 1,
                expectedActual = it.expectedActual.copy(
                    actual = "$screen.onNewIntent 已出现，flags=$flags。",
                    verdict = "实例被复用，启动请求没有变成一次新的 onCreate。"
                )
            )
        }
        record("$screen.onNewIntent", "flags=$flags")
    }

    fun recordClearTop(source: String) {
        _state.update {
            it.copy(
                clearTopCount = it.clearTopCount + 1,
                expectedActual = LaunchExpectedActual(
                    operation = "$source 使用 CLEAR_TOP 回 MainActivity",
                    expected = "系统会寻找任务栈里的 MainActivity，并清理它之上的页面；配合 SINGLE_TOP 时首页应收到 onNewIntent。",
                    actual = "等待 MainActivity.onNewIntent、子页面 onPause/onDestroy 等日志。",
                    verdict = "等待生命周期证据。"
                ),
                stackSnapshot = defaultStackSnapshot
            )
        }
        record("CLEAR_TOP", "$source 使用 FLAG_ACTIVITY_CLEAR_TOP 回 MainActivity")
    }

    fun clearTrace() {
        _state.update {
            it.copy(
                traceEvents = emptyList(),
                standardLaunchCount = 0,
                singleTopLaunchCount = 0,
                newIntentCount = 0,
                clearTopCount = 0,
                duplicateDetailCount = 0,
                expectedActual = LaunchExpectedActual(),
                stackSnapshot = defaultStackSnapshot
            )
        }
        Log.d(TAG, "Trace cleared")
    }

    fun record(title: String, detail: String) {
        Log.d(TAG, "$title: $detail")
        val event = LaunchTraceEvent(
            title = title,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _state.update { state ->
            state.copy(
                traceEvents = (listOf(event) + state.traceEvents).take(16),
                expectedActual = state.expectedActual.withLifecycleEvidence(title),
                stackSnapshot = state.stackSnapshot.withLifecycleChange(title)
            )
        }
    }

    private fun LaunchExpectedActual.withLifecycleEvidence(title: String): LaunchExpectedActual {
        return when {
            title == "DetailActivity.onCreate" -> copy(
                actual = "DetailActivity.onCreate 已出现，说明这次 standard 启动创建了新实例。",
                verdict = "预期成立：返回栈增加了一层 DetailActivity。"
            )
            title == "SingleTopActivity.onCreate" -> copy(
                actual = "SingleTopActivity.onCreate 已出现，说明目标不在栈顶时仍会创建或进入一个实例。",
                verdict = "预期成立：singleTop 只在栈顶命中时复用。"
            )
            title == "MainActivity.onNewIntent" -> copy(
                actual = "MainActivity.onNewIntent 已出现，CLEAR_TOP 已把启动请求交回首页实例。",
                verdict = "预期成立：首页复用，中间页面会陆续销毁。"
            )
            title.endsWith(".onDestroy") && operation.contains("CLEAR_TOP") -> copy(
                actual = "$actual；同时观察到 $title，说明栈清理正在发生。",
                verdict = "CLEAR_TOP 清理证据更完整。"
            )
            else -> this
        }
    }

    private fun List<LaunchStackNode>.withLifecycleChange(title: String): List<LaunchStackNode> {
        val destroyedScreen = when (title) {
            "DetailActivity.onDestroy" -> "DetailActivity"
            "SingleTopActivity.onDestroy" -> "SingleTopActivity"
            else -> return this
        }
        val top = lastOrNull() ?: return this
        return if (top.label.startsWith(destroyedScreen) && size > 1) {
            dropLast(1)
        } else {
            this
        }
    }

    private const val MAX_STACK_SNAPSHOT = 6
}
