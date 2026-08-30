# 24.1 为什么要学习系统观测工具、证据链与事故分析

第 20 章到第 23 章，我们已经看过很多事故：

```text
ANR
Crash
后台任务丢失
文件不可见
权限授权了仍不可用
签名不一致
组件误暴露
```

现在第 24 章要补上一个资深工程师必须拥有的能力：

```text
不要只会猜原因，要会拿证据。
```

一个复杂问题经常不是单点故障。

它可能同时涉及：

```text
主线程卡顿
Binder 等待
后台调度延迟
进程被回收
权限或 AppOps 拦截
I/O 过慢
内存抖动
系统服务状态异常
厂商策略干预
```

如果没有观测工具，你只能说：

```text
可能是系统问题。
可能是网络问题。
可能是性能问题。
可能是权限问题。
```

这些话听起来像经验，实际上没有证据。

## 本章通关画面

学完第 24 章后，你应该能把一次复杂事故拆成这样的证据链：

```text
用户现象
  -> 复现路径
      -> 时间点
          -> logcat 事件
              -> dumpsys 状态
                  -> Perfetto 时间线
                      -> bugreport 全量现场
                          -> gfxinfo / meminfo / simpleperf 辅助定位
                              -> 根因判断
                                  -> 修复方案
                                      -> 回归证据
```

如果第 23 章像安全门禁系统，第 24 章就是系统事故调查室。

调查室里最重要的不是工具越多越好，而是：

```text
知道哪种问题该拿哪种证据。
知道每份证据能说明什么，也知道它不能说明什么。
```

## 本章要解决的真实问题

真实项目里的复杂问题通常长这样：

```text
用户说页面偶尔卡死，但本地复现不了。
测试说点击后 2 秒才响应，日志里却没有错误。
线上监控显示 ANR 增加，但主线程堆栈每次不一样。
后台同步任务偶尔丢失，不知道是被系统限制还是代码没跑。
冷启动慢，但不知道慢在 Application、ContentProvider、I/O 还是首帧绘制。
某个机型滑动掉帧，普通日志看不出原因。
App 被系统杀掉后恢复错乱，不知道是内存压力还是后台限制。
安全事故发生后，只有一段用户描述，没有完整现场。
```

这些问题不能只靠读代码解决。

你需要建立一套证据意识：

```text
logcat 看事件发生了什么
dumpsys 看系统当前认为状态是什么
bugreport 保存完整事故现场
Perfetto 看时间线上谁在运行、谁在等待
gfxinfo 看帧耗时和渲染问题
meminfo / procstats 看内存和进程状态
simpleperf 看 CPU 时间花在哪些函数上
```

## 本章核心判断：证据五问

面对任何复杂事故，先问五句话：

```text
第一问：现象发生在什么时间点？
第二问：当时哪个线程、进程或系统服务参与了？
第三问：有没有能证明状态的系统快照？
第四问：有没有能证明先后顺序的时间线？
第五问：修复后能否用同样证据证明问题消失？
```

例如：

| 场景 | 第一工具 | 关键证据 |
| --- | --- | --- |
| 点击后无响应 | logcat + Perfetto | 点击时间、主线程状态、Input / Binder / Choreographer 事件 |
| 页面掉帧 | Perfetto + gfxinfo | FrameTimeline、DrawFrame、主线程和 RenderThread 耗时 |
| 后台任务丢失 | dumpsys jobscheduler / alarm / activity | job 状态、standby bucket、后台限制 |
| 内存压力死亡 | dumpsys meminfo / procstats / bugreport | PSS、进程状态、LMKD 线索 |
| 安全权限问题 | dumpsys package + cmd appops | permission grant、AppOps mode、组件状态 |
| Native 崩溃 | tombstone + bugreport | signal、backtrace、so 符号 |

证据链的价值是：它把“感觉”变成“可复盘的事实”。

## 本章读法：先学取证，再学解释

第 24 章工具很多，不建议把它读成命令大全。

建议按三层读：

```text
第一层：基础必读
  -> logcat、dumpsys、bugreport 各自解决什么问题
  -> 能知道复杂问题第一份证据该拿什么

第二层：工程必会
  -> Perfetto、gfxinfo、meminfo、simpleperf 如何组合
  -> 能写出一份证据链报告

第三层：深入选读
  -> trace config、FrameTimeline、Binder wait、调度器、符号化和 CI 采集
```

读第 24 章时请始终记住：

```text
工具不是答案。
工具只是把系统现场摊开。
真正的答案来自你能不能把现场连成链。
```

## 本章贯穿案例：一次“偶发卡顿 + 后台同步丢失”的事故

`Hello Android 学习中心`上线后，用户反馈：

```text
打开课程详情页偶尔卡住。
晚上自动同步学习进度偶尔失败。
第二天重新打开 App，进度又恢复了。
```

这个事故很适合第 24 章。

因为它可能不是一个问题：

```text
打开详情页卡住
  -> 可能是主线程 I/O
  -> 可能是数据库锁等待
  -> 可能是 Binder 调用慢
  -> 可能是首帧绘制过重

后台同步失败
  -> 可能是 WorkManager 没调度
  -> 可能是网络约束不满足
  -> 可能是 App Standby / Doze 限制
  -> 可能是进程被回收后状态恢复错误
```

本章会带你用不同工具把它拆开。

## 本章学习顺序

```text
24.1 建立系统观测和证据链意识
24.2 logcat、结构化日志与时间点定位
24.3 dumpsys：系统服务状态快照
24.4 bugreport、DropBox、ANR trace 与 tombstone
24.5 Perfetto：系统时间线、线程、Binder 与帧
24.6 gfxinfo、meminfo、procstats 与 simpleperf
24.7 观测体验问题：误读、隐私、复现和团队协作
24.8 综合实践：系统证据链分析实验室
24 附录 从 Perfetto 到 CausalPerf / SmartPerfetto
```

附录不是为了替代前面的系统工具。

它要回答另一个问题：

```text
当证据越来越多、trace 越来越复杂，
工程师如何借助更智能的工具把线索组织成因果链？
```

## 官方资料

第 24 章建议配合官方资料阅读：

- [Perfetto command-line tool](https://developer.android.com/tools/perfetto)
- [Capture a system trace on a device](https://developer.android.com/topic/performance/tracing/on-device)
- [Perfetto Android tracing quickstart](https://perfetto.dev/docs/getting-started/system-tracing)
- [Capture and read bug reports](https://developer.android.com/studio/debug/bug-report)
- [Read bug reports - AOSP](https://source.android.com/docs/core/tests/debug/read-bug-reports)
- [dumpsys](https://developer.android.com/tools/dumpsys)
- [Logcat command-line tool](https://developer.android.com/tools/logcat)

## 本节自测

- 为什么复杂事故不能只靠日志？
- logcat、dumpsys、bugreport、Perfetto 分别更适合回答什么问题？
- 为什么证据一定要带时间点？
- 为什么修复后也要采集同样证据？

## 本节总结

第 24 章不是教你背命令，而是训练你成为一个会取证的 Android 工程师。

复杂事故的第一步不是猜根因，而是建立现场：

```text
现象
  -> 时间点
      -> 系统状态
          -> 执行时间线
              -> 根因
                  -> 回归证据
```

当你能把这条链写清楚，Framework 知识就不再只是源码名词，而会变成真正的工程判断力。
