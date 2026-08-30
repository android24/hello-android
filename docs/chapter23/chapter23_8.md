# 23.8 综合实践：安全模型观察实验室

第 23 章最后一节，我们把 UID、沙箱、权限、AppOps、签名、Keystore、组件暴露、FileProvider、日志脱敏和安全事故排查放进一个综合实践。

目标是：让你不只是会申请权限，而是能从一个敏感能力出发，判断调用方是谁、访问的是什么、授权从哪里来、系统是否实际放行、数据是否被保护、出问题时证据在哪里。

## 本节先记住三句话

```text
Demo 不是按钮合集，而是安全判断链的可视化。
每个实验都要显示判断模块、判断输入、判断结果、系统证据和修复建议。
最终目标不是跑通功能，而是写出一份能复盘的安全诊断报告。
```

## 贯穿案例：一次课程 App 安全体检

综合实践不再拆散问题，而是把 `Hello Android 学习中心`当成一个真实 App 体检：

```text
学习提醒不弹
拍照上传失败
学习报告分享被拦
换机后 token 解不开
内部调试页被外部拉起
测试包覆盖线上包失败
日志导出含敏感字段
```

读者要做的不是逐个点按钮，而是把每个现象写成：

```text
调用方是谁？
访问对象是什么？
哪个模块参与判断？
第一证据在哪里？
修复后如何证明？
```

这就是第 23 章 Demo 的灵魂。

## 安全侦探问题

如果你只能给 Demo 首页放一个问题，我建议放这句：

```text
系统为什么允许或拒绝这个 App 做这件事？
```

所有实验都围绕这句话展开。

## 本节剧情钩子

现在你已经知道：

```text
App 有自己的 UID 和沙箱
Manifest 声明不等于用户授权
runtime permission 不是实际放行的终点
AppOps 会参与敏感操作控制
签名决定升级和信任关系
Keystore 保护的是密钥
组件 exported 和 FileProvider 都是边界上的门
```

综合实践要继续追问：

```text
一个敏感能力没有按预期工作
  -> 是调用方身份不对？
      -> 是权限没有授权？
          -> 是 AppOps 拦截？
              -> 是组件暴露或 Provider 配置错？
                  -> 是签名不一致？
                      -> 是敏感数据没有保护或脱敏？
```

本节要把这些问题做成一套安全模型观察流程。

## 本节定位

本节是第 23 章综合实践。

配套示例工程：

```text
examples/23-security-permission-lab/
```

这个工程围绕权限状态、AppOps 裁决、Photo Picker、签名升级身份、PendingIntent 授权令牌、组件 exported 观察、FileProvider 分享边界、Keystore 加密演示、安全事故剧本和诊断报告，组成一个可运行实验室。

它不是为了证明“权限弹窗能弹出来”，而是为了训练一个更重要的能力：

```text
当敏感能力没有按预期工作，或数据有泄露风险时
  -> 你能不能解释系统为什么这样判断
```

## 学习目标

学完本节后，你应该能够：

- 观察 App 的 packageName、uid、targetSdk、签名和权限状态。
- 区分 Manifest 声明、runtime permission、AppOps 和用户设置。
- 解释 FileProvider、Uri grant 和 exported 组件的安全边界。
- 设计 token、日志、导出文件的保护策略。
- 用命令收集权限、AppOps、包信息和组件信息。
- 写出一份安全事故诊断报告。

## 第一部分：实践工程规划

第 23 章 Demo 建议拆成这些可观察区域：

- `身份观察卡`：展示 packageName、uid、processName、targetSdk、dataDir。
- `权限状态卡`：展示相机、通知、照片、定位等权限声明和授权状态。
- `AppOps 裁决实验`：选择 op 和 mode，也可以粘贴命令输出解析 `allow / ignore / foreground / deny / default`。
- `相册访问与 Photo Picker 实验`：观察单个 `content://` Uri、整库媒体权限和部分照片授权的差异。
- `签名升级身份实验`：展示 debug / release 概念、证书指纹观察命令、signing lineage、安装失败剧本，并支持粘贴双 APK 证书输出做对比。
- `PendingIntent 授权令牌实验`：对比显式 immutable、mutable 和隐式入口。
- `Keystore 实验区`：生成密钥，写入加密文本，模拟恢复失败和重新初始化。
- `组件边界实验区`：展示 exported、intent-filter、Provider authority、FileProvider paths。
- `日志脱敏实验区`：输入一段含 token 的日志，输出脱敏结果。
- `安全事故剧本`：权限通过但不可用、前台定位可用后台失败、组件误暴露、日志泄露、签名不一致、FileProvider 暴露过宽。
- `安全诊断答题区`：选择问题类型、第一证据和修复动作，并填写自由文本报告获得评分反馈。
- `安全诊断报告`：输出现象、身份、权限、AppOps、签名、组件、数据保护和回归，并支持 SAF 导出。
- `体验评分机制`：提醒学习者是否完成“身份、授权、实际放行、边界、保护、复盘”。

