package com.helloandroid.capstone

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
fun CapstoneScreen(
    state: CapstoneState,
    report: String,
    onPhaseSelected: (CapstonePhase) -> Unit,
    onReviewStageSelected: (String) -> Unit,
    onChapterSelected: (String) -> Unit,
    onScenarioSelected: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onToggleMission: (String) -> Unit,
    onToggleReleaseGate: (String) -> Unit,
    onRunTrace: () -> Unit,
    onSimulateIncident: () -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F8F3),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HeaderSection(state)
                EngineeringSnapshotSection(state)
                GraduationChallengeSection(state)
                PhaseSection(state, onPhaseSelected)
                ReviewMapSection(state, onReviewStageSelected)
                TaskBoardSection(state, onToggleTask)
                PracticeMissionSection(state, onToggleMission)
                CoursePathSection(state, onChapterSelected, onRunTrace)
                FrameworkCausalitySection(state)
                IncidentSection(state, onScenarioSelected, onSimulateIncident)
                RecallQuestionSection(state)
                EvidenceTimelineSection(state)
                AbilityRadarSection(state)
                ReleaseGateSection(state, onToggleReleaseGate)
                DefenseRubricSection(state)
                OperationHistorySection(state)
                DefenseSection(report, onCopyReport, onShareReport)
            }
        }
    }
}

@Composable
private fun HeaderSection(state: CapstoneState) {
    SectionCard {
        Text(
            text = "第26章 Hello Android Capstone",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17201B),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "把主路径、架构、Framework、诊断、治理和答辩放进同一个毕业项目。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "buildProfile = ${state.engineeringSnapshot.buildProfile} · Logcat Tag = HelloCapstone",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF6F4E1E),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricTile(
                "综合完成",
                "${state.doneTaskCount + state.completedMissionCount}/${state.tasks.size + state.practiceMissions.size}",
                Modifier.weight(1f),
            )
            MetricTile("能力均分", state.abilityScore.toString(), Modifier.weight(1f))
            MetricTile("挑战进度", "${state.challengeCompletion}%", Modifier.weight(1f))
            MetricTile("毕业结论", state.graduationDecision, Modifier.weight(1.5f))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = state.notes,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1D6A62),
        )
    }
}

