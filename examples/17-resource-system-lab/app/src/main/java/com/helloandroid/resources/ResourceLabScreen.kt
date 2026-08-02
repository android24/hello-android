package com.helloandroid.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ResourceSystemLabApp() {
    val context = LocalContext.current
    val state by ResourceLabStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        ResourceLabStore.refresh(context, reason = "进入资源仓库实验室")
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF315F72),
            secondary = Color(0xFF8A6A25),
            tertiary = Color(0xFF4D6B58),
            background = Color(0xFFF7F8F6),
            surface = Color.White
        )
    ) {
        ResourceLabScreen(state = state)
    }
}

@Composable
fun ResourceLabScreen(state: ResourceLabState) {
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeaderCard() }
            item { ScoreCard(score = state.score) }
            item { ExperimentCard(experiment = state.experiment) }
            item { ResourceIdentityCard(identity = state.identity) }
            item { LocaleProbeCard(localeProbe = state.localeProbe) }
            item { DensityProbeCard(densityProbe = state.densityProbe) }
            item { DynamicLookupCard(dynamicLookup = state.dynamicLookup) }
            item { ShrinkProbeCard(shrinkProbe = state.shrinkProbe) }
            item { ConfigurationCard(configuration = state.configuration) }
            item { ThemeCard(cards = state.themeCards) }
            item { DynamicReplacementCard(replacement = state.replacement) }
            item {
                SkinningAppendixCard(
                    cards = state.skinningStrategies,
                    workbench = state.skinningWorkbench
                )
            }
            item { DependencySourceCard(cards = state.sourceCards) }
            item { DependencyConflictProbeCard(probe = state.conflictProbe) }
            item { FileResourceCard(cards = state.fileCards) }
            item { DiagnosticCard(cards = state.diagnosticCards) }
            item { EventTrailCard(events = state.eventTrail) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    val context = LocalContext.current
    LabCard(background = Color(0xFF263A45), contentColor = Color.White) {
        Text(
            text = "第17章 资源仓库实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "观察 R、resources.arsc、Configuration、Theme、assets/raw 和依赖资源如何一起决定最终 UI。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE7EEF4)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { ResourceLabStore.refresh(context, reason = "手动刷新资源证据") }) {
            Text(text = "刷新资源证据")
        }
    }
}

@Composable
private fun ScoreCard(score: ResourceLabScore) {
    val total = listOf(
        score.idObserved,
        score.configurationObserved,
        score.themeObserved,
        score.replacementObserved,
        score.localeObserved,
        score.densityObserved,
        score.dynamicObserved,
        score.shrinkObserved,
        score.dependencyObserved,
        score.conflictObserved,
        score.fileObserved,
        score.diagnosisObserved,
        score.appendixObserved,
        score.reportObserved
    ).count { it } * 100 / 14

    LabCard(background = Color(0xFFFFF4D8)) {
        SectionTitle(title = "资源观察分数")
        Text(
            text = "$total / 100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A6A25)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(text = "拆解资源 ID")
        BulletText(text = "观察 Configuration")
        BulletText(text = "对比多语言资源")
        BulletText(text = "观察图片密度")
        BulletText(text = "读取 Theme attr")
        BulletText(text = "切换动态资源槽位")
        BulletText(text = "对比 R 与 getIdentifier")
        BulletText(text = "观察 shrink 风险")
        BulletText(text = "观察依赖资源来源")
        BulletText(text = "分析依赖冲突")
        BulletText(text = "对比 assets / raw")
        BulletText(text = "阅读诊断卡")
        BulletText(text = "阅读动态换肤附录")
        BulletText(text = "完成诊断报告")
    }
}

@Composable
private fun ExperimentCard(experiment: ResourceExperiment) {
    LabCard(background = Color(0xFFF3EDE6)) {
        SectionTitle(title = "预期 vs 实际")
        InfoRow(label = "本次操作", value = experiment.operation)
        Spacer(modifier = Modifier.height(6.dp))
        LabelText(label = "预期", text = experiment.expected, color = Color(0xFF8A6A25))
        LabelText(label = "实际", text = experiment.actual, color = Color(0xFF315F72))
        LabelText(label = "结论", text = experiment.conclusion, color = Color(0xFF4D6B58))
    }
}

@Composable
private fun ResourceIdentityCard(identity: ResourceIdentity) {
    LabCard(background = Color(0xFFEAF2F4)) {
        SectionTitle(title = "资源身份证与 AAPT2 匹配卡")
        InfoRow(label = "resourceId", value = identity.resourceIdHex)
        InfoRow(label = "package", value = identity.packagePart)
        InfoRow(label = "type", value = identity.typePart)
        InfoRow(label = "entry", value = identity.entryPart)
        InfoRow(label = "name", value = identity.resourceName)
        InfoRow(label = "typeName", value = identity.typeName)
        InfoRow(label = "entryName", value = identity.entryName)
        InfoRow(label = "value", value = identity.value)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = identity.aaptLinkStory, style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
    }
}

@Composable
private fun LocaleProbeCard(localeProbe: LocaleProbe) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFEFF4EA)) {
        SectionTitle(title = "字符串多语言实验区")
        Text(
            text = "同一个 string 资源 ID 会跟着 Configuration 选择 values、values-zh 或 values-en。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markLocaleExperiment(context) }) {
            Text(text = "记录多语言实验")
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "resource", value = localeProbe.resourceName)
        InfoRow(label = "currentLocale", value = localeProbe.currentLocale)
        InfoRow(label = "current", value = localeProbe.currentValue)
        InfoRow(label = "default", value = localeProbe.defaultValue)
        InfoRow(label = "zh", value = localeProbe.zhValue)
        InfoRow(label = "en", value = localeProbe.enValue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = localeProbe.conclusion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4D6B58))
    }
}

