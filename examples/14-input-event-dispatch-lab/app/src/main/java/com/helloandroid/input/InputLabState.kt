package com.helloandroid.input

data class InputLabState(
    val interceptMove: Boolean = false,
    val currentExperiment: InputExperiment = InputExperiment(),
    val score: InputScore = InputScore(),
    val diagnosticCards: List<InputDiagnosticCard> = defaultInputDiagnosticCards,
    val eventTrail: List<InputEventLog> = emptyList()
)

data class InputExperiment(
    val operation: String = "等待第一次输入实验",
    val expected: String = "先猜测：这次触摸会被 Activity、父容器、子 View 还是 Compose 手势处理？",
    val actual: String = "操作后观察事件轨迹，再修正自己的判断。",
    val conclusion: String = "还没有证据。"
)

data class InputScore(
    val activityObserved: Boolean = false,
    val childClickObserved: Boolean = false,
    val moveObserved: Boolean = false,
    val cancelObserved: Boolean = false,
    val composeGestureObserved: Boolean = false,
    val busyObserved: Boolean = false
)

data class InputEventLog(
    val source: String,
    val phase: String,
    val action: String,
    val detail: String,
    val timestamp: String
)

data class InputDiagnosticCard(
    val title: String,
    val symptom: String,
    val likelyCause: String,
    val fixDirection: String
)

val defaultInputDiagnosticCards = listOf(
    InputDiagnosticCard(
        title = "点击无响应",
        symptom = "用户点了控件，但 click 回调没有触发。",
        likelyCause = "事件没有进入 Activity、父容器拦截、子 View 没消费 DOWN，或主线程阻塞。",
        fixDirection = "从 Activity、Parent、Child 三层打日志，先找事件断点。"
    ),
    InputDiagnosticCard(
        title = "滑动冲突",
        symptom = "内层列表和外层容器都想处理 MOVE，滑动体验不稳定。",
        likelyCause = "父容器和子 View 没有清晰约定横向、纵向手势归属。",
        fixDirection = "根据 MOVE 方向和距离判断拦截，必要时使用 requestDisallowInterceptTouchEvent。"
    ),
    InputDiagnosticCard(
        title = "点击穿透",
        symptom = "用户点在上层区域，却触发了下层控件。",
        likelyCause = "上层可见但没有消费事件，或弹层关闭时事件继续传递。",
        fixDirection = "明确上层是否应该消费 DOWN，避免看得见却接不住事件。"
    ),
    InputDiagnosticCard(
        title = "Input ANR",
        symptom = "点击后页面长时间无响应，最终出现 ANR。",
        likelyCause = "主线程执行耗时任务、等待锁、同步 IO 或耗时 Binder 调用。",
        fixDirection = "结合 ANR trace、主线程堆栈和事件日志定位阻塞点。"
    )
)
