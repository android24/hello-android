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
    private var activeSkinningStrategy = SkinningStrategy.ThemeAttr

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
            localeProbe = context.collectLocaleProbe(),
            densityProbe = context.collectDensityProbe(),
            themeCards = context.collectThemeCards(),
            replacement = context.collectReplacementState(replacementMode),
            skinningWorkbench = context.collectSkinningWorkbench(activeSkinningStrategy, replacementMode),
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
            shrinkProbe = context.collectShrinkProbe(dynamicName, dynamicId),
            sourceCards = context.collectSourceCards(),
            conflictProbe = context.collectConflictProbe(),
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
                localeObserved = _state.value.score.localeObserved,
                densityObserved = _state.value.score.densityObserved,
                dynamicObserved = true,
                shrinkObserved = _state.value.score.shrinkObserved,
                dependencyObserved = true,
                conflictObserved = _state.value.score.conflictObserved,
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
                skinningWorkbench = context.collectSkinningWorkbench(activeSkinningStrategy, replacementMode),
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

    fun selectSkinningStrategy(context: Context, key: String) {
        activeSkinningStrategy = SkinningStrategy.fromKey(key)
        _state.update {
            it.copy(
                skinningWorkbench = context.collectSkinningWorkbench(activeSkinningStrategy, replacementMode),
                score = it.score.copy(appendixObserved = true),
                experiment = ResourceExperiment(
                    operation = "动态换肤方案演示：${activeSkinningStrategy.label}",
                    expected = "观察这类方案替换了资源读取链路上的哪一层。",
                    actual = activeSkinningStrategy.summary,
                    conclusion = "换肤方案的关键是让资源入口稳定、证据可追踪、失败可兜底。"
                )
            )
        }
        record("Skinning", activeSkinningStrategy.key, "PLAY", activeSkinningStrategy.summary)
    }

    fun markLocaleExperiment(context: Context) {
        val probe = context.collectLocaleProbe()
        _state.update {
            it.copy(
                localeProbe = probe,
                score = it.score.copy(localeObserved = true),
                experiment = ResourceExperiment(
                    operation = "字符串多语言实验",
                    expected = "同一个 string 资源 ID 会根据 Locale 选择不同 values 目录。",
                    actual = "current=${probe.currentValue}, zh=${probe.zhValue}, en=${probe.enValue}",
                    conclusion = "多语言问题要先看 Configuration，再看 values-xx 是否存在，最后看是否缓存了旧字符串。"
                )
            )
        }
        record("Locale", "configurationContext", "READ", probe.conclusion)
    }

    fun markDensityExperiment(context: Context) {
        val probe = context.collectDensityProbe()
        _state.update {
            it.copy(
                densityProbe = probe,
                score = it.score.copy(densityObserved = true),
                experiment = ResourceExperiment(
                    operation = "图片密度实验",
                    expected = "Resources 会根据 densityDpi 和 drawable 限定符选择最合适资源。",
                    actual = "${probe.drawableName}, density=${probe.densityDpi}, size=${probe.intrinsicSize}",
                    conclusion = "图片模糊通常不是 draw 代码坏了，而是资源密度、缩放策略或资源格式不合适。"
                )
            )
        }
        record("Density", "drawable", "READ", probe.conclusion)
    }

    fun markShrinkExperiment(context: Context) {
        val dynamicName = context.getString(R.string.resource_lab_dynamic_name)
        val dynamicId = context.resources.getIdentifier(dynamicName, "string", context.packageName)
        val probe = context.collectShrinkProbe(dynamicName, dynamicId)
        _state.update {
            it.copy(
                shrinkProbe = probe,
                score = it.score.copy(shrinkObserved = true),
                experiment = ResourceExperiment(
                    operation = "混淆与 shrink 观察实验",
                    expected = "R 直接引用依赖资源 ID；字符串式查找依赖资源名、包名、类型和 keep 规则。",
                    actual = "direct=${probe.directId}, dynamic=${probe.dynamicId}, missing=${probe.missingId}",
                    conclusion = "代码混淆通常不破坏 R ID 查表，但 shrink 和资源名混淆会让字符串式查找更危险。"
                )
            )
        }
        record("Shrink", "getIdentifier", "TRACE", probe.shrinkRisk)
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

    fun markConflictExperiment(context: Context) {
        val probe = context.collectConflictProbe()
        _state.update {
            it.copy(
                conflictProbe = probe,
                score = it.score.copy(conflictObserved = true),
                experiment = ResourceExperiment(
                    operation = "依赖冲突实验",
                    expected = "资源冲突来自身份撞车、覆盖优先级、传递依赖和多版本漂移。",
                    actual = "已整理 ${probe.cards.size} 类依赖冲突证据。",
                    conclusion = probe.conclusion
                )
            )
        }
        record("Conflict", "dependencyGraph", "TRACE", probe.conclusion)
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

private fun Context.collectLocaleProbe(): LocaleProbe {
    val defaultContext = createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.ROOT)
    })
    val zhContext = createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.SIMPLIFIED_CHINESE)
    })
    val enContext = createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.ENGLISH)
    })
    return LocaleProbe(
        resourceName = resources.getResourceName(R.string.resource_lab_locale_probe),
        currentLocale = resources.configuration.locales[0].toLanguageTag(),
        currentValue = getString(R.string.resource_lab_locale_probe),
        defaultValue = defaultContext.getString(R.string.resource_lab_locale_probe),
        zhValue = zhContext.getString(R.string.resource_lab_locale_probe),
        enValue = enContext.getString(R.string.resource_lab_locale_probe),
        conclusion = "同一个 R.string.resource_lab_locale_probe 在 zh / en ConfigurationContext 中返回不同文案。"
    )
}

