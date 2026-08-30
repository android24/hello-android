package com.helloandroid.observability

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
fun ObservabilityLabScreen(
    state: ObservabilityLabState,
    report: String,
    onScenarioSelected: (String) -> Unit,
    onEvidenceSelected: (String) -> Unit,
    onCompleteNextTask: () -> Unit,
    onCompleteArea: (EvidenceArea) -> Unit,
    onTriggerMainThreadWork: () -> Unit,
    onTriggerMemoryGrowth: () -> Unit,
    onQuizAnswerSelected: (String, String) -> Unit,
    onSubmitQuiz: () -> Unit,
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
                TriggerSection(onTriggerMainThreadWork, onTriggerMemoryGrowth)
                TaskBoard(state, onCompleteNextTask)
                ScenarioSection(state, onScenarioSelected)
                EvidenceSection(state, onEvidenceSelected)
                CommandSection(state, onCompleteArea)
                PerfettoSection(onCompleteArea)
                MetricsSection(state, onCompleteArea)
                PrivacySection(onCompleteArea)
                QuizSection(state, onQuizAnswerSelected, onSubmitQuiz)
                ReportSection(report, onCompleteArea, onCopyReport, onShareReport)
            }
        }
    }
}

@Composable
private fun HeaderSection(state: ObservabilityLabState) {
    SectionCard {
        Text(
            text = "第24章 系统证据链分析实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "把 logcat、dumpsys、Perfetto、bugreport 和性能指标串成一份能复盘的报告。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricTile("诊断可信度", state.confidenceScore.toString(), Modifier.weight(1f))
            MetricTile("当前等级", state.diagnosisLevel, Modifier.weight(1.3f))
        }
        Spacer(Modifier.height(10.dp))
        Text(state.packageName, style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
        Text("traceId: ${state.activeTraceId}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
        Text(state.lastAction, style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F766E))
    }
}

@Composable
private fun TriggerSection(
    onTriggerMainThreadWork: () -> Unit,
    onTriggerMemoryGrowth: () -> Unit,
) {
    SectionCard(title = "现场触发器") {
        Text(
            text = "这两个按钮会制造轻量可观察信号，适合配合 logcat、Perfetto、gfxinfo 和 meminfo 练手。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF475569),
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = onTriggerMainThreadWork) {
                Text("制造主线程忙碌")
            }
            Button(modifier = Modifier.weight(1f), onClick = onTriggerMemoryGrowth) {
                Text("制造内存增长")
            }
        }
    }
}

