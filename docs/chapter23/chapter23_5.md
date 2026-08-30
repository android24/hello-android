# 23.5 签名、证书、安装升级、签名权限与供应链风险

权限回答：

```text
这个 App 能不能访问某个能力？
```

签名回答另一个更根本的问题：

```text
这个 App 是不是同一个发布者？
这个升级包是不是可信？
这个调用方是不是我信任的那一方？
```

Android App 的签名不是发布前的仪式感，而是系统识别应用身份的重要依据。

## 本节先记住三句话

```text
包名相同不等于同一个 App，签名身份连续才是可信升级。
APK 签名校验分完整性校验和升级身份校验。
signature permission 信任的是签名身份，不是权限字符串本身。
```

## 贯穿案例：测试包为什么覆盖不了线上包

`Hello Android 学习中心`线上包使用 release signing。

测试同学拿到一个 debug 包，尝试覆盖安装到线上设备，结果失败：

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

这时不要只说：

```text
签名不一致。
```

要继续拆：

```text
这个 APK 的签名本身是否有效？
它的 signer certificate 和已安装包是否匹配？
是否存在可信 signing lineage？
versionCode 是否递增？
是否装了不同 flavor 或渠道包？
```

本节要让你能讲清楚：为什么“签得没问题的新包”，也可能不是“旧 App 的可信升级包”。

## 安全侦探问题

你只有两条命令机会排查覆盖安装失败。

你会选哪两条？

```bash
adb shell dumpsys package com.helloandroid
apksigner verify --verbose --print-certs app-debug.apk
```

请说出你分别想从里面看到什么。

## 本节定位

本节负责回答：

- App 签名为什么重要？
- 为什么签名不一致会导致覆盖安装失败？
- debug 签名和 release 签名有什么区别？
- 签名权限是什么？
- 签名和供应链风险有什么关系？

## 学习目标

学完本节后，你应该能够：

- 理解签名证书是 App 身份的一部分。
- 能解释为什么包名相同但签名不同不能直接升级。
- 区分 debug keystore、release keystore 和 Play App Signing。
- 知道 signature permission 不是普通业务 App 能随便拿到的能力。
- 能把签名问题纳入安装失败和安全事故排查。

## 本节阅读导航

这一节比较长，建议按四层读：

```text
先看事故
  -> 测试包为什么覆盖不了线上包

再看身份
  -> 包名、证书、公钥、SigningDetails 到底谁代表 App

再看校验
  -> APK 完整性校验和升级身份校验为什么不是一回事

最后看信任边界
  -> signature permission、同签名组件访问和供应链风险如何串起来
```

读完不要只记住“签名不一致会失败”，而是要能说清楚：

```text
系统先确认 APK 自己有没有被篡改，
再确认它是不是已安装 App 的可信继任者。
```

## 第一部分：签名是 App 的长期身份

Android 安装 APK / AAB 产物时，会检查签名。

签名用于：

```text
确认 App 发布者身份
确认升级包是否和已安装 App 匹配
保护 App 数据不被签名不同的包覆盖读取
支持签名权限
支持某些跨包信任关系
```

包名相同不够。

系统要看：

```text
packageName 相同
  + signing certificate 匹配
      -> 才能作为同一个 App 升级
```

如果签名不一致，常见现象：

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE
App not installed as package conflicts with an existing package
```

这不是安装器脾气不好，而是系统在防止“冒名顶替”。

## 第二部分：debug 签名和 release 签名

开发时，Android Studio 会使用 debug keystore 签名。

发布时，应该使用 release signing。

两者差异：

| 类型 | 用途 | 风险 |
| --- | --- | --- |
| debug 签名 | 本地开发、调试 | 不应该发布线上 |
| release 签名 | 正式发布 | 丢失或泄露都会造成严重事故 |
| Play App Signing | Google Play 管理应用签名密钥 | 需要理解上传密钥和应用签名密钥 |

常见事故：

```text
测试包覆盖不了线上包
  -> 签名不同

线上包误用 debug 签名
  -> 发布流程事故

release keystore 丢失
  -> 升级链断裂

keystore 泄露
  -> 供应链风险
