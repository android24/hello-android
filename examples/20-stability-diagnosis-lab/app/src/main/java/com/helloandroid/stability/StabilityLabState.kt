package com.helloandroid.stability

data class StabilityLabState(
    val identity: RuntimeIdentity = RuntimeIdentity(),
    val score: StabilityScore = StabilityScore(),
    val experiment: StabilityExperiment = StabilityExperiment(),
    val activeScript: StabilityIncidentScript = defaultIncidentScripts.first(),
    val incidentScripts: List<StabilityIncidentScript> = defaultIncidentScripts,
    val anr: AnrExperiment = AnrExperiment(),
    val trace: TraceReadingCard = TraceReadingCard(),
    val crash: JavaCrashCard = JavaCrashCard(),
    val nativeCrash: NativeCrashCard = NativeCrashCard(),
    val systemEvidence: SystemEvidenceCard = SystemEvidenceCard(),
    val report: DiagnosisReport = DiagnosisReport(),
    val diagnosticCards: List<StabilityDiagnosticCard> = defaultDiagnosticCards,
    val events: List<StabilityEvent> = emptyList()
)

data class RuntimeIdentity(
    val packageName: String = "-",
    val processName: String = "-",
    val pid: String = "-",
    val uid: String = "-",
    val threadName: String = "-",
    val oomScoreAdj: String = "-"
)

data class StabilityScore(
    val identityObserved: Boolean = false,
    val anrObserved: Boolean = false,
    val traceObserved: Boolean = false,
    val javaCrashObserved: Boolean = false,
    val nativeCrashObserved: Boolean = false,
    val systemEvidenceObserved: Boolean = false,
    val lockWaitObserved: Boolean = false,
    val binderWaitObserved: Boolean = false,
    val reportReady: Boolean = false
)

data class StabilityExperiment(
    val operation: String = "等待稳定性实验",
    val expected: String = "先判断：这是 ANR、Java Crash、Native Crash、Watchdog、低内存，还是业务失败？",
    val actual: String = "点击实验卡后收集证据。",
    val conclusion: String = "还没有证据。"
)

data class StabilityIncidentScript(
    val id: String,
    val title: String,
    val symptom: String,
    val firstClue: String,
    val hiddenTrap: String,
    val nextMove: String,
    val expectedReportType: String
)

data class AnrExperiment(
    val scenario: String = "尚未触发 ANR 场景。",
    val lastAction: String = "-",
    val expectedReason: String = "选择一个场景后，观察 Input / Broadcast / Service / Binder / lock 等线索。",
    val warning: String = "危险按钮会故意阻塞线程或触发崩溃，请只在 debug 环境使用。"
)

data class TraceReadingCard(
    val title: String = "等待 trace 样例。",
    val reason: String = "先看 ANR reason。",
    val mainThread: String = "再看 main 线程状态。",
    val waitingChain: String = "然后找锁、Binder、worker 和远端进程。",
    val conclusion: String = "最后写出能指导修复的结论。",
    val sample: String = defaultAnrTraceSample
)

data class JavaCrashCard(
    val title: String = "Java Crash 样例",
    val exceptionType: String = "IllegalStateException",
    val threadName: String = "main",
    val firstAppStack: String = "CourseDetailViewModel.load(CourseDetailViewModel.kt:46)",
    val diagnosis: String = "详情页缺少 courseId，不能只 catch；应补参数校验、恢复和降级页。",
    val sample: String = defaultJavaCrashSample
)

data class NativeCrashCard(
    val title: String = "Native tombstone 样例",
    val signal: String = "SIGSEGV",
    val crashingThread: String = "ImageDecoder",
    val library: String = "libcourse_image.so",
    val diagnosis: String = "fault addr 0x0，疑似 native 空指针；需要符号表、ABI、JNI 入口和输入图片。",
    val sample: String = defaultTombstoneSample
)

data class SystemEvidenceCard(
    val title: String = "系统证据样例",
    val dropboxTag: String = "data_app_anr",
    val commandHint: String = "adb logcat / dumpsys activity anr / dumpsys dropbox / bugreport",
    val diagnosis: String = "先用时间、进程、reason 对齐，再进入 trace、stack、tombstone 或 dumpsys。",
    val sample: String = defaultDropBoxSample
)

data class DiagnosisReport(
    val type: String = "-",
    val evidence: String = "-",
    val rootCause: String = "-",
    val fix: String = "-",
    val regression: String = "-"
)

data class StabilityDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

