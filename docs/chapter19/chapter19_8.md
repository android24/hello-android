# 19.8 综合实践：进程、Zygote、多进程与内存回收观察实验

第 19 章最后一节，我们把进程身份、Zygote、ActivityThread、主线程、Binder 线程、多进程、OOM Adj 和后台回收放进一个综合观察实验。

目标是：让你能从一次 App 启动、一次后台返回和一次 remote Service 调用，解释进程如何创建、运行、隔离、通信和被回收。

## 本节剧情钩子

现在你已经知道：

```text
系统如何认识 App
系统如何读取资源
系统如何加载代码
```

第 19 章继续追问：

```text
这些代码运行在哪个进程？
进程是谁创建的？
为什么后台回来状态没了？
为什么多进程单例不同步？
为什么 Service 还在跑，进程却可能被杀？
```

本节要把这些问题做成一个可观察实验室。

## 本节定位

本节是第 19 章综合实践。

后续可以配套工程：

```text
examples/19-process-zygote-lab/
```

这个工程可以围绕 pid / uid / processName 打印、Application 初始化轨迹、Zygote 冷启动观察、多进程 Service、Binder 通信、后台进程死亡恢复、线程 dump 和进程问题诊断卡做成一个可运行实验。

## 学习目标

学完本节后，你应该能够：

- 打印并解释当前 pid、uid、processName、threadName。
- 区分冷启动、热启动、Activity 重建和进程重建。
- 观察主进程和 remote 进程的 Application 初始化差异。
- 解释多进程单例为什么不是同一份对象。
- 初步判断后台进程被杀后的恢复问题。
- 写一份进程问题诊断报告。

## 第一部分：实践工程规划

第 19 章 demo 可以拆成这些可观察区域：

- `进程观察分数`：提示实验完成度。
- `进程身份证`：展示 packageName、processName、pid、uid、threadName。
- `启动轨迹卡`：记录 Application、Activity、Service 的创建时间。
- `Zygote 冷启动卡`：解释 force-stop、am kill、后台切回的差异。
- `ActivityThread 链路卡`：展示 `ActivityThread.main -> attach -> bindApplication`。
- `多进程实验卡`：启动 `:remote` Service，对比主进程和 remote 进程状态。
- `单例隔离卡`：展示主进程和 remote 进程中的单例值不同。
- `Binder 通信卡`：通过 Messenger / AIDL 模拟主进程和 remote Service 通信。
- `后台回收实验卡`：模拟进程被杀后的状态恢复。
- `OOM Adj 观察卡`：指导使用 `/proc/<pid>/oom_score_adj` 和 `dumpsys activity processes` 对比前后台状态。
- `Provider 初始化卡`：展示 Provider 可能早于 Application 参与启动路径。
- `Binder Death 卡`：remote 进程死亡后触发死亡监听，解释远端生命周期。
- `线程观察卡`：展示 main、Binder、RenderThread、业务线程。
- `线程 Dump 阅读卡`：标注 main、Binder、RenderThread、worker 的状态和等待关系。
- `ANR 线索卡`：模拟主线程阻塞、Binder 等待和锁竞争，把 trace 变成诊断题。
- `进程问题诊断卡`：整理后台死亡、多进程错乱、保活误区和 remote 卡死。
- `事件轨迹`：记录每次进程、线程、Service 和恢复动作。

## 第二部分：进程观察通关路线

建议按三段完成：

```text
初级侦探：认出当前进程
  -> 打印 pid / uid / processName
      -> 打印 threadName
          -> 观察 Application 和 Activity 初始化轨迹
              -> 杀进程后重新打开
                  -> 对比 pid 是否变化

中级侦探：进入多进程
  -> 启动 :remote Service
      -> 打印 remote 进程 pid
          -> 对比主进程和 remote 进程单例
              -> 通过 Binder / Messenger 通信
                  -> 观察 Application 是否执行多次

高级侦探：解释后台死亡和恢复
  -> 模拟 am kill
      -> 回到 App
          -> 检查 savedInstanceState 和持久化状态
              -> 观察后台任务是否可恢复
                  -> 写进程问题诊断报告
```

## 第三部分：手动实验路线

在配套工程创建之前，也可以先用任意项目做手动实验。

准备：

- 一个 Application。
- 一个 Activity。
- 一个 remote Service。
- 一个单例对象。
- 一份持久化状态。
- 一段能打印 processName / pid / threadName 的日志工具。

观察路线：

```text
启动 App
  -> 记录 pid
      -> 切后台
          -> adb shell am kill 包名
              -> 再回到 App
                  -> 对比 pid
                      -> 检查状态恢复
                          -> 启动 remote Service
                              -> 对比主进程和 remote 进程单例
```

## 第四部分：进程身份证

建议记录：

```text
packageName
processName
pid
ppid
uid
oom_score_adj
threadName
Application createdAt
Activity createdAt
isMainProcess
```

你要回答：

- 当前代码运行在哪个进程？
- 当前进程是否主进程？
- pid 是否发生变化？
- ppid 是否指向 Zygote 相关进程？
- 前后台切换后 oom_score_adj 是否变化？
- Application 是否重新创建？
- 当前日志来自哪个线程？

## 第五部分：Zygote 与冷启动观察

建议记录：

```text
第一次启动 pid
后台切回 pid
am kill 后 pid
force-stop 后 pid
Application onCreate 时间
Activity onCreate 时间
```

你要回答：

- 冷启动为什么更慢？
- Zygote fork 在哪一段链路中出现？
- 进程重启后内存状态为什么会丢？
- `am kill` 和 `force-stop` 有什么体验差异？

## 第六部分：多进程观察

建议记录：

```text
main processName
remote processName
main pid
remote pid
main singleton value
remote singleton value
remote service bind result
remote binder death
```