@Composable
private fun DensityProbeCard(densityProbe: DensityProbe) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "图片密度实验区")
        Text(
            text = "这里用一个 drawable 探针观察 densityDpi、资源类型和 intrinsic size。真实 bitmap 还会比较 mdpi/hdpi/xhdpi 等候选目录。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markDensityExperiment(context) }) {
            Text(text = "记录密度实验")
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "drawable", value = densityProbe.drawableName)
        InfoRow(label = "id", value = densityProbe.drawableId)
        InfoRow(label = "densityDpi", value = densityProbe.densityDpi)
        InfoRow(label = "bucket", value = densityProbe.densityBucket)
        InfoRow(label = "intrinsic", value = densityProbe.intrinsicSize)
        InfoRow(label = "type", value = densityProbe.resourceType)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = densityProbe.conclusion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
    }
}

@Composable
private fun DynamicLookupCard(dynamicLookup: DynamicLookup) {
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "R 直接引用 vs getIdentifier")
        InfoRow(label = "name", value = dynamicLookup.requestedName)
        InfoRow(label = "directId", value = dynamicLookup.directId)
        InfoRow(label = "dynamicId", value = dynamicLookup.dynamicId)
        InfoRow(label = "directValue", value = dynamicLookup.directValue)
        InfoRow(label = "dynamicValue", value = dynamicLookup.dynamicValue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = dynamicLookup.conclusion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
    }
}

@Composable
private fun ShrinkProbeCard(shrinkProbe: ShrinkProbe) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFFFFBEB)) {
        SectionTitle(title = "混淆与 shrink 观察卡")
        Text(
            text = "代码混淆主要改变类名和字段名；资源 shrink 和资源名混淆会影响资源是否还在包里，以及字符串式查找还能不能命中。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markShrinkExperiment(context) }) {
            Text(text = "记录 shrink 实验")
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "directName", value = shrinkProbe.directName)
        InfoRow(label = "directId", value = shrinkProbe.directId)
        InfoRow(label = "dynamicName", value = shrinkProbe.dynamicName)
        InfoRow(label = "dynamicId", value = shrinkProbe.dynamicId)
        InfoRow(label = "missingName", value = shrinkProbe.missingName)
        InfoRow(label = "missingId", value = shrinkProbe.missingId)
        LabelText(label = "风险", text = shrinkProbe.shrinkRisk, color = Color(0xFF8A6A25))
        LabelText(label = "建议", text = shrinkProbe.keepAdvice, color = Color(0xFF4D6B58))
    }
}

@Composable
private fun ConfigurationCard(configuration: ConfigurationSnapshot) {
    LabCard(background = Color(0xFFEFF4EA)) {
        SectionTitle(title = "Configuration 面板")
        InfoRow(label = "locale", value = configuration.locale)
        InfoRow(label = "orientation", value = configuration.orientation)
        InfoRow(label = "densityDpi", value = configuration.densityDpi)
        InfoRow(label = "fontScale", value = configuration.fontScale)
        InfoRow(label = "uiMode", value = configuration.uiMode)
        InfoRow(label = "widthDp", value = configuration.widthDp)
        InfoRow(label = "heightDp", value = configuration.heightDp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "同一个资源 ID 会根据 Configuration 选择 default、values-zh、values-night 或不同密度资源。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4D6B58)
        )
    }
}