```

签名密钥不是普通配置文件。

它更像 App 的护照和印章。

## 第三部分：签名方案不是一个版本号

Android 签名方案经历过演进，例如 v1、v2、v3、v4。

你不需要在应用开发阶段背所有细节，但要知道：

```text
签名方案影响安装校验、完整性校验和兼容性
```

排查安装问题时，要记录：

```text
构建方式
签名配置
minSdk / targetSdk
是否开启 v1 / v2 / v3 / v4
安装渠道
安装错误信息
```

不要只说：

```text
安装失败。
```

要说清楚：

```text
是签名不一致、版本号回退、ABI 不兼容、权限冲突，还是安装来源限制？
```

### 原理加深：安装时到底校验了什么

签名不是安装包最后贴上的标签。

对系统来说，签名参与的是安装和升级身份判断：

```text
读取 APK
  -> 解析 packageName、versionCode、targetSdk、组件、权限
      -> 校验 APK 签名完整性
          -> 和已安装包的签名历史比较
              -> 判断是否允许安装、升级、共享信任关系
                  -> 写入包状态和权限状态
```

所以同一个包名如果签名不同，系统不能直接把它当成升级包。

否则攻击者可以做：

```text
伪造同包名 APK
  -> 覆盖原 App
      -> 继承原 App 数据目录和用户信任
```

这正是 Android 必须阻止的事情。

### 原理加深：签名校验由哪些模块完成

从应用开发视角看，你执行的是：

```bash
adb install app-release.apk
```

或者用户点击安装包。

但系统内部不是“安装器直接信任这个 APK”，而是大致走过下面这条链路：

```text
安装入口
  -> adb install / PackageInstaller / 应用商店

安装会话
  -> PackageInstallerService
      -> 把 APK 作为一次 install session 提交给系统

包管理核心
  -> PackageManagerService
      -> 调度安装、解析包信息、写入包状态

APK 解析
  -> 解析 AndroidManifest.xml
      -> packageName、versionCode、targetSdk、uses-permission、组件声明

签名解析与校验
  -> ApkSignatureVerifier / apksig 相关逻辑
      -> 读取 v1 / v2 / v3 / v4 签名信息
      -> 校验 APK 内容摘要和签名
      -> 提取 signer certificate

签名结果建模
  -> SigningDetails
      -> 当前证书
      -> 历史证书
      -> proof-of-rotation / lineage

升级身份判断
  -> 和已安装包的 PackageSetting / SigningDetails 比对
      -> 判断是不是同一个 App 的可信升级

权限与信任延伸
  -> signature permission
  -> shared UID / 同签名信任
  -> 组件调用方校验
```

这里要注意两个层次：

```text
APK 完整性校验
  -> 这个 APK 有没有被篡改？

App 身份连续性校验
  -> 这个 APK 能不能作为已安装 App 的升级？
```

第一层校验证明：

```text
APK 内容和签名匹配
```

第二层校验证明：

```text
新 APK 的签名身份和旧 App 的签名身份是同一条可信链
```

所以一个 APK 可能出现：

```text
签名本身有效
  -> 但不能覆盖已安装 App
```

原因是：

```text
它是一个“签得没问题的新身份”，不是“旧 App 的可信升级身份”。
```

这就是很多安装问题最容易混淆的地方。

### 原理加深：v2 / v3 校验到底在校验什么

以 v2 / v3 为例，它们不是只校验 `META-INF` 目录里的几个文件。

它们会把 APK 当成一个整体文件来处理：

```text
定位 APK Signing Block
  -> 找到对应签名方案分块
      -> 读取 signer、certificate、signature、digest
          -> 用 certificate 里的 public key 验证 signed data
              -> 重新计算 APK 受保护内容的 digest
                  -> 比较 digest 是否一致
                      -> 确认证书、公钥和签名数据匹配
```

所以如果你签名后又修改 APK：

```text
改资源
改 dex
改 ZIP 元数据
追加文件
重新压缩
```

都可能导致：

```text
digest 不匹配
  -> signature verification failed
