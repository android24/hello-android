package com.helloandroid.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityLabScreen(
    state: SecurityLabState,
    onRefresh: () -> Unit,
    onRequestCamera: () -> Unit,
    onRequestNotification: () -> Unit,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
    onDeleteKeyAndDecrypt: () -> Unit,
    onSanitizeLog: () -> Unit,
    onShareReport: () -> Unit,
    onJudgeAppOps: (String, String) -> Unit,
    onUpdateAppOpsOutput: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onSelectPendingIntentCase: (String) -> Unit,
    onSelectSignatureScenario: (String) -> Unit,
    onUpdateSignatureOutputs: (String?, String?) -> Unit,
    onSelectIncident: (IncidentScript) -> Unit,
    onUpdateDiagnosisDraft: (String?, String?, String?) -> Unit,
    onUpdateFreeTextReport: (String) -> Unit,
    onSubmitDiagnosisQuiz: () -> Unit,
    onExportReport: () -> Unit,
) {
    MaterialTheme(
        colorScheme = securityColorScheme(),
    ) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HeaderCard()
                ScoreBoard(state.scoreItems)
                StoryModeCard(state.scoreItems)
                IdentitySection(state.identity, onRefresh)
                PermissionSection(
                    permissions = state.permissions,
                    onRequestCamera = onRequestCamera,
                    onRequestNotification = onRequestNotification,
                )
                AppOpsExperimentSection(
                    experiment = state.appOpsExperiment,
                    onJudgeAppOps = onJudgeAppOps,
                    onUpdateAppOpsOutput = onUpdateAppOpsOutput,
                )
                PhotoAccessSection(
                    experiment = state.photoAccessExperiment,
                    onPickPhoto = onPickPhoto,
                )
                SignatureExperimentSection(
                    experiment = state.signatureExperiment,
                    onSelectScenario = onSelectSignatureScenario,
                    onUpdateSignatureOutputs = onUpdateSignatureOutputs,
                )
                JudgmentSection(state.judgmentCards)
                PendingIntentSection(
                    experiment = state.pendingIntentExperiment,
                    onSelectCase = onSelectPendingIntentCase,
                )
                KeystoreSection(
                    experiment = state.keystore,
                    onEncrypt = onEncrypt,
                    onDecrypt = onDecrypt,
                    onDeleteKeyAndDecrypt = onDeleteKeyAndDecrypt,
                )
                DataBoundarySection(
                    rawLog = state.rawLog,
                    sanitizedLog = state.sanitizedLog,
                    shareUri = state.shareUri,
                    onSanitizeLog = onSanitizeLog,
                    onShareReport = onShareReport,
                )
                IncidentSection(
                    incidents = state.incidents,
                    selectedIncident = state.selectedIncident,
                    onSelectIncident = onSelectIncident,
                )
                DiagnosisQuizSection(
                    selectedIncident = state.selectedIncident,
                    quiz = state.diagnosisQuiz,
                    onUpdateDiagnosisDraft = onUpdateDiagnosisDraft,
                    onUpdateFreeTextReport = onUpdateFreeTextReport,
                    onSubmitDiagnosisQuiz = onSubmitDiagnosisQuiz,
                )
                DiagnosisSection(
                    report = state.diagnosisReport,
                    safExportUri = state.safExportUri,
                    onExportReport = onExportReport,
                )
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    LabCard {
        Text(
            text = "第23章 安全模型观察实验室",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "系统为什么允许或拒绝这个 App 做这件事？",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "这不是一个权限按钮合集。每个实验都要回答：判断模块、判断输入、判断结果、系统证据和修复建议。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreBoard(items: List<ScoreItem>) {
    LabCard(title = "任务板") {
        val doneCount = items.count { it.done }
        val nextItem = items.firstOrNull { !it.done }
        Text(
            text = "已完成 $doneCount / ${items.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "当前建议：${nextItem?.title ?: "导出报告并复盘一次完整判断链"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                StatusChip(
                    text = item.title,
                    done = item.done,
                    supporting = item.description,
                )
            }
        }
    }
}

@Composable
private fun StoryModeCard(items: List<ScoreItem>) {
    val nextItem = items.firstOrNull { !it.done }
    LabCard(
        title = "剧情模式",
        statusLabel = if (nextItem == null) "已通关" else "进行中",
        statusDone = nextItem == null,
    ) {
        CommandBlock(
            "从一个安全事故开始，不要从 API 开始。\n" +
                "路线：选事故 -> 查身份 -> 查权限 -> 查 AppOps -> 查边界 -> 查数据保护 -> 答题 -> 导出报告"
        )
        LabeledText("当前步骤", nextItem?.title ?: "完整复盘")
        LabeledText("下一步", nextItem?.description ?: "重新选择一个事故剧本，尝试用另一条证据链解释它。")
    }
}

@Composable
private fun IdentitySection(identity: IdentitySnapshot, onRefresh: () -> Unit) {
    LabCard(
        title = "身份与沙箱实验",
        statusLabel = "已完成",
        statusDone = true,
    ) {
        CommandBlock(
            "判断模块：Linux uid / SELinux / Binder callingUid\n" +
                "判断输入：packageName、uid、processName、dataDir\n" +
                "判断结果：App 的安全身份不只是包名，还包括 uid、签名和当前用户空间。"
        )
        InfoGrid(
            listOf(
                "packageName" to identity.packageName,
                "uid / pid" to "${identity.uid} / ${identity.pid}",
                "processName" to identity.processName,
                "targetSdk" to identity.targetSdk.toString(),
                "buildProfile" to identity.buildProfile,
                "installer" to identity.installer,
                "dataDir" to identity.dataDir,
                "filesDir" to identity.filesDir,
                "cacheDir" to identity.cacheDir,
                "signature SHA-256" to identity.signatureSha256,
            )
        )
        OutlinedButton(onClick = onRefresh) {
            Text("刷新运行现场")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PermissionSection(
    permissions: List<PermissionSnapshot>,
    onRequestCamera: () -> Unit,
    onRequestNotification: () -> Unit,
) {
    val done = permissions.any { it.runtimeGranted }
    LabCard(
        title = "权限状态实验",
        statusLabel = if (done) "已完成" else "待完成",
        statusDone = done,
    ) {
        CommandBlock(
            "判断模块：PackageManagerService / PermissionManagerService / PermissionController\n" +
                "判断输入：Manifest、targetSdk、permission、用户选择\n" +
                "系统证据：adb shell dumpsys package com.helloandroid.security"
        )
        permissions.forEach { permission ->
            PermissionRow(permission)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onRequestCamera) {
                Text("请求相机权限")
            }
            OutlinedButton(onClick = onRequestNotification) {
                Text("请求通知权限")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppOpsExperimentSection(
    experiment: AppOpsExperiment,
    onJudgeAppOps: (String, String) -> Unit,
    onUpdateAppOpsOutput: (String) -> Unit,
) {
    val ops = listOf("POST_NOTIFICATION", "CAMERA", "FINE_LOCATION", "READ_MEDIA_IMAGES")
    val modes = listOf("allow", "ignore", "foreground", "deny", "default")

    LabCard(
        title = "AppOps 裁决实验",
        statusLabel = if (experiment.done) "已完成" else "待完成",
        statusDone = experiment.done,
    ) {
        CommandBlock(
            "判断模块：AppOpsService + 具体系统服务\n" +
                "判断输入：op、mode、前后台状态、调用方 uid\n" +
                "实验目标：看懂 granted 之后，系统为什么还可能拦一次。"
        )
        LabeledText("当前判断", "${experiment.selectedOp} / ${experiment.selectedMode}")
        LabeledText("解析 mode", experiment.parsedMode)
        LabeledText("系统结论", experiment.verdict)
        LabeledText("取证命令", experiment.evidenceCommand)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = experiment.rawOutput,
            onValueChange = onUpdateAppOpsOutput,
            label = { Text("粘贴 appops 输出") },
            minLines = 2,
            maxLines = 5,
        )
        LabeledText("可粘贴示例", "POST_NOTIFICATION: ignore\nCAMERA: foreground")
        Text("选择 op", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ops.forEach { op ->
                OutlinedButton(onClick = { onJudgeAppOps(op, experiment.selectedMode) }) {
                    Text(op)
                }
            }
        }
        Text("选择 mode", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            modes.forEach { mode ->
                Button(onClick = { onJudgeAppOps(experiment.selectedOp, mode) }) {
                    Text(mode)
                }
            }
        }
    }
}

@Composable
private fun PhotoAccessSection(
    experiment: PhotoAccessExperiment,
    onPickPhoto: () -> Unit,
) {
    LabCard(
        title = "相册访问与 Photo Picker 实验",
        statusLabel = if (experiment.done) "已完成" else "待完成",
        statusDone = experiment.done,
    ) {
        CommandBlock(
            "判断模块：Photo Picker / MediaProvider / UriGrantsManagerService\n" +
                "判断输入：用户选择的单个 Uri、媒体权限、部分授权\n" +
                "实验目标：区分“用户给我这一张”和“App 读取整个媒体库”。"
        )
        LabeledText("权限模型", experiment.permissionModel)
        LabeledText("pickedUri", experiment.pickedUri)
        LabeledText("系统结论", experiment.verdict)
        Button(onClick = onPickPhoto) {
            Text("打开 Photo Picker")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignatureExperimentSection(
    experiment: SignatureExperiment,
    onSelectScenario: (String) -> Unit,
    onUpdateSignatureOutputs: (String?, String?) -> Unit,
) {
    val done = experiment.done && experiment.comparisonDone
    LabCard(
        title = "签名升级身份实验",
        statusLabel = if (done) "已完成" else "待完成",
        statusDone = done,
    ) {
        CommandBlock(
            "判断模块：PackageInstallerService / PackageManagerService / ApkSignatureVerifier / SigningDetails\n" +
                "判断输入：APK 签名块、证书链、已安装包签名、signing lineage\n" +
                "实验目标：区分 APK 完整性有效、App 升级身份可信和 debug / release 证书是否一致。"
        )
        LabeledText("当前证书 SHA-256", experiment.currentSha256)
        LabeledText("升级场景", experiment.selectedScenario)
        LabeledText("系统结论", experiment.verdict)
        LabeledText("验证命令", experiment.command)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { onSelectScenario("same") }) {
                Text("同签名升级")
            }
            OutlinedButton(onClick = { onSelectScenario("lineage") }) {
                Text("签名轮换")
            }
            OutlinedButton(onClick = { onSelectScenario("different") }) {
                Text("签名不同")
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = experiment.debugCertOutput,
            onValueChange = { onUpdateSignatureOutputs(it, null) },
            label = { Text("粘贴 debug APK apksigner 输出") },
            minLines = 2,
            maxLines = 5,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = experiment.releaseCertOutput,
            onValueChange = { onUpdateSignatureOutputs(null, it) },
            label = { Text("粘贴 release APK apksigner 输出") },
            minLines = 2,
            maxLines = 5,
        )
        LabeledText("debug SHA-256", experiment.debugSha256)
        LabeledText("release SHA-256", experiment.releaseSha256)
        LabeledText("实测对比", experiment.comparisonVerdict)
    }
}

@Composable
private fun PermissionRow(permission: PermissionSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = permission.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            StatusPill(if (permission.runtimeGranted) "granted" else "denied", permission.runtimeGranted)
        }
        Text(
            text = permission.permission,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text("Manifest 声明：${permission.declaredInManifest}")
        Text("AppOps 命令：${permission.appOpsCommand}")
        Text(
            text = permission.note,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PendingIntentSection(
    experiment: PendingIntentExperiment,
    onSelectCase: (String) -> Unit,
) {
    LabCard(
        title = "PendingIntent 授权令牌实验",
        statusLabel = if (experiment.done) "已完成" else "待完成",
        statusDone = experiment.done,
    ) {
        CommandBlock(
            "判断模块：ActivityTaskManagerService / PendingIntentRecord\n" +
                "判断输入：目标组件、requestCode、flags、extras、接收方\n" +
                "实验目标：理解 PendingIntent 不是普通 Intent，而是带着创建方身份的授权令牌。"
        )
        LabeledText("当前场景", experiment.selectedCase)
        LabeledText("风险等级", experiment.riskLevel)
        LabeledText("判断结论", experiment.verdict)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { onSelectCase("safe") }) {
                Text("显式 + Immutable")
            }
            OutlinedButton(onClick = { onSelectCase("mutable") }) {
                Text("Mutable")
            }
            OutlinedButton(onClick = { onSelectCase("implicit") }) {
                Text("隐式入口")
            }
        }
    }
}

@Composable
private fun JudgmentSection(cards: List<JudgmentCard>) {
    LabCard(title = "系统判断可视化") {
        cards.forEach { card ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F4EE), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(card.title, fontWeight = FontWeight.Bold)
                LabeledText("判断模块", card.module)
                LabeledText("判断输入", card.input)
                LabeledText("判断结果", card.result)
                LabeledText("系统证据", card.evidence)
                LabeledText("修复建议", card.fix)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeystoreSection(
    experiment: KeystoreExperiment,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
    onDeleteKeyAndDecrypt: () -> Unit,
) {
    val done = experiment.status != "等待实验"
    LabCard(
        title = "Keystore 密钥生命周期实验",
        statusLabel = if (done) "已完成" else "待完成",
        statusDone = done,
    ) {
        CommandBlock(
            "判断模块：Android Keystore Provider / keystore2 / KeyMint\n" +
                "判断输入：alias、认证状态、密文、iv、tag、设备安全状态\n" +
                "判断目标：密文可恢复不代表 key 一定可恢复。"
        )
        InfoGrid(
            listOf(
                "alias" to experiment.alias,
                "plainText" to experiment.plainText,
                "cipherText" to experiment.cipherTextPreview,
                "decryptedText" to experiment.decryptedText,
                "status" to experiment.status,
                "lastError" to experiment.lastError,
            )
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onEncrypt) {
                Text("生成 Keystore key 并加密 token")
            }
            OutlinedButton(onClick = onDecrypt) {
                Text("解密 token")
            }
            OutlinedButton(onClick = onDeleteKeyAndDecrypt) {
                Text("模拟换机后 key 丢失")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DataBoundarySection(
    rawLog: String,
    sanitizedLog: String,
    shareUri: String,
    onSanitizeLog: () -> Unit,
    onShareReport: () -> Unit,
) {
    val done = sanitizedLog != "点击“生成脱敏日志”后查看安全输出。"
    LabCard(
        title = "日志脱敏与 FileProvider 边界",
        statusLabel = if (done) "已完成" else "待完成",
        statusDone = done,
    ) {
        CommandBlock(
            "判断模块：FileProvider / UriGrantsManagerService / 日志网关\n" +
                "判断输入：share/ 目录、content Uri、grant flag、敏感字段\n" +
                "修复建议：只暴露 share/，分享前生成脱敏副本。"
        )
        LabeledText("原始日志", rawLog)
        LabeledText("脱敏日志", sanitizedLog)
        LabeledText("shareUri", shareUri)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onSanitizeLog) {
                Text("生成脱敏日志")
            }
            OutlinedButton(onClick = onShareReport) {
                Text("创建并分享报告")
            }
        }
    }
}

@Composable
private fun IncidentSection(
    incidents: List<IncidentScript>,
    selectedIncident: IncidentScript?,
    onSelectIncident: (IncidentScript) -> Unit,
) {
    LabCard(
        title = "安全事故剧本",
        statusLabel = if (selectedIncident != null) "已选择" else "待选择",
        statusDone = selectedIncident != null,
    ) {
        selectedIncident?.let { incident ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F4EE), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("当前办案：${incident.title}", fontWeight = FontWeight.Bold)
                LabeledText("错误直觉", incident.wrongGuess)
                LabeledText("正确诊断", incident.correctDiagnosis)
                LabeledText("下一步动作", incident.nextAction)
            }
            Spacer(Modifier.height(8.dp))
        }
        incidents.forEach { incident ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(incident.title, fontWeight = FontWeight.Bold)
                LabeledText("用户现象", incident.symptom)
                LabeledText("第一证据", incident.firstEvidence)
                LabeledText("隐藏陷阱", incident.hiddenTrap)
                LabeledText("下一步动作", incident.nextAction)
                OutlinedButton(onClick = { onSelectIncident(incident) }) {
                    Text("选择这个案子")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DiagnosisQuizSection(
    selectedIncident: IncidentScript?,
    quiz: DiagnosisQuiz,
    onUpdateDiagnosisDraft: (String?, String?, String?) -> Unit,
    onUpdateFreeTextReport: (String) -> Unit,
    onSubmitDiagnosisQuiz: () -> Unit,
) {
    val hasIncident = selectedIncident != null
    val missingItems = listOfNotNull(
        if (!hasIncident) "事故剧本" else null,
        if (quiz.selectedCategory == "尚未选择") "问题类型" else null,
        if (quiz.selectedEvidence == "尚未选择") "第一证据" else null,
        if (quiz.selectedFix == "尚未选择") "修复动作" else null,
    )
    val canSubmit = missingItems.isEmpty()
    val statusLabel = when {
        quiz.done -> "${quiz.score}/3"
        hasIncident -> "答题中"
        else -> "待选择"
    }

    val categoryOptions = listOf(
        "权限和实际放行问题",
        "AppOps / 设置问题",
        "组件边界问题",
        "Keystore / 数据保护问题",
        "日志 / 导出泄露问题",
    )
    val evidenceOptions = listOf(
        "通知权限 + AppOps + channel",
        "CAMERA 权限 + AppOps + 前台状态",
        "alias + 解密异常",
        "manifest exported + permission",
        "file_paths + content Uri",
        "定位权限 + AppOps foreground + 前后台状态",
    )
    val fixOptions = listOf(
        "分层引导和降级",
        "权限拒绝降级",
        "清理密文并重新登录",
        "收窄 exported 或加 signature permission",
        "收窄 paths 并脱敏",
        "前台触发或后台定位降级",
    )

    LabCard(
        title = "安全诊断答题区",
        statusLabel = statusLabel,
        statusDone = quiz.done,
    ) {
        CommandBlock(
            "玩法：先选择一个事故剧本，再提交三段判断。\n" +
                "目标：不要凭感觉说“权限问题”，要把问题类型、第一证据和修复动作连成判断链。"
        )
        LabeledText("当前事故", selectedIncident?.title ?: "尚未选择")
        LabeledText("用户现象", selectedIncident?.symptom ?: "请先在上方选择一个案子。")
        ChoiceGroup(
            title = "问题类型",
            selected = quiz.selectedCategory,
            options = categoryOptions,
            onSelect = { onUpdateDiagnosisDraft(it, null, null) },
        )
        ChoiceGroup(
            title = "第一证据",
            selected = quiz.selectedEvidence,
            options = evidenceOptions,
            onSelect = { onUpdateDiagnosisDraft(null, it, null) },
        )
        ChoiceGroup(
            title = "修复动作",
            selected = quiz.selectedFix,
            options = fixOptions,
            onSelect = { onUpdateDiagnosisDraft(null, null, it) },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = quiz.freeTextReport,
            onValueChange = onUpdateFreeTextReport,
            label = { Text("自由文本诊断报告") },
            minLines = 4,
            maxLines = 8,
        )
        LabeledText("自由报告得分", "${quiz.freeTextScore}/4")
        LabeledText("自由报告反馈", quiz.freeTextFeedback)
        Button(
            enabled = canSubmit,
            onClick = onSubmitDiagnosisQuiz,
        ) {
            Text("提交并对照标准答案")
        }
        if (!canSubmit) {
            LabeledText("还差", missingItems.joinToString("、"))
        }
        LabeledText("得分", "${quiz.score}/3")
        LabeledText("反馈", quiz.feedback)
        selectedIncident?.let { incident ->
            LabeledText("标准判断", incident.correctDiagnosis)
        }
    }
}

@Composable
private fun ChoiceGroup(
    title: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    Text(title, fontWeight = FontWeight.SemiBold)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            if (option == selected) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(option) },
                ) {
                    Text(option)
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(option) },
                ) {
                    Text(option)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiagnosisSection(
    report: String,
    safExportUri: String,
    onExportReport: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    LabCard(
        title = "安全诊断报告",
        statusLabel = if (safExportUri.startsWith("content://")) "已导出" else "待导出",
        statusDone = safExportUri.startsWith("content://"),
    ) {
        LabeledText("SAF 导出", safExportUri)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起诊断报告" else "查看诊断报告")
            }
            OutlinedButton(onClick = onExportReport) {
                Text("导出报告到用户选择的位置")
            }
        }
        if (expanded) {
            CommandBlock(report)
        }
    }
}

@Composable
private fun LabCard(
    title: String? = null,
    statusLabel: String? = null,
    statusDone: Boolean = false,
    content: @Composable Column.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (statusLabel != null) {
                        StatusPill(statusLabel, statusDone)
                    }
                }
                Divider()
            }
            content()
        }
    }
}

@Composable
private fun InfoGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, value) ->
            LabeledText(label, value)
        }
    }
}

@Composable
private fun LabeledText(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CommandBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF172021), RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFEAF2EF),
        )
    }
}

@Composable
private fun StatusChip(text: String, done: Boolean, supporting: String) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .background(
                color = if (done) Color(0xFFE2F0E9) else Color(0xFFF4E6E0),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatusPill(if (done) "已观察" else "待观察", done)
        Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Text(
            supporting,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4F5F5E),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(text: String, positive: Boolean) {
    val background = when {
        positive -> Color(0xFF2F6F57)
        text == "denied" || text.startsWith("0/3") -> Color(0xFF9B4A3F)
        else -> Color(0xFF8A6A2F)
    }
    Text(
        text = text,
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(100.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun securityColorScheme() = MaterialTheme.colorScheme.copy(
    primary = Color(0xFF325B5F),
    secondary = Color(0xFF7B5E35),
    surface = Color(0xFFFFFBF7),
    surfaceVariant = Color(0xFFE8EFEC),
    background = Color(0xFFF2F5F1),
    onSurface = Color(0xFF152021),
    onSurfaceVariant = Color(0xFF52615E),
)
