# 20.1 为什么要学习 ANR、Crash、Watchdog 与系统稳定性诊断

第 19 章我们回答了一个关键问题：

```text
App 代码运行在哪个进程里？
进程如何出生、隔离、通信、被回收？
```

第 20 章继续追问：

```text
当这个进程卡住、崩溃、被杀、native 异常或系统服务失去响应时
  -> Android 如何发现问题？
      -> 问题证据被写到哪里？
          -> 工程师应该从哪条线索开始排查？
```

这会进入 Android 稳定性诊断机制：`ANR`、Java Crash、Native Crash、tombstone、Watchdog、DropBox、logcat、bugreport、StrictMode、trace 和线上稳定性治理。

## 本章通关画面

学完第 20 章后，你应该能画出这张稳定性地图：

```text
用户感知异常
  -> 卡死 / 闪退 / 黑屏 / 后台任务中断
      -> 系统产生证据
          -> ANR traces / crash stack / tombstone / logcat / DropBox / bugreport
              -> 定位线程、进程、组件、系统服务和资源状态
                  -> 判断根因类型
                      -> 修复代码、增加保护、补充监控和回归验证
```

如果第 19 章像进入进程调度室，第 20 章就是进入事故调查室。

进程会运行，当然也会出问题。资深工程师和普通工程师的差异，很多时候不在于“永远不写 bug”，而在于事故发生后能不能迅速拿到证据、读懂证据、缩小范围、给出可信结论。

## 本章剧情线

很多线上事故刚出现时都很模糊：

```text
用户说卡住了
测试说偶现闪退
监控说 ANR 上升
某些设备启动后黑屏
native so 在少数机型崩溃
后台服务偶尔不执行
系统日志里出现 watchdog
```

如果只看业务代码，很容易陷入猜测：

- 是不是主线程耗时？
- 是不是 Binder 卡住？
- 是不是死锁？
- 是不是低内存？
- 是不是某个 native so 崩了？
- 是不是系统服务不可用？
- 是不是只在后台、低端机、特定 ROM、特定 Android 版本出现？

第 20 章要做的事，是把这些模糊现象拆成可验证证据。

## 本章探索任务

```text
认识稳定性问题分类
  -> 区分 ANR、Java Crash、Native Crash、Watchdog 和低内存杀进程
      -> 深入 ANR 产生条件与 trace 阅读方法
          -> 深入 Java Crash 分发、线程异常和恢复边界
              -> 认识 Native Crash、signal 和 tombstone
                  -> 理解 Watchdog 如何监控 system_server
                      -> 学会使用 logcat、DropBox、bugreport 和 dumpsys
                          -> 建立线上稳定性治理闭环
```

## 本节定位

本节是第 20 章入口。

它负责回答：

- 第 20 章和第 9、19 章是什么关系？
- 为什么稳定性问题不能只看崩溃栈？
- 为什么 ANR、Crash、Watchdog 和 tombstone 要放在同一章学习？
- 第 20 章的学习顺序是什么？

## 学习目标

学完本节后，你应该能够：

- 区分“进程被系统杀掉”和“进程自己崩溃”。
- 区分“主线程卡住导致 ANR”和“Java 异常导致 Crash”。
- 知道 native 崩溃为什么需要 tombstone，而不是只看 Java stack。
- 知道 Watchdog 主要面向 system_server 和系统服务健康。
- 能把稳定性问题放进“现象 -> 证据 -> 判断 -> 修复 -> 回归”的链路。

## 第一部分：稳定性不是一个词，而是一组事故类型

Android 稳定性问题至少可以拆成几类：

| 类型 | 用户感知 | 典型证据 | 常见根因 |
| --- | --- | --- | --- |
| ANR | 卡住、无响应、弹出等待或关闭 | traces、logcat、dumpsys activity anr | 主线程耗时、锁等待、Binder 阻塞、Broadcast / Service 超时 |
| Java Crash | App 闪退 | Java stack、UncaughtException、logcat | 空指针、越界、非法状态、线程异常 |
| Native Crash | App 闪退或进程死亡 | tombstone、signal、backtrace | so 崩溃、野指针、JNI 错误、ABI 问题 |
| Watchdog | 系统卡死、重启或系统服务异常 | system_server trace、watchdog log、DropBox | 系统服务死锁、HandlerChecker 超时 |
| Low Memory Kill | 后台回来重建、状态丢失 | pid 变化、oom_score_adj、lmkd log | 后台进程被回收、状态未持久化 |

