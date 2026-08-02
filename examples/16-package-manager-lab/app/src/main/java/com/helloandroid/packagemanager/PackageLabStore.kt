package com.helloandroid.packagemanager

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PackageLabStore {
    private const val TAG = "PackageManagerLab"
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(PackageLabState())

    val state: StateFlow<PackageLabState> = _state

    fun refresh(context: Context, reason: String = "读取包信息", keepExperiment: Boolean = false) {
        val appContext = context.applicationContext
        val packageInfo = appContext.packageManager.getPackageInfoCompat(
            appContext.packageName,
            PACKAGE_INFO_FLAGS
        )
        val identity = packageInfo.toIdentity(appContext)
        val components = packageInfo.toComponentCards(appContext)
        val permissions = packageInfo.toPermissionCards(appContext)
        val signature = packageInfo.toSignatureCard()
        val visibility = appContext.collectVisibilityCards()

        _state.update {
            it.copy(
                identity = identity,
                components = components,
                permissionCards = permissions,
                signatureCard = signature,
                visibilityCards = visibility,
                score = it.score.copy(
                    packageInfoObserved = true,
                    componentsObserved = components.isNotEmpty(),
                    visibilityObserved = visibility.isNotEmpty(),
                    permissionObserved = permissions.isNotEmpty(),
                    signatureObserved = signature.digest != "-"
                ),
                currentExperiment = if (keepExperiment) {
                    it.currentExperiment
                } else {
                    PackageExperiment(
                        operation = reason,
                        expected = "PackageManager 会从系统包信息表返回当前 App 的身份、组件、权限和签名摘要。",
                        actual = "读取到 ${components.size} 个组件、${permissions.size} 个权限观察项、${visibility.size} 条可见性证据。",
                        conclusion = "Manifest 声明和安装状态已经被整理成可查询的 PackageInfo。"
                    )
                }
            )
        }
        record("PackageManager", "getPackageInfo", "READ", "components=${components.size}, permissions=${permissions.size}")
    }

    fun runIntentScenario(context: Context, scenario: IntentScenario) {
        val appContext = context.applicationContext
        val intent = scenario.createIntent(appContext)
        val resolveInfo = appContext.packageManager.resolveActivityCompat(intent, MATCH_DEFAULT_FLAGS)
        val candidates = appContext.packageManager.queryIntentActivitiesCompat(intent, MATCH_DEFAULT_FLAGS)
            .map { it.toCandidate(appContext.packageManager) }
        val result = IntentResult(
            scenario = scenario.title,
            intentSummary = intent.describe(),
            resolvedName = resolveInfo?.activityInfo?.shortName().orEmpty().ifBlank { "没有默认解析结果" },
            candidates = candidates,
            conclusion = when {
                candidates.isEmpty() -> "没有候选组件。请检查 action、category、data、MIME、enabled、permission 和 queries。"
                candidates.size == 1 -> "PMS 找到 1 个候选，默认解析结果比较明确。"
                else -> "PMS 找到 ${candidates.size} 个候选，真实启动时可能进入 Resolver 选择页。"
            }
        )

        _state.update {
            it.copy(
                intentResult = result,
                score = it.score.copy(intentResolved = true),
                currentExperiment = PackageExperiment(
                    operation = "Intent 解析：${scenario.title}",
                    expected = scenario.expected,
                    actual = "resolveActivity=${result.resolvedName}, candidates=${candidates.size}",
                    conclusion = result.conclusion
                )
            )
        }
        record("PackageManager", "queryIntentActivities", "INTENT", "${scenario.title}, candidates=${candidates.size}")
    }

    fun toggleDeepLinkComponent(context: Context) {
        val appContext = context.applicationContext
        val componentName = ComponentName(appContext, DeepLinkActivity::class.java)
        val pm = appContext.packageManager
        val current = pm.getComponentEnabledSetting(componentName)
        val next = if (current == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        pm.setComponentEnabledSetting(
            componentName,
            next,
            PackageManager.DONT_KILL_APP
        )
        _state.update {
            it.copy(
                score = it.score.copy(componentStateChanged = true),
                currentExperiment = PackageExperiment(
                    operation = "切换 DeepLinkActivity enabled",
                    expected = "组件 enabled 状态会影响 PMS 的查询和 Intent 匹配。",
                    actual = "DeepLinkActivity 已切换为 ${next.componentStateName()}。",
                    conclusion = "Manifest 是初始登记，用户态/运行态组件状态也会影响最终可用性。"
                )
            )
        }
        record("PackageManager", "setComponentEnabledSetting", "STATE", next.componentStateName())
        refresh(appContext, "刷新组件状态", keepExperiment = true)
    }

    fun markDiagnosisRead(title: String) {
        _state.update {
            it.copy(
                score = it.score.copy(diagnosisObserved = true),
                currentExperiment = it.currentExperiment.copy(
                    operation = "阅读诊断卡：$title",
                    conclusion = "包管理问题要先翻译成安装、解析、可见性、权限或组件状态证据。"
                )
            )
        }
        record("Diagnosis", title, "READ", "diagnostic card inspected")
    }

    fun clearEvents() {
        _state.update {
            it.copy(
                eventTrail = emptyList(),
                currentExperiment = PackageExperiment(),
                intentResult = IntentResult(),
                score = PackageLabScore()
            )
        }
        Log.d(TAG, "Events cleared")
    }

    fun recordExternalEntry(source: String, detail: String) {
        record(source, "entry", "EXTERNAL", detail)
    }

    private fun record(source: String, phase: String, signal: String, detail: String) {
        Log.d(TAG, "$source.$phase $signal: $detail")
        val event = PackageEventLog(
            source = source,
            phase = phase,
            signal = signal,
            detail = detail,
            timestamp = timeFormat.format(Date())
        )
        _state.update { state ->
            state.copy(eventTrail = (listOf(event) + state.eventTrail).take(36))
        }
    }
}

enum class IntentScenario(
    val title: String,
    val expected: String
) {
    ExplicitMain(
        title = "显式启动 MainActivity",
        expected = "显式 Intent 直接给出组件名，PMS 不需要在多个 intent-filter 中猜。"
    ),
    CustomDeepLink(
        title = "自定义 DeepLink",
        expected = "action + DEFAULT + helloandroid://package-lab 应该匹配 DeepLinkActivity。"
    ),
    SendText(
        title = "ACTION_SEND text/plain",
        expected = "系统会查找能分享 text/plain 的 Activity，queries 声明会影响可见范围。"
    ),
    ViewHttps(
        title = "ACTION_VIEW https",
        expected = "浏览器或能处理 https 的 Activity 可能成为候选。"
    ),
    MissingAction(
        title = "不存在的 action",
        expected = "没有组件声明这个 action 时，查询结果应为空。"
    );

    fun createIntent(context: Context): Intent {
        return when (this) {
            ExplicitMain -> Intent(context, MainActivity::class.java)
            CustomDeepLink -> Intent("com.helloandroid.packagemanager.ACTION_INSPECT").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("helloandroid://package-lab")
            }
            SendText -> Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Hello PackageManager")
            }
            ViewHttps -> Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))
            MissingAction -> Intent("com.helloandroid.packagemanager.ACTION_MISSING").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
        }
    }
}

