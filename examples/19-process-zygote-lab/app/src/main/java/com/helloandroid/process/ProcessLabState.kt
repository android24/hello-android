package com.helloandroid.process

data class ProcessLabState(
    val identity: ProcessIdentity = ProcessIdentity(),
    val startup: StartupTrace = StartupTrace(),
    val remote: RemoteProcessState = RemoteProcessState(),
    val isolated: IsolatedProcessState = IsolatedProcessState(),
    val recovery: ProcessRecoveryState = ProcessRecoveryState(),
    val oomSnapshots: List<OomSnapshot> = emptyList(),
    val anr: AnrLabState = AnrLabState(),
    val traceGuide: AnrTraceGuide = AnrTraceGuide(),
    val threadProbe: ThreadProbe = ThreadProbe(),
    val diagnosticCards: List<ProcessDiagnosticCard> = defaultProcessDiagnosticCards,
    val experiment: ProcessExperiment = ProcessExperiment(),
    val score: ProcessLabScore = ProcessLabScore(),
    val events: List<ProcessEvent> = emptyList()
)

data class ProcessIdentity(
    val packageName: String = "-",
    val processName: String = "-",
    val pid: String = "-",
    val ppid: String = "-",
    val uid: String = "-",
    val uidLine: String = "-",
    val oomScoreAdj: String = "-",
    val threadName: String = "-",
    val threadCount: String = "-",
    val singleton: String = "-"
)

data class StartupTrace(
    val providerCreated: String = "-",
    val applicationCreated: String = "-",
    val activityCreated: String = "-",
    val zygoteStory: String = "等待观察冷启动链路。",
    val commandHint: String = "adb shell am kill 包名 / adb shell am force-stop 包名 / adb shell cat /proc/<pid>/oom_score_adj"
)

data class RemoteProcessState(
    val bound: Boolean = false,
    val processName: String = "-",
    val pid: String = "-",
    val uid: String = "-",
    val threadName: String = "-",
    val singleton: String = "-",
    val oomScoreAdj: String = "-",
    val lastReply: String = "尚未绑定 remote service。",
    val binderDeath: String = "尚未观察 Binder death。"
)

data class IsolatedProcessState(
    val bound: Boolean = false,
    val processName: String = "-",
    val pid: String = "-",
    val uid: String = "-",
    val threadName: String = "-",
    val oomScoreAdj: String = "-",
    val lastReply: String = "尚未绑定 isolated service。",
    val binderDeath: String = "尚未观察 isolated Binder death。"
)

data class ProcessRecoveryState(
    val memoryDraft: String = "0",
    val persistentDraft: String = "0",
    val currentPid: String = "-",
    val savedPid: String = "-",
    val lastSavedAt: String = "-",
    val verdict: String = "内存草稿会随进程死亡消失；持久化草稿可以在进程重建后恢复。"
)

data class OomSnapshot(
    val label: String,
    val processName: String,
    val pid: String,
    val oomScoreAdj: String,
    val timestamp: String
)

data class AnrLabState(
    val selectedScenario: String = "尚未触发 ANR 场景。",
    val lastAction: String = "-",
    val expectedTrace: String = "选择一个场景后，观察 main / Binder / Service / Broadcast 的线程状态。",
    val warning: String = "这些按钮会故意阻塞线程，可能触发系统 ANR 弹窗。请在 debug 环境使用。"
)

data class AnrTraceGuide(
    val title: String = "等待选择 ANR 场景。",
    val mainThreadClue: String = "先看 main 线程是否处于 RUNNABLE、BLOCKED、WAITING 或 TIMED_WAITING。",
    val binderClue: String = "再看 Binder 线程池是否正在等待远端、持锁调用或被耗时任务占住。",
    val lockClue: String = "继续找 held lock / waiting lock，判断谁拿着锁，谁在等锁。",
    val systemClue: String = "最后把 trace 和 logcat 里的 Input dispatching timed out、Broadcast timeout、Service timeout 对齐。",
    val conclusion: String = "ANR 不是只看一行栈，而是把主线程、锁、Binder、系统超时原因放在一起读。"
)

data class ThreadProbe(
    val mainThread: String = "main: UI、输入、生命周期、帧调度。",
    val binderThread: String = "Binder:<pid>_<n>: 处理进入本进程的 IPC。",
    val renderThread: String = "RenderThread: 执行硬件渲染相关工作。",
    val workerThread: String = "worker: 网络、数据库、文件和 CPU 任务。",
    val dumpReadingOrder: String = "先看 main，再找锁持有方，再看 Binder 线程池和 worker 队列。"
)

data class ProcessDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

data class ProcessExperiment(
    val operation: String = "等待进程实验",
    val expected: String = "先判断：这次实验观察进程身份、Zygote、remote 进程、ANR，还是后台回收？",
    val actual: String = "点击实验卡后收集证据。",
    val conclusion: String = "还没有证据。"
)

data class ProcessLabScore(
    val identityObserved: Boolean = false,
    val startupObserved: Boolean = false,
    val remoteObserved: Boolean = false,
    val isolatedObserved: Boolean = false,
    val recoveryObserved: Boolean = false,
    val singletonObserved: Boolean = false,
    val anrObserved: Boolean = false,
    val oomObserved: Boolean = false,
    val oomCompared: Boolean = false,
    val threadObserved: Boolean = false,
    val traceObserved: Boolean = false,
    val diagnosisObserved: Boolean = false,
    val reportReady: Boolean = false
)

data class ProcessEvent(
    val source: String,
    val phase: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val defaultProcessDiagnosticCards = listOf(
    ProcessDiagnosticCard(
        title = "后台回来白屏",
        symptom = "切后台一段时间后回到页面，UI 空白或状态丢失。",
        firstEvidence = "pid 是否变化、Application 是否重新创建、savedInstanceState 是否存在。",
        fixDirection = "关键状态持久化，进程重建后恢复 UI，不依赖内存缓存长期存活。"
    ),
    ProcessDiagnosticCard(
        title = "多进程单例错乱",
        symptom = "主进程和 remote / push 进程看到的登录态或缓存不一致。",
        firstEvidence = "processName、pid、单例值、状态来源、IPC / 持久化同步方式。",
        fixDirection = "按进程分流初始化，用 IPC 或可靠持久化同步状态。"
    ),
    ProcessDiagnosticCard(
        title = "Input ANR",
        symptom = "用户点击后长时间无响应。",
        firstEvidence = "main 线程栈、是否 sleep / I/O / 锁等待、输入派发超时日志。",
        fixDirection = "主线程只做短任务，耗时工作放后台，结果再回主线程。"
    ),
    ProcessDiagnosticCard(
        title = "Broadcast / Service ANR",
        symptom = "Receiver 或 Service 生命周期回调长时间阻塞。",
        firstEvidence = "onReceive / onStartCommand 栈、耗时、主线程状态。",
        fixDirection = "回调中快速返回，耗时工作交给 WorkManager、线程池或前台服务。"
    ),
    ProcessDiagnosticCard(
        title = "Binder 阻塞",
        symptom = "IPC 调用卡住、remote 调用超时、主线程等待远端。",
        firstEvidence = "Binder 线程栈、main 线程栈、held lock / waiting lock、Binder death。",
        fixDirection = "避免同步等待，缩小锁范围，增加超时、降级和死亡监听。"
    )
)
