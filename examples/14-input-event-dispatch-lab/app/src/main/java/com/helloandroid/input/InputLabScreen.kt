package com.helloandroid.input

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InputDispatchLabApp(
    onMainThreadBusy: () -> Unit
) {
    val state by InputLabStore.state.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2F6F73),
            secondary = Color(0xFF9A6B22),
            tertiary = Color(0xFF4A5A89),
            background = Color(0xFFF6F8F7),
            surface = Color.White
        )
    ) {
        InputLabScreen(state = state, onMainThreadBusy = onMainThreadBusy)
    }
}

@Composable
fun InputLabScreen(
    state: InputLabState,
    onMainThreadBusy: () -> Unit
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
            item { ScoreCard(score = state.score) }
            item { ExperimentCard(experiment = state.currentExperiment) }
            item { NativeDispatchCard(interceptMove = state.interceptMove) }
            item { ComposeGestureCard(onMainThreadBusy = onMainThreadBusy) }
            item { DiagnosticCard(cards = state.diagnosticCards) }
            item { EventTrailCard(events = state.eventTrail) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard(background = Color(0xFF243B53), contentColor = Color.White) {
        Text(
            text = "第14章 Input 事件分发实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "观察触摸事件如何进入 Activity，又如何在父容器、子 View 和 Compose 手势之间流转。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE7EEF4)
        )
    }
}

@Composable
private fun ScoreCard(score: InputScore) {
    val total = listOf(
        score.activityObserved,
        score.childClickObserved,
        score.moveObserved,
        score.cancelObserved,
        score.composeGestureObserved,
        score.busyObserved
    ).count { it } * 100 / 6

    LabCard(background = Color(0xFFFFF7D6)) {
        SectionTitle(title = "输入观察分数")
        Text(
            text = "$total / 100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9A6B22)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(text = "Activity 收到触摸事件")
        BulletText(text = "子 View 完成一次点击")
        BulletText(text = "观察 MOVE 事件")
        BulletText(text = "触发 CANCEL 或拦截路径")
        BulletText(text = "触发 Compose 手势")
        BulletText(text = "模拟主线程忙碌")
    }
}

@Composable
private fun ExperimentCard(experiment: InputExperiment) {
    LabCard(background = Color(0xFFF7EEE7)) {
        SectionTitle(title = "预期 vs 实际")
        InfoRow(label = "本次操作", value = experiment.operation)
        Spacer(modifier = Modifier.height(6.dp))
        LabelText(label = "预期", text = experiment.expected, color = Color(0xFF9A6B22))
        LabelText(label = "实际", text = experiment.actual, color = Color(0xFF2F6F73))
        LabelText(label = "结论", text = experiment.conclusion, color = Color(0xFF4A5A89))
    }
}

@Composable
private fun NativeDispatchCard(interceptMove: Boolean) {
    LabCard(background = Color(0xFFEAF3F1)) {
        SectionTitle(title = "原生 View 分发实验区")
        Text(
            text = "打开 MOVE 拦截后，在黄色子 View 上滑动，观察 Parent.onInterceptTouchEvent 和 Child 收到的事件变化。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "父容器拦截 MOVE",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = interceptMove,
                onCheckedChange = { InputLabStore.setInterceptMove(it) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AndroidView(
            factory = { context -> TouchLoggingParentView(context) },
            update = { view -> view.interceptMove = interceptMove },
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun ComposeGestureCard(onMainThreadBusy: () -> Unit) {
    var dragDistance by remember { mutableFloatStateOf(0f) }

    LabCard(background = Color(0xFFEFF3FA)) {
        SectionTitle(title = "Compose 手势实验区")
        Text(
            text = "Compose 用 Modifier 描述手势，但它仍然运行在 Android 输入链路之上。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF243B53))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        InputLabStore.recordComposeGesture(
                            name = "pointerInput tap",
                            detail = "tap x=${offset.x.toInt()}, y=${offset.y.toInt()}"
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "点我：pointerInput tap",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFF7D6))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragDistance = 0f
                            InputLabStore.recordComposeGesture("drag start", "Compose drag started")
                        },
                        onDrag = { _, dragAmount ->
                            dragDistance += dragAmount.getDistance()
                            InputLabStore.recordComposeGesture(
                                name = "drag move",
                                detail = "dx=${dragAmount.x.toInt()}, dy=${dragAmount.y.toInt()}, total=${dragDistance.toInt()}"
                            )
                        },
                        onDragEnd = {
                            InputLabStore.recordComposeGesture("drag end", "totalDistance=${dragDistance.toInt()}")
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "拖我：观察 Compose drag",
                color = Color(0xFF1D2B27),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                InputLabStore.recordComposeGesture("clickable", "Button onClick fired")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Compose Button 点击")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onMainThreadBusy, modifier = Modifier.fillMaxWidth()) {
            Text(text = "模拟主线程忙碌 180ms")
        }
    }
}

@Composable
private fun DiagnosticCard(cards: List<InputDiagnosticCard>) {
    LabCard(background = Color(0xFFF1EFE8)) {
        SectionTitle(title = "输入问题诊断卡")
        cards.forEach { card ->
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A6B22)
            )
            Text(text = card.symptom, style = MaterialTheme.typography.bodySmall)
            Text(text = "可能原因：${card.likelyCause}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            Text(text = "处理方向：${card.fixDirection}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2F6F73))
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EventTrailCard(events: List<InputEventLog>) {
    LabCard {
        SectionTitle(title = "事件序列与分发轨迹")
        if (events.isEmpty()) {
            Text(
                text = "暂无事件。点击、滑动原生 View 或 Compose 手势区后，这里会显示 DOWN / MOVE / UP / CANCEL 和分发路径。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF66736F)
            )
        } else {
            events.forEach { event ->
                Text(
                    text = "${event.timestamp}  ${event.source}.${event.phase}  ${event.action}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = event.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { InputLabStore.clearEvents() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "清空事件")
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
private fun LabelText(label: String, text: String, color: Color) {
    Text(text = label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    Text(text = text, style = MaterialTheme.typography.bodySmall)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4D4538))
    }
}

@Preview(showBackground = true)
@Composable
private fun InputLabScreenPreview() {
    MaterialTheme {
        InputLabScreen(
            state = InputLabState(
                interceptMove = true,
                currentExperiment = InputExperiment(
                    operation = "开启父容器 MOVE 拦截",
                    expected = "父容器会在 MOVE 阶段尝试拦截。",
                    actual = "Parent.onInterceptTouchEvent 收到 MOVE。",
                    conclusion = "父容器正在接管这段手势。"
                ),
                score = InputScore(
                    activityObserved = true,
                    childClickObserved = true,
                    moveObserved = true,
                    cancelObserved = true,
                    composeGestureObserved = true,
                    busyObserved = false
                ),
                eventTrail = listOf(
                    InputEventLog("Activity", "dispatchTouchEvent", "DOWN", "x=100, y=200", "12:30:01.100")
                )
            ),
            onMainThreadBusy = {}
        )
    }
}