@Composable
private fun TaskBoard(state: ObservabilityLabState, onCompleteNextTask: () -> Unit) {
    SectionCard(title = "任务板 ${state.completedCount}/${state.tasks.size}") {
        state.nextTask?.let {
            Text("当前建议：${it.title} - ${it.description}", color = Color(0xFF334155))
        } ?: Text("证据链已经闭合，可以整理报告。", color = Color(0xFF047857))
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.tasks.forEach { task ->
                val done = task.id in state.completedTaskIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (done) Color(0xFFEFF6FF) else Color.White, RoundedCornerShape(8.dp))
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
private fun ScenarioSection(state: ObservabilityLabState, onScenarioSelected: (String) -> Unit) {
    SectionCard(title = "事故剧本") {
        state.scenarios.forEach { scenario ->
            SelectableCard(
                selected = scenario.id == state.selectedScenarioId,
                onClick = { onScenarioSelected(scenario.id) },
            ) {
                Text(scenario.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("现象：${scenario.symptom}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                Text("第一问题：${scenario.firstQuestion}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7C2D12))
                Text("根因候选：${scenario.rootCandidate}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EvidenceSection(state: ObservabilityLabState, onEvidenceSelected: (String) -> Unit) {
    SectionCard(title = "证据链卡片") {
        state.activeEvidence.forEach { evidence ->
            SelectableCard(
                selected = evidence.id == state.selectedEvidenceId,
                onClick = { onEvidenceSelected(evidence.id) },
                borderColor = kindColor(evidence.kind),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KindBadge(evidence.kind)
                    Spacer(Modifier.width(8.dp))
                    Text(evidence.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Text("线索：${evidence.clue}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                Text("结论：${evidence.conclusion}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F766E))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CommandSection(state: ObservabilityLabState, onCompleteArea: (EvidenceArea) -> Unit) {
    SectionCard(title = "命令取证区") {
        val evidence = state.selectedEvidence
        if (evidence != null) {
            Text(evidence.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            CodeBlock(evidence.command)
            Text("下一步：${evidence.nextStep}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
        } else {
            Text("请选择一张证据卡片。", color = Color(0xFF64748B))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(modifier = Modifier.weight(1f), onClick = { onCompleteArea(EvidenceArea.Timeline) }) {
                Text("完成时间点")
            }
            TextButton(modifier = Modifier.weight(1f), onClick = { onCompleteArea(EvidenceArea.State) }) {
                Text("完成状态快照")
            }
        }
    }
}

@Composable
private fun PerfettoSection(onCompleteArea: (EvidenceArea) -> Unit) {
    SectionCard(title = "Perfetto 阅读路线") {
        CodeBlock(
            """
            1. 找到复现时间点
            2. 展开 app main thread
            3. 对齐 RenderThread / Binder / system_server
            4. 看慢帧附近谁在运行、谁在等待
            5. 回到 logcat 和 dumpsys 验证推论
            """.trimIndent(),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(EvidenceArea.Trace) }) {
            Text("完成 Perfetto 时间线检查")
        }
    }
}

@Composable
private fun MetricsSection(state: ObservabilityLabState, onCompleteArea: (EvidenceArea) -> Unit) {
    SectionCard(title = "性能证据面板") {
        state.metrics.forEach { metric ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(metric.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text("基线：${metric.baseline}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
                Text(metric.value, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                Spacer(Modifier.width(8.dp))
                Text(metric.status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C2D12))
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(EvidenceArea.Performance) }) {
            Text("完成性能指标检查")
        }
    }
}

@Composable
private fun PrivacySection(onCompleteArea: (EvidenceArea) -> Unit) {
    SectionCard(title = "脱敏与协作检查") {
        CodeBlock(
            """
            分享前检查：
            - 账号、手机号、token、定位、文件路径
            - 设备序列号、广告 ID、业务订单号
            - bugreport / trace 中的应用私有数据
            """.trimIndent(),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onCompleteArea(EvidenceArea.Privacy) }) {
            Text("完成脱敏检查")
        }
    }
}

@Composable
private fun QuizSection(
    state: ObservabilityLabState,
    onQuizAnswerSelected: (String, String) -> Unit,
    onSubmitQuiz: () -> Unit,
) {
    SectionCard(title = "证据链答题区") {
        Text(
            text = "选择你认为最能支撑结论的证据。提交后不要只看对错，要看解释为什么成立。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF475569),
        )
        Spacer(Modifier.height(8.dp))
        state.quizQuestions.forEach { question ->
            Text(question.prompt, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEach { option ->
                    val selected = state.selectedQuizAnswers[question.id] == option
                    val correct = state.quizSubmitted && option == question.answer
                    SelectableCard(
                        selected = selected || correct,
                        onClick = { onQuizAnswerSelected(question.id, option) },
                        borderColor = if (correct) Color(0xFF047857) else Color(0xFF2563EB),
                    ) {
                        Text(option, color = if (correct) Color(0xFF047857) else Color(0xFF334155))
                    }
                }
            }
            if (state.quizSubmitted) {
                Text(
                    text = "解释：${question.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0F766E),
                )
            }
            Divider(Modifier.padding(vertical = 8.dp))
        }
        Text(
            text = "当前结果：${state.quizResult}",
            fontWeight = FontWeight.Bold,
            color = if (state.quizSubmitted) Color(0xFF0F766E) else Color(0xFF64748B),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSubmitQuiz,
        ) {
            Text("提交证据链判断")
        }
    }
}

@Composable
private fun ReportSection(
    report: String,
    onCompleteArea: (EvidenceArea) -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
) {
    SectionCard(title = "证据链报告") {
        CodeBlock(report)
        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onCompleteArea(EvidenceArea.Report) },
        ) {
            Text("确认报告")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(modifier = Modifier.weight(1f), onClick = onCopyReport) {
                Text("复制报告")
            }
            TextButton(modifier = Modifier.weight(1f), onClick = onShareReport) {
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
private fun KindBadge(kind: EvidenceKind) {
    Text(
        text = kind.label,
        modifier = Modifier
            .background(kindColor(kind).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = kindColor(kind),
        fontWeight = FontWeight.Bold,
    )
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

private fun kindColor(kind: EvidenceKind): Color {
    return when (kind) {
        EvidenceKind.Logcat -> Color(0xFF2563EB)
        EvidenceKind.Dumpsys -> Color(0xFF0F766E)
        EvidenceKind.Perfetto -> Color(0xFF7C3AED)
        EvidenceKind.Bugreport -> Color(0xFFDC2626)
        EvidenceKind.Gfxinfo -> Color(0xFFEA580C)
        EvidenceKind.Meminfo -> Color(0xFFB45309)
        EvidenceKind.Procstats -> Color(0xFF475569)
        EvidenceKind.Simpleperf -> Color(0xFF0369A1)
    }
}
