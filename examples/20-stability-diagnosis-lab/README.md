# 第20章示例工程：稳定性诊断实验室

这个工程对应课程第 20 章：ANR、Crash、Watchdog 与系统稳定性诊断机制。

它不是普通功能 demo，而是一块事故演练场。你可以在页面里触发主线程阻塞、锁等待、慢 Broadcast、慢 Service、remote Binder 延迟，也可以阅读 Java Crash、Native tombstone、DropBox / bugreport 的样例，并把证据整理成稳定性诊断报告。

## 学习目标

运行本工程后，你应该能回答：

- ANR 和普通卡顿有什么区别？
- Input、Broadcast、Service、Binder、锁等待分别会留下什么线索？
- Java Crash 应该如何提取异常类型、线程和第一业务栈？
- Native Crash 为什么要看 signal、tombstone、so、ABI 和符号化？
- DropBox、dumpsys、bugreport 分别适合保存或查询什么证据？
- 一个稳定性问题如何从现象走向证据、根因、修复和回归？

## 工程结构

```text
20-stability-diagnosis-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/helloandroid/stability/
      MainActivity.kt
      StabilityLabApplication.kt
      StabilityLabScreen.kt
      StabilityLabState.kt
      StabilityLabStore.kt
      StabilityUtils.kt
      RemoteDelayService.kt
      SlowBroadcastReceiver.kt
      SlowStartService.kt
```

## 实验区域

### 事故任务板

页面顶部提供一块任务板，把本章知识拆成 5 个连续任务：

```text
确认案发现场
制造一次可解释的 ANR
拆开等待链
区分 Java 与 Native 现场
完成稳定性复盘
```

它不是装饰性的进度条，而是本章的学习节奏。每完成一步，都要能说出“我看到了什么证据、这个证据能支持什么结论、下一步应该查哪里”。

### 事故剧本模式

任务板下面提供 3 个事故剧本：

```text
剧本 A：课程详情页卡死
剧本 B：下载完成后闪退
剧本 C：图片页少数机型闪退
```

剧本模式不会立刻让 App 卡死或退出，它会先给出“用户现象、第一线索、隐藏陷阱和下一步动作”。建议先进入剧本，读完证据，再选择是否触发对应实验。

这个区域解决的是另一个问题：真实线上事故通常不是按知识点出现的。用户不会告诉你“这是锁等待 ANR”，他只会说“页面卡住了”。剧本模式就是让你从模糊现象开始，训练自己一步步把问题归类。

### 运行现场

展示当前：

```text
packageName
processName
pid
uid
threadName
oom_score_adj
```

稳定性排查不要一上来猜代码，先确认事故发生在哪个进程、哪个线程、哪个系统状态。

### ANR 实验区

提供这些危险按钮：

```text
主线程阻塞
锁等待 ANR
慢 Broadcast
慢 Service
绑定 remote
remote 延迟
```

ANR 与 Crash 按钮都带二次确认。建议先读清楚提示，再触发实验；触发后不要急着重启，先观察页面、logcat、trace 阅读卡和事件轨迹。

它们分别对应第 20 章的：

- `20.2 ANR：系统如何判断 App 无响应`
- `20.3 ANR trace：如何读 main、Binder、锁和系统超时`

其中 `remote 延迟` 使用异步 Messenger，不会直接卡住主进程，但它用来说明真实项目中如果主线程同步等待远端 Binder，可能把远端慢扩散成 ANR。

### Trace 阅读卡

页面会根据实验场景切换 trace 阅读提示：

```text
reason
main thread
waiting chain
conclusion
sample trace
```

目标不是让你背 trace，而是训练你写出能指导修复的结论。

### Java Crash 实验区

包含：

```text
读取 Java Crash 样例
触发主线程 Crash
触发后台 Crash
```

触发 Crash 会导致 App 退出。建议先阅读样例，再在 debug 设备上触发。

### Native tombstone 观察区

当前工程不直接制造 native crash，而是提供教学 tombstone 样例，训练你识别：

```text
signal
fault addr
crashing thread
so
ABI
JNI bridge
```

真实 native crash 后，还需要符号表和对应版本的 so 才能继续定位。

### DropBox / bugreport 证据区

提供 DropBox / bugreport 摘要样例，训练你从：

```text
DropBox tag
Time
Process
Reason
dumpsys activity processes
logcat
```

串出下一步排查路线。

## 推荐观察命令

运行工程后可以配合：

```bash
adb logcat -s StabilityLab
adb logcat | grep -i anr
adb shell dumpsys activity anr
adb shell dumpsys activity processes
adb shell dumpsys dropbox
adb shell dumpsys meminfo com.helloandroid.stability
adb bugreport
```

观察进程：

```bash
adb shell ps -A | grep com.helloandroid.stability
adb shell cat /proc/<pid>/oom_score_adj
```

## 建议学习路线

1. 先刷新运行现场，记录 `processName / pid / threadName / oom_score_adj`。
2. 看事故任务板，只做第一个未完成任务，不要同时乱点多个事故按钮。
3. 选择一个事故剧本，先从用户现象和第一线索判断问题类型。
4. 点击“主线程阻塞”，观察 UI 卡住、trace 阅读卡和事件轨迹。
5. 点击“锁等待 ANR”，理解 main 为什么只是被拖住，持锁线程才是关键证据。
6. 点击“慢 Broadcast”和“慢 Service”，对比组件回调超时。
7. 绑定 remote，再点击“remote 延迟”，理解 Binder 诊断为什么要跨进程。
8. 先读取 Java Crash 样例，再决定是否触发主线程或后台线程 Crash。
9. 阅读 tombstone、DropBox / bugreport 样例，提取第一证据。
10. 点击诊断卡，最后完成稳定性诊断报告。

## 通关标准

你不需要把页面上的分数刷满才算学会，但至少应该完成这些动作：

- 能说清楚当前事故属于 ANR、Java Crash、Native Crash 还是系统级问题。
- 能从 `reason / main thread / waiting chain / first app stack / signal / DropBox tag` 中挑出第一证据。
- 能把“现象、证据、根因、修复、回归”写成一份稳定性诊断报告。
- 能解释为什么主线程不能等待锁、I/O 或远端同步调用。
- 能说明 DropBox 和 bugreport 是证据入口，不是最终结论。
- 能从一个模糊事故剧本出发，主动选择下一步应该读 trace、stack、tombstone 还是 bugreport。

## 小挑战

请分析下面事故：

```text
用户点击课程详情页后卡住。
logcat 出现 Input dispatching timed out。
trace 显示 main waiting to lock CourseCache。
worker 持有 CourseCache，并在锁内等待 remote Service。
```

你需要回答：

- 这是 Crash 还是 ANR？
- 第一证据是什么？
- main 在等什么？
- 谁持有锁？
- remote 是否参与？
- 修复方向是什么？
- 如何回归验证？
