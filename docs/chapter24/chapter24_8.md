# 24.8 综合实践：系统证据链分析实验室

第 24 章最后一节，我们把 logcat、dumpsys、bugreport、Perfetto、gfxinfo、meminfo、procstats 和 simpleperf 放进一个综合实践。

目标不是让你背命令，而是做一次完整的事故取证。

## 本节先记住三句话

```text
综合实践要从事故开始，不要从工具开始。
每份证据都要说明它回答了哪个问题。
最终输出不是截图合集，而是一份能复盘的证据链报告。
```

## 贯穿案例：课程详情页偶发卡顿

Demo 可以围绕 `Hello Android 学习中心` 的一次复杂事故设计：

```text
用户点击课程详情页
  -> 页面偶尔白屏 2 秒
      -> 晚上学习进度同步偶尔失败
          -> 第二天重新打开 App 后进度恢复
```

这个案例可以把前几章都串起来：

```text
Activity 启动
主线程消息
Binder
渲染
后台调度
存储
安全与日志脱敏
```

第 24 章要回答的是：

```text
证据在哪里？
证据之间如何互相印证？
修复后如何证明有效？
```

## 现场侦探问题

如果你只能采三份证据，你会选哪三份？

推荐组合：

```text
logcat
Perfetto trace
dumpsys / bugreport
```

logcat 给时间点，Perfetto 给执行过程，dumpsys / bugreport 给系统状态。

## 本节定位

本节是第 24 章综合实践。

配套示例工程建议命名为：

```text
examples/24-observability-evidence-lab/
```

这个工程可以做成一个“系统证据链分析实验室”：页面里制造轻量卡顿、后台任务、日志事件、内存变化和报告模板，读者再通过 adb、Perfetto、dumpsys、bugreport 把证据采回来。

## 学习目标

学完本节后，你应该能够：

- 为一个事故选择合适的观测工具。
- 采集 logcat、dumpsys、Perfetto 和 bugreport。
- 把工具输出按时间点串起来。
- 区分现象、证据、推论和根因。
- 写出优化前后对比和回归报告。

## 第一部分：实践工程规划

第 24 章 Demo 建议拆成这些区域：

- `事故任务板`：列出本章需要完成的证据采集任务。
- `复现控制台`：制造主线程卡顿、后台任务延迟、轻量内存增长、日志事件。
- `时间点记录卡`：显示用户点击时间、任务开始时间、任务结束时间。
- `logcat 证据区`：生成统一 tag 和 traceId，提示采集命令。
- `dumpsys 命令区`：按问题推荐 package、activity、jobscheduler、gfxinfo、meminfo。
- `Perfetto 采集区`：给出短 trace 配置和关键 category。
- `bugreport 检查区`：提示如何搜索包名、时间点、ANR、tombstone。
- `证据链答题区`：选择第一证据、系统状态证据、时间线证据和根因候选。
- `证据链报告`：输出复现、证据、推论、根因、修复和回归。

## 第一部分补充：体验评分怎么设计

Demo 可以把学习过程拆成 12 个观察点：

| 观察点 | 得分条件 |
| --- | --- |
| 复现路径 | 选择一个事故并记录步骤 |
| 时间点 | 记录点击、卡顿、恢复或任务触发时间 |
| logcat | 采集带 traceId 的日志 |
| dumpsys package | 观察包、权限、版本和组件状态 |
| dumpsys activity | 观察前台页面、任务栈或进程状态 |
| jobscheduler / alarm | 观察后台任务状态 |
| gfxinfo | 获取帧统计 |
| meminfo | 获取内存快照 |
| Perfetto | 采集并打开 trace |
| bugreport | 导出完整事故包 |
| 证据答题 | 选择证据对应的问题 |
| 证据链报告 | 写出根因和回归证据 |

评分不是为了把工具做成小游戏，而是防止“截图很多，结论没有”。

## 第二部分：证据采集路线

推荐按五段完成：

```text
第一段：明确事故
  -> 用户现象、复现路径、设备、版本、时间点

第二段：采集轻量证据
  -> logcat、关键 dumpsys

第三段：采集时间线
  -> Perfetto、gfxinfo

第四段：保存完整现场
  -> bugreport、ANR trace、tombstone、DropBox

第五段：写报告和回归
  -> 根因判断、修复方案、优化前后对比
```

## 第三部分：事故剧本怎么玩

Demo 可以设计几个事故剧本：

