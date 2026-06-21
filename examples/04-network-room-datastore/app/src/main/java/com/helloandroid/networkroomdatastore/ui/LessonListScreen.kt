package com.helloandroid.networkroomdatastore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helloandroid.networkroomdatastore.R

@Composable
fun HelloNetworkRoomDatastoreApp(viewModel: LessonListViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    HelloNetworkRoomDatastoreApp(
        uiState = uiState.value,
        onRefresh = { viewModel.refreshLessons() },
        onFailRefresh = { viewModel.refreshLessons(shouldFail = true) },
        onShowCompletedChange = viewModel::setShowCompleted
    )
}

@Composable
fun HelloNetworkRoomDatastoreApp(
    uiState: LessonListUiState,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            LessonListScreen(
                uiState = uiState,
                onRefresh = onRefresh,
                onFailRefresh = onFailRefresh,
                onShowCompletedChange = onShowCompletedChange
            )
        }
    }
}

@Composable
fun LessonListScreen(
    uiState: LessonListUiState,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LessonHeader()

        SyncStatusCard(uiState = uiState)

        PreferenceRow(
            showCompleted = uiState.showCompleted,
            onShowCompletedChange = onShowCompletedChange
        )

        ActionRow(
            isLoading = uiState.isLoading,
            onRefresh = onRefresh,
            onFailRefresh = onFailRefresh
        )

        uiState.errorMessage?.let { message ->
            ErrorMessage(message = message)
        }

        if (uiState.lessons.isEmpty()) {
            EmptyMessage()
        } else {
            LessonList(lessons = uiState.lessons)
        }
    }
}

@Composable
fun LessonHeader() {
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
fun SyncStatusCard(uiState: LessonListUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.status_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "当前显示：${uiState.lessons.size} 节",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = R.color.brand_teal),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = uiState.lastSyncText,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_primary)
            )

            if (uiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 2.dp),
                        color = colorResource(id = R.color.brand_teal),
                        strokeWidth = 3.dp
                    )

                    Text(
                        text = stringResource(id = R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceRow(
    showCompleted: Boolean,
    onShowCompletedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.show_completed),
                    style = MaterialTheme.typography.titleSmall,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "这个开关会写入 DataStore，重启后仍然保留。",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.text_secondary)
                )
            }

            Switch(
                checked = showCompleted,
                onCheckedChange = onShowCompletedChange
            )
        }
    }
}

@Composable
fun ActionRow(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onFailRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            onClick = onRefresh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.refresh_lessons))
        }

        OutlinedButton(
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            onClick = onFailRefresh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.refresh_failure))
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.error_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.error_text)
        )
    }
}

@Composable
fun EmptyMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.empty_lessons),
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun LessonList(lessons: List<LessonUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lessons.forEach { lesson ->
            LessonCard(lesson = lesson)
        }
    }
}

@Composable
fun LessonCard(lesson: LessonUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = lesson.level,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorResource(id = R.color.brand_blue),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_secondary)
            )

            Text(
                text = if (lesson.isCompleted) "状态：已完成" else "状态：学习中",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.brand_teal)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessonListScreenPreview() {
    HelloNetworkRoomDatastoreApp(
        uiState = LessonListUiState(
            lessons = listOf(
                LessonUiModel(
                    id = 1,
                    title = "Room 本地数据库",
                    description = "用 Entity、Dao、Database 保存结构化缓存。",
                    level = "核心",
                    isCompleted = false
                )
            ),
            lastSyncText = "最近同步：10:30:12"
        ),
        onRefresh = {},
        onFailRefresh = {},
        onShowCompletedChange = {}
    )
}
