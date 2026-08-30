package com.helloandroid.security

data class IdentitySnapshot(
    val packageName: String = "",
    val uid: Int = -1,
    val pid: Int = -1,
    val processName: String = "",
    val targetSdk: Int = -1,
    val buildProfile: String = "",
    val dataDir: String = "",
    val filesDir: String = "",
    val cacheDir: String = "",
    val installer: String = "unknown",
    val signatureSha256: String = "unknown",
)

data class PermissionSnapshot(
    val label: String,
    val permission: String,
    val declaredInManifest: Boolean,
    val runtimeGranted: Boolean,
    val appOpsCommand: String,
    val note: String,
)

data class JudgmentCard(
    val title: String,
    val module: String,
    val input: String,
    val result: String,
    val evidence: String,
    val fix: String,
)

data class IncidentScript(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val hiddenTrap: String,
    val nextAction: String,
    val wrongGuess: String,
    val correctDiagnosis: String,
    val expectedCategory: String,
    val expectedEvidence: String,
    val expectedFix: String,
)

data class ScoreItem(
    val title: String,
    val done: Boolean,
    val description: String,
)

data class AppOpsExperiment(
    val selectedOp: String = "POST_NOTIFICATION",
    val selectedMode: String = "default",
    val rawOutput: String = "",
    val parsedMode: String = "尚未解析",
    val verdict: String = "选择一个 op 和 mode，观察系统服务会如何继续判断。",
    val evidenceCommand: String = "adb shell cmd appops get com.helloandroid.security POST_NOTIFICATION",
    val done: Boolean = false,
)

data class PendingIntentExperiment(
    val selectedCase: String = "尚未选择",
    val riskLevel: String = "待判断",
    val verdict: String = "选择一个 PendingIntent 场景，判断它是稳定授权令牌，还是可被篡改的入口。",
    val done: Boolean = false,
)

data class PhotoAccessExperiment(
    val pickedUri: String = "尚未选择",
    val permissionModel: String = "Photo Picker 不要求读取整个相册权限；媒体权限适合持续访问，部分授权适合 Android 14+ 的收窄场景。",
    val verdict: String = "点击 Photo Picker 选择一张图片，观察 content Uri 与相册读取权限之间的关系。",
    val done: Boolean = false,
)

data class SignatureExperiment(
    val currentSha256: String = "unknown",
    val selectedScenario: String = "尚未选择",
    val debugCertOutput: String = "",
    val releaseCertOutput: String = "",
    val debugSha256: String = "尚未解析",
    val releaseSha256: String = "尚未解析",
    val comparisonVerdict: String = "粘贴 debug / release 两个 APK 的 apksigner 输出后，观察证书身份是否一致。",
    val verdict: String = "选择一个升级场景，观察 PackageManagerService 如何用 SigningDetails 判断可信身份。",
    val command: String = "apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk",
    val comparisonDone: Boolean = false,
    val done: Boolean = false,
)

data class KeystoreExperiment(
    val alias: String = "hello_android_token_key_v1",
    val plainText: String = "token_user_42_scope_course_sync",
    val cipherTextPreview: String = "尚未加密",
    val decryptedText: String = "尚未解密",
    val status: String = "等待实验",
    val lastError: String = "无",
)

data class DiagnosisQuiz(
    val selectedCategory: String = "尚未选择",
    val selectedEvidence: String = "尚未选择",
    val selectedFix: String = "尚未选择",
    val freeTextReport: String = "",
    val freeTextFeedback: String = "写一段自由文本诊断报告，至少包含现象、证据、根因和修复。",
    val freeTextScore: Int = 0,
    val score: Int = 0,
    val feedback: String = "先选择一个事故剧本，再提交你的安全判断。",
    val freeTextDone: Boolean = false,
    val done: Boolean = false,
)

data class SecurityLabState(
    val identity: IdentitySnapshot = IdentitySnapshot(),
    val permissions: List<PermissionSnapshot> = emptyList(),
    val judgmentCards: List<JudgmentCard> = emptyList(),
    val incidents: List<IncidentScript> = emptyList(),
    val scoreItems: List<ScoreItem> = emptyList(),
    val appOpsExperiment: AppOpsExperiment = AppOpsExperiment(),
    val pendingIntentExperiment: PendingIntentExperiment = PendingIntentExperiment(),
    val photoAccessExperiment: PhotoAccessExperiment = PhotoAccessExperiment(),
    val signatureExperiment: SignatureExperiment = SignatureExperiment(),
    val selectedIncident: IncidentScript? = null,
    val diagnosisQuiz: DiagnosisQuiz = DiagnosisQuiz(),
    val keystore: KeystoreExperiment = KeystoreExperiment(),
    val rawLog: String = sampleRawLog,
    val sanitizedLog: String = "点击“生成脱敏日志”后查看安全输出。",
    val shareUri: String = "尚未创建",
    val safExportUri: String = "尚未导出",
    val diagnosisReport: String = "",
)

const val sampleRawLog: String =
    "POST /course/report Authorization=Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 phone=13800138000 uri=content://com.helloandroid.security.fileprovider/safe_share/report.txt"
