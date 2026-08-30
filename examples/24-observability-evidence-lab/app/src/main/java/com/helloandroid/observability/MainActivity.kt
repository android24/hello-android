package com.helloandroid.observability

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val store = ObservabilityLabStore()
    private val memorySamples = mutableListOf<ByteArray>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(store.initialState()) }
            val report = store.generatedReport(state)

            ObservabilityLabScreen(
                state = state,
                report = report,
                onScenarioSelected = { state = store.selectScenario(state, it) },
                onEvidenceSelected = { state = store.selectEvidence(state, it) },
                onCompleteNextTask = { state = store.completeNextTask(state) },
                onCompleteArea = { state = store.completeArea(state, it) },
                onTriggerMainThreadWork = {
                    val traceId = simulateMainThreadWork()
                    state = store.recordAction(
                        current = state,
                        message = "已制造一次约 120ms 主线程忙碌，traceId=$traceId，请用 logcat / Perfetto 观察。",
                        traceId = traceId,
                    )
                },
                onTriggerMemoryGrowth = {
                    val traceId = simulateMemoryGrowth()
                    state = store.recordAction(
                        current = state,
                        message = "已分配 ${memorySamples.size}MB 内存样本，traceId=$traceId，请用 meminfo 观察 PSS。",
                        traceId = traceId,
                    )
                },
                onQuizAnswerSelected = { questionId, answer ->
                    state = store.selectQuizAnswer(state, questionId, answer)
                },
                onSubmitQuiz = { state = store.submitQuiz(state) },
                onCopyReport = { copyReport(report) },
                onShareReport = { shareReport(report) },
            )
        }
    }

    private fun simulateMainThreadWork(): String {
        val traceId = newTraceId("main-thread")
        val start = SystemClock.uptimeMillis()
        Log.w("Chapter24Lab", "traceId=$traceId main-thread-work start")
        Trace.beginSection("ch24_main_thread_work_$traceId")
        try {
            while (SystemClock.uptimeMillis() - start < 120) {
                Math.sqrt(SystemClock.elapsedRealtimeNanos().toDouble())
            }
        } finally {
            Trace.endSection()
        }
        Log.w("Chapter24Lab", "traceId=$traceId main-thread-work end cost=${SystemClock.uptimeMillis() - start}ms")
        return traceId
    }

    private fun simulateMemoryGrowth(): String {
        val traceId = newTraceId("memory-growth")
        Log.w("Chapter24Lab", "traceId=$traceId memory-growth start")
        Trace.beginSection("ch24_memory_growth_$traceId")
        try {
            repeat(4) {
                memorySamples += ByteArray(1024 * 1024)
            }
        } finally {
            Trace.endSection()
        }
        Log.w("Chapter24Lab", "traceId=$traceId allocated memory samples: ${memorySamples.size}MB")
        return traceId
    }

    private fun newTraceId(prefix: String): String {
        return "$prefix-${SystemClock.uptimeMillis()}"
    }

    private fun copyReport(report: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("系统证据链报告", report))
        Toast.makeText(this, "证据链报告已复制", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport(report: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "第24章系统证据链报告")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "分享证据链报告"))
    }
}
