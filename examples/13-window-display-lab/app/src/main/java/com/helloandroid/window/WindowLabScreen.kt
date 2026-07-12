package com.helloandroid.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WindowDisplayLabApp(
    onRefreshWindowInfo: () -> Unit,
    onShowToast: () -> Unit,
    onRequestFrame: () -> Unit,
    onMainThreadBusy: () -> Unit
) {
    val state by WindowLabStore.state.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2F6F73),
            secondary = Color(0xFF9A6B22),
            tertiary = Color(0xFF4A5A89),
            background = Color(0xFFF7F8F5),
            surface = Color.White
        )
    ) {
        WindowLabScreen(
            state = state,
            onRefreshWindowInfo = onRefreshWindowInfo,
            onShowToast = onShowToast,
            onRequestFrame = onRequestFrame,
            onMainThreadBusy = onMainThreadBusy
        )
    }
}

@Composable
fun WindowLabScreen(
    state: WindowLabState,
    onRefreshWindowInfo: () -> Unit,
    onShowToast: () -> Unit,
    onRequestFrame: () -> Unit,
    onMainThreadBusy: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var colorToggle by remember { mutableStateOf(false) }
    var longText by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Dialog 是附属窗口") },
            text = {
                Text(text = "它盖在 Activity 主窗口之上，也依赖宿主 Activity 的有效 Token。退出页面前，要记得让它跟随生命周期释放。")
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "知道了")
                }
            }
        )
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 18.dp)
                .onGloballyPositioned {
                    WindowLabStore.recordContentSize(it.size.width, it.size.height)
                },
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeaderCard() }
            item { ScoreCard(score = state.score) }
            item { WindowInfoCard(info = state.windowInfo, onRefreshWindowInfo = onRefreshWindowInfo) }
            item { ExperimentCard(experiment = state.currentExperiment) }
            item {
                WindowActionCard(
                    showPopup = showPopup,
                    onShowDialog = {
                        WindowLabStore.recordDialogShown()
                        showDialog = true
                    },
                    onShowPopup = {
                        WindowLabStore.recordPopupShown()
                        showPopup = true
                    },
                    onDismissPopup = { showPopup = false },
                    onShowToast = onShowToast,
                    onFocusInput = {
                        WindowLabStore.recordKeyboardRequested()
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                )
            }
            item {
                InputMethodCard(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    focusRequester = focusRequester
                )
            }
            item {
                FrameExperimentCard(
                    colorToggle = colorToggle,
                    longText = longText,
                    onToggleColor = {
                        colorToggle = !colorToggle
                        WindowLabStore.recordColorInvalidate()
                        onRequestFrame()
                    },
                    onToggleText = {
                        longText = !longText
                        WindowLabStore.recordTextRelayout()
                        onRequestFrame()
                    },
                    onRequestFrame = onRequestFrame,
                    onMainThreadBusy = onMainThreadBusy
                )
            }
            item { DiagnosticCard(cards = state.diagnosticCards) }
            item { EventTraceCard(events = state.events) }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard(background = Color(0xFF243B53), contentColor = Color.White) {
        Text(
            text = "第13章 Window 显示链路实验室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "观察 Activity 内容如何进入 Window、DecorView、ViewRootImpl 与 WMS 的世界。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE7EEF4)
        )
    }
}

@Composable
private fun ScoreCard(score: WindowScore) {
    val total = listOf(
        score.decorObserved,
        score.dialogObserved,
        score.popupObserved,
        score.keyboardObserved,
        score.frameObserved
    ).count { it } * 20

    LabCard(background = Color(0xFFFFF7D6)) {
        SectionTitle(title = "窗口观察分数")
        Text(
            text = "$total / 100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9A6B22)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(text = "20 分：读取 Window / DecorView")
        BulletText(text = "20 分：显示 Dialog")
        BulletText(text = "20 分：显示 Popup")
        BulletText(text = "20 分：请求输入法")
        BulletText(text = "20 分：触发一帧刷新实验")
    }
}

@Composable
private fun WindowInfoCard(info: WindowInfo, onRefreshWindowInfo: () -> Unit) {
    LabCard {
        SectionTitle(title = "窗口身份证")
        InfoRow(label = "Activity", value = info.activityName)
        InfoRow(label = "Window", value = info.windowClass)
        InfoRow(label = "DecorView", value = info.decorViewClass)
        InfoRow(label = "DecorView 尺寸", value = info.decorSize)
        InfoRow(label = "内容区尺寸", value = info.contentSize)
        InfoRow(label = "内容根节点", value = info.rootViewClass)
        InfoRow(label = "软键盘策略", value = info.softInputMode)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onRefreshWindowInfo, modifier = Modifier.fillMaxWidth()) {
            Text(text = "刷新窗口信息")
        }
    }
}