## 第一部分补充：体验评分怎么设计

Demo 可以把学习过程拆成 16 个观察点：

| 观察点 | 得分条件 |
| --- | --- |
| 运行身份 | 记录 packageName、uid、processName、targetSdk |
| 沙箱路径 | 观察 dataDir、filesDir 和默认隔离 |
| 权限声明 | 读取 Manifest 请求权限 |
| runtime 权限 | 检查至少一个危险权限授权状态 |
| AppOps | 选择 op/mode，或粘贴命令输出解析系统裁决 |
| 签名 | 观察证书指纹，并判断同签名、签名轮换和签名不同三类升级结果 |
| 签名实测 | 粘贴 debug / release 证书输出并比较 |
| Keystore | 生成密钥并加密一段文本 |
| FileProvider | 观察 paths 配置和 content Uri |
| PendingIntent | 判断授权令牌是否显式、不可变、可回收 |
| Photo Picker | 选择一张图片，观察 Uri grant 和整库权限的差异 |
| 日志脱敏 | 把敏感日志转换成安全输出 |
| 事故剧本 | 完成至少一个安全事故判断 |
| 诊断答题 | 提交问题类型、第一证据和修复动作 |
| 自由报告 | 写出含现象、证据、根因和修复的文本报告 |
| 诊断报告 | 写出证据、原因、修复和回归 |

评分不是为了把安全做成游戏，而是为了防止只会点权限弹窗，不会解释系统判断。

## 第二部分：安全决策路线

建议按五段完成：

```text
第一段：确认身份
  -> packageName、uid、签名、安装来源、进程

第二段：确认访问对象
  -> 系统能力、用户数据、组件、Provider、Uri、密钥、日志

第三段：确认授权
  -> Manifest、runtime permission、Uri grant、signature permission、特殊权限设置

第四段：确认实际放行
  -> AppOps、targetSdk、前后台状态、系统设置、组件 exported

第五段：确认保护和复盘
  -> 加密、备份、脱敏、用户确认、撤销、日志和回归
```

这五段对应真实工作里的安全排查闭环。

## 第二部分补充：安全选择表

实践时可以先填这张表：

| 业务需求 | 第一判断 | 推荐机制 | 关键证据 |
| --- | --- | --- | --- |
| 拍照上传作业 | 敏感硬件能力 | CAMERA runtime permission | permission grant、AppOps camera |
| 发学习提醒 | 用户通知能力 | POST_NOTIFICATIONS + channel | permission、AppOps、channel |
| 分享诊断日志 | 私有文件临时共享 | FileProvider + 脱敏 | content Uri、grant flag、脱敏结果 |
| 保存登录 token | 高敏感本地数据 | Keystore + 加密存储 | key alias、密文、备份策略 |
| 内部调试 Activity | 不应外部暴露 | exported=false | manifest、dumpsys package |
| 同厂商 App 调用内部 Provider | 高信任跨包通信 | signature permission | 签名、权限、Provider |
| 用户导出学习报告 | 用户文档 | SAF + 明确提示 | 目标 Uri、导出内容说明 |
| 覆盖安装失败 | 身份不一致 | 签名和 versionCode 排查 | install error、证书指纹 |

## 第三部分：事故剧本怎么玩

安全问题最像一套门禁误会。

Demo 可以提供几个事故剧本：

| 剧本 | 表面现象 | 第一线索 | 隐藏陷阱 |
| --- | --- | --- | --- |
| 通知不弹 | 用户说权限开了 | channel 被关或 AppOps ignore | runtime permission 不是唯一闸门 |
| 组件误暴露 | 外部能打开内部页面 | exported=true + intent-filter | 入口没有最小化 |
| 日志泄露 | 诊断日志含 token | release 打印完整请求 | 证据链变泄露源 |
| 覆盖安装失败 | 测试包装不上 | 签名不同 | 包名不是唯一身份 |
| 分享日志风险 | FileProvider paths 暴露 filesDir | 分享范围过大 | content Uri 也要最小授权 |
| 后台定位失败 | 前台能定位，后台签到失败 | AppOps foreground 或后台定位缺失 | 前台成功不等于后台放行 |