@Composable
private fun ThemeCard(cards: List<ThemeAttrCard>) {
    LabCard(background = Color(0xFFFFFBEB)) {
        SectionTitle(title = "Theme attr 实验区")
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = "${card.name} · ${card.contextName}", fontWeight = FontWeight.Bold)
                InfoRow(label = "value", value = card.value)
                Text(text = card.meaning, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DynamicReplacementCard(replacement: DynamicReplacementState) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFEAF2F4)) {
        SectionTitle(title = "动态资源替换实验区")
        Text(
            text = "这里不修改 R 文件，而是切换业务槽位映射到的资源 ID。真实项目可以把这层做成 Theme 或 ResourceProvider。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.toggleReplacement(context) }) {
            Text(text = "切换资源槽位")
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "mode", value = replacement.mode)
        InfoRow(label = "titleRes", value = replacement.titleResourceName)
        InfoRow(label = "titleId", value = replacement.titleId)
        InfoRow(label = "titleValue", value = replacement.titleValue)
        InfoRow(label = "panelColor", value = replacement.panelColorName)
        InfoRow(label = "panelId", value = replacement.panelColorId)
        InfoRow(label = "panelValue", value = replacement.panelColorValue)
        InfoRow(label = "signalColor", value = replacement.signalColorName)
        InfoRow(label = "signalValue", value = replacement.signalColorValue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = replacement.explanation, style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
    }
}

