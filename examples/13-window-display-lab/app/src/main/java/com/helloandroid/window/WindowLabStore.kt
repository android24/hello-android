package com.helloandroid.window

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WindowLabStore {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(WindowLabState())

    val state: StateFlow<WindowLabState> = _state

    fun recordWindowInfo(info: WindowInfo) {
        _state.update {
            it.copy(
                windowInfo = info,
                score = it.score.copy(decorObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "读取 Activity Window / DecorView",
                    expected = "Activity 拥有 Window，Window 持有 DecorView，业务 UI 挂在内容区域。",
                    actual = "window=${info.windowClass}, decor=${info.decorViewClass}, decorSize=${info.decorSize}, contentSize=${info.contentSize}",
                    conclusion = "Activity 不是屏幕本身，DecorView 才是窗口里的根视图。"
                )
            )
        }
        record("WindowInfo", "window=${info.windowClass}, decor=${info.decorViewClass}, size=${info.decorSize}")
    }

    fun recordDialogShown() {
        _state.update {
            it.copy(
                score = it.score.copy(dialogObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "显示 AlertDialog",
                    expected = "Dialog 会作为附属窗口盖在 Activity 主窗口之上，并依赖宿主 Activity 的有效 Token。",
                    actual = "AlertDialog 已显示；关闭前它会占据前台交互焦点。",
                    conclusion = "这更接近 Window 层级问题，不是普通 View 的 zIndex 问题。"
                )
            )
        }
        record("Dialog.show", "附属窗口覆盖 Activity 内容")
    }

    fun recordPopupShown() {
        _state.update {
            it.copy(
                score = it.score.copy(popupObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "显示 Popup / DropdownMenu",
                    expected = "Popup 依附当前锚点区域显示，适合观察轻量浮层和锚点关系。",
                    actual = "Popup 已显示；它会覆盖部分内容，但仍依赖当前界面上下文。",
                    conclusion = "Popup 比 Dialog 轻，但同样要注意宿主和生命周期。"
                )
            )
        }
        record("Popup.show", "轻量浮层依附按钮区域显示")
    }

    fun recordToastShown() {
        _state.update {
            it.copy(
                currentExperiment = WindowExperiment(
                    operation = "显示 Toast",
                    expected = "Toast 是系统管理的短提示，不适合承载复杂交互。",
                    actual = "Toast 已请求显示；它短暂出现后由系统移除。",
                    conclusion = "提示类能力和业务弹层不是同一类窗口工具。"
                )
            )
        }
        record("Toast.show", "短提示请求已发送")
    }

    fun recordKeyboardRequested() {
        _state.update {
            it.copy(
                score = it.score.copy(keyboardObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "聚焦输入框并请求输入法",
                    expected = "软键盘不是页面里的普通 View，而是输入法窗口；它会影响可见区域和 Insets。",
                    actual = "输入框已获得焦点，请观察底部内容是否被压缩、遮挡或跟随滚动。",
                    conclusion = "输入法问题要同时看焦点、WindowInsets 和滚动容器。"
                )
            )
        }
        record("IME.request", "输入框获得焦点，等待输入法窗口出现")
    }

    fun recordColorInvalidate() {
        _state.update {
            it.copy(
                score = it.score.copy(frameObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "改变颜色",
                    expected = "颜色变化通常更像重绘请求，尺寸位置不一定变化。",
                    actual = "色块状态已改变，等待下一帧刷新后绘制新颜色。",
                    conclusion = "这更接近 invalidate / draw 观察。"
                )
            )
        }
        record("invalidate-like", "颜色变化，观察下一帧绘制")
    }

    fun recordTextRelayout() {
        _state.update {
            it.copy(
                score = it.score.copy(frameObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "改变文本长度",
                    expected = "文本长度变化可能影响尺寸或换行，更像 requestLayout + draw。",
                    actual = "长短文本已切换，观察卡片高度和布局是否变化。",
                    conclusion = "这更接近 measure / layout / draw 观察。"
                )
            )
        }
        record("requestLayout-like", "文本长度变化，观察布局是否重新计算")
    }

    fun recordMainThreadBusy(durationMs: Long) {
        _state.update {
            it.copy(
                score = it.score.copy(frameObserved = true),
                currentExperiment = WindowExperiment(
                    operation = "模拟主线程忙碌 ${durationMs}ms",
                    expected = "主线程忙碌会挤占下一帧刷新时间，可能让点击反馈变慢。",
                    actual = "忙碌任务已执行；观察事件时间戳和页面反馈。",
                    conclusion = "一帧刷新依赖主线程及时回到消息循环。"
                )
            )
        }
        record("MainThread.busy", "模拟主线程忙碌 ${durationMs}ms")
    }

    fun recordFrame(frameTimeNanos: Long) {
        val frameMs = frameTimeNanos / 1_000_000
        record("Choreographer.frame", "frameTime=${frameMs}ms")
    }

    fun recordContentSize(width: Int, height: Int) {
        _state.update {
            val nextSize = "${width}x$height"
            if (it.windowInfo.contentSize == nextSize) {
                return@update it
            }
            it.copy(
                windowInfo = it.windowInfo.copy(contentSize = nextSize)
            )
        }
    }

    fun clearEvents() {
        _state.update {
            it.copy(
                events = emptyList(),
                currentExperiment = WindowExperiment(),
                score = WindowScore()
            )
        }
        Log.d(TAG, "Events cleared")
    }

    private fun record(title: String, detail: String) {
        Log.d(TAG, "$title: $detail")
        val event = WindowEvent(
            title = title,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _state.update { state ->
            state.copy(events = (listOf(event) + state.events).take(18))
        }
    }
}
