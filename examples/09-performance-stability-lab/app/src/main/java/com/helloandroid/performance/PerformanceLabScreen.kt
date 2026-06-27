package com.helloandroid.performance

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PerformanceLabApp(
    viewModel: PerformanceLabViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PerformanceLabScreen(
        uiState = uiState,
        onMainThreadJank = viewModel::simulateMainThreadJank,
        onBackgroundCpuWork = viewModel::runBackgroundCpuWork,
        onAllocateMemory = viewModel::allocateMemoryChunk,
        onReleaseMemory = viewModel::releaseMemory,
        onCaptureCrash = viewModel::captureHandledCrash,
        onAnrRisk = viewModel::simulateAnrRisk
    )
}

@Composable
fun PerformanceLabScreen(
    uiState: PerformanceLabState,
    onMainThreadJank: () -> Unit,
    onBackgroundCpuWork: () -> Unit,
    onAllocateMemory: () -> Unit,
    onReleaseMemory: () -> Unit,
    onCaptureCrash: () -> Unit,
    onAnrRisk: () -> Unit
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
                HeaderCard(uiState.startupReport)
                StartupCard(uiState.startupReport)
                JankCard(
                    report = uiState.mainThreadReport,
                    onMainThreadJank = onMainThreadJank,
                    onAnrRisk = onAnrRisk
                )
                CpuCard(
                    report = uiState.cpuReport,
                    onBackgroundCpuWork = onBackgroundCpuWork
                )
                MemoryCard(
                    report = uiState.memoryReport,
                    onAllocateMemory = onAllocateMemory,
                    onReleaseMemory = onReleaseMemory
                )
                StabilityCard(
                    report = uiState.stabilityReport,
                    onCaptureCrash = onCaptureCrash
                )
                ComposeListCard()
                ChecklistCard(uiState.checklist)
            }
        }
    }
}

@Composable
private fun HeaderCard(startupReport: StartupReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "第 9 章性能与稳定性实验室",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "用可观察的小实验理解启动、卡顿、内存、ANR、崩溃和包体积治理。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoPill(
                    label = "构建",
                    value = startupReport.buildProfile,
                    modifier = Modifier.weight(1f)
                )
                InfoPill(
                    label = "日志",
                    value = if (startupReport.verboseLogEnabled) "verbose" else "release-safe",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StartupCard(report: StartupReport) {
    LabCard(
        title = "启动体检",
        description = "先记录关键节点，再谈优化。不要一上来就凭感觉删代码。",
        result = report.summary
    ) {
        CodeLine("Application.onCreate -> MainActivity.onCreate -> 首屏 Composable")
        CodeLine("重点观察：Application 重活、首屏 IO、同步初始化、冷启动链路")
    }
}

@Composable
private fun JankCard(
    report: String,
    onMainThreadJank: () -> Unit,
    onAnrRisk: () -> Unit
) {
    LabCard(
        title = "卡顿与 ANR 风险",
        description = "主线程阻塞会带来掉帧；长时间等待可能进一步演变为 ANR。",
        result = report
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onMainThreadJank) {
                Text("模拟 180ms 卡顿")
            }
            OutlinedButton(onClick = onAnrRisk) {
                Text("模拟 ANR 风险")
            }
        }
    }
}

@Composable
private fun CpuCard(
    report: String,
    onBackgroundCpuWork: () -> Unit
) {
    LabCard(
        title = "后台 CPU 实验",
        description = "同样是重活，放到后台线程后，页面响应会更稳定。",
        result = report
    ) {
        Button(onClick = onBackgroundCpuWork) {
            Text("在后台计算质数")
        }
    }
}

@Composable
private fun MemoryCard(
    report: MemoryReport,
    onAllocateMemory: () -> Unit,
    onReleaseMemory: () -> Unit
) {
    LabCard(
        title = "内存体检",
        description = "用小块内存模拟列表缓存、图片缓存和大对象持有，观察引用释放前后的变化。",
        result = report.summary
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onAllocateMemory) {
                Text("保留 2MB")
            }
            OutlinedButton(onClick = onReleaseMemory) {
                Text("释放内存")
            }
        }
    }
}

@Composable
private fun StabilityCard(
    report: String,
    onCaptureCrash: () -> Unit
) {
    LabCard(
        title = "崩溃治理",
        description = "可预期错误应该被建模和捕获；真正崩溃则需要堆栈、版本、设备和用户路径。",
        result = report
    ) {
        Button(onClick = onCaptureCrash) {
            Text("捕获一次模拟异常")
        }
    }
}

@Composable
private fun ComposeListCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle("Compose 列表观察")
            Text(
                text = "列表性能要关注 item key、重组范围、图片加载、滚动时对象创建和状态提升。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = (1..40).toList(),
                    key = { it }
                ) { index ->
                    CodeLine("课程性能样本 #$index：稳定 key + 小范围重组")
                }
            }
        }
    }
}

@Composable
private fun ChecklistCard(items: List<HealthCheckItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFEFF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle("发布前性能稳定性体检")
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.status.label,
                        modifier = Modifier.width(64.dp),
                        color = item.status.color,
                        fontWeight = FontWeight.Bold
                    )
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabCard(
    title: String,
    description: String,
    result: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(title)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
            Text(
                text = result,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDFA), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF115E59),
                fontWeight = FontWeight.SemiBold
            )
            content()
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
            .background(Color(0xFFF0FDFA), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF0F766E),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF134E4A),
            fontWeight = FontWeight.SemiBold
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

private val CheckStatus.label: String
    get() = when (this) {
        CheckStatus.Ready -> "待观察"
        CheckStatus.Warning -> "重点"
        CheckStatus.Passed -> "通过"
    }

private val CheckStatus.color: Color
    get() = when (this) {
        CheckStatus.Ready -> Color(0xFF0F766E)
        CheckStatus.Warning -> Color(0xFFB45309)
        CheckStatus.Passed -> Color(0xFF4338CA)
    }
