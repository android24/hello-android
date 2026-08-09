# 第19章示例工程：进程、Zygote、多进程与 ANR 观察实验室

这个工程对应课程第 19 章：Android 进程模型、Zygote、应用沙箱与内存管理机制。

它不是一个普通页面 demo，而是一个小型“进程调度实验室”。你可以在页面里观察当前进程身份，启动 `:remote` 进程和 `isolatedProcess`，对比主进程和 remote 进程的单例状态，读取并记录 `oom_score_adj`，模拟进程死亡后的状态恢复，触发 Binder death，并模拟多种 ANR 场景。

## 学习目标

运行本工程后，你应该能把下面这些问题讲清楚：

- 当前代码运行在哪个进程、哪个线程里？
- `pid`、`ppid`、`uid`、`processName` 分别说明什么？
- App 进程为什么通常由 Zygote fork 出来？
- `ContentProvider`、`Application`、`Activity` 的初始化顺序如何观察？
- 主线程为什么只适合做 UI、输入和生命周期调度？
- remote Service 为什么是独立进程，而不是一条后台线程？
- `isolatedProcess` 和普通 `:remote` 进程有什么区别？
- 主进程和 remote 进程为什么不共享同一个单例对象？
- 进程死亡后，为什么内存状态会丢，而持久化状态可以恢复？
- `oom_score_adj` 如何帮助判断进程重要性？
- ANR trace 应该先看哪条线程，再看哪些锁和系统超时线索？
- 主线程阻塞、Broadcast 阻塞、Service 阻塞、remote Binder 阻塞分别如何制造 ANR 风险？

## 工程结构

```text
19-process-zygote-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/helloandroid/process/
      MainActivity.kt
      ProcessLabApplication.kt
      ProcessInitProvider.kt
      ProcessLabScreen.kt
      ProcessLabState.kt
      ProcessLabStore.kt
      ProcessUtils.kt
      IsolatedInspectorService.kt
      RemoteInspectorService.kt
      SlowBroadcastReceiver.kt
      SlowStartService.kt
  quality/
    process-diagnosis-report-template.md
    process-reading-notes.md
```

## 实验区域

### 进程身份证

页面会展示：

```text
packageName
processName
pid / ppid
uid
oom_score_adj
threadName
thread count
singleton
```

这张卡用于回答“我现在究竟运行在哪个进程里”。排查多进程、后台死亡、沙箱权限、进程重建时，先看它。

### 进程死亡与状态恢复实验

页面提供两个状态：

```text
内存草稿
持久化草稿
```

推荐玩法：

```text
内存 +1
  -> 保存草稿
      -> 切后台
          -> adb shell am kill com.helloandroid.process
              -> 回到 App
                  -> 刷新进程身份证
                      -> 从持久化恢复
```

重点观察：

```text
current pid 是否变化
saved pid 是否还是旧进程
内存草稿是否回到 0
持久化草稿是否仍然存在
```

这个实验用来解释真实项目里最常见的一类问题：后台回来白屏、草稿丢失、登录态短暂异常、详情页参数缺失。根因往往不是“系统不稳定”，而是关键状态只存在进程内存里。

### Zygote 与启动轨迹

工程内置了一个 `ContentProvider`、一个 `Application` 和一个 `Activity`。它们都会向事件轨迹写入生命周期记录。

你可以观察：

```text
ContentProvider.onCreate
Application.onCreate
Activity.onCreate
```

这条链路对应课程中提到的：

```text
Zygote fork
  -> ActivityThread.main
      -> bindApplication
          -> Provider / Application / Activity 初始化
```

### 多进程与 Binder 实验区

`RemoteInspectorService` 在 Manifest 中声明为：

```xml
android:process=":remote"
```

页面提供这些动作：

- `绑定 remote`：创建并绑定 remote Service。
- `Ping remote`：通过 Messenger 获取 remote 进程信息。
- `修改 remote 单例`：让 remote 进程里的单例值自增。
- `阻塞 remote`：让 remote 主线程 sleep，观察 IPC 延迟。
- `杀掉 remote`：杀死 remote 进程，观察 Binder death。

重点观察：

```text
main pid != remote pid
main singleton != remote singleton
remote 进程死亡后需要重新建立连接
```

### 应用沙箱与 isolatedProcess 实验区

