# 16.8 综合实践：包管理、安装与权限观察实验

第 16 章最后一节，我们把 APK 安装、Manifest 解析、Intent 查询、包可见性、签名、权限和组件状态放进一个观察实验。

目标是：让你能从一个安装包和一个 Intent，解释系统如何认识 App、如何找到组件、如何校验权限和签名。

## 本节剧情钩子

现在你已经从渲染控制台走到了包管理登记处。

你不再只问：

```text
页面为什么能打开？
```

而是继续追问：

```text
系统为什么知道这个 Activity 存在？
这个 Intent 为什么能找到它？
为什么安装失败？
为什么 Android 11 后查询不到别的 App？
为什么权限声明了却不能用？
```

本节要做的，就是把这些问题串成一张包管理地图。

你可以把自己当成包管理侦探。

第 12 章负责追踪“页面如何被调度”，第 16 章负责追踪“系统如何知道页面存在”。没有 PMS 的包信息，AMS / ATMS 就很难继续完成组件调度。

## 本节定位

本节是第 16 章综合实践。

当前配套工程是：

```text
examples/16-package-manager-lab/
```

这个工程围绕 Manifest 信息、PackageManager 查询、Intent 解析、queries、权限状态和组件 enabled 状态做成一个可观察实验室。

## 学习目标

学完本节后，你应该能够：

- 使用 PackageManager 查询当前 App 包信息。
- 查询 Activity、Service、Receiver、Provider 等组件信息。
- 观察显式 Intent 和隐式 Intent 的解析结果。
- 理解 queries 对包可见性的影响。
- 写一份包管理问题诊断报告。

## 第一部分：实践工程规划

第 16 章 demo 拆成这些可观察区域：

- `包管理观察分数`：提示实验完成度。
- `应用身份证`：展示 packageName、versionCode、targetSdk、sourceDir。
- `Manifest 组件清单`：展示 Activity、Service、Receiver、Provider。
- `Intent 解析实验区`：输入 action / data，查询候选 Activity。
- `包可见性观察`：对比 queries 配置前后的查询结果。
- `权限状态卡`：展示 declared permission、requested permission 和 runtime grant。
- `签名与安装诊断卡`：整理签名不一致、版本降级、ABI 不匹配。
- `包管理事件轨迹`：记录每次查询和诊断结论。

它的目标不是模拟完整 PMS，而是把 App 侧能观察到的包管理证据整理出来。

## 第二部分：包管理侦探通关路线

建议按三段路线完成：

```text
初级侦探：读取应用身份证
  -> 查询 packageName
      -> 查询 versionCode / targetSdk
          -> 查看 sourceDir 和 firstInstallTime

中级侦探：解析组件和 Intent
  -> 读取 Manifest 组件
      -> 构造隐式 Intent
          -> 查询 resolveActivity / queryIntentActivities
              -> 对比 exported / permission

高级侦探：解释安装、权限和可见性问题
  -> 观察权限状态
      -> 分析 signature / dangerous 权限
          -> 对比 queries
              -> 写一份包管理诊断报告
```

读者不是“看系统 API 返回值”，而是在复原系统如何认识一个 App。

## 第三部分：手动实验路线

在配套工程创建之前，也可以先用任意项目做手动实验。

准备：

- 一个带多个 Activity 的 App。
- 一个隐式 Intent。
- 一个 dangerous 权限。
- 一个想查询的第三方包名。
- 一台 Android 11+ 设备或模拟器。

观察路线：

```text
读取本 App PackageInfo
  -> 打印 ActivityInfo
      -> 构造隐式 Intent 查询候选
          -> 测试 queries 前后差异
              -> 检查权限声明和授权状态
                  -> 写诊断报告
```

## 第四部分：PackageInfo 观察

建议记录：

```text
packageName
versionCode
versionName
firstInstallTime
lastUpdateTime
applicationInfo.sourceDir
targetSdkVersion
```

你要回答：

- 这些信息来自哪里？
- 哪些来自 Manifest？
- 哪些来自安装状态？
- 哪些会随升级变化？

## 第五部分：组件查询实验

