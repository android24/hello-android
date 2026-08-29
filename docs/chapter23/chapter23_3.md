# 23.3 权限系统：Manifest、runtime permission、权限组与用户授权

第 23.2 节讲了沙箱和 UID。

现在进入 Android 安全模型里用户最熟悉的一层：

```text
权限
```

但本节不会停留在“如何弹权限框”。

我们要看的是：

```text
权限什么时候声明？
什么时候申请？
用户授权后系统记录在哪里？
为什么 targetSdk 会改变权限行为？
为什么权限通过了，功能仍然可能不可用？
```

## 本节先记住三句话

```text
Manifest 声明只是说“我可能需要”，不等于用户已经同意。
runtime granted 只说明权限层通过，不代表具体能力一定可用。
权限问题要分安装阶段、授权阶段和使用阶段排查。
```

## 贯穿案例：拍照上传作业为什么会失败

`Hello Android 学习中心`有一个功能：

```text
拍照上传作业
```

用户点击拍照后，页面提示相机不可用。

你不能只问：

```text
用户有没有点允许？
```

还要继续问：

```text
Manifest 是否声明 CAMERA？
runtime permission 是否 granted？
用户是否选择了拒绝或不再询问？
设备是否有相机？
相机是否被系统隐私开关关闭？
AppOps camera 是否允许？
调用发生在前台还是后台？
```

这就是权限章节的核心：权限不是一个按钮，而是一条状态链。

## 安全侦探问题

用户说“我已经开了权限”，但拍照仍然失败。

你会先把问题拆成哪三个阶段？

```text
安装阶段
授权阶段
使用阶段
```

请分别给出每个阶段最该看的证据。

## 本节定位

本节负责回答：

- Manifest 权限和 runtime permission 有什么区别？
- normal、dangerous、signature 权限分别是什么？
- 权限组为什么影响用户理解？
- targetSdk 为什么会改变权限行为？
- 如何设计一个不过度索取权限的功能？

## 学习目标

学完本节后，你应该能够：

- 区分安装时权限和运行时权限。
- 理解 dangerous permission 为什么需要用户参与授权。
- 知道权限组是用户理解权限的视角，不应当被业务滥用。
- 能设计权限申请前的解释、拒绝后的降级和重新授权入口。
- 能用 `dumpsys package` 初步观察权限状态。

## 第一部分：Manifest 声明不是用户授权

在 Manifest 中写：

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

表达的是：

```text
这个 App 可能需要相机能力。
```

但对于 dangerous permission 来说，这不等于用户已经授权。

运行时还要：

```text
检查权限
  -> 解释为什么需要
      -> 发起请求
          -> 用户同意或拒绝
              -> 根据结果启用功能或降级
```

这是一条用户信任链。

不要把它写成：

```text
一进首页就弹 5 个权限
```

更好的方式是：

```text
用户触发需要权限的功能
  -> 页面解释用途
      -> 请求最小必要权限
          -> 被拒后提供替代路径
```

例如：

```text
点击“拍照上传作业”
  -> 请求 CAMERA

点击“选择相册图片”
  -> 优先 Photo Picker
```

## 第二部分：权限类型

Android 权限大体可以按保护级别理解：

| 类型 | 含义 | 例子 | 用户体验 |
| --- | --- | --- | --- |
| normal | 风险较低，安装时自动授予 | 网络状态等 | 通常无弹窗 |
| dangerous | 涉及敏感数据或能力 | 相机、定位、通讯录 | 运行时授权 |
| signature | 只有相同签名或系统认可调用方可用 | 厂商 / 系统能力 | 普通 App 不能申请成功 |
| special app access | 特殊高风险能力 | 所有文件访问、悬浮窗、通知监听 | 进入设置页授权 |

很多事故来自误解：

```text
Manifest 声明了
  -> 就以为一定能用

用户同意了 runtime permission
  -> 就以为 AppOps 一定允许

普通 App 声明 signature permission
  -> 就以为能获得系统能力
```

安全模型不是“写了就有”，而是多层判断。

## 第三部分：权限组是用户理解视角

Android 会把一些权限组织到权限组里，让用户更容易理解。

例如：

```text
位置
相机
麦克风
通讯录
日历
附近设备
照片和视频
```

但工程上不要滥用权限组做推断。

你应该关心具体权限和具体业务：

```text
我要拍照
  -> CAMERA

我要后台定位
  -> ACCESS_FINE_LOCATION / ACCESS_BACKGROUND_LOCATION / AppOps / 前后台状态

我要读取图片集合
  -> READ_MEDIA_IMAGES 或 Photo Picker

我要发通知
  -> POST_NOTIFICATIONS
```

