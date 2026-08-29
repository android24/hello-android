# 23.4 AppOps：为什么授权了也可能被系统继续拦

很多 Android 权限问题最让人困惑的地方是：

```text
权限明明 granted
功能还是不能用
```

这时只看 `checkSelfPermission()` 往往不够。

你还要知道 AppOps。

AppOps 可以粗略理解成：

```text
系统对某些敏感操作的运行时开关、审计和策略层。
```

它不是 Manifest 权限的简单重复，而是更接近“这个 App 此刻是否允许执行这个操作”。

## 本节先记住三句话

```text
permission 问“有没有资格”，AppOps 问“这次操作现在让不让做”。
AppOps 常嵌在定位、通知、媒体等具体系统服务里。
权限 granted 后仍失败时，AppOps 往往是第二证据。
```

## 贯穿案例：学习提醒为什么不弹

`Hello Android 学习中心`每天晚上提醒用户复习。

用户反馈：

```text
通知权限显示已开启，但学习提醒仍然不弹。
```

这时你不能只看：

```text
POST_NOTIFICATIONS 是否 granted
```

还要继续看：

```text
post_notification AppOps 是否 allow
通知总开关是否关闭
通知 channel 是否关闭
前台服务通知类型是否合规
定时任务是否真的被触发
```

AppOps 会把“用户是否曾经授权”推进到“系统此刻是否允许这次敏感操作”。

## 安全侦探问题

现在有三条证据：

```text
POST_NOTIFICATIONS granted
post_notification ignore
channel enabled
```

你会把根因先归到哪一层？

```text
permission
AppOps
notification channel
业务调度
```

请说明理由。

## 本节定位

本节负责回答：

- AppOps 是什么？
- permission 和 app op 有什么区别？
- 为什么用户授权后，系统设置或策略仍可能拦截？
- 如何用 `cmd appops` 和 `dumpsys` 观察 AppOps？
- AppOps 在定位、通知、存储、后台能力中如何出现？

## 学习目标

学完本节后，你应该能够：

- 理解 permission 是能力声明和授权，AppOps 是敏感操作控制和审计。
- 知道某些 API 会同时检查 permission 和 AppOps。
- 能用 `adb shell cmd appops get <package>` 获取第一证据。
- 能解释“权限通过但功能不可用”的常见原因。
- 能把 AppOps 纳入权限事故报告。

## 第一部分：permission 和 AppOps 的区别

先用一句话区分：

```text
permission 问：你有没有资格？
AppOps 问：这个操作现在让不让做？
```

例如：

```text
Manifest 声明 ACCESS_FINE_LOCATION
用户运行时授权 location
AppOps 仍然可能根据前后台、用户设置、系统策略控制实际操作
```

常见观察：

```bash
adb shell cmd appops get com.example.app
```

你可能看到类似：

```text
android:fine_location: allow
android:camera: allow
android:post_notification: ignore
```

这表示系统对具体操作有自己的记录。

## 第二部分：AppOps 像一道运行时闸门

Android 运行敏感操作时，可能会经历：

```text
检查调用方 UID / package
  -> 检查 Manifest 权限
      -> 检查 runtime permission 授权
          -> 检查 AppOps mode
              -> 检查前后台状态、targetSdk 和系统策略
                  -> 执行或拒绝
```

AppOps mode 可以粗略理解为：

```text
allow
ignore
deny
foreground
default
```

不同操作的语义会不同。

例如 `foreground` 可能意味着：

```text
前台允许
后台不允许或降级
```

这就是为什么定位、通知、后台能力经常不能只看权限。

## 第三部分：AppOps 常见出现位置

### 位置

定位是 AppOps 参与非常明显的场景。

你要看：

```text
ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION
ACCESS_BACKGROUND_LOCATION
AppOps fine_location / coarse_location
前后台状态
是否只允许使用期间访问
```

### 通知

通知也可能受 AppOps 和通知渠道影响。

你要看：

```text
POST_NOTIFICATIONS
AppOps post_notification
NotificationChannel 是否关闭
系统通知总开关
厂商通知策略
```

### 存储和媒体

媒体访问会受权限、部分授权和 AppOps 影响。

你要看：