private fun Context.collectDensityProbe(): DensityProbe {
    val drawable = resources.getDrawable(R.drawable.resource_density_probe, theme)
    return DensityProbe(
        drawableName = resources.getResourceName(R.drawable.resource_density_probe),
        drawableId = R.drawable.resource_density_probe.toHex(),
        densityDpi = resources.displayMetrics.densityDpi.toString(),
        densityBucket = resources.displayMetrics.densityDpi.toDensityBucket(),
        intrinsicSize = "${drawable.intrinsicWidth} x ${drawable.intrinsicHeight}",
        resourceType = resources.getResourceTypeName(R.drawable.resource_density_probe),
        conclusion = "当前 demo 使用 vector 资源，密度变化主要影响 dp 到 px 的换算；bitmap 资源还会涉及 drawable-mdpi/hdpi/xhdpi 的候选选择。"
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

private fun Context.collectSkinningWorkbench(
    active: SkinningStrategy,
    replacement: ReplacementMode
): SkinningWorkbenchState {
    val defaultThemePanel = resolveColorAttr(R.attr.resourceLabPanelColor)
    val altTheme = ContextThemeWrapper(this, R.style.Theme_HelloResourceSystemLab_Alt)
    val festivalTheme = ContextThemeWrapper(this, R.style.Theme_HelloResourceSystemLab_Festival)
    val zhContext = createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.SIMPLIFIED_CHINESE)
    })
    val enContext = createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.ENGLISH)
    })
    val mappedTitleName = "skin_pkg_spring_title"
    val mappedPanelName = "skin_pkg_spring_panel"
    val mappedTitleId = resources.getIdentifier(mappedTitleName, "string", packageName)
    val mappedPanelId = resources.getIdentifier(mappedPanelName, "color", packageName)
    val skinManifest = assets.open("skin_package_manifest.json").bufferedReader().use { it.readText() }
        .trim()
        .replace('\n', ' ')

    return SkinningWorkbenchState(
        activeStrategy = active.label,
        activeSummary = active.summary,
        evidenceCards = listOf(
            SkinningEvidenceCard(
                key = SkinningStrategy.ThemeAttr.key,
                title = "方案一：Theme / Style / Attribute",
                implementation = "用 ContextThemeWrapper 模拟切换 Theme；同一个 attr 在默认、Alt、Festival 三套 Theme 中解析出不同值。",
                operation = "点击本卡后观察 Theme attr 的解析环境。",
                target = "?attr/resourceLabPanelColor",
                before = "Default=$defaultThemePanel",
                after = "Alt=${altTheme.resolveColorAttr(R.attr.resourceLabPanelColor)}, Festival=${festivalTheme.resolveColorAttr(R.attr.resourceLabPanelColor)}",
                evidence = "代码入口：collectThemeCards() / collectSkinningWorkbench()；真实项目中 View 体系通常 recreate，Compose 则由主题状态驱动重组。",
                risk = "如果页面硬编码颜色，Theme 切了也不会生效。",
                isActive = active == SkinningStrategy.ThemeAttr
            ),
            SkinningEvidenceCard(
                key = SkinningStrategy.Provider.key,
                title = "方案二：ResourceProvider 业务槽位",
                implementation = "用 ReplacementMode 模拟 Provider：同一个 title/panel/signal 槽位根据皮肤 key 返回不同资源 ID。",
                operation = "先点“切换资源槽位”，再点本卡，观察 Provider 证据。",
                target = "titleRes / panelColorRes / signalColorRes",
                before = "当前皮肤 key=${replacement.label}",
                after = "title=${resources.getResourceName(replacement.titleRes)}, panel=${resources.getResourceName(replacement.panelColorRes)}",
                evidence = "代码入口：ReplacementMode + collectReplacementState()；真实项目可以抽成 SkinProvider.current()。",
                risk = "Provider 没有成为唯一入口时，页面会散落 if/else 和旧资源缓存。",
                isActive = active == SkinningStrategy.Provider
            ),
            SkinningEvidenceCard(
                key = SkinningStrategy.Configuration.key,
                title = "方案三：ConfigurationContext 语言探针",
                implementation = "创建 zh / en 两个 ConfigurationContext，读取同一个 R.string.resource_lab_locale_probe。",
                operation = "点击本卡，观察同一个资源 ID 在不同 Locale 下的返回值。",
                target = "R.string.resource_lab_locale_probe",
                before = "zh=${zhContext.getString(R.string.resource_lab_locale_probe)}",
                after = "en=${enContext.getString(R.string.resource_lab_locale_probe)}",
                evidence = "代码入口：createConfigurationContext(Configuration(...).setLocale(...))。",
                risk = "只创建新 Context 不代表旧 UI 自动刷新，已经缓存的字符串仍然是旧值。",
                isActive = active == SkinningStrategy.Configuration
            ),
            SkinningEvidenceCard(
                key = SkinningStrategy.ExternalPackage.key,
                title = "方案四：外部皮肤包协议模拟",
                implementation = "用 assets/skin_package_manifest.json 模拟皮肤包 manifest，再用 getIdentifier 验证资源名映射。",
                operation = "点击本卡，观察皮肤包协议、资源名映射和 fallback 证据。",
                target = "skin_pkg_spring_title / skin_pkg_spring_panel",
                before = "manifest=${skinManifest.take(180)}...",
                after = "titleId=${mappedTitleId.toHex()}, title=${if (mappedTitleId != 0) getString(mappedTitleId) else "not found"}, panel=${if (mappedPanelId != 0) getColorCompat(mappedPanelId) else "not found"}",
                evidence = "真实外部 APK 会通过额外 AssetManager 路径或加载策略取资源；本 demo 保留协议结构，避免依赖隐藏 API。",
                risk = "主包和皮肤包资源名、版本、shrink 规则不一致时，运行时查找会错位或返回 0。",
                isActive = active == SkinningStrategy.ExternalPackage
            ),
            SkinningEvidenceCard(
                key = SkinningStrategy.Overlay.key,
                title = "方案五：RRO 系统覆盖模拟",
                implementation = "展示 overlay 要覆盖的目标资源和系统侧生效条件；普通 App 只观察结构，不直接执行系统 overlay。",
                operation = "点击本卡，观察 RRO 与普通 App 内换肤的边界。",
                target = resources.getResourceName(R.color.resource_lab_primary),
                before = "base=${getColorCompat(R.color.resource_lab_primary)}",
                after = "overlay candidate=#D93654, targetPackage=$packageName",
                evidence = "真实 RRO 由系统 overlay 包、targetPackage、优先级和 Resources 解析共同决定。",
                risk = "RRO 是系统层能力，不适合作为普通业务 App 的换肤按钮。",
                isActive = active == SkinningStrategy.Overlay
            )
        )
    )
}

