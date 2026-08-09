# 示例工程：代码加载实验室

## 对应章节

第18章 ClassLoader、Dex、Dalvik / ART 与动态加载机制

## 工程目标

本工程用于配合第 18 章，把 `Dex`、`ClassLoader`、`DexPathList`、`dexElements`、`R8`、`Dalvik / ART`、`JNI`、`System.loadLibrary`、native so、动态加载、插件化边界和代码加载崩溃诊断放进一个可以运行、可以点击、可以复盘的小实验室。

它围绕七个问题展开：

- App 代码从源码到运行时，中间到底经过了哪些形态？
- `ClassLoader` 为什么不只是“根据类名找类”？
- `BaseDexClassLoader -> DexPathList -> dexElements -> DexFile` 这条链路如何影响热修复？
- 为什么 debug 正常，release 可能因为 R8 / keep 规则崩溃？
- Dalvik 和 ART 都执行 Dex，但它们的执行、编译和优化取舍有什么差异？
- `System.loadLibrary("foo")` 为什么会变成查找 `libfoo.so`？
- 动态加载、热修复和插件化之间是什么关系？

## 当前效果

运行后你会看到一个“第18章 代码加载实验室”页面：

- `代码加载分数` 用 100 分制提示当前实验进度。
- `预期 vs 实际` 展示每次实验的判断、证据和结论。
- `ClassLoader 身份卡` 展示 Activity、Application、业务类、系统类分别由哪个 ClassLoader 加载。
- `Dex 路径观察卡` 展示 package、sourceDir、splitSourceDirs、nativeLibraryDir、ClassLoader 链和 dexElements。
- `反射与 R8 风险卡` 对比 direct call、`Class.forName`、缺失类、路由表反射和 keep 规则。
- `真实动态加载实验` 会把 `sample-plugin-debug.apk` 从 assets 复制到私有目录，再用 `DexClassLoader` 加载插件入口。
- `热修复核心逻辑实验` 会先执行宿主里的错误结算逻辑，再从插件 APK 里加载补丁类修正结果。
- `Dalvik / ART 运行时卡` 展示当前 VM、SDK、ABI 和 Profile 相关解释。
- `native so 观察卡` 展示库名映射、nativeLibraryDir、ABI，并通过一次故意失败的 `System.loadLibrary` 观察 `UnsatisfiedLinkError`。
- `动态加载边界卡` 从 assets 中读取插件协议，解释代码、资源、so、生命周期、安全和回滚边界。
- `代码加载诊断卡` 把 `ClassNotFoundException`、`NoSuchMethodError`、`VerifyError`、`UnsatisfiedLinkError` 变成排查清单。
- `代码加载事件轨迹` 记录每次刷新、反射、native 加载和诊断动作。

这个 demo 不做危险的运行时 `dexElements` 修改，也不鼓励线上直接照搬热修复框架。但它会构建一个真实插件 APK，并在宿主运行时通过 `DexClassLoader` 加载它。热修复部分用“补丁策略替换”演示核心逻辑：宿主原逻辑有 bug，补丁类来自插件 dex，运行时优先使用补丁结果。

## 探索玩法

建议把自己当成代码引擎室的值班工程师，按三段路线完成。

### 初级侦探：认出类从哪里来

先完成：

```text
打开页面
  -> 点击刷新
      -> 对比 Activity / Application / 业务类 / 系统类的 ClassLoader
          -> 查看 ClassLoader 链
              -> 查看 sourceDir 和 nativeLibraryDir
                  -> 阅读 dexElements 观察结果
```

通关判断：

```text
你能解释：类名相同不代表类型一定相同，类型身份还和 ClassLoader 有关。
```

### 中级侦探：制造一次可解释的反射风险

继续完成：

```text
点击反射与 R8 风险卡
  -> 对比 direct call 和 Class.forName
      -> 观察 MissingRouteTable 的失败
          -> 阅读 proguard-rules.pro
              -> 思考 release 中 keep 规则保护了什么
```

通关判断：

```text
你能解释：源码存在，只说明你写过；Dex 存在、ClassLoader 可见、类名稳定，才说明运行时能找到。
```

### 高级侦探：加载一个真实插件

继续完成：

```text
点击真实动态加载实验
  -> 宿主从 assets 复制 sample-plugin-debug.apk
      -> 创建 DexClassLoader
          -> loadClass("com.helloandroid.sampleplugin.SamplePluginEntry")
              -> 强转为 plugin-contract 中的 CodePlugin
                  -> 调用 plugin.execute()
```

通关判断：

```text
你能解释：宿主和插件必须共享稳定 contract，否则类名相同也可能因为 ClassLoader 不同而无法协作。
```