```text
READ_MEDIA_IMAGES / VIDEO / AUDIO
用户是否只授权部分照片
AppOps 是否允许对应访问
Photo Picker 是否更合适
```

### 特殊能力

一些特殊能力不走普通 runtime permission 弹窗。

例如：

```text
悬浮窗
修改系统设置
无障碍
通知监听
所有文件访问
```

它们通常需要进入系统设置页，由用户明确打开。

## 第四部分：为什么 AppOps 适合做事故证据

如果用户反馈：

```text
我已经授权了，为什么还是不能用？
```

你可以把排查分成：

```text
permission grant
  -> 是否授权

appops mode
  -> 实际操作是否允许

channel / setting
  -> 系统设置或功能开关是否关闭

foreground / background
  -> 当前状态是否满足策略
```

这会让排查从“用户说了什么”变成“系统记录了什么”。

## 第五部分：AppOps 命令观察

推荐命令：

```bash
adb shell cmd appops get com.helloandroid.storage
adb shell cmd appops get com.helloandroid.storage READ_MEDIA_IMAGES
adb shell dumpsys package com.helloandroid.storage
adb shell dumpsys notification
```

也可以在调试环境里临时设置某些 op：

```bash
adb shell cmd appops set com.example.app POST_NOTIFICATION ignore
adb shell cmd appops set com.example.app POST_NOTIFICATION allow
```

注意：

```text
不要把调试命令当成线上修复方案。
线上修复要从权限解释、设置引导、降级路径和产品语义入手。
```

## 第六部分：AppOps 事故排查

### 事故一：通知权限授权了，但仍然不弹

排查：

```text
POST_NOTIFICATIONS 是否 granted？
AppOps post_notification 是 allow 还是 ignore？
通知渠道是否关闭？
系统通知总开关是否关闭？
前台服务通知是否合规？
```

### 事故二：定位只在前台可用

排查：

```text
用户是否选择“仅使用期间允许”？
是否申请后台定位？
AppOps 是否是 foreground？
调用是否发生在后台？
是否应该改成前台服务或用户触发？
```

### 事故三：所有文件访问在测试机可用，线上不可用

排查：

```text
是否依赖 MANAGE_EXTERNAL_STORAGE？
用户是否在特殊权限设置页打开？
审核或分发渠道是否允许？
是否可以改成 SAF / MediaStore？
```

## 第六部分补充：AppOps 不只是开关，还是访问记录

如果 permission 像门票，AppOps 更像闸机和监控记录。

它有两个重要职责：

```text
access control
  -> 控制这次敏感操作能不能继续

tracking
  -> 记录敏感操作何时发生、是否活跃、由谁触发
```

这解释了为什么 AppOps 适合做事故证据。

用户说：

```text
我已经授权了。
```

系统记录说：

```text
这个 op 当前是 ignore / foreground / allow。
```

后者更接近真实执行现场。

### check、note、start 的差别

从概念上看，AppOps 有几类动作：

```text
check
  -> 看看某个 op 当前是否允许

note
  -> 记录一次离散访问，并检查是否允许

start / finish
  -> 记录一个持续访问的开始和结束

proxy note
  -> 记录一个 App 代替另一个 App 访问敏感能力
```

比如读取一次剪贴板、访问一次位置、开启一段麦克风录音，它们的访问形态并不一样。

所以你可以这样理解：

```text
check 更像预检
note 更像刷闸
start / finish 更像开始和结束一段占用
```

不要把“预检通过”误认为“真正访问一定会成功”。

真实 API 里还会继续结合：

```text
uid / package 是否匹配
权限是否授权
op mode
前后台状态
attribution tag
系统设置
设备策略
```

### permission 和 op 的映射

很多 runtime permission 会有关联的 app-op。

这也是为什么：

```text
permission granted
  -> 仍然要看对应 op
```

例如排查定位时，你不要只写：

```text
ACCESS_FINE_LOCATION granted
```

还要继续看：

```text
fine_location op
coarse_location op
foreground / background 状态
是否只允许使用期间访问
```

排查通知时也是一样：

```text
POST_NOTIFICATIONS granted
post_notification op
notification channel
系统通知总开关
前台服务通知策略
```

### attribution tag：为什么“谁用的”还可以更细

较新的 Android 隐私体系里，系统不只关心：

