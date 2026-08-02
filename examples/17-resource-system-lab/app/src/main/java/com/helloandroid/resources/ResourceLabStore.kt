package com.helloandroid.resources

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import com.helloandroid.coredesign.CoreDesignResources
import com.helloandroid.featurecatalog.CatalogResources
import com.helloandroid.legacywidget.LegacyWidgetResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ResourceLabStore {
    private const val TAG = "ResourceSystemLab"
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(ResourceLabState())
    private var replacementMode = ReplacementMode.Default

    val state: StateFlow<ResourceLabState> = _state

    fun refresh(context: Context, reason: String = "读取资源系统证据") {
        val appContext = context.applicationContext
        val resources = context.resources
        val targetId = R.string.resource_lab_greeting
        val dynamicName = context.getString(R.string.resource_lab_dynamic_name)
        val dynamicId = resources.getIdentifier(dynamicName, "string", context.packageName)

        val state = ResourceLabState(
            identity = targetId.toResourceIdentity(context),
            configuration = context.toConfigurationSnapshot(),
            themeCards = context.collectThemeCards(),
            replacement = context.collectReplacementState(replacementMode),
            dynamicLookup = DynamicLookup(
                requestedName = dynamicName,
                directId = targetId.toHex(),
                dynamicId = dynamicId.toHex(),
                directValue = context.getString(targetId),
                dynamicValue = if (dynamicId != 0) context.getString(dynamicId) else "not found",
                conclusion = if (dynamicId == targetId) {
                    "R 直接引用和 getIdentifier 最终拿到同一个资源 ID；release 中要小心 shrink 和资源名混淆。"
                } else {
                    "动态查找没有命中同一个 ID，请检查资源名、类型、包名、shrink 和混淆规则。"
                }
            ),
            sourceCards = context.collectSourceCards(),
            fileCards = context.collectFileCards(),
            experiment = ResourceExperiment(
                operation = reason,
                expected = "读取 R 常量、resources.arsc 反查、Configuration、Theme、依赖模块和文件资源证据。",
                actual = "resourceId=${targetId.toHex()}, dynamicId=${dynamicId.toHex()}, locale=${resources.configuration.locales[0]}",
                conclusion = "资源读取不是按文件名搜索，而是用资源 ID 查 resources.arsc，再由 Configuration 和 Theme 决定最终值。"
            ),
            score = ResourceLabScore(
                idObserved = true,
                configurationObserved = true,
                themeObserved = true,
                replacementObserved = _state.value.score.replacementObserved,
                dynamicObserved = true,
                dependencyObserved = true,
                fileObserved = true,
                appendixObserved = _state.value.score.appendixObserved,
                diagnosisObserved = _state.value.score.diagnosisObserved,
                reportObserved = _state.value.score.reportObserved
            ),
            eventTrail = _state.value.eventTrail
        )

        _state.value = state
        record("Resources", "refresh", "READ", "id=${targetId.toHex()}, package=${appContext.packageName}")
    }

    fun toggleReplacement(context: Context) {
        replacementMode = when (replacementMode) {
            ReplacementMode.Default -> ReplacementMode.Forest
            ReplacementMode.Forest -> ReplacementMode.Default
        }
        _state.update {
            it.copy(
                replacement = context.collectReplacementState(replacementMode),
                score = it.score.copy(replacementObserved = true),
                experiment = ResourceExperiment(
                    operation = "动态资源替换：${replacementMode.label}",
                    expected = "不修改 R 文件，只切换业务槽位映射到的资源 ID。",
                    actual = "title=${replacementMode.titleRes.toHex()}, panel=${replacementMode.panelColorRes.toHex()}",
                    conclusion = "动态替换更推荐走 Theme attr、ResourceProvider 或 Configuration，而不是运行时修改 R。"
                )
            )
        }
        record("Replacement", "toggle", "SWITCH", replacementMode.label)
    }

    fun markDependencyExperiment(context: Context) {
        _state.update {
            it.copy(
                score = it.score.copy(dependencyObserved = true),
                experiment = ResourceExperiment(
                    operation = "依赖资源覆盖实验",
                    expected = "app、feature、library 和旧依赖都会向最终资源表贡献资源。",
                    actual = "已展示 app/debug、core-design、feature-catalog、legacy-widget 的资源来源。",
                    conclusion = "排查资源冲突要看 merged resources 和依赖树，不能只搜当前模块。"
                )
            )
        }
        record("Dependency", "mergedResources", "TRACE", context.getString(R.string.resource_origin_marker))
    }

    fun markDiagnosisRead(title: String) {
        _state.update {
            it.copy(
                score = it.score.copy(diagnosisObserved = true),
                experiment = it.experiment.copy(
                    operation = "阅读诊断卡：$title",
                    conclusion = "资源问题要从 ID、资源表、Configuration、Theme、shrink 和依赖来源找证据。"
                )
            )
        }
        record("Diagnosis", title, "READ", "diagnostic card inspected")
    }

    fun markSkinningAppendixRead() {
        _state.update {
            it.copy(
                score = it.score.copy(appendixObserved = true),
                experiment = ResourceExperiment(
                    operation = "动态换肤附录实验",
                    expected = "先判断需求应该替换 Theme、Provider、Configuration、外部资源路径还是系统 overlay。",
                    actual = "已对比五种换肤方案的替换层、适用场景、第一风险和证据入口。",
                    conclusion = "动态换肤不是修改 R 文件，而是选择资源读取链路上的稳定决策点。"
                )
            )
        }
        record("Skinning", "appendix", "READ", "strategy map inspected")
    }

    fun markReportReady() {
        _state.update {
            it.copy(
                score = it.score.copy(reportObserved = true),
                experiment = it.experiment.copy(
                    operation = "资源系统诊断报告",
                    conclusion = "已经具备写报告的证据：ID 拆解、动态查找、配置、主题、依赖和文件读取。"
                )
            )
        }
        record("Report", "resourceReport", "READY", "report evidence collected")
    }

    fun clearEvents() {
        _state.update { it.copy(eventTrail = emptyList()) }
        Log.d(TAG, "Events cleared")
    }

    private fun record(source: String, phase: String, signal: String, detail: String) {
        Log.d(TAG, "$source.$phase $signal: $detail")
        val event = ResourceEventLog(
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

private fun Int.toResourceIdentity(context: Context): ResourceIdentity {
    val resources = context.resources
    return ResourceIdentity(
        resourceIdHex = toHex(),
        packagePart = ((this ushr 24) and 0xff).toHexByte(),
        typePart = ((this ushr 16) and 0xff).toHexByte(),
        entryPart = (this and 0xffff).toHexEntry(),
        resourceName = resources.getResourceName(this),
        typeName = resources.getResourceTypeName(this),
        entryName = resources.getResourceEntryName(this),
        value = resources.getString(this),
        aaptLinkStory = "AAPT2 link 同时写入 R.${resources.getResourceTypeName(this)}.${resources.getResourceEntryName(this)} 和 resources.arsc entry。"
    )
}

private fun Context.toConfigurationSnapshot(): ConfigurationSnapshot {
    val config = resources.configuration
    return ConfigurationSnapshot(
        locale = config.locales[0].toLanguageTag(),
        orientation = when (config.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> "landscape"
            Configuration.ORIENTATION_PORTRAIT -> "portrait"
            else -> "undefined"
        },
        densityDpi = resources.displayMetrics.densityDpi.toString(),
        fontScale = "%.2f".format(config.fontScale),
        uiMode = if ((config.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            "night"
        } else {
            "not-night"
        },
        widthDp = config.screenWidthDp.toString(),
        heightDp = config.screenHeightDp.toString()
    )
}

private fun Context.collectThemeCards(): List<ThemeAttrCard> {
    val alt = ContextThemeWrapper(this, R.style.Theme_HelloResourceSystemLab_Alt)
    return listOf(
        ThemeAttrCard(
            name = "resourceLabPanelColor",
            contextName = "Activity Context",
            value = resolveColorAttr(R.attr.resourceLabPanelColor),
            meaning = "当前 Activity Theme 中的面板颜色。"
        ),
        ThemeAttrCard(
            name = "resourceLabSignalColor",
            contextName = "Activity Context",
            value = resolveColorAttr(R.attr.resourceLabSignalColor),
            meaning = "当前 Activity Theme 中的信号色。"
        ),
        ThemeAttrCard(
            name = "resourceLabPanelColor",
            contextName = "ContextThemeWrapper Alt",
            value = alt.resolveColorAttr(R.attr.resourceLabPanelColor),
            meaning = "同一个 attr 在不同 Theme 中可以解析出不同值。"
        ),
        ThemeAttrCard(
            name = "resourceLabPanelColor",
            contextName = "Application Context",
            value = applicationContext.resolveColorAttr(R.attr.resourceLabPanelColor),
            meaning = "Application Context 可能缺少 Activity 级主题覆盖。"
        )
    )
}

private fun Context.collectReplacementState(mode: ReplacementMode): DynamicReplacementState {
    return DynamicReplacementState(
        mode = mode.label,
        titleResourceName = resources.getResourceName(mode.titleRes),
        titleId = mode.titleRes.toHex(),
        titleValue = getString(mode.titleRes),
        panelColorName = resources.getResourceName(mode.panelColorRes),
        panelColorId = mode.panelColorRes.toHex(),
        panelColorValue = getColorCompat(mode.panelColorRes),
        signalColorName = resources.getResourceName(mode.signalColorRes),
        signalColorId = mode.signalColorRes.toHex(),
        signalColorValue = getColorCompat(mode.signalColorRes),
        explanation = "业务槽位保持稳定，运行时只切换槽位映射到的资源 ID；这就是安全的 ResourceProvider 思路。"
    )
}

private fun Context.collectSourceCards(): List<ResourceSourceCard> {
    return listOf(
        sourceCard("app variant", R.string.resource_origin_marker, "main/debug 可以合法覆盖同名资源。"),
        sourceCard("app override", R.string.shared_action_label, "App 资源靠近最终包，适合观察 app 覆盖。"),
        sourceCard("core-design", CoreDesignResources.sharedActionLabel, "core-design 声明该资源，但 app 定义同名资源后会在最终包中覆盖它。"),
        sourceCard("feature-catalog", CatalogResources.sharedActionLabel, "业务 feature 贡献自己的资源，并依赖 core-design。"),
        sourceCard("legacy-widget", LegacyWidgetResources.sharedActionLabel, "模拟旧三方 AAR，提醒依赖资源也会进入 merge。"),
        sourceCard("feature uses design token", CatalogResources.dependencyColor, "feature 通过代码导出 core-design 的资源 ID。")
    )
}

private fun Context.collectFileCards(): List<ResourceFileCard> {
    val rawText = resources.openRawResource(R.raw.resource_lab_raw_note).bufferedReader().use { it.readText() }
    val assetText = assets.open("resource_lab_asset.json").bufferedReader().use { it.readText() }
    return listOf(
        ResourceFileCard(
            source = "res/raw/resource_lab_raw_note.txt",
            readMode = "Resources.openRawResource(R.raw.resource_lab_raw_note)",
            content = rawText.trim(),
            meaning = "raw 有资源 ID，会进入 R 和资源表。"
        ),
        ResourceFileCard(
            source = "assets/resource_lab_asset.json",
            readMode = "assets.open(\"resource_lab_asset.json\")",
            content = assetText.trim().replace('\n', ' '),
            meaning = "assets 更像路径文件，不通过 R.raw 访问。"
        )
    )
}

private fun Context.sourceCard(owner: String, resId: Int, meaning: String): ResourceSourceCard {
    return ResourceSourceCard(
        owner = owner,
        resourceName = resources.getResourceName(resId),
        idHex = resId.toHex(),
        value = when (resources.getResourceTypeName(resId)) {
            "string" -> resources.getString(resId)
            "color" -> getColorCompat(resId)
            else -> resources.getResourceEntryName(resId)
        },
        sourceMeaning = meaning
    )
}

private fun Context.resolveColorAttr(attr: Int): String {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(attr, typedValue, true)
    if (!resolved) return "not resolved"
    return if (typedValue.resourceId != 0) {
        getColorCompat(typedValue.resourceId)
    } else {
        "#%08X".format(typedValue.data)
    }
}

private fun Context.getColorCompat(resId: Int): String {
    return "#%08X".format(resources.getColor(resId, theme))
}

private fun Int.toHex(): String = "0x%08X".format(this)

private fun Int.toHexByte(): String = "0x%02X".format(this)

private fun Int.toHexEntry(): String = "0x%04X".format(this)

private enum class ReplacementMode(
    val label: String,
    val titleRes: Int,
    val panelColorRes: Int,
    val signalColorRes: Int
) {
    Default(
        label = "default",
        titleRes = R.string.resource_lab_replace_title_default,
        panelColorRes = R.color.resource_lab_replace_panel_default,
        signalColorRes = R.color.resource_lab_replace_signal_default
    ),
    Forest(
        label = "forest",
        titleRes = R.string.resource_lab_replace_title_forest,
        panelColorRes = R.color.resource_lab_replace_panel_forest,
        signalColorRes = R.color.resource_lab_replace_signal_forest
    )
}
