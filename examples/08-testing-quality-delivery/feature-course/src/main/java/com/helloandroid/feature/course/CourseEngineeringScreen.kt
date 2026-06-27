package com.helloandroid.feature.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty

@Composable
fun CourseEngineeringScreen(
    uiState: CourseEngineeringUiState,
    onRefresh: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HeaderCard(
                    uiState = uiState,
                    onRefresh = onRefresh
                )
                EngineeringMapCard()
                InjectionChainCard()
                LessonsCard(lessons = uiState.dashboard?.lessons.orEmpty())
            }
        }
    }
}

@Composable
private fun HeaderCard(
    uiState: CourseEngineeringUiState,
    onRefresh: () -> Unit
) {
    val dashboard = uiState.dashboard

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "第 8 章质量保障演练",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "从工程化继续前进，用测试、Fake、CI 和发布清单保护关键路径。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoPill(
                    label = "环境",
                    value = dashboard?.networkConfig?.badge ?: "loading",
                    modifier = Modifier.weight(1f)
                )
                InfoPill(
                    label = "同步",
                    value = "${dashboard?.syncedCount ?: 0}/${dashboard?.lessons?.size ?: 0}",
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = dashboard?.networkConfig?.baseUrl ?: "等待 app 模块提供 NetworkConfig",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0E7490),
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onRefresh,
                enabled = !uiState.isRefreshing
            ) {
                Text(if (uiState.isRefreshing) "刷新中..." else "通过 Hilt 刷新课程")
            }
        }
    }
}

@Composable
private fun InfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFECFEFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF0E7490),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF164E63),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EngineeringMapCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("测试金字塔")
            CodeLine("UseCase 单元测试：保护业务规则")
            CodeLine("Repository 测试：保护数据组合与缓存规则")
            CodeLine("ViewModel 测试：保护 UI State 转换")
            CodeLine("Compose UI 测试：保护关键用户路径")
        }
    }
}

@Composable
private fun InjectionChainCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("被测试的 Hilt 注入链路")
            CodeLine("CourseEngineeringViewModel")
            CodeLine("  -> GetCourseDashboardUseCase")
            CodeLine("      -> CourseRepository 接口")
            CodeLine("          -> CourseRepositoryImpl")
            CodeLine("              -> CourseRemoteDataSource")
            CodeLine("              -> CourseLocalDataSource")
        }
    }
}

@Composable
private fun LessonsCard(lessons: List<CourseLesson>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle("课程列表")
            lessons.forEach { lesson ->
                LessonRow(lesson)
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: CourseLesson) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = lesson.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = lesson.difficulty.label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4338CA),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = lesson.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (lesson.isSynced) "已同步" else "等待同步",
            style = MaterialTheme.typography.labelMedium,
            color = if (lesson.isSynced) Color(0xFF047857) else Color(0xFFB45309)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
    )
}

@Composable
private fun CodeLine(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF334155)
    )
}

private val LessonDifficulty.label: String
    get() = when (this) {
        LessonDifficulty.Beginner -> "基础"
        LessonDifficulty.Intermediate -> "进阶"
        LessonDifficulty.Advanced -> "工程"
    }