private const val PACKAGE_INFO_FLAGS =
    PackageManager.GET_ACTIVITIES.toLong() or
        PackageManager.GET_SERVICES.toLong() or
        PackageManager.GET_RECEIVERS.toLong() or
        PackageManager.GET_PROVIDERS.toLong() or
        PackageManager.GET_PERMISSIONS.toLong() or
        PackageManager.GET_SIGNING_CERTIFICATES.toLong() or
        PackageManager.MATCH_DISABLED_COMPONENTS.toLong()

private const val MATCH_DEFAULT_FLAGS = PackageManager.MATCH_DEFAULT_ONLY.toLong()

private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Long): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags.toInt())
    }
}

private fun PackageManager.resolveActivityCompat(intent: Intent, flags: Long): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags))
    } else {
        @Suppress("DEPRECATION")
        resolveActivity(intent, flags.toInt())
    }
}

private fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Long): List<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags))
    } else {
        @Suppress("DEPRECATION")
        queryIntentActivities(intent, flags.toInt())
    }
}

private fun PackageInfo.toIdentity(context: Context): AppIdentity {
    val versionCodeText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION")
        versionCode.toString()
    }
    val targetSdk = applicationInfoCompat?.targetSdkVersion?.toString().orEmpty().ifBlank { "-" }
    val sourceDir = applicationInfoCompat?.sourceDir.orEmpty().ifBlank { "-" }

    return AppIdentity(
        packageName = packageName,
        versionName = versionName ?: "-",
        versionCode = versionCodeText,
        targetSdk = targetSdk,
        firstInstallTime = dateFormatSafe(firstInstallTime),
        lastUpdateTime = dateFormatSafe(lastUpdateTime),
        sourceDir = sourceDir.replace(context.packageName, "\${applicationId}")
    )
}