private fun Context.collectShrinkProbe(dynamicName: String, dynamicId: Int): ShrinkProbe {
    val missingName = "resource_lab_removed_by_shrink"
    val missingId = resources.getIdentifier(missingName, "string", packageName)
    return ShrinkProbe(
        directName = resources.getResourceName(R.string.resource_lab_greeting),
        directId = R.string.resource_lab_greeting.toHex(),
        dynamicName = dynamicName,
        dynamicId = dynamicId.toHex(),
        missingName = missingName,
        missingId = missingId.toHex(),
        shrinkRisk = if (dynamicId != 0 && missingId == 0) {
            "已命中的动态名仍可查到；不存在或被 shrink / 资源名混淆的名字会返回 0。"
        } else {
            "动态资源查找异常，请检查资源名、类型、包名、资源 shrink 和资源名混淆。"
        },
        keepAdvice = "优先 R 直接引用；必须动态查找时维护映射表，并为动态资源配置 keep 规则。"
    )
}

private fun Context.collectConflictProbe(): DependencyConflictProbe {
    return DependencyConflictProbe(
        cards = listOf(
            DependencyConflictCard(
                scenario = "app 覆盖 library 同名资源",
                trigger = "app 和 core-design 都声明 design_shared_action_label。",
                firstEvidence = "app 里 R.string.design_shared_action_label 的值是：${getString(R.string.design_shared_action_label)}",
                risk = "升级设计库后，业务以为使用了库默认文案，实际被 app 侧覆盖。",
                fixDirection = "明确 app 覆盖边界，公共模块使用 resourcePrefix，排查 merged resources。"
            ),
            DependencyConflictCard(
                scenario = "feature 依赖 core-design",
                trigger = "feature-catalog 通过代码导出 core-design 的颜色资源 ID。",
                firstEvidence = "feature dependency color=${getColorCompat(CatalogResources.dependencyColor)}",
                risk = "业务模块看到的是 feature API，最终资源却来自更底层依赖。",
                fixDirection = "排查依赖图和资源来源时同时看 feature 与 transitive dependency。"
            ),
            DependencyConflictCard(
                scenario = "旧 AAR 资源进入最终包",
                trigger = "legacy-widget 模拟历史三方库贡献资源。",
                firstEvidence = "legacy label=${getString(LegacyWidgetResources.sharedActionLabel)}",
                risk = "旧依赖资源命名过于通用，容易和 app 或新库撞名。",
                fixDirection = "隔离旧依赖，升级或替换库，并用 resourcePrefix 降低撞名概率。"
            ),
            DependencyConflictCard(
                scenario = "同库多版本漂移",
                trigger = "不同 feature 间接依赖同一设计库的不同版本。",
                firstEvidence = "本 demo 用 core-design / feature-catalog / legacy-widget 标出这种依赖图排查入口。",
                risk = "最终被 Gradle 选中的版本不符合某个业务模块的预期。",
                fixDirection = "使用 dependencyInsight、版本约束和锁定策略，再核对最终 merged resources。"
            )
        ),
        conclusion = "依赖冲突不是只搜当前模块源码，而是要把 app、variant、feature、library、transitive dependency 和最终资源表放在一起看。"
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

private fun Int.toDensityBucket(): String {
    return when {
        this <= 120 -> "ldpi"
        this <= 160 -> "mdpi"
        this <= 240 -> "hdpi"
        this <= 320 -> "xhdpi"
        this <= 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }
}

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

private enum class SkinningStrategy(
    val key: String,
    val label: String,
    val summary: String
) {
    ThemeAttr(
        key = "theme_attr",
        label = "Theme / Style / Attribute",
        summary = "替换的是 attr 解析环境，适合颜色、字体、圆角和控件默认样式。"
    ),
    Provider(
        key = "resource_provider",
        label = "ResourceProvider",
        summary = "替换的是业务槽位到资源 ID 的映射，适合文案、图标、插画成套切换。"
    ),
    Configuration(
        key = "configuration_context",
        label = "ConfigurationContext",
        summary = "替换的是资源选择时的 Configuration，适合语言、夜间模式和限定符资源验证。"
    ),
    ExternalPackage(
        key = "external_package",
        label = "外部皮肤包 / AssetManager",
        summary = "替换的是资源加载路径或加载策略，适合插件化皮肤，但协议和兼容成本高。"
    ),
    Overlay(
        key = "rro",
        label = "RRO",
        summary = "替换的是系统资源覆盖层，适合系统应用、ROM 定制和 Framework 资源覆盖。"
    );

    companion object {
        fun fromKey(key: String): SkinningStrategy {
            return values().firstOrNull { it.key == key } ?: ThemeAttr
        }
    }
}
