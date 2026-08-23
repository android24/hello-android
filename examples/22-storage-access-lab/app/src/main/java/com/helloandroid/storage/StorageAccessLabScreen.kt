package com.helloandroid.storage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

private val LabBackground = Color(0xFFF7F6F0)
private val LabPrimary = Color(0xFF2E5E52)
private val LabSecondary = Color(0xFFB56B2B)
private val LabInk = Color(0xFF1F2528)
private val LabSoft = Color(0xFFE8E8DC)
private val LabMedia = Color(0xFFEFF5F7)
private val LabDocument = Color(0xFFFFF4E4)
private val LabIncident = Color(0xFFF5ECE8)

@Composable
fun StorageAccessLabApp() {
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
            StorageLabScreen()
        }
    }
}

@Composable
private fun StorageLabScreen() {
    val context = LocalContext.current
    val state by StorageLabStore.state.collectAsStateWithLifecycle()
    val createReportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            if (uri != null) {
                StorageLabStore.exportReportToUri(context, uri)
            } else {
                StorageLabStore.record("SAF", "CANCEL", "create document returned null")
            }
        }
    )
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> StorageLabStore.onPhotoPicked(context, uri) }
    )
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> StorageLabStore.onPersistableDocumentPicked(context, uri) }
    )

    LaunchedEffect(Unit) {
        StorageLabStore.refresh(context)
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
        item { RuntimeCard(state.runtime, onRefresh = { StorageLabStore.refresh(context) }) }
        item {
            DecisionCard(
                decision = state.decision,
                onDraft = { StorageLabStore.chooseDecision("draft") },
                onCache = { StorageLabStore.chooseDecision("cache") },
                onPoster = { StorageLabStore.chooseDecision("poster") },
                onAvatar = { StorageLabStore.chooseDecision("avatar") },
                onReport = { StorageLabStore.chooseDecision("report") },
                onShare = { StorageLabStore.chooseDecision("share") }
            )
        }
        item {
            PrivateStorageCard(
                panel = state.privatePanel,
                onWrite = { StorageLabStore.writePrivateDraft(context) },
                onRead = { StorageLabStore.readPrivateDraft(context) }
            )
        }
        item {
            CacheCard(
                panel = state.cachePanel,
                onWrite = { StorageLabStore.writeCache(context) },
                onClear = { StorageLabStore.clearCacheSafely(context) }
            )
        }
        item {
            MediaStoreCard(
                panel = state.mediaPanel,
                onSave = { StorageLabStore.savePosterToMediaStore(context) }
            )
        }
        item {
            PickerCard(
                panel = state.pickerPanel,
                onPick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
        item {
            DocumentCard(
                panel = state.documentPanel,
                onExport = { createReportLauncher.launch("hello-storage-report.txt") }
            )
        }
        item {
            PersistedUriCard(
                panel = state.persistedUriPanel,
                onOpen = { openDocumentLauncher.launch(arrayOf("*/*")) },
                onRefresh = { StorageLabStore.refreshPersistedUriPermissions(context) }
            )
        }
        item {
            ShareCard(
                panel = state.sharePanel,
                onCreate = { StorageLabStore.createPrivateReport(context) },
                onShare = { StorageLabStore.sharePrivateReport(context) }
            )
        }
        item {
            AntiPatternCard(
                panel = state.antiPatternPanel,
                onPrivateExport = { StorageLabStore.simulatePrivateExportTrap(context) },
                onPendingRollback = { StorageLabStore.simulatePendingMediaRollback(context) },
                onFileUri = { StorageLabStore.simulateFileUriTrap(context) },
                onCacheDraft = { StorageLabStore.simulateCacheDraftTrap(context) }
            )
        }
        item {
            IncidentScriptCard(
                activeScript = state.activeScript,
                scripts = state.scripts,
                onSelect = StorageLabStore::selectScript
            )
        }
        item { SystemCommandCard() }
        item {
            ReportCard(
                report = state.report,
                onReady = StorageLabStore::markReportReady
            )
        }
        item {
            EventTrailCard(
                events = state.events,
                onClear = StorageLabStore::clearEvents
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第22章 存储访问观察实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "把 filesDir、cacheDir、MediaStore、Photo Picker、SAF、FileProvider 和存储事故剧本放进同一个面板，训练“数据归属 -> 存储入口 -> 授权索引 -> 证据复盘”的判断路径。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScoreCard(score: StorageScore) {
    val completed = score.completedCount()
    val total = score.totalCount()
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("存储实验分数")
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
            text = "完成 $completed / $total 个观察点：运行现场、数据归属、私有草稿、缓存清理、MediaStore、媒体证据、Photo Picker、SAF 导出、持久 URI、FileProvider 分享、反例实验、事故剧本和诊断报告。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MissionBoardCard(score: StorageScore) {
    LabCard {
        SectionTitle("资料室任务板")
        Text(
            text = "按顺序推进：先判断数据属于谁，再触发读写，最后用 Uri、路径、权限和索引证据解释结果。",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        MissionStep("确认运行现场", score.runtimeObserved, "记录 package、targetSdk、SDK、filesDir 和 cacheDir。")
        MissionStep("完成数据归属判断", score.decisionMade, "判断 App 私有、缓存、用户媒体、用户文档或跨应用分享。")
        MissionStep("写入私有草稿", score.privateDraftWritten, "草稿属于核心数据，应该进入 filesDir。")
        MissionStep("清理可再生缓存", score.cacheCleaned, "清理 cacheDir，并验证草稿仍然存在。")
        MissionStep("保存共享媒体", score.mediaSaved, "用 MediaStore 写入课程海报。")
        MissionStep("阅读媒体证据", score.mediaEvidenceRead, "观察 Uri、MIME_TYPE、RELATIVE_PATH 和 IS_PENDING。")
        MissionStep("选择用户图片", score.photoPicked, "用 Photo Picker 获取用户选择的 Uri。")
        MissionStep("导出用户报告", score.safExported, "用 SAF 创建用户可见文档。")
        MissionStep("观察持久 URI", score.persistedUriObserved, "选择一个文档并查看 persistedUriPermissions。")
        MissionStep("分享私有文件", score.fileShared, "用 FileProvider 临时授权外部 App。")
        MissionStep("完成反例实验", score.antiPatternObserved, "观察错误存储设计会留下什么坏信号。")
        MissionStep("完成事故剧本", score.scriptCompleted, "从现象、线索和陷阱判断根因。")
        MissionStep("完成诊断报告", score.reportReady, "写出数据归属、存储入口、权限、索引、修复和回归。")
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
        KeyValue("targetSdk", runtime.targetSdk)
        KeyValue("sdk", runtime.sdk)
        KeyValue("process", runtime.processName)
        KeyValue("filesDir", runtime.filesDir)
        KeyValue("cacheDir", runtime.cacheDir)
    }
}

@Composable
private fun DecisionCard(
    decision: StorageDecision,
    onDraft: () -> Unit,
    onCache: () -> Unit,
    onPoster: () -> Unit,
    onAvatar: () -> Unit,
    onReport: () -> Unit,
    onShare: () -> Unit
) {
    LabCard {
        SectionTitle("数据归属决策卡")
        KeyValue("场景", decision.scenario)
        KeyValue("数据归属", decision.ownership)
        KeyValue("卸载后保留", decision.shouldSurviveUninstall)
        KeyValue("其他 App 可见", decision.visibleToOtherApps)
        KeyValue("推荐入口", decision.recommended)
        KeyValue("第一证据", decision.evidence)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "学习草稿" to onDraft,
                "缩略图缓存" to onCache,
                "保存海报" to onPoster,
                "选择头像" to onAvatar,
                "导出报告" to onReport,
                "分享报告" to onShare
            )
        )
    }
}

@Composable
private fun PrivateStorageCard(
    panel: PrivateStoragePanel,
    onWrite: () -> Unit,
    onRead: () -> Unit
) {
    LabCard {
        SectionTitle("App 私有草稿实验区")
        KeyValue("文件名", panel.fileName)
        KeyValue("状态", panel.status)
        KeyValue("路径", panel.path)
        KeyValue("大小", panel.size)
        KeyValue("预览", panel.contentPreview)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "写入草稿" to onWrite,
                "读取草稿" to onRead
            ),
            filled = true
        )
    }
}

@Composable
private fun CacheCard(
    panel: CachePanel,
    onWrite: () -> Unit,
    onClear: () -> Unit
) {
    LabCard(background = Color(0xFFF4F1E7)) {
        SectionTitle("缓存清理实验区")
        KeyValue("文件名", panel.fileName)
        KeyValue("状态", panel.status)
        KeyValue("路径", panel.path)
        KeyValue("清理前", panel.sizeBefore)
        KeyValue("清理后", panel.sizeAfter)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "写入缓存" to onWrite,
                "安全清缓存" to onClear
            )
        )
    }
}

