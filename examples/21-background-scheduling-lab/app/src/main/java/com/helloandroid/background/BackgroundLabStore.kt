package com.helloandroid.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object BackgroundLabStore {
    private const val TAG = "BackgroundLab"
    private const val UNIQUE_SYNC_WORK = "course-progress-sync"
    private val _state = MutableStateFlow(BackgroundLabState())
    private val workQueryExecutor = Executors.newSingleThreadExecutor()

    val state: StateFlow<BackgroundLabState> = _state

    fun refresh(context: Context) {
        val snapshot = runtimeSnapshot(context)
        _state.update {
            it.copy(
                runtime = snapshot,
                score = it.score.copy(runtimeObserved = true)
            )
        }
        record("Runtime", "REFRESH", "pid=${snapshot.pid}, process=${snapshot.processName}")
    }

    fun chooseDecision(scenario: String) {
        val decision = when (scenario) {
            "download" -> TaskDecision(
                scenario = "离线课程下载",
                userVisible = "是，用户正在等待进度。",
                mustRunNow = "是，用户刚刚点击下载。",
                mustRecover = "是，切后台、断网、进程重建后都要能恢复。",
                recommended = "Foreground Service + 通知 + 任务状态持久化。",
                reason = "这是用户可感知的持续数据任务，不能只依赖页面协程。"
            )

            "sync" -> TaskDecision(
                scenario = "学习记录同步",
                userVisible = "否，用户不需要盯着进度。",
                mustRunNow = "否，允许稍后完成。",
                mustRecover = "是，最终必须可靠上传。",
                recommended = "WorkManager / JobScheduler。",
                reason = "可靠、可重试、可约束，比手写 Service 更符合系统调度。"
            )

            "reminder" -> TaskDecision(
                scenario = "每天学习提醒",
                userVisible = "是，提醒本身面向用户。",
                mustRunNow = "取决于产品承诺。",
                mustRecover = "是，重启和时区变化后要恢复。",
                recommended = "AlarmManager + 通知；强时间敏感时再评估精确闹钟。",
                reason = "这是时间触发，不是后台执行框架。"
            )

            else -> TaskDecision(
                scenario = "后台高频轮询",
                userVisible = "否。",
                mustRunNow = "通常不是。",
                mustRecover = "不一定。",
                recommended = "重新设计为 Push、批量同步或用户触发。",
                reason = "高频轮询容易耗电，也不符合后台治理方向。"
            )
        }
        _state.update {
            it.copy(
                decision = decision,
                score = it.score.copy(decisionMade = true),
                report = it.report.copy(
                    title = decision.scenario,
                    evidence = "三问法：userVisible=${decision.userVisible}, mustRunNow=${decision.mustRunNow}",
                    rootCause = decision.reason,
                    fix = decision.recommended
                )
            )
        }
        record("Decision", "SELECT", decision.scenario)
    }

    fun startForegroundDownload(context: Context) {
        val intent = Intent(context, CourseDownloadForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _state.update {
            it.copy(
                foreground = ForegroundServicePanel(
                    status = "启动请求已发送",
                    serviceType = "dataSync",
                    notification = "请观察通知栏中的离线课程下载通知。",
                    diagnosis = "系统会要求它快速进入 foreground，并展示用户可感知通知。"
                ),
                score = it.score.copy(foregroundObserved = true),
                report = it.report.copy(
                    title = "离线课程下载",
                    evidence = "startForegroundService + foreground notification",
                    systemState = "需要通知权限、前台服务权限和 dataSync 类型声明。",
                    rootCause = "用户可感知持续任务不能只靠页面生命周期。",
                    fix = "前台服务承载进度，任务状态持久化，失败后可恢复。",
                    regression = "切后台、熄屏、进程重建、网络切换后进度仍可恢复。"
                )
            )
        }
        record("FGS", "START_REQUEST", "offline course download")
    }

    fun stopForegroundDownload(context: Context) {
        context.stopService(Intent(context, CourseDownloadForegroundService::class.java))
        _state.update {
            it.copy(foreground = it.foreground.copy(status = "已发送停止请求"))
        }
        record("FGS", "STOP_REQUEST", "offline course download")
    }

    fun enqueueSyncWork(context: Context, constrained: Boolean) {
        val constraints = if (constrained) {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()
        } else {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }
        val request = OneTimeWorkRequestBuilder<CourseProgressSyncWorker>()
            .setInitialDelay(2, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag(UNIQUE_SYNC_WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
        _state.update {
            it.copy(
                work = WorkPanel(
                    workId = request.id.toString(),
                    state = WorkInfo.State.ENQUEUED.name,
                    constraints = if (constrained) {
                        "UNMETERED + CHARGING：用于观察约束过严时为什么延迟。"
                    } else {
                        "CONNECTED：更适合学习记录同步。"
                    },
                    diagnosis = "Work 已经入队，接下来要观察约束、JobScheduler、Doze 和执行结果。",
                    realSnapshot = "已创建 WorkRequest，点击“刷新真实状态”读取 WorkManager 保存的 WorkInfo。",
                    attemptCount = "0",
                    lastObservedAt = now()
                ),
                score = it.score.copy(
                    workObserved = true,
                    workConstraintObserved = constrained
                ),
                report = it.report.copy(
                    title = "学习记录同步",
                    evidence = "WorkRequest -> WorkSpec -> JobScheduler -> Worker",
                    systemState = if (constrained) "约束要求 Wi-Fi + charging，可能等待条件。" else "约束只要求 connected。",
                    rootCause = if (constrained) "任务延迟可能是约束未满足，不是任务丢失。" else "可靠同步适合交给 WorkManager。",
                    fix = "为同步任务设计幂等 key、退避重试和任务状态日志。",
                    regression = "断网、4G、Wi-Fi、充电、进程重建下分别验证。"
                )
            )
        }
        record("Work", "ENQUEUED", "id=${request.id}, constrained=$constrained")
        refreshWorkSnapshot(context)
    }

    fun updateWorkState(state: WorkInfo.State, message: String) {
        _state.update {
            it.copy(work = it.work.copy(state = state.name, diagnosis = message))
        }
        record("Work", state.name, message)
    }

    fun refreshWorkSnapshot(context: Context) {
        val future = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UNIQUE_SYNC_WORK)
        future.addListener(
            {
                val activeWorkId = _state.value.work.workId
                val result = runCatching {
                    val infos = future.get()
                    infos.firstOrNull { it.id.toString() == activeWorkId } ?: infos.firstOrNull()
                }
                val latest = result.getOrNull()
                if (latest == null) {
                    _state.update {
                        it.copy(
                            work = it.work.copy(
                                state = "IDLE",
                                realSnapshot = result.exceptionOrNull()?.message
                                    ?: "WorkManager 中没有找到 $UNIQUE_SYNC_WORK。",
                                attemptCount = "-",
                                lastObservedAt = now()
                            )
                        )
                    }
                    record("Work", "SNAPSHOT_EMPTY", "no WorkInfo found")
                    return@addListener
                }
                _state.update {
                    it.copy(
                        work = it.work.copy(
                            workId = latest.id.toString(),
                            state = latest.state.name,
                            realSnapshot = "真实 WorkInfo：state=${latest.state.name}, tags=${latest.tags.joinToString()}",
                            attemptCount = latest.runAttemptCount.toString(),
                            lastObservedAt = now()
                        )
                    )
                }
                record("Work", "SNAPSHOT", "id=${latest.id}, state=${latest.state}, attempts=${latest.runAttemptCount}")
            },
            workQueryExecutor
        )
    }

    fun cancelSyncWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_SYNC_WORK)
        _state.update {
            it.copy(
                work = it.work.copy(
                    diagnosis = "已请求取消唯一同步任务。刷新真实状态后观察 CANCELLED / IDLE。",
                    lastObservedAt = now()
                )
            )
        }
        record("Work", "CANCEL_REQUEST", UNIQUE_SYNC_WORK)
        refreshWorkSnapshot(context)
    }

    fun scheduleReminder(context: Context) {
        val triggerAt = System.currentTimeMillis() + 15_000L
        val requestCode = 2101
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, StudyReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            10_000L,
            pendingIntent
        )
        _state.update {
            it.copy(
                alarm = AlarmPanel(
                    requestCode = requestCode.toString(),
                    expectedAt = "注册于 ${now()}，期望约 15 秒后进入 10 秒窗口。",
                    actual = "已注册 15 秒后的窗口 Alarm，真实触发可能受系统调度影响。",
                    exactness = "setWindow：表达一个触发窗口，而不是强精确闹钟。"
                ),
                score = it.score.copy(alarmRegistered = true),
                report = it.report.copy(
                    title = "学习提醒",
                    evidence = "Alarm registered with setWindow",
                    systemState = "Doze / App Standby / exact alarm permission may affect timing.",
                    rootCause = "普通 Alarm 不承诺秒级准点。",
                    fix = "强时间敏感再评估精确闹钟；普通提醒接受时间窗口。",
                    regression = "记录 expectedAt 和 actualAt，对比 deviceidle 状态。"
                )
            )
        }
        record("Alarm", "REGISTER", "requestCode=$requestCode, window=10s")
    }

    fun onAlarmTriggered() {
        _state.update {
            it.copy(
                alarm = it.alarm.copy(actual = "提醒已触发：${now()}"),
                score = it.score.copy(alarmTriggered = true)
            )
        }
        record("Alarm", "TRIGGERED", "study reminder delivered")
    }

    fun selectScript(script: IncidentScript) {
        _state.update {
            it.copy(
                activeScript = script,
                score = it.score.copy(scriptCompleted = true),
                decision = it.decision.copy(
                    scenario = script.title,
                    recommended = script.expectedMechanism,
                    reason = script.hiddenTrap
                ),
                report = DiagnosisReport(
                    title = script.title,
                    evidence = script.firstClue,
                    rootCause = script.hiddenTrap,
                    fix = script.nextMove,
                    regression = "完成剧本后，用系统命令和事件时间线验证结论。"
                )
            )
        }
        record("Script", "START", script.title)
    }

    fun markSystemCommandsRead() {
        _state.update { it.copy(score = it.score.copy(systemCommandRead = true)) }
        record("System", "COMMANDS", "dumpsys command card read")
    }

    fun markAdbChallenge(challenge: String) {
        val result = when (challenge) {
            "jobscheduler" -> "已完成 Work 证据挑战：请对照 dumpsys jobscheduler，确认任务是否被 JobScheduler 接收、是否存在约束等待。"
            "alarm" -> "已完成 Alarm 证据挑战：请对照 dumpsys alarm，确认 requestCode、触发窗口和是否处于 idle 调整。"
            else -> "已完成 Doze 证据挑战：请对照 dumpsys deviceidle，确认设备 idle 状态是否会影响后台任务。"
        }
        _state.update {
            it.copy(
                system = it.system.copy(challengeResult = result),
                score = it.score.copy(adbChallengeCompleted = true)
            )
        }
        record("System", "ADB_CHALLENGE", challenge)
    }

    fun markReportReady() {
        _state.update { it.copy(score = it.score.copy(reportReady = true)) }
        record("Report", "READY", "background diagnosis report marked ready")
    }

    fun clearEvents() {
        _state.update { it.copy(events = emptyList()) }
    }

    fun record(source: String, signal: String, detail: String) {
        Log.d(TAG, "$source $signal: $detail")
        val event = LabEvent(source, signal, detail, now())
        _state.update { state ->
            state.copy(events = (listOf(event) + state.events).take(80))
        }
    }
}
