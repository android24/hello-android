package com.helloandroid.rendering

data class RenderingLabState(
    val probeState: ProbeState = ProbeState(),
    val currentExperiment: RenderingExperiment = RenderingExperiment(),
    val frameStats: FrameStats = FrameStats(),
    val score: RenderingScore = RenderingScore(),
    val renderLinks: List<RenderLink> = defaultRenderLinks,
    val diagnosticCards: List<RenderingDiagnosticCard> = defaultRenderingDiagnosticCards,
    val eventTrail: List<RenderingEventLog> = emptyList()
)

data class ProbeState(
    val colorIndex: Int = 0,
    val longText: Boolean = false,
    val largeSize: Boolean = false
)

data class RenderingExperiment(
    val operation: String = "等待第一次渲染实验",
    val expected: String = "先猜测：这次变化会触发 invalidate、requestLayout，还是一段连续帧？",
    val actual: String = "操作后观察帧节拍、View 探针和事件轨迹。",
    val conclusion: String = "还没有证据。"
)

data class FrameStats(
    val capturing: Boolean = false,
    val samples: Int = 0,
    val lastFrameMs: Float = 0f,
    val maxFrameMs: Float = 0f,
    val slowFrames: Int = 0
)

data class RenderingScore(
    val invalidateObserved: Boolean = false,
    val layoutObserved: Boolean = false,
    val drawObserved: Boolean = false,
    val frameObserved: Boolean = false,
    val busyObserved: Boolean = false,
    val scrollObserved: Boolean = false,
    val animationObserved: Boolean = false,
    val diagnosisObserved: Boolean = false
)

data class RenderLink(
    val layer: String,
    val role: String,
    val latestSignal: String = "等待",
    val evidence: String = "还没有证据"
)

data class RenderingEventLog(
    val source: String,
    val phase: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

data class RenderingDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

val defaultRenderLinks = listOf(
    RenderLink(
        layer = "刷新请求",
        role = "invalidate / requestLayout / recomposition",
        evidence = "等待颜色、文案或尺寸变化"
    ),
    RenderLink(
        layer = "View 树",
        role = "measure / layout / draw",
        evidence = "等待 RenderProbeView 回调"
    ),
    RenderLink(
        layer = "帧调度",
        role = "Choreographer / VSYNC",
        evidence = "等待帧节拍采样"
    ),
    RenderLink(
        layer = "渲染提交",
        role = "HardwareRenderer / RenderThread / GPU",
        evidence = "等待动画、滚动或慢帧证据"
    ),
    RenderLink(
        layer = "系统合成",
        role = "Surface / BufferQueue / SurfaceFlinger",
        evidence = "普通 App 侧只能记录边界推断"
    )
)

val defaultRenderingDiagnosticCards = listOf(
    RenderingDiagnosticCard(
        title = "UI 不刷新",
        symptom = "数据变了，但自定义 View 画面没有变化。",
        firstEvidence = "查看是否调用 invalidate，以及 onDraw 是否执行。",
        fixDirection = "内容变化调用 invalidate；尺寸变化调用 requestLayout。"
    ),
    RenderingDiagnosticCard(
        title = "文本变长后卡顿",
        symptom = "点击后文案变长，页面明显停顿。",
        firstEvidence = "观察是否触发 onMeasure / onLayout。",
        fixDirection = "减少不必要布局，控制文本变化影响范围。"
    ),
    RenderingDiagnosticCard(
        title = "滚动掉帧",
        symptom = "列表快速滚动时出现不连续。",
        firstEvidence = "查看 Choreographer 帧间隔、item 复杂度和图片加载。",
        fixDirection = "简化 item，避免滚动中重任务，优化图片和状态更新。"
    ),
    RenderingDiagnosticCard(
        title = "动画卡顿",
        symptom = "动画第一帧或某几帧明显停顿。",
        firstEvidence = "比较 transform 动画和尺寸动画的帧表现。",
        fixDirection = "优先使用 translation、alpha、scale，避免每帧触发布局。"
    ),
    RenderingDiagnosticCard(
        title = "黑屏或闪烁",
        symptom = "窗口存在，但画面短暂空白或内容闪动。",
        firstEvidence = "确认首帧、Surface、Buffer 和窗口切换时机。",
        fixDirection = "稳定首帧内容，检查 Surface 生命周期和窗口过渡。"
    )
)