玩剧本时先不要看答案，先写下自己的第一判断：

```text
这是身份问题、授权问题、AppOps 问题、组件边界问题，还是数据保护问题？
第一证据应该看哪里？
下一步要查 permission、appops、manifest、签名，还是日志内容？
这个能力是否真的需要现在申请？
```

## 第四部分：推荐观察命令与工具

包与权限：

```bash
adb shell dumpsys package com.helloandroid.security
```

AppOps：

```bash
adb shell cmd appops get com.helloandroid.security
```

进程与 UID：

```bash
adb shell ps -A | grep com.helloandroid.security
adb shell dumpsys activity processes | grep com.helloandroid.security
```

通知：

```bash
adb shell dumpsys notification
adb shell cmd appops get com.helloandroid.security POST_NOTIFICATION
```

安装和签名：

```bash
apksigner verify --print-certs app-debug.apk
adb install app-debug.apk
```

组件：

```bash
adb shell dumpsys package com.helloandroid.security | grep -i exported
adb shell dumpsys activity providers | grep com.helloandroid.security
```

日志：

```bash
adb logcat | grep -i SecurityLab
```

## 第四部分补充：命令观察顺序

排查安全问题时，可以按下面顺序：

```text
先看身份
  -> packageName、uid、签名、targetSdk

再看授权
  -> Manifest、runtime permission、Uri grant

再看实际放行
  -> AppOps、channel、设置、前后台状态

再看边界
  -> exported、Provider、FileProvider paths

最后看数据
  -> 是否加密、脱敏、备份、导出确认
```

不要一开始就说“用户没给权限”。

要把权限问题拆成系统证据。

## 第五部分：安全诊断报告模板

建议报告格式：

```text
问题标题：
用户现象：
业务场景：
Android 版本：
targetSdk：
设备 / 厂商：
packageName：
uid：
processName：
签名证书指纹：
安装来源：
访问对象：
Manifest 权限：
runtime permission：
AppOps：
前后台状态：
组件名：
exported：
intent-filter：
Provider authority：
Uri：
Uri grant：
FileProvider paths：
Keystore alias：
是否加密：
是否参与备份：
是否日志脱敏：
是否用户确认导出：
第一证据：
系统证据：
根因判断：
修复方案：
回归用例：
监控指标：
```

这份报告要回答的不是：

```text
权限有没有弹。
```

而是：

```text
系统为什么允许或拒绝这个调用方访问这项能力或数据？
```

## 第六部分：本章通关检查

完成第 23 章后，请确认自己能回答：

- UID 和 packageName 分别代表什么？
- 应用沙箱默认保护什么？
- Manifest 声明和 runtime permission 有什么区别？
- AppOps 为什么会影响已经授权的能力？
- 签名为什么决定升级和签名权限？
- Keystore 保护的是密钥还是数据？
- exported 组件为什么可能带来风险？
- FileProvider 为什么也要限制 paths？
- 日志脱敏应该处理哪些敏感字段？
- 安全事故第一证据应该看哪里？

## 第六部分补充：本章最终事故题

请分析下面这个综合事故：

```text
某课程 App 上线后，安全和测试同时反馈：

1. Android 13 上通知权限显示已开启，但学习提醒仍然不弹。
2. 一个内部调试 Activity 可以被外部 App 拉起。
3. 用户上传的诊断日志里包含完整 token 和课程资料 Uri。
4. 测试包无法覆盖线上包，提示签名不一致。
5. 分享日志使用 FileProvider，但 paths 暴露了整个 filesDir。

系统证据：

- dumpsys package 显示 POST_NOTIFICATIONS 已 granted。
- cmd appops 显示 post_notification 为 ignore。
- Manifest 中 DebugActivity exported=true 且有 intent-filter。
- 日志网关在 release 中打印了完整 Authorization header。
- 测试包使用 debug keystore，线上包使用 release signing。
- file_paths.xml 使用 files-path path="."。
```

你需要回答：

