# 23.1 为什么要学习 Android 安全模型、权限、签名、AppOps 与数据保护

第 22 章我们把存储问题拆成了：

```text
数据归属
  -> 存储入口
      -> 授权状态
          -> 系统索引
              -> 生命周期
                  -> 诊断复盘
```

第 23 章继续往下追问：

```text
系统凭什么相信这个 App？
这个 App 凭什么访问这份数据？
用户授权之后，系统还会不会继续拦？
签名为什么能决定升级、共享 UID 和组件信任？
敏感数据为什么不能只靠“放在私有目录”保护？
```

这不是安全工程师才需要懂的内容。

只要你写 Android App，就会碰到：

```text
权限申请被拒
后台定位无法使用
相册权限只给了部分照片
通知权限打开了但仍然不弹
分享文件被拦截
升级包安装失败
测试包覆盖不了线上包
token 恢复后失效
导出的日志泄露用户信息
```

这些问题表面上很分散，底层都和 Android 安全模型有关。

## 本章通关画面

学完第 23 章后，你应该能画出这张安全判断链：

```text
App 想访问一个能力或数据
  -> 先看进程、UID 和沙箱边界
      -> 再看 Manifest 声明和组件暴露
          -> 再看 runtime permission 是否授权
              -> 再看 AppOps 是否允许实际操作
                  -> 再看签名、证书和安装来源是否可信
                      -> 再看敏感数据是否加密、脱敏、可撤销和可追踪
                          -> 最后形成安全、隐私和合规的工程方案
```

如果第 22 章像资料室，第 23 章就是门禁系统。

资料室告诉你：

```text
文件放在哪个柜子
```

门禁系统继续问：

```text
谁在开柜子？
这张卡是谁发的？
有没有过期？
是不是只能开这一格？
开门动作有没有被记录？
里面有没有不该带出去的东西？
```

这就是 Android 安全模型的味道：身份、边界、授权、审计和最小化。

## 本章核心判断：安全五问

面对任何敏感能力，先问五句话：

```text
第一问：调用方是谁？进程、UID、包名、签名分别是什么？
第二问：访问的是什么？系统能力、用户数据、组件入口，还是本地密钥？
第三问：授权从哪里来？Manifest、runtime permission、Uri grant、签名权限，还是用户设置？
第四问：系统实际有没有放行？AppOps、targetSdk、后台限制、组件 exported 是否拦截？
第五问：出问题时能否复盘？日志、审计、脱敏、撤销、回归是否完整？
```

例如：

| 场景 | 第一判断 | 关键机制 | 第一证据 |
| --- | --- | --- | --- |
| 读取通讯录 | 用户敏感数据 | runtime permission + AppOps | permission grant、appops |
| 后台定位 | 高敏感能力 | foreground/background location + AppOps | 权限状态、前后台状态 |
| 分享私有文件 | 跨应用数据共享 | FileProvider + Uri grant | content Uri、grant flag |
| 覆盖安装失败 | App 身份不一致 | signing certificate | 安装错误、签名证书 |
| 保存 token | 敏感本地数据 | Keystore / 加密存储 | key alias、备份策略 |
| 暴露 Activity | 组件入口 | exported / intent-filter | manifest、dumpsys package |
| 导出日志 | 可能含隐私 | 脱敏和用户确认 | 日志内容、导出路径 |

安全设计的第一步，不是“申请权限”，而是识别身份和边界。

## 本章读法：先拿地图，再进机房

第 23 章内容偏硬，不建议一口气把所有系统模块都啃完。

你可以按三层阅读：

```text
第一层：基础必读
  -> 先记住每节的三句话
  -> 看懂常见事故
  -> 能说出第一证据在哪里

第二层：工程必会
  -> 能按身份、边界、授权、实际放行、数据保护写诊断报告
  -> 能用 dumpsys、appops、apksigner、日志和设置页取证

第三层：深入选读
  -> 再看 PackageManagerService、PermissionManagerService、AppOpsService、SigningDetails、keystore2 等系统模块
  -> 理解系统到底在哪一层做判断
```

如果你第一次学习，先完成第一层。

如果你准备面试、做安全治理或读 Framework，再进入第二层和第三层。

不要因为看见系统服务名字就觉得自己“还没学会”。资深工程能力不是一次读懂所有源码，而是知道问题应该往哪条链路继续追。

## 本章贯穿案例：Hello Android 学习中心安全体检

本章用同一个课程 App 贯穿所有小节。

这个 App 叫：

```text
Hello Android 学习中心
```

它有几个真实功能：