@Composable
private fun MediaStoreCard(panel: MediaStorePanel, onSave: () -> Unit) {
    LabCard(background = LabMedia) {
        SectionTitle("MediaStore 海报实验区")
        KeyValue("状态", panel.status)
        KeyValue("Uri", panel.uri)
        KeyValue("DISPLAY_NAME", panel.displayName)
        KeyValue("MIME_TYPE", panel.mimeType)
        KeyValue("RELATIVE_PATH", panel.relativePath)
        KeyValue("IS_PENDING", panel.pending)
        KeyValue("证据", panel.evidence)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onSave) {
            Text("保存课程海报")
        }
    }
}

@Composable
private fun PickerCard(panel: PickerPanel, onPick: () -> Unit) {
    LabCard {
        SectionTitle("Photo Picker 实验区")
        KeyValue("状态", panel.status)
        KeyValue("Uri", panel.uri)
        KeyValue("授权", panel.access)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onPick) {
            Text("选择头像图片")
        }
    }
}

@Composable
private fun DocumentCard(panel: DocumentPanel, onExport: () -> Unit) {
    LabCard(background = LabDocument) {
        SectionTitle("SAF 导出报告实验区")
        KeyValue("状态", panel.status)
        KeyValue("Uri", panel.uri)
        KeyValue("MIME_TYPE", panel.mimeType)
        KeyValue("归属", panel.ownership)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onExport) {
            Text("导出学习报告")
        }
    }
}

