package com.helloandroid.rendering

import android.view.Choreographer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RenderingFrameLabApp(
    onMainThreadBusy: () -> Unit
) {
    val state by RenderingLabStore.state.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF315F72),
            secondary = Color(0xFF8A6A25),
            tertiary = Color(0xFF5D6B4C),
            background = Color(0xFFF7F8F6),
            surface = Color.White
        )
    ) {
        FrameSampler(enabled = state.frameStats.capturing)
        RenderingLabScreen(state = state, onMainThreadBusy = onMainThreadBusy)
    }
}

@Composable
private fun FrameSampler(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (!enabled) {
            onDispose { }
        } else {
            val choreographer = Choreographer.getInstance()
            var lastFrameTime = 0L
            val callback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    if (lastFrameTime != 0L) {
                        RenderingLabStore.recordFrame((frameTimeNanos - lastFrameTime) / 1_000_000f)
                    }
                    lastFrameTime = frameTimeNanos
                    if (RenderingLabStore.state.value.frameStats.capturing) {
                        choreographer.postFrameCallback(this)
                    }
                }
            }
            choreographer.postFrameCallback(callback)
            onDispose { choreographer.removeFrameCallback(callback) }
        }
    }
}

@Composable
fun RenderingLabScreen(
    state: RenderingLabState,
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
            item { RenderLinkCard(links = state.renderLinks) }
            item { RefreshProbeCard(probeState = state.probeState) }
            item { FrameClockCard(frameStats = state.frameStats, onMainThreadBusy = onMainThreadBusy) }
            item { ScrollAndAnimationCard() }
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
            text = "第15章 一帧渲染链路实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "观察状态变化如何触发刷新请求、帧调度、View 树遍历、渲染提交和系统合成边界。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE7EEF4)
        )
    }
}

