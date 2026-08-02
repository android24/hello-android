package com.helloandroid.resources

data class ResourceLabState(
    val identity: ResourceIdentity = ResourceIdentity(),
    val configuration: ConfigurationSnapshot = ConfigurationSnapshot(),
    val themeCards: List<ThemeAttrCard> = emptyList(),
    val replacement: DynamicReplacementState = DynamicReplacementState(),
    val skinningStrategies: List<SkinningStrategyCard> = defaultSkinningStrategyCards,
    val dynamicLookup: DynamicLookup = DynamicLookup(),
    val sourceCards: List<ResourceSourceCard> = emptyList(),
    val fileCards: List<ResourceFileCard> = emptyList(),
    val diagnosticCards: List<ResourceDiagnosticCard> = defaultResourceDiagnosticCards,
    val experiment: ResourceExperiment = ResourceExperiment(),
    val score: ResourceLabScore = ResourceLabScore(),
    val eventTrail: List<ResourceEventLog> = emptyList()
)

data class ResourceIdentity(
    val resourceIdHex: String = "-",
    val packagePart: String = "-",
    val typePart: String = "-",
    val entryPart: String = "-",
    val resourceName: String = "-",
    val typeName: String = "-",
    val entryName: String = "-",
    val value: String = "-",
    val aaptLinkStory: String = "等待读取 R 常量和 resources.arsc 匹配证据。"
)

data class ConfigurationSnapshot(
    val locale: String = "-",
    val orientation: String = "-",
    val densityDpi: String = "-",
    val fontScale: String = "-",
    val uiMode: String = "-",
    val widthDp: String = "-",
    val heightDp: String = "-"
)

data class ThemeAttrCard(
    val name: String,
    val contextName: String,
    val value: String,
    val meaning: String
)

data class DynamicReplacementState(
    val mode: String = "default",
    val titleResourceName: String = "-",
    val titleId: String = "-",
    val titleValue: String = "-",
    val panelColorName: String = "-",
    val panelColorId: String = "-",
    val panelColorValue: String = "-",
    val signalColorName: String = "-",
    val signalColorId: String = "-",
    val signalColorValue: String = "-",
    val explanation: String = "等待动态资源替换实验。"
)

data class SkinningStrategyCard(
    val name: String,
    val replaceTarget: String,
    val bestFor: String,
    val firstRisk: String,
    val evidence: String
)

data class DynamicLookup(
    val requestedName: String = "-",
    val directId: String = "-",
    val dynamicId: String = "-",
    val directValue: String = "-",
    val dynamicValue: String = "-",
    val conclusion: String = "等待对比 R 直接引用和 getIdentifier。"
)

data class ResourceSourceCard(
    val owner: String,
    val resourceName: String,
    val idHex: String,
    val value: String,
    val sourceMeaning: String
)

data class ResourceFileCard(
    val source: String,
    val readMode: String,
    val content: String,
    val meaning: String
)

data class ResourceDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

data class ResourceExperiment(
    val operation: String = "等待资源系统实验",
    val expected: String = "先猜测：这次操作会验证资源 ID、Configuration、Theme，还是依赖覆盖？",
    val actual: String = "点击实验按钮后观察资源表证据和事件轨迹。",
    val conclusion: String = "还没有证据。"
)

data class ResourceLabScore(
    val idObserved: Boolean = false,
    val configurationObserved: Boolean = false,
    val themeObserved: Boolean = false,
    val replacementObserved: Boolean = false,
    val dynamicObserved: Boolean = false,
    val dependencyObserved: Boolean = false,
    val fileObserved: Boolean = false,
    val diagnosisObserved: Boolean = false,
    val appendixObserved: Boolean = false,
    val reportObserved: Boolean = false
)