private fun PackageInfo.toComponentCards(context: Context): List<ComponentCard> {
    val appPackage = context.packageName
    return buildList {
        activities.orEmpty().forEach { add(it.toComponentCard("Activity", context)) }
        services.orEmpty().forEach { add(it.toComponentCard("Service", context)) }
        receivers.orEmpty().forEach { add(it.toComponentCard("Receiver", context)) }
        providers.orEmpty().forEach { provider ->
            add(
                ComponentCard(
                    type = "Provider",
                    name = provider.name.shortClassName(appPackage),
                    exported = provider.exported.yesNo(),
                    enabled = provider.enabled.yesNo(),
                    permission = provider.readPermission ?: provider.writePermission ?: "无",
                    note = "authority=${provider.authority}；runtime=${context.componentStateName(provider.name)}"
                )
            )
        }
    }
}

private fun ActivityInfo.toComponentCard(type: String, context: Context): ComponentCard {
    val appPackage = context.packageName
    val role = when {
        name.endsWith("MainActivity") -> "Launcher 入口"
        name.endsWith("DeepLinkActivity") -> "自定义 action + data，且带 signature 权限门禁"
        else -> "Manifest 注册组件"
    }
    return ComponentCard(
        type = type,
        name = name.shortClassName(appPackage),
        exported = exported.yesNo(),
        enabled = enabled.yesNo(),
        permission = permission ?: "无",
        note = "$role；runtime=${context.componentStateName(name)}"
    )
}

private fun PackageInfo.toPermissionCards(context: Context): List<PermissionCard> {
    val requested = requestedPermissions.orEmpty().toSet()
    return listOf(
        PermissionCard(
            name = Manifest.permission.POST_NOTIFICATIONS,
            protection = "dangerous/runtime",
            declared = (Manifest.permission.POST_NOTIFICATIONS in requested).yesNo(),
            granted = context.permissionState(Manifest.permission.POST_NOTIFICATIONS),
            note = "Android 13+ 需要运行时授权；旧版本表现不同。"
        ),
        PermissionCard(
            name = Manifest.permission.ACCESS_FINE_LOCATION,
            protection = "dangerous/runtime",
            declared = (Manifest.permission.ACCESS_FINE_LOCATION in requested).yesNo(),
            granted = context.permissionState(Manifest.permission.ACCESS_FINE_LOCATION),
            note = "声明只是登记，真正使用前还要检查授权状态。"
        ),
        PermissionCard(
            name = "${context.packageName}.permission.PACKAGE_LAB_SIGNATURE",
            protection = "signature/custom",
            declared = ("${context.packageName}.permission.PACKAGE_LAB_SIGNATURE" in requested).yesNo(),
            granted = context.permissionState("${context.packageName}.permission.PACKAGE_LAB_SIGNATURE"),
            note = "signature 权限要求调用方与定义方签名匹配。"
        )
    )
}

private fun PackageInfo.toSignatureCard(): SignatureCard {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val signingInfo = signingInfo ?: return SignatureCard(note = "当前 PackageInfo 没有返回签名信息。")
        val signers = if (signingInfo.hasMultipleSigners()) {
            signingInfo.apkContentsSigners.orEmpty()
        } else {
            signingInfo.signingCertificateHistory.orEmpty()
        }
        SignatureCard(
            mode = if (signingInfo.hasMultipleSigners()) "multiple signers" else "single signer/history",
            signerCount = signers.size.toString(),
            digest = signers.firstOrNull()?.toByteArray()?.sha256Short() ?: "-",
            note = "安装覆盖、sharedUserId、signature 权限都会用签名身份参与判断。"
        )
    } else {
        @Suppress("DEPRECATION")
        SignatureCard(
            mode = "legacy signatures",
            signerCount = signatures?.size?.toString() ?: "0",
            digest = signatures?.firstOrNull()?.toByteArray()?.sha256Short() ?: "-",
            note = "旧版本通过 signatures 字段观察签名。"
        )
    }
}

