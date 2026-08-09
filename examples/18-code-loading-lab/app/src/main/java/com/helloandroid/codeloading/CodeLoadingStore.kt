package com.helloandroid.codeloading

import android.content.Context
import android.os.Build
import android.util.Log
import com.helloandroid.plugin.contract.CodePlugin
import com.helloandroid.plugin.contract.HotfixPatch
import dalvik.system.DexClassLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CodeLoadingStore {
    private const val TAG = "CodeLoadingLab"
    private const val PLUGIN_ASSET = "sample-plugin-debug.apk"
    private const val PLUGIN_ENTRY_CLASS = "com.helloandroid.sampleplugin.SamplePluginEntry"
    private const val HOTFIX_PATCH_CLASS = "com.helloandroid.sampleplugin.CheckoutHotfixPatch"
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _state = MutableStateFlow(CodeLoadingLabState())
    private var loadedPlugin: LoadedDynamicPlugin? = null

    val state: StateFlow<CodeLoadingLabState> = _state

    fun refresh(context: Context, reason: String = "读取代码加载证据") {
        val classLoaderCards = context.collectClassLoaderCards()
        val dexPathProbe = context.collectDexPathProbe()
        val runtimeProbe = collectRuntimeProbe()
        val nativeProbe = context.collectNativeProbe()
        val pluginProbe = context.collectPluginBoundaryProbe()

        _state.value = CodeLoadingLabState(
            classLoaderCards = classLoaderCards,
            dexPathProbe = dexPathProbe,
            reflectionProbe = _state.value.reflectionProbe,
            dynamicPluginProbe = _state.value.dynamicPluginProbe,
            hotfixProbe = _state.value.hotfixProbe,
            runtimeProbe = runtimeProbe,
            nativeProbe = nativeProbe,
            pluginProbe = pluginProbe,
            experiment = CodeLoadingExperiment(
                operation = reason,
                expected = "读取 ClassLoader、APK 路径、nativeLibraryDir、ART 和插件协议证据。",
                actual = "loader=${context.classLoader.shortName()}, vm=${runtimeProbe.vmName}, abi=${runtimeProbe.supportedAbis}",
                conclusion = "App 代码加载可以拆成 Dex 可见、ClassLoader 可见、运行时执行、native 加载和动态边界五组证据。"
            ),
            score = _state.value.score.copy(
                classLoaderObserved = true,
                dexPathObserved = true,
                runtimeObserved = true,
                pluginObserved = true
            ),
            eventTrail = _state.value.eventTrail
        )
        record("Runtime", "refresh", "READ", "package=${context.packageName}, loader=${context.classLoader.shortName()}")
    }

    fun runReflectionExperiment(context: Context) {
        val direct = ReflectionTarget().run()
        val targetName = ReflectionTarget::class.java.name
        val reflected = runCatching {
            val instance = Class.forName(targetName)
                .getDeclaredConstructor()
                .newInstance() as LoadableEntry
            "${instance.entryName()}: ${instance.run()}"
        }.getOrElse { "${it::class.java.simpleName}: ${it.message}" }

        val missingName = context.getString(R.string.missing_route_table_name)
        val missing = runCatching {
            Class.forName(missingName).name
        }.getOrElse { "${it::class.java.simpleName}: ${it.message}" }

        val routeName = context.getString(R.string.generated_route_table_name)
        val routeResult = runCatching {
            val table = Class.forName(routeName).getDeclaredConstructor().newInstance() as GeneratedRouteTable
            table.routes().joinToString()
        }.getOrElse { "${it::class.java.simpleName}: ${it.message}" }

        val probe = ReflectionProbe(
            directCall = direct,
            reflectionClassName = targetName,
            reflectionResult = reflected,
            missingClassName = missingName,
            missingResult = missing,
            routeTableResult = routeResult,
            minifyEnabled = BuildConfig.MINIFY_ENABLED.toString(),
            keepAdvice = "字符串反射依赖类名稳定。release 开启 R8 后，要用 keep 保护入口类、构造方法和被框架访问的成员。"
        )

        _state.update {
            it.copy(
                reflectionProbe = probe,
                score = it.score.copy(reflectionObserved = true),
                experiment = CodeLoadingExperiment(
                    operation = "反射与 R8 风险实验",
                    expected = "直接调用能被 R8 看见，字符串反射需要 keep 规则保护。",
                    actual = "direct=${probe.directCall}, reflected=${probe.reflectionResult}, missing=${probe.missingResult}",
                    conclusion = "debug 正常 release 崩溃时，要同时检查 mapping、keep、最终 dex 和反射类名。"
                )
            )
        }
        record("Reflection", "Class.forName", "TRACE", "target=$targetName, missing=$missingName")
    }

    fun loadDynamicPlugin(context: Context) {
        val probe = runCatching {
            val loaded = ensurePluginLoaded(context)
            DynamicPluginProbe(
                assetApk = PLUGIN_ASSET,
                installedPath = loaded.apkFile.absolutePath,
                entryClass = PLUGIN_ENTRY_CLASS,
                pluginClassLoader = loaded.classLoader.shortName(),
                parentClassLoader = loaded.classLoader.parent.shortName(),
                pluginDexElements = loaded.classLoader.readDexElements(),
                contractResult = "插件对象成功强转为宿主可见的 CodePlugin；contractLoader=${CodePlugin::class.java.classLoader.shortName()}",
                executeResult = loaded.plugin.execute("hello from host"),
                boundarySummary = loaded.plugin.boundaries(),
                conclusion = "动态加载闭环：宿主复制插件 APK -> DexClassLoader 创建子加载器 -> loadClass 找到入口 -> 通过父加载器里的 contract 安全调用。"
            )
        }.getOrElse { throwable ->
            DynamicPluginProbe(
                contractResult = "${throwable::class.java.simpleName}: ${throwable.message.orEmpty()}",
                conclusion = "插件加载失败时先查 APK asset 是否存在、DexClassLoader 路径、entryClass 名称和 contract 是否在父加载器可见。"
            )
        }

        _state.update {
            it.copy(
                dynamicPluginProbe = probe,
                score = it.score.copy(dynamicPluginObserved = probe.executeResult != "-"),
                experiment = CodeLoadingExperiment(
                    operation = "真实动态加载插件 APK",
                    expected = "从 assets 复制 sample-plugin-debug.apk，并用 DexClassLoader 加载 SamplePluginEntry。",
                    actual = probe.contractResult,
                    conclusion = probe.conclusion
                )
            )
        }
        record("Plugin", "DexClassLoader", "LOAD", probe.conclusion)
    }

    fun runHotfixExperiment(context: Context) {
        val price = 100
        val discount = 20
        val buggyResult = HostCheckoutCalculator.checkoutTotal(price, discount)
        val probe = runCatching {
            val loaded = ensurePluginLoaded(context)
            val patch = loaded.classLoader
                .loadClass(HOTFIX_PATCH_CLASS)
                .getDeclaredConstructor()
                .newInstance() as HotfixPatch
            val fixedResult = patch.fixedCheckoutTotal(price, discount)
            HotfixProbe(
                targetClass = patch.targetClass,
                input = "price=$price, discount=$discount",
                buggyResult = buggyResult.toString(),
                patchClass = HOTFIX_PATCH_CLASS,
                patchId = patch.patchId,
                fixedResult = fixedResult.toString(),
                dexOrderStory = patch.patchPlan(),
                conclusion = "宿主原实现返回 $buggyResult，补丁实现返回 $fixedResult。真实热修复会尽量让补丁 dex 在查找顺序中排到原 dex 前面；本实验用策略替换演示同一件事。"
            )
        }.getOrElse { throwable ->
            HotfixProbe(
                buggyResult = buggyResult.toString(),
                fixedResult = "${throwable::class.java.simpleName}: ${throwable.message.orEmpty()}",
                conclusion = "补丁加载失败时先确认插件 APK 是否存在、补丁类名是否正确、contract 是否可见、类是否已经被原实现提前加载。"
            )
        }

        _state.update {
            it.copy(
                hotfixProbe = probe,
                score = it.score.copy(hotfixObserved = probe.patchId != "-"),
                experiment = CodeLoadingExperiment(
                    operation = "热修复核心逻辑实验",
                    expected = "宿主保留一个错误实现，再从插件 APK 中加载补丁类修正结果。",
                    actual = "buggy=${probe.buggyResult}, fixed=${probe.fixedResult}",
                    conclusion = probe.conclusion
                )
            )
        }
        record("Hotfix", "patch", "APPLY", probe.conclusion)
    }

    fun runNativeExperiment(context: Context) {
        val library = context.getString(R.string.native_probe_library)
        val result = runCatching {
            System.loadLibrary(library)
            "加载成功"
        }.getOrElse { throwable ->
            "${throwable::class.java.simpleName}: ${throwable.message?.lineSequence()?.firstOrNull().orEmpty()}"
        }
        val probe = context.collectNativeProbe().copy(
            loadResult = result,
            diagnosisLayer = if (result.contains("couldn't find", ignoreCase = true) || result.contains("findLibrary", ignoreCase = true)) {
                "文件层：当前 APK 没有打包 ${System.mapLibraryName(library)}，或 nativeLibraryDir 不包含它。"
            } else {
                "继续判断依赖层或符号层：so 依赖缺失、JNI_OnLoad 失败、方法签名不匹配都可能触发同类异常。"
            }
        )
        _state.update {
            it.copy(
                nativeProbe = probe,
                score = it.score.copy(nativeObserved = true),
                experiment = CodeLoadingExperiment(
                    operation = "native so 加载实验",
                    expected = "System.loadLibrary 会把库名映射成 libxxx.so，并从 nativeLibraryDir 查找。",
                    actual = result,
                    conclusion = "UnsatisfiedLinkError 要分文件层、依赖层和符号层，不能只看异常名。"
                )
            )
        }
        record("Native", "System.loadLibrary", "ERROR", result)
    }

    fun inspectPluginBoundary(context: Context) {
        val probe = context.collectPluginBoundaryProbe(forceRead = true)
        _state.update {
            it.copy(
                pluginProbe = probe,
                score = it.score.copy(pluginObserved = true),
                experiment = CodeLoadingExperiment(
                    operation = "动态加载边界实验",
                    expected = "插件化要同时处理代码、资源、so、组件生命周期、安全和回滚。",
                    actual = probe.manifestSummary,
                    conclusion = probe.conclusion
                )
            )
        }
        record("Plugin", "boundary", "READ", probe.conclusion)
    }

    fun markDiagnosisRead(title: String) {
        _state.update {
            it.copy(
                score = it.score.copy(diagnosisObserved = true),
                experiment = it.experiment.copy(
                    operation = "阅读诊断卡：$title",
                    conclusion = "先收集第一证据，再把异常归入 Dex、ClassLoader、R8、依赖版本、ART 校验或 native 加载。"
                )
            )
        }
        record("Diagnosis", title, "READ", "diagnostic card inspected")
    }

    fun markReportReady() {
        _state.update {
            it.copy(
                score = it.score.copy(reportReady = true),
                experiment = it.experiment.copy(
                    operation = "代码加载诊断报告",
                    conclusion = "已经具备写报告的证据：ClassLoader、dexElements、R8、ABI、nativeLibraryDir 和插件边界。"
                )
            )
        }
        record("Report", "codeLoadingReport", "READY", "report evidence collected")
    }

    fun clearEvents() {
        _state.update { it.copy(eventTrail = emptyList()) }
        Log.d(TAG, "Events cleared")
    }

    private fun Context.collectClassLoaderCards(): List<ClassLoaderCard> {
        val appContext = applicationContext
        return listOf(
            ClassLoaderCard(
                label = "Activity Context",
                className = MainActivity::class.java.name,
                loaderName = MainActivity::class.java.classLoader.shortName(),
                parentName = MainActivity::class.java.classLoader?.parent.shortName(),
                meaning = "Activity 属于应用代码，通常由 PathClassLoader 从已安装 APK 中加载。"
            ),
            ClassLoaderCard(
                label = "Application Context",
                className = appContext::class.java.name,
                loaderName = appContext::class.java.classLoader.shortName(),
                parentName = appContext::class.java.classLoader?.parent.shortName(),
                meaning = "Application 与业务类一般共享应用 ClassLoader。"
            ),
            ClassLoaderCard(
                label = "业务类",
                className = ReflectionTarget::class.java.name,
                loaderName = ReflectionTarget::class.java.classLoader.shortName(),
                parentName = ReflectionTarget::class.java.classLoader?.parent.shortName(),
                meaning = "业务类能否被找到，取决于它是否进入 dex，且当前 ClassLoader 是否可见。"
            ),
            ClassLoaderCard(
                label = "系统类",
                className = String::class.java.name,
                loaderName = String::class.java.classLoader.shortName(),
                parentName = String::class.java.classLoader?.parent.shortName(),
                meaning = "系统核心类通常来自 BootClassLoader，应用不能随意覆盖它。"
            )
        )
    }

    private fun Context.collectDexPathProbe(): DexPathProbe {
        val info = applicationInfo
        val splitDirs = info.splitSourceDirs?.joinToString("\n") ?: "no split apk"
        val classLoader = classLoader
        val dexElements = classLoader.readDexElements()
        return DexPathProbe(
            packageName = packageName,
            sourceDir = info.sourceDir.orEmpty(),
            splitSourceDirs = splitDirs,
            nativeLibraryDir = info.nativeLibraryDir.orEmpty(),
            classLoaderChain = classLoader.readClassLoaderChain(),
            dexElements = dexElements,
            status = if (dexElements.isEmpty()) {
                "当前系统限制或实现差异导致无法反射 dexElements，但 sourceDir 和 ClassLoader 链路仍可观察。"
            } else {
                "已读取 dexElements。注意：顺序会影响同名类的首次命中。"
            },
            evidence = "loadClass -> parent -> findClass -> DexPathList -> dexElements -> DexFile"
        )
    }

    private fun collectRuntimeProbe(): RuntimeProbe {
        val vmName = System.getProperty("java.vm.name").orEmpty()
        val vmVersion = System.getProperty("java.vm.version").orEmpty()
        return RuntimeProbe(
            vmName = vmName.ifBlank { "Dalvik / ART" },
            vmVersion = vmVersion.ifBlank { "-" },
            sdkInt = Build.VERSION.SDK_INT.toString(),
            supportedAbis = Build.SUPPORTED_ABIS.joinToString(),
            strategy = "现代 Android 主要由 ART 负责加载、校验、解释执行、JIT、AOT 和 Profile 引导优化。",
            profileHint = "Baseline Profile 不是替代业务优化，它帮助 ART 更早知道启动和关键路径上的热点代码。"
        )
    }

    private fun Context.collectNativeProbe(): NativeProbe {
        val library = getString(R.string.native_probe_library)
        return NativeProbe(
            requestedLibrary = library,
            mappedLibraryName = System.mapLibraryName(library),
            nativeLibraryDir = applicationInfo.nativeLibraryDir.orEmpty(),
            supportedAbis = Build.SUPPORTED_ABIS.joinToString(),
            loadResult = _state.value.nativeProbe.loadResult,
            diagnosisLayer = _state.value.nativeProbe.diagnosisLayer
        )
    }

    private fun Context.collectPluginBoundaryProbe(forceRead: Boolean = false): PluginBoundaryProbe {
        val manifest = runCatching {
            assets.open("plugin_manifest.json").bufferedReader().use { it.readText() }
        }.getOrElse { "plugin_manifest.json read failed: ${it.message}" }
        val summary = if (forceRead) {
            manifest.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .take(8)
                .joinToString(" ")
        } else {
            "assets/plugin_manifest.json 描述了插件入口、资源包、native 库和公共协议。"
        }
        return PluginBoundaryProbe(
            manifestSummary = summary,
            entryBoundary = "代码入口：DexClassLoader 能加载类，但公共 API 最好由宿主或公共模块定义。",
            resourceBoundary = "资源边界：插件资源需要独立 Resources / AssetManager 策略，不能只靠 R.id。",
            nativeBoundary = "so 边界：插件 native 库要选择 ABI、解压路径、依赖顺序和加载时机。",
            lifecycleBoundary = "组件边界：Activity / Service 生命周期需要宿主代理或系统注册策略。",
            safetyBoundary = "安全边界：签名、版本、灰度、回滚和崩溃隔离都要纳入设计。",
            conclusion = "如果只回答 DexClassLoader，那还是动态加载 demo；能闭合代码、资源、组件、so、安全，才接近插件化工程。"
        )
    }

    private fun ensurePluginLoaded(context: Context): LoadedDynamicPlugin {
        loadedPlugin?.let { return it }
        val apkFile = context.copyAssetToPrivateFile(PLUGIN_ASSET, File(context.filesDir, "dynamic-plugins"))
        val optimizedDir = File(context.codeCacheDir, "plugin-optimized").apply { mkdirs() }
        val classLoader = DexClassLoader(
            apkFile.absolutePath,
            optimizedDir.absolutePath,
            null,
            context.classLoader
        )
        val plugin = classLoader
            .loadClass(PLUGIN_ENTRY_CLASS)
            .getDeclaredConstructor()
            .newInstance() as CodePlugin
        return LoadedDynamicPlugin(apkFile, classLoader, plugin).also {
            loadedPlugin = it
        }
    }

    private fun Context.copyAssetToPrivateFile(assetName: String, targetDir: File): File {
        targetDir.mkdirs()
        val target = File(targetDir, assetName)
        if (target.exists()) {
            target.delete()
        }
        assets.open(assetName).use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        target.setReadOnly()
        target.setWritable(false, false)
        return target
    }

    private fun ClassLoader?.readClassLoaderChain(): List<String> {
        val chain = mutableListOf<String>()
        var current = this
        while (current != null) {
            chain += current.shortName()
            current = current.parent
        }
        chain += "BootClassLoader"
        return chain
    }

    private fun ClassLoader?.readDexElements(): List<String> {
        if (this == null) return emptyList()
        return runCatching {
            val pathListField = findField(this.javaClass, "pathList")
            pathListField.isAccessible = true
            val pathList = pathListField.get(this)
            val elementsField = findField(pathList.javaClass, "dexElements")
            elementsField.isAccessible = true
            val elements = elementsField.get(pathList) as? Array<*> ?: return emptyList()
            elements.mapIndexed { index, element ->
                "[$index] ${element?.toString().orEmpty()}"
            }
        }.getOrElse {
            listOf("无法读取 dexElements：${it::class.java.simpleName}: ${it.message.orEmpty()}")
        }
    }

    private fun findField(type: Class<*>, name: String): java.lang.reflect.Field {
        var current: Class<*>? = type
        while (current != null) {
            runCatching { return current.getDeclaredField(name) }
            current = current.superclass
        }
        error("field $name not found in ${type.name}")
    }

    private fun ClassLoader?.shortName(): String {
        if (this == null) return "BootClassLoader"
        return this::class.java.name.substringAfterLast('.')
    }

    private fun record(source: String, phase: String, signal: String, detail: String) {
        Log.d(TAG, "$source.$phase $signal: $detail")
        val event = CodeLoadingEvent(
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

    private data class LoadedDynamicPlugin(
        val apkFile: File,
        val classLoader: DexClassLoader,
        val plugin: CodePlugin
    )
}
