package com.helloandroid.async.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helloandroid.async.R
import com.helloandroid.async.data.model.SyncLesson

@Composable
fun SyncCenterApp(viewModel: SyncCenterViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var eventText by remember { mutableStateOf("SharedFlow：等待一次性事件") }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SyncEvent.ShowMessage -> eventText = "SharedFlow：${event.text}"
            }
        }
    }

    SyncCenterApp(
        uiState = uiState.value,
        eventText = eventText,
        onRunThread = viewModel::runThreadDemo,
        onRunExecutor = viewModel::runExecutorDemo,
        onRefresh = { viewModel.refreshLessons() },
        onFailRefresh = { viewModel.refreshLessons(shouldFail = true) },
        onWifiOnlyChange = viewModel::setWifiOnly,
        onStartWork = viewModel::startBackgroundSync
    )
}

@Composable
fun SyncCenterApp(
    uiState: SyncCenterUiState,
    eventText: String,
    onRunThread: () -> Unit,
    onRunExecutor: () -> Unit,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onStartWork: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            SyncCenterScreen(
                uiState = uiState,
                eventText = eventText,
                onRunThread = onRunThread,
                onRunExecutor = onRunExecutor,
                onRefresh = onRefresh,
                onFailRefresh = onFailRefresh,
                onWifiOnlyChange = onWifiOnlyChange,
                onStartWork = onStartWork
            )
        }
    }
}

@Composable
fun SyncCenterScreen(
    uiState: SyncCenterUiState,
    eventText: String,
    onRunThread: () -> Unit,
    onRunExecutor: () -> Unit,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onStartWork: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()
        StatusPanel(uiState = uiState, eventText = eventText)
        LegacyAsyncPanel(
            threadLog = uiState.threadLog,
            executorLog = uiState.executorLog,
            onRunThread = onRunThread,
            onRunExecutor = onRunExecutor
        )
        CoroutineAndFlowPanel(
            uiState = uiState,
            onRefresh = onRefresh,
            onFailRefresh = onFailRefresh,
            onWifiOnlyChange = onWifiOnlyChange
        )
        WorkManagerPanel(
            workStateText = uiState.workStateText,
            onStartWork = onStartWork
        )
        LessonList(lessons = uiState.lessons)
    }
}

@Composable
fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colorResource(id = R.color.text_primary),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun StatusPanel(uiState: SyncCenterUiState, eventText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoBox(
            title = "StateFlow",
            body = "课程：${uiState.lessons.size} 节，${uiState.lastSyncText}"
        )
        InfoBox(
            title = "SharedFlow",
            body = eventText
        )
    }
}

@Composable
fun LegacyAsyncPanel(
    threadLog: String,
    executorLog: String,
    onRunThread: () -> Unit,
    onRunExecutor: () -> Unit
) {
    SectionCard(title = "Java 旧世界：Thread 与线程池") {
        Text(
            text = "它们能把任务放到后台，但切回主线程、取消和生命周期都需要自己照看。",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_secondary)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRunThread,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.run_thread))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRunExecutor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.run_executor))
        }
        LogBox(text = threadLog)
        LogBox(text = executorLog)
    }
}

@Composable
fun CoroutineAndFlowPanel(
    uiState: SyncCenterUiState,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit
) {
    SectionCard(title = "Kotlin 新工具：协程与 Flow") {
        Text(
            text = "ViewModel 启动协程，Repository 刷新数据，Flow 持续把缓存和配置推到页面。",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_secondary)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.wifi_only),
                style = MaterialTheme.typography.titleSmall,
                color = colorResource(id = R.color.text_primary),
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = uiState.wifiOnly,
                onCheckedChange = onWifiOnlyChange
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isRefreshing,
            onClick = onRefresh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = if (uiState.isRefreshing) "刷新中..." else stringResource(id = R.string.refresh_courses))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isRefreshing,
            onClick = onFailRefresh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.fail_refresh))
        }
    }
}

@Composable
fun WorkManagerPanel(
    workStateText: String,
    onStartWork: () -> Unit
) {
    SectionCard(title = "系统托管：WorkManager") {
        Text(
            text = "不急着立刻完成、但希望可靠执行的同步任务，交给 WorkManager 更合适。",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_secondary)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartWork,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.start_work))
        }
        LogBox(text = workStateText)
    }
}

@Composable
fun LessonList(lessons: List<SyncLesson>) {
    SectionCard(title = "课程缓存") {
        if (lessons.isEmpty()) {
            Text(
                text = stringResource(id = R.string.empty_lessons),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_secondary)
            )
        } else {
            lessons.forEach { lesson ->
                LessonCard(lesson = lesson)
            }
        }
    }
}

@Composable
fun LessonCard(lesson: SyncLesson) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = lesson.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorResource(id = R.color.brand_indigo),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.text_secondary)
            )
            Text(
                text = if (lesson.isSynced) "状态：已同步" else "状态：等待同步",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.brand_cyan)
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable Column.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = R.color.text_primary),
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun InfoBox(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.panel_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorResource(id = R.color.brand_cyan),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}

@Composable
fun LogBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.event_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SyncCenterScreenPreview() {
    SyncCenterApp(
        uiState = SyncCenterUiState(
            lessons = listOf(
                SyncLesson(
                    id = 1,
                    title = "Coroutines",
                    description = "用顺序代码表达异步任务。",
                    source = "Coroutine",
                    isSynced = true
                )
            ),
            lastSyncText = "最近同步：10:30:20"
        ),
        eventText = "SharedFlow：预览事件",
        onRunThread = {},
        onRunExecutor = {},
        onRefresh = {},
        onFailRefresh = {},
        onWifiOnlyChange = {},
        onStartWork = {}
    )
}