```

这也是为什么工具链里通常要求：

```text
先 zipalign
再 apksigner sign
```

签完以后再改 APK，相当于把封好的档案袋拆开又塞了东西进去。

系统当然不会继续相信它。

### 原理加深：v1、v2、v3、v4 的校验边界

可以用下面这张表理解：

| 方案 | 核心校验对象 | 主要解决的问题 | 典型边界 |
| --- | --- | --- | --- |
| v1 | JAR entries / META-INF | 兼容早期 Android | 不保护部分 ZIP 元数据，验证成本高 |
| v2 | APK 整体内容摘要 + APK Signing Block | 更完整的完整性校验 | Android 7.0+ 支持 |
| v3 | v2 思路 + 额外签名历史信息 | 支持证书轮换能力 | Android 9+ 支持 |
| v4 | 独立 `.idsig` 和 Merkle tree | 增量安装 / 流式安装场景 | 需要配合 v2 或 v3 |

要强调一点：

```text
v4 不是 v2 / v3 的替代品。
```

它需要互补的 v2 或 v3 签名。

因此发布排查时，不要只问：

```text
有没有 v4？
```

更应该问：

```text
这个 APK 在目标 Android 版本上，能不能被对应签名方案成功验证？
```

你可以用：

```bash
apksigner verify --verbose --print-certs app-release.apk
```

观察：

```text
Verified using v1 scheme
Verified using v2 scheme
Verified using v3 scheme
Signer certificate SHA-256 digest
```

这条命令适合放进 CI 发布前检查。

### v1、v2、v3、v4 粗略怎么理解

这部分不要求背格式，但要知道它们解决的问题不完全一样。

可以粗略记成：

```text
v1
  -> JAR 签名，兼容早期 Android

v2
  -> APK Signature Scheme v2，对 APK 做更完整的签名校验

v3
  -> 在 v2 思路上继续支持签名证书轮换等能力

v4
  -> 生成独立签名信息，常用于增量安装等场景
```

排查安装问题时，你不用先陷入格式细节。

但你要有这条链路意识：

```text
不同 Android 版本
  -> 支持的签名方案不同
      -> APK 构建配置不同
          -> 校验结果和兼容性可能不同
```

所以发布前不要只验证：

```text
能在我的手机上装。
```

而应该验证：

```text
最低支持版本能装
主流版本能装
目标版本能装
升级安装能装
覆盖安装失败时错误可解释
```

### signing lineage：签名也会有历史

真实项目里，release keystore 可能会遇到：

```text
密钥需要轮换
上传密钥泄露
组织迁移
发布平台接管应用签名
```

这时不能简单地说：

```text
换一个新 keystore 重新签。
```

因为系统要知道：

```text
旧签名和新签名之间是否存在可信的继承关系
```

这就是 signing lineage 的意义。

你可以把它理解为：

```text
App 身份的家谱
```

它帮助系统判断：

```text
这个新证书是不是被旧身份认可过
这个升级是不是可信
哪些能力从旧签名继承到新签名
```

课程阶段不用你立刻掌握所有命令，但至少要知道：

```text
签名变更不是简单替换文件
签名轮换需要工具、记录和发布策略配合
签名历史是安装、升级和权限信任的一部分
```

### 原理加深：签名如何影响 signature permission

签名校验不只影响安装升级。

它还会影响权限授予。

例如 App A 声明：

```xml
<permission
    android:name="com.example.permission.INTERNAL_SYNC"
    android:protectionLevel="signature" />
```

App B 申请：

```xml
<uses-permission android:name="com.example.permission.INTERNAL_SYNC" />
```

系统判断时不会只看 App B 有没有写 `uses-permission`。

它会看：

```text
谁声明了这个 permission？
  -> App A

这个 permission 的 protectionLevel 是什么？
  -> signature

申请方 App B 和声明方 App A 的 signing certificate 是否匹配？
  -> 匹配才授予
```

所以 signature permission 的核心不是：

```text
谁声明了权限字符串
```

而是：

```text
谁和权限声明方处在同一条可信签名身份链上
```

这也是为什么大型 App 套件会把签名、权限和组件边界放在一起设计。

## 第四部分：签名权限

signature permission 的语义是：

```text
只有和声明权限的 App 使用相同签名，或被系统认可的调用方，才能获得该权限。
```

它常用于：

```text
同厂商 App 间的高信任通信
系统 App 能力
平台级能力
内部 SDK 或套件保护
```

普通 App 声明一个 signature permission，不等于自己能获得系统能力。

常见误解：

```text
Manifest 里写了某个系统 signature 权限
  -> 就能调用系统内部接口
