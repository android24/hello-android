package com.helloandroid.architecture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import com.helloandroid.architecture.R
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.CourseLesson
import com.helloandroid.architecture.common.CourseListUiState

@Composable
fun ArchitectureHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.home_title),
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = colorResource(id = R.color.text_primary),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = R.string.home_subtitle),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun ArchitectureModeSelector(
    selectedMode: ArchitectureMode,
    onModeSelected: (ArchitectureMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArchitectureMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            val onClick = { onModeSelected(mode) }

            if (selected) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = mode.label)
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = mode.label)
                }
            }
        }
    }
}

@Composable
fun CourseArchitecturePanel(
    mode: ArchitectureMode,
    state: CourseListUiState,
    boundaryNote: String,
    onRefresh: () -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.mode_background),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = mode.label,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = colorResource(id = R.color.brand_indigo),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = mode.summary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.text_primary)
                )

                Text(
                    text = boundaryNote,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        }

        PreferenceAndActionRow(
            isLoading = state.isLoading,
            showCompleted = state.showCompleted,
            onRefresh = onRefresh,
            onShowCompletedChange = onShowCompletedChange
        )

        Text(
            text = state.message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.brand_teal)
        )

        if (state.visibleLessons.isEmpty()) {
            EmptyLessonsCard()
        } else {
            state.visibleLessons.forEach { lesson ->
                LessonCard(lesson = lesson)
            }
        }
    }
}

@Composable
fun PreferenceAndActionRow(
    isLoading: Boolean,
    showCompleted: Boolean,
    onRefresh: () -> Unit,
    onShowCompletedChange: (Boolean) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.show_completed),
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )

                Switch(
                    checked = showCompleted,
                    onCheckedChange = onShowCompletedChange
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onRefresh,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isLoading) "刷新中..." else stringResource(id = R.string.refresh))
            }
        }
    }
}

@Composable
fun LessonCard(lesson: CourseLesson) {
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
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = lesson.owner,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = colorResource(id = R.color.brand_indigo),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = lesson.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_secondary)
            )

            Text(
                text = if (lesson.isCompleted) "状态：已完成" else "状态：学习中",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.brand_teal)
            )
        }
    }
}

@Composable
fun EmptyLessonsCard() {
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
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseArchitecturePanelPreview() {
    CourseArchitecturePanel(
        mode = ArchitectureMode.MVVM,
        state = CourseListUiState(
            lessons = listOf(
                CourseLesson(
                    id = 1,
                    title = "MVVM 示例",
                    description = "ViewModel 持有状态，UI 只负责展示。",
                    owner = "MVVM",
                    isCompleted = false
                )
            ),
            message = "Preview 状态"
        ),
        boundaryNote = "Preview 中展示通用课程列表面板。",
        onRefresh = {},
        onShowCompletedChange = {}
    )
}
