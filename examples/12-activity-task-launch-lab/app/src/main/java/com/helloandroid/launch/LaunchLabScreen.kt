package com.helloandroid.launch

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LaunchLabApp(
    screenTitle: String,
    screenRole: String,
    primaryAction: Pair<String, () -> Unit>,
    secondaryAction: Pair<String, () -> Unit>,
    tertiaryAction: Pair<String, () -> Unit>
) {
    val state by LaunchTraceStore.state.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2F6F73),
            secondary = Color(0xFF9A6B22),
            tertiary = Color(0xFF4A5A89),
            surface = Color.White,
            background = Color(0xFFF8F7F4)
        )
    ) {
        LaunchLabScreen(
            state = state,
            screenTitle = screenTitle,
            screenRole = screenRole,
            primaryAction = primaryAction,
            secondaryAction = secondaryAction,
            tertiaryAction = tertiaryAction
        )
    }
}

@Composable
fun LaunchLabScreen(
    state: LaunchLabState,
    screenTitle: String,
    screenRole: String,
    primaryAction: Pair<String, () -> Unit>,
    secondaryAction: Pair<String, () -> Unit>,
    tertiaryAction: Pair<String, () -> Unit>
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeaderCard(screenTitle = screenTitle, screenRole = screenRole) }
            item { LaunchScoreCard(state = state) }
            item { ActionCard(primaryAction, secondaryAction, tertiaryAction) }
            item { ExpectedActualCard(expectedActual = state.expectedActual) }
            item { MissionCard(state = state) }
            item { ProcessCard(state = state) }
            item { StackSnapshotCard(nodes = state.stackSnapshot) }
            item { LaunchModelSectionCard(models = state.launchModels) }
            item { TraceCard(events = state.traceEvents) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard(screenTitle: String, screenRole: String) {
    LabCard(background = Color(0xFF243B53), contentColor = Color.White) {
        Text(
            text = "第12章 Activity 启动与任务栈实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = screenTitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFFF4D35E))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = screenRole, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE7EEF4))
    }
}

@Composable
private fun LaunchScoreCard(state: LaunchLabState) {
    val score = calculateLaunchScore(state)
    val status = when {
        score >= 90 -> "观察完成：已经覆盖普通启动、singleTop 和 CLEAR_TOP。"
        score >= 65 -> "接近通关：再补一个启动模式实验。"
        score >= 35 -> "实验进行中：生命周期脚印已经出现。"
        else -> "准备开始：先触发一次页面启动。"
    }

    LabCard(background = Color(0xFFFFF7D6)) {
        SectionTitle(title = "启动观察分数")
        Text(
            text = "$score / 100",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF9A6B22),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = status, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4D4538))
        Spacer(modifier = Modifier.height(10.dp))
        BulletText(text = "25 分：普通启动详情页")
        BulletText(text = "25 分：触发 singleTop 启动")
        BulletText(text = "25 分：观察 onNewIntent")
        BulletText(text = "25 分：使用 CLEAR_TOP 回首页")
        if (state.duplicateDetailCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "加分观察：已经制造 ${state.duplicateDetailCount} 次连续详情页启动。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9A6B22),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionCard(
    primaryAction: Pair<String, () -> Unit>,
    secondaryAction: Pair<String, () -> Unit>,
    tertiaryAction: Pair<String, () -> Unit>
) {
    LabCard {
        SectionTitle(title = "启动操作台")
        Button(onClick = primaryAction.second, modifier = Modifier.fillMaxWidth()) {
            Text(text = primaryAction.first)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = secondaryAction.second, modifier = Modifier.fillMaxWidth()) {
            Text(text = secondaryAction.first)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = tertiaryAction.second, modifier = Modifier.fillMaxWidth()) {
            Text(text = tertiaryAction.first)
        }
    }
}

@Composable
private fun ExpectedActualCard(expectedActual: LaunchExpectedActual) {
    LabCard(background = Color(0xFFF7EEE7)) {
        SectionTitle(title = "预期 vs 实际")
        InfoRow(label = "本次操作", value = expectedActual.operation)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "预期",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9A6B22)
        )
        Text(text = expectedActual.expected, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "实际",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F6F73)
        )
        Text(text = expectedActual.actual, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "结论：${expectedActual.verdict}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4A5A89),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MissionCard(state: LaunchLabState) {
    LabCard(background = Color(0xFFEAF3F1)) {
        SectionTitle(title = "Activity 启动任务卡")
        state.missions.forEachIndexed { index, mission ->
            TimelineRow(index = index + 1, text = mission.title)
            MissionStatusText(done = isMissionDone(index, state))
            Text(
                text = mission.clue,
                modifier = Modifier.padding(start = 34.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44514D)
            )
            Text(
                text = mission.action,
                modifier = Modifier.padding(start = 34.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2F6F73),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProcessCard(state: LaunchLabState) {
    LabCard {
        SectionTitle(title = "当前观察状态")
        InfoRow(label = "当前页面", value = state.currentScreen)
        InfoRow(label = "本地 PID", value = state.processInfo.pid.toString())
        InfoRow(label = "当前线程", value = state.processInfo.threadName)
        InfoRow(label = "进程启动年龄", value = "${state.processInfo.processAgeMs}ms")
        InfoRow(label = "普通启动次数", value = state.standardLaunchCount.toString())
        InfoRow(label = "singleTop 启动次数", value = state.singleTopLaunchCount.toString())
        InfoRow(label = "onNewIntent 次数", value = state.newIntentCount.toString())
        InfoRow(label = "CLEAR_TOP 次数", value = state.clearTopCount.toString())
        InfoRow(label = "连续详情实验", value = state.duplicateDetailCount.toString())
    }
}

@Composable
private fun StackSnapshotCard(nodes: List<LaunchStackNode>) {
    LabCard(background = Color(0xFFEFF3FA)) {
        SectionTitle(title = "任务栈推断图")
        Text(
            text = "这是根据按钮操作和生命周期日志推断出的模型，不是系统直接暴露的真实 Task 数据。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        nodes.asReversed().forEachIndexed { index, node ->
            val isTop = index == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isTop) Color(0xFF4A5A89) else Color(0xFFB8C6D9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isTop) "顶" else (nodes.size - index).toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = node.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF66736F)
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchModelSectionCard(models: List<LaunchModelCard>) {
    LabCard(background = Color(0xFFF1EFE8)) {
        SectionTitle(title = "任务栈模型卡片")
        models.forEach { model ->
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A6B22)
            )
            Text(text = model.rule, style = MaterialTheme.typography.bodyMedium)
            Text(text = model.observation, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TraceCard(events: List<LaunchTraceEvent>) {
    LabCard {
        SectionTitle(title = "生命周期与启动轨迹")
        if (events.isEmpty()) {
            Text(
                text = "暂无轨迹。点击启动按钮、返回或使用 CLEAR_TOP 后，这里会显示生命周期脚印。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF66736F)
            )
        } else {
            events.forEach { event ->
                Text(
                    text = "${event.timestamp}  ${event.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = event.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LabCard(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    contentColor: Color = Color(0xFF1D2B27),
    content: @Composable Column.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = background, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TimelineRow(index: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MissionStatusText(done: Boolean) {
    Text(
        text = if (done) "状态：已完成" else "状态：待观察",
        modifier = Modifier.padding(start = 34.dp),
        style = MaterialTheme.typography.bodySmall,
        color = if (done) Color(0xFF2F6F73) else Color(0xFF9A6B22),
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text(text = "•", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4D4538))
    }
}

private fun calculateLaunchScore(state: LaunchLabState): Int {
    var score = 0
    if (state.standardLaunchCount > 0) score += 25
    if (state.singleTopLaunchCount > 0) score += 25
    if (state.newIntentCount > 0) score += 25
    if (state.clearTopCount > 0) score += 25
    return score
}

private fun isMissionDone(index: Int, state: LaunchLabState): Boolean {
    return when (index) {
        0 -> state.standardLaunchCount > 0
        1 -> state.singleTopLaunchCount > 0 && state.newIntentCount > 0
        2 -> state.clearTopCount > 0
        3 -> state.duplicateDetailCount > 0
        4 -> state.traceEvents.any { it.title.contains("onNewIntent") } && state.clearTopCount > 0
        else -> false
    }
}

@Preview(showBackground = true)
@Composable
private fun LaunchLabScreenPreview() {
    MaterialTheme {
        LaunchLabScreen(
            state = LaunchLabState(
                processInfo = LaunchProcessInfo(pid = 13579, threadName = "main", processAgeMs = 40),
                standardLaunchCount = 1,
                singleTopLaunchCount = 2,
                newIntentCount = 1,
                clearTopCount = 1,
                duplicateDetailCount = 1,
                expectedActual = LaunchExpectedActual(
                    operation = "SingleTopActivity 启动 SingleTopActivity",
                    expected = "目标已经位于栈顶，singleTop 应复用当前实例。",
                    actual = "SingleTopActivity.onNewIntent 已出现。",
                    verdict = "实例被复用。"
                ),
                stackSnapshot = listOf(
                    LaunchStackNode("MainActivity", "任务栈根页面"),
                    LaunchStackNode("DetailActivity #1", "standard 新实例"),
                    LaunchStackNode("SingleTopActivity", "singleTop 观察页")
                ),
                traceEvents = listOf(
                    LaunchTraceEvent("SingleTopActivity.onNewIntent", "flags=0", "12:30:01.100")
                )
            ),
            screenTitle = "MainActivity",
            screenRole = "任务栈入口。",
            primaryAction = "普通启动详情页" to {},
            secondaryAction = "打开 singleTop 页面" to {},
            tertiaryAction = "清空轨迹" to {}
        )
    }
}
