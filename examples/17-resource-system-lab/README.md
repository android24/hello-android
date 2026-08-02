# 示例工程：资源仓库实验室

## 对应章节

第17章 资源系统、AssetManager、Resources 与主题机制

## 工程目标

本工程用于配合第 17 章，把 `R` 文件、资源 ID、`resources.arsc`、`Resources`、`Configuration`、Theme attr、动态资源替换、动态换肤方案、`assets` / `raw`、多模块资源合并和依赖冲突放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕七个问题展开：

- `R.string.xxx` 和 `resources.arsc` 到底凭什么能对上？
- 资源 ID 的 `0xPPTTEEEE` 如何拆解？
- 同一个资源 ID 为什么会根据语言、夜间模式和密度返回不同结果？
- `R.xxx` 直接引用和 `getIdentifier` 字符串查找有什么差异？
- Theme attr 为什么和 Context 有关？
- 如何用稳定业务槽位安全地做动态资源替换？
- Theme、ResourceProvider、Configuration、外部皮肤包和 RRO 分别适合哪类换肤场景？
- app、feature、library、旧依赖模块的资源如何进入最终资源表？

## 当前效果

运行后你会看到一个“第17章 资源仓库实验室”页面：

- `资源观察分数` 用 100 分制提示当前实验进度。
- `预期 vs 实际` 展示每次资源实验的判断和证据。
- `资源身份证与 AAPT2 匹配卡` 展示 resourceId、package/type/entry、resourceName、typeName、entryName 和返回值。
- `字符串多语言实验区` 对比当前 Locale、默认资源、中文资源和英文资源的返回值。
- `图片密度实验区` 展示 drawable 资源 ID、densityDpi、密度桶和 intrinsic size。
- `R 直接引用 vs getIdentifier` 对比直接资源 ID 和字符串式动态查找。
- `混淆与 shrink 观察卡` 对比 R 直接引用、动态查找和缺失资源名的结果。
- `Configuration 面板` 展示 locale、orientation、densityDpi、fontScale、uiMode、screenWidthDp、screenHeightDp。
- `Theme attr 实验区` 对比 Activity Context、Application Context、ContextThemeWrapper 的 attr 解析结果。
- `动态资源替换实验区` 通过稳定业务槽位在两套资源 ID 之间切换。
- `动态换肤附录实验区` 提供五张可点击实验卡：Theme attr、ResourceProvider、ConfigurationContext、外部皮肤包协议模拟、RRO 系统覆盖模拟。
- `经典框架结构速览` 对照 Android-skin-support、MagicaSakura、MultipleTheme 的核心结构。
- `依赖覆盖观察卡` 展示 app/debug、app/main、core-design、feature-catalog、legacy-widget 贡献的资源。
- `依赖冲突实验区` 整理 app 覆盖 library、传递依赖、旧 AAR 和同库多版本漂移的排查证据。
- `assets / raw 实验区` 对比 `Resources.openRawResource` 和 `assets.open`。
- `资源问题诊断卡` 把 R 与资源表不匹配、release 动态资源找不到、依赖覆盖、主题异常、多语言异常变成排查清单。
- `资源事件轨迹` 记录资源读取、依赖实验和诊断动作。

这个 demo 不模拟完整 AAPT2，也不直接解析二进制 `resources.arsc`；它从 App 侧能拿到的资源 ID、反查 API、Context、Theme 和多模块资源证据出发，帮助你把资源系统的匹配规则讲清楚。

## 探索玩法

建议把自己当成资源仓库管理员，按三段路线完成。

你不是在“看几个字符串”，而是在复原一次资源从源码到运行时的旅程：

```text
res 源文件
  -> AAPT2 compile
      -> AAPT2 link
          -> R 常量
              -> resources.arsc
                  -> Resources 查表
                      -> Configuration / Theme 决定最终值
```

### 初级侦探：拆资源 ID

先完成：

```text
打开页面
  -> 查看资源身份证
      -> 记录 resourceId
          -> 拆解 package / type / entry
              -> 对照 resourceName / typeName / entryName
                  -> 点击混淆与 shrink 观察卡
```

通关判断：

```text
你能解释 R.string.resource_lab_greeting 不是字符串本身，而是一个能进入 resources.arsc 查表的整数 ID。
```

### 中级侦探：观察运行时选择

继续完成：

```text
查看 Configuration
  -> 点击字符串多语言实验区
      -> 点击图片密度实验区
          -> 切换系统语言
              -> 切换深色模式
                  -> 观察字符串、颜色和 Theme attr
                      -> 切换动态资源槽位
                          -> 打开动态换肤附录实验区
                              -> 逐张点击五种方案实验卡
                                  -> 对比事件轨迹
```

通关判断：

```text
你能解释同一个资源 ID 为什么会因为 locale、uiMode 和 Theme 返回不同结果。
```

### 高级侦探：解释依赖资源和动态查找

最后完成：

```text
对比 R 直接引用和 getIdentifier
  -> 查看依赖覆盖观察卡
      -> 点击依赖冲突实验区
          -> 记录 core-design / feature-catalog / legacy-widget 资源
              -> 阅读诊断卡
                  -> 写一份资源系统诊断报告
```

通关判断：

```text
你能把 release 动态资源找不到、依赖资源被覆盖、同名资源冲突和 Theme 读取异常分别放进正确的排查路径里。
```

## 运行方式

