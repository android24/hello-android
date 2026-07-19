package com.helloandroid.rendering

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RenderingLabStore {
    private const val TAG = "RenderingFrameLab"
    private const val FRAME_BUDGET_MS = 16.6f
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(RenderingLabState())

    val state: StateFlow<RenderingLabState> = _state

    fun changeColor() {
        _state.update {
            it.copy(
                probeState = it.probeState.copy(colorIndex = it.probeState.colorIndex + 1),
                score = it.score.copy(invalidateObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "刷新请求",
                    signal = "invalidate",
                    evidence = "颜色变化，只需要内容重画"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "改变颜色",
                    expected = "颜色变化通常只需要 invalidate 和 draw。",
                    actual = "已提交颜色变化，等待 RenderProbeView.onDraw 证据。",
                    conclusion = "这类变化更像内容重画。"
                )
            )
        }
        record("Control", "changeColor", "INVALIDATE", "colorIndex=${_state.value.probeState.colorIndex}")
    }

    fun toggleLongText() {
        _state.update {
            it.copy(
                probeState = it.probeState.copy(longText = !it.probeState.longText),
                score = it.score.copy(layoutObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "刷新请求",
                    signal = "requestLayout",
                    evidence = "文本长度变化，尺寸可能改变"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "切换文本长度",
                    expected = "文案变长可能触发 measure / layout / draw。",
                    actual = "已提交文本变化，等待 View 树探针证据。",
                    conclusion = "内容影响尺寸时，只 invalidate 可能不够。"
                )
            )
        }
        record("Control", "toggleText", "REQUEST_LAYOUT", "longText=${_state.value.probeState.longText}")
    }

    fun toggleLargeSize() {
        _state.update {
            it.copy(
                probeState = it.probeState.copy(largeSize = !it.probeState.largeSize),
                score = it.score.copy(layoutObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "刷新请求",
                    signal = "requestLayout",
                    evidence = "View 高度变化，需要重新测量布局"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "切换 View 尺寸",
                    expected = "尺寸变化应该触发 requestLayout。",
                    actual = "已提交尺寸变化，观察 onMeasure / onLayout 是否执行。",
                    conclusion = "尺寸变化属于布局协商，不只是重画。"
                )
            )
        }
        record("Control", "toggleSize", "REQUEST_LAYOUT", "largeSize=${_state.value.probeState.largeSize}")
    }

    fun startFrameCapture() {
        _state.update {
            it.copy(
                frameStats = FrameStats(capturing = true),
                currentExperiment = RenderingExperiment(
                    operation = "Choreographer 帧采样",
                    expected = "记录连续帧间隔，观察是否超过 16.6ms。",
                    actual = "正在采样 18 帧。",
                    conclusion = "帧间隔是判断流畅度的第一批证据。"
                )
            )
        }
        record("FrameClock", "capture", "START", "sampling=18")
    }

    fun recordFrame(frameIntervalMs: Float) {
        _state.update {
            val samples = it.frameStats.samples + 1
            val slowFrames = it.frameStats.slowFrames + if (frameIntervalMs > FRAME_BUDGET_MS) 1 else 0
            val maxFrame = maxOf(it.frameStats.maxFrameMs, frameIntervalMs)
            val done = samples >= 18
            it.copy(
                frameStats = FrameStats(
                    capturing = !done,
                    samples = samples,
                    lastFrameMs = frameIntervalMs,
                    maxFrameMs = maxFrame,
                    slowFrames = slowFrames
                ),
                score = it.score.copy(frameObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "帧调度",
                    signal = if (frameIntervalMs > FRAME_BUDGET_MS) "SLOW_FRAME" else "FRAME",
                    evidence = "interval=${"%.2f".format(frameIntervalMs)}ms, samples=$samples"
                ),
                currentExperiment = it.currentExperiment.copy(
                    actual = "last=${"%.2f".format(frameIntervalMs)}ms, max=${"%.2f".format(maxFrame)}ms, slow=$slowFrames",
                    conclusion = if (slowFrames > 0) "已经观察到超过 16.6ms 的慢帧。" else "当前采样暂未出现明显慢帧。"
                )
            )
        }
        record("FrameClock", "doFrame", "FRAME", "interval=${"%.2f".format(frameIntervalMs)}ms")
    }

    fun recordProbeEvent(phase: String, detail: String) {
        val signal = when (phase) {
            "onMeasure", "onLayout" -> "LAYOUT"
            "onDraw" -> "DRAW"
            else -> "VIEW"
        }
        _state.update {
            it.copy(
                score = it.score.copy(
                    layoutObserved = it.score.layoutObserved || phase == "onMeasure" || phase == "onLayout",
                    drawObserved = it.score.drawObserved || phase == "onDraw",
                    invalidateObserved = it.score.invalidateObserved || phase == "invalidate"
                ),
                renderLinks = it.renderLinks.updateLink(
                    layer = "View 树",
                    signal = signal,
                    evidence = "$phase $detail"
                ),
                currentExperiment = it.currentExperiment.copy(
                    actual = "RenderProbeView.$phase: $detail"
                )
            )
        }
        record("RenderProbeView", phase, signal, detail)
    }

    fun recordScroll(firstVisibleItem: Int) {
        _state.update {
            it.copy(
                score = it.score.copy(scrollObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "渲染提交",
                    signal = "SCROLL",
                    evidence = "LazyColumn firstVisibleItem=$firstVisibleItem"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "滚动列表",
                    expected = "滚动会触发连续帧刷新，item 复杂度会影响流畅度。",
                    actual = "当前首个可见条目：#$firstVisibleItem",
                    conclusion = "滚动是观察帧预算最直观的场景之一。"
                )
            )
        }
        record("Compose", "LazyColumn", "SCROLL", "firstVisibleItem=$firstVisibleItem")
    }

    fun recordAnimation(name: String, detail: String) {
        _state.update {
            it.copy(
                score = it.score.copy(animationObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "渲染提交",
                    signal = "ANIMATION",
                    evidence = "$name $detail"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "动画实验：$name",
                    expected = "transform 动画通常比尺寸动画更少触发布局。",
                    actual = detail,
                    conclusion = "动画是否流畅，要结合属性类型和帧间隔观察。"
                )
            )
        }
        record("Compose", name, "ANIMATION", detail)
    }

    fun recordMainThreadBusy(durationMs: Long) {
        _state.update {
            it.copy(
                score = it.score.copy(busyObserved = true),
                renderLinks = it.renderLinks.updateLink(
                    layer = "帧调度",
                    signal = "BLOCK",
                    evidence = "mainThreadBusy=${durationMs}ms"
                ),
                currentExperiment = RenderingExperiment(
                    operation = "模拟主线程忙碌 ${durationMs}ms",
                    expected = "主线程忙碌会挤压 traversal 和下一帧提交时间。",
                    actual = "忙碌任务已执行，请启动帧采样观察后续帧间隔。",
                    conclusion = "主线程迟到，后面的绘制和提交也会迟到。"
                )
            )
        }
        record("MainThread", "busy", "BLOCK", "duration=${durationMs}ms")
    }

    fun markDiagnosisRead(title: String) {
        _state.update {
            it.copy(
                score = it.score.copy(diagnosisObserved = true),
                currentExperiment = it.currentExperiment.copy(
                    operation = "阅读诊断卡：$title",
                    conclusion = "渲染问题要从现象翻译成链路证据。"
                )
            )
        }
        record("Diagnosis", title, "READ", "diagnostic card inspected")
    }

    fun clearEvents() {
        _state.update {
            it.copy(
                eventTrail = emptyList(),
                currentExperiment = RenderingExperiment(),
                frameStats = FrameStats(),
                score = RenderingScore(),
                renderLinks = defaultRenderLinks
            )
        }
        Log.d(TAG, "Events cleared")
    }

    private fun record(source: String, phase: String, signal: String, detail: String) {
        Log.d(TAG, "$source.$phase $signal: $detail")
        val event = RenderingEventLog(
            source = source,
            phase = phase,
            signal = signal,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _state.update { state ->
            state.copy(eventTrail = (listOf(event) + state.eventTrail).take(30))
        }
    }
}

private fun List<RenderLink>.updateLink(
    layer: String,
    signal: String,
    evidence: String
): List<RenderLink> {
    return map { link ->
        if (link.layer == layer) {
            link.copy(latestSignal = signal, evidence = evidence)
        } else {
            link
        }
    }
}
