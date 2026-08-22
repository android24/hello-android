package com.helloandroid.background

data class BackgroundLabState(
    val runtime: RuntimeSnapshot = RuntimeSnapshot(),
    val score: BackgroundScore = BackgroundScore(),
    val activeScript: IncidentScript = incidentScripts.first(),
    val scripts: List<IncidentScript> = incidentScripts,
    val decision: TaskDecision = TaskDecision(),
    val foreground: ForegroundServicePanel = ForegroundServicePanel(),
    val work: WorkPanel = WorkPanel(),
    val alarm: AlarmPanel = AlarmPanel(),
    val system: SystemPanel = SystemPanel(),
    val report: DiagnosisReport = DiagnosisReport(),
    val events: List<LabEvent> = emptyList()
)

data class RuntimeSnapshot(
    val packageName: String = "-",
    val processName: String = "-",
    val pid: String = "-",
    val uid: String = "-",
    val threadName: String = "-",
    val sdk: String = "-"
)

data class BackgroundScore(
    val runtimeObserved: Boolean = false,
    val decisionMade: Boolean = false,
    val foregroundObserved: Boolean = false,
    val workObserved: Boolean = false,
    val workConstraintObserved: Boolean = false,
    val alarmRegistered: Boolean = false,
    val alarmTriggered: Boolean = false,
    val systemCommandRead: Boolean = false,
    val adbChallengeCompleted: Boolean = false,
    val scriptCompleted: Boolean = false,
    val reportReady: Boolean = false
)

data class IncidentScript(
    val id: String,
    val title: String,
    val symptom: String,
    val firstClue: String,
    val hiddenTrap: String,
    val nextMove: String,
    val expectedMechanism: String
)

data class TaskDecision(
    val scenario: String = "等待选择后台任务场景。",
    val userVisible: String = "-",
    val mustRunNow: String = "-",
    val mustRecover: String = "-",
    val recommended: String = "先用三问法判断：用户是否感知、是否必须现在完成、失败后是否必须恢复。",
    val reason: String = "-"
)

data class ForegroundServicePanel(
    val status: String = "未启动",
    val serviceType: String = "dataSync",
    val notification: String = "前台服务需要用户可感知通知。",
    val diagnosis: String = "适合用户正在感知的持续任务，例如离线课程下载。"
)

data class WorkPanel(
    val workName: String = "course-progress-sync",
    val workId: String = "-",
    val state: String = "IDLE",
    val constraints: String = "network connected；可切换到 Wi-Fi + charging 思考延迟原因。",
    val retry: String = "失败后退避重试，任务必须幂等。",
    val diagnosis: String = "Work 入队不等于立刻运行，要观察约束、配额、Doze 和待机桶。",
    val realSnapshot: String = "还没有读取 WorkManager 的真实 WorkInfo。",
    val attemptCount: String = "-",
    val lastObservedAt: String = "-"
)

data class AlarmPanel(
    val requestCode: String = "-",
    val expectedAt: String = "-",
    val actual: String = "等待提醒触发。",
    val exactness: String = "本 demo 使用非精确短延迟提醒，真实精确闹钟需要更强用户理由。"
)

data class SystemPanel(
    val commandHint: String = defaultCommandHint,
    val decisionMap: String = defaultDecisionMap,
    val challengeGuide: String = defaultAdbChallengeGuide,
    val challengeResult: String = "还没有完成 ADB 挑战。请选择一个系统入口，执行命令后记录观察。"
)

data class DiagnosisReport(
    val title: String = "-",
    val evidence: String = "-",
    val systemState: String = "-",
    val rootCause: String = "-",
    val fix: String = "-",
    val regression: String = "-"
)

data class LabEvent(
    val source: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val incidentScripts = listOf(
    IncidentScript(
        id = "offline-download-stopped",
        title = "剧本 A：离线课程切后台后停住",
        symptom = "下载到一半切到其他 App，回来后发现进度消失。",
        firstClue = "下载任务只在页面协程里运行，进度没有完整持久化。",
        hiddenTrap = "这不是简单的系统杀后台，而是任务语义选错了。",
        nextMove = "选择离线下载场景，启动前台服务，观察通知、时间线和停止动作。",
        expectedMechanism = "Foreground Service + 持久化进度"
    ),
    IncidentScript(
        id = "progress-sync-delayed",
        title = "剧本 B：学习记录同步晚了半小时",
        symptom = "用户切后台后学习记录没有马上同步，但后来又成功了。",
        firstClue = "Work 已经 ENQUEUED，约束要求网络和充电。",
        hiddenTrap = "延迟不是丢失；可能是约束、Doze、待机桶或配额在起作用。",
        nextMove = "入队 Work，观察状态机和命令卡里的 jobscheduler 证据。",
        expectedMechanism = "WorkManager / JobScheduler"
    ),
    IncidentScript(
        id = "reminder-late",
        title = "剧本 C：学习提醒晚了几分钟",
        symptom = "每天 8 点学习提醒，有些设备 8 点 06 分才弹。",
        firstClue = "使用普通 Alarm，设备可能处于 Doze。",
        hiddenTrap = "普通 Alarm 的语义不是精确准点，不能直接判为 bug。",
        nextMove = "注册提醒，记录期望时间和实际触发，再阅读 dumpsys alarm / deviceidle 命令。",
        expectedMechanism = "AlarmManager + 通知"
    )
)

const val defaultCommandHint = """adb shell dumpsys activity services
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys deviceidle
adb shell dumpsys battery
adb shell dumpsys notification
adb shell dumpsys activity processes"""

const val defaultDecisionMap = """业务请求
  -> 用户可感知持续任务：Foreground Service
  -> 可靠但可延迟任务：WorkManager / JobScheduler
  -> 时间触发任务：AlarmManager
  -> 高频轮询 / 保活诉求：重新设计业务语义

系统继续判断：
  uid state / process state / notification / permission / FGS type
  Doze / App Standby / Battery Saver / quota / constraints"""

const val defaultAdbChallengeGuide = """挑战 1：Work 证据
adb shell dumpsys jobscheduler | grep com.helloandroid.background

挑战 2：Alarm 证据
adb shell dumpsys alarm | grep com.helloandroid.background

挑战 3：Doze 证据
adb shell dumpsys deviceidle

读法：
先看任务有没有被系统接收，再看约束、idle、quota 或 alarm window。"""