1. 使用 Android Studio 打开 `examples/17-resource-system-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `ResourceSystemLab`。
5. 点击刷新、多语言实验、密度实验、shrink 实验、资源槽位切换、动态换肤方案卡、依赖资源实验、依赖冲突实验、诊断卡，观察资源事件轨迹。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
17-resource-system-lab/
  app/
    src/main/java/com/helloandroid/resources/
      ResourceLabApplication.kt
      MainActivity.kt
      ResourceLabState.kt
      ResourceLabStore.kt
      ResourceLabScreen.kt
    src/main/res/
      values/
      values-en/
      values-zh/
      values-night/
      raw/
    src/main/assets/
    src/debug/res/
  feature-catalog/
  core-design/
  legacy-widget/
  quality/
    resource-system-report-template.md
    resource-system-reading-notes.md
```

## 关键源码入口

- `settings.gradle.kts`：声明 `app`、`feature-catalog`、`core-design`、`legacy-widget` 四个模块。
- `gradle.properties`：开启 `android.nonTransitiveRClass=true`。
- `app/src/main/res`：定义主资源、多语言、night、raw 和 assets。
- `app/src/debug/res`：模拟 buildType 对 main 资源的合法覆盖。
- `core-design`：模拟公共设计资源模块，并配置 `resourcePrefix = "design_"`。
- `feature-catalog`：模拟业务 feature 模块，依赖 `core-design`。
- `legacy-widget`：模拟旧三方 AAR 资源来源。
- `ResourceLabStore.kt`：集中读取资源 ID、反查资源名、拆解 ID、多语言探针、图片密度探针、Theme attr、`getIdentifier` / shrink 风险、依赖资源和 assets/raw。
- `DynamicReplacementCard`：展示业务槽位如何切换到不同资源 ID，这是 ResourceProvider 思路的最小实现。
- `SkinningAppendixCard`：动态换肤工作台，展示五种方案的实现方式、操作玩法、替换目标、替换前后证据、风险和经典框架结构。
- `LocaleProbeCard` / `DensityProbeCard` / `ShrinkProbeCard` / `DependencyConflictProbeCard`：把 17.8 中规划的四个实验区显性化。
- `ResourceLabScreen.kt`：展示资源实验页面和事件轨迹。

## 动态换肤工作台怎么玩

这部分不是完整引入某个三方框架，而是把附录里的五类方案都做成可观察实验：

```text
Theme attr
  -> 点击“演示此方案”
      -> 对比 Default / Alt / Festival 三套 Theme 下同一个 attr 的值

ResourceProvider
  -> 点击“切换资源槽位”
      -> 再点击 ResourceProvider 实验卡
          -> 观察 title / panel / signal 槽位映射到的资源 ID

ConfigurationContext
  -> 点击语言探针实验卡
      -> 对比 zh / en Context 读取同一个 R.string.resource_lab_locale_probe 的结果

外部皮肤包协议模拟
  -> 查看 assets/skin_package_manifest.json
      -> 点击实验卡
          -> 观察资源名映射、getIdentifier 结果和 fallback 风险

RRO
  -> 点击实验卡
      -> 观察 targetPackage、目标资源、overlay candidate
          -> 明确它属于系统层覆盖，不是普通业务按钮
```

这个设计故意没有直接调用隐藏 API，也没有真正安装 overlay 包。它更适合课程：能让读者看懂完整结构，同时避开普通 App 不该鼓励的系统级操作。

## 经典框架速览

这些框架适合当作结构参考，而不是要求学习者照搬：

| 框架 | 可以重点看什么 | 和本 demo 的对应关系 |
| --- | --- | --- |
| Android-skin-support | `SkinCompatManager`、Inflater、Loader Strategy、插件皮肤包、自定义 View 换肤 | 对应外部皮肤包、View 收集、资源加载策略 |
| MagicaSakura | 多主题 / 夜间主题、Tint 控件、主题色刷新 | 对应 Theme attr、Tint 和 UI 刷新 |
| MultipleTheme | 多主题资源约定、无重启切换、控件刷新 | 对应主题切换入口和刷新边界 |

## 推荐对照的 AOSP 入口

```text
frameworks/base/core/java/android/content/res/Resources.java
frameworks/base/core/java/android/content/res/AssetManager.java
frameworks/base/core/java/android/content/res/Configuration.java
frameworks/base/core/java/android/content/res/ResourcesImpl.java
frameworks/base/core/java/android/content/res/ResourcesManager.java
frameworks/base/libs/androidfw/ResourceTypes.cpp
frameworks/base/tools/aapt2/
```

## 练习任务

### 基础任务

- 记录 `R.string.resource_lab_greeting` 的资源 ID。
- 拆解 package / type / entry。
- 对比 `resources.getResourceName`、`getResourceTypeName`、`getResourceEntryName`。
- 对比 `R` 直接引用和 `getIdentifier`。
- 点击字符串多语言实验区，记录 current / default / zh / en。
- 点击图片密度实验区，记录 densityDpi、bucket 和 intrinsic size。
- 点击混淆与 shrink 观察卡，解释 missingId 为什么是 0。
- 切换动态资源槽位，记录 title、panel、signal 对应的资源 ID 和返回值。
- 点击依赖冲突实验区，写出至少两类冲突原因。
- 读取 `assets` 和 `res/raw`。
- 逐张点击动态换肤工作台的五种方案实验卡。
- 使用 `quality/resource-system-report-template.md` 写一份短报告。

### 进阶任务

- 新增一个 `values-en` 资源，观察 locale 变化。
- 新增一个 `values-night` 颜色，观察 Theme attr 变化。
- 在 `feature-catalog` 和 `legacy-widget` 中模拟同名资源，观察构建或 merged resources 证据。
- 给 library 模块新增不符合 `resourcePrefix` 的资源，观察 lint 或构建提示。
- 尝试开启资源 shrink，验证 `getIdentifier` 的风险。
- 为一个“春节皮肤”需求写出 Theme attr + ResourceProvider 的资源槽位设计。
- 参考 `assets/skin_package_manifest.json`，再新增一套“会员皮肤”协议映射。