| 剧本 | 表面现象 | 第一线索 | 推荐工具 |
| --- | --- | --- | --- |
| 剧本 A：详情页白屏 | 点击后 2 秒才显示 | 首帧时间异常 | logcat + Perfetto + gfxinfo |
| 剧本 B：滑动掉帧 | 列表滚动不顺 | janky frames 高 | gfxinfo + Perfetto |
| 剧本 C：后台同步丢失 | 晚上没同步 | job 没 ready 或被限制 | dumpsys jobscheduler + bugreport |
| 剧本 D：内存上涨 | 用久了变慢 | PSS / Objects 增长 | meminfo + procstats |
| 剧本 E：偶发 ANR | 用户说卡死 | 主线程等待或系统超时 | bugreport + traces + Perfetto |
| 剧本 F：Native 崩溃 | 某机型闪退 | tombstone signal | bugreport + tombstone + symbols |

每个剧本都要回答：

```text
第一证据是什么？
系统状态证据是什么？
时间线证据是什么？
根因候选是什么？
还缺哪份证据？
```

## 第四部分：推荐命令清单

logcat：

```bash
adb logcat -c
adb logcat -v threadtime HelloAndroid:D *:S
adb logcat -d -v threadtime > logcat.txt
```

dumpsys：

```bash
adb shell dumpsys package com.helloandroid
adb shell dumpsys activity activities
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys gfxinfo com.helloandroid framestats
adb shell dumpsys meminfo com.helloandroid
```

Perfetto：

```bash
adb shell perfetto -o /data/misc/perfetto-traces/course-detail.perfetto-trace -t 20s \
  sched freq idle am wm gfx view binder_driver dalvik input res memory
```

bugreport：

```bash
adb bugreport bugreports/
```

DropBox：

```bash
adb shell dumpsys dropbox
```

## 第五部分：证据链报告模板

最终报告建议这样写：

```text
问题标题：
用户现象：
复现路径：
设备 / Android 版本 / App 版本：
发生时间点：

第一证据：
  -> logcat 里哪一行证明事故开始

系统状态证据：
  -> dumpsys 哪个 service 证明系统状态

时间线证据：
  -> Perfetto 哪个线程 / slice / frame 证明耗时

性能或稳定性辅助证据：
  -> gfxinfo / meminfo / simpleperf / tombstone

根因判断：
  -> 不是猜测，而是证据如何指向这个结论

修复方案：
  -> 改了什么

回归证据：
  -> 同样路径下，指标如何变化

残留风险：
  -> 哪些设备、版本、场景还没覆盖
```

## 第六部分：本章通关检查

完成第 24 章后，你应该能回答：

- logcat、dumpsys、bugreport、Perfetto 各自回答什么问题？
- 为什么没有时间点的证据很难分析？
- 为什么 Perfetto 适合看跨线程和跨进程问题？
- 为什么性能优化必须有优化前后对比？
- 为什么 bugreport 和 trace 分享前要考虑脱敏？
- 如何把工具输出写成团队能理解的事故报告？

## 第七部分：Framework / AOSP 阅读入口

如果想继续往源码里读，可以从这些方向开始：

```text
logd / liblog
  -> 日志写入、过滤和读取

dumpstate
  -> bugreport 如何采集系统现场

Service dump()
  -> dumpsys 为什么能拿到系统服务状态

Perfetto / atrace / ftrace
  -> trace 数据如何进入时间线

ActivityManagerService / WindowManagerService / JobSchedulerService
  -> 各系统服务如何暴露自己的 dump 信息
```

源码入口不要求一次读完。

第 24 章的目标是让你知道：

```text
工具输出不是凭空来的。
它们背后都是系统模块主动留下的观察窗口。
```

## 第八部分：Demo 要做成“证据链闯关台”

如果第 24 章 Demo 只是按钮合集，读者会学会制造卡顿，却学不会分析卡顿。

更好的设计是每个实验固定回答：

```text
事故现象：
复现步骤：
第一证据：
系统快照：
时间线证据：
根因候选：
修复建议：
回归方式：
```

这样 Demo 才能和第 20 到第 23 章形成闭环。

## 本节总结

第 24 章把系统观测工具串成了一条证据链。

它让你从：

```text
我觉得可能是这个原因
```

走向：

```text
这是复现路径，这是时间点，这是日志，这是系统状态，这是时间线，这是根因，这是回归证据。
```

这就是资深 Android 工程师和普通排查之间的差别。

如果你已经能完成这一节的证据链报告，可以继续阅读：

- [24 附录：从 Perfetto 到 CausalPerf / Smart Perfetto](appendix_smart_perfetto.md)

那里会继续讨论一个更进阶的问题：当 trace 和系统证据越来越多，如何把人工分析流程进一步工具化、智能化和团队化。
