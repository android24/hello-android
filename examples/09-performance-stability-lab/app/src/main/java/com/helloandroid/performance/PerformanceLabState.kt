package com.helloandroid.performance

data class PerformanceLabState(
    val startupReport: StartupReport = StartupReport(),
    val mainThreadReport: String = "尚未执行主线程压力实验",
    val cpuReport: String = "尚未执行后台 CPU 实验",
    val memoryReport: MemoryReport = MemoryReport(),
    val stabilityReport: String = "尚未捕获异常",
    val checklist: List<HealthCheckItem> = defaultChecklist
)

data class StartupReport(
    val elapsedFromProcessStartMs: Long = 0L,
    val buildProfile: String = "unknown",
    val verboseLogEnabled: Boolean = false
) {
    val summary: String
        get() = if (elapsedFromProcessStartMs == 0L) {
            "等待 Activity 启动记录"
        } else {
            "进程创建到首个 Activity onCreate：${elapsedFromProcessStartMs}ms"
        }
}

data class MemoryReport(
    val allocatedChunks: Int = 0,
    val allocatedMb: Int = 0,
    val runtimeUsedMb: Long = 0L
) {
    val summary: String
        get() = "已保留 ${allocatedChunks} 个内存块，约 ${allocatedMb}MB；Runtime 已用约 ${runtimeUsedMb}MB"
}

data class HealthCheckItem(
    val title: String,
    val description: String,
    val status: CheckStatus
)

enum class CheckStatus {
    Ready,
    Warning,
    Passed
}

val defaultChecklist = listOf(
    HealthCheckItem(
        title = "启动",
        description = "记录首屏前关键节点，避免把重活塞进 Application 和首个 Activity。",
        status = CheckStatus.Ready
    ),
    HealthCheckItem(
        title = "卡顿",
        description = "主线程只做 UI 工作，耗时计算转移到后台线程或协程调度器。",
        status = CheckStatus.Warning
    ),
    HealthCheckItem(
        title = "内存",
        description = "观察大对象、列表缓存和图片资源，及时释放不再需要的引用。",
        status = CheckStatus.Ready
    ),
    HealthCheckItem(
        title = "ANR",
        description = "避免主线程等待锁、IO、网络和长时间计算。",
        status = CheckStatus.Warning
    ),
    HealthCheckItem(
        title = "崩溃",
        description = "捕获可预期错误，保留堆栈、场景、版本和用户操作路径。",
        status = CheckStatus.Ready
    ),
    HealthCheckItem(
        title = "包体积",
        description = "检查资源、依赖、R8、ABI 和构建产物大小。",
        status = CheckStatus.Ready
    )
)
