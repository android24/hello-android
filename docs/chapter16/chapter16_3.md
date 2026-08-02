# 16.3 AndroidManifest 解析与组件注册

安装流程中最关键的输入之一，就是 AndroidManifest.xml。

这一节我们看 PMS 如何通过 Manifest 认识一个 App。

```text
Manifest 不是给人看的说明书，
它是系统认识应用的登记表。
```

## 本节剧情钩子

你新增了一个 Activity。

代码写好了，页面也写好了。

但启动时报：

```text
ActivityNotFoundException
```

原因可能很简单：系统根本不知道这个 Activity 存在。

对 PMS 来说，没有被正确解析和注册的组件，就像没有登记户口。

## 本节定位

本节讲清楚 Manifest 如何变成 PMS 可查询的包信息和组件信息。

## 学习目标

学完本节后，你应该能够：

- 理解 Manifest 在包管理中的作用。
- 知道四大组件如何被解析成系统信息。
- 理解 intent-filter、exported、permission 的基础意义。
- 能把组件找不到、无法启动、无法被外部访问和 Manifest 联系起来。

## 第一部分：Manifest 里的核心信息

Manifest 常见信息包括：

```xml
<manifest package="...">
    <uses-permission />
    <application>
        <activity />
        <service />
        <receiver />
        <provider />
    </application>
</manifest>
```

PMS 会把这些声明解析成系统可查询的数据结构。

常见结果包括：

- PackageInfo。
- ApplicationInfo。
- ActivityInfo。
- ServiceInfo。
- ProviderInfo。
- PermissionInfo。

这些信息会被其他系统服务和 App 查询。

## 第二部分：四大组件是系统入口

Activity、Service、Receiver、Provider 都不是普通类。

它们是系统可调度的组件。

系统需要知道：

- 类名。
- 所属包。
- 是否 enabled。
- 是否 exported。
- 需要什么 permission。
- 有哪些 intent-filter。
- 运行在哪个 process。

没有这些信息，AMS、广播系统、ContentResolver 都无法正确调度组件。

## 第三部分：intent-filter 是匹配规则

隐式 Intent 能找到目标，是因为组件声明了匹配规则。

例如：

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:scheme="https" />
</intent-filter>
```

PMS 会根据 action、category、data 等信息建立匹配能力。

当外部发起隐式 Intent 时，系统会查询这些规则，找到可能的目标组件。

## 第四部分：exported 是对外边界

`exported` 决定组件是否允许被其他 App 访问。

简化理解：

- `exported=true`：可以被外部 App 调用，前提是满足权限等条件。
- `exported=false`：通常只允许本 App 内部使用。

Android 12 开始，对带 intent-filter 的组件，`exported` 要求更明确。

这是安全边界，不只是配置细节。

## 第五部分：permission 是入口门禁

组件可以声明访问权限。

例如：

```xml
<service
    android:name=".SyncService"
    android:permission="com.example.permission.SYNC" />
```

这意味着外部调用者不仅要找到组件，还要满足权限要求。

所以组件访问失败时，不能只看组件是否存在，还要看：

- exported。
- permission。
- 调用者身份。
- 用户状态。

## 第六部分：Manifest Merge 的影响

现代 Android 项目通常有多个 Manifest：

- app 主 Manifest。
- build variant Manifest。
- library Manifest。
- manifestPlaceholders。

最终打包时会发生 Manifest Merge。

所以你在源码里看到的 Manifest，不一定等于最终 APK 中的 Manifest。

排查 PMS 解析问题时，应该看最终合并结果。

## 本节小挑战

### 组件登记题

请判断下面问题可能对应 Manifest 哪一块：

- 隐式 Intent 找不到 Activity。
- 外部 App 无法启动你的 Service。
- Android 12 安装时报 exported 相关错误。
- Provider 访问时报权限异常。
- Debug 版本和 Release 版本组件行为不一致。

## 本节实践任务

### 基础任务

- 打开一个项目的 AndroidManifest.xml。
- 标出四大组件和它们的 exported / permission。
- 找一个带 intent-filter 的 Activity。

### 进阶任务

- 查看最终 merged manifest。
- 对比源码 Manifest 和合并后 Manifest。
- 写下 library 或 build variant 对组件声明的影响。

## 本节小结

Manifest 是系统认识应用的登记表。PMS 会解析包名、应用信息、四大组件、权限、intent-filter 和 exported 等配置，并把它们变成系统可查询的包管理数据。组件找不到、外部无法访问、权限异常和安装失败，很多时候都要回到 Manifest 解析结果里找证据。
