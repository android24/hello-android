package com.helloandroid.process

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LabBackground = Color(0xFFF3F6F8)
private val LabPrimary = Color(0xFF245B63)
private val LabSecondary = Color(0xFFC0643B)
private val LabInk = Color(0xFF172326)
private val LabSoft = Color(0xFFE6EEF0)
private val LabWarning = Color(0xFFFFF2E8)

@Composable
fun ProcessZygoteLabApp() {
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
            ProcessLabScreen()
        }
    }
}

@Composable
private fun ProcessLabScreen() {
    val context = LocalContext.current
    val state by ProcessLabStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        ProcessLabStore.refresh(context)
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
        item { ExperimentCard(state.experiment) }
        item {
            IdentityCard(
                identity = state.identity,
                onRefresh = { ProcessLabStore.refresh(context, "手动刷新进程身份证") }
            )
        }
        item {
            SandboxCard(
                packageName = state.identity.packageName,
                uidLine = state.identity.uidLine,
                dataDir = context.applicationInfo.dataDir
            )
        }
        item { StartupCard(state.startup) }
        item {
            RecoveryCard(
                recovery = state.recovery,
                onIncrease = { ProcessLabStore.increaseMemoryDraft(context) },
                onSave = { ProcessLabStore.savePersistentDraft(context) },
                onRestore = { ProcessLabStore.restorePersistentDraft(context) },
                onClear = { ProcessLabStore.clearRecovery(context) }
            )
        }
        item { ThreadModelCard(state.threadProbe) }
        item {
            RemoteProcessCard(
                remote = state.remote,
                onBind = { ProcessLabStore.bindRemote(context) },
                onPing = ProcessLabStore::pingRemote,
                onMutate = ProcessLabStore::mutateRemoteSingleton,
                onBlock = { ProcessLabStore.blockRemoteBinder() },
                onCrash = ProcessLabStore::crashRemote
            )
        }
        item {
            IsolatedProcessCard(
                isolated = state.isolated,
                onBind = { ProcessLabStore.bindIsolated(context) },
                onPing = ProcessLabStore::pingIsolated,
                onCrash = ProcessLabStore::crashIsolated
            )
        }
        item {
            AnrLabCard(
                anr = state.anr,
                traceGuide = state.traceGuide,
                onMainBlock = { ProcessLabStore.blockMainThread() },
                onBroadcast = { ProcessLabStore.sendSlowBroadcast(context) },
                onService = { ProcessLabStore.startSlowService(context) },
                onRemoteBlock = { ProcessLabStore.blockRemoteBinder() }
            )
        }
        item {
            OomAndRecoveryCard(
                identity = state.identity,
                snapshots = state.oomSnapshots,
                onCaptureForeground = { ProcessLabStore.captureOomSnapshot(context, "前台样本") },
                onCaptureAfterBackground = { ProcessLabStore.captureOomSnapshot(context, "后台返回样本") },
                onCaptureAfterRemote = { ProcessLabStore.captureOomSnapshot(context, "remote 后样本") }
            )
        }
        item { SectionTitle("进程问题诊断卡") }
        items(state.diagnosticCards) { card ->
            DiagnosticCard(
                card = card,
                onRead = { ProcessLabStore.markDiagnosisRead(card.title) }
            )
        }
        item {
            ReportCard(onReady = ProcessLabStore::markReportReady)
        }
        item {
            EventTrailCard(
                events = state.events,
                onClear = ProcessLabStore::clearEvents
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第19章 进程调度实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从 pid、uid、Zygote、ActivityThread、主线程、Binder 线程、多进程、OOM Adj 到 ANR，把“App 运行在哪里”变成一组可以亲手触发和观察的证据。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScoreCard(score: ProcessLabScore) {
    val completed = score.completedCount()
    val total = score.totalCount()
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "进程观察分数",
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
            text = "完成 $completed / $total 个观察点：进程身份、启动链、状态恢复、remote 进程、isolated 进程、单例隔离、ANR、trace 阅读、OOM Adj 对比、线程模型、诊断卡和报告。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ExperimentCard(experiment: ProcessExperiment) {
    LabCard {
        SectionTitle("预期 vs 实际")
        KeyValue("操作", experiment.operation)
        KeyValue("预期", experiment.expected)
        KeyValue("实际", experiment.actual)
        KeyValue("结论", experiment.conclusion)
    }
}

@Composable
private fun IdentityCard(identity: ProcessIdentity, onRefresh: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("进程身份证")
            Button(onClick = onRefresh) {
                Text("刷新")
            }
        }
        KeyValue("packageName", identity.packageName)
        KeyValue("processName", identity.processName)
        KeyValue("pid / ppid", "${identity.pid} / ${identity.ppid}")
        KeyValue("uid", identity.uid)
        KeyValue("uid line", identity.uidLine)
        KeyValue("oom_score_adj", identity.oomScoreAdj)
        KeyValue("thread", identity.threadName)
        KeyValue("thread count", identity.threadCount)
        KeyValue("singleton", identity.singleton)
    }
}

@Composable
private fun SandboxCard(packageName: String, uidLine: String, dataDir: String) {
    LabCard {
        SectionTitle("应用沙箱观察卡")
        KeyValue("package", packageName)
        KeyValue("dataDir", dataDir)
        KeyValue("uid line", uidLine)
        MiniPanel("沙箱要点") {
            Text(
                text = "Android 用 Linux uid 把不同 App 的数据目录、文件权限和进程权限隔开；SELinux 再用安全上下文限制进程能访问哪些系统能力。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        MiniPanel("观察方法") {
            Text(
                text = "普通 App 不能直接读取其他 App 的 data 目录。跨应用共享要走 Binder、ContentProvider、文件授权、系统权限或用户显式授权。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StartupCard(startup: StartupTrace) {
    LabCard {
        SectionTitle("Zygote 与启动轨迹")
        KeyValue("Provider.onCreate", startup.providerCreated)
        KeyValue("Application.onCreate", startup.applicationCreated)
        KeyValue("Activity.onCreate", startup.activityCreated)
        MiniPanel("冷启动链路") {
            Text(startup.zygoteStory, style = MaterialTheme.typography.bodyMedium)
        }
        MiniPanel("ADB 观察命令") {
            Text(startup.commandHint, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecoveryCard(
    recovery: ProcessRecoveryState,
    onIncrease: () -> Unit,
    onSave: () -> Unit,
    onRestore: () -> Unit,
    onClear: () -> Unit
) {
    LabCard {
        SectionTitle("进程死亡与状态恢复实验")
        KeyValue("内存草稿", recovery.memoryDraft)
        KeyValue("持久化草稿", recovery.persistentDraft)
        KeyValue("current pid", recovery.currentPid)
        KeyValue("saved pid", recovery.savedPid)
        KeyValue("last saved", recovery.lastSavedAt)
        MiniPanel("观察结论") {
            Text(recovery.verdict, style = MaterialTheme.typography.bodyMedium)
        }
        MiniPanel("推荐玩法") {
            Text(
                text = "先点“内存 +1”，再点“保存草稿”，切后台执行 adb shell am kill 包名，回到 App 后刷新并点“从持久化恢复”。如果 pid 变了但草稿能回来，就说明恢复链路成立。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "内存 +1" to onIncrease,
                "保存草稿" to onSave,
                "从持久化恢复" to onRestore,
                "清空实验" to onClear
            )
        )
    }
}

@Composable
private fun ThreadModelCard(threadProbe: ThreadProbe) {
    LabCard {
        SectionTitle("线程分工观察卡")
        KeyValue("main", threadProbe.mainThread)
        KeyValue("Binder", threadProbe.binderThread)
        KeyValue("RenderThread", threadProbe.renderThread)
        KeyValue("worker", threadProbe.workerThread)
        MiniPanel("trace 阅读顺序") {
            Text(threadProbe.dumpReadingOrder, style = MaterialTheme.typography.bodyMedium)
        }
        MiniPanel("为什么要分主线程和其他线程") {
            Text(
                text = "主线程负责输入、生命周期和 UI 帧调度，它像 App 对用户的响应入口；I/O、计算、网络和长事务放到其他线程，是为了让点击、滑动和绘制始终有机会及时执行。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RemoteProcessCard(
    remote: RemoteProcessState,
    onBind: () -> Unit,
    onPing: () -> Unit,
    onMutate: () -> Unit,
    onBlock: () -> Unit,
    onCrash: () -> Unit
) {
    LabCard {
        SectionTitle("多进程与 Binder 实验区")
        KeyValue("bound", remote.bound.toString())
        KeyValue("remote process", remote.processName)
        KeyValue("remote pid / uid", "${remote.pid} / ${remote.uid}")
        KeyValue("remote thread", remote.threadName)
        KeyValue("remote singleton", remote.singleton)
        KeyValue("remote oom", remote.oomScoreAdj)
        KeyValue("last reply", remote.lastReply)
        KeyValue("binder death", remote.binderDeath)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "绑定 remote" to onBind,
                "Ping remote" to onPing,
                "修改 remote 单例" to onMutate,
                "阻塞 remote" to onBlock,
                "杀掉 remote" to onCrash
            )
        )
    }
}

@Composable
private fun IsolatedProcessCard(
    isolated: IsolatedProcessState,
    onBind: () -> Unit,
    onPing: () -> Unit,
    onCrash: () -> Unit
) {
    LabCard {
        SectionTitle("isolatedProcess 隔离实验区")
        KeyValue("bound", isolated.bound.toString())
        KeyValue("process", isolated.processName)
        KeyValue("pid / uid", "${isolated.pid} / ${isolated.uid}")
        KeyValue("thread", isolated.threadName)
        KeyValue("oom", isolated.oomScoreAdj)
        KeyValue("last reply", isolated.lastReply)
        KeyValue("binder death", isolated.binderDeath)
        MiniPanel("和普通 :remote 的区别") {
            Text(
                text = ":remote 通常仍属于同一个应用 uid；isolatedProcess 会运行在更受限的隔离进程里，适合承载插件渲染、解析不可信数据等需要缩小风险边界的工作。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "绑定 isolated" to onBind,
                "Ping isolated" to onPing,
                "杀掉 isolated" to onCrash
            )
        )
    }
}

@Composable
private fun AnrLabCard(
    anr: AnrLabState,
    traceGuide: AnrTraceGuide,
    onMainBlock: () -> Unit,
    onBroadcast: () -> Unit,
    onService: () -> Unit,
    onRemoteBlock: () -> Unit
) {
    LabCard(background = LabWarning) {
        SectionTitle("ANR 场景模拟区")
        Text(
            text = anr.warning,
            style = MaterialTheme.typography.bodySmall,
            color = LabSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValue("当前场景", anr.selectedScenario)
        KeyValue("最近动作", anr.lastAction)
        KeyValue("预期 trace", anr.expectedTrace)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "主线程阻塞 7s" to onMainBlock,
                "Broadcast 阻塞 12s" to onBroadcast,
                "Service 阻塞 22s" to onService,
                "remote 阻塞 8s" to onRemoteBlock
            ),
            outlined = false
        )
        MiniPanel(traceGuide.title) {
            KeyValue("main", traceGuide.mainThreadClue)
            KeyValue("Binder", traceGuide.binderClue)
            KeyValue("lock", traceGuide.lockClue)
            KeyValue("system", traceGuide.systemClue)
            KeyValue("结论", traceGuide.conclusion)
        }
    }
}

@Composable
private fun OomAndRecoveryCard(
    identity: ProcessIdentity,
    snapshots: List<OomSnapshot>,
    onCaptureForeground: () -> Unit,
    onCaptureAfterBackground: () -> Unit,
    onCaptureAfterRemote: () -> Unit
) {
    LabCard {
        SectionTitle("OOM Adj 与后台回收观察")
        KeyValue("当前 pid", identity.pid)
        KeyValue("当前 oom_score_adj", identity.oomScoreAdj)
        KeyValue("线程数", identity.threadCount)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "记录前台样本" to onCaptureForeground,
                "记录后台返回" to onCaptureAfterBackground,
                "记录 remote 后" to onCaptureAfterRemote
            )
        )
        if (snapshots.isNotEmpty()) {
            MiniPanel("OOM Adj 样本表") {
                snapshots.forEach { snapshot ->
                    KeyValue(
                        label = snapshot.label,
                        value = "${snapshot.processName}, pid=${snapshot.pid}, oom=${snapshot.oomScoreAdj}, ${snapshot.timestamp}"
                    )
                }
            }
        }
        MiniPanel("手动路线") {
            Text(
                text = "切到后台后执行 adb shell am kill ${identity.packageName}，再回到 App。若 pid 改变、Application 重新创建，就说明内存状态已经不能被当成可靠数据源。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        MiniPanel("重点判断") {
            Text(
                text = "前台 Activity、后台缓存进程、前台服务和 remote 进程的重要性不同，LMKD 会优先回收更不重要的进程。观察 oom_score_adj 时，要同时记录前后台状态。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DiagnosticCard(card: ProcessDiagnosticCard, onRead: () -> Unit) {
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
private fun ReportCard(onReady: () -> Unit) {
    LabCard {
        SectionTitle("进程问题诊断报告")
        Text(
            text = "报告至少写清楚：操作、现象、processName、pid、uid、oom_score_adj、Application 是否重建、remote pid、Binder death、线程栈、锁等待和你的结论。",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onReady) {
            Text("标记报告证据已收集")
        }
    }
}

@Composable
private fun EventTrailCard(events: List<ProcessEvent>, onClear: () -> Unit) {
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
            Text("暂无事件。先刷新、绑定 remote 或触发一个 ANR 场景。", style = MaterialTheme.typography.bodyMedium)
        } else {
            events.take(12).forEach { event ->
                MiniPanel("${event.source}.${event.phase}") {
                    KeyValue("signal", event.signal)
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
    outlined: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { (label, action) ->
                    if (outlined) {
                        OutlinedButton(
                            onClick = action,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    } else {
                        Button(
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
private fun KeyValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(116.dp),
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

private fun ProcessLabScore.completedCount(): Int {
    return listOf(
        identityObserved,
        startupObserved,
        remoteObserved,
        isolatedObserved,
        recoveryObserved,
        singletonObserved,
        anrObserved,
        oomObserved,
        oomCompared,
        threadObserved,
        traceObserved,
        diagnosisObserved,
        reportReady
    ).count { it }
}

private fun ProcessLabScore.totalCount(): Int = 13
