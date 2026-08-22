package com.helloandroid.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LabBackground = Color(0xFFF6F8F3)
private val LabPrimary = Color(0xFF254D63)
private val LabSecondary = Color(0xFFB35F34)
private val LabInk = Color(0xFF1F2528)
private val LabSoft = Color(0xFFE9EFE9)
private val LabWarm = Color(0xFFFFF7EA)
private val LabEvidence = Color(0xFFEFF4FF)

@Composable
fun BackgroundSchedulingLabApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LabPrimary,
            secondary = LabSecondary,
            background = LabBackground,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = LabInk,
            onSurface = LabInk
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(LabBackground)
        ) {
            BackgroundLabScreen()
        }
    }
}

@Composable
private fun BackgroundLabScreen() {
    val context = LocalContext.current
    val state by BackgroundLabStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        BackgroundLabStore.refresh(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LabBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeaderCard() }
        item { ScoreCard(state.score) }
        item { MissionBoardCard(state.score) }
        item { RuntimeCard(state.runtime, onRefresh = { BackgroundLabStore.refresh(context) }) }
        item {
            IncidentScriptCard(
                activeScript = state.activeScript,
                scripts = state.scripts,
                onSelect = BackgroundLabStore::selectScript
            )
        }
        item {
            DecisionCard(
                decision = state.decision,
                onDownload = { BackgroundLabStore.chooseDecision("download") },
                onSync = { BackgroundLabStore.chooseDecision("sync") },
                onReminder = { BackgroundLabStore.chooseDecision("reminder") },
                onPolling = { BackgroundLabStore.chooseDecision("polling") }
            )
        }
        item {
            ForegroundServiceCard(
                panel = state.foreground,
                onStart = { BackgroundLabStore.startForegroundDownload(context) },
                onStop = { BackgroundLabStore.stopForegroundDownload(context) }
            )
        }
        item {
            WorkCard(
                panel = state.work,
                onSimpleWork = { BackgroundLabStore.enqueueSyncWork(context, constrained = false) },
                onConstrainedWork = { BackgroundLabStore.enqueueSyncWork(context, constrained = true) },
                onRefresh = { BackgroundLabStore.refreshWorkSnapshot(context) },
                onCancel = { BackgroundLabStore.cancelSyncWork(context) }
            )
        }
        item {
            AlarmCard(
                panel = state.alarm,
                onSchedule = { BackgroundLabStore.scheduleReminder(context) }
            )
        }
        item {
            SystemCommandCard(
                panel = state.system,
                onRead = BackgroundLabStore::markSystemCommandsRead,
                onJobscheduler = { BackgroundLabStore.markAdbChallenge("jobscheduler") },
                onAlarm = { BackgroundLabStore.markAdbChallenge("alarm") },
                onDeviceIdle = { BackgroundLabStore.markAdbChallenge("deviceidle") }
            )
        }
        item {
            ReportCard(
                report = state.report,
                onReady = BackgroundLabStore::markReportReady
            )
        }
        item {
            EventTrailCard(
                events = state.events,
                onClear = BackgroundLabStore::clearEvents
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第21章 后台调度观察实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "把 Foreground Service、WorkManager、Alarm、Doze 命令卡和后台事故剧本放到同一个面板里，训练“业务语义 -> 系统机制 -> 证据 -> 复盘”的判断路径。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScoreCard(score: BackgroundScore) {
    val completed = score.completedCount()
    val total = score.totalCount()
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("后台调度分数")
            Text("${completed * 100 / total} 分", color = LabPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { completed.toFloat() / total.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = LabSecondary,
            trackColor = LabSoft
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "完成 $completed / $total 个观察点：运行现场、任务决策、前台服务、Work、约束、Alarm 注册、Alarm 触发、命令卡、ADB 挑战、事故剧本和诊断报告。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MissionBoardCard(score: BackgroundScore) {
    LabCard {
        SectionTitle("调度任务板")
        Text(
            text = "按顺序推进：先给后台需求定性，再触发实验，最后用系统证据解释结果。",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        MissionStep("确认运行现场", score.runtimeObserved, "记录 package、process、pid、uid、SDK。")
        MissionStep("完成三问法决策", score.decisionMade, "判断用户是否感知、是否必须现在完成、失败后是否恢复。")
        MissionStep("观察用户可感知任务", score.foregroundObserved, "启动前台服务并观察通知、生命周期和停止。")
        MissionStep("观察可靠任务状态机", score.workObserved, "入队 Work，观察 ENQUEUED / RUNNING / SUCCEEDED。")
        MissionStep("观察约束延迟", score.workConstraintObserved, "入队强约束 Work，理解条件未满足为什么会等待。")
        MissionStep("注册时间触发任务", score.alarmRegistered, "注册一次窗口 Alarm，确认它是时间入口。")
        MissionStep("观察时间触发任务", score.alarmTriggered, "等待提醒触发，对比期望时间和实际时间。")
        MissionStep("观察系统证据", score.systemCommandRead, "读懂 jobscheduler、alarm、deviceidle 的命令入口。")
        MissionStep("完成 ADB 挑战", score.adbChallengeCompleted, "至少执行一个 dumpsys 命令，把系统证据和页面状态对上。")
        MissionStep("完成事故剧本", score.scriptCompleted, "从现象、线索和隐藏陷阱中判断任务机制是否选对。")
        MissionStep("完成复盘报告", score.reportReady, "写出证据、系统状态、根因、修复和回归。")
    }
}

@Composable
private fun MissionStep(title: String, done: Boolean, detail: String) {
    MiniPanel("${if (done) "已完成" else "待完成"} $title") {
        KeyValue("目标", detail)
    }
}

@Composable
private fun RuntimeCard(runtime: RuntimeSnapshot, onRefresh: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("运行现场")
            Button(onClick = onRefresh) {
                Text("刷新")
            }
        }
        KeyValue("package", runtime.packageName)
        KeyValue("process", runtime.processName)
        KeyValue("pid / uid", "${runtime.pid} / ${runtime.uid}")
        KeyValue("thread", runtime.threadName)
        KeyValue("sdk", runtime.sdk)
    }
}

@Composable
private fun IncidentScriptCard(
    activeScript: IncidentScript,
    scripts: List<IncidentScript>,
    onSelect: (IncidentScript) -> Unit
) {
    LabCard(background = LabWarm) {
        SectionTitle("事故剧本模式")
        KeyValue("当前剧本", activeScript.title)
        KeyValue("第一线索", activeScript.firstClue)
        KeyValue("隐藏陷阱", activeScript.hiddenTrap)
        Spacer(modifier = Modifier.height(8.dp))
        scripts.forEach { script ->
            MiniPanel(script.title) {
                KeyValue("现象", script.symptom)
                KeyValue("下一步", script.nextMove)
                Button(onClick = { onSelect(script) }) {
                    Text("开始剧本")
                }
            }
        }
    }
}

@Composable
private fun DecisionCard(
    decision: TaskDecision,
    onDownload: () -> Unit,
    onSync: () -> Unit,
    onReminder: () -> Unit,
    onPolling: () -> Unit
) {
    LabCard {
        SectionTitle("任务决策卡")
        KeyValue("场景", decision.scenario)
        KeyValue("用户感知", decision.userVisible)
        KeyValue("必须现在", decision.mustRunNow)
        KeyValue("必须恢复", decision.mustRecover)
        KeyValue("推荐机制", decision.recommended)
        KeyValue("理由", decision.reason)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "离线下载" to onDownload,
                "学习同步" to onSync,
                "学习提醒" to onReminder,
                "高频轮询" to onPolling
            )
        )
    }
}

@Composable
private fun ForegroundServiceCard(
    panel: ForegroundServicePanel,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    LabCard(background = Color(0xFFFFF0EA)) {
        SectionTitle("Foreground Service 实验区")
        KeyValue("状态", panel.status)
        KeyValue("服务类型", panel.serviceType)
        KeyValue("通知", panel.notification)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "启动下载服务" to onStart,
                "停止下载服务" to onStop
            ),
            filled = true
        )
    }
}