@Composable
private fun ExperimentCard(experiment: WindowExperiment) {
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
private fun WindowActionCard(
    showPopup: Boolean,
    onShowDialog: () -> Unit,
    onShowPopup: () -> Unit,
    onDismissPopup: () -> Unit,
    onShowToast: () -> Unit,
    onFocusInput: () -> Unit
) {
    LabCard(background = Color(0xFFEAF3F1)) {
        SectionTitle(title = "特殊窗口操作台")
        Button(onClick = onShowDialog, modifier = Modifier.fillMaxWidth()) {
            Text(text = "显示 Dialog")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = onShowPopup, modifier = Modifier.fillMaxWidth()) {
                Text(text = "显示 Popup")
            }
            DropdownMenu(expanded = showPopup, onDismissRequest = onDismissPopup) {
                DropdownMenuItem(
                    text = { Text(text = "Popup 依附当前按钮区域") },
                    onClick = onDismissPopup
                )
                DropdownMenuItem(
                    text = { Text(text = "观察它和 Dialog 的差异") },
                    onClick = onDismissPopup
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onShowToast, modifier = Modifier.fillMaxWidth()) {
            Text(text = "显示 Toast")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onFocusInput, modifier = Modifier.fillMaxWidth()) {
            Text(text = "聚焦输入框并请求输入法")
        }
    }
}

@Composable
private fun InputMethodCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    LabCard {
        SectionTitle(title = "输入法与 Insets 观察")
        Text(
            text = "点击按钮或输入框，观察软键盘出现后内容区域是否被压缩、遮挡或滚动。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF66736F)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text(text = "输入一点文字") },
            keyboardActions = KeyboardActions(onDone = {
                WindowLabStore.recordKeyboardRequested()
            })
        )
    }
}

@Composable
private fun FrameExperimentCard(
    colorToggle: Boolean,
    longText: Boolean,
    onToggleColor: () -> Unit,
    onToggleText: () -> Unit,
    onRequestFrame: () -> Unit,
    onMainThreadBusy: () -> Unit
) {
    val blockColor = if (colorToggle) Color(0xFF4A5A89) else Color(0xFFF4D35E)
    val sampleText = if (longText) {
        "这是一段故意变长的文本，用来观察内容尺寸、换行和布局高度是否发生变化。它更适合理解 requestLayout、measure 和 layout。"
    } else {
        "短文本：观察 draw。"
    }

    LabCard(background = Color(0xFFEFF3FA)) {
        SectionTitle(title = "一帧刷新实验")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(blockColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = sampleText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onToggleColor, modifier = Modifier.fillMaxWidth()) {
            Text(text = "改变颜色：观察重绘")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onToggleText, modifier = Modifier.fillMaxWidth()) {
            Text(text = "改变文本长度：观察重新布局")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onRequestFrame, modifier = Modifier.fillMaxWidth()) {
            Text(text = "记录下一帧 Choreographer")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onMainThreadBusy, modifier = Modifier.fillMaxWidth()) {
            Text(text = "模拟主线程忙碌 180ms")
        }
    }
}

@Composable
private fun DiagnosticCard(cards: List<WindowDiagnosticCard>) {
    LabCard(background = Color(0xFFF1EFE8)) {
        SectionTitle(title = "窗口问题诊断卡")
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
private fun EventTraceCard(events: List<WindowEvent>) {
    LabCard {
        SectionTitle(title = "窗口事件轨迹")
        if (events.isEmpty()) {
            Text(
                text = "暂无事件。点击上面的实验按钮后，这里会记录窗口、弹层、输入法和刷新事件。",
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
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { WindowLabStore.clearEvents() }, modifier = Modifier.fillMaxWidth()) {
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
        Text(text = "-", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4D4538))
    }
}

@Preview(showBackground = true)
@Composable
private fun WindowLabScreenPreview() {
    MaterialTheme {
        WindowLabScreen(
            state = WindowLabState(
                windowInfo = WindowInfo(
                    windowClass = "PhoneWindow",
                    decorViewClass = "DecorView",
                    decorSize = "1080x2200",
                    contentSize = "1080x1860"
                ),
                currentExperiment = WindowExperiment(
                    operation = "显示 Dialog",
                    expected = "Dialog 作为附属窗口盖在 Activity 上。",
                    actual = "AlertDialog 已显示。",
                    conclusion = "这是 Window 层级。"
                ),
                score = WindowScore(
                    decorObserved = true,
                    dialogObserved = true,
                    popupObserved = true,
                    keyboardObserved = false,
                    frameObserved = true
                ),
                events = listOf(
                    WindowEvent("WindowInfo", "window=PhoneWindow, decor=DecorView", "12:30:01.100")
                )
            ),
            onRefreshWindowInfo = {},
            onShowToast = {},
            onRequestFrame = {},
            onMainThreadBusy = {}
        )
    }
}