data class StabilityEvent(
    val source: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val defaultIncidentScripts = listOf(
    StabilityIncidentScript(
        id = "course-detail-lock-anr",
        title = "剧本 A：课程详情页卡死",
        symptom = "用户点击课程详情页后界面卡住，几秒后系统弹出应用无响应。",
        firstClue = "logcat 出现 Input dispatching timed out，Crash 平台没有记录。",
        hiddenTrap = "main 不是根因，它只是 waiting to lock CourseCache；真正持锁的是 worker。",
        nextMove = "先读 trace，再触发“锁等待 ANR”，最后写出等待链。",
        expectedReportType = "Input ANR / lock wait"
    ),
    StabilityIncidentScript(
        id = "background-worker-crash",
        title = "剧本 B：下载完成后闪退",
        symptom = "课程视频下载完成后 App 偶发退出，用户只看到回到桌面。",
        firstClue = "logcat 有 FATAL EXCEPTION: CrashWorker，但页面主线程没有直接抛错。",
        hiddenTrap = "后台线程 Crash 依然会让进程退出，不能因为不是 main 就忽略。",
        nextMove = "先读取 Java Crash 样例，再触发“后台 Crash”，观察线程名和第一业务栈。",
        expectedReportType = "Java Crash / background thread"
    ),
    StabilityIncidentScript(
        id = "native-image-tombstone",
        title = "剧本 C：图片页少数机型闪退",
        symptom = "只有部分 ABI / 机型打开课程图片时闪退，Java 栈只有 JNI 边界。",
        firstClue = "DropBox 里有 crash 条目，tombstone 显示 SIGSEGV 和 libcourse_image.so。",
        hiddenTrap = "没有符号表时，Java stack 只能告诉你崩在 native 边界，不能直接定位 native 根因。",
        nextMove = "读取 tombstone 和系统证据样例，标出 signal、fault addr、so、ABI。",
        expectedReportType = "Native Crash"
    )
)

val defaultDiagnosticCards = listOf(
    StabilityDiagnosticCard(
        title = "Input ANR",
        symptom = "点击、滑动、按键后 App 无响应。",
        firstEvidence = "Input dispatching timed out、main 线程状态、锁等待、Binder 等待。",
        fixDirection = "主线程不做耗时任务，不持锁等待远端，必要时异步化和降级。"
    ),
    StabilityDiagnosticCard(
        title = "Java Crash",
        symptom = "App 闪退，logcat 中出现 FATAL EXCEPTION。",
        firstEvidence = "异常类型、线程名、第一业务栈、进程名、pid、版本。",
        fixDirection = "修业务状态语义，保留现场并回归，不靠吞异常续命。"
    ),
    StabilityDiagnosticCard(
        title = "Native Crash",
        symptom = "少数设备或 ABI 闪退，Java 栈不完整。",
        firstEvidence = "signal、fault addr、tombstone、崩溃 so、ABI、BuildId。",
        fixDirection = "符号化 native 栈，检查 JNI、so 版本、ABI 和输入数据。"
    ),
    StabilityDiagnosticCard(
        title = "Watchdog / System Server",
        symptom = "系统整体卡顿、多个 App 受影响或系统服务异常。",
        firstEvidence = "system_server trace、watchdog log、DropBox、bugreport。",
        fixDirection = "区分 App ANR 与系统级问题，按时间线拼接系统证据。"
    ),
    StabilityDiagnosticCard(
        title = "恢复与降级",
        symptom = "无 Crash 但页面空白、功能不可用、后台回来状态丢失。",
        firstEvidence = "pid 是否变化、前后台状态、业务成功率、缓存和降级日志。",
        fixDirection = "状态持久化、失败恢复、超时重试、降级 UI 和监控闭环。"
    )
)

const val defaultAnrTraceSample = """Reason: Input dispatching timed out
Process: com.example.course, PID: 24680

"main" tid=1 BLOCKED
  waiting to lock <0x01> (a CourseCache)
  at CourseRepository.getCurrentCourse(CourseRepository.kt:42)

"DefaultDispatcher-worker-1" tid=31 RUNNABLE
  locked <0x01> (a CourseCache)
  at RemoteCourseService.fetch(RemoteCourseService.kt:77)

"Binder:24680_2" tid=42 WAITING
  at android.os.BinderProxy.transactNative(Native Method)
"""

const val defaultJavaCrashSample = """FATAL EXCEPTION: main
Process: com.example.course, PID: 13579
java.lang.IllegalStateException: Course detail requires courseId
    at CourseDetailViewModel.load(CourseDetailViewModel.kt:46)
    at CourseDetailScreenKt.CourseDetailScreen(CourseDetailScreen.kt:72)
"""

const val backgroundCrashSample = """FATAL EXCEPTION: CrashWorker
Process: com.example.course, PID: 13579
java.lang.IllegalStateException: download state is already closed
    at DownloadRepository.markCompleted(DownloadRepository.kt:88)
    at CourseDownloadWorker.doWork(CourseDownloadWorker.kt:41)
    at java.lang.Thread.run(Thread.java:1012)
"""

const val defaultTombstoneSample = """pid: 24680, tid: 24731, name: ImageDecoder  >>> com.example.course <<<
signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x0
ABI: arm64
backtrace:
  #00 pc 000124a0 libcourse_image.so (ImageDecoder::decodeFrame+64)
  #01 pc 00011988 libcourse_image.so (ImagePipeline::decode+120)
  #02 pc 0036bb44 libart.so (art_quick_generic_jni_trampoline+148)
"""

const val defaultDropBoxSample = """DropBox entry: data_app_anr
Process: com.example.course
Time: 2026-08-16 21:18:42
Reason: Input dispatching timed out

dumpsys activity processes:
  Proc #12: cached com.example.course/u0a123
  pid=24680 adj=900 state=Cached
"""

const val nativeCrashDropBoxSample = """DropBox entry: data_app_native_crash
Process: com.example.course
Time: 2026-08-16 22:06:11
signal: 11 (SIGSEGV), fault addr 0x0
ABI: arm64
library: libcourse_image.so

next evidence:
  tombstone_17
  matching symbols for libcourse_image.so
  input image url and decode options
"""
