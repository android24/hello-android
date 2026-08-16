package com.helloandroid.stability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LabBackground = Color(0xFFF5F7F4)
private val LabPrimary = Color(0xFF3B5168)
private val LabSecondary = Color(0xFFB75D42)
private val LabInk = Color(0xFF1E2428)
private val LabSoft = Color(0xFFE9EEF0)
private val LabDanger = Color(0xFFFFEFE8)
private val LabEvidence = Color(0xFFF1F4FF)

@Composable
fun StabilityDiagnosisLabApp() {
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
            StabilityLabScreen()
        }
    }
}

@Composable
private fun StabilityLabScreen() {
    val context = LocalContext.current
    val state by StabilityLabStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        StabilityLabStore.refresh(context)
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
        item {
            IncidentScriptCard(
                activeScript = state.activeScript,
                scripts = state.incidentScripts,
                onStart = StabilityLabStore::startIncidentScript
            )
        }
        item { RuntimeCard(state.identity, onRefresh = { StabilityLabStore.refresh(context) }) }
        item { ExperimentCard(state.experiment) }
        item {
            AnrLabCard(
                anr = state.anr,
                onMainSleep = { StabilityLabStore.simulateMainThreadAnr() },
                onLockWait = { StabilityLabStore.simulateLockWaitAnr() },
                onBroadcast = { StabilityLabStore.sendSlowBroadcast(context) },
                onService = { StabilityLabStore.startSlowService(context) },
                onBindRemote = { StabilityLabStore.bindRemote(context) },
                onRemoteDelay = { StabilityLabStore.blockRemoteBinder() }
            )
        }
        item { TraceCard(state.trace) }
        item {
            JavaCrashLabCard(
                crash = state.crash,
                onPrepareSample = StabilityLabStore::prepareJavaCrashSample,
                onCrashMain = StabilityLabStore::crashMainThread,
                onCrashWorker = StabilityLabStore::crashBackgroundThread
            )
        }
        item {
            NativeCrashLabCard(
                nativeCrash = state.nativeCrash,
                onPrepareSample = StabilityLabStore::prepareNativeSample
            )
        }
        item {
            SystemEvidenceLabCard(
                evidence = state.systemEvidence,
                onPrepareSample = StabilityLabStore::prepareSystemEvidence
            )
        }
        item { SectionTitle("稳定性诊断卡") }
        items(state.diagnosticCards) { card ->
            DiagnosticCard(
                card = card,
                onRead = { StabilityLabStore.markDiagnosticRead(card.title) }
            )
        }
        item {
            ReportCard(
                report = state.report,
                onReady = StabilityLabStore::markReportReady
            )
        }
        item {
            EventTrailCard(
                events = state.events,
                onClear = StabilityLabStore::clearEvents
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第20章 稳定性诊断实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "把 ANR、Crash、tombstone、DropBox、bugreport 和诊断报告放到同一个事故面板里，从“发生了什么”一路追到“证据在哪里、怎么修、怎么回归”。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScoreCard(score: StabilityScore) {
    val completed = score.completedCount()
    val total = score.totalCount()
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "稳定性诊断分数",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${completed * 100 / total} 分",
                color = LabPrimary,
                fontWeight = FontWeight.Bold
            )
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
            text = "完成 $completed / $total 个观察点：运行现场、ANR、trace、Java Crash、tombstone、系统证据、锁等待、Binder 等待和诊断报告。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MissionBoardCard(score: StabilityScore) {
    LabCard {
        SectionTitle("事故任务板")
        Text(
            text = "建议按任务顺序推进：先确认现场，再制造事故，最后把证据写成报告。每完成一步，都回头问一句：这个证据能不能支持我的结论？",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        MissionStep(
            index = 1,
            done = score.identityObserved,
            title = "确认案发现场",
            evidence = "刷新运行现场，记下 processName、pid、threadName、oom_score_adj。"
        )
        MissionStep(
            index = 2,
            done = score.anrObserved && score.traceObserved,
            title = "制造一次可解释的 ANR",
            evidence = "触发主线程阻塞、慢 Broadcast 或慢 Service，并能说出 reason。"
        )
        MissionStep(
            index = 3,
            done = score.lockWaitObserved || score.binderWaitObserved,
            title = "拆开等待链",
            evidence = "说明 main 在等谁，谁持有锁，remote 进程是否参与。"
        )
        MissionStep(
            index = 4,
            done = score.javaCrashObserved && score.nativeCrashObserved,
            title = "区分 Java 与 Native 现场",
            evidence = "读出异常类型、第一业务栈、signal、fault addr 和 so。"
        )
        MissionStep(
            index = 5,
            done = score.systemEvidenceObserved && score.reportReady,
            title = "完成稳定性复盘",
            evidence = "把 DropBox / bugreport / trace / stack 串成根因、修复和回归。"
        )
    }
}

@Composable
private fun MissionStep(index: Int, done: Boolean, title: String, evidence: String) {
    MiniPanel("${if (done) "DONE" else "TODO"} 任务 $index：$title") {
        KeyValue("证据", evidence)
    }
}

@Composable
private fun IncidentScriptCard(
    activeScript: StabilityIncidentScript,
    scripts: List<StabilityIncidentScript>,
    onStart: (StabilityIncidentScript) -> Unit
) {
    LabCard(background = Color(0xFFFFFBF1)) {
        SectionTitle("事故剧本模式")
        Text(
            text = "先选一个线上事故，再沿着线索查。剧本不会立刻制造危险操作，它会先把现场、证据和报告草稿摆出来。",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValue("当前剧本", activeScript.title)
        KeyValue("第一线索", activeScript.firstClue)
        KeyValue("隐藏陷阱", activeScript.hiddenTrap)
        Spacer(modifier = Modifier.height(8.dp))
        scripts.forEach { script ->
            MiniPanel(script.title) {
                KeyValue("现象", script.symptom)
                KeyValue("下一步", script.nextMove)
                Button(onClick = { onStart(script) }) {
                    Text("开始剧本")
                }
            }
        }
    }
}

@Composable
private fun RuntimeCard(identity: RuntimeIdentity, onRefresh: () -> Unit) {
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
        KeyValue("package", identity.packageName)
        KeyValue("process", identity.processName)
        KeyValue("pid / uid", "${identity.pid} / ${identity.uid}")
        KeyValue("thread", identity.threadName)
        KeyValue("oom_score_adj", identity.oomScoreAdj)
    }
}

@Composable
private fun ExperimentCard(experiment: StabilityExperiment) {
    LabCard {
        SectionTitle("预期 vs 实际")
        KeyValue("操作", experiment.operation)
        KeyValue("预期", experiment.expected)
        KeyValue("实际", experiment.actual)
        KeyValue("结论", experiment.conclusion)
    }
}

@Composable
private fun AnrLabCard(
    anr: AnrExperiment,
    onMainSleep: () -> Unit,
    onLockWait: () -> Unit,
    onBroadcast: () -> Unit,
    onService: () -> Unit,
    onBindRemote: () -> Unit,
    onRemoteDelay: () -> Unit
) {
    LabCard(background = LabDanger) {
        SectionTitle("ANR 实验区")
        Text(
            text = anr.warning,
            color = LabSecondary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValue("场景", anr.scenario)
        KeyValue("最近动作", anr.lastAction)
        KeyValue("预期 reason", anr.expectedReason)
        Spacer(modifier = Modifier.height(10.dp))
        DangerActionGrid(
            actions = listOf(
                DangerAction(
                    label = "主线程阻塞",
                    warning = "会让当前页面短时间无响应，用来观察 main thread sleep 与 Input ANR。",
                    onConfirm = onMainSleep
                ),
                DangerAction(
                    label = "锁等待 ANR",
                    warning = "会让 worker 先持有 CourseCache，再让 main 等同一把锁，用来观察等待链。",
                    onConfirm = onLockWait
                ),
                DangerAction(
                    label = "慢 Broadcast",
                    warning = "会让 onReceive 在主线程等待，用来观察 Broadcast timeout。",
                    onConfirm = onBroadcast
                ),
                DangerAction(
                    label = "慢 Service",
                    warning = "会让 onStartCommand 在主线程等待，用来观察 Service timeout。",
                    onConfirm = onService
                ),
                DangerAction(
                    label = "绑定 remote",
                    warning = "这是安全准备动作：先建立 remote 进程连接，再观察跨进程延迟。",
                    requiresConfirm = false,
                    onConfirm = onBindRemote
                ),
                DangerAction(
                    label = "remote 延迟",
                    warning = "本 demo 使用异步 Messenger，不会卡主进程；请重点观察 remote 证据和跨进程诊断思路。",
                    onConfirm = onRemoteDelay
                )
            ),
            filled = true
        )
    }
}

@Composable
private fun TraceCard(trace: TraceReadingCard) {
    LabCard(background = LabEvidence) {
        SectionTitle(trace.title)
        KeyValue("reason", trace.reason)
        KeyValue("main", trace.mainThread)
        KeyValue("等待链", trace.waitingChain)
        KeyValue("结论", trace.conclusion)
        CodeBlock(trace.sample)
    }
}

@Composable
private fun JavaCrashLabCard(
    crash: JavaCrashCard,
    onPrepareSample: () -> Unit,
    onCrashMain: () -> Unit,
    onCrashWorker: () -> Unit
) {
    LabCard {
        SectionTitle("Java Crash 实验区")
        KeyValue("异常类型", crash.exceptionType)
        KeyValue("线程", crash.threadName)
        KeyValue("第一业务栈", crash.firstAppStack)
        KeyValue("诊断", crash.diagnosis)
        CodeBlock(crash.sample)
        Spacer(modifier = Modifier.height(10.dp))
        DangerActionGrid(
            actions = listOf(
                DangerAction(
                    label = "读取样例",
                    warning = "这是安全动作：先读异常类型、线程和第一业务栈。",
                    requiresConfirm = false,
                    onConfirm = onPrepareSample
                ),
                DangerAction(
                    label = "触发主线程 Crash",
                    warning = "App 会立即退出。触发前请先打开 logcat，并确认已经读过样例。",
                    onConfirm = onCrashMain
                ),
                DangerAction(
                    label = "后台 Crash",
                    warning = "后台线程会抛出异常，App 也可能退出。请重点观察 FATAL EXCEPTION 的线程名。",
                    onConfirm = onCrashWorker
                )
            )
        )
    }
}

@Composable
private fun NativeCrashLabCard(nativeCrash: NativeCrashCard, onPrepareSample: () -> Unit) {
    LabCard {
        SectionTitle("Native tombstone 观察区")
        KeyValue("signal", nativeCrash.signal)
        KeyValue("线程", nativeCrash.crashingThread)
        KeyValue("so", nativeCrash.library)
        KeyValue("诊断", nativeCrash.diagnosis)
        CodeBlock(nativeCrash.sample)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onPrepareSample) {
            Text("读取 tombstone 样例")
        }
    }
}

@Composable
private fun SystemEvidenceLabCard(evidence: SystemEvidenceCard, onPrepareSample: () -> Unit) {
    LabCard {
        SectionTitle("DropBox / bugreport 证据区")
        KeyValue("DropBox tag", evidence.dropboxTag)
        KeyValue("命令", evidence.commandHint)
        KeyValue("诊断", evidence.diagnosis)
        CodeBlock(evidence.sample)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onPrepareSample) {
            Text("读取系统证据样例")
        }
    }
}

@Composable
private fun DiagnosticCard(card: StabilityDiagnosticCard, onRead: () -> Unit) {
    LabCard(modifier = Modifier.clickable(onClick = onRead)) {
        Text(
            text = card.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValue("现象", card.symptom)
        KeyValue("第一证据", card.firstEvidence)
        KeyValue("修复方向", card.fixDirection)
    }
}

@Composable
private fun ReportCard(report: DiagnosisReport, onReady: () -> Unit) {
    LabCard {
        SectionTitle("稳定性诊断报告")
        KeyValue("类型", report.type)
        KeyValue("证据", report.evidence)
        KeyValue("根因", report.rootCause)
        KeyValue("修复", report.fix)
        KeyValue("回归", report.regression)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onReady) {
            Text("标记报告已完成")
        }
    }
}

@Composable
private fun EventTrailCard(events: List<StabilityEvent>, onClear: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("事件轨迹")
            OutlinedButton(onClick = onClear) {
                Text("清空")
            }
        }
        if (events.isEmpty()) {
            Text("暂无事件。先刷新运行现场，或触发一个实验。", style = MaterialTheme.typography.bodyMedium)
        } else {
            events.take(14).forEach { event ->
                MiniPanel("${event.source} / ${event.signal}") {
                    KeyValue("detail", event.detail)
                    KeyValue("time", event.timestamp)
                }
            }
        }
    }
}

