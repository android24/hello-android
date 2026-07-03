package com.helloandroid.framework

data class FrameworkWalkthroughState(
    val processInfo: ProcessInfo = ProcessInfo(),
    val handlerReport: String = "还没有发送 Handler 消息。点击按钮后，观察消息如何回到主线程执行。",
    val lifecycleEvents: List<TraceEvent> = emptyList(),
    val missionCards: List<MissionCard> = defaultMissionCards,
    val layers: List<FrameworkLayer> = defaultLayers,
    val launchChain: List<String> = defaultLaunchChain,
    val sourceTargets: List<SourceTarget> = defaultSourceTargets,
    val binderExamples: List<String> = defaultBinderExamples
)

data class ProcessInfo(
    val pid: Int = 0,
    val threadName: String = "unknown",
    val mainLooperThread: String = "unknown",
    val isMainLooper: Boolean = false,
    val sdkInt: Int = 0,
    val elapsedFromProcessStart: Long = 0L,
    val activityContextName: String = "unknown",
    val applicationContextName: String = "unknown"
)

data class TraceEvent(
    val title: String,
    val detail: String,
    val timestamp: String
)

data class MissionCard(
    val title: String,
    val clue: String,
    val action: String
)

data class FrameworkLayer(
    val name: String,
    val role: String,
    val examples: String
)

data class SourceTarget(
    val name: String,
    val path: String,
    val question: String
)

val defaultMissionCards = listOf(
    MissionCard(
        title = "任务一：找到后台入口",
        clue = "观察 Application 与 Activity 的日志顺序。",
        action = "打开 Logcat，搜索 FrameworkWalkthrough。"
    ),
    MissionCard(
        title = "任务二：追踪主线程传送带",
        clue = "发送一次 Handler 消息，看 Runnable 回到哪个线程。",
        action = "点击发送 Handler 消息，并记录等待耗时。"
    ),
    MissionCard(
        title = "任务三：标记源码路标",
        clue = "从页面里的源码入口卡片选择一个类。",
        action = "优先搜索 ActivityThread.main 或 Looper.loop。"
    ),
    MissionCard(
        title = "任务四：写出通关报告",
        clue = "把现象、源码入口和调用链写成 10 行笔记。",
        action = "参考 quality/framework-trace-template.md。"
    )
)

val defaultLayers = listOf(
    FrameworkLayer(
        name = "App",
        role = "你写的业务、页面、状态和数据逻辑",
        examples = "Activity, Application, ViewModel, Compose"
    ),
    FrameworkLayer(
        name = "Framework",
        role = "管理组件、窗口、消息、权限和系统服务 API",
        examples = "ActivityThread, ContextImpl, Handler, WindowManager"
    ),
    FrameworkLayer(
        name = "Native",
        role = "承接 Runtime、图形、媒体和数据库等底层能力",
        examples = "ART, Skia, SQLite, SurfaceFlinger"
    ),
    FrameworkLayer(
        name = "Kernel",
        role = "管理进程、线程、内存、驱动和 Binder 通信基础",
        examples = "Linux scheduler, memory, Binder driver"
    )
)

val defaultLaunchChain = listOf(
    "Launcher 发起 startActivity 请求",
    "ActivityTaskManagerService 判断任务栈与目标组件",
    "Zygote fork 出应用进程",
    "ActivityThread.main() 准备主线程 Looper",
    "Application.onCreate() 执行应用级初始化",
    "Instrumentation 调度 Activity 创建",
    "Activity.onCreate() 开始页面初始化"
)

val defaultSourceTargets = listOf(
    SourceTarget(
        name = "ActivityThread",
        path = "frameworks/base/core/java/android/app/ActivityThread.java",
        question = "应用进程主线程如何进入 Looper.loop()？"
    ),
    SourceTarget(
        name = "Instrumentation",
        path = "frameworks/base/core/java/android/app/Instrumentation.java",
        question = "Activity 生命周期是谁调起来的？"
    ),
    SourceTarget(
        name = "ContextImpl",
        path = "frameworks/base/core/java/android/app/ContextImpl.java",
        question = "startActivity() 如何继续走向系统服务？"
    ),
    SourceTarget(
        name = "Handler",
        path = "frameworks/base/core/java/android/os/Handler.java",
        question = "post() 如何变成 Message？"
    ),
    SourceTarget(
        name = "Looper",
        path = "frameworks/base/core/java/android/os/Looper.java",
        question = "主线程消息循环在哪里运行？"
    ),
    SourceTarget(
        name = "MessageQueue",
        path = "frameworks/base/core/java/android/os/MessageQueue.java",
        question = "消息如何排队、等待和取出？"
    ),
    SourceTarget(
        name = "Binder / IBinder",
        path = "frameworks/base/core/java/android/os/",
        question = "App 和 system_server 如何跨进程通信？"
    )
)

val defaultBinderExamples = listOf(
    "startActivity(): App 请求 system_server 启动目标 Activity",
    "getSystemService(): App 获取系统能力的本地代理入口",
    "PackageManager: App 查询安装包和组件信息"
)
