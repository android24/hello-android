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
- `R 直接引用 vs getIdentifier` 对比直接资源 ID 和字符串式动态查找。
- `Configuration 面板` 展示 locale、orientation、densityDpi、fontScale、uiMode、screenWidthDp、screenHeightDp。
- `Theme attr 实验区` 对比 Activity Context、Application Context、ContextThemeWrapper 的 attr 解析结果。
- `动态资源替换实验区` 通过稳定业务槽位在两套资源 ID 之间切换。
- `动态换肤附录实验区` 对比 Theme、ResourceProvider、Configuration、外部皮肤包和 RRO 的边界。
- `依赖覆盖观察卡` 展示 app/debug、app/main、core-design、feature-catalog、legacy-widget 贡献的资源。
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
```

通关判断：

```text
你能解释 R.string.resource_lab_greeting 不是字符串本身，而是一个能进入 resources.arsc 查表的整数 ID。
```

### 中级侦探：观察运行时选择

继续完成：

```text
查看 Configuration
  -> 切换系统语言
      -> 切换深色模式
          -> 观察字符串、颜色和 Theme attr
              -> 切换动态资源槽位
                  -> 对比 Activity Context 与 ContextThemeWrapper
                      -> 阅读动态换肤附录实验区
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
5. 点击刷新、依赖资源实验、诊断卡，观察资源事件轨迹。

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
- `ResourceLabStore.kt`：集中读取资源 ID、反查资源名、拆解 ID、读取 Theme attr、对比 `getIdentifier`、读取依赖资源和 assets/raw。
- `DynamicReplacementCard`：展示业务槽位如何切换到不同资源 ID。
- `SkinningAppendixCard`：展示动态换肤方案地图，帮助选择 Theme、ResourceProvider、Configuration、外部皮肤包或 RRO。
- `ResourceLabScreen.kt`：展示资源实验页面和事件轨迹。

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
- 切换动态资源槽位，记录 title、panel、signal 对应的资源 ID 和返回值。
- 读取 `assets` 和 `res/raw`。
- 使用 `quality/resource-system-report-template.md` 写一份短报告。

### 进阶任务

- 新增一个 `values-en` 资源，观察 locale 变化。
- 新增一个 `values-night` 颜色，观察 Theme attr 变化。
- 在 `feature-catalog` 和 `legacy-widget` 中模拟同名资源，观察构建或 merged resources 证据。
- 给 library 模块新增不符合 `resourcePrefix` 的资源，观察 lint 或构建提示。
- 尝试开启资源 shrink，验证 `getIdentifier` 的风险。
- 为一个“春节皮肤”需求写出 Theme attr + ResourceProvider 的资源槽位设计。