data class ResourceEventLog(
    val source: String,
    val phase: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val defaultSkinningStrategyCards = listOf(
    SkinningStrategyCard(
        name = "Theme / Style / Attribute",
        replaceTarget = "替换 attr 解析环境",
        bestFor = "深色模式、节日主题、会员色彩、多品牌视觉。",
        firstRisk = "页面绕过 attr，直接写死颜色或 drawable。",
        evidence = "对比 Activity Context、Application Context、ContextThemeWrapper 的 attr 返回值。"
    ),
    SkinningStrategyCard(
        name = "ResourceProvider",
        replaceTarget = "替换业务槽位到资源 ID 的映射",
        bestFor = "文案、图标、插画、运营素材成套切换。",
        firstRisk = "Provider 不统一，页面散落 if/else 或缓存旧资源。",
        evidence = "切换资源槽位后，观察 title、panel、signal 的资源 ID 和返回值。"
    ),
    SkinningStrategyCard(
        name = "ConfigurationContext",
        replaceTarget = "替换资源选择所处的 Configuration",
        bestFor = "App 内语言、夜间模式、横竖屏和限定符资源验证。",
        firstRisk = "只换了 Context，没有让 UI 重新读取资源。",
        evidence = "记录 locale、uiMode、densityDpi，再对比同一个资源 ID 的返回值。"
    ),
    SkinningStrategyCard(
        name = "外部皮肤包 / AssetManager",
        replaceTarget = "追加外部资源路径并按协议查找",
        bestFor = "大型 App 的插件化皮肤和在线完整视觉包。",
        firstRisk = "主包与皮肤包资源名、版本、shrink 规则不一致。",
        evidence = "检查皮肤包版本、资源映射表、getIdentifier 结果和缺失兜底。"
    ),
    SkinningStrategyCard(
        name = "RRO",
        replaceTarget = "系统层覆盖目标包资源",
        bestFor = "系统应用、ROM 定制、Framework 组件主题覆盖。",
        firstRisk = "把系统工程能力误用成普通业务 App 的换肤开关。",
        evidence = "确认 overlay 包、目标包、优先级和系统资源解析结果。"
    )
)

val defaultResourceDiagnosticCards = listOf(
    ResourceDiagnosticCard(
        title = "R 与 resources.arsc 对不上",
        symptom = "资源 ID 查不到、类型不匹配或使用了错误包的资源 ID。",
        firstEvidence = "记录资源 ID、package/type/entry、resourceName 和 APK 内容。",
        fixDirection = "确认 R 常量来自同一次构建，避免跨包硬塞资源 ID。"
    ),
    ResourceDiagnosticCard(
        title = "release 动态资源找不到",
        symptom = "debug 正常，release 通过 getIdentifier 返回 0。",
        firstEvidence = "检查资源 shrink、资源名混淆、动态资源名和 keep 规则。",
        fixDirection = "优先使用 R 引用；动态资源维护映射表和 keep 规则。"
    ),
    ResourceDiagnosticCard(
        title = "依赖资源被覆盖",
        symptom = "升级库或切换 buildType 后颜色、文案或样式变化。",
        firstEvidence = "查 merged resources、依赖树、AAR res 目录和最终 APK。",
        fixDirection = "统一依赖版本，使用 resourcePrefix，明确 app 覆盖 library 的边界。"
    ),
    ResourceDiagnosticCard(
        title = "主题属性读取异常",
        symptom = "Activity 能读到主题色，Application Context 读不到或读到默认值。",
        firstEvidence = "对比 Activity、Application、ContextThemeWrapper 的 attr 解析结果。",
        fixDirection = "UI 相关资源使用带正确 Theme 的 Context，避免硬编码颜色。"
    ),
    ResourceDiagnosticCard(
        title = "动态资源替换失效",
        symptom = "切换皮肤后，部分控件仍然使用旧文案、旧颜色或旧图片。",
        firstEvidence = "检查资源是否经过 Theme attr / ResourceProvider，是否缓存旧资源值。",
        fixDirection = "让可替换资源走统一槽位；切换后触发重组或重新读取资源。"
    ),
    ResourceDiagnosticCard(
        title = "多语言或 night 资源未生效",
        symptom = "切换语言或深色模式后，页面仍显示旧资源。",
        firstEvidence = "查看 Configuration、values-xx、values-night、是否缓存旧字符串。",
        fixDirection = "使用正确 Context 重新读取资源，避免把文案永久缓存到单例。"
    )
)
