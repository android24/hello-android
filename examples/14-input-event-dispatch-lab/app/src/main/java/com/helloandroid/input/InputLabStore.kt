package com.helloandroid.input

import android.util.Log
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InputLabStore {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(InputLabState())

    val state: StateFlow<InputLabState> = _state

    fun setInterceptMove(enabled: Boolean) {
        _state.update {
            it.copy(
                interceptMove = enabled,
                currentExperiment = InputExperiment(
                    operation = if (enabled) "开启父容器 MOVE 拦截" else "关闭父容器 MOVE 拦截",
                    expected = if (enabled) {
                        "父容器会在 MOVE 阶段尝试拦截，子 View 可能收到 CANCEL。"
                    } else {
                        "父容器不主动拦截，子 View 更容易拿到完整 DOWN / MOVE / UP。"
                    },
                    actual = "请在原生 View 实验区点击或滑动，观察事件轨迹。",
                    conclusion = "拦截开关已经改变，等待触摸证据。"
                )
            )
        }
        record("Control", "interceptMove", "STATE", "enabled=$enabled")
    }

    fun recordActivityEvent(event: MotionEvent) {
        val action = event.actionName()
        _state.update {
            it.copy(
                score = it.score.copy(activityObserved = true),
                dispatchLinks = it.dispatchLinks.updateLink(
                    layer = "Activity",
                    action = action,
                    evidence = "dispatchTouchEvent ${event.positionDetail()}"
                ),
                currentExperiment = it.currentExperiment.copy(
                    actual = "Activity.dispatchTouchEvent 已收到 $action。",
                    conclusion = "事件已经进入 App 的 Activity 层。"
                )
            )
        }
        record("Activity", "dispatchTouchEvent", action, event.positionDetail())
    }

    fun recordParentEvent(phase: String, event: MotionEvent, intercepted: Boolean? = null) {
        val action = event.actionName()
        val detail = buildString {
            append(event.positionDetail())
            if (intercepted != null) append(", intercepted=$intercepted")
        }
        val sawMove = action == "MOVE"
        _state.update {
            it.copy(
                score = it.score.copy(
                    moveObserved = it.score.moveObserved || sawMove,
                    cancelObserved = it.score.cancelObserved || action == "CANCEL",
                    conflictObserved = it.score.conflictObserved || intercepted == true
                ),
                dispatchLinks = it.dispatchLinks.updateLink(
                    layer = "Parent",
                    action = action,
                    evidence = "$phase ${detail}"
                ),
                currentExperiment = it.currentExperiment.copy(
                    actual = "Parent.$phase 收到 $action。",
                    conclusion = if (intercepted == true) {
                        "父容器正在接管这段手势，子 View 可能会被取消。"
                    } else {
                        it.currentExperiment.conclusion
                    }
                )
            )
        }
        record("ParentViewGroup", phase, action, detail)
    }

    fun recordChildEvent(phase: String, event: MotionEvent) {
        val action = event.actionName()
        _state.update {
            it.copy(
                score = it.score.copy(
                    childClickObserved = it.score.childClickObserved || action == "UP",
                    moveObserved = it.score.moveObserved || action == "MOVE",
                    cancelObserved = it.score.cancelObserved || action == "CANCEL"
                ),
                currentExperiment = it.currentExperiment.copy(
                    actual = "Child.$phase 收到 $action。",
                    conclusion = when (action) {
                        "DOWN" -> "子 View 拿到了事件序列起点。"
                        "UP" -> "子 View 收到 UP，这次点击有机会完成。"
                        "CANCEL" -> "子 View 收到 CANCEL，这段手势被打断。"
                        else -> it.currentExperiment.conclusion
                    }
                )
            )
        }
        _state.update {
            it.copy(
                dispatchLinks = it.dispatchLinks.updateLink(
                    layer = "Child",
                    action = action,
                    evidence = "$phase ${event.positionDetail()}"
                )
            )
        }
        record("ChildView", phase, action, event.positionDetail())
    }

    fun recordComposeGesture(name: String, detail: String) {
        _state.update {
            it.copy(
                score = it.score.copy(composeGestureObserved = true),
                dispatchLinks = it.dispatchLinks.updateLink(
                    layer = "Compose",
                    action = "GESTURE",
                    evidence = "$name $detail"
                ),
                currentExperiment = InputExperiment(
                    operation = "Compose 手势实验：$name",
                    expected = "Compose 通过 Modifier 描述手势，但仍运行在 Android 输入链路之上。",
                    actual = detail,
                    conclusion = "Compose 改变了写法，没有绕开输入系统。"
                )
            )
        }
        record("Compose", name, "GESTURE", detail)
    }

    fun recordConflictDrag(deltaX: Float, deltaY: Float) {
        val direction = if (kotlin.math.abs(deltaX) >= kotlin.math.abs(deltaY)) "横向 MOVE" else "纵向 MOVE"
        val owner = if (direction == "横向 MOVE") "外层横滑容器" else "内层纵向列表"
        val advice = if (direction == "横向 MOVE") {
            "横向距离更明显，父容器可以考虑拦截并接管手势。"
        } else {
            "纵向距离更明显，父容器应该放行，让子列表继续滚动。"
        }
        _state.update {
            it.copy(
                score = it.score.copy(conflictObserved = true, moveObserved = true),
                conflictLab = it.conflictLab.copy(
                    horizontalMoves = it.conflictLab.horizontalMoves + if (direction == "横向 MOVE") 1 else 0,
                    verticalMoves = it.conflictLab.verticalMoves + if (direction == "纵向 MOVE") 1 else 0,
                    lastDirection = direction,
                    owner = owner,
                    advice = advice
                ),
                currentExperiment = InputExperiment(
                    operation = "滑动冲突实验：$direction",
                    expected = "根据 MOVE 的横向 / 纵向距离判断事件应该交给谁。",
                    actual = "dx=${deltaX.toInt()}, dy=${deltaY.toInt()}, owner=$owner",
                    conclusion = advice
                )
            )
        }
        record("ConflictLab", "drag", direction, "dx=${deltaX.toInt()}, dy=${deltaY.toInt()}, owner=$owner")
    }

    fun recordComposeScroll(index: Int) {
        _state.update {
            it.copy(
                score = it.score.copy(scrollObserved = true, composeGestureObserved = true),
                dispatchLinks = it.dispatchLinks.updateLink(
                    layer = "Compose",
                    action = "SCROLL",
                    evidence = "LazyColumn visibleItem=$index"
                ),
                currentExperiment = InputExperiment(
                    operation = "Compose 可滚动列表",
                    expected = "列表滚动同样来自输入事件序列，只是被 Compose 滚动容器解释。",
                    actual = "当前首个可见条目：#$index",
                    conclusion = "scroll、drag、click 是同一条输入链路上的不同解释。"
                )
            )
        }
        record("Compose", "LazyColumn", "SCROLL", "firstVisibleItem=$index")
    }

    fun recordMainThreadBusy(durationMs: Long) {
        _state.update {
            it.copy(
                score = it.score.copy(busyObserved = true),
                currentExperiment = InputExperiment(
                    operation = "模拟主线程忙碌 ${durationMs}ms",
                    expected = "主线程忙碌会让输入事件处理和点击反馈变慢。",
                    actual = "忙碌任务已执行，请对照事件时间戳观察反馈延迟。",
                    conclusion = "Input ANR 的核心线索之一，是主线程无法及时处理输入。"
                )
            )
        }
        record("MainThread", "busy", "BLOCK", "duration=${durationMs}ms")
    }

    fun clearEvents() {
        _state.update {
            it.copy(
                eventTrail = emptyList(),
                currentExperiment = InputExperiment(),
                score = InputScore(),
                dispatchLinks = defaultDispatchLinks,
                conflictLab = ConflictLabState()
            )
        }
        Log.d(TAG, "Events cleared")
    }

    private fun record(source: String, phase: String, action: String, detail: String) {
        Log.d(TAG, "$source.$phase $action: $detail")
        val event = InputEventLog(
            source = source,
            phase = phase,
            action = action,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _state.update { state ->
            state.copy(eventTrail = (listOf(event) + state.eventTrail).take(24))
        }
    }
}

private fun List<DispatchLink>.updateLink(
    layer: String,
    action: String,
    evidence: String
): List<DispatchLink> {
    return map { link ->
        if (link.layer == layer) {
            link.copy(latestAction = action, evidence = evidence)
        } else {
            link
        }
    }
}

fun MotionEvent.actionName(): String {
    return when (actionMasked) {
        MotionEvent.ACTION_DOWN -> "DOWN"
        MotionEvent.ACTION_MOVE -> "MOVE"
        MotionEvent.ACTION_UP -> "UP"
        MotionEvent.ACTION_CANCEL -> "CANCEL"
        MotionEvent.ACTION_POINTER_DOWN -> "POINTER_DOWN"
        MotionEvent.ACTION_POINTER_UP -> "POINTER_UP"
        else -> "ACTION_$actionMasked"
    }
}

fun MotionEvent.positionDetail(): String {
    return "x=${x.toInt()}, y=${y.toInt()}, rawX=${rawX.toInt()}, rawY=${rawY.toInt()}, pointers=$pointerCount"
}