@Composable
private fun GraduationChallengeSection(state: CapstoneState) {
    SectionCard(title = "毕业挑战模式") {
        Text(
            text = "按这 6 步走完终章：先选路线，再追链路，再注入事故，再补证据，再清 P0，最后生成答辩材料。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricTile("挑战完成", "${state.challengeSteps.count { it.done }}/${state.challengeSteps.size}", Modifier.weight(1f))
            MetricTile("进度", "${state.challengeCompletion}%", Modifier.weight(1f))
            MetricTile("当前状态", state.graduationDecision, Modifier.weight(1.4f))
        }
        state.challengeSteps.forEach { step ->
            EvidenceCard(
                selected = step.done,
                borderColor = if (step.done) Color(0xFF1D6A62) else Color(0xFFB45309),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    StatusDot(if (step.done) Color(0xFF1D6A62) else Color(0xFFB45309))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(step.title, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                        Text(step.goal, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                        Text("证据：${step.evidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F4E1E))
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineeringSnapshotSection(state: CapstoneState) {
    SectionCard(title = "工程快照与真实观察点") {
        Text(
            text = "这一块把 Demo 从“页面模拟”往真实工程拉近：点击链路和事故注入会写入 Trace section 与 Logcat，报告也会带上 BuildConfig 快照。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricTile("AppId", state.engineeringSnapshot.appId, Modifier.weight(1.4f))
            MetricTile("Version", "${state.engineeringSnapshot.versionName}(${state.engineeringSnapshot.versionCode})", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricTile("Profile", state.engineeringSnapshot.buildProfile, Modifier.weight(1f))
            MetricTile("Debug", state.engineeringSnapshot.debug.toString(), Modifier.weight(1f))
            MetricTile("StartMs", state.engineeringSnapshot.sessionStartMs.toString(), Modifier.weight(1f))
        }
        CodeBlock(
            """
            Trace.beginSection("CapstoneRunTrace")
            Trace.beginSection("CapstoneIncident:<scenarioId>")
            Logcat tag: HelloCapstone
            clock: SystemClock.elapsedRealtime()
            """.trimIndent(),
        )
    }
}

@Composable
private fun PhaseSection(state: CapstoneState, onPhaseSelected: (CapstonePhase) -> Unit) {
    SectionCard(title = "分阶段毕业路线") {
        CapstonePhase.entries.forEach { phase ->
            SelectableCard(
                selected = state.selectedPhase == phase,
                onClick = { onPhaseSelected(phase) },
            ) {
                Text(phase.label, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                Text(phase.goal, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
            }
        }
    }
}

@Composable
private fun ReviewMapSection(state: CapstoneState, onReviewStageSelected: (String) -> Unit) {
    SectionCard(title = "课程回顾地图") {
        Text(
            text = "终章 Demo 的第一件事，是把 1 到 26 章重新串成能力路线。选中一个阶段后，先回答关键问题，再完成对应动手证据。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.reviewStages.forEach { stage ->
            SelectableCard(
                selected = stage.id == state.selectedReviewStageId,
                onClick = { onReviewStageSelected(stage.id) },
                borderColor = Color(0xFF2563EB),
            ) {
                Text(stage.title, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                Text("章节：${stage.chapters}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1D6A62))
                Text("关键问题：${stage.keyQuestion}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
            }
        }
        Divider(Modifier.padding(vertical = 6.dp), color = Color(0xFFE3E7DA))
        Text(
            text = "当前动手目标：${state.selectedReviewStage.handsOnGoal}",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF17201B),
        )
        Text(
            text = "验收证据：${state.selectedReviewStage.proof}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F4E1E),
        )
    }
}

@Composable
private fun TaskBoardSection(state: CapstoneState, onToggleTask: (String) -> Unit) {
    SectionCard(title = "终章任务板") {
        Text(
            text = "点击任务可以切换状态。终章项目不是把勾打满，而是把证据补齐。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.tasks.forEach { task ->
            SelectableCard(
                selected = task.status == TaskStatus.Done,
                onClick = { onToggleTask(task.id) },
                borderColor = statusColor(task.status),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(statusColor(task.status))
                    Spacer(Modifier.width(8.dp))
                    Text(task.status.label, style = MaterialTheme.typography.labelMedium, color = statusColor(task.status))
                }
                Spacer(Modifier.height(4.dp))
                Text(task.title, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                Text("映射：${task.chapterMapping}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                Text("输出：${task.output}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F4E1E))
            }
        }
    }
}

@Composable
private fun PracticeMissionSection(state: CapstoneState, onToggleMission: (String) -> Unit) {
    SectionCard(title = "动手回顾实验室") {
        Text(
            text = "这些任务故意跨章节设计：每做一个动作，都要说清它回顾了哪些章节、修改了什么、留下什么证据。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.practiceMissions.forEach { mission ->
            SelectableCard(
                selected = mission.status == TaskStatus.Done,
                onClick = { onToggleMission(mission.id) },
                borderColor = statusColor(mission.status),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(statusColor(mission.status))
                    Spacer(Modifier.width(8.dp))
                    Text(mission.status.label, style = MaterialTheme.typography.labelMedium, color = statusColor(mission.status))
                }
                Spacer(Modifier.height(4.dp))
                Text(mission.title, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                Text("回顾：${mission.reviewTarget}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1D6A62))
                Text("动手：${mission.action}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                Text("证据：${mission.proof}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F4E1E))
            }
        }
    }
}

@Composable
private fun CoursePathSection(
    state: CapstoneState,
    onChapterSelected: (String) -> Unit,
    onRunTrace: () -> Unit,
) {
    SectionCard(title = "课程主路径") {
        Text(
            text = "当前打开：${state.selectedChapter.title}",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17201B),
        )
        Text(state.selectedChapter.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
        Text(
            text = "阶段：${state.selectedChapter.stage} · 进度 ${state.selectedChapter.progress}% · ${state.selectedChapter.offlineState}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF1D6A62),
        )
        Spacer(Modifier.height(10.dp))
        state.chapters.forEach { chapter ->
            SelectableCard(
                selected = chapter.id == state.selectedChapterId,
                onClick = { onChapterSelected(chapter.id) },
            ) {
                Text(chapter.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF17201B))
                Text(chapter.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRunTrace) {
            Text("重新追踪一次点击链路")
        }
    }
}

@Composable
private fun FrameworkCausalitySection(state: CapstoneState) {
    SectionCard(title = "Framework 因果链") {
        Text(
            text = "不要只背类名，试着讲清每个节点为什么存在、解决什么问题、能留下什么证据。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.frameworkNodes.forEach { node ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                StatusDot(Color(0xFF1D6A62))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(node.name, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                    Text("为什么：${node.why}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                    Text("证据：${node.evidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F4E1E))
                }
            }
        }
    }
}

@Composable
private fun IncidentSection(
    state: CapstoneState,
    onScenarioSelected: (String) -> Unit,
    onSimulateIncident: () -> Unit,
) {
    SectionCard(title = "事故剧本实验区") {
        Text(
            text = "注入事故后，相关任务和动手实验会被推进：未开始进入进行中，进行中进入需要补证据。这不是为了替你完成任务，而是提醒你：事故一出现，工程状态就必须跟着变化。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.scenarios.forEach { scenario ->
            SelectableCard(
                selected = scenario.id == state.selectedScenarioId,
                onClick = { onScenarioSelected(scenario.id) },
                borderColor = Color(0xFFB45309),
            ) {
                Text(scenario.title, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                Text("现象：${scenario.symptom}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                Text("章节映射：${scenario.chapterMapping}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1D6A62))
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSimulateIncident) {
            Text("注入当前事故证据")
        }
    }
}

@Composable
private fun RecallQuestionSection(state: CapstoneState) {
    SectionCard(title = "复盘问答卡") {
        Text(
            text = "每完成一次操作，都用这些问题逼自己回到原理。能回答，才说明 Demo 没有只停留在点点按钮。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.recallQuestions.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("${index + 1}.", fontWeight = FontWeight.Bold, color = Color(0xFF1D6A62))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.question, fontWeight = FontWeight.SemiBold, color = Color(0xFF17201B))
                    Text("提示：${item.answerHint}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F4E1E))
                }
            }
        }
    }
}

@Composable
private fun EvidenceTimelineSection(state: CapstoneState) {
    SectionCard(title = "traceId 与证据时间线") {
        Text(
            text = "traceId = ${state.traceId}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17201B),
        )
        Spacer(Modifier.height(8.dp))
        CodeBlock(state.traceEvents.joinToString("\n"))
    }
}

@Composable
private fun AbilityRadarSection(state: CapstoneState) {
    SectionCard(title = "终章项目能力雷达") {
        state.dynamicAbilityRadar.forEach { ability ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ability.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Color(0xFF17201B))
                    Text("${ability.score}", fontWeight = FontWeight.Bold, color = scoreColor(ability.score))
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFE3E7DA), RoundedCornerShape(8.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ability.score / 100f)
                            .height(8.dp)
                            .background(scoreColor(ability.score), RoundedCornerShape(8.dp)),
                    )
                }
                Text(ability.evidence, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
            }
        }
    }
}

@Composable
private fun ReleaseGateSection(state: CapstoneState, onToggleReleaseGate: (String) -> Unit) {
    SectionCard(title = "发布门禁与治理结论") {
        Text(
            text = "点击门禁可以切换通过状态。试着补齐 mapping 和签名证据，观察 P0 消失后能力雷达、毕业结论和报告建议如何变化。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.releaseGates.forEach { gate ->
            SelectableCard(
                selected = gate.passed,
                onClick = { onToggleReleaseGate(gate.title) },
                borderColor = if (gate.passed) Color(0xFF1D6A62) else riskColor(gate.level),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    StatusDot(if (gate.passed) Color(0xFF1D6A62) else riskColor(gate.level))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${gate.level.label}｜${gate.title}", fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
                        Text(if (gate.passed) "状态：通过" else "状态：未通过", style = MaterialTheme.typography.labelMedium, color = if (gate.passed) Color(0xFF1D6A62) else riskColor(gate.level))
                        Text(gate.evidence, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                    }
                }
            }
        }
        Divider(Modifier.padding(vertical = 8.dp))
        Text(
            text = "治理结论：${state.graduationDecision}",
            fontWeight = FontWeight.Bold,
            color = if (state.hasBlockingGate) Color(0xFFB91C1C) else Color(0xFF1D6A62),
        )
    }
}

@Composable
private fun DefenseRubricSection(state: CapstoneState) {
    SectionCard(title = "答辩评分 Rubric") {
        Text(
            text = "不要只问“我能不能毕业”，要问“别人追问到哪一层，我还能不能拿出证据”。这张 Rubric 就是终章答辩的刻度尺。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.defenseRubric.forEach { item ->
            EvidenceCard(
                selected = state.abilityScore >= item.level.scoreThreshold(),
                borderColor = scoreColor(item.level.scoreThreshold()),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(item.level, fontWeight = FontWeight.Bold, color = scoreColor(item.level.scoreThreshold()))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.standard, fontWeight = FontWeight.SemiBold, color = Color(0xFF17201B))
                        Text("证据：${item.evidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationHistorySection(state: CapstoneState) {
    SectionCard(title = "操作记录与验收轨迹") {
        Text(
            text = "这里记录最近 12 次关键动作。毕业报告会带上这些轨迹，用来说明你不是只得到一个结果，而是真的走过诊断和治理过程。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        state.operationHistory.forEach { record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "t+${record.timestampMs}ms",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1D6A62),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF17201B))
                    Text(record.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF536159))
                }
            }
        }
    }
}

@Composable
private fun DefenseSection(
    report: String,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
) {
    SectionCard(title = "毕业答辩报告") {
        Text(
            text = "优秀答辩不是把所有代码讲完，而是用 10 到 15 分钟讲清：目标、主路径、架构、Framework 因果链、事故诊断、发布治理。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF536159),
        )
        Spacer(Modifier.height(8.dp))
        CodeBlock(report)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCopyReport) {
                Text("复制报告")
            }
            TextButton(onClick = onShareReport) {
                Text("分享报告")
            }
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        title?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
            Divider(color = Color(0xFFE3E7DA))
        }
        content()
    }
}

@Composable
private fun EvidenceCard(
    selected: Boolean,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    val background = if (selected) Color(0xFFEAF4EF) else Color(0xFFFBFCF8)
    val stroke = if (selected) borderColor else Color(0xFFE3E7DA)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, stroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}

@Composable
private fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    borderColor: Color = Color(0xFF1D6A62),
    content: @Composable ColumnScope.() -> Unit,
) {
    val background = if (selected) Color(0xFFEAF4EF) else Color(0xFFFBFCF8)
    val stroke = if (selected) borderColor else Color(0xFFE3E7DA)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, stroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFEAF4EF), RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF536159))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF17201B))
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape),
    )
}

@Composable
private fun CodeBlock(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF17201B), RoundedCornerShape(8.dp))
            .padding(12.dp),
        color = Color(0xFFEAF4EF),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
    )
}

private fun statusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.NotStarted -> Color(0xFF64748B)
        TaskStatus.Doing -> Color(0xFF2563EB)
        TaskStatus.NeedEvidence -> Color(0xFFB45309)
        TaskStatus.Done -> Color(0xFF1D6A62)
    }
}

private fun riskColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.P0 -> Color(0xFFB91C1C)
        RiskLevel.P1 -> Color(0xFFB45309)
        RiskLevel.P2 -> Color(0xFF6F4E1E)
        RiskLevel.P3 -> Color(0xFF64748B)
    }
}

private fun scoreColor(score: Int): Color {
    return when {
        score >= 88 -> Color(0xFF1D6A62)
        score >= 80 -> Color(0xFF2563EB)
        score >= 70 -> Color(0xFFB45309)
        else -> Color(0xFFB91C1C)
    }
}

private fun String.scoreThreshold(): Int {
    return when {
        startsWith("95") -> 95
        startsWith("90") -> 90
        startsWith("80") -> 80
        startsWith("70") -> 70
        else -> 60
    }
}