- 哪些属于授权问题，哪些属于组件边界问题，哪些属于数据保护问题？
- 通知不弹为什么不能只看 runtime permission？
- DebugActivity 应该如何改造？
- 日志脱敏应该覆盖哪些字段？
- 签名不一致为什么不能覆盖安装？
- FileProvider paths 应该如何收窄？
- 修复后如何设计回归用例和监控指标？

底线是：

```text
不能只说“权限问题”。
必须拆成身份、授权、AppOps、组件边界、签名和数据保护。
```

## 第七部分：Demo 已覆盖的实验路线

当前 `examples/23-security-permission-lab/` 已按下面路线组织。学习时建议顺着这条链路走，不要只挑权限按钮点：

```text
第一优先级：身份与权限观察
  -> 展示 packageName、uid、targetSdk、权限声明和授权状态

第二优先级：AppOps 裁决实验
  -> 选择 op/mode，从系统证据判断实际放行

第三优先级：Photo Picker 与媒体授权
  -> 区分单个 Uri grant、整库媒体权限和部分照片授权

第四优先级：签名升级身份实验
  -> debug/release、证书指纹、signing lineage、覆盖安装失败、双 APK 输出对比

第五优先级：PendingIntent、组件边界与 FileProvider
  -> exported、Provider authority、paths、Uri grant、immutable/mutable

第六优先级：Keystore 与脱敏实验
  -> 加密一段文本、脱敏 token、导出安全报告

第七优先级：安全事故剧本和诊断报告
  -> 先答题，再写自由文本报告，把实验结果变成安全判断，并通过 SAF 导出
```

不要一开始就追求模拟所有安全风险。

第 23 章 Demo 最重要的是让读者形成判断路径：

```text
身份
  -> 授权
      -> 实际放行
          -> 边界
              -> 数据保护
                  -> 诊断复盘
```

## 第七部分补充：剧情模式怎么玩

如果你第一次运行 Demo，不建议从页面中间随便点。

更好的玩法是把自己当成一次安全事故的值班工程师，按下面顺序走：

```text
第 1 步：选择事故
  -> 先在安全事故剧本里选一个现象

第 2 步：确认身份
  -> 看 packageName、uid、pid、processName、targetSdk 和签名指纹

第 3 步：确认授权
  -> 看 Manifest 声明、runtime grant 和权限说明

第 4 步：确认实际放行
  -> 选择 AppOps op/mode，或粘贴 adb 输出让 Demo 解析

第 5 步：确认边界
  -> 看 PendingIntent、exported 组件、FileProvider paths 和 Photo Picker Uri

第 6 步：确认数据保护
  -> 运行 Keystore 加密 / 解密 / 删除 key 后解密，再生成脱敏日志

第 7 步：提交诊断
  -> 在诊断答题区选择问题类型、第一证据和修复动作，再写一段自由文本报告

第 8 步：导出报告
  -> 通过 FileProvider 分享，或通过 SAF 导出到用户选择的位置
```

Demo 会用几个 UI 细节帮助你不迷路：

```text
任务板显示已完成 x / 16 和当前建议
每个关键实验区显示待完成 / 已完成
答题区在选完三段判断后才允许提交，并对自由文本报告给出 0/4 到 4/4 的反馈
诊断报告默认收起，完成答题后再展开或导出
```

这个剧情模式的重点不是“全部点亮”，而是让读者形成一种肌肉记忆：

```text
先找身份
再找授权
再找实际放行
再看入口和数据出口
最后写证据链
```

## 第八部分：Demo 的原理观察点

第 23 章 Demo 不能只做成：

```text
申请权限按钮
加密按钮
分享按钮
```

这样会退回 API 示例。

更好的设计是让每个按钮都对应一个系统判断点。

| 实验 | 观察点 | 想证明什么 |
| --- | --- | --- |
| 身份观察 | packageName、uid、targetSdk | App 安全身份不只是包名 |
| 权限检查 | Manifest、runtime grant | 声明不等于授权 |
| AppOps | appops mode | 授权后仍要看实际放行 |
| FileProvider | content Uri、paths、grant flag | 跨应用分享要最小授权 |
| exported | manifest、intent-filter | 组件暴露是安全边界 |
| Keystore | key alias、密文 | Keystore 管密钥，不是存明文 |
| 日志脱敏 | 原始日志、脱敏日志 | 证据不能变泄露源 |
| 签名剧本 | debug/release、证书指纹 | 签名决定升级身份 |
| 诊断答题 | 问题类型、第一证据、修复动作 | 先自己判断，再对照标准答案 |
| 事故报告 | 身份、授权、边界、保护、SAF 导出 | 安全问题需要证据链 |