private data class DangerAction(
    val label: String,
    val warning: String,
    val requiresConfirm: Boolean = true,
    val onConfirm: () -> Unit
)

@Composable
private fun DangerActionGrid(
    actions: List<DangerAction>,
    filled: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    DangerActionButton(
                        action = action,
                        filled = filled,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DangerActionButton(
    action: DangerAction,
    filled: Boolean,
    modifier: Modifier = Modifier
) {
    var showConfirm by remember { mutableStateOf(false) }
    val runAction = {
        if (action.requiresConfirm) {
            showConfirm = true
        } else {
            action.onConfirm()
        }
    }

    if (filled) {
        Button(
            onClick = runAction,
            modifier = modifier
        ) {
            Text(action.label)
        }
    } else {
        OutlinedButton(
            onClick = runAction,
            modifier = modifier
        ) {
            Text(action.label)
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("确认进入事故现场") },
            text = { Text(action.warning) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        action.onConfirm()
                    }
                ) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("先不触发")
                }
            }
        )
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
            .background(Color(0xFF172326), MaterialTheme.shapes.small)
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
            modifier = Modifier.width(108.dp),
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

private fun StabilityScore.completedCount(): Int {
    return listOf(
        identityObserved,
        anrObserved,
        traceObserved,
        javaCrashObserved,
        nativeCrashObserved,
        systemEvidenceObserved,
        lockWaitObserved,
        binderWaitObserved,
        reportReady
    ).count { it }
}

private fun StabilityScore.totalCount(): Int = 9