@Composable
private fun ShareCard(
    panel: SharePanel,
    onCreate: () -> Unit,
    onShare: () -> Unit
) {
    LabCard {
        SectionTitle("FileProvider 分享实验区")
        KeyValue("文件名", panel.fileName)
        KeyValue("状态", panel.status)
        KeyValue("Uri", panel.uri)
        KeyValue("authority", panel.authority)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "创建私有报告" to onCreate,
                "分享报告" to onShare
            )
        )
    }
}

@Composable
private fun PersistedUriCard(
    panel: PersistedUriPanel,
    onOpen: () -> Unit,
    onRefresh: () -> Unit
) {
    LabCard(background = Color(0xFFEAF0FF)) {
        SectionTitle("持久 URI 授权观察区")
        KeyValue("状态", panel.status)
        KeyValue("Uri", panel.uri)
        KeyValue("读取结果", panel.readResult)
        KeyValue("持久授权", panel.persistedPermissions)
        KeyValue("诊断", panel.diagnosis)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "选择文档并持久授权" to onOpen,
                "刷新授权列表" to onRefresh
            )
        )
    }
}

@Composable
private fun AntiPatternCard(
    panel: AntiPatternPanel,
    onPrivateExport: () -> Unit,
    onPendingRollback: () -> Unit,
    onFileUri: () -> Unit,
    onCacheDraft: () -> Unit
) {
    LabCard(background = Color(0xFFFFECE8)) {
        SectionTitle("反例实验区")
        KeyValue("当前反例", panel.activeCase)
        KeyValue("坏信号", panel.badSignal)
        KeyValue("预期后果", panel.expectedFailure)
        KeyValue("学到什么", panel.lesson)
        Spacer(modifier = Modifier.height(10.dp))
        ActionGrid(
            actions = listOf(
                "私有导出反例" to onPrivateExport,
                "MediaStore 半成品" to onPendingRollback,
                "file:// 分享反例" to onFileUri,
                "缓存草稿反例" to onCacheDraft
            )
        )
    }
}

@Composable
private fun IncidentScriptCard(
    activeScript: StorageIncidentScript,
    scripts: List<StorageIncidentScript>,
    onSelect: (StorageIncidentScript) -> Unit
) {
    LabCard(background = LabIncident) {
        SectionTitle("存储事故剧本")
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
private fun SystemCommandCard() {
    LabCard(background = Color(0xFFEFF2E7)) {
        SectionTitle("系统证据命令卡")
        Text(
            text = "命令不会自动执行，请在终端配合 adb 观察。重点不是背命令，而是知道第一证据在哪里。",
            style = MaterialTheme.typography.bodySmall
        )
        CodeBlock(storageCommandHint)
    }
}

@Composable
private fun ReportCard(report: StorageDiagnosisReport, onReady: () -> Unit) {
    LabCard {
        SectionTitle("存储诊断报告")
        KeyValue("标题", report.title)
        KeyValue("数据归属", report.dataOwnership)
        KeyValue("存储入口", report.storageEntry)
        KeyValue("权限状态", report.permissionState)
        KeyValue("系统索引", report.systemIndex)
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
private fun EventTrailCard(events: List<StorageEvent>, onClear: () -> Unit) {
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
            Text("暂无事件。先刷新运行现场，或触发一个存储实验。")
        } else {
            events.take(18).forEach { event ->
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
            .background(Color(0xFF1B2B28), MaterialTheme.shapes.small)
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

private fun StorageScore.completedCount(): Int {
    return listOf(
        runtimeObserved,
        decisionMade,
        privateDraftWritten,
        cacheCleaned,
        mediaSaved,
        mediaEvidenceRead,
        photoPicked,
        safExported,
        persistedUriObserved,
        fileShared,
        antiPatternObserved,
        scriptCompleted,
        reportReady
    ).count { it }
}

private fun StorageScore.totalCount(): Int = 13