@Composable
private fun SkinningAppendixCard(
    cards: List<SkinningStrategyCard>,
    workbench: SkinningWorkbenchState
) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFF7F0E8)) {
        SectionTitle(title = "动态换肤附录实验区")
        Text(
            text = "把换肤需求先放到资源读取链路上定位：到底是换 Theme、换业务槽位、换 Configuration、换外部资源路径，还是系统 overlay。每张实验卡都能点，点完看预期、实际和事件轨迹。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markSkinningAppendixRead() }) {
            Text(text = "记录换肤方案判断")
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "当前方案", value = workbench.activeStrategy)
        Text(text = workbench.activeSummary, style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
        Spacer(modifier = Modifier.height(12.dp))
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.name, fontWeight = FontWeight.Bold)
                InfoRow(label = "替换层", value = card.replaceTarget)
                LabelText(label = "适合", text = card.bestFor, color = Color(0xFF4D6B58))
                LabelText(label = "第一风险", text = card.firstRisk, color = Color(0xFF8A6A25))
                LabelText(label = "证据入口", text = card.evidence, color = Color(0xFF315F72))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        SectionTitle(title = "五种方案实操证据")
        Spacer(modifier = Modifier.height(8.dp))
        workbench.evidenceCards.forEachIndexed { index, evidence ->
            NumberedBlock(index = index + 1) {
                Text(text = evidence.title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                if (evidence.isActive) {
                    Button(onClick = { ResourceLabStore.selectSkinningStrategy(context, evidence.key) }) {
                        Text(text = "当前演示中")
                    }
                } else {
                    OutlinedButton(onClick = { ResourceLabStore.selectSkinningStrategy(context, evidence.key) }) {
                        Text(text = "演示此方案")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LabelText(label = "实现方式", text = evidence.implementation, color = Color(0xFF4D6B58))
                LabelText(label = "操作玩法", text = evidence.operation, color = Color(0xFF315F72))
                InfoRow(label = "替换目标", value = evidence.target)
                InfoRow(label = "替换前", value = evidence.before)
                InfoRow(label = "替换后", value = evidence.after)
                LabelText(label = "证据入口", text = evidence.evidence, color = Color(0xFF315F72))
                LabelText(label = "风险", text = evidence.risk, color = Color(0xFF8A6A25))
            }
            if (index != workbench.evidenceCards.lastIndex) Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        SectionTitle(title = "经典框架结构速览")
        Spacer(modifier = Modifier.height(8.dp))
        workbench.frameworkCards.forEachIndexed { index, framework ->
            NumberedBlock(index = index + 1) {
                Text(text = framework.name, fontWeight = FontWeight.Bold)
                InfoRow(label = "repo", value = framework.repository)
                LabelText(label = "核心结构", text = framework.coreStructure, color = Color(0xFF4D6B58))
                LabelText(label = "学习重点", text = framework.learningFocus, color = Color(0xFF315F72))
            }
            if (index != workbench.frameworkCards.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DependencySourceCard(cards: List<ResourceSourceCard>) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "依赖覆盖观察卡")
        Text(
            text = "这些资源来自 app、debug source set、feature、core-design 和 legacy-widget。排查冲突时要看最终 merged resources，而不只是当前源码。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markDependencyExperiment(context) }) {
            Text(text = "记录依赖资源实验")
        }
        Spacer(modifier = Modifier.height(10.dp))
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.owner, fontWeight = FontWeight.Bold)
                InfoRow(label = "resource", value = card.resourceName)
                InfoRow(label = "id", value = card.idHex)
                InfoRow(label = "value", value = card.value)
                Text(text = card.sourceMeaning, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DependencyConflictProbeCard(probe: DependencyConflictProbe) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFF3EDE6)) {
        SectionTitle(title = "依赖冲突实验区")
        Text(
            text = "依赖冲突不只是同名文件冲突，还可能来自覆盖优先级、传递依赖、旧 AAR 和同库多版本漂移。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { ResourceLabStore.markConflictExperiment(context) }) {
            Text(text = "记录依赖冲突实验")
        }
        Spacer(modifier = Modifier.height(10.dp))
        probe.cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.scenario, fontWeight = FontWeight.Bold)
                LabelText(label = "触发条件", text = card.trigger, color = Color(0xFF8A6A25))
                LabelText(label = "第一证据", text = card.firstEvidence, color = Color(0xFF315F72))
                LabelText(label = "风险", text = card.risk, color = Color(0xFF8A6A25))
                LabelText(label = "修复方向", text = card.fixDirection, color = Color(0xFF4D6B58))
            }
            if (index != probe.cards.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = probe.conclusion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
    }
}

@Composable
private fun FileResourceCard(cards: List<ResourceFileCard>) {
    LabCard(background = Color(0xFFEAF2F4)) {
        SectionTitle(title = "assets / raw 实验区")
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.source, fontWeight = FontWeight.Bold)
                InfoRow(label = "read", value = card.readMode)
                InfoRow(label = "content", value = card.content)
                Text(text = card.meaning, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DiagnosticCard(cards: List<ResourceDiagnosticCard>) {
    LabCard(background = Color(0xFFF3EDE6)) {
        SectionTitle(title = "资源问题诊断卡")
        cards.forEach { card ->
            OutlinedButton(onClick = { ResourceLabStore.markDiagnosisRead(card.title) }) {
                Text(text = card.title)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LabelText(label = "现象", text = card.symptom, color = Color(0xFF8A6A25))
            LabelText(label = "第一证据", text = card.firstEvidence, color = Color(0xFF315F72))
            LabelText(label = "修复方向", text = card.fixDirection, color = Color(0xFF4D6B58))
            Spacer(modifier = Modifier.height(10.dp))
        }
        Button(onClick = { ResourceLabStore.markReportReady() }) {
            Text(text = "标记报告证据已收集")
        }
    }
}

@Composable
private fun EventTrailCard(events: List<ResourceEventLog>) {
    LabCard(background = Color(0xFF263A45), contentColor = Color.White) {
        SectionTitle(title = "资源事件轨迹")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { ResourceLabStore.clearEvents() }) {
            Text(text = "清空轨迹")
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (events.isEmpty()) {
            Text(text = "等待第一条资源系统证据。", color = Color(0xFFE7EEF4))
        } else {
            events.forEach { event ->
                Text(
                    text = "${event.timestamp}  ${event.source}.${event.phase}  ${event.signal}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF6D58B)
                )
                Text(
                    text = event.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE7EEF4)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NumberedBlock(index: Int, content: @Composable Column.() -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFD6E7EF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2B27)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f), content = content)
    }
}

@Composable
private fun LabCard(
    background: Color,
    contentColor: Color = Color(0xFF1D2B27),
    content: @Composable Column.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = background, contentColor = contentColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
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
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F),
            modifier = Modifier.weight(0.36f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.64f)
        )
    }
}

@Composable
private fun LabelText(label: String, text: String, color: Color) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
    Text(text = text, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun BulletText(text: String) {
    Text(text = "- $text", style = MaterialTheme.typography.bodySmall)
}

@Preview(showBackground = true)
@Composable
private fun ResourceLabPreview() {
    ResourceLabScreen(
        state = ResourceLabState(
            identity = ResourceIdentity(resourceIdHex = "0x7F100001", resourceName = "com.demo:string/title")
        )
    )
}
