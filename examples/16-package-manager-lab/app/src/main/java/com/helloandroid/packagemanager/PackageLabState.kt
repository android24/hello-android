package com.helloandroid.packagemanager

data class PackageLabState(
    val identity: AppIdentity = AppIdentity(),
    val score: PackageLabScore = PackageLabScore(),
    val currentExperiment: PackageExperiment = PackageExperiment(),
    val components: List<ComponentCard> = emptyList(),
    val intentResult: IntentResult = IntentResult(),
    val visibilityCards: List<VisibilityCard> = emptyList(),
    val permissionCards: List<PermissionCard> = emptyList(),
    val signatureCard: SignatureCard = SignatureCard(),
    val diagnosticCards: List<PackageDiagnosticCard> = defaultPackageDiagnosticCards,
    val eventTrail: List<PackageEventLog> = emptyList()
)

data class AppIdentity(
    val packageName: String = "等待读取",
    val versionName: String = "-",
    val versionCode: String = "-",
    val targetSdk: String = "-",
    val firstInstallTime: String = "-",
    val lastUpdateTime: String = "-",
    val sourceDir: String = "-"
)

data class PackageLabScore(
    val packageInfoObserved: Boolean = false,
    val componentsObserved: Boolean = false,
    val intentResolved: Boolean = false,
    val visibilityObserved: Boolean = false,
    val permissionObserved: Boolean = false,
    val signatureObserved: Boolean = false,
    val componentStateChanged: Boolean = false,
    val diagnosisObserved: Boolean = false
)

data class PackageExperiment(
    val operation: String = "等待包管理实验",
    val expected: String = "先猜测：这次操作会读取包信息、匹配组件、检查权限，还是暴露包可见性？",
    val actual: String = "点击实验按钮后观察 PackageManager 返回值和事件轨迹。",
    val conclusion: String = "还没有证据。"
)

data class ComponentCard(
    val type: String,
    val name: String,
    val exported: String,
    val enabled: String,
    val permission: String,
    val note: String
)

data class IntentResult(
    val scenario: String = "尚未查询 Intent",
    val intentSummary: String = "等待选择一个 Intent 场景",
    val resolvedName: String = "-",
    val candidates: List<IntentCandidate> = emptyList(),
    val conclusion: String = "Intent 解析最适合用来观察 PMS 如何从 Manifest 组件表里找候选。"
)

data class IntentCandidate(
    val label: String,
    val packageName: String,
    val className: String,
    val exported: String,
    val permission: String
)

data class VisibilityCard(
    val target: String,
    val query: String,
    val result: String,
    val meaning: String
)

data class PermissionCard(
    val name: String,
    val protection: String,
    val declared: String,
    val granted: String,
    val note: String
)

data class SignatureCard(
    val mode: String = "-",
    val signerCount: String = "-",
    val digest: String = "-",
    val note: String = "签名是安装覆盖、signature 权限和应用身份校验的重要证据。"
)

data class PackageDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

data class PackageEventLog(
    val source: String,
    val phase: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val defaultPackageDiagnosticCards = listOf(
    PackageDiagnosticCard(
        title = "Activity 找不到",
        symptom = "startActivity 或 resolveActivity 失败。",
        firstEvidence = "查看 action、category、data、MIME、exported 和 queryIntentActivities 结果。",
        fixDirection = "校准 intent-filter；Android 11+ 同时检查 queries。"
    ),
    PackageDiagnosticCard(
        title = "安装失败",
        symptom = "adb install 返回签名、版本或 ABI 相关错误。",
        firstEvidence = "先把错误码归类到解析、校验、提交或数据迁移阶段。",
        fixDirection = "检查签名一致性、versionCode、minSdk、ABI、APK 结构和存储空间。"
    ),
    PackageDiagnosticCard(
        title = "权限异常",
        symptom = "Manifest 声明了权限，但运行时依然 Permission Denial。",
        firstEvidence = "区分 requested permission、runtime grant、signature 权限和组件 permission。",
        fixDirection = "危险权限要请求授权；signature 权限需要相同签名；组件权限要调用方满足门禁。"
    ),
    PackageDiagnosticCard(
        title = "查询不到第三方 App",
        symptom = "PackageManager 查询返回空或 NameNotFoundException。",
        firstEvidence = "在 Android 11+ 设备上检查 queries、Intent 查询类型和目标包是否可见。",
        fixDirection = "增加最小 queries 声明，优先用业务真实 Intent 查询而不是滥用 QUERY_ALL_PACKAGES。"
    ),
    PackageDiagnosticCard(
        title = "组件被系统忽略",
        symptom = "Manifest 里有组件，但查询或启动时像不存在。",
        firstEvidence = "检查 enabled 状态、exported、permission、user state 和 intent-filter。",
        fixDirection = "确认组件未被禁用；外部入口必须 exported=true，并配置清晰的权限边界。"
    )
)