@Composable
private fun WorkCard(
    panel: WorkPanel,
    onSimpleWork: () -> Unit,
    onConstrainedWork: () -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit
) {
    LabCard(background = LabEvidence) {
        SectionTitle("WorkManager / JobScheduler 实验区")
        KeyValue("Work 名称", panel.workName)
        KeyValue("Work id", panel.workId)
        KeyValue("状态", panel.state)
        KeyValue("约束", panel.constraints)
        KeyValue("重试", panel.retry)
        KeyValue("诊断", panel.diagnosis)
        KeyValue("真实快照", panel.realSnapshot)
        KeyValue("尝试次数", panel.attemptCount)
        KeyValue("观察时间", panel.lastObservedAt)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "入队普通同步" to onSimpleWork,
                "入队强约束同步" to onConstrainedWork,
                "刷新真实状态" to onRefresh,
                "取消同步任务" to onCancel
            )
        )
    }
}

@Composable
private fun AlarmCard(panel: AlarmPanel, onSchedule: () -> Unit) {
    LabCard {
        SectionTitle("Alarm 实验区")
        KeyValue("requestCode", panel.requestCode)
        KeyValue("注册时间", panel.expectedAt)
        KeyValue("触发状态", panel.actual)
        KeyValue("精确性", panel.exactness)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onSchedule) {
            Text("注册 15 秒提醒")
        }
    }
}