@Composable
private fun ScoreCard(score: RenderingScore) {
    val total = listOf(
        score.invalidateObserved,
        score.layoutObserved,
        score.drawObserved,
        score.frameObserved,
        score.busyObserved,
        score.scrollObserved,
        score.animationObserved,
        score.diagnosisObserved
    ).count { it } * 100 / 8

    LabCard(background = Color(0xFFFFF4D8)) {
        SectionTitle(title = "渲染观察分数")
        Text(
            text = "$total / 100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A6A25)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(text = "触发 invalidate")
        BulletText(text = "观察 measure / layout")
        BulletText(text = "观察 draw")
        BulletText(text = "采样 Choreographer frame")
        BulletText(text = "模拟主线程忙碌")
        BulletText(text = "滚动列表")
        BulletText(text = "执行动画实验")
        BulletText(text = "阅读诊断卡")
    }
}

@Composable
private fun ExperimentCard(experiment: RenderingExperiment) {
    LabCard(background = Color(0xFFF3EDE6)) {
        SectionTitle(title = "预期 vs 实际")
        InfoRow(label = "本次操作", value = experiment.operation)
        Spacer(modifier = Modifier.height(6.dp))
        LabelText(label = "预期", text = experiment.expected, color = Color(0xFF8A6A25))
        LabelText(label = "实际", text = experiment.actual, color = Color(0xFF315F72))
        LabelText(label = "结论", text = experiment.conclusion, color = Color(0xFF5D6B4C))
    }
}

@Composable
private fun RenderLinkCard(links: List<RenderLink>) {
    LabCard(background = Color(0xFFF8FAFC)) {
        SectionTitle(title = "一帧链路卡片")
        Text(
            text = "先看摘要，再读日志：这张卡把刷新请求、View 树、帧调度、渲染提交和系统合成边界放在一起。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        links.forEachIndexed { index, link ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (link.latestSignal == "等待") Color(0xFFE5E7EB) else Color(0xFFD6E7EF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D2B27)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = link.layer,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = link.latestSignal,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF315F72),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = link.role, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
                    Text(text = link.evidence, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (index != links.lastIndex) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RefreshProbeCard(probeState: ProbeState) {
    LabCard(background = Color(0xFFEAF2F4)) {
        SectionTitle(title = "刷新请求实验区")
        Text(
            text = "颜色变化更像 invalidate；文本和尺寸变化更容易触发 requestLayout。观察下方 View 探针日志。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        AndroidView(
            factory = { context -> RenderProbeView(context) },
            update = { view -> view.applyState(probeState) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { RenderingLabStore.changeColor() }, modifier = Modifier.weight(1f)) {
                Text(text = "改变颜色")
            }
            Button(onClick = { RenderingLabStore.toggleLongText() }, modifier = Modifier.weight(1f)) {
                Text(text = "切换文案")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { RenderingLabStore.toggleLargeSize() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "切换 View 尺寸")
        }
    }
}

@Composable
private fun FrameClockCard(
    frameStats: FrameStats,
    onMainThreadBusy: () -> Unit
) {
    LabCard(background = Color(0xFFEFF4EA)) {
        SectionTitle(title = "Choreographer 帧节拍器")
        InfoRow(label = "采样状态", value = if (frameStats.capturing) "采样中" else "等待")
        InfoRow(label = "样本数", value = "${frameStats.samples} / 18")
        InfoRow(label = "最近帧间隔", value = "%.2f ms".format(frameStats.lastFrameMs))
        InfoRow(label = "最大帧间隔", value = "%.2f ms".format(frameStats.maxFrameMs))
        InfoRow(label = "慢帧数量", value = "${frameStats.slowFrames}")
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { RenderingLabStore.startFrameCapture() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "采样 18 帧")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onMainThreadBusy, modifier = Modifier.fillMaxWidth()) {
            Text(text = "模拟主线程忙碌 140ms")
        }
    }
}

@Composable
private fun ScrollAndAnimationCard() {
    var moved by remember { mutableStateOf(false) }
    var largeBox by remember { mutableStateOf(false) }
    val offset by animateFloatAsState(
        targetValue = if (moved) 96f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "transform-offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (moved) 0.55f else 1f,
        animationSpec = tween(durationMillis = 420),
        label = "transform-alpha"
    )
    val listState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex > 0) {
            RenderingLabStore.recordScroll(listState.firstVisibleItemIndex)
        }
    }

    LabCard(background = Color(0xFFF1EEF7)) {
        SectionTitle(title = "滚动与动画实验区")
        Text(
            text = "对比 transform 动画和尺寸变化，再滚动列表观察连续帧压力。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.toInt(), 16) }
                    .alpha(alpha)
                    .size(if (largeBox) 56.dp else 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF315F72))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    moved = !moved
                    RenderingLabStore.recordAnimation("transform", "translation / alpha")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "transform")
            }
            Button(
                onClick = {
                    largeBox = !largeBox
                    RenderingLabStore.recordAnimation("size", "box size changed")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "尺寸动画")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items((1..18).toList()) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color(0xFFF8FAFC) else Color(0xFFF1EEF7))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#$index",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF315F72),
                        modifier = Modifier.width(42.dp)
                    )
                    Text(
                        text = if (index % 3 == 0) "复杂 item 会吃掉更多帧预算" else "滚动时观察 frame 与事件轨迹",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1D2B27)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCard(cards: List<RenderingDiagnosticCard>) {
    LabCard(background = Color(0xFFF1EFE8)) {
        SectionTitle(title = "渲染问题诊断卡")
        cards.forEach { card ->
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A6A25)
            )
            Text(text = card.symptom, style = MaterialTheme.typography.bodySmall)
            Text(text = "第一证据：${card.firstEvidence}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
            Text(text = "处理方向：${card.fixDirection}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF315F72))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = { RenderingLabStore.markDiagnosisRead(card.title) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "标记已阅读：${card.title}")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EventTrailCard(events: List<RenderingEventLog>) {
    LabCard {
        SectionTitle(title = "一帧事件轨迹")
        if (events.isEmpty()) {
            Text(
                text = "暂无事件。触发颜色、文本、尺寸、帧采样、滚动或动画后，这里会记录刷新请求与帧证据。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF66736F)
            )
        } else {
            events.forEach { event ->
                Text(
                    text = "${event.timestamp}  ${event.source}.${event.phase}  ${event.signal}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = event.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF66736F))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { RenderingLabStore.clearEvents() }, modifier = Modifier.fillMaxWidth()) {
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
private fun RenderingLabScreenPreview() {
    MaterialTheme {
        RenderingLabScreen(
            state = RenderingLabState(
                probeState = ProbeState(colorIndex = 1, longText = true, largeSize = true),
                frameStats = FrameStats(samples = 8, lastFrameMs = 16.4f, maxFrameMs = 24.2f, slowFrames = 1),
                score = RenderingScore(
                    invalidateObserved = true,
                    layoutObserved = true,
                    drawObserved = true,
                    frameObserved = true,
                    busyObserved = false,
                    scrollObserved = true,
                    animationObserved = true,
                    diagnosisObserved = false
                ),
                eventTrail = listOf(
                    RenderingEventLog("FrameClock", "doFrame", "FRAME", "interval=16.40ms", "12:30:01.100")
                )
            ),
            onMainThreadBusy = {}
        )
    }
}