用户看到的是“这一类敏感能力”，系统检查的是更具体的 permission 和 AppOps。

## 第四部分：targetSdk 会改变权限行为

同一个权限，在不同 `targetSdk` 下可能有不同表现。

原因是：

```text
Android 会通过 targetSdk 判断 App 是否应该适配新规则。
```

第 21 章后台限制、第 22 章存储权限、第 23 章安全策略都会受影响。

例如：

```text
Android 13 通知权限
  -> targetSdk 较新时需要运行时请求 POST_NOTIFICATIONS

Android 13 媒体权限
  -> 图片、视频、音频权限拆分

Android 14 部分照片授权
  -> 授权后也可能只能访问用户选择的部分媒体
```

所以权限排查报告必须记录：

```text
Android 版本
targetSdk
permission 声明
permission 授权结果
AppOps 状态
用户是否在设置页改过
```

## 第五部分：权限申请的工程设计

一个成熟的权限申请流程应该包含：

```text
触发场景
  -> 用户为什么现在需要这个能力

申请前解释
  -> 用产品语言说明用途

请求权限
  -> 只请求最小必要权限

授权成功
  -> 执行功能

拒绝
  -> 降级、引导或使用替代入口

永久拒绝
  -> 提供设置页入口

撤销或系统重置
  -> 下次使用前重新检查
```

反例：

```text
用户打开 App
  -> 立刻请求相机、相册、定位、麦克风
```

这不是安全设计，是信任透支。

## 第六部分：权限事故排查

### 事故一：用户说已经授权，相机仍打不开

排查：

```text
Manifest 是否声明 CAMERA？
runtime permission 是否 granted？
是否在隐私设置里关闭相机访问？
是否被 AppOps 拦截？
是否调用时机在后台？
是否设备没有相机或被占用？
```

### 事故二：Android 13 后通知不弹

排查：

```text
是否声明 POST_NOTIFICATIONS？
用户是否授权通知权限？
通知渠道是否关闭？
AppOps 是否允许 POST_NOTIFICATION？
是否前台服务通知类型合规？
```

### 事故三：照片权限通过但只能看到一部分图片

排查：

```text
Android 14 是否只授权部分照片？
App 是否处理了重新选择入口？
是否误以为权限通过等于全相册？
是否应该改用 Photo Picker？
```

## 第六部分补充：权限状态是怎么形成的

权限不是一个布尔值。

当你调用：

```kotlin
checkSelfPermission(Manifest.permission.CAMERA)
```

看到 `granted` 时，背后至少经历过这些阶段：

```text
安装阶段
  -> PackageManager 解析 AndroidManifest.xml
      -> 记录 requested permissions
          -> 根据权限类型、签名、安装状态、targetSdk 形成初始授权状态

运行阶段
  -> 用户在权限弹窗或设置页做选择
      -> 系统记录 runtime permission grant / deny / flags
          -> 敏感操作发生时再结合 AppOps、前后台状态和具体 API 策略
```

所以更准确的表达应该是：

```text
Manifest 决定“我声明需要什么”
PackageManager / PermissionManager 决定“这个包当前拥有哪些权限状态”
用户设置决定“用户是否允许这类敏感能力”
AppOps 决定“这次具体操作是否被允许和记录”
API 自身策略决定“当前场景是否合规”
```

### 为什么声明了也可能拿不到

常见原因：

```text
权限不存在或拼写错误
权限保护级别不是 normal / dangerous，而是 signature / privileged
声明方和调用方签名不匹配
targetSdk 触发新权限模型
安装来源或系统策略限制
用户拒绝或系统自动重置
```

比如你声明一个系统 signature 权限：

```xml
<uses-permission android:name="android.permission.MANAGE_USERS" />
```

普通三方 App 通常不会因为写了这行就获得能力。

因为系统还会看：

```text
权限保护级别
调用方签名
是否系统 / 特权应用
平台策略
```

### 权限 flags 比 granted 更能解释事故

真实排查里，光看 granted 不够。

你还要关心：

```text
用户是否选择了“不再询问”
权限是否被系统自动重置
权限是否由策略固定
是否只授予了部分媒体
是否来自兼容模式
是否被设备管理策略影响
```

这些状态会影响产品引导。

例如：

```text
普通拒绝
  -> 可以再次解释并请求

不再询问
  -> 应该引导到设置页

部分照片授权
  -> 应该提供“选择更多照片”入口

策略固定
  -> 普通用户无法在设置页打开
```

因此权限事故报告不要只写：

```text
CAMERA=false
```

更好的写法是：

```text
permission=CAMERA
manifestDeclared=true
runtimeGrant=false
shouldShowRationale=false
appOps=camera:ignore
targetSdk=35
foreground=true
userAction=denied twice
```