```

实际通常会失败。

因为系统看的是：

```text
权限保护级别
调用方签名
安装位置和特权状态
平台策略
```

## 第五部分：签名和组件信任

签名还会影响组件之间的信任设计。

例如一个内部 ContentProvider：

```text
只允许同签名 App 访问
```

一个内部 Service：

```text
要求调用方持有自定义 signature permission
```

这类设计适合 App 套件内部通信。

但要注意：

```text
签名权限保护的是调用方身份
不等于数据自动安全
Provider 仍然要校验参数
返回数据仍然要最小化
日志仍然不能泄露敏感内容
```

## 第六部分：供应链风险

签名和构建链路也关系到供应链安全。

风险包括：

```text
签名密钥泄露
构建产物被替换
第三方 SDK 注入恶意行为
CI 密钥管理不当
渠道包被二次打包
测试包误发布
```

工程治理建议：

```text
release keystore 不入库
CI 使用受控密钥管理
区分 debug / staging / release 签名
发布前校验签名证书指纹
保留构建产物和 commit 关系
限制上传密钥权限
审查第三方 SDK 权限和行为
```

这会在第 25 章大型工程治理里继续展开。

## 第七部分：签名事故排查

### 事故一：测试包覆盖不了线上包

排查：

```text
包名是否相同？
签名证书是否相同？
versionCode 是否更高？
是否安装了不同渠道变体？
```

### 事故二：同厂商两个 App 无法互相调用内部 Provider

排查：

```text
是否使用相同签名？
Provider 是否 exported？
是否配置 signature permission？
调用方是否真的持有权限？
```

### 事故三：线上包疑似被二次打包

排查：

```text
签名证书指纹是否匹配官方发布记录？
包名和 versionCode 是否异常？
安装来源是否可信？
是否出现未知权限或组件？
```

## 第七部分补充：签名事故要分三层看

遇到签名类事故，不要一上来只问：

```text
是不是 keystore 不一样？
```

更稳的拆法是三层：

```text
第一层：安装身份
  -> packageName、versionCode、signing certificate、signing lineage

第二层：发布链路
  -> 构建变体、CI 产物、渠道包、上传密钥、应用签名密钥

第三层：信任边界
  -> signature permission、同签名组件访问、Provider / Service 调用方校验
```

这样你就能区分：

```text
装不上
  -> 可能是升级身份问题

装上了但内部能力不能用
  -> 可能是 signature permission 或调用方签名不匹配

用户装的是同包名 App 但行为异常
  -> 可能是二次打包或渠道污染
```

建议保留发布证据：

```text
commit sha
构建时间
构建机器或 CI job
variant / flavor
versionName / versionCode
签名证书 SHA-256 指纹
apksigner verify 结果
发布渠道
```

这些证据会在第 25 章大型工程治理里变成发布门禁。

## 第八部分：本节自测

请回答：

- 为什么包名相同但签名不同不能覆盖安装？
- debug 签名为什么不应该发布线上？
- release keystore 丢失会带来什么后果？
- signature permission 解决什么问题？
- 签名是否等于数据一定安全？为什么？
- 供应链风险和签名有什么关系？
- signing lineage 解决的是什么问题？
- 为什么签名事故要同时看安装身份、发布链路和信任边界？
- APK 完整性校验和 App 升级身份校验有什么区别？
- 签名校验链路里 PackageManagerService、ApkSignatureVerifier、SigningDetails 分别负责什么？
- 为什么一个 APK “签名有效”仍然可能不能覆盖已安装 App？

## 本节小结

签名是 Android App 的长期身份。

```text
安装
  -> 看签名确认身份

升级
  -> 看签名确认是不是同一个 App

权限
  -> signature permission 看签名信任

供应链
  -> 看密钥、构建和发布链路是否可信
```

第 23.5 节的关键结论是：

```text
权限决定能不能做事，签名决定系统相不相信你是谁。
```