### 终局侦探：理解热修复和插件化边界

最后完成：

```text
点击热修复核心逻辑实验
  -> 观察 HostCheckoutCalculator 的错误结果
      -> 从插件 APK 加载 CheckoutHotfixPatch
          -> 对比 buggy result 和 fixed result
              -> 点击 native so 观察卡
                  -> 点击动态加载边界卡
                      -> 写一份代码加载诊断报告
```

通关判断：

```text
你能解释：真实热修复通常依赖补丁 dex 的加载顺序；插件化则要比热修复多处理资源、组件生命周期、so、安全、版本和回滚。
```

## 运行方式

1. 使用 Android Studio 打开 `examples/18-code-loading-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `CodeLoadingLab`。
5. 点击刷新、反射实验、真实动态加载实验、热修复核心逻辑实验、native 加载实验、动态加载边界卡、诊断卡，观察事件轨迹。

构建 `app` 时，Gradle 会先构建 `sample-plugin`，再把 `sample-plugin-debug.apk` 复制到宿主 app 的 assets 中。如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :sample-plugin:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
18-code-loading-lab/
  app/
    src/main/java/com/helloandroid/codeloading/
      CodeLoadingLabApplication.kt
      MainActivity.kt
      ReflectionTarget.kt
      CodeLoadingState.kt
      CodeLoadingStore.kt
      CodeLoadingScreen.kt
      HostCheckoutCalculator.kt
    src/main/assets/
      plugin_manifest.json
      code_loading_notes.txt
    src/main/res/
      values/
      drawable/
    proguard-rules.pro
  plugin-contract/
    src/main/java/com/helloandroid/plugin/contract/
      CodePlugin.kt
  sample-plugin/
    src/main/java/com/helloandroid/sampleplugin/
      SamplePluginEntry.kt
  quality/
    code-loading-report-template.md
    code-loading-reading-notes.md
```

## 关键源码入口

- `CodeLoadingStore.kt`：集中收集 ClassLoader、Dex 路径、反射、动态插件、热修复、ART、native 和插件边界证据。
- `CodeLoadingScreen.kt`：展示所有实验卡片和事件轨迹。
- `ReflectionTarget.kt`：用于 direct call 和 `Class.forName` 对照。
- `HostCheckoutCalculator.kt`：宿主里的错误实现，热修复实验会用插件补丁修正它。
- `GeneratedRouteTable`：模拟路由生成类，配合字符串反射和 keep 规则观察 release 风险。
- `plugin-contract`：宿主和插件共同依赖的协议层，定义 `CodePlugin` 和 `HotfixPatch`。
- `sample-plugin`：真实插件 APK 来源，包含 `SamplePluginEntry` 和 `CheckoutHotfixPatch`。
- `proguard-rules.pro`：保护反射入口。可以临时注释后构建 release 包，观察 mapping 和运行风险。
- `plugin_manifest.json`：描述插件入口、补丁入口、资源包、so 和公共协议。
- `code-loading-report-template.md`：代码加载问题诊断报告模板。

## 关键逻辑图

```text
宿主 app
  -> 依赖 plugin-contract
  -> 构建时拿到 sample-plugin-debug.apk
  -> 运行时复制到 files/dynamic-plugins
  -> DexClassLoader(plugin.apk, codeCache, parent=宿主 ClassLoader)
      -> loadClass(SamplePluginEntry)
          -> as CodePlugin
              -> execute()
      -> loadClass(CheckoutHotfixPatch)
          -> as HotfixPatch
              -> 修正 HostCheckoutCalculator 的错误结果
```

真实热修复框架可能会继续往下做：

```text
patch.dex
  -> 插入 pathList.dexElements 前面
      -> 同名类首次查找时优先命中 patch
          -> 原类如果已经加载，修复窗口会变窄
```

本 demo 没有直接修改系统私有字段去替换 `dexElements`，但把“补丁要比原逻辑更早命中”的核心思想讲出来了。

## 推荐改造

- 增加一张 `NoSuchMethodError` 依赖版本漂移模拟卡。
- 增加一张 `VerifyError` 字节码插桩风险说明卡。
- 把 `plugin_manifest.json` 改成多个插件协议，对比不同插件的代码、资源和 so 边界。
- 增加第二个 `sample-plugin-v2`，观察同一个 contract 下如何做版本选择和回滚。
- 尝试把补丁类名写错，观察 `ClassNotFoundException` 的第一证据。
- 增加一个真实 native so，再对比“找不到 so”和“找到 so 但 JNI 方法不匹配”的差异。
- 构建 release 包，查看 `mapping.txt`，并修改 keep 规则观察反射入口变化。