```text
哪个 App 使用了敏感能力
```

还可能关心：

```text
App 里的哪个功能模块、哪个调用路径使用了敏感能力
```

这就是 attribution tag 的意义。

它让一个 App 内部可以更细地标记访问来源，例如：

```text
course_camera_scan
profile_photo_picker
background_location_sync
```

课程里不要求你一开始就把 attribution 做到极致，但要形成意识：

```text
敏感访问最好能被解释、被归因、被审计。
```

安全不是只看用户点没点同意，还要看同意之后能力被谁、在何时、为了什么使用。

## 第六部分补充二：AppOps 如何嵌入具体系统服务

AppOps 不是孤零零站在旁边的开关。

它通常嵌在具体系统服务的敏感操作链路里。

例如定位可以粗略理解成：

```text
App 请求位置
  -> LocationManagerService
      -> 检查调用方 uid / package / attribution
          -> 检查 location permission
              -> 检查 AppOps fine_location / coarse_location
                  -> 检查前后台状态和后台定位策略
                      -> 返回位置、降级精度或拒绝
```

通知可以粗略理解成：

```text
App 发送通知
  -> NotificationManagerService
      -> 检查 POST_NOTIFICATIONS 权限
          -> 检查 AppOps post_notification
              -> 检查通知总开关
                  -> 检查 channel 状态
                      -> 检查前台服务通知规则
                          -> 展示、静默、丢弃或报错
```

媒体访问可以粗略理解成：

```text
App 查询图片
  -> MediaProvider / MediaStore
      -> 检查媒体权限或 Photo Picker 授权
          -> 检查 AppOps
              -> 检查用户是否只授权部分媒体
                  -> 返回允许范围内的数据
```

所以 AppOps 的真实价值是：

```text
把“用户是否授权过某类能力”
变成
“这一次具体敏感操作是否应该被放行和记录”
```

### 为什么 AppOps 常常是第二证据

权限事故排查里，第一证据通常是：

```text
dumpsys package
  -> requested permissions
  -> granted permissions
```

第二证据通常就是：

```text
cmd appops get <package>
```

因为它能回答：

```text
权限看起来通过后，系统对具体操作的态度是什么？
```

典型组合：

```text
permission granted + appops allow
  -> 继续看业务参数、设备能力、channel、API 返回

permission granted + appops ignore
  -> 很可能被系统策略或用户设置拦住

permission granted + appops foreground
  -> 前台可用，后台可能被拦

permission denied + appops allow
  -> 仍不能简单认为可用，要回到权限层
```

AppOps 不是代替 permission，而是补上“实际操作层”的证据。

### Demo 应该如何表现 AppOps

第 23 章 Demo 不一定能在 App 内直接读取所有 AppOps 状态。

更合理的设计是：

```text
App 页面展示：
  -> 当前权限状态
  -> 当前业务操作
  -> 推荐 adb 命令
  -> 用户填入或截图 appops 结果
  -> 页面根据结果解释事故
```

这样读者会形成习惯：

```text
App 内观察
  -> 系统命令取证
      -> 回到 App 写诊断结论
```

这比只做一个“请求权限按钮”更接近真实工作。

## 第七部分：本节自测

请回答：

- permission 和 AppOps 分别回答什么问题？
- 为什么 `checkSelfPermission()` 返回 granted 仍然可能不能用？
- `cmd appops get <package>` 可以帮你观察什么？
- 通知不弹时除了权限还要看哪些状态？
- 前台定位和后台定位为什么不能只看一个权限？
- AppOps 调试命令为什么不能当作线上修复方案？
- AppOps 的 check、note、start 分别适合理解成什么？
- 为什么 AppOps 同时适合做访问控制和访问审计？
- 为什么 AppOps 通常嵌在 LocationManagerService、NotificationManagerService、MediaProvider 这类系统服务里？
- 权限 granted + AppOps foreground 的事故应该怎么解释？

## 本节小结

AppOps 是 Android 安全和隐私治理里非常重要的一层。

```text
permission
  -> 你是否被授权拥有某类能力

AppOps
  -> 这个具体敏感操作当前是否允许、如何记录、如何被策略控制
```

第 23.4 节的关键结论是：

```text
权限通过不是终点，系统实际放行才是功能可用的证据。
```
