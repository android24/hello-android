package com.helloandroid.binder

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
fun BinderLabRoute(
    viewModel: BinderLabViewModel,
    onBindRemoteService: () -> Unit,
    onUnbindRemoteService: () -> Unit,
    onSendBinderMessage: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BinderLabScreen(
        state = state,
        onBindRemoteService = onBindRemoteService,
        onUnbindRemoteService = onUnbindRemoteService,
        onSendBinderMessage = onSendBinderMessage,
        onClearEvents = viewModel::clearEvents
    )
}

@Composable
fun BinderLabScreen(
    state: BinderLabState,
    onBindRemoteService: () -> Unit,
    onUnbindRemoteService: () -> Unit,
    onSendBinderMessage: () -> Unit,
    onClearEvents: () -> Unit
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
            item { MissionCard(missions = state.missions) }
            item { ProcessCard(processInfo = state.processInfo) }
            item { RemoteBinderCard(state, onBindRemoteService, onUnbindRemoteService, onSendBinderMessage) }
            item { SystemServiceCard(services = state.systemServices) }
            item { BinderModelCard(steps = state.binderModel) }
            item { CallTraceCard(events = state.callEvents, onClearEvents = onClearEvents) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard(background = Color(0xFF16324F), contentColor = Color.White) {
        Text(
            text = "第11章 Binder 与系统服务实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从系统服务窗口、Binder 通道和远程 Service 往返开始，理解 App 如何向系统提交请求。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE5F1F4)
        )
    }
}

@Composable
private fun MissionCard(missions: List<BinderMission>) {
    LabCard(background = Color(0xFFEAF6F7)) {
        SectionTitle(title = "系统服务追踪任务卡")
        Text(
            text = "按任务卡完成一轮观察：先看 Manager，再绑定远程 Service，最后写出 Binder 调用链报告。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2D4248)
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
                color = Color(0xFF146C78),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProcessCard(processInfo: BinderProcessInfo) {
    LabCard {
        SectionTitle(title = "App 进程观察")
        InfoRow(label = "本地 PID", value = processInfo.pid.toString())
        InfoRow(label = "当前线程", value = processInfo.threadName)
        InfoRow(label = "进程启动年龄", value = "${processInfo.processAgeMs}ms")
        InfoRow(label = "Package", value = processInfo.packageName)
    }
}

@Composable
private fun RemoteBinderCard(
    state: BinderLabState,
    onBindRemoteService: () -> Unit,
    onUnbindRemoteService: () -> Unit,
    onSendBinderMessage: () -> Unit
) {
    LabCard {
        SectionTitle(title = "远程 Service / Binder 通道")
        InfoRow(label = "绑定状态", value = if (state.isRemoteServiceBound) "已绑定" else "未绑定")
        Text(
            text = state.remoteStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF44514D)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.lastReply,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF146C78),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBindRemoteService) {
                Text(text = "绑定")
            }
            OutlinedButton(onClick = onSendBinderMessage) {
                Text(text = "发送")
            }
            OutlinedButton(onClick = onUnbindRemoteService) {
                Text(text = "解绑")
            }
        }
    }
}

@Composable
private fun SystemServiceCard(services: List<SystemServiceItem>) {
    LabCard {
        SectionTitle(title = "系统服务观察")
        services.forEach { service ->
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = service.appEntry, style = MaterialTheme.typography.bodySmall, color = Color(0xFF525E87))
            Text(text = service.responsibility, style = MaterialTheme.typography.bodyMedium)
            Text(text = service.binderHint, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BinderModelCard(steps: List<BinderModelStep>) {
    LabCard(background = Color(0xFFFFF5E8)) {
        SectionTitle(title = "Binder 调用模型")
        steps.forEachIndexed { index, step ->
            TimelineRow(index = index + 1, text = step.name)
            Text(
                text = step.detail,
                modifier = Modifier.padding(start = 34.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5B4630)
            )
        }
    }
}

@Composable
private fun CallTraceCard(events: List<BinderCallEvent>, onClearEvents: () -> Unit) {
    LabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Binder 调用轨迹",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onClearEvents) {
                Text(text = "清空")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (events.isEmpty()) {
            Text(
                text = "暂无轨迹。绑定远程 Service 并发送消息后，这里会显示请求和回复。",
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
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(0.6f),
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

@Preview(showBackground = true)
@Composable
private fun BinderLabScreenPreview() {
    MaterialTheme {
        BinderLabScreen(
            state = BinderLabState(
                processInfo = BinderProcessInfo(
                    pid = 24680,
                    threadName = "main",
                    processAgeMs = 38,
                    packageName = "com.helloandroid.binder"
                ),
                isRemoteServiceBound = true,
                remoteStatus = "远程 Service 已绑定，Binder 通道已建立。",
                lastReply = "requestId=1, remotePid=24699, remoteThread=main, roundTrip=2ms",
                callEvents = listOf(
                    BinderCallEvent("Remote reply", "requestId=1, remotePid=24699, roundTrip=2ms", "11:20:12.040")
                )
            ),
            onBindRemoteService = {},
            onUnbindRemoteService = {},
            onSendBinderMessage = {},
            onClearEvents = {}
        )
    }
}