@Composable
private fun SystemCommandCard(
    panel: SystemPanel,
    onRead: () -> Unit,
    onJobscheduler: () -> Unit,
    onAlarm: () -> Unit,
    onDeviceIdle: () -> Unit
) {
    LabCard(background = Color(0xFFF1F5EC)) {
        SectionTitle("系统证据命令卡")
        Text(
            text = "命令不会自动执行，请在终端配合 adb 观察。重点不是背命令，而是知道第一证据在哪里。",
            style = MaterialTheme.typography.bodySmall
        )
        CodeBlock(panel.commandHint)
        CodeBlock(panel.decisionMap)
        CodeBlock(panel.challengeGuide)
        KeyValue("挑战结果", panel.challengeResult)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "标记已阅读命令卡" to onRead,
                "完成 Work 证据" to onJobscheduler,
                "完成 Alarm 证据" to onAlarm,
                "完成 Doze 证据" to onDeviceIdle
            )
        )
    }
}

@Composable
private fun ReportCard(report: DiagnosisReport, onReady: () -> Unit) {
    LabCard {
        SectionTitle("后台任务诊断报告")
        KeyValue("标题", report.title)
        KeyValue("证据", report.evidence)
        KeyValue("系统状态", report.systemState)
        KeyValue("根因", report.rootCause)
        KeyValue("修复", report.fix)
        KeyValue("回归", report.regression)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onReady) {
            Text("标记报告完成")
        }
    }
}

@Composable
private fun EventTrailCard(events: List<LabEvent>, onClear: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("事件时间线")
            OutlinedButton(onClick = onClear) {
                Text("清空")
            }
        }
        if (events.isEmpty()) {
            Text("暂无事件。先刷新运行现场，或触发一个后台实验。")
        } else {
            events.take(16).forEach { event ->
                MiniPanel("${event.source} / ${event.signal}") {
                    KeyValue("detail", event.detail)
                    KeyValue("time", event.timestamp)
                }
            }
        }
    }
}

@Composable
private fun ActionGrid(
    actions: List<Pair<String, () -> Unit>>,
    filled: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { (label, action) ->
                    if (filled) {
                        Button(
                            onClick = action,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = action,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LabCard(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    content: @Composable Column.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun MiniPanel(title: String, content: @Composable Column.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(LabSoft, MaterialTheme.shapes.small)
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = LabPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun CodeBlock(text: String) {
    Text(
        text = text.trim(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFF16262D), MaterialTheme.shapes.small)
            .padding(12.dp),
        color = Color(0xFFEAF2F1),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun KeyValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(104.dp),
            style = MaterialTheme.typography.bodySmall,
            color = LabPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value.ifBlank { "-" },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

private fun BackgroundScore.completedCount(): Int {
    return listOf(
        runtimeObserved,
        decisionMade,
        foregroundObserved,
        workObserved,
        workConstraintObserved,
        alarmRegistered,
        alarmTriggered,
        systemCommandRead,
        adbChallengeCompleted,
        scriptCompleted,
        reportReady
    ).count { it }
}

private fun BackgroundScore.totalCount(): Int = 11
