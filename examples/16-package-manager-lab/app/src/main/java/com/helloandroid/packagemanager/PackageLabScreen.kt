package com.helloandroid.packagemanager

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
fun PackageManagerLabApp() {
    val context = LocalContext.current
    val state by PackageLabStore.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        PackageLabStore.refresh(context, reason = "进入包管理实验室")
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
        PackageLabScreen(state = state)
    }
}

@Composable
fun PackageLabScreen(state: PackageLabState) {
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
            item { ExperimentCard(experiment = state.currentExperiment) }
            item { IdentityCard(identity = state.identity, signatureCard = state.signatureCard) }
            item { ComponentListCard(components = state.components) }
            item { IntentLabCard(result = state.intentResult) }
            item { VisibilityCardList(cards = state.visibilityCards) }
            item { PermissionCardList(cards = state.permissionCards) }
            item { DiagnosticCard(cards = state.diagnosticCards) }
            item { EventTrailCard(events = state.eventTrail) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard(background = Color(0xFF263A45), contentColor = Color.White) {
        Text(
            text = "第16章 包管理登记处实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "读取 PackageInfo、组件清单、Intent 匹配、权限状态、签名摘要和包可见性证据。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE7EEF4)
        )
    }
}

@Composable
private fun ScoreCard(score: PackageLabScore) {
    val total = listOf(
        score.packageInfoObserved,
        score.componentsObserved,
        score.intentResolved,
        score.visibilityObserved,
        score.permissionObserved,
        score.signatureObserved,
        score.componentStateChanged,
        score.diagnosisObserved
    ).count { it } * 100 / 8

    LabCard(background = Color(0xFFFFF4D8)) {
        SectionTitle(title = "包管理观察分数")
        Text(
            text = "$total / 100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A6A25)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(text = "读取 PackageInfo")
        BulletText(text = "观察 Manifest 组件")
        BulletText(text = "执行 Intent 解析")
        BulletText(text = "观察包可见性")
        BulletText(text = "检查权限状态")
        BulletText(text = "读取签名摘要")
        BulletText(text = "切换组件 enabled")
        BulletText(text = "阅读诊断卡")
    }
}

@Composable
private fun ExperimentCard(experiment: PackageExperiment) {
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
private fun IdentityCard(identity: AppIdentity, signatureCard: SignatureCard) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFEAF2F4)) {
        SectionTitle(title = "应用身份证")
        InfoRow(label = "packageName", value = identity.packageName)
        InfoRow(label = "version", value = "${identity.versionName} (${identity.versionCode})")
        InfoRow(label = "targetSdk", value = identity.targetSdk)
        InfoRow(label = "firstInstall", value = identity.firstInstallTime)
        InfoRow(label = "lastUpdate", value = identity.lastUpdateTime)
        InfoRow(label = "sourceDir", value = identity.sourceDir)
        Spacer(modifier = Modifier.height(10.dp))
        SectionTitle(title = "签名摘要")
        InfoRow(label = "mode", value = signatureCard.mode)
        InfoRow(label = "signers", value = signatureCard.signerCount)
        InfoRow(label = "sha256", value = signatureCard.digest)
        Text(text = signatureCard.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { PackageLabStore.refresh(context, reason = "手动刷新包信息") }) {
            Text(text = "刷新包信息")
        }
    }
}