它们都叫“不稳定”，但排查入口完全不同。

如果把它们混成一团，就会出现很常见的误判：

```text
后台回来页面空白
  -> 以为是 Crash
      -> 实际是进程被 LMKD 回收，状态没有恢复

点击按钮后卡死
  -> 以为是网络慢
      -> 实际是主线程同步等待 Binder 返回

少数设备闪退
  -> 只看 Java 崩溃平台
      -> 实际是 native so 触发 SIGSEGV
```

第 20 章的第一原则是：先给事故分类，再进入细节。

## 第二部分：为什么第 20 章接在第 19 章之后

第 19 章讲进程、线程、OOM Adj、LMKD、多进程和 Binder。

这些正是稳定性诊断的底座。

例如：

```text
ANR
  -> 必须知道主线程、Binder 线程和 Service / Broadcast 回调在哪个线程执行

后台死亡
  -> 必须知道进程优先级、oom_score_adj 和 LMKD

多进程状态错乱
  -> 必须知道 :remote 进程和主进程不共享内存

Watchdog
  -> 必须知道 system_server、Handler、锁和系统服务协作

Native Crash
  -> 必须知道 Java 进程里也可能加载 native so
```

所以第 20 章不是突然转向“线上问题”，而是把第 19 章的运行空间变成事故现场。

## 第三部分：稳定性排查的基本姿势

不要从“猜原因”开始。

应该从证据开始：

```text
现象
  -> 时间
      -> 设备 / 系统版本 / 前后台状态
          -> 进程是否还在
              -> pid 是否变化
                  -> logcat
                      -> trace / tombstone / crash stack
                          -> 结论
```

一个成熟的稳定性报告至少包含：

```text
问题类型：
发生时间：
设备 / 系统版本：
App 版本：
前后台状态：
进程名：
pid：
线程名：
关键栈：
系统日志：
是否多进程：
是否低内存：
是否可复现：
初步结论：
修复方案：
回归验证：
```

这份报告看似繁琐，但它可以避免团队在没有证据时来回争论。

## 第四部分：本章学习地图

第 20 章按下面顺序展开：

```text
20.1 建立稳定性诊断地图
20.2 ANR：系统如何判断 App 无响应
20.3 ANR trace：如何读 main、Binder、锁和系统超时
20.4 Java Crash：异常如何杀死进程
20.5 Native Crash 与 tombstone：signal、JNI 和 so 崩溃
20.6 Watchdog、DropBox 与 bugreport：系统级事故证据
20.7 稳定性体验问题：误判、漏报、恢复和降级
20.8 综合实践：稳定性诊断实验室
```

你会发现，这一章既有 Framework，也有工程治理。

Framework 负责解释系统如何发现问题；工程治理负责把问题变成可定位、可修复、可回归的流程。

## 本节小挑战

### 事故分类开场题

请判断下面三个问题分别更像什么类型：

```text
1. 用户点击按钮后卡住 10 秒，系统弹出“应用无响应”。
2. 用户打开某页面后 App 立刻退出，日志里有 NullPointerException。
3. 用户从后台回来，页面回到首页，之前输入的草稿没了，但没有崩溃日志。
```

你需要回答：

- 哪个更像 ANR？
- 哪个更像 Java Crash？
- 哪个更像进程被回收后的状态恢复问题？
- 每个问题第一份证据应该去哪找？

## 本节实践任务

### 基础任务

- 找一个已有 ANR 日志，标出主线程栈。
- 找一个 Java Crash 日志，标出异常类型、线程和业务入口。
- 找一个后台进程重建问题，记录 pid 是否变化。

### 进阶任务

- 用一张表整理 ANR、Java Crash、Native Crash、Watchdog、Low Memory Kill 的差异。
- 写一份稳定性问题诊断模板。
- 在团队项目中补充 `processName`、`pid`、`threadName`、前后台状态日志。

## 本节小结

第 20 章的核心不是背一堆名词，而是建立事故调查方式。

当 App 出问题时，你要先问：

```text
它是卡住、闪退、native 崩溃、系统服务卡死，还是后台进程被回收？
```

只有分类正确，后面的证据才会有方向。
