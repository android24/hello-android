# 示例工程：包管理登记处实验室

## 对应章节

第16章 PMS、应用安装、包管理与权限机制

## 工程目标

本工程用于配合第 16 章，把 APK 安装后的包信息、Manifest 组件注册、Intent 解析、权限状态、签名摘要、包可见性和组件 enabled 状态放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕五个问题展开：

- 系统如何知道这个 App 的包名、版本、targetSdk 和安装路径？
- Manifest 里的 Activity、Service、Receiver、Provider 如何变成可查询的组件表？
- 一个 Intent 为什么能找到某个 Activity，又为什么有时找不到？
- Android 11+ 包可见性为什么会影响 PackageManager 查询？
- 权限声明、运行时授权、signature 权限和组件权限有什么区别？

## 当前效果

运行后你会看到一个“第16章 包管理登记处实验室”页面：

- `包管理观察分数` 用 100 分制提示当前实验进度。
- `预期 vs 实际` 会在每次实验后展示你的判断和 PackageManager 返回证据。
- `应用身份证` 展示 packageName、versionName、versionCode、targetSdk、安装时间和 sourceDir。
- `签名摘要` 展示当前安装包签名模式、签名数量和 SHA-256 短摘要。
- `Manifest 组件清单` 展示 Activity、Service、Receiver、Provider 的 exported、enabled 和 permission。
- `Intent 解析实验区` 提供显式 Intent、自定义 DeepLink、ACTION_SEND、ACTION_VIEW 和不存在 action 的对比。
- `包可见性观察` 展示自包、系统设置、分享文本候选和 https 候选的查询结果。
- `权限状态卡` 对比 dangerous/runtime 权限和 signature/custom 权限。
- `包管理问题诊断卡` 把安装失败、组件找不到、权限异常、包不可见和组件状态异常变成排查清单。
- `包管理事件轨迹` 记录每次 PackageManager 查询、组件状态切换和诊断阅读日志。

这个 demo 不模拟完整 PackageManagerService，也不修改真实系统安装流程；它从普通 App 能拿到的 PackageManager 证据出发，帮助你反推 PMS 如何认识一个 App。

## 探索玩法

建议把自己当成包管理侦探，按三段路线完成。

你不是在“看一堆 API 返回值”，而是在复原系统登记一个 App 的现场：

```text
APK 被安装
  -> Manifest 被解析
      -> 组件进入包信息表
          -> Intent 查询候选组件
              -> 权限和签名参与校验
                  -> 包可见性决定你能看到谁
```

每做完一段实验，都建议先写下自己的预期，再看 `预期 vs 实际` 和事件轨迹。PMS 很适合用“证据链”学习：先问系统凭什么知道，再看 PackageManager 给了什么证据。

### 初级侦探：读取应用身份证

先完成：

```text
打开页面
  -> 阅读应用身份证
      -> 查看 firstInstallTime / lastUpdateTime
          -> 查看 sourceDir
              -> 查看签名摘要
```

通关判断：

```text
你能区分哪些信息来自 Manifest，哪些信息来自安装状态，哪些信息来自签名校验。
```

### 中级侦探：观察组件和 Intent

继续完成：

```text
查看 Manifest 组件清单
  -> 查询显式 MainActivity
      -> 查询自定义 DeepLink
          -> 切换 DeepLinkActivity enabled
              -> 再次查询自定义 DeepLink
```

通关判断：

```text
你能解释一个 Activity 不是“写了类就能启动”，而是要被 Manifest 登记、状态可用、Intent 匹配成功。
```

### 高级侦探：解释权限和可见性

最后完成：

```text
查询 ACTION_SEND text/plain
  -> 查询 ACTION_VIEW https
      -> 阅读包可见性观察
          -> 阅读权限状态卡
              -> 阅读诊断卡
                  -> 写一份包管理诊断报告
```

通关判断：

```text
你能把“查不到 App”“组件找不到”“权限被拒绝”“安装覆盖失败”分别放到可见性、Intent、权限、签名/安装阶段里排查。
```

最小报告可以写成：

```text
操作：查询自定义 DeepLink，然后禁用 DeepLinkActivity 再查一次。
预期：组件 enabled=false 后，Intent 匹配结果会发生变化。
实际：queryIntentActivities 候选减少或为空。
事件推断：Manifest 是初始登记，组件运行态 enabled 状态会影响 PMS 的最终查询结果。
源码入口：PackageManager, PackageInfo, ActivityInfo, PackageManagerService, ComputerEngine
仍不确定：多用户场景下另一个 user 的 enabled 状态是否相同？
```

## 包管理问题诊断表

遇到包管理问题时，不要先散射式搜索。先按下面这张表找证据。

