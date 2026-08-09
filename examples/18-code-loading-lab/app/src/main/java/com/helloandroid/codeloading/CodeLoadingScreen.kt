package com.helloandroid.codeloading

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

private val LabBackground = Color(0xFFF6F8F7)
private val LabPrimary = Color(0xFF29524A)
private val LabSecondary = Color(0xFFB65F2A)
private val LabInk = Color(0xFF1E2422)

@Composable
fun CodeLoadingLabApp() {
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
            CodeLoadingLabScreen()
        }
    }
}

@Composable
private fun CodeLoadingLabScreen() {
    val context = LocalContext.current
    val state by CodeLoadingStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        CodeLoadingStore.refresh(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LabBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard()
        }
        item {
            ScoreCard(state.score)
        }
        item {
            ExperimentCard(state.experiment)
        }
        item {
            ClassLoaderIdentityCard(
                cards = state.classLoaderCards,
                dexPathProbe = state.dexPathProbe,
                onRefresh = { CodeLoadingStore.refresh(context, "手动刷新 ClassLoader 和 Dex 证据") }
            )
        }
        item {
            DexPathCard(state.dexPathProbe)
        }
        item {
            ReflectionRiskCard(
                probe = state.reflectionProbe,
                onRun = { CodeLoadingStore.runReflectionExperiment(context) }
            )
        }
        item {
            DynamicPluginCard(
                probe = state.dynamicPluginProbe,
                onLoad = { CodeLoadingStore.loadDynamicPlugin(context) }
            )
        }
        item {
            HotfixCard(
                probe = state.hotfixProbe,
                onRun = { CodeLoadingStore.runHotfixExperiment(context) }
            )
        }
        item {
            RuntimeCard(state.runtimeProbe)
        }
        item {
            NativeProbeCard(
                probe = state.nativeProbe,
                onRun = { CodeLoadingStore.runNativeExperiment(context) }
            )
        }
        item {
            PluginBoundaryCard(
                probe = state.pluginProbe,
                onInspect = { CodeLoadingStore.inspectPluginBoundary(context) }
            )
        }
        item {
            SectionTitle("代码加载诊断卡")
        }
        items(state.diagnosticCards) { card ->
            DiagnosticCard(
                card = card,
                onRead = { CodeLoadingStore.markDiagnosisRead(card.title) }
            )
        }
        item {
            ReportCard(onReady = CodeLoadingStore::markReportReady)
        }
        item {
            EventTrailCard(
                events = state.eventTrail,
                onClear = CodeLoadingStore::clearEvents
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第18章 代码加载实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从 classes.dex、ClassLoader、ART、R8、JNI、so 到动态加载边界，把一次“代码为什么能运行”拆成可观察证据。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScoreCard(score: CodeLoadingScore) {
    val completed = score.completedCount()
    val total = score.totalCount()
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "代码加载分数",
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
            trackColor = LabBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "完成 $completed / $total 个观察点：ClassLoader、DexPath、反射、动态插件、热修复、ART、native、插件边界、诊断卡和报告。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ExperimentCard(experiment: CodeLoadingExperiment) {
    LabCard {
        SectionTitle("预期 vs 实际")
        KeyValue("操作", experiment.operation)
        KeyValue("预期", experiment.expected)
        KeyValue("实际", experiment.actual)
        KeyValue("结论", experiment.conclusion)
    }
}

@Composable
private fun ClassLoaderIdentityCard(
    cards: List<ClassLoaderCard>,
    dexPathProbe: DexPathProbe,
    onRefresh: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("ClassLoader 身份卡")
            Button(onClick = onRefresh) {
                Text("刷新")
            }
        }
        cards.forEach { card ->
            MiniPanel(title = card.label) {
                KeyValue("class", card.className)
                KeyValue("loader", card.loaderName)
                KeyValue("parent", card.parentName)
                KeyValue("意义", card.meaning)
            }
        }
        MiniPanel(title = "查找链路") {
            Text(
                text = dexPathProbe.evidence,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            KeyValue("状态", dexPathProbe.status)
        }
    }
}

@Composable
private fun DexPathCard(probe: DexPathProbe) {
    LabCard {
        SectionTitle("Dex 路径观察卡")
        KeyValue("package", probe.packageName)
        KeyValue("sourceDir", probe.sourceDir)
        KeyValue("splitSourceDirs", probe.splitSourceDirs)
        KeyValue("nativeLibraryDir", probe.nativeLibraryDir)
        MiniPanel(title = "ClassLoader 链") {
            probe.classLoaderChain.forEachIndexed { index, item ->
                Text("$index. $item", style = MaterialTheme.typography.bodySmall)
            }
        }
        MiniPanel(title = "dexElements") {
            if (probe.dexElements.isEmpty()) {
                Text("尚未读取。", style = MaterialTheme.typography.bodySmall)
            } else {
                probe.dexElements.take(8).forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ReflectionRiskCard(
    probe: ReflectionProbe,
    onRun: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("反射与 R8 风险卡")
            Button(onClick = onRun) {
                Text("运行")
            }
        }
        KeyValue("direct call", probe.directCall)
        KeyValue("Class.forName", probe.reflectionClassName)
        KeyValue("reflection result", probe.reflectionResult)
        KeyValue("missing class", probe.missingClassName)
        KeyValue("missing result", probe.missingResult)
        KeyValue("route table", probe.routeTableResult)
        KeyValue("minify", probe.minifyEnabled)
        KeyValue("keep", probe.keepAdvice)
    }
}

@Composable
private fun DynamicPluginCard(
    probe: DynamicPluginProbe,
    onLoad: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("真实动态加载实验")
            Button(onClick = onLoad) {
                Text("加载插件")
            }
        }
        KeyValue("asset apk", probe.assetApk)
        KeyValue("installed", probe.installedPath)
        KeyValue("entry", probe.entryClass)
        KeyValue("loader", probe.pluginClassLoader)
        KeyValue("parent", probe.parentClassLoader)
        KeyValue("contract", probe.contractResult)
        KeyValue("execute", probe.executeResult)
        MiniPanel(title = "plugin dexElements") {
            if (probe.pluginDexElements.isEmpty()) {
                Text("尚未读取插件 dexElements。", style = MaterialTheme.typography.bodySmall)
            } else {
                probe.pluginDexElements.take(6).forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        MiniPanel(title = "插件化边界") {
            if (probe.boundarySummary.isEmpty()) {
                Text("加载插件后显示 contract、code、resource、native、lifecycle、safety 边界。", style = MaterialTheme.typography.bodySmall)
            } else {
                probe.boundarySummary.forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        KeyValue("结论", probe.conclusion)
    }
}

@Composable
private fun HotfixCard(
    probe: HotfixProbe,
    onRun: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("热修复核心逻辑实验")
            Button(onClick = onRun) {
                Text("应用补丁")
            }
        }
        KeyValue("target", probe.targetClass)
        KeyValue("input", probe.input)
        KeyValue("buggy", probe.buggyResult)
        KeyValue("patchClass", probe.patchClass)
        KeyValue("patchId", probe.patchId)
        KeyValue("fixed", probe.fixedResult)
        MiniPanel(title = "dexElements 顺序故事") {
            Text(probe.dexOrderStory, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("真实方案：patch.dex 放前面 -> 查找同名类时先命中补丁。", style = MaterialTheme.typography.bodySmall)
            Text("本实验：插件补丁类替换业务策略 -> 先执行补丁结果，再回看原错误结果。", style = MaterialTheme.typography.bodySmall)
        }
        KeyValue("结论", probe.conclusion)
    }
}

@Composable
private fun RuntimeCard(probe: RuntimeProbe) {
    LabCard {
        SectionTitle("Dalvik / ART 运行时卡")
        KeyValue("vm", probe.vmName)
        KeyValue("vmVersion", probe.vmVersion)
        KeyValue("sdk", probe.sdkInt)
        KeyValue("supportedAbis", probe.supportedAbis)
        KeyValue("策略", probe.strategy)
        KeyValue("Profile", probe.profileHint)
    }
}

@Composable
private fun NativeProbeCard(
    probe: NativeProbe,
    onRun: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("native so 观察卡")
            Button(onClick = onRun) {
                Text("加载")
            }
        }
        KeyValue("library", probe.requestedLibrary)
        KeyValue("mapped", probe.mappedLibraryName)
        KeyValue("nativeLibraryDir", probe.nativeLibraryDir)
        KeyValue("supportedAbis", probe.supportedAbis)
        KeyValue("loadResult", probe.loadResult)
        KeyValue("诊断层", probe.diagnosisLayer)
        MiniPanel(title = "三层排查") {
            Text("文件层：libxxx.so 是否存在，ABI 是否匹配。", style = MaterialTheme.typography.bodySmall)
            Text("依赖层：目标 so 依赖的其他 so 是否存在。", style = MaterialTheme.typography.bodySmall)
            Text("符号层：JNI_OnLoad、RegisterNatives 或静态方法签名是否匹配。", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PluginBoundaryCard(
    probe: PluginBoundaryProbe,
    onInspect: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("动态加载边界卡")
            Button(onClick = onInspect) {
                Text("读取")
            }
        }
        KeyValue("manifest", probe.manifestSummary)
        KeyValue("代码", probe.entryBoundary)
        KeyValue("资源", probe.resourceBoundary)
        KeyValue("so", probe.nativeBoundary)
        KeyValue("生命周期", probe.lifecycleBoundary)
        KeyValue("安全", probe.safetyBoundary)
        KeyValue("结论", probe.conclusion)
    }
}

@Composable
private fun DiagnosticCard(
    card: CodeLoadingDiagnosticCard,
    onRead: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onRead) {
                Text("标记")
            }
        }
        KeyValue("现象", card.symptom)
        KeyValue("第一证据", card.firstEvidence)
        KeyValue("修复方向", card.fixDirection)
    }
}

@Composable
private fun ReportCard(onReady: () -> Unit) {
    LabCard {
        SectionTitle("代码加载诊断报告")
        Text(
            text = "把当前 ClassLoader、parent、dexElements、R8、mapping、ABI、nativeLibraryDir、目标 so 和 JNI 注册方式写进 quality/code-loading-report-template.md。",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onReady) {
            Text("证据已收集")
        }
    }
}

@Composable
private fun EventTrailCard(
    events: List<CodeLoadingEvent>,
    onClear: () -> Unit
) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("代码加载事件轨迹")
            OutlinedButton(onClick = onClear) {
                Text("清空")
            }
        }
        if (events.isEmpty()) {
            Text("等待实验事件。", style = MaterialTheme.typography.bodySmall)
        } else {
            events.forEach { event ->
                MiniPanel(title = "${event.timestamp} ${event.source}.${event.phase}") {
                    KeyValue(event.signal, event.detail)
                }
            }
        }
    }
}

@Composable
private fun LabCard(content: @Composable Column.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun MiniPanel(
    title: String,
    content: @Composable Column.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LabBackground, shape = MaterialTheme.shapes.small)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = LabPrimary,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun KeyValue(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = key,
            modifier = Modifier.width(112.dp),
            style = MaterialTheme.typography.bodySmall,
            color = LabPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun CodeLoadingScore.completedCount(): Int {
    return listOf(
        classLoaderObserved,
        dexPathObserved,
        reflectionObserved,
        dynamicPluginObserved,
        hotfixObserved,
        runtimeObserved,
        nativeObserved,
        pluginObserved,
        diagnosisObserved,
        reportReady
    ).count { it }
}

private fun CodeLoadingScore.totalCount(): Int = 10