```text
学习提醒
  -> 需要通知权限、通知渠道和 AppOps

拍照上传作业
  -> 需要相机、照片选择和 Uri 授权

学习报告导出
  -> 需要 SAF、FileProvider 和日志脱敏

账号登录
  -> 需要 token 加密、Keystore 和备份恢复策略

内部调试面板
  -> 需要组件 exported、签名权限和调用方校验

测试包发布
  -> 需要签名、证书指纹、安装升级和供应链检查
```

后面每一节都围绕它问同一个问题：

```text
这个功能出问题时，是身份问题、授权问题、实际放行问题、组件边界问题，还是数据保护问题？
```

这样学到最后，你不是记住一堆安全名词，而是完成一次课程 App 的安全体检。

## 安全术语速查表

第 23 章会反复出现这些术语。

先有个印象，后面读到时再回来查。

| 术语 | 简单理解 | 排查时看什么 |
| --- | --- | --- |
| packageName | App 在包管理里的名字 | `dumpsys package`、Manifest |
| uid | App 在 Linux 和系统服务里的运行身份 | `dumpsys package`、`ps -A` |
| appId | App 在 Android 用户空间内的应用编号 | uid、userId、包状态 |
| userId | Android 多用户空间编号 | 当前用户、工作资料、多用户数据目录 |
| sandbox | App 默认隔离边界 | dataDir、uid、文件 owner、SELinux |
| DAC | Linux 文件权限模型 | owner、group、mode |
| MAC | 强制访问控制模型 | SELinux domain、type、policy |
| SELinux | Android 的强制访问控制层 | `avc: denied`、scontext、tcontext |
| runtime permission | 用户运行时授权的危险权限 | grant 状态、flags、设置页 |
| AppOps | 敏感操作的实际放行和记录层 | `cmd appops get`、mode |
| signing certificate | App 签名证书身份 | SHA-256 指纹、安装错误 |
| SigningDetails | 系统建模 App 签名和历史签名的结构 | 当前证书、历史证书、lineage |
| signature permission | 只有同签名或可信调用方才能拿到的权限 | 声明方签名、申请方签名 |
| Keystore | 系统密钥保护机制 | alias、key 是否可用、异常类型 |
| StrongBox | 独立安全硬件能力 | 设备支持、性能、兼容性 |
| Uri grant | 对某个 content Uri 的临时或持久授权 | grant flag、authority、path |
| exported | 组件是否允许外部进入 | Manifest、intent-filter |
| PendingIntent | 交给系统保存的未来动作令牌 | mutable / immutable、extras、目标组件 |

读第 23 章时，最重要的不是背术语，而是把术语放回问题现场。

```text
uid 解决“谁在运行”
permission 解决“有没有授权”
AppOps 解决“这次是否放行”
signing certificate 解决“是不是可信身份”
Keystore 解决“密钥是否受保护”
Uri grant 解决“这次分享给了谁多大范围”
```

## 本章探索任务

```text
理解 Android 应用沙箱和 UID
  -> 理解 Manifest 权限、runtime permission 和权限组
      -> 理解 AppOps 为什么像运行时开关和审计层
          -> 理解签名、证书、安装、升级和签名权限
              -> 理解 Keystore、加密存储、备份和密钥生命周期
                  -> 理解 exported、Intent、Provider、FileProvider 与组件暴露
                      -> 学会排查权限拒绝、签名不一致、组件误暴露和数据泄露
                          -> 做一个安全模型观察实验室
```

## 官方参考入口

第 23 章涉及系统安全、权限和签名策略，强烈建议配合官方文档学习：