建议查询：

- ActivityInfo。
- ServiceInfo。
- ProviderInfo。
- ReceiverInfo。

观察：

- 组件是否 enabled。
- 组件是否 exported。
- 组件需要什么 permission。
- 组件是否有 intent-filter。

这组实验能帮助你把 Manifest 声明和系统查询结果连接起来。

## 第六部分：Intent 解析实验

设计几个 Intent：

```text
显式 Intent
ACTION_VIEW + https
ACTION_SEND + text/plain
自定义 action
```

观察：

- resolveActivity 返回什么。
- queryIntentActivities 返回多少候选。
- 没有 DEFAULT category 会怎样。
- data scheme / mimeType 改变后结果是否变化。

Intent 解析是 PMS 最适合做实验的地方。

## 第七部分：权限与可见性诊断卡

demo 可以把常见问题做成诊断卡：

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| Activity 找不到 | Intent 和 query 结果 | intent-filter 不匹配或包不可见 | 检查 action / category / data / queries |
| 安装失败 | adb install 错误码 | 签名、版本、ABI、APK 结构 | 对照安装阶段定位 |
| 权限被拒绝 | Permission Denial | 未授权或保护级别不满足 | 区分声明、授权、签名 |
| 外部无法调用组件 | exported / permission | 组件未导出或权限门禁 | 检查 Manifest 组件声明 |
| 查询不到第三方 App | PackageManager 返回空 | Android 11 包可见性 | 增加 queries 或使用合适 Intent 查询 |

## 第八部分：包管理诊断报告

建议报告格式：

```text
操作：
目标包名：
目标组件：
Intent 信息：
PackageManager 查询结果：
Manifest 相关配置：
权限 / 签名 / 可见性：
可能的 PMS 入口：
我的结论：
仍不确定：
```

推荐 AOSP 入口：

```text
frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java
frameworks/base/services/core/java/com/android/server/pm/InstallPackageHelper.java
frameworks/base/services/core/java/com/android/server/pm/ScanPackageUtils.java
frameworks/base/services/core/java/com/android/server/pm/ComputerEngine.java
frameworks/base/core/java/android/content/pm/PackageManager.java
frameworks/base/core/java/android/content/pm/PackageInfo.java
frameworks/base/core/java/android/content/pm/ActivityInfo.java
```

## 第九部分：本章通关检查

完成第 16 章后，请确认自己能回答：

- PMS 为什么是 Framework 核心系统服务？
- APK 安装时系统大致做了哪些事？
- Manifest 如何变成包信息和组件信息？
- Intent 解析为什么会失败？
- Android 11 包可见性影响什么？
- 签名为什么能阻止恶意覆盖安装？
- 权限声明、请求和授权有什么区别？
- 安装失败、组件找不到、权限异常应该如何分类排查？

## 本节小挑战

### 包管理侦探终局题

请为下面路径写一份包管理诊断报告：

```text
安装 debug 包
  -> 升级 release 包失败
      -> 修改签名后安装成功
          -> 隐式 Intent 查询第三方 App 返回空
              -> 增加 queries 后查到候选
                  -> 启动组件时又遇到 permission denied
```

要求写出每一步对应的包管理概念和第一证据。

## 本节实践任务

### 基础任务

- 查询当前 App 的 PackageInfo。
- 打印 ActivityInfo。
- 构造一个 ACTION_VIEW Intent 并查询候选。
- 写一份 10 行以内的包管理报告。

### 进阶任务

- 测试 Android 11+ queries。
- 模拟同包名不同签名安装失败。
- 增加一个 exported=false 组件并尝试外部启动。
- 对照源码搜索 `PackageManagerService`、`PackageInfo`、`ActivityInfo`。

## 本节小结

第 16 章把“系统如何认识 App”从 APK 文件推进到 PMS 包管理体系。你不需要一次读完 PackageManagerService，但应该已经能把安装、Manifest、组件、Intent、权限、签名、包可见性和多用户状态放在同一张地图上。到这里，Framework 入门链路不只覆盖 App 如何运行，也覆盖了 App 为什么能被系统识别和调度。