为了让 Demo 真正体现本章精华，可以把它理解成一张“原理翻译卡”：

| 系统原理 | Demo 观察 | 读者应该说出的结论 |
| --- | --- | --- |
| UID 沙箱 | packageName、uid、dataDir | App 身份不是路径，而是 uid 和包状态 |
| DAC / MAC | 私有目录访问失败、SELinux 说明 | 文件权限和 SELinux 共同形成边界 |
| Binder 身份 | 调用方 uid / package 字段 | 系统服务不能只信任字符串参数 |
| Permission state | manifest、grant、flags | 声明、授权和用户选择是三件事 |
| AppOps note | appops mode、访问时间 | 敏感操作需要实际放行和记录 |
| Signing lineage | debug/release 指纹 | 包名相同不代表升级身份相同 |
| Keystore lifecycle | alias、密文、解密失败 | 安全设计必须考虑 key 失效和恢复 |
| Entry map | exported、FileProvider、PendingIntent | 所有入口都要问谁能进、能做什么、会带出什么 |

每个实验页面都应该显示：

```text
业务期望
调用方身份
访问对象
授权来源
AppOps / 实际放行
组件边界
数据保护
复盘结论
```

再配一个“事故解释输入框”：

```text
请不要只写：权限问题。

请写：
1. 第一证据是什么？
2. 系统在哪一层拒绝或放行？
3. 调用方身份是什么？
4. 授权来源是什么？
5. 数据是否越过了边界？
6. 修复后如何证明？
```

这样 Demo 就不只是好玩，而是在训练系统化表达。

## 第九部分：Framework / AOSP 阅读入口

如果想把第 23 章继续往 Framework 深处读，可以从这些入口开始：

```text
包与权限
  -> PackageManagerService
  -> PermissionManagerService

AppOps
  -> AppOpsService

安装与签名
  -> PackageInstallerService
  -> PackageParser / ParsingPackage
  -> SigningDetails

组件启动
  -> ActivityTaskManagerService
  -> ActivityManagerService
  -> IntentResolver

Provider 与 Uri 授权
  -> ContentProvider
  -> UriGrantsManagerService

密钥与凭据
  -> Android Keystore
  -> KeyChain / keystore2
```

建议带着具体问题进入源码：

```text
为什么这个权限 granted 但操作被拒？
为什么这个组件能被外部拉起？
为什么这个 Uri 能被另一个 App 读取？
为什么签名不同不能覆盖安装？
为什么 Keystore key 恢复后不可用？
```

源码阅读的目标不是背类名，而是把安全判断点和系统证据对上。

## 第九部分补充：第 23 章系统模块对照表

为了避免安全内容停留在概念层，可以把每个小节都落到一个“谁在判断”的问题上。

| 小节 | 核心问题 | 主要系统模块 / 机制 | 观察证据 |
| --- | --- | --- | --- |
| 23.2 沙箱与 UID | 这个进程是谁？它默认能访问什么？ | Linux uid / gid、文件权限、SELinux、Binder callingUid | `dumpsys package`、`ps -A`、`run-as`、`avc: denied` |
| 23.3 权限系统 | 这个包声明了什么？用户授权了什么？ | PackageManagerService、PermissionManagerService、权限状态与 flags | `dumpsys package`、runtime grant、`shouldShowRationale` |
| 23.4 AppOps | 这次敏感操作实际放行了吗？ | AppOpsService、AppOpsManager、check / note / start op | `cmd appops get`、隐私指示器、系统设置 |
| 23.5 签名 | 这个 APK 是否完整？它是不是旧 App 的可信升级？ | PackageInstallerService、PackageManagerService、ApkSignatureVerifier、SigningDetails、apksig | install error、证书 SHA-256、`apksigner verify` |
| 23.6 Keystore | 密钥在哪里生成、保存、使用和失效？ | Android Keystore、keystore2、KeyMint / Keymaster、TEE / StrongBox | key alias、解密异常、认证状态、备份恢复行为 |
| 23.7 组件边界 | 谁能从外部进入 App？能带入和带出什么？ | ActivityTaskManagerService、ActivityManagerService、PackageManagerService、UriGrantsManagerService | manifest exported、intent-filter、Uri grant、FileProvider paths |

你可以把它记成第 23 章的通用下钻公式：

