package com.helloandroid.codeloading

data class CodeLoadingLabState(
    val classLoaderCards: List<ClassLoaderCard> = emptyList(),
    val dexPathProbe: DexPathProbe = DexPathProbe(),
    val reflectionProbe: ReflectionProbe = ReflectionProbe(),
    val dynamicPluginProbe: DynamicPluginProbe = DynamicPluginProbe(),
    val hotfixProbe: HotfixProbe = HotfixProbe(),
    val runtimeProbe: RuntimeProbe = RuntimeProbe(),
    val nativeProbe: NativeProbe = NativeProbe(),
    val pluginProbe: PluginBoundaryProbe = PluginBoundaryProbe(),
    val diagnosticCards: List<CodeLoadingDiagnosticCard> = defaultDiagnosticCards,
    val experiment: CodeLoadingExperiment = CodeLoadingExperiment(),
    val score: CodeLoadingScore = CodeLoadingScore(),
    val eventTrail: List<CodeLoadingEvent> = emptyList()
)

data class ClassLoaderCard(
    val label: String,
    val className: String,
    val loaderName: String,
    val parentName: String,
    val meaning: String
)

data class DexPathProbe(
    val packageName: String = "-",
    val sourceDir: String = "-",
    val splitSourceDirs: String = "-",
    val nativeLibraryDir: String = "-",
    val classLoaderChain: List<String> = emptyList(),
    val dexElements: List<String> = emptyList(),
    val status: String = "等待观察 ClassLoader 与 DexPathList。",
    val evidence: String = "点击 ClassLoader 实验后，观察 sourceDir、nativeLibraryDir 和 dexElements。"
)

data class ReflectionProbe(
    val directCall: String = "-",
    val reflectionClassName: String = "-",
    val reflectionResult: String = "-",
    val missingClassName: String = "-",
    val missingResult: String = "-",
    val routeTableResult: String = "-",
    val minifyEnabled: String = "-",
    val keepAdvice: String = "等待反射与 R8 实验。"
)

data class DynamicPluginProbe(
    val assetApk: String = "sample-plugin-debug.apk",
    val installedPath: String = "-",
    val entryClass: String = "com.helloandroid.sampleplugin.SamplePluginEntry",
    val pluginClassLoader: String = "-",
    val parentClassLoader: String = "-",
    val pluginDexElements: List<String> = emptyList(),
    val contractResult: String = "尚未加载插件。",
    val executeResult: String = "-",
    val boundarySummary: List<String> = emptyList(),
    val conclusion: String = "等待动态加载实验。"
)

data class HotfixProbe(
    val targetClass: String = "HostCheckoutCalculator",
    val input: String = "price=100, discount=20",
    val buggyResult: String = "-",
    val patchClass: String = "com.helloandroid.sampleplugin.CheckoutHotfixPatch",
    val patchId: String = "-",
    val fixedResult: String = "-",
    val dexOrderStory: String = "等待热修复实验。",
    val conclusion: String = "热修复的关键不是魔法，而是让补丁逻辑比原逻辑更早命中。"
)

data class RuntimeProbe(
    val vmName: String = "-",
    val vmVersion: String = "-",
    val sdkInt: String = "-",
    val supportedAbis: String = "-",
    val strategy: String = "等待 Dalvik / ART 观察。",
    val profileHint: String = "-"
)

data class NativeProbe(
    val requestedLibrary: String = "-",
    val mappedLibraryName: String = "-",
    val nativeLibraryDir: String = "-",
    val supportedAbis: String = "-",
    val loadResult: String = "尚未触发 native 加载实验。",
    val diagnosisLayer: String = "等待判断：文件层、依赖层，还是符号层。"
)

data class PluginBoundaryProbe(
    val manifestSummary: String = "等待读取插件协议。",
    val entryBoundary: String = "-",
    val resourceBoundary: String = "-",
    val nativeBoundary: String = "-",
    val lifecycleBoundary: String = "-",
    val safetyBoundary: String = "-",
    val conclusion: String = "插件化不是只加载一个 dex。"
)

data class CodeLoadingDiagnosticCard(
    val title: String,
    val symptom: String,
    val firstEvidence: String,
    val fixDirection: String
)

data class CodeLoadingExperiment(
    val operation: String = "等待代码加载实验",
    val expected: String = "先猜测：这次操作要验证 Dex、ClassLoader、R8、ART、so，还是插件边界？",
    val actual: String = "点击实验卡后收集运行时证据。",
    val conclusion: String = "还没有证据。"
)

data class CodeLoadingScore(
    val classLoaderObserved: Boolean = false,
    val dexPathObserved: Boolean = false,
    val reflectionObserved: Boolean = false,
    val dynamicPluginObserved: Boolean = false,
    val hotfixObserved: Boolean = false,
    val runtimeObserved: Boolean = false,
    val nativeObserved: Boolean = false,
    val pluginObserved: Boolean = false,
    val diagnosisObserved: Boolean = false,
    val reportReady: Boolean = false
)

data class CodeLoadingEvent(
    val source: String,
    val phase: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val defaultDiagnosticCards = listOf(
    CodeLoadingDiagnosticCard(
        title = "ClassNotFoundException",
        symptom = "反射、路由或插件入口找不到类。",
        firstEvidence = "类名、APK / dex 内容、当前 ClassLoader、dexElements 顺序。",
        fixDirection = "确认类是否打进 dex，检查 R8 keep、动态 dex 加载路径和加载时机。"
    ),
    CodeLoadingDiagnosticCard(
        title = "NoSuchMethodError",
        symptom = "编译期存在的方法，运行时调用失败。",
        firstEvidence = "方法签名、依赖树、运行时实际版本、调用方和被调用方 dex。",
        fixDirection = "统一依赖版本，清理传递依赖漂移，确认 release 产物和 debug 是否一致。"
    ),
    CodeLoadingDiagnosticCard(
        title = "VerifyError",
        symptom = "类或字节码校验失败，常见于插桩、热修复或兼容性问题。",
        firstEvidence = "设备版本、字节码改写链路、R8 / ASM / 热修复框架版本。",
        fixDirection = "检查字节码结构、方法签名、父类接口变化，以及不同 Android 版本限制。"
    ),
    CodeLoadingDiagnosticCard(
        title = "UnsatisfiedLinkError",
        symptom = "找不到 so，或 so 已加载但找不到 native 方法。",
        firstEvidence = "库名、ABI、nativeLibraryDir、APK lib 目录、JNI 方法签名。",
        fixDirection = "按文件层、依赖层、符号层分层排查，补 ABI、修加载路径或保护 JNI 入口。"
    )
)