- [Android app permissions](https://developer.android.com/guide/topics/permissions/overview)
- [Request app permissions](https://developer.android.com/training/permissions/requesting)
- [Best practices for app permissions](https://developer.android.com/training/permissions/usage-notes)
- [Android Keystore system](https://developer.android.com/privacy-and-security/keystore)
- [Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Security tips](https://developer.android.com/privacy-and-security/security-tips)
- [AppOpsManager API reference](https://developer.android.com/reference/android/app/AppOpsManager)
- [apksigner tool](https://developer.android.com/tools/apksigner)
- [Verify hardware-backed key pairs with key attestation](https://developer.android.com/privacy-and-security/security-key-attestation)

## 本节定位

本节是第 23 章入口。

它负责回答：

- 第 23 章为什么不是单纯讲权限申请？
- Android 安全模型和第 16、19、22 章是什么关系？
- 权限、签名、AppOps、Keystore 和组件暴露如何连成一条链？
- 本章应该按照什么顺序学习？

## 学习目标

学完本节后，你应该能够：

- 知道 Android 安全模型的核心是身份、沙箱、授权、审计和数据保护。
- 区分 Manifest 权限、runtime permission、AppOps、Uri grant、签名权限。
- 初步理解签名为什么决定安装、升级和信任关系。
- 知道敏感数据不能只靠“放进私有目录”保护。
- 建立一个原则：先确认身份和边界，再申请权限和设计数据保护。

## 第一部分：安全不是“权限弹窗”

很多人把 Android 安全理解成：

```text
需要权限
  -> 弹窗申请
      -> 用户同意
          -> 功能可用
```

这只是很小的一块。

真正的安全链路更像：

```text
包安装时
  -> 系统确认包名、签名、Manifest、targetSdk、组件声明

进程启动时
  -> 系统分配 UID、沙箱、进程权限边界

调用敏感 API 时
  -> 系统检查 permission、AppOps、前后台状态和调用来源

跨应用通信时
  -> 系统检查 exported、Intent、Provider、Uri grant 和签名

保存敏感数据时
  -> App 还要处理加密、备份、日志脱敏和失效策略
```

所以安全不是某一个 API，而是贯穿安装、运行、通信、存储和排障的系统规则。

## 第二部分：第 23 章和前面章节的关系

第 16 章讲过 PMS、Manifest、权限和签名入口。

第 19 章讲过 UID、进程、应用沙箱和 SELinux。

第 22 章讲过存储、Uri、MediaStore、SAF 和 FileProvider。

第 23 章要把这些线拧成一个问题：

```text
系统如何判断一个 App 是否有资格做某件事？
```

例如 FileProvider 分享：

```text
第 22 章看它是存储共享入口
第 23 章继续看它是临时授权和组件暴露问题
```

例如安装失败：

```text
第 16 章看 PMS 安装流程
第 23 章继续看签名身份和证书链
```

例如 token 保存：

```text
第 22 章看它放在哪里
第 23 章继续看它是否加密、是否备份、是否脱敏
```

## 第三部分：本章学习顺序

建议按下面顺序推进：

```text
23.1 建立安全模型总图
23.2 应用沙箱、UID、SELinux 与进程边界
23.3 权限系统：Manifest、runtime permission、权限组与用户授权
23.4 AppOps：为什么授权了也可能被系统继续拦
23.5 签名、证书、安装升级、签名权限与供应链风险
23.6 Keystore、加密存储、备份恢复与敏感数据保护
23.7 安全体验问题：权限拒绝、组件暴露、日志泄露与合规风险
23.8 综合实践：安全模型观察实验室
```

也可以按下面这张表分层学习：

| 小节 | 基础必读 | 深入选读 | 最终要能回答 |
| --- | --- | --- | --- |
| 23.2 沙箱与 UID | uid、私有目录、FileProvider | DAC / MAC、SELinux、Binder callingUid | 为什么路径正确也不能读别的 App 文件？ |
| 23.3 权限系统 | Manifest、runtime permission、拒绝降级 | PermissionManagerService、flags、targetSdk | 为什么授权状态不是一个简单布尔值？ |
| 23.4 AppOps | allow / ignore / foreground | check / note / start、attribution、系统服务嵌入点 | 为什么 granted 后仍可能不可用？ |
| 23.5 签名 | debug / release、覆盖安装失败 | ApkSignatureVerifier、SigningDetails、lineage | 为什么签名有效也未必能升级？ |
| 23.6 Keystore | key、alias、密文、日志脱敏 | keystore2、KeyMint、StrongBox、key invalidated | 为什么恢复了密文还可能解不开？ |
| 23.7 组件边界 | exported、FileProvider、日志泄露 | IntentResolver、Uri grant、PendingIntent mutable | 为什么入口能被发现就是风险的一部分？ |
| 23.8 综合实践 | 完成安全体检报告 | 把 Demo 映射到系统模块和证据链 | 如何证明修复真的有效？ |

不要把本章学成“权限表背诵”。

它更像一套调查方法：

```text
谁在访问
  -> 访问什么
      -> 凭什么访问
          -> 系统有没有实际放行
              -> 数据有没有被保护
                  -> 出问题时证据在哪里
```

## 本节小结

第 23 章要训练的是安全判断力。

```text
身份
  -> 包名、UID、签名、进程

边界
  -> 沙箱、组件、Provider、Uri

授权
  -> permission、AppOps、用户选择、签名权限

保护
  -> Keystore、加密、备份、脱敏

复盘
  -> dumpsys package、appops、日志、报告和回归
```

当你能先问“谁凭什么访问什么”，再决定如何申请权限、暴露组件和保护数据，第 23 章就真正开始了。
