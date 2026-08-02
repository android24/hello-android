# 16.5 签名、权限与安装校验

安装和包管理离不开安全。

这一节看三个容易混在一起的概念：

```text
签名
权限
安装校验
```

它们共同决定：一个 APK 能不能安装、能不能升级、能不能访问某些能力。

## 本节剧情钩子

你拿到一个新 APK，想覆盖安装旧版本。

结果失败：

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

或者你调用某个能力时失败：

```text
Permission Denial
```

这类问题不是“系统抽风”，而是 Android 安全模型在工作。

## 本节定位

本节讲清楚签名、权限和安装校验之间的关系，为排查安装失败、权限异常和升级问题打基础。

## 学习目标

学完本节后，你应该能够：

- 理解 APK 签名为什么重要。
- 知道同包名升级为什么要求签名一致。
- 区分权限声明、权限请求和用户授权。
- 理解 signature 权限和普通权限的差异。
- 能解释常见安装校验失败。

## 第一部分：签名是应用身份

APK 签名不是为了让文件看起来正规。

它是系统识别应用身份的重要依据。

系统会用签名判断：

- 新 APK 是否能覆盖旧 APK。
- 两个 App 是否来自同一签名主体。
- 是否能获得 signature 级别权限。
- 是否允许某些共享或受保护能力。

同包名升级时，签名不一致通常不允许覆盖。

这是为了防止恶意 APK 冒充已有应用。

## 第二部分：debug 签名和 release 签名

开发阶段常用 debug keystore。

发布阶段使用 release keystore。

如果你在同一台设备上用 release 包覆盖 debug 包，或者反过来，就可能失败。

排查时要确认：

```text
包名是否相同？
签名是否相同？
版本是否允许升级？
设备上是否已有旧包？
```

很多“安装不上”的问题，其实是签名或版本规则在阻止覆盖。

## 第三部分：权限分几层

权限问题至少有三层：

```text
声明权限
  -> 请求权限
      -> 获得授权
```

Manifest 里写：

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

只是声明“我想用”。

对 dangerous 权限，还需要运行时向用户请求。

对 signature 权限，则可能要求调用方和权限定义方签名一致。

## 第四部分：权限保护级别

常见权限保护级别可以简化理解：

- normal：安装时自动授予，风险较低。
- dangerous：涉及隐私或敏感能力，需要运行时授权。
- signature：要求签名匹配。
- privileged / system 相关权限：通常只给系统或特权应用。

排查权限问题时，不要只看：

```text
Manifest 里有没有写。
```

还要看保护级别和授权状态。

## 第五部分：组件权限

组件也可以设置权限门禁。

例如：

```xml
<receiver
    android:name=".SecretReceiver"
    android:permission="com.example.permission.SECRET" />
```

这意味着调用者要满足权限要求。

所以组件访问失败可能来自：

- 组件不存在。
- 组件未 exported。
- 调用者看不见目标包。
- 调用者没有权限。
- 权限保护级别不满足。

## 第六部分：安装校验常见错误

常见安装失败可以这样分类：

| 错误 | 可能原因 | 第一排查方向 |
| --- | --- | --- |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 同包名签名不一致 | 卸载旧包或使用同一签名 |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | versionCode 降级 | 提高 versionCode 或允许降级安装 |
| `INSTALL_FAILED_INVALID_APK` | APK 损坏或结构异常 | 重新打包，检查 APK |
| `INSTALL_FAILED_NO_MATCHING_ABIS` | native so 架构不匹配 | 检查 ABI 配置 |
| `INSTALL_FAILED_MISSING_SHARED_LIBRARY` | 依赖库缺失 | 检查 uses-library |

这些错误背后都能在安装链路里找到对应规则。

## 本节小挑战

### 安全门禁题

请判断下面问题可能涉及签名、权限还是安装校验：

- 同包名 debug 包无法覆盖 release 包。
- 相机权限 Manifest 写了，但仍然打不开相机。
- 调用系统隐藏能力时报权限拒绝。
- 外部 App 无法发送广播给你的 Receiver。
- 安装旧版本时报 version downgrade。

## 本节实践任务

### 基础任务

- 查看 debug APK 的签名信息。
- 对比同包名不同签名 APK 的安装结果。
- 写下系统为什么不允许覆盖。

### 进阶任务

- 声明一个 dangerous 权限。
- 对比 Manifest 声明和运行时授权状态。
- 写下权限问题的三层排查路径。

## 本节小结

签名决定应用身份，权限决定能力边界，安装校验决定 APK 是否能进入系统。PMS 在这些过程中维护包信息、签名信息和权限声明，并和权限管理、安装流程、组件访问共同形成 Android 的安全门禁。排查安装和权限问题时，要同时看包名、签名、版本、权限保护级别和用户授权状态。
