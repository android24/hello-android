package com.helloandroid.stability

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object StabilityLabStore {
    private const val TAG = "StabilityLab"
    private val cacheLock = Any()
    private val _state = MutableStateFlow(StabilityLabState())
    private var remoteMessenger: Messenger? = null
    private var remoteConnection: ServiceConnection? = null

    val state: StateFlow<StabilityLabState> = _state

    fun startIncidentScript(script: StabilityIncidentScript) {
        val transform: StabilityLabState.() -> StabilityLabState = when (script.id) {
            "course-detail-lock-anr" -> {
                {
                    copy(
                        activeScript = script,
                        experiment = StabilityExperiment(
                            operation = "进入事故剧本：${script.title}",
                            expected = "先判断为什么没有 Crash 记录，却出现应用无响应。",
                            actual = script.firstClue,
                            conclusion = "第一假设：这是 Input ANR，需要继续读 main 状态和等待链。"
                        ),
                        anr = AnrExperiment(
                            scenario = "剧本 A / 课程详情页锁等待",
                            lastAction = "选择事故剧本，准备复现锁等待 ANR。",
                            expectedReason = "Input dispatching timed out；main waiting to lock CourseCache。"
                        ),
                        trace = TraceReadingCard(
                            title = "剧本 A trace 阅读",
                            reason = "Input dispatching timed out。",
                            mainThread = "main: BLOCKED, waiting to lock CourseCache。",
                            waitingChain = "CourseCache-worker 持有 CourseCache，并同步等待 remote Service。",
                            conclusion = "根因不是点击事件本身，而是持锁线程把远端慢调用包进了锁里。"
                        ),
                        score = score.copy(anrObserved = true, traceObserved = true, lockWaitObserved = true),
                        report = DiagnosisReport(
                            type = script.expectedReportType,
                            evidence = "Input ANR + main waiting to lock CourseCache。",
                            rootCause = "worker 持有业务锁，并在锁内同步等待 remote Service。",
                            fix = "缩小锁范围，锁外发起 remote 请求，主线程只读快照并展示加载态。",
                            regression = "构造 remote 慢回复和连续点击，验证 main 不再 BLOCKED。"
                        )
                    )
                }
            }

            "background-worker-crash" -> {
                {
                    copy(
                        activeScript = script,
                        experiment = StabilityExperiment(
                            operation = "进入事故剧本：${script.title}",
                            expected = "确认后台线程异常是否同样会导致进程退出。",
                            actual = script.firstClue,
                            conclusion = "第一假设：这是 Java Crash，关键证据是线程名和第一业务栈。"
                        ),
                        crash = JavaCrashCard(
                            title = "后台线程 Crash 样例",
                            exceptionType = "IllegalStateException",
                            threadName = "CrashWorker",
                            firstAppStack = "DownloadRepository.markCompleted(DownloadRepository.kt:88)",
                            diagnosis = "下载状态机已经关闭后仍写入完成态，应修复生命周期和并发状态。",
                            sample = backgroundCrashSample
                        ),
                        score = score.copy(javaCrashObserved = true),
                        report = DiagnosisReport(
                            type = script.expectedReportType,
                            evidence = "FATAL EXCEPTION: CrashWorker + first app stack。",
                            rootCause = "后台任务在资源关闭后继续写状态，触发非法状态异常。",
                            fix = "给下载任务增加生命周期取消、状态机校验和幂等提交。",
                            regression = "覆盖取消下载、切后台、进程重建和重复完成回调。"
                        )
                    )
                }
            }

            else -> {
                {
                    copy(
                        activeScript = script,
                        experiment = StabilityExperiment(
                            operation = "进入事故剧本：${script.title}",
                            expected = "区分 Java 边界和 native 根因，找到 tombstone 第一证据。",
                            actual = script.firstClue,
                            conclusion = "第一假设：这是 Native Crash，需要 tombstone、ABI、so 和符号化。"
                        ),
                        nativeCrash = NativeCrashCard(),
                        systemEvidence = SystemEvidenceCard(
                            title = "Native crash 系统证据",
                            dropboxTag = "data_app_native_crash",
                            commandHint = "adb shell dumpsys dropbox / adb bugreport / tombstone",
                            diagnosis = "DropBox 找入口，tombstone 找 signal、fault addr、崩溃线程和 so。",
                            sample = nativeCrashDropBoxSample
                        ),
                        score = score.copy(nativeCrashObserved = true, systemEvidenceObserved = true),
                        report = DiagnosisReport(
                            type = script.expectedReportType,
                            evidence = "SIGSEGV + fault addr 0x0 + libcourse_image.so。",
                            rootCause = "native 图片解码链路疑似空指针或非法输入。",
                            fix = "保留符号表，按 ABI 符号化 backtrace，校验 JNI 输入和图片边界。",
                            regression = "覆盖 arm64 / armeabi-v7a、异常图片和低内存解码路径。"
                        )
                    )
                }
            }
        }

        _state.update { state -> state.transform() }
        record("Script", "START", script.title)
    }

    fun refresh(context: Context) {
        val identity = runtimeIdentity(context)
        _state.update {
            it.copy(
                identity = identity,
                score = it.score.copy(identityObserved = true),
                experiment = StabilityExperiment(
                    operation = "读取运行现场",
                    expected = "记录 packageName、processName、pid、uid、threadName 和 oom_score_adj。",
                    actual = "process=${identity.processName}, pid=${identity.pid}, thread=${identity.threadName}",
                    conclusion = "稳定性诊断先确认事故发生在哪个进程、哪个线程和哪个状态。"
                )
            )
        }
        record("Runtime", "REFRESH", "pid=${identity.pid}, process=${identity.processName}")
    }

    fun simulateMainThreadAnr(delayMs: Long = 6_500L) {
        _state.update {
            it.copy(
                anr = AnrExperiment(
                    scenario = "Input ANR / main sleep",
                    lastAction = "主线程 sleep ${delayMs}ms",
                    expectedReason = "Input dispatching timed out；main 线程 TIMED_WAITING，无法处理输入和绘制。"
                ),
                trace = TraceReadingCard(
                    title = "main sleep trace 阅读",
                    reason = "系统等待输入事件完成处理。",
                    mainThread = "main 停在 Thread.sleep / TIMED_WAITING。",
                    waitingChain = "没有锁等待时，先判断是否是主线程自阻塞。",
                    conclusion = "主线程不能承载耗时任务；耗时工作应转到后台并回主线程提交结果。"
                ),
                score = it.score.copy(anrObserved = true, traceObserved = true),
                report = DiagnosisReport(
                    type = "Input ANR",
                    evidence = "main sleep ${delayMs}ms",
                    rootCause = "主线程主动阻塞。",
                    fix = "移出主线程，改为异步任务和加载态。",
                    regression = "连续点击和滑动时 main 不再长时间 TIMED_WAITING。"
                )
            )
        }
        record("ANR", "MAIN_SLEEP_START", "delay=${delayMs}ms")
        Thread.sleep(delayMs)
        record("ANR", "MAIN_SLEEP_END", "main resumed")
    }

    fun simulateLockWaitAnr(delayMs: Long = 8_000L) {
        Thread {
            synchronized(cacheLock) {
                record("ANR", "LOCK_HELD", "worker holds CourseCache for ${delayMs}ms")
                Thread.sleep(delayMs)
            }
        }.apply { name = "CourseCache-worker" }.start()

        Handler(Looper.getMainLooper()).postDelayed({
            _state.update {
                it.copy(
                    anr = AnrExperiment(
                        scenario = "Input ANR / lock wait",
                        lastAction = "worker 持有锁，main 尝试进入同一把锁",
                        expectedReason = "main 线程 BLOCKED，trace 里应继续找 locked 同一对象的线程。"
                    ),
                    trace = TraceReadingCard(
                        title = "锁等待 trace 阅读",
                        reason = "Input dispatching timed out。",
                        mainThread = "main: BLOCKED, waiting to lock CourseCache。",
                        waitingChain = "CourseCache-worker: locked CourseCache，并在锁内 sleep。",
                        conclusion = "真正根因在持锁线程；修复要缩小锁范围，避免锁内耗时。"
                    ),
                    score = it.score.copy(
                        anrObserved = true,
                        traceObserved = true,
                        lockWaitObserved = true
                    ),
                    report = DiagnosisReport(
                        type = "Input ANR / lock wait",
                        evidence = "main waiting to lock CourseCache。",
                        rootCause = "worker 持有业务锁执行耗时任务。",
                        fix = "锁内只读写内存快照，耗时 I/O 和 remote 调用移出锁外。",
                        regression = "构造 worker 持锁延迟，main 不再 BLOCKED。"
                    )
                )
            }
            record("ANR", "MAIN_WAIT_LOCK", "main tries CourseCache")
            synchronized(cacheLock) {
                record("ANR", "MAIN_LOCK_ENTERED", "main entered CourseCache")
            }
        }, 250L)
    }

    fun sendSlowBroadcast(context: Context, delayMs: Long = 12_000L) {
        context.sendBroadcast(Intent(context, SlowBroadcastReceiver::class.java).apply {
            putExtra(SlowBroadcastReceiver.EXTRA_DELAY_MS, delayMs)
        })
        _state.update {
            it.copy(
                anr = AnrExperiment(
                    scenario = "Broadcast ANR",
                    lastAction = "发送慢广播，onReceive sleep ${delayMs}ms",
                    expectedReason = "Broadcast timeout；onReceive 长时间占用主线程。"
                ),
                trace = TraceReadingCard(
                    title = "Broadcast trace 阅读",
                    reason = "Broadcast of Intent timeout。",
                    mainThread = "main 停在 SlowBroadcastReceiver.onReceive。",
                    waitingChain = "如果 onReceive 内等待锁或 I/O，继续找持锁线程或慢调用。",
                    conclusion = "Receiver 应快速返回，把耗时任务交给 WorkManager / 线程池。"
                ),
                score = it.score.copy(anrObserved = true, traceObserved = true)
            )
        }
        record("ANR", "BROADCAST_SENT", "delay=${delayMs}ms")
    }

    fun startSlowService(context: Context, delayMs: Long = 16_000L) {
        context.startService(Intent(context, SlowStartService::class.java).apply {
            putExtra(SlowStartService.EXTRA_DELAY_MS, delayMs)
        })
        _state.update {
            it.copy(
                anr = AnrExperiment(
                    scenario = "Service ANR",
                    lastAction = "startService，onStartCommand sleep ${delayMs}ms",
                    expectedReason = "executing service timeout；Service 回调默认仍在主线程。"
                ),
                trace = TraceReadingCard(
                    title = "Service trace 阅读",
                    reason = "executing service timeout。",
                    mainThread = "main 停在 SlowStartService.onStartCommand。",
                    waitingChain = "Service 不是后台线程；如果要做长任务，应启动后台执行单元。",
                    conclusion = "onStartCommand 要快速返回，耗时任务用可靠后台方案。"
                ),
                score = it.score.copy(anrObserved = true, traceObserved = true)
            )
        }
        record("ANR", "SERVICE_STARTED", "delay=${delayMs}ms")
    }

    fun bindRemote(context: Context) {
        val appContext = context.applicationContext
        if (remoteConnection != null) {
            blockRemoteBinder()
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                remoteMessenger = Messenger(service)
                _state.update {
                    it.copy(
                        experiment = StabilityExperiment(
                            operation = "绑定 remote Service",
                            expected = "建立跨进程 Messenger，用于模拟远端 Binder 延迟。",
                            actual = "connected=${name.className}",
                            conclusion = "Binder 诊断需要同时看主进程和 remote 进程。"
                        )
                    )
                }
                record("Remote", "CONNECTED", name.className)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                remoteMessenger = null
                remoteConnection = null
                record("Remote", "DISCONNECTED", name.className)
            }
        }
        remoteConnection = connection
        appContext.bindService(
            Intent(appContext, RemoteDelayService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun blockRemoteBinder(delayMs: Long = 8_000L) {
        val messenger = remoteMessenger
        if (messenger == null) {
            _state.update {
                it.copy(experiment = it.experiment.copy(actual = "remote service not bound"))
            }
            return
        }
        val replyHandler = Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == RemoteDelayService.MSG_REPLY) {
                record("Remote", "REPLY", msg.data.getString("note").orEmpty())
            }
            true
        }
        val message = Message.obtain(null, RemoteDelayService.MSG_BLOCK).apply {
            data = Bundle().apply { putLong(RemoteDelayService.KEY_DELAY_MS, delayMs) }
            replyTo = Messenger(replyHandler)
        }
        messenger.send(message)
        _state.update {
            it.copy(
                anr = AnrExperiment(
                    scenario = "Remote Binder delay",
                    lastAction = "请求 remote 进程延迟 ${delayMs}ms 回复",
                    expectedReason = "本 demo 使用异步 Messenger，不会直接卡主进程；真实项目若 main 同步等待远端，可能扩散成 ANR。"
                ),
                trace = TraceReadingCard(
                    title = "Binder trace 阅读",
                    reason = "如果主线程同步等待 Binder，先看 main 停在哪个 transact 或等待点。",
                    mainThread = "本 demo 主进程 main 不应阻塞，因为 IPC 是异步。",
                    waitingChain = "remote main 会延迟回复；真实事故要同时看两个进程的 trace。",
                    conclusion = "Binder 问题要跨进程读证据，避免持锁同步 IPC。"
                ),
                score = it.score.copy(
                    anrObserved = true,
                    traceObserved = true,
                    binderWaitObserved = true
                )
            )
        }
        record("Remote", "BLOCK_REQUEST", "delay=${delayMs}ms")
    }

    fun prepareJavaCrashSample() {
        _state.update {
            it.copy(
                crash = JavaCrashCard(),
                score = it.score.copy(javaCrashObserved = true),
                report = DiagnosisReport(
                    type = "Java Crash",
                    evidence = "IllegalStateException + first app stack。",
                    rootCause = "详情页缺少 courseId，业务状态非法。",
                    fix = "补导航参数校验、savedState 恢复和缺参降级页。",
                    regression = "通知跳转、进程重建、深链进入均能展示合法状态。"
                ),
                experiment = StabilityExperiment(
                    operation = "阅读 Java Crash 样例",
                    expected = "提取异常类型、线程和第一业务栈。",
                    actual = "IllegalStateException on main。",
                    conclusion = "Crash 修复要回到业务状态语义，不能只 catch。"
                )
            )
        }
        record("Crash", "SAMPLE", "Java crash sample prepared")
    }

    fun crashMainThread() {
        throw IllegalStateException("Course detail requires courseId")
    }

    fun crashBackgroundThread() {
        Thread {
            throw IllegalStateException("database is closed")
        }.apply { name = "CrashWorker" }.start()
    }

    fun prepareNativeSample() {
        _state.update {
            it.copy(
                nativeCrash = NativeCrashCard(),
                score = it.score.copy(nativeCrashObserved = true),
                report = DiagnosisReport(
                    type = "Native Crash",
                    evidence = "SIGSEGV, fault addr 0x0, libcourse_image.so。",
                    rootCause = "疑似 native 空指针或无效输入。",
                    fix = "符号化 backtrace，检查 JNI 入口、输入图片和 so 版本。",
                    regression = "覆盖 arm64 / armeabi-v7a 与异常图片输入。"
                )
            )
        }
        record("Native", "SAMPLE", "tombstone sample prepared")
    }

    fun prepareSystemEvidence() {
        _state.update {
            it.copy(
                systemEvidence = SystemEvidenceCard(),
                score = it.score.copy(systemEvidenceObserved = true),
                experiment = StabilityExperiment(
                    operation = "阅读系统证据样例",
                    expected = "从 DropBox tag、时间、进程、reason 找到下一步证据。",
                    actual = "data_app_anr + Input dispatching timed out。",
                    conclusion = "DropBox 是索引，bugreport 是材料包，trace / stack / tombstone 才是具体现场。"
                )
            )
        }
        record("System", "EVIDENCE", "DropBox sample prepared")
    }

    fun markDiagnosticRead(title: String) {
        _state.update {
            it.copy(experiment = it.experiment.copy(operation = "阅读诊断卡：$title"))
        }
        record("Diagnosis", "READ", title)
    }

    fun markReportReady() {
        _state.update {
            it.copy(
                score = it.score.copy(reportReady = true),
                report = it.report.copy(
                    regression = if (it.report.regression == "-") {
                        "补充复现场景、修复验证、灰度观察和指标回看。"
                    } else {
                        it.report.regression
                    }
                )
            )
        }
        record("Report", "READY", "diagnosis report marked ready")
    }

    fun clearEvents() {
        _state.update { it.copy(events = emptyList()) }
    }

    fun record(source: String, signal: String, detail: String) {
        Log.d(TAG, "$source $signal: $detail")
        val event = StabilityEvent(
            source = source,
            signal = signal,
            detail = detail,
            timestamp = now()
        )
        _state.update { state ->
            state.copy(events = (listOf(event) + state.events).take(80))
        }
    }
}
