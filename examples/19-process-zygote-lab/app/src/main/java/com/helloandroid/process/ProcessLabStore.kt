package com.helloandroid.process

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
import android.os.Process
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ProcessLabStore {
    private const val TAG = "ProcessZygoteLab"
    private const val PREFS = "process_recovery_lab"
    private const val KEY_PERSISTENT_DRAFT = "persistent_draft"
    private const val KEY_SAVED_PID = "saved_pid"
    private const val KEY_LAST_SAVED_AT = "last_saved_at"
    private val _state = MutableStateFlow(ProcessLabState())
    private var remoteMessenger: Messenger? = null
    private var remoteConnection: ServiceConnection? = null
    private var isolatedMessenger: Messenger? = null
    private var isolatedConnection: ServiceConnection? = null

    val state: StateFlow<ProcessLabState> = _state

    fun refresh(context: Context, reason: String = "读取进程身份证") {
        val identity = context.collectIdentity()
        val recovery = context.collectRecovery(identity)
        _state.update {
            it.copy(
                identity = identity,
                recovery = recovery,
                startup = it.startup.copy(
                    providerCreated = findEvent("ContentProvider", "onCreate"),
                    applicationCreated = findEvent("Application", "onCreate"),
                    activityCreated = findEvent("Activity", "onCreate"),
                    zygoteStory = "冷启动时：system_server 请求 Zygote fork，子进程进入 ActivityThread.main，再 bindApplication。"
                ),
                score = it.score.copy(
                    identityObserved = true,
                    startupObserved = true,
                    oomObserved = identity.oomScoreAdj != "-",
                    recoveryObserved = recovery.savedPid != "-" || recovery.memoryDraft != "0"
                ),
                experiment = ProcessExperiment(
                    operation = reason,
                    expected = "读取 packageName、processName、pid、ppid、uid、oom_score_adj、threadName。",
                    actual = "process=${identity.processName}, pid=${identity.pid}, oom=${identity.oomScoreAdj}",
                    conclusion = "进程身份证是排查 Zygote、沙箱、多进程和后台回收问题的第一证据。"
                )
            )
        }
        record("Process", "refresh", "READ", "pid=${identity.pid}, process=${identity.processName}")
    }

    fun increaseMemoryDraft(context: Context) {
        ProcessSessionDraft.memoryDraft += 1
        val identity = context.collectIdentity()
        _state.update {
            it.copy(
                identity = identity,
                recovery = context.collectRecovery(identity),
                score = it.score.copy(recoveryObserved = true),
                experiment = ProcessExperiment(
                    operation = "修改内存草稿",
                    expected = "只写进程内存，杀进程后会丢失。",
                    actual = "memoryDraft=${ProcessSessionDraft.memoryDraft}, pid=${identity.pid}",
                    conclusion = "内存状态适合临时 UI 状态，不适合承担进程死亡后的恢复责任。"
                )
            )
        }
        record("Recovery", "memoryDraft", "INC", "memory=${ProcessSessionDraft.memoryDraft}")
    }

    fun savePersistentDraft(context: Context) {
        val identity = context.collectIdentity()
        val savedAt = now()
        context.recoveryPrefs()
            .edit()
            .putInt(KEY_PERSISTENT_DRAFT, ProcessSessionDraft.memoryDraft)
            .putString(KEY_SAVED_PID, identity.pid)
            .putString(KEY_LAST_SAVED_AT, savedAt)
            .apply()
        _state.update {
            it.copy(
                identity = identity,
                recovery = context.collectRecovery(identity),
                score = it.score.copy(recoveryObserved = true),
                experiment = ProcessExperiment(
                    operation = "保存持久化草稿",
                    expected = "把关键状态写入持久化存储，进程重建后仍可读取。",
                    actual = "persistentDraft=${ProcessSessionDraft.memoryDraft}, savedPid=${identity.pid}",
                    conclusion = "后台回收不可怕，可怕的是关键业务状态只存在内存里。"
                )
            )
        }
        record("Recovery", "persistentDraft", "SAVE", "value=${ProcessSessionDraft.memoryDraft}, pid=${identity.pid}")
    }

    fun restorePersistentDraft(context: Context) {
        val prefs = context.recoveryPrefs()
        ProcessSessionDraft.memoryDraft = prefs.getInt(KEY_PERSISTENT_DRAFT, 0)
        val identity = context.collectIdentity()
        _state.update {
            it.copy(
                identity = identity,
                recovery = context.collectRecovery(identity),
                score = it.score.copy(recoveryObserved = true),
                experiment = ProcessExperiment(
                    operation = "从持久化恢复草稿",
                    expected = "进程死亡后，新进程可以从持久化状态重建 UI。",
                    actual = "memoryDraft=${ProcessSessionDraft.memoryDraft}, currentPid=${identity.pid}",
                    conclusion = "恢复逻辑应该由持久化状态、导航参数和 savedInstanceState 共同兜底。"
                )
            )
        }
        record("Recovery", "persistentDraft", "RESTORE", "memory=${ProcessSessionDraft.memoryDraft}, pid=${identity.pid}")
    }

    fun clearRecovery(context: Context) {
        ProcessSessionDraft.memoryDraft = 0
        context.recoveryPrefs().edit().clear().apply()
        val identity = context.collectIdentity()
        _state.update {
            it.copy(
                identity = identity,
                recovery = context.collectRecovery(identity),
                score = it.score.copy(recoveryObserved = false),
                experiment = ProcessExperiment(
                    operation = "清空恢复实验",
                    expected = "内存草稿和持久化草稿都归零。",
                    actual = "memoryDraft=0, persistentDraft=0",
                    conclusion = "现在可以重新做一次后台死亡恢复实验。"
                )
            )
        }
        record("Recovery", "draft", "CLEAR", "memory and persistent draft cleared")
    }

    fun captureOomSnapshot(context: Context, label: String) {
        val identity = context.collectIdentity()
        val snapshot = OomSnapshot(
            label = label,
            processName = identity.processName,
            pid = identity.pid,
            oomScoreAdj = identity.oomScoreAdj,
            timestamp = now()
        )
        _state.update {
            val snapshots = (listOf(snapshot) + it.oomSnapshots).take(8)
            it.copy(
                identity = identity,
                oomSnapshots = snapshots,
                score = it.score.copy(
                    oomObserved = true,
                    oomCompared = snapshots.size >= 2
                ),
                experiment = ProcessExperiment(
                    operation = "记录 OOM Adj 样本：$label",
                    expected = "对比前台、后台返回、remote、isolated 等不同状态的重要性。",
                    actual = "${snapshot.processName}, pid=${snapshot.pid}, oom=${snapshot.oomScoreAdj}",
                    conclusion = "oom_score_adj 不是进程固定属性，它会随进程重要性和系统调度状态变化。"
                )
            )
        }
        record("OOM", label, "SNAPSHOT", "pid=${identity.pid}, oom=${identity.oomScoreAdj}")
    }

    fun bindRemote(context: Context) {
        val appContext = context.applicationContext
        if (remoteConnection != null) {
            pingRemote()
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                remoteMessenger = Messenger(service)
                service.linkToDeath({
                    _state.update {
                        it.copy(
                            remote = it.remote.copy(
                                bound = false,
                                binderDeath = "remote binder died at ${now()}"
                            ),
                            score = it.score.copy(remoteObserved = true),
                            experiment = it.experiment.copy(
                                operation = "remote binder death",
                                conclusion = "remote 进程死亡后，主进程需要清理状态、重连或降级。"
                            )
                        )
                    }
                    remoteMessenger = null
                    remoteConnection = null
                    record("Remote", "death", "BINDER_DIED", "remote binder died")
                }, 0)
                _state.update {
                    it.copy(
                        remote = it.remote.copy(bound = true, lastReply = "service connected: ${name.className}"),
                        score = it.score.copy(remoteObserved = true),
                        experiment = ProcessExperiment(
                            operation = "绑定 remote Service",
                            expected = "创建 :remote 进程，并通过 Messenger 建立 IPC。",
                            actual = "connected=${name.className}",
                            conclusion = "remote Service 是独立进程，不是后台线程。"
                        )
                    )
                }
                record("Remote", "bind", "CONNECTED", name.className)
                sendRemote(RemoteInspectorService.MSG_PING)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                remoteMessenger = null
                remoteConnection = null
                _state.update {
                    it.copy(remote = it.remote.copy(bound = false, lastReply = "service disconnected"))
                }
                record("Remote", "bind", "DISCONNECTED", name.className)
            }
        }
        remoteConnection = connection
        val bound = appContext.bindService(
            Intent(appContext, RemoteInspectorService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        if (!bound) {
            remoteConnection = null
            _state.update {
                it.copy(remote = it.remote.copy(bound = false, lastReply = "bind remote service failed"))
            }
        }
    }

    fun pingRemote() {
        sendRemote(RemoteInspectorService.MSG_PING)
    }

    fun mutateRemoteSingleton() {
        sendRemote(RemoteInspectorService.MSG_MUTATE_SINGLETON)
    }

    fun blockRemoteBinder(delayMs: Long = 8_000L) {
        sendRemote(RemoteInspectorService.MSG_BLOCK_REMOTE, Bundle().apply {
            putLong(RemoteInspectorService.KEY_DELAY_MS, delayMs)
        })
        _state.update {
            it.copy(
                anr = AnrLabState(
                    selectedScenario = "Remote Binder 阻塞场景",
                    lastAction = "请求 remote 主线程 sleep ${delayMs}ms",
                    expectedTrace = "观察 remote 进程 main / Binder 响应延迟；主进程如果同步等待远端，可能扩散成 ANR。"
                ),
                traceGuide = remoteBinderTraceGuide(delayMs),
                score = it.score.copy(anrObserved = true, threadObserved = true, traceObserved = true)
            )
        }
    }

    fun crashRemote() {
        sendRemote(RemoteInspectorService.MSG_CRASH_REMOTE)
    }

    fun bindIsolated(context: Context) {
        val appContext = context.applicationContext
        if (isolatedConnection != null) {
            pingIsolated()
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                isolatedMessenger = Messenger(service)
                service.linkToDeath({
                    isolatedMessenger = null
                    isolatedConnection = null
                    _state.update {
                        it.copy(
                            isolated = it.isolated.copy(
                                bound = false,
                                binderDeath = "isolated binder died at ${now()}"
                            ),
                            score = it.score.copy(isolatedObserved = true),
                            experiment = it.experiment.copy(
                                operation = "isolated binder death",
                                conclusion = "isolated 进程死亡后，主进程同样只能通过 Binder death 感知远端消失。"
                            )
                        )
                    }
                    record("Isolated", "death", "BINDER_DIED", "isolated binder died")
                }, 0)
                _state.update {
                    it.copy(
                        isolated = it.isolated.copy(bound = true, lastReply = "service connected: ${name.className}"),
                        score = it.score.copy(isolatedObserved = true),
                        experiment = ProcessExperiment(
                            operation = "绑定 isolated Service",
                            expected = "系统为 isolated service 创建受限进程，用独立 uid 运行隔离任务。",
                            actual = "connected=${name.className}",
                            conclusion = "isolatedProcess 适合承载高风险或需隔离的能力，不适合拿来做普通后台保活。"
                        )
                    )
                }
                record("Isolated", "bind", "CONNECTED", name.className)
                sendIsolated(IsolatedInspectorService.MSG_PING)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                isolatedMessenger = null
                isolatedConnection = null
                _state.update {
                    it.copy(isolated = it.isolated.copy(bound = false, lastReply = "service disconnected"))
                }
                record("Isolated", "bind", "DISCONNECTED", name.className)
            }
        }
        isolatedConnection = connection
        val bound = appContext.bindService(
            Intent(appContext, IsolatedInspectorService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        if (!bound) {
            isolatedConnection = null
            _state.update {
                it.copy(isolated = it.isolated.copy(bound = false, lastReply = "bind isolated service failed"))
            }
        }
    }

    fun pingIsolated() {
        sendIsolated(IsolatedInspectorService.MSG_PING)
    }

    fun crashIsolated() {
        sendIsolated(IsolatedInspectorService.MSG_CRASH)
    }

    fun blockMainThread(delayMs: Long = 7_000L) {
        record("ANR", "main", "BLOCK_START", "sleep ${delayMs}ms")
        _state.update {
            it.copy(
                anr = AnrLabState(
                    selectedScenario = "Input / Main Thread ANR 场景",
                    lastAction = "主线程 sleep ${delayMs}ms",
                    expectedTrace = "main 线程 TIMED_WAITING / sleep，输入和绘制无法及时处理。"
                ),
                traceGuide = mainThreadTraceGuide(delayMs),
                score = it.score.copy(anrObserved = true, threadObserved = true, traceObserved = true),
                experiment = ProcessExperiment(
                    operation = "主线程阻塞实验",
                    expected = "UI 会短暂卡住，连续输入可能触发 Input ANR。",
                    actual = "main thread sleeping ${delayMs}ms",
                    conclusion = "主线程是 UI 和生命周期调度入口，不能承载耗时任务。"
                )
            )
        }
        Thread.sleep(delayMs)
        record("ANR", "main", "BLOCK_END", "main thread resumed")
    }

    fun sendSlowBroadcast(context: Context, delayMs: Long = 12_000L) {
        val intent = Intent(context, SlowBroadcastReceiver::class.java).apply {
            putExtra(SlowBroadcastReceiver.EXTRA_DELAY_MS, delayMs)
        }
        context.sendBroadcast(intent)
        _state.update {
            it.copy(
                anr = AnrLabState(
                    selectedScenario = "Broadcast ANR 场景",
                    lastAction = "发送显式广播，Receiver sleep ${delayMs}ms",
                    expectedTrace = "onReceive 长时间占用主线程，观察 Broadcast 处理超时风险。"
                ),
                traceGuide = broadcastTraceGuide(delayMs),
                score = it.score.copy(anrObserved = true, traceObserved = true),
                experiment = it.experiment.copy(
                    operation = "Broadcast ANR 实验",
                    conclusion = "BroadcastReceiver.onReceive 应该快速返回，耗时任务要交出去。"
                )
            )
        }
        record("ANR", "broadcast", "SEND", "delay=${delayMs}ms")
    }

    fun startSlowService(context: Context, delayMs: Long = 22_000L) {
        val intent = Intent(context, SlowStartService::class.java).apply {
            putExtra(SlowStartService.EXTRA_DELAY_MS, delayMs)
        }
        context.startService(intent)
        _state.update {
            it.copy(
                anr = AnrLabState(
                    selectedScenario = "Service ANR 场景",
                    lastAction = "startService，onStartCommand sleep ${delayMs}ms",
                    expectedTrace = "Service 生命周期回调长时间阻塞主线程，观察 Service ANR 风险。"
                ),
                traceGuide = serviceTraceGuide(delayMs),
                score = it.score.copy(anrObserved = true, traceObserved = true),
                experiment = it.experiment.copy(
                    operation = "Service ANR 实验",
                    conclusion = "Service 回调运行在主线程，启动后应快速交给后台任务。"
                )
            )
        }
        record("ANR", "service", "START", "delay=${delayMs}ms")
    }

    fun markDiagnosisRead(title: String) {
        _state.update {
            it.copy(
                score = it.score.copy(diagnosisObserved = true),
                experiment = it.experiment.copy(
                    operation = "阅读诊断卡：$title",
                    conclusion = "进程问题要同时看 processName、pid、线程栈、锁、Binder 和状态恢复。"
                )
            )
        }
        record("Diagnosis", title, "READ", "diagnostic card inspected")
    }

    fun markReportReady() {
        _state.update {
            it.copy(
                score = it.score.copy(reportReady = true),
                experiment = it.experiment.copy(
                    operation = "进程问题诊断报告",
                    conclusion = "已经具备报告证据：pid、uid、oom、remote、线程栈和 ANR 场景。"
                )
            )
        }
        record("Report", "processReport", "READY", "report evidence collected")
    }

    fun clearEvents() {
        _state.update { it.copy(events = emptyList()) }
    }

    fun recordLifecycle(source: String, phase: String, detail: String) {
        record(source, phase, "LIFECYCLE", detail)
    }

    private fun sendRemote(what: Int, data: Bundle = Bundle()) {
        val messenger = remoteMessenger
        if (messenger == null) {
            _state.update {
                it.copy(remote = it.remote.copy(lastReply = "remote service not bound"))
            }
            return
        }
        val replyHandler = Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == RemoteInspectorService.MSG_REPLY) {
                val bundle = msg.data
                val remote = RemoteProcessState(
                    bound = true,
                    processName = bundle.getString("processName").orEmpty(),
                    pid = bundle.getInt("pid").toString(),
                    uid = bundle.getInt("uid").toString(),
                    threadName = bundle.getString("threadName").orEmpty(),
                    singleton = bundle.getString("singleton").orEmpty(),
                    oomScoreAdj = bundle.getString("oom").orEmpty(),
                    lastReply = "${bundle.getString("status")}: ${bundle.getString("note")}",
                    binderDeath = _state.value.remote.binderDeath
                )
                _state.update {
                    it.copy(
                        remote = remote,
                        score = it.score.copy(remoteObserved = true, singletonObserved = true),
                        experiment = ProcessExperiment(
                            operation = "remote IPC 回复",
                            expected = "remote 进程返回自己的 pid、uid、thread、singleton 和 oom_score_adj。",
                            actual = remote.lastReply,
                            conclusion = "remote 进程有独立 pid、主线程和单例状态。"
                        )
                    )
                }
                record("Remote", "reply", "IPC", remote.lastReply)
            }
            true
        }
        val message = Message.obtain(null, what).apply {
            this.data = data
            replyTo = Messenger(replyHandler)
        }
        runCatching { messenger.send(message) }
            .onFailure { error ->
                _state.update {
                    it.copy(remote = it.remote.copy(lastReply = "${error::class.java.simpleName}: ${error.message}"))
                }
            }
    }

    private fun Context.collectIdentity(): ProcessIdentity {
        val pid = Process.myPid()
        return ProcessIdentity(
            packageName = packageName,
            processName = currentProcessName(),
            pid = pid.toString(),
            ppid = readParentPid(pid),
            uid = Process.myUid().toString(),
            uidLine = readUidLine(),
            oomScoreAdj = readProcValue("/proc/$pid/oom_score_adj"),
            threadName = currentThreadName(),
            threadCount = readThreadCountLine(),
            singleton = "value=${ProcessSingleton.value}, createdAt=${ProcessSingleton.createdAt}"
        )
    }

    private fun Context.collectRecovery(identity: ProcessIdentity): ProcessRecoveryState {
        val prefs = recoveryPrefs()
        val persistentDraft = prefs.getInt(KEY_PERSISTENT_DRAFT, 0)
        val savedPid = prefs.getString(KEY_SAVED_PID, "-") ?: "-"
        val verdict = when {
            savedPid != "-" && savedPid != identity.pid ->
                "检测到 savedPid=$savedPid，currentPid=${identity.pid}：这通常意味着进程已经重建，内存草稿可能丢失，但持久化草稿还在。"
            persistentDraft.toString() == ProcessSessionDraft.memoryDraft.toString() && persistentDraft != 0 ->
                "内存草稿与持久化草稿一致，当前 UI 可以从可靠状态恢复。"
            persistentDraft != ProcessSessionDraft.memoryDraft ->
                "内存草稿与持久化草稿不一致：这正是后台死亡、草稿丢失和状态错乱最常见的观察入口。"
            else ->
                "先增加内存草稿并保存，再用 am kill 模拟后台回收，回来后对比 pid 与草稿值。"
        }
        return ProcessRecoveryState(
            memoryDraft = ProcessSessionDraft.memoryDraft.toString(),
            persistentDraft = persistentDraft.toString(),
            currentPid = identity.pid,
            savedPid = savedPid,
            lastSavedAt = prefs.getString(KEY_LAST_SAVED_AT, "-") ?: "-",
            verdict = verdict
        )
    }

    private fun Context.recoveryPrefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun sendIsolated(what: Int) {
        val messenger = isolatedMessenger
        if (messenger == null) {
            _state.update {
                it.copy(isolated = it.isolated.copy(lastReply = "isolated service not bound"))
            }
            return
        }
        val replyHandler = Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == IsolatedInspectorService.MSG_REPLY) {
                val bundle = msg.data
                val isolated = IsolatedProcessState(
                    bound = true,
                    processName = bundle.getString("processName").orEmpty(),
                    pid = bundle.getInt("pid").toString(),
                    uid = bundle.getInt("uid").toString(),
                    threadName = bundle.getString("threadName").orEmpty(),
                    oomScoreAdj = bundle.getString("oom").orEmpty(),
                    lastReply = "${bundle.getString("status")}: ${bundle.getString("note")}",
                    binderDeath = _state.value.isolated.binderDeath
                )
                _state.update {
                    it.copy(
                        isolated = isolated,
                        score = it.score.copy(isolatedObserved = true),
                        experiment = ProcessExperiment(
                            operation = "isolated IPC 回复",
                            expected = "isolated 进程返回自己的 pid、uid、thread 和 oom_score_adj。",
                            actual = isolated.lastReply,
                            conclusion = "isolatedProcess 是比普通 :remote 更强的运行隔离，uid 和权限边界更特殊。"
                        )
                    )
                }
                record("Isolated", "reply", "IPC", isolated.lastReply)
            }
            true
        }
        val message = Message.obtain(null, what).apply {
            replyTo = Messenger(replyHandler)
        }
        runCatching { messenger.send(message) }
            .onFailure { error ->
                _state.update {
                    it.copy(isolated = it.isolated.copy(lastReply = "${error::class.java.simpleName}: ${error.message}"))
                }
            }
    }

    private fun readParentPid(pid: Int): String {
        val stat = readProcValue("/proc/$pid/stat")
        val afterProcessName = stat.substringAfter(") ", missingDelimiterValue = "")
        return afterProcessName.split(" ").getOrNull(1).orEmpty().ifBlank { "-" }
    }

    private fun findEvent(source: String, phase: String): String {
        return _state.value.events.firstOrNull { it.source == source && it.phase == phase }?.timestamp ?: "-"
    }

    private fun mainThreadTraceGuide(delayMs: Long) = AnrTraceGuide(
        title = "Input ANR trace 阅读卡",
        mainThreadClue = "main 线程大概率停在 Thread.sleep / TIMED_WAITING，输入事件和 Choreographer 帧回调都无法及时处理。",
        binderClue = "Binder 线程不一定异常；如果 main 没有等待 Binder，问题先归因到 UI 线程自阻塞。",
        lockClue = "如果不是 sleep，而是 BLOCKED，就继续找 main waiting to lock 的对象和持锁线程。",
        systemClue = "logcat 里通常能看到 Input dispatching timed out，说明输入事件没有在超时时间内完成分发。",
        conclusion = "Input ANR 的第一判断是 main 是否能及时处理消息；${delayMs}ms 的 sleep 足以让用户感知卡死。"
    )

    private fun broadcastTraceGuide(delayMs: Long) = AnrTraceGuide(
        title = "Broadcast ANR trace 阅读卡",
        mainThreadClue = "main 线程会停在 SlowBroadcastReceiver.onReceive，说明广播回调占住了主线程。",
        binderClue = "关注 ActivityThread 处理 Receiver 的消息链路，Binder 只是把广播调度带进 App。",
        lockClue = "如果 onReceive 内部等待锁或 I/O，继续找持锁线程或慢 I/O 调用。",
        systemClue = "系统侧会出现 Broadcast of Intent timeout 一类线索，对齐 Receiver 名称和包名。",
        conclusion = "BroadcastReceiver 的职责是快速接棒，不适合直接执行 ${delayMs}ms 的耗时任务。"
    )

    private fun serviceTraceGuide(delayMs: Long) = AnrTraceGuide(
        title = "Service ANR trace 阅读卡",
        mainThreadClue = "main 线程会停在 SlowStartService.onStartCommand，说明 Service 生命周期回调没有快速返回。",
        binderClue = "Service 启动由系统服务调度进入 App，真正卡住体验的是 App 主线程回调。",
        lockClue = "如果 Service 等待数据库、网络或锁，继续追踪 worker 或锁持有方。",
        systemClue = "logcat / dumpsys activity anr 中寻找 executing service timeout。",
        conclusion = "Service 不等于后台线程；onStartCommand 里 sleep ${delayMs}ms 依然是在堵主线程。"
    )

    private fun remoteBinderTraceGuide(delayMs: Long) = AnrTraceGuide(
        title = "Remote Binder 阻塞 trace 阅读卡",
        mainThreadClue = "先看主进程 main 是否同步等待远端结果；本 demo 使用异步 Messenger，主进程通常不会被直接堵死。",
        binderClue = "再看 remote 进程 main 线程是否停在 RemoteInspectorService 的 sleep，IPC 回复会被延迟。",
        lockClue = "真实项目里最危险的是：主线程持锁同步调 Binder，远端又反向调用或等待同一把锁。",
        systemClue = "结合两个进程的 trace：主进程看等待点，remote 进程看执行点，Binder 问题经常跨进程才完整。",
        conclusion = "Binder 阻塞不是只看一个进程；remote sleep ${delayMs}ms 展示的是远端变慢如何拖长 IPC 链路。"
    )

    private fun record(source: String, phase: String, signal: String, detail: String) {
        Log.d(TAG, "$source.$phase $signal: $detail")
        val event = ProcessEvent(
            source = source,
            phase = phase,
            signal = signal,
            detail = detail,
            timestamp = now()
        )
        _state.update { state ->
            state.copy(events = (listOf(event) + state.events).take(60))
        }
    }
}
