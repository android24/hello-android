package com.helloandroid.framework

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
fun FrameworkWalkthroughRoute(viewModel: FrameworkWalkthroughViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FrameworkWalkthroughScreen(
        state = state,
        onSendHandlerMessage = viewModel::sendHandlerMessage,
        onClearTrace = viewModel::clearTrace
    )
}

@Composable
fun FrameworkWalkthroughScreen(
    state: FrameworkWalkthroughState,
    onSendHandlerMessage: () -> Unit,
    onClearTrace: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeaderCard() }
            item { MissionDeckCard(missions = state.missionCards) }
            item { ProcessCard(processInfo = state.processInfo) }
            item { LaunchChainCard(chain = state.launchChain) }
            item { HandlerCard(report = state.handlerReport, onSend = onSendHandlerMessage) }
            item { LayerMapCard(layers = state.layers) }
            item { SourceTargetCard(targets = state.sourceTargets) }
            item { BinderCard(examples = state.binderExamples) }
            item {
                TraceCard(
                    events = state.lifecycleEvents,
                    onClearTrace = onClearTrace
                )
            }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard(background = Color(0xFF12312B), contentColor = Color.White) {
        Text(
            text = "第10章 Framework 观察实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从一次启动、一次生命周期回调和一次 Handler 消息开始，给 AOSP 源码阅读找到第一批路标。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFDDEBE7)
        )
    }
}

@Composable
private fun MissionDeckCard(missions: List<MissionCard>) {
    LabCard(background = Color(0xFFEAF4F1)) {
        SectionTitle(title = "Framework 追踪任务卡")
        Text(
            text = "按顺序完成这些任务，你会把一次普通运行拆成可观察、可定位、可复盘的 Framework 线索。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF33413D)
        )
        Spacer(modifier = Modifier.height(12.dp))
        missions.forEachIndexed { index, mission ->
            TimelineRow(index = index + 1, text = mission.title)
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
                color = Color(0xFF315F8C),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProcessCard(processInfo: ProcessInfo) {
    LabCard {
        SectionTitle(title = "进程、线程与 Context")
        InfoRow(label = "PID", value = processInfo.pid.toString())
        InfoRow(label = "当前线程", value = processInfo.threadName)
        InfoRow(label = "主 Looper 线程", value = processInfo.mainLooperThread)
        InfoRow(label = "当前是否主 Looper", value = processInfo.isMainLooper.toString())
        InfoRow(label = "SDK", value = processInfo.sdkInt.toString())
        InfoRow(label = "进程到 Activity.onCreate", value = "${processInfo.elapsedFromProcessStart}ms")
        InfoRow(label = "Activity Context", value = processInfo.activityContextName)
        InfoRow(label = "Application Context", value = processInfo.applicationContextName)
    }
}

@Composable
private fun LaunchChainCard(chain: List<String>) {
    LabCard {
        SectionTitle(title = "App 启动链路")
        chain.forEachIndexed { index, item ->
            TimelineRow(index = index + 1, text = item)
        }
    }
}

@Composable
private fun HandlerCard(report: String, onSend: () -> Unit) {
    LabCard {
        SectionTitle(title = "Handler / Looper 实验")
        Text(
            text = report,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF33413D)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onSend) {
            Text(text = "发送 Handler 消息")
        }
    }
}

@Composable
private fun LayerMapCard(layers: List<FrameworkLayer>) {
    LabCard {
        SectionTitle(title = "系统分层地图")
        layers.forEach { layer ->
            Text(
                text = layer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = layer.role, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = layer.examples,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF66736F)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SourceTargetCard(targets: List<SourceTarget>) {
    LabCard {
        SectionTitle(title = "AOSP 源码入口")
        targets.forEach { target ->
            Text(
                text = target.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = target.path,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF315F8C)
            )
            Text(
                text = target.question,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF44514D)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BinderCard(examples: List<String>) {
    LabCard(background = Color(0xFFFFF8E6)) {
        SectionTitle(title = "Binder 初识")
        Text(
            text = "App 和 system_server 不在同一进程。Framework API 经常把 Binder 通信包装得像普通方法调用。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4E3B1F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        examples.forEach { item ->
            BulletText(text = item)
        }
    }
}

@Composable
private fun TraceCard(events: List<TraceEvent>, onClearTrace: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "生命周期与消息轨迹",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onClearTrace) {
                Text(text = "清空")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (events.isEmpty()) {
            Text(
                text = "暂无轨迹。启动页面、切到后台或点击 Handler 按钮后，这里会记录新的观察点。",
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
                Text(
                    text = event.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF66736F)
                )
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
        colors = CardDefaults.cardColors(
            containerColor = background,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
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
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BulletText(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "•", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun FrameworkWalkthroughScreenPreview() {
    MaterialTheme {
        FrameworkWalkthroughScreen(
            state = FrameworkWalkthroughState(
                processInfo = ProcessInfo(
                    pid = 12345,
                    threadName = "main",
                    mainLooperThread = "main",
                    isMainLooper = true,
                    sdkInt = 35,
                    elapsedFromProcessStart = 42,
                    activityContextName = "MainActivity",
                    applicationContextName = "FrameworkWalkthroughApplication"
                ),
                lifecycleEvents = listOf(
                    TraceEvent("Activity.onResume", "Activity 已进入前台可交互阶段", "10:24:32.120"),
                    TraceEvent("Handler dispatch", "Runnable 在 main 线程执行，等待 1ms", "10:24:31.982")
                )
            ),
            onSendHandlerMessage = {},
            onClearTrace = {}
        )
    }
}
