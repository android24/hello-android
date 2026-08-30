package com.helloandroid.governance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GovernanceLabScreen(
    state: GovernanceLabState,
    report: String,
    onPolicySelected: (String) -> Unit,
    onScenarioSelected: (String) -> Unit,
    onRiskSelected: (String) -> Unit,
    onCompleteNextTask: () -> Unit,
    onCompleteArea: (GovernanceArea) -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HeaderSection(state)
                PolicySection(state, onPolicySelected)
                TaskBoard(state, onCompleteNextTask)
                ScenarioSection(state, onScenarioSelected)
                RiskSection(state, onRiskSelected)
                ModuleGovernanceSection(state, onCompleteArea)
                BuildSection(state, onCompleteArea)
                DependencySection(state, onCompleteArea)
                ReleaseSection(state, onCompleteArea)
                ReleaseGateSection(state, onCompleteArea)
                MonitorSection(state, onCompleteArea)
                TechDebtSection(state, onCompleteArea)
                ReportSection(report, onCompleteArea, onCopyReport, onShareReport)
            }
        }
    }
}

@Composable
private fun HeaderSection(state: GovernanceLabState) {
    SectionCard {
        Text(
            text = "第25章 工程治理工作台",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "把模块、组件、插件、构建、依赖、发布和监控放到同一张治理报告里。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricTile("健康分", state.healthScore.toString(), Modifier.weight(1f))
            MetricTile("发布结论", state.releaseDecision, Modifier.weight(1.4f))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "版本 ${state.appVersion} · build ${state.buildNumber} · ${state.gitCommit}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF64748B),
        )
        Text(
            text = state.notes,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2563EB),
        )
    }
}

@Composable
private fun PolicySection(state: GovernanceLabState, onPolicySelected: (String) -> Unit) {
    SectionCard(title = "治理规则模式") {
        Text(
            text = "同一批风险，放在不同发布策略下，结论可能不同；但 P0 永远不能被放行。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF475569),
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.policies.forEach { policy ->
                SelectableCard(
                    selected = policy.id == state.selectedPolicyId,
                    onClick = { onPolicySelected(policy.id) },
                    borderColor = Color(0xFF0F766E),
                ) {
                    Text(policy.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(policy.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                    Text(
                        "P1 结论：${policy.p1Decision} · 灰度最低分：${policy.minHealthToGray}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF0F766E),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskBoard(state: GovernanceLabState, onCompleteNextTask: () -> Unit) {
    SectionCard(title = "任务板 ${state.completedCount}/${state.tasks.size}") {
        state.nextTask?.let {
            Text(
                text = "当前建议：${it.title} - ${it.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF334155),
            )
        } ?: Text(
            text = "任务板已完成，可以整理治理报告。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF047857),
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.tasks.forEach { task ->
                val done = task.id in state.completedTaskIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (done) Color(0xFFEFF6FF) else Color(0xFFFFFFFF), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusDot(done)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        Text(task.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onCompleteNextTask) {
            Text("完成下一个观察点")
        }
    }
}

@Composable
private fun ScenarioSection(state: GovernanceLabState, onScenarioSelected: (String) -> Unit) {
    SectionCard(title = "事故剧本") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.scenarios.forEach { scenario ->
                val selected = scenario.id == state.selectedScenarioId
                SelectableCard(
                    selected = selected,
                    onClick = { onScenarioSelected(scenario.id) },
                ) {
                    Text(scenario.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("表面现象：${scenario.surface}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                    Text("隐藏风险：${scenario.hiddenRisk}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB45309))
                    Text("建议决策：${scenario.expectedDecision}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
                }
            }
        }
    }
}

@Composable
private fun RiskSection(state: GovernanceLabState, onRiskSelected: (String) -> Unit) {
    SectionCard(title = "风险证据") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.activeRisks.forEach { risk ->
                SelectableCard(
                    selected = risk.id == state.selectedRiskId,
                    onClick = { onRiskSelected(risk.id) },
                    borderColor = levelColor(risk.level),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RiskBadge(risk.level)
                        Spacer(Modifier.width(8.dp))
                        Text(risk.area.label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF475569))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(risk.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("证据：${risk.evidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                    Text("决策：${risk.decision}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7C2D12))
                }
            }
        }
    }
}

@Composable
private fun ModuleGovernanceSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "模块 / 组件 / 插件治理") {
        CodeBlock(
            """
            app
              -> feature:course
                  -> feature:payment.impl
                      -> feature:profile

            core:common
              -> feature:profile

            plugin:download
              -> minHostProtocol = 3
              -> currentHostProtocol = 2
            """.trimIndent(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "判断重点：core 反向依赖 feature 是 P0；组件绕过 contract 是 P1；插件协议不兼容是 P0。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF334155),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(GovernanceArea.Module) }) {
            Text("完成模块边界检查")
        }
        TextButton(onClick = { onCompleteArea(GovernanceArea.Component) }) {
            Text("完成组件契约检查")
        }
        TextButton(onClick = { onCompleteArea(GovernanceArea.Plugin) }) {
            Text("完成插件协议检查")
        }
        state.selectedRisk?.let {
            Divider(Modifier.padding(vertical = 8.dp))
            Text("当前动作：${it.action}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F766E))
        }
    }
}

@Composable
private fun BuildSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "构建效率面板") {
        state.buildMetrics.forEach { metric ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(metric.task, fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A))
                    Text(metric.hint, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
                Text(metric.duration, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(GovernanceArea.Build) }) {
            Text("完成构建耗时检查")
        }
    }
}

@Composable
private fun DependencySection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "依赖 / 资源 / 配置变化") {
        CodeBlock(
            """
            okio:3.6.0
               Selection reasons:
                  - By conflict resolution: between versions 3.6.0 and 2.10.0

            okio:2.10.0 -> 3.6.0
            \--- network-lib:1.5
                 \--- appDebugRuntimeClasspath
            """.trimIndent(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "结论：编译通过不代表运行时安全。network-lib 编译预期和最终 classpath 不一致，需要升级、约束或完整回归。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF334155),
        )
        Divider(Modifier.padding(vertical = 8.dp))
        state.configChanges.forEach { change ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                StatusDot(change.safe)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${change.name}：${change.value}", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text(change.risk, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(GovernanceArea.Dependency) }) {
            Text("完成依赖与配置检查")
        }
    }
}

@Composable
private fun ReleaseSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "发布决策") {
        InfoRow("mapping", state.mappingId, state.mappingId != "missing")
        InfoRow("symbols", state.symbolsId, true)
        InfoRow("remote config", state.remoteConfigVersion, true)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "底线规则：总分高也不能掩盖 mapping 缺失、签名错误或插件协议校验失败。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7C2D12),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(GovernanceArea.Release) }) {
            Text("完成发布产物检查")
        }
    }
}

