package com.helloandroid.security

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var store: SecurityLabStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SecurityLabStore(application)

        setContent {
            var state by remember { mutableStateOf(store.loadState()) }
            val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                state = store.refreshWith(state)
            }
            val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                state = store.refreshWith(state)
            }
            val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                state = store.recordPickedPhoto(state, uri?.toString())
            }
            val exportReportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                state = store.exportReportToSaf(state, uri?.toString())
            }

            SecurityLabScreen(
                state = state,
                onRefresh = { state = store.refreshWith(state) },
                onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                onRequestNotification = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        state = store.refreshWith(state)
                    }
                },
                onEncrypt = { state = store.encryptToken(state) },
                onDecrypt = { state = store.decryptToken(state) },
                onDeleteKeyAndDecrypt = { state = store.deleteKeyAndTryDecrypt(state) },
                onSanitizeLog = { state = store.sanitizeLog(state) },
                onShareReport = {
                    val (next, intent) = store.createShareReport(state)
                    state = next
                    startActivity(intent)
                },
                onJudgeAppOps = { op, mode -> state = store.judgeAppOps(state, op, mode) },
                onUpdateAppOpsOutput = { output -> state = store.updateAppOpsOutput(state, output) },
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSelectPendingIntentCase = { caseName -> state = store.selectPendingIntentCase(state, caseName) },
                onSelectSignatureScenario = { scenario -> state = store.selectSignatureScenario(state, scenario) },
                onUpdateSignatureOutputs = { debugOutput, releaseOutput ->
                    state = store.updateSignatureOutputs(
                        current = state,
                        debugOutput = debugOutput,
                        releaseOutput = releaseOutput,
                    )
                },
                onSelectIncident = { incident -> state = store.selectIncident(state, incident) },
                onUpdateDiagnosisDraft = { category, evidence, fix ->
                    state = store.updateDiagnosisDraft(
                        current = state,
                        category = category,
                        evidence = evidence,
                        fix = fix,
                    )
                },
                onUpdateFreeTextReport = { report -> state = store.updateFreeTextReport(state, report) },
                onSubmitDiagnosisQuiz = { state = store.submitDiagnosisQuiz(state) },
                onExportReport = { exportReportLauncher.launch("security-diagnosis-report.txt") },
            )
        }
    }
}