你要回答：

- `android:process=":remote"` 创建了什么？
- 主进程和 remote 进程是否共享单例？
- Application 为什么会执行多次？
- 哪些初始化应该只在主进程执行？
- remote 进程死亡后主进程如何感知？
- Binder 调用失败时应该重连、降级还是提示用户？

在 demo 里可以把 remote 进程做成一个小沙盘：

```text
启动 remote Service
  -> bind 成功
      -> 读取 remote pid
          -> 改变 remote 单例值
              -> 杀掉 remote 进程
                  -> 观察 DeathRecipient
                      -> 重新 bind
                          -> 发现 remote 单例重新初始化
```

这样学习者会明白：remote 进程不是后台线程，它有自己的出生、死亡和恢复。

## 第七部分：线程观察

建议记录：

```text
main thread
Binder thread
RenderThread
worker thread
current thread in callback
thread state
held lock
waiting lock
sync Binder call
```

你要回答：

- 主线程负责什么？
- Binder 回调一定在主线程吗？
- RenderThread 能不能解决主线程阻塞？
- 多进程里为什么每个进程都有自己的 main 线程？
- main 线程 BLOCKED 时，谁持有它等待的锁？
- Binder 线程池被占满时，会影响哪些调用？

线程实验不要只显示线程名，最好做成“证据卡”：

```text
main
  state=BLOCKED
  waitingLock=SessionStore
  meaning=主线程正在等登录态锁，输入和绘制可能受影响

Binder:12345_2
  state=RUNNABLE
  heldLock=SessionStore
  meaning=Binder 回调持锁执行远程同步调用，可能拖住 main

RenderThread
  state=WAITING
  meaning=没有新的渲染命令，问题可能在主线程没有及时产出帧
```

这样读者才能把线程 dump 和体验问题连起来。

## 第八部分：OOM Adj 与后台回收观察

建议记录：

```text
foreground oom_score_adj
background oom_score_adj
foreground service oom_score_adj
cached process state
am kill result
force-stop result
lmk / low memory log
```

你要回答：

- 同一个进程的 oom_score_adj 是否固定？
- 前台 Activity、后台缓存、前台服务分别如何影响重要性？
- `am kill` 和 `force-stop` 是否是同一种状态？
- 进程死亡后，页面靠什么恢复关键状态？

## 第九部分：进程问题诊断报告

建议报告格式：

```text
操作：
现象：
当前 processName：
当前 pid：
当前 ppid：
上一次 pid：
uid：
oom_score_adj：
Application 创建时间：
Activity 是否重建：
是否多进程：
remote 进程名：
remote pid：
Binder death 是否触发：
单例是否一致：
关键状态来源：
是否有后台任务：
是否有 low memory / lmk 证据：
main 线程状态：
Binder 线程状态：
RenderThread 状态：
是否存在锁等待：
是否存在同步 Binder 调用：
我的结论：
仍不确定：
```

推荐 AOSP / Android 入口：

```text
frameworks/base/core/java/android/app/ActivityThread.java
frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
frameworks/base/services/core/java/com/android/server/am/ProcessList.java
frameworks/base/services/core/java/com/android/server/wm/ActivityTaskManagerService.java
frameworks/base/core/java/android/os/Process.java
system/core/init/
system/core/lmkd/
```

## 第十部分：本章通关检查

完成第 19 章后，请确认自己能回答：

- pid、uid、processName 分别是什么？
- Android 应用沙箱为什么能隔离不同 App？
- Zygote 为什么要预加载并 fork App 进程？
- `ActivityThread.main()` 在 App 进程里扮演什么角色？
- `attachApplication` 和 `bindApplication` 大致发生在什么时候？
- 为什么 Android 要区分主线程 UI 工作和其他线程耗时工作？
- 主线程、Binder 线程、RenderThread 分别负责什么？
- OOM Adj 和 LMKD 大致解决什么问题？
- 为什么后台进程会被系统杀掉？
- 多进程为什么不共享单例？
- ContentProvider 为什么可能导致初始化提前？
- isolatedProcess 适合什么类型的隔离？
- 为什么不应该把“保活”当成后台可靠性的核心方案？

## 本节小挑战

### 进程调度室终局题

请为下面问题写一份诊断报告：

```text
用户打开 App 后登录成功。
切到后台 40 分钟，再点通知进入详情页。
详情页显示未登录，刷新后又恢复正常。
App 使用了 :push 进程接收通知。
```

你需要回答：

- 通知点击来自哪个进程？
- 主进程是否已经被杀？
- 登录态是否只存在内存？
- push 进程和主进程如何同步状态？
- 是否需要从持久化状态恢复登录信息？
- 日志里应该补哪些进程证据？

## 本节实践任务

### 基础任务

- 打印 pid、uid、processName、threadName。
- 模拟 App 后台进程被杀。
- 启动 remote Service 并打印 remote 进程信息。
- 对比主进程和 remote 进程单例值。
- 写一份进程问题诊断报告。

### 进阶任务

- 用 Messenger / AIDL 做一次跨进程通信。
- 给初始化逻辑增加主进程判断。
- 观察 `am kill`、`force-stop`、最近任务划掉的差异。
- 查阅 `ActivityThread`、`ProcessList` 和 `lmkd` 相关资料。

## 本节小结

第 19 章把“App 代码运行在哪里”推进到进程、线程、沙箱和内存回收。你不需要一次读完所有 AMS 和 LMKD 源码，但应该能把 Zygote、ActivityThread、uid、processName、多进程、Binder 线程、OOM Adj、LMKD 和后台恢复放在同一张地图上。到这里，Framework 阶段从代码加载继续推进到运行空间本身：App 不只是代码和资源，它还是一个会出生、运行、通信、被回收并再次恢复的系统进程。