@Composable
private fun ReleaseGateSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "发布门禁清单") {
        state.releaseGates.forEach { gate ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                StatusDot(gate.passed)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(gate.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text("负责人：${gate.owner}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    Text("证据：${gate.evidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "玩法：如果门禁失败项是 P0，就不能靠平均分放行；必须补负责人、补证据、补回滚。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7C2D12),
        )
        TextButton(onClick = { onCompleteArea(GovernanceArea.Release) }) {
            Text("完成发布门禁检查")
        }
    }
}

@Composable
private fun MonitorSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "监控告警面板") {
        state.releaseSignals.forEach { signal ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(signal.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text("基线：${signal.baseline}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
                Text(signal.current, fontWeight = FontWeight.Bold, color = levelColor(signal.status))
                Spacer(Modifier.width(8.dp))
                RiskBadge(signal.status)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(GovernanceArea.Monitor) }) {
            Text("完成监控告警检查")
        }
    }
}

@Composable
private fun TechDebtSection(state: GovernanceLabState, onCompleteArea: (GovernanceArea) -> Unit) {
    SectionCard(title = "技术债排序") {
        state.techDebts.forEachIndexed { index, debt ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "${index + 1}",
                    modifier = Modifier
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("${debt.area.label} · ${debt.owner} · ${debt.due}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    Text(debt.impact, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "玩法：把“这次必须修”和“可以排期偿还”分开，技术债才不会在下次发版时变成事故。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF0F766E),
        )
        TextButton(onClick = { onCompleteArea(GovernanceArea.Debt) }) {
            Text("完成技术债排序")
        }
    }
}

@Composable
private fun ReportSection(
    report: String,
    onCompleteArea: (GovernanceArea) -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
) {
    SectionCard(title = "治理报告") {
        CodeBlock(report)
        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onCompleteArea(GovernanceArea.Report) },
        ) {
            Text("确认治理报告")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onCopyReport,
            ) {
                Text("复制报告")
            }
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onShareReport,
            ) {
                Text("分享报告")
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Spacer(Modifier.height(6.dp))
            }
            content()
        }
    }
}

@Composable
private fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    borderColor: Color = Color(0xFF2563EB),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = if (selected) BorderStroke(1.5.dp, borderColor) else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFF8FAFC) else Color.White),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF475569))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
    }
}

@Composable
private fun StatusDot(done: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(if (done) Color(0xFF14B8A6) else Color(0xFFCBD5E1), CircleShape),
    )
}

@Composable
private fun RiskBadge(level: RiskLevel) {
    Text(
        text = level.label,
        modifier = Modifier
            .background(levelColor(level).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = levelColor(level),
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun InfoRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(ok)
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF475569))
        Text(value, fontWeight = FontWeight.SemiBold, color = if (ok) Color(0xFF047857) else Color(0xFFDC2626))
    }
}

@Composable
private fun CodeBlock(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(12.dp),
        color = Color(0xFFE2E8F0),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
    )
}

private fun levelColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.P0 -> Color(0xFFDC2626)
        RiskLevel.P1 -> Color(0xFFEA580C)
        RiskLevel.P2 -> Color(0xFFB45309)
        RiskLevel.P3 -> Color(0xFF047857)
    }
}