```text
业务现象
  -> 找到访问对象
      -> 找到调用方身份
          -> 找到负责判断的系统模块
              -> 找到模块留下的证据
                  -> 再决定修复方案
```

例如签名事故：

```text
测试包覆盖不了线上包
  -> 访问对象：已安装 packageName
      -> 调用方身份：新 APK 的 signing certificate
          -> 判断模块：PackageManagerService + ApkSignatureVerifier + SigningDetails
              -> 证据：INSTALL_FAILED_UPDATE_INCOMPATIBLE、证书 SHA-256 不一致
                  -> 修复：使用正确 release signing，或按签名轮换流程处理
```

权限事故：

```text
通知权限显示开启但不弹
  -> 访问对象：post notification 操作
      -> 调用方身份：packageName + uid
          -> 判断模块：PermissionManagerService + AppOpsService + NotificationManagerService
              -> 证据：POST_NOTIFICATIONS granted、post_notification ignore、channel off
                  -> 修复：区分权限、AppOps、channel，并给出对应引导
```

Keystore 事故：

```text
备份恢复后 token 解不开
  -> 访问对象：本地密文
      -> 调用方身份：当前安装后的 App uid / package
          -> 判断模块：Keystore / keystore2 + key alias
              -> 证据：key missing、key invalidated、AEAD tag mismatch
                  -> 修复：清理密文、重新登录、重建本地索引
```

这张表已经映射到 Demo 的主要实验区：

```text
每个实验区必须回答：
  -> 哪个系统模块参与判断？
  -> 这次判断的输入是什么？
  -> 判断结果在哪里观察？
  -> 失败时第一证据是什么？
```

## 第九部分补充二：Demo 要做成“系统判断可视化”

如果第 23 章 Demo 只是按钮合集，读者会学会 API，却学不会安全模型。

建议每个实验区都固定展示五行：

```text
判断模块：
判断输入：
判断结果：
系统证据：
修复建议：
```

例如沙箱实验：

```text
判断模块：Linux uid / SELinux / Binder callingUid
判断输入：packageName、uid、dataDir、目标路径
判断结果：私有目录只能由同 uid 访问
系统证据：dumpsys package、run-as、访问失败日志
修复建议：改用 FileProvider / SAF / ContentProvider
```

权限实验：

```text
判断模块：PackageManagerService / PermissionManagerService / PermissionController
判断输入：Manifest、targetSdk、permission、用户选择
判断结果：granted、denied、partial、user fixed
系统证据：dumpsys package、权限弹窗结果、设置页状态
修复建议：最小权限、场景化请求、拒绝降级、设置页引导
```

AppOps 实验：

```text
判断模块：AppOpsService + 具体业务系统服务
判断输入：uid、packageName、op、前后台状态、attribution
判断结果：allow、ignore、foreground、default
系统证据：cmd appops get、业务 API 返回、系统设置
修复建议：区分权限和实际放行，提供前台触发或设置引导
```

Keystore 实验：

```text
判断模块：Android Keystore Provider / keystore2 / KeyMint
判断输入：alias、认证状态、密文、iv、tag、设备安全状态
判断结果：加密成功、解密成功、key missing、key invalidated、tag mismatch
系统证据：异常类型、alias 列表、恢复后解密结果
修复建议：清理密文、重新登录、密钥轮换、服务端重建
```

组件实验：

```text
判断模块：PackageManagerService / ActivityTaskManagerService / UriGrantsManagerService
判断输入：component、exported、intent-filter、permission、Uri grant、PendingIntent flags
判断结果：可启动、被拒绝、只读授权、临时授权、参数被拦截
系统证据：manifest、adb am start、Uri grant、FileProvider paths
修复建议：exported=false、signature permission、收窄 paths、immutable PendingIntent
```

最后让读者输出一份“判断链报告”：

```text
现象：
调用方：
访问对象：
判断模块：
判断输入：
系统结果：
第一证据：
根因：
修复：
回归：
```

这样 Demo 才会真正对应本章深度。

## 本节小结

第 23 章把 UID、权限、AppOps、签名、组件边界、Uri 授权和数据保护连接到了一起。

你不只是知道几个权限 API，而是要能把安全需求翻译成工程决策：

```text
业务语义
  -> 调用方身份
      -> 授权来源
          -> 实际放行
              -> 边界控制
                  -> 数据保护
                      -> 诊断复盘
```

真正成熟的安全设计，不是让权限弹窗弹出来，而是让每一次敏感访问都有身份、有边界、有授权、有证据。
