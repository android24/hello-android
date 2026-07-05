package com.helloandroid.binder

data class BinderLabState(
    val processInfo: BinderProcessInfo = BinderProcessInfo(),
    val isRemoteServiceBound: Boolean = false,
    val remoteStatus: String = "远程 Service 尚未绑定。点击绑定按钮，建立一条 Binder 通道。",
    val lastReply: String = "还没有收到远程回复。",
    val requestCount: Int = 0,
    val missions: List<BinderMission> = defaultBinderMissions,
    val systemServices: List<SystemServiceItem> = defaultSystemServices,
    val registryEntries: List<ServiceRegistryEntry> = defaultRegistryEntries,
    val binderModel: List<BinderModelStep> = defaultBinderModel,
    val callEvents: List<BinderCallEvent> = emptyList()
)

data class BinderProcessInfo(
    val pid: Int = 0,
    val threadName: String = "unknown",
    val processAgeMs: Long = 0L,
    val packageName: String = "unknown"
)

data class BinderMission(
    val title: String,
    val clue: String,
    val action: String
)

data class SystemServiceItem(
    val name: String,
    val appEntry: String,
    val responsibility: String,
    val binderHint: String
)

data class ServiceRegistryEntry(
    val serviceName: String,
    val registeredBy: String,
    val clientEntry: String,
    val binderRole: String
)

data class BinderModelStep(
    val name: String,
    val detail: String
)

data class BinderCallEvent(
    val title: String,
    val detail: String,
    val timestamp: String
)

val defaultBinderMissions = listOf(
    BinderMission(
        title = "任务一：查看系统服务窗口",
        clue = "先从 App 侧 Manager 认识系统能力入口。",
        action = "阅读系统服务观察卡片。"
    ),
    BinderMission(
        title = "任务二：建立 Binder 通道",
        clue = "绑定运行在 :binder 进程中的 RemoteEchoService。",
        action = "点击绑定远程 Service。"
    ),
    BinderMission(
        title = "任务三：发送跨进程消息",
        clue = "观察本地 PID、远程 PID、线程名和往返耗时。",
        action = "点击发送 Binder 消息。"
    ),
    BinderMission(
        title = "任务四：写调用链报告",
        clue = "把一次请求整理成 App -> Binder -> Remote Service -> Reply。",
        action = "参考 quality/binder-call-report-template.md。"
    )
)

val defaultSystemServices = listOf(
    SystemServiceItem(
        name = "ActivityManager",
        appEntry = "getSystemService(ActivityManager::class.java)",
        responsibility = "观察应用任务、进程和内存相关信息。",
        binderHint = "背后会和 Activity / Process 管理相关系统能力协作。"
    ),
    SystemServiceItem(
        name = "WindowManager",
        appEntry = "getSystemService(WindowManager::class.java)",
        responsibility = "提供窗口、显示和布局相关入口。",
        binderHint = "窗口管理最终会连接到系统窗口服务。"
    ),
    SystemServiceItem(
        name = "NotificationManager",
        appEntry = "getSystemService(NotificationManager::class.java)",
        responsibility = "提交、更新和取消通知。",
        binderHint = "通知展示需要系统统一管理，通常涉及系统服务调用。"
    ),
    SystemServiceItem(
        name = "ClipboardManager",
        appEntry = "getSystemService(ClipboardManager::class.java)",
        responsibility = "访问系统剪贴板。",
        binderHint = "剪贴板是跨应用共享能力，需要系统集中协调。"
    )
)

val defaultRegistryEntries = listOf(
    ServiceRegistryEntry(
        serviceName = "activity",
        registeredBy = "SystemServer / ActivityTaskManagerService",
        clientEntry = "ActivityManager / startActivity",
        binderRole = "管理 Activity 启动、任务栈和应用进程相关调度。"
    ),
    ServiceRegistryEntry(
        serviceName = "window",
        registeredBy = "SystemServer / WindowManagerService",
        clientEntry = "WindowManager",
        binderRole = "管理窗口添加、布局、显示层级和输入焦点。"
    ),
    ServiceRegistryEntry(
        serviceName = "package",
        registeredBy = "SystemServer / PackageManagerService",
        clientEntry = "PackageManager",
        binderRole = "管理安装包、组件解析、权限声明和应用信息查询。"
    ),
    ServiceRegistryEntry(
        serviceName = "notification",
        registeredBy = "SystemServer / NotificationManagerService",
        clientEntry = "NotificationManager",
        binderRole = "统一处理通知提交、更新、取消和展示策略。"
    )
)

val defaultBinderModel = listOf(
    BinderModelStep("Client", "MainActivity 发起绑定和发送消息。"),
    BinderModelStep("Proxy", "Messenger 持有远程 Binder，像本地对象一样发送 Message。"),
    BinderModelStep("Binder Driver", "底层通道负责把请求送到远程进程。"),
    BinderModelStep("Server", "RemoteEchoService 在 :binder 进程中处理消息。"),
    BinderModelStep("Reply", "远程服务通过 replyTo 把结果发回 App 进程。")
)
