package com.helloandroid.governance

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
    private val store = GovernanceLabStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(store.initialState()) }
            val report = store.generatedReport(state)

            GovernanceLabScreen(
                state = state,
                report = report,
                onPolicySelected = { state = store.selectPolicy(state, it) },
                onScenarioSelected = { state = store.selectScenario(state, it) },
                onRiskSelected = { state = store.selectRisk(state, it) },
                onCompleteNextTask = { state = store.completeNextTask(state) },
                onCompleteArea = { state = store.completeArea(state, it) },
                onCopyReport = { copyReport(report) },
                onShareReport = { shareReport(report) },
            )
        }
    }

    private fun copyReport(report: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("大型工程治理报告", report))
        Toast.makeText(this, "治理报告已复制", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport(report: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "第25章大型工程治理报告")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "分享治理报告"))
    }
}