页面会展示应用 `uid`、`dataDir` 和 `/proc/<pid>/status` 中的 UID 信息，用来解释 Android 应用沙箱的第一层基础：不同 App 通常运行在不同 Linux uid 下，文件目录和进程权限天然隔离。

工程还声明了一个 `IsolatedInspectorService`：

```xml
android:isolatedProcess="true"
```

它用于对比普通 `:remote` 进程：

```text
:remote
  -> 独立 pid
  -> 通常仍属于同一个应用 uid
  -> 适合拆分推送、播放器、地图等独立组件

isolatedProcess
  -> 独立 pid
  -> 使用更受限的隔离 uid
  -> 适合解析不可信数据、隔离高风险能力或缩小故障影响范围
```

### ANR 场景模拟区

本工程故意提供了几个危险按钮，只建议在 debug 环境中使用。

```text
主线程阻塞 7s
Broadcast 阻塞 12s
Service 阻塞 22s
remote 阻塞 8s
```

它们分别用于观察：

- Input / Main Thread ANR：主线程被 `Thread.sleep()` 卡住，输入和绘制无法及时处理。
- Broadcast ANR：`BroadcastReceiver.onReceive()` 长时间占用主线程。
- Service ANR：`Service.onStartCommand()` 长时间占用主线程。
- Binder 阻塞风险：remote 主线程被阻塞，IPC 回复延迟，真实项目中如果主线程同步等待远端，很容易把卡顿扩散成 ANR。

每次触发场景后，页面会给出一张 trace 阅读卡，提示你按下面顺序看证据：

```text
main 线程
  -> Binder 线程
      -> held lock / waiting lock
          -> logcat / dumpsys activity anr 中的系统超时原因
              -> 结论：是哪条链路堵住了用户体验
```

### OOM Adj 样本表

页面可以记录多个 `oom_score_adj` 样本：

```text
前台样本
后台返回样本
remote 后样本
```

建议你在不同状态下点击记录，形成一张对比表。要记住：`oom_score_adj` 不是进程出生后永远不变的身份证，它会跟随 Activity 前后台、Service 状态、进程角色和系统调度一起变化。

## 推荐观察命令

安装运行后，可以配合下面的命令观察系统侧证据。

```bash
adb shell ps -A | grep com.helloandroid.process
adb shell cat /proc/<pid>/oom_score_adj
adb shell cat /proc/<pid>/status | grep -E "Name|Pid|PPid|Uid|Threads"
adb shell dumpsys activity processes | grep com.helloandroid.process -A 20
adb logcat -s ProcessZygoteLab
```

模拟进程死亡：

```bash
adb shell am kill com.helloandroid.process
adb shell am force-stop com.helloandroid.process
```

观察 ANR 时，可以额外查看：

```bash
adb logcat | grep -i anr
adb shell dumpsys activity anr
```

## 建议学习路线

1. 先运行 App，刷新“进程身份证”，记录主进程 `pid`、`uid`、`oom_score_adj`。
2. 点击“绑定 remote”，观察 remote 进程的 `pid` 和 `processName`。
3. 点击“修改 remote 单例”，对比主进程和 remote 进程的单例值。
4. 点击“杀掉 remote”，观察 Binder death，再重新绑定。
5. 点击“绑定 isolated”，观察 isolated 进程的 `pid`、`uid` 和 Binder death。
6. 完成“内存草稿 / 持久化草稿”实验，观察进程死亡后的状态恢复。
7. 切到后台，执行 `adb shell am kill com.helloandroid.process`，回到 App，观察 `pid` 是否变化。
8. 分别记录前台、后台返回、remote 后的 OOM Adj 样本。
9. 分别触发几种 ANR 场景，结合 trace 阅读卡、logcat 和事件轨迹判断问题发生在哪条线程链路上。
10. 最后填写 `quality/process-diagnosis-report-template.md`，把现象、证据和结论写完整。

## 小挑战

请尝试回答这个问题：

```text
用户点击通知进入详情页，页面显示未登录；
刷新后又恢复登录态；
项目里存在 :remote 推送进程。
```

你需要判断：

- 点击通知时，入口来自哪个进程？
- 主进程是否经历过重建？
- 登录态是否只放在内存单例里？
- remote 进程和主进程之间有没有可靠同步？
- 页面恢复时应该从哪里读取关键状态？

如果你能用 `pid`、`processName`、`oom_score_adj`、Application 创建时间、Binder death 和线程栈把它说清楚，第 19 章就真的开始进入工程经验区了。