这类记录才能解释“为什么不能用”和“下一步应该怎么引导”。

### 权限组是 UI 语言，不是工程边界

用户看到的经常是：

```text
照片和视频
位置
附近设备
通知
```

但系统内部判断更细：

```text
READ_MEDIA_IMAGES
READ_MEDIA_VIDEO
READ_MEDIA_VISUAL_USER_SELECTED
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
BLUETOOTH_SCAN
POST_NOTIFICATIONS
```

权限组是帮用户理解风险的 UI 语言。

工程设计不能把权限组当成唯一判断边界。

真正的判断要落到：

```text
具体 permission
具体 AppOps
具体 API
具体 Android 版本
具体 targetSdk
具体用户选择
```

## 第六部分补充二：权限判断链路里的模块分工

权限系统可以拆成三个阶段。

### 安装阶段：声明被解析

安装 APK 时，系统会解析 Manifest：

```text
AndroidManifest.xml
  -> uses-permission
  -> permission
  -> permission-group
  -> component permission
  -> targetSdk
  -> exported
```

这些信息进入包状态。

可以粗略理解为：

```text
PackageManagerService
  -> 管包是否存在、组件是什么、Manifest 声明了什么

PermissionManagerService
  -> 管权限定义、权限授予、权限状态和权限 flags
```

Manifest 声明完成后，系统不会简单地全部授予。

它会继续看：

```text
权限是否存在
权限保护级别是什么
声明方是谁
申请方是谁
申请方签名是否匹配
是否系统 / 特权应用
当前 Android 版本和 targetSdk
```

### 授权阶段：用户和策略参与

危险权限进入运行时授权流程。

这个流程不只是 App 弹一个系统框。

背后还涉及：

```text
PermissionController
  -> 展示系统权限 UI
  -> 处理用户选择
  -> 引导设置页

PermissionManagerService
  -> 更新 grant / revoke 状态
  -> 记录 flags
  -> 处理自动重置、策略固定、兼容行为
```

所以产品上看到的：

```text
用户点了允许
用户点了拒绝
用户点了只允许部分照片
用户在设置页关掉
```

最终都应该转成系统状态：

```text
granted / denied
flags
partial access
policy fixed
user fixed
auto revoked
```

### 使用阶段：API 继续做现场判断

真正调用敏感 API 时，系统还会做现场判断：

```text
调用方 uid / package
  -> permission 是否 granted
      -> AppOps 是否 allow / foreground / ignore
          -> 当前前后台状态是否满足
              -> API 自身是否允许这个场景
                  -> 返回数据、降级或抛异常
```

这也是为什么：

```text
checkSelfPermission() == granted
```

只能说明权限层面通过，不代表功能完整可用。

例如通知：

```text
POST_NOTIFICATIONS granted
  -> post_notification op 可能 ignore
      -> channel 可能 off
          -> 系统总开关可能关闭
              -> 前台服务类型可能不合规
```

例如照片：

```text
READ_MEDIA_IMAGES granted
  -> 用户可能只选择部分照片
      -> 返回集合不等于全相册
          -> App 必须提供重新选择入口
```

### 权限事故的证据格式

建议把权限证据写成这种结构：

```text
packageName:
uid:
targetSdk:
androidVersion:
permission:
manifestDeclared:
protectionLevel:
runtimeGrant:
permissionFlags:
appOps:
foregroundState:
userSetting:
apiResult:
firstEvidence:
rootCause:
```

这比一句：

```text
用户没开权限
```

可靠得多。

## 第七部分：本节自测

请回答：

- Manifest 声明权限和用户授权有什么区别？
- normal、dangerous、signature 权限有什么区别？
- 为什么权限申请应该发生在用户触发功能时？
- targetSdk 为什么会影响权限行为？
- 用户授权后，为什么还要看 AppOps 或系统设置？
- 一个权限问题诊断报告必须记录哪些字段？
- 为什么权限组不能代替具体 permission 判断？
- 为什么 signature 权限不是普通 App 写进 Manifest 就能获得？
- 权限系统里 PackageManagerService、PermissionManagerService、PermissionController 分别更像负责什么？
- 为什么权限问题要分安装阶段、授权阶段和使用阶段排查？

## 本节小结

权限系统的核心不是弹窗，而是最小必要授权。

```text
声明
  -> 说明 App 可能需要什么

申请
  -> 让用户在具体场景里做决定

授权
  -> 只是通过第一道门

AppOps / 设置 / targetSdk
  -> 还可能继续影响实际能力
```

第 23.3 节的关键结论是：

```text
权限不是越早申请越稳，而是越贴近用户场景越可信。
```
