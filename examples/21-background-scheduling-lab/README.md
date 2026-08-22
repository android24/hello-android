# 第21章示例工程：后台调度观察实验室

这个工程对应课程第 21 章：后台任务、前台服务、Alarm、JobScheduler 与系统后台限制。

它不是普通 API demo，而是一块后台任务调度观察台。你可以选择业务场景，启动前台服务，入队 WorkManager 任务，注册 Alarm，阅读 Doze / JobScheduler / Alarm 命令卡，完成 ADB 证据挑战，并把一次“任务为什么没执行”的事故写成诊断报告。

## 学习目标

运行本工程后，你应该能回答：

- 为什么 Service 不是后台线程？
- Foreground Service 为什么必须让用户感知？
- WorkManager 入队后为什么不一定立刻运行？
- 约束过严时，任务延迟为什么不是任务丢失？
- 如何用真实 `WorkInfo` 区分“已入队”“正在执行”“已经取消”和“执行完成”？
- AlarmManager 为什么是时间入口，而不是后台执行框架？
- Doze、App Standby、Battery Saver 和厂商策略会如何影响后台任务？
- `dumpsys jobscheduler / alarm / deviceidle` 分别应该看什么？
- 一个后台任务事故如何从现象走向证据、系统状态、根因、修复和回归？

## 工程结构

```text
21-background-scheduling-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/helloandroid/background/
      MainActivity.kt
      BackgroundSchedulingApplication.kt
      BackgroundSchedulingLabScreen.kt
      BackgroundLabState.kt
      BackgroundLabStore.kt
      BackgroundUtils.kt
      CourseDownloadForegroundService.kt
      CourseProgressSyncWorker.kt
      StudyReminderReceiver.kt
```

## 实验区域

### 调度任务板

页面顶部提供学习任务板，把本章拆成几个观察点：

```text
确认运行现场
完成三问法决策
观察用户可感知任务
观察可靠任务状态机
观察约束延迟
观察时间触发任务
完成 ADB 证据挑战
观察系统证据
完成事故剧本
完成复盘报告
```

不要一上来乱点按钮。先看任务板，再做第一个未完成的观察点。

### 事故剧本模式

内置 3 个事故剧本：

```text
剧本 A：离线课程切后台后停住
剧本 B：学习记录同步晚了半小时
剧本 C：学习提醒晚了几分钟
```

剧本模式会先给出现象、第一线索、隐藏陷阱和下一步动作。它训练的是“从模糊用户反馈开始判断机制”的能力。

### 任务决策卡

使用第 21 章的三问法：

```text
用户是否正在感知？
是否必须现在完成？
失败后是否必须恢复？
```

选择不同场景后，页面会给出推荐机制：

```text
离线下载 -> Foreground Service + 通知 + 状态持久化
学习同步 -> WorkManager / JobScheduler
学习提醒 -> AlarmManager + 通知
高频轮询 -> 重新设计业务语义
```

### Foreground Service 实验区

点击“启动下载服务”后，工程会启动 `CourseDownloadForegroundService`。

观察点：

```text
startForegroundService
startForeground
通知
onCreate
onStartCommand
onDestroy
事件时间线
```

它用来说明：前台服务不是保活工具，而是用户可感知持续任务的契约。

### WorkManager / JobScheduler 实验区

提供两个按钮：

```text
入队普通同步
入队强约束同步
刷新真实状态
取消同步任务
```

普通同步只要求网络连接。强约束同步要求：

```text
UNMETERED + CHARGING
```

这用来观察：

```text
Work 已经 ENQUEUED
  -> 不代表 Worker 已经 RUNNING
      -> 约束、Doze、待机桶、配额都会影响执行时机
```

其中“刷新真实状态”会读取 WorkManager 保存的 `WorkInfo`。这一步很重要，因为真实排障时不能只相信页面上的一句“任务已开始”，而要看系统调度层是否真的接收了任务、当前状态是什么、尝试次数是多少。

推荐玩法：

```text
入队普通同步 -> 立刻刷新真实状态 -> 等几秒再刷新
入队强约束同步 -> 断开 Wi-Fi 或拔掉充电 -> 刷新真实状态
取消同步任务 -> 再刷新真实状态 -> 对比 CANCELLED / IDLE
```

### Alarm 实验区

点击“注册 15 秒提醒”后，工程会注册一个 `setWindow` Alarm。

它用来说明：

```text
Alarm 是时间触发入口
  -> setWindow 表达时间窗口
      -> 普通提醒不等于强精确闹钟
```

### 系统证据命令卡

页面会列出推荐命令：

```bash
adb shell dumpsys activity services
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys deviceidle
adb shell dumpsys battery
adb shell dumpsys notification
adb shell dumpsys activity processes
```

这些命令需要你在终端中手动执行。Demo 不替你自动读系统状态，因为第 21 章真正要训练的是：你知道第一证据在哪里。

### ADB 证据挑战

命令卡下面新增了 3 个挑战：

```text
完成 Work 证据
完成 Alarm 证据
完成 Doze 证据
```

不要把它当成“打卡按钮”。正确玩法是：先执行对应命令，再回到页面标记完成，并把观察写进诊断报告。

你要训练的是下面这条链路：

```text
页面现象
  -> 事件时间线
      -> WorkInfo / Alarm 注册状态
          -> dumpsys 系统证据
              -> 根因判断
```

## 推荐学习路线

1. 先刷新运行现场，记录 `package / process / pid / uid / sdk`。
2. 选择一个事故剧本，只读现象和第一线索，不急着看答案。
3. 在任务决策卡里选择对应场景，判断它应该用哪种后台机制。
4. 如果是离线下载，启动前台服务，观察通知和事件时间线。
5. 如果是学习同步，入队普通 Work 和强约束 Work，对比状态差异。
6. 点击“刷新真实状态”，用 WorkManager 的 `WorkInfo` 校验页面判断。
7. 点击“取消同步任务”，观察取消请求和真实状态之间的关系。
8. 如果是学习提醒，注册 Alarm，记录期望时间和实际触发。
9. 阅读系统证据命令卡，在终端执行至少一个 dumpsys 命令。
10. 完成一个 ADB 证据挑战，把命令输出和页面事件对上。
11. 最后完成后台任务诊断报告。

## 通关标准

你不需要把所有按钮都点一遍，但至少应该能做到：

- 能从业务语义判断应该用 Foreground Service、WorkManager 还是 AlarmManager。
- 能解释为什么 Work 入队不等于立刻执行。
- 能用 `WorkInfo` 说明任务当前处于哪个状态。
- 能解释任务取消为什么也需要进入状态机观察。
- 能解释为什么普通 Alarm 可能不准时。
- 能等待并记录一次 Alarm 真实触发。
- 能解释为什么前台服务必须展示用户可感知通知。
- 能说出一次 `dumpsys jobscheduler / alarm / deviceidle` 观察到了什么。
- 能把一次后台任务问题写成“现象、证据、系统状态、根因、修复、回归”。

## 小挑战

请分析下面事故：

```text
用户点击下载离线课程后切后台。
通知没有出现。
30 分钟后回来，下载进度消失。
logcat 没有 Crash。
任务也没有 WorkManager 记录。
```

你需要回答：

- 这是 Crash、ANR，还是后台任务设计问题？
- 第一证据是什么？
- 下载任务是否用户可感知？
- 是否应该使用 Foreground Service？
- 进度为什么不能只存在内存？
- 如何设计恢复和回归验证？
