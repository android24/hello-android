package com.helloandroid.capstone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val store = CapstoneStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(store.initialState()) }
            val report = store.generatedReport(state)

            CapstoneScreen(
                state = state,
                report = report,
                onPhaseSelected = { state = store.selectPhase(state, it) },
                onReviewStageSelected = { state = store.selectReviewStage(state, it) },
                onChapterSelected = { state = store.selectChapter(state, it) },
                onScenarioSelected = { state = store.selectScenario(state, it) },
                onToggleTask = { state = store.toggleTask(state, it) },
                onToggleMission = { state = store.toggleMission(state, it) },
                onToggleReleaseGate = { state = store.toggleReleaseGate(state, it) },
                onRunTrace = { state = store.runTrace(state) },
                onSimulateIncident = { state = store.simulateIncident(state) },
                onCopyReport = {
                    val updated = store.markReportGenerated(state, "复制")
                    state = updated
                    copyReport(store.generatedReport(updated))
                },
                onShareReport = {
                    val updated = store.markReportGenerated(state, "分享")
                    state = updated
                    shareReport(store.generatedReport(updated))
                },
            )
        }
    }

    private fun copyReport(report: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("Hello Android Capstone 毕业报告", report))
        Toast.makeText(this, "毕业报告已复制", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport(report: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "第26章 Android 资深工程师毕业项目报告")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "分享毕业报告"))
    }
}