@Composable
private fun ComponentListCard(components: List<ComponentCard>) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "Manifest 组件清单")
        Text(
            text = "这里展示 PMS 解析后的组件登记结果。点击下方按钮可以切换 DeepLinkActivity enabled 状态，再看 Intent 匹配变化。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { PackageLabStore.toggleDeepLinkComponent(context) }) {
            Text(text = "切换 DeepLinkActivity enabled")
        }
        Spacer(modifier = Modifier.height(10.dp))
        components.forEachIndexed { index, component ->
            NumberedBlock(index = index + 1) {
                Text(text = "${component.type} · ${component.name}", fontWeight = FontWeight.Bold)
                InfoRow(label = "exported", value = component.exported)
                InfoRow(label = "enabled", value = component.enabled)
                InfoRow(label = "permission", value = component.permission)
                Text(text = component.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != components.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun IntentLabCard(result: IntentResult) {
    val context = LocalContext.current
    LabCard(background = Color(0xFFEFF4EA)) {
        SectionTitle(title = "Intent 解析实验区")
        Text(
            text = "选择一个 Intent 场景，观察 resolveActivity 和 queryIntentActivities 的返回差异。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonGrid(
            buttons = IntentScenario.entries.map { scenario ->
                scenario.title to { PackageLabStore.runIntentScenario(context, scenario) }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(label = "场景", value = result.scenario)
        InfoRow(label = "Intent", value = result.intentSummary)
        InfoRow(label = "默认解析", value = result.resolvedName)
        Text(text = result.conclusion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4D6B58))
        Spacer(modifier = Modifier.height(10.dp))
        if (result.candidates.isEmpty()) {
            Text(text = "候选列表为空。", style = MaterialTheme.typography.bodySmall)
        } else {
            result.candidates.forEachIndexed { index, candidate ->
                NumberedBlock(index = index + 1) {
                    Text(text = candidate.label, fontWeight = FontWeight.Bold)
                    InfoRow(label = "package", value = candidate.packageName)
                    InfoRow(label = "activity", value = candidate.className)
                    InfoRow(label = "exported", value = candidate.exported)
                    InfoRow(label = "permission", value = candidate.permission)
                }
                if (index != result.candidates.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VisibilityCardList(cards: List<VisibilityCard>) {
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "包可见性观察")
        Text(
            text = "Android 11+ 以后，能不能查询到目标包，本身就是一个需要被设计的能力。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.target, fontWeight = FontWeight.Bold)
                InfoRow(label = "query", value = card.query)
                InfoRow(label = "result", value = card.result)
                Text(text = card.meaning, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PermissionCardList(cards: List<PermissionCard>) {
    LabCard(background = Color(0xFFFFFBEB)) {
        SectionTitle(title = "权限状态卡")
        cards.forEachIndexed { index, card ->
            NumberedBlock(index = index + 1) {
                Text(text = card.name, fontWeight = FontWeight.Bold)
                InfoRow(label = "protection", value = card.protection)
                InfoRow(label = "declared", value = card.declared)
                InfoRow(label = "granted", value = card.granted)
                Text(text = card.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            }
            if (index != cards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DiagnosticCard(cards: List<PackageDiagnosticCard>) {
    LabCard(background = Color(0xFFF3EDE6)) {
        SectionTitle(title = "包管理问题诊断卡")
        cards.forEach { card ->
            OutlinedButton(onClick = { PackageLabStore.markDiagnosisRead(card.title) }) {
                Text(text = card.title)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LabelText(label = "现象", text = card.symptom, color = Color(0xFF8A6A25))
            LabelText(label = "第一证据", text = card.firstEvidence, color = Color(0xFF315F72))
            LabelText(label = "修复方向", text = card.fixDirection, color = Color(0xFF4D6B58))
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun EventTrailCard(events: List<PackageEventLog>) {
    LabCard(background = Color(0xFF263A45), contentColor = Color.White) {
        SectionTitle(title = "包管理事件轨迹")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { PackageLabStore.clearEvents() }) {
            Text(text = "清空轨迹")
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (events.isEmpty()) {
            Text(text = "等待第一条 PackageManager 证据。", color = Color(0xFFE7EEF4))
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
private fun ButtonGrid(buttons: List<Pair<String, () -> Unit>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (title, action) ->
                    Button(onClick = action, modifier = Modifier.weight(1f)) {
                        Text(text = title)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
private fun PackageLabPreview() {
    PackageLabScreen(
        state = PackageLabState(
            identity = AppIdentity(packageName = "com.helloandroid.packagemanager"),
            components = defaultPackageDiagnosticCards.take(2).mapIndexed { index, card ->
                ComponentCard("Activity", card.title, exported = "是", enabled = "是", permission = "无", note = "preview-$index")
            }
        )
    )
}