| 现象 | 先看哪条证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| Activity 找不到 | `resolveActivity` / `queryIntentActivities` | action、category、data、MIME 不匹配，或组件 disabled | 对照 intent-filter，检查 enabled 和 exported |
| 查询不到第三方 App | PackageManager 返回空 | Android 11+ 包可见性限制 | 增加最小 queries，优先用真实业务 Intent 查询 |
| 权限被拒绝 | `checkSelfPermission`、组件 permission | 未授权、保护级别不满足、签名不一致 | 区分 dangerous、normal、signature 和组件门禁 |
| 安装覆盖失败 | adb install 错误码、签名摘要 | 签名不一致、versionCode 降级、ABI 不匹配 | 对照安装阶段定位，确认签名和版本策略 |
| Manifest 有组件但不可用 | ComponentInfo enabled/exported | 组件被禁用、未导出、权限门禁 | 检查 Manifest 和 PackageManager 运行态状态 |

推荐排查顺序：

```text
包是否可见？
  -> 组件是否登记？
      -> Intent 是否匹配？
          -> 组件是否 enabled / exported？
              -> 权限和签名是否满足？
```

## 运行方式

1. 使用 Android Studio 打开 `examples/16-package-manager-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `PackageManagerLab`。
5. 点击 Intent 解析、组件 enabled、诊断卡，观察事件轨迹和 PackageManager 返回值。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
16-package-manager-lab/
  app/
    src/main/java/com/helloandroid/packagemanager/
      PackageLabApplication.kt
      MainActivity.kt
      DeepLinkActivity.kt
      PackageInspectorService.kt
      PackageChangedReceiver.kt
      LabContentProvider.kt
      PackageLabState.kt
      PackageLabStore.kt
      PackageLabScreen.kt
  quality/
    package-manager-report-template.md
    package-manager-reading-notes.md
```

## 关键源码入口

- `AndroidManifest.xml`：声明权限、queries、Activity、Service、Receiver、Provider 和 DeepLink intent-filter。
- `PackageLabStore.kt`：集中执行 `getPackageInfo`、`queryIntentActivities`、`resolveActivity`、权限检查、签名摘要和组件状态切换。
- `PackageLabState.kt`：定义应用身份证、组件卡、Intent 结果、包可见性、权限状态和事件轨迹。
- `PackageLabScreen.kt`：展示包管理实验页面、Intent 解析区、权限卡、可见性卡和诊断卡。
- `DeepLinkActivity.kt`：用于观察自定义 Intent 匹配和 signature 组件权限。
- `quality/package-manager-report-template.md`：包管理诊断报告模板。
- `quality/package-manager-reading-notes.md`：PMS / PackageManager 源码阅读建议。

## 推荐对照的 AOSP 入口

```text
frameworks/base/core/java/android/content/pm/PackageManager.java
frameworks/base/core/java/android/content/pm/PackageInfo.java
frameworks/base/core/java/android/content/pm/ActivityInfo.java
frameworks/base/core/java/android/content/pm/ResolveInfo.java
frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java
frameworks/base/services/core/java/com/android/server/pm/InstallPackageHelper.java
frameworks/base/services/core/java/com/android/server/pm/ScanPackageUtils.java
frameworks/base/services/core/java/com/android/server/pm/ComputerEngine.java
```

建议带着问题看：

```text
PackageInfo 的字段来自 Manifest 还是安装状态？
PackageParser / parsing 结果如何进入系统包信息表？
queryIntentActivities 如何使用 action、category、data、MIME 做匹配？
queries 为什么会影响 App 能查询到哪些包？
setComponentEnabledSetting 修改的是哪一类组件状态？
signature 权限为什么要求调用方签名匹配？
```

## 练习任务

### 基础任务

- 打开页面，记录 packageName、versionCode、targetSdk 和 sourceDir。
- 查看 Manifest 组件清单，找出哪个组件 exported=true。
- 查询 `显式启动 MainActivity`，观察候选组件。
- 查询 `自定义 DeepLink`，再切换 DeepLinkActivity enabled 后重新查询。
- 查询 `不存在的 action`，解释为什么候选为空。
- 阅读权限状态卡，区分声明、授权和 protection level。
- 使用 `quality/package-manager-report-template.md` 写一份短报告。

### 进阶任务

- 新增一个 Activity 和 intent-filter，观察组件清单和 Intent 查询结果如何变化。
- 移除 Manifest 中某个 queries 声明，在 Android 11+ 设备上对比查询结果。
- 为 DeepLinkActivity 去掉 signature 权限，观察组件 permission 字段变化。
- 增加一个运行时权限请求按钮，对比授权前后的权限状态卡。
