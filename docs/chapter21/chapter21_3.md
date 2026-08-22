# 21.3 Service 与 Foreground Service：后台执行的边界

Service 是 Android 里最容易被误解的组件之一。

很多初学者会以为：

```text
Service = 后台线程
```

这是一个非常危险的误解。

Service 首先是组件，不是线程。它的生命周期由系统管理，它的回调默认运行在主线程。它可以承载后台语义，但并不自动拥有无限后台执行能力。

## 本节定位

本节负责回答：

- Service 到底是什么，不是什么？
- 普通 Service 和 Foreground Service 的边界在哪里？
- 为什么后台启动 Service 会受到限制？
- 前台服务为什么必须让用户感知？
- 哪些业务适合前台服务，哪些不适合？

## 学习目标

学完本节后，你应该能够：

- 知道 Service 不是后台线程。
- 理解 started Service、bound Service 和 Foreground Service 的差异。
- 知道前台服务适合用户正在感知的持续任务。
- 能解释为什么后台 Service 启动失败不一定是代码写错。
- 能为下载、播放、导航、同步等场景选择更合适的方案。

## 第一部分：Service 不是后台线程

先记住一句话：

```text
Service 的回调默认在主线程。
```

例如：

```text
onCreate
onStartCommand
onBind
onDestroy
```

这些回调并不会自动跑到 worker 线程。

如果你在 `onStartCommand` 里直接做耗时任务：

```text
下载大文件
读写大量数据库
压缩图片
同步等待网络
长时间 sleep
```

仍然可能阻塞主线程，甚至触发 ANR。

所以 Service 的正确理解应该是：

```text
Service 提供一种没有界面的组件生命周期
  -> 但耗时工作仍然要交给线程、协程、线程池或系统调度机制
```

## 第二部分：Started Service

Started Service 通常通过 `startService` 或相关方式启动。

它的语义是：

```text
请系统启动这个组件，让它执行某项工作。
```

但随着后台限制加强，App 在后台直接启动普通 Service 会受到限制。

为什么？

因为普通 Service 对用户不可见。如果系统允许每个 App 在后台随意启动 Service，就会回到早期后台滥用的问题。

适合普通 Service 的场景越来越少，尤其是长时间后台工作，通常要重新评估是否应该改为：

- Foreground Service。
- WorkManager。
- JobScheduler。
- 页面生命周期内任务。
- Push 或服务端方案。

## 第三部分：Bound Service

Bound Service 通过 `bindService` 建立连接。

它的语义更像：

```text
另一个组件想和这个服务保持一段通信关系。
```

常见场景：

- Activity 绑定音乐播放服务。
- App 内部绑定远程进程服务。
- 通过 AIDL 暴露跨进程能力。

Bound Service 的重点不是“后台执行”，而是“组件通信”和“能力暴露”。

如果 bound service 位于远程进程，就会涉及第 11 章的 Binder、第 19 章的多进程、第 20 章的 Binder 阻塞风险。

## 第四部分：Foreground Service

Foreground Service 的核心语义是：

```text
这个任务正在持续运行，并且用户应该知道它在运行。
```

典型场景：

| 场景 | 为什么适合前台服务 |
| --- | --- |
| 音乐播放 | 用户正在听，任务持续存在 |
| 导航定位 | 用户正在导航，需要持续定位 |
| 离线课程下载 | 用户知道正在下载，可能需要进度通知 |
| 运动记录 | 用户正在记录运动轨迹 |
| 通话或录音 | 用户强感知，且需要持续执行 |

前台服务通常需要通知。

通知不是累赘，而是契约：

```text
App 正在消耗资源
  -> 用户应该知道
      -> 用户应该能理解为什么
          -> 用户应该有办法停止或管理
```

## 第五部分：前台服务的版本意识

前台服务不是一个“声明通知就能长期运行”的万能通道。Android 对前台服务的限制一直在收紧。

可以用下面这张表建立版本意识：

| 系统方向 | 你需要理解的变化 |
| --- | --- |
| Android 8.0 以后 | 后台 Service 执行受到限制，普通后台 Service 不再适合承担长期任务 |
| Android 9 以后 | 使用前台服务需要 `FOREGROUND_SERVICE` 权限 |
| Android 10 / 11 以后 | 涉及 location、camera、microphone 等能力时，需要声明对应前台服务类型 |
| Android 12 以后 | 后台启动前台服务受到限制，不满足例外条件时会抛 `ForegroundServiceStartNotAllowedException` |
| Android 14 以后 | 目标版本较高时，前台服务必须声明合适类型，并申请对应类型权限 |
| Android 15 以后 | 某些前台服务类型存在更严格的启动和运行限制，例如 dataSync、mediaProcessing 等 |
| Android 16 以后 | 从前台服务启动的后台 Job / WorkManager 任务也可能继续受到运行配额影响 |

