package com.helloandroid.window

data class WindowLabState(
    val windowInfo: WindowInfo = WindowInfo(),
    val currentExperiment: WindowExperiment = WindowExperiment(),
    val score: WindowScore = WindowScore(),
    val diagnosticCards: List<WindowDiagnosticCard> = defaultDiagnosticCards,
    val events: List<WindowEvent> = emptyList()
)

data class WindowInfo(
    val activityName: String = "MainActivity",
    val windowClass: String = "waiting",
    val decorViewClass: String = "waiting",
    val decorSize: String = "waiting",
    val contentSize: String = "waiting",
    val rootViewClass: String = "Compose content",
    val softInputMode: String = "adjustResize"
)

data class WindowExperiment(
    val operation: String = "等待第一次窗口实验",
    val expected: String = "先猜测：这次变化属于 View 内部、Window 层级，还是一帧刷新？",
    val actual: String = "操作后观察事件轨迹，再修正自己的判断。",
    val conclusion: String = "还没有证据。"
)

data class WindowScore(
    val decorObserved: Boolean = false,
    val dialogObserved: Boolean = false,
    val popupObserved: Boolean = false,
    val keyboardObserved: Boolean = false,
    val frameObserved: Boolean = false
)

data class WindowDiagnosticCard(
    val title: String,
    val symptom: String,
    val likelyCause: String,
    val fixDirection: String
)

data class WindowEvent(
    val title: String,
    val detail: String,
    val timestamp: String
)

val defaultDiagnosticCards = listOf(
    WindowDiagnosticCard(
        title = "BadTokenException",
        symptom = "弹窗偶发崩溃，常发生在页面退出或异步回调之后。",
        likelyCause = "宿主 Activity 已经无效，窗口 Token 不再合法。",
        fixDirection = "显示前检查生命周期，宿主销毁时 dismiss Dialog / PopupWindow。"
    ),
    WindowDiagnosticCard(
        title = "输入法遮挡",
        symptom = "输入框获得焦点后，软键盘盖住底部内容。",
        likelyCause = "输入法窗口改变可见区域，页面没有正确处理 Insets 或滚动容器。",
        fixDirection = "使用 adjustResize、WindowInsets、滚动容器或 imePadding 处理可见区域。"
    ),
    WindowDiagnosticCard(
        title = "启动白屏",
        symptom = "Activity 已经打开，但首屏内容迟迟没有出现。",
        likelyCause = "窗口先显示了主题背景，业务首帧被主线程任务或重布局拖慢。",
        fixDirection = "优化启动主题、减少主线程初始化、拆分首屏和非首屏任务。"
    ),
    WindowDiagnosticCard(
        title = "遮挡混淆",
        symptom = "页面元素或弹层盖住了其他内容，不清楚该改 zIndex 还是窗口配置。",
        likelyCause = "没有区分 View 层级和 Window 层级。",
        fixDirection = "先判断遮挡发生在同一窗口内部，还是 Dialog / 输入法 / 系统窗口之间。"
    )
)