private fun Context.collectVisibilityCards(): List<VisibilityCard> {
    val pm = packageManager
    val sendIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
    val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))
    val selfVisible = try {
        pm.getPackageInfoCompat(packageName, 0)
        "可见"
    } catch (_: PackageManager.NameNotFoundException) {
        "不可见"
    }
    val settingsVisible = try {
        pm.getPackageInfoCompat("com.android.settings", 0)
        "可见"
    } catch (_: PackageManager.NameNotFoundException) {
        "不可见或未安装"
    }

    return listOf(
        VisibilityCard(
            target = "当前 App",
            query = "getPackageInfo(packageName)",
            result = selfVisible,
            meaning = "自己永远应该可见，这是包管理实验的基线。"
        ),
        VisibilityCard(
            target = "系统设置",
            query = "getPackageInfo(com.android.settings)",
            result = settingsVisible,
            meaning = "Manifest 中声明了 queries/package，因此 Android 11+ 也可作为观察目标。"
        ),
        VisibilityCard(
            target = "分享文本候选",
            query = "queryIntentActivities(ACTION_SEND text/plain)",
            result = "${pm.queryIntentActivitiesCompat(sendIntent, MATCH_DEFAULT_FLAGS).size} 个候选",
            meaning = "queries/intent 声明让这类业务 Intent 查询更稳定。"
        ),
        VisibilityCard(
            target = "https 打开候选",
            query = "queryIntentActivities(ACTION_VIEW https)",
            result = "${pm.queryIntentActivitiesCompat(viewIntent, MATCH_DEFAULT_FLAGS).size} 个候选",
            meaning = "不要只问某个包在不在，更要问谁能处理这个 Intent。"
        )
    )
}

private fun ResolveInfo.toCandidate(pm: PackageManager): IntentCandidate {
    val label = loadLabel(pm)?.toString().orEmpty().ifBlank { activityInfo.shortName() }
    return IntentCandidate(
        label = label,
        packageName = activityInfo.packageName,
        className = activityInfo.name.shortClassName(activityInfo.packageName),
        exported = activityInfo.exported.yesNo(),
        permission = activityInfo.permission ?: "无"
    )
}

private fun Intent.describe(): String {
    val parts = buildList {
        component?.let { add("component=${it.shortClassName}") }
        action?.let { add("action=$it") }
        categories?.takeIf { it.isNotEmpty() }?.let { add("categories=${it.joinToString()}") }
        data?.let { add("data=$it") }
        type?.let { add("type=$it") }
    }
    return parts.joinToString(" | ").ifBlank { "empty intent" }
}

private fun String.shortClassName(packageName: String): String {
    return removePrefix("$packageName.")
}

private fun ActivityInfo.shortName(): String {
    return name.shortClassName(packageName)
}

private fun Boolean.yesNo(): String = if (this) "是" else "否"

private fun Context.permissionState(permission: String): String {
    return if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) "已授权" else "未授权"
}

private fun Int.componentStateName(): String {
    return when (this) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "enabled"
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "disabled"
        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> "default"
        else -> "state=$this"
    }
}

private fun Context.componentStateName(className: String): String {
    val componentName = ComponentName(packageName, className)
    return packageManager.getComponentEnabledSetting(componentName).componentStateName()
}

private fun ByteArray.sha256Short(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(this)
    return digest.joinToString("") { "%02x".format(it) }.take(24)
}

private fun dateFormatSafe(time: Long): String {
    return if (time <= 0L) "-" else PackageLabStoreDateHolder.format(time)
}

private object PackageLabStoreDateHolder {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun format(time: Long): String = formatter.format(Date(time))
}

private val PackageInfo.applicationInfoCompat: ApplicationInfo?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) applicationInfo else {
        @Suppress("DEPRECATION")
        applicationInfo
    }