这张表不是让你背版本号，而是提醒你：

```text
前台服务正在从“后台常驻工具”
  -> 变成“用户可感知、类型明确、权限匹配、运行受控的任务契约”
```

所以写前台服务时，不能只问“能不能启动”，还要问：

```text
启动时 App 是否在后台？
是否属于允许后台启动的例外？
Manifest 是否声明 foregroundServiceType？
是否申请了对应前台服务权限？
是否还需要运行时权限？
通知是否解释清楚任务？
任务是否真的需要持续运行？
```

## 第六部分：前台服务类型

较新的 Android 版本越来越强调前台服务类型。

你不能再把所有后台长任务都塞进一个模糊的前台服务里。

常见类型包括：

```text
dataSync
mediaPlayback
location
camera
microphone
connectedDevice
health
phoneCall
remoteMessaging
mediaProcessing
```

具体类型和权限会随着 Android 版本变化而变化。

学习时不要死记某个版本的完整表，而要理解设计原则：

```text
你声明的前台服务类型
  -> 应该和用户可感知的任务一致
      -> 系统和用户才知道你为什么在后台持续运行
```

## 第七部分：前台服务和 WorkManager 的边界

有些任务看起来既像前台服务，又像 WorkManager。

例如：

```text
用户点击“下载离线课程”
  -> 文件可能很大
      -> 用户希望看到进度
          -> 下载失败后要能恢复
```

这类任务需要拆开看：

| 关注点 | 更偏向 |
| --- | --- |
| 用户正在等进度 | Foreground Service / 用户可感知通知 |
| 短时可靠上传或同步 | WorkManager |
| 长时间、用户主动发起的数据传输 | 可以关注 user-initiated data transfer job |
| 任务链、约束、重试 | WorkManager |
| 音乐、导航、录音等持续用户感知任务 | Foreground Service |

不要把所有“长任务”都塞进前台服务，也不要把所有“可靠任务”都塞进 WorkManager。

真正要判断的是：

```text
用户是否正在看着它发生？
系统是否可以选择更合适的时机？
失败后是否需要重试和恢复？
```

## 第八部分：常见误用

### 误用一：用 Service 做长时间计算

问题：

```text
Service 回调在主线程
长时间计算可能触发 ANR
后台执行也可能被限制
```

更合理：

```text
短任务：协程 / 线程池
可靠任务：WorkManager
用户可感知长任务：Foreground Service
```

### 误用二：用前台服务做保活

问题：

```text
通知解释不了真实业务
用户可能反感
系统版本越高越容易受限制
审核和合规风险增加
```

更合理：

```text
重新设计业务触发方式
使用 Push
使用 WorkManager
接受系统调度
做好状态恢复
```

### 误用三：在前台服务里同步等待远端 Binder

问题：

```text
服务虽然前台了
但线程模型仍然可能出问题
同步等待可能导致阻塞、超时或 ANR
```

更合理：

```text
异步调用
超时控制
进度状态持久化
失败可恢复
```

## 第九部分：排查前台服务问题看什么

遇到前台服务启动失败或行为异常时，建议检查：

```text
启动时 App 是否在后台
是否满足前台服务启动条件
是否及时调用 startForeground
通知渠道是否创建
通知权限是否授予
Manifest 是否声明服务类型
服务类型和实际行为是否匹配
是否缺少相关权限
是否有异常栈或系统拒绝日志
是否触发运行时长限制或系统配额
是否在 BOOT_COMPLETED 等受限制场景里启动
```

常用观察入口：

```bash
adb logcat | grep -i service
adb shell dumpsys activity services
adb shell dumpsys activity processes
adb shell dumpsys notification
```

## 本节小结

Service 的正确打开方式是：

```text
Service 是组件
Foreground Service 是用户可感知的持续任务契约
后台长任务应该优先考虑系统调度和业务语义
```

不要问：

```text
我能不能用 Service 保活？
```

要问：

```text
这个任务是否真的需要用户感知地持续运行？
如果不需要，它是不是应该交给 WorkManager、JobScheduler 或业务恢复机制？
```
