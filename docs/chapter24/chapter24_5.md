# 24.5 Perfetto：从系统时间线看线程、Binder、调度与帧

logcat 告诉你发生了什么。

dumpsys 告诉你系统状态是什么。

Perfetto 继续回答一个更难的问题：

```text
在那几秒钟里，系统到底按什么顺序运行了谁？
```

它把线程调度、Binder、系统服务、应用主线程、RenderThread、FrameTimeline、I/O 和 CPU 频率等信息放到同一条时间线上。

这就是为什么 Perfetto 是复杂性能和系统问题的核心工具。

## 本节先记住三句话

```text
Perfetto 看的是时间线，不是单条日志。
先定问题窗口，再看主线程、RenderThread、Binder 和系统服务。
trace 越大不等于越好，采集配置要围绕问题设计。
```

## 贯穿案例：课程详情页首帧慢

用户说：

```text
点进课程详情页，白屏一会儿才出现内容。
```

logcat 可能只看到：

```text
CourseDetailActivity onCreate
CourseDetail loaded
```

dumpsys 可能告诉你 Activity 已经启动。

但你仍然不知道：

```text
主线程是否在做 I/O？
RenderThread 是否被阻塞？
Binder 调用是否等待 system_server？
首帧是 measure 慢、draw 慢，还是 Surface 等待？
CPU 是否降频？
```

Perfetto 的时间线能把这些放在一起看。

## 现场侦探问题

一个页面掉帧，你会先看哪几条轨道？

建议：

```text
App main thread
RenderThread
Choreographer / FrameTimeline
Binder threads
system_server
CPU scheduling
```

如果只看一条主线程堆栈，很容易误判。

## 学习目标

学完本节后，你应该能够：

- 理解 Perfetto 适合分析时间线和跨线程协作。
- 能采集一份基础 Android trace。
- 能在 trace 中观察主线程、RenderThread、Binder、system_server 和帧。
- 知道 trace 配置要围绕问题选择。
- 能把 Perfetto 证据写进事故报告。

## 第一部分：如何采集 Perfetto trace

设备上可以使用系统自带的 System Tracing。

命令行可以使用：

```bash
adb shell perfetto -o /data/misc/perfetto-traces/trace_file.perfetto-trace -t 20s \
  sched freq idle am wm gfx view binder_driver hal dalvik input res memory
```

也可以使用 Perfetto 官方的 `record_android_trace` 辅助脚本：

```bash
./record_android_trace -o trace_file.perfetto-trace -t 30s -b 64mb \
  sched freq idle am wm gfx view binder_driver hal dalvik input res memory
```

采集后用 Perfetto UI 打开：

```text
https://ui.perfetto.dev
```

不同 Android 版本对 Perfetto 服务、路径和配置支持会有差异。遇到采集失败，先确认设备版本、USB 调试、权限和输出路径。

## 第二部分：trace 不是越大越好

很多人第一次用 Perfetto，会犯一个错误：

```text
所有 category 全开，录很久。
```

结果是：

```text
文件巨大
噪声很多
关键窗口反而不好找
对设备本身造成额外负担
```

更好的方式：

```text
先明确问题：启动、掉帧、ANR、后台任务、Binder 等待、I/O、内存
再选择 category
缩短复现路径
控制采集时长
记录操作时间点
```

trace 不是监控录像，trace 是带目的的取证。

## 第三部分：Perfetto 数据从哪里来

Perfetto 的价值不只是“图好看”。

它能把多种来源的事件放进同一条时间轴：

```text
ftrace / sched
  -> 线程什么时候运行、休眠、被调度

atrace / trace section
  -> Framework、系统服务和 App 主动打出来的 trace 片段

FrameTimeline / gfx / view
  -> 帧开始、处理、提交、显示相关事件

binder_driver
  -> Binder transaction、等待和跨进程调用线索

memory / freq / idle
  -> 内存、CPU 频率、空闲状态等系统背景信息
```

它真正厉害的地方是：

```text
把不同模块说的话，放到同一个时间坐标里。
```

所以 Perfetto 适合分析“先后关系”和“谁影响了谁”。

## 第四部分：看主线程

主线程常见线索：

```text
ActivityThread
Handler 消息
Choreographer doFrame
View traversal
数据库 / 文件 I/O
JSON 解析
锁等待
Binder 调用
```

如果主线程长时间运行：

```text
可能是 CPU 计算过重
可能是同步 I/O
可能是锁等待
可能是等待 Binder 返回
```

不要只说：

```text
主线程卡了。
```

要继续说：

```text
主线程在什么时间段卡？
执行的是哪个 slice？
是否同时发生 Binder wait？
是否错过了 Choreographer 帧？
```

可以把一次主线程长任务写成这种证据：

```text
21:16:02.120 - 21:16:02.186
main thread 运行 CourseDetailRepository.load()
期间没有回到 MessageQueue
同一窗口内 Choreographer#doFrame 延迟
对应帧超过 16.6ms 预算
```

这时“主线程卡了”就变成了“哪段代码在哪个时间窗口占用了主线程”。

## 第五部分：看 Binder 和 system_server

很多 Framework 问题跨进程。

App 里看起来是：

```text
某个 Manager API 调用慢。
```

Perfetto 里可能是：

```text
App main thread
  -> Binder transact
      -> system_server 某个线程处理
          -> 等锁 / 等 I/O / 调用其他服务
              -> 返回 App
```

这条链比单独堆栈更有解释力。

如果看到 Binder 等待，要继续问：

```text
远端进程是谁？
远端线程在运行还是等待？
system_server 是否有长任务？
Binder 线程池是否拥堵？
```

一条 Binder 证据链可以这样写：

```text
App main thread 发起 Binder transact
  -> 进入 system_server Binder 线程
      -> system_server 线程等待 AMS 内部锁
          -> App main thread 持续等待返回
              -> 输入或绘制事件被延迟处理
```

这比“调用系统 API 慢”更接近根因。

## 第六部分：看帧和渲染

掉帧问题不要只看 FPS。

你要看：

```text
FrameTimeline
Choreographer#doFrame
measure / layout / draw
RenderThread
GPU / Surface / BufferQueue 线索
```

常见判断：

```text
主线程过慢
  -> 状态计算、布局、Compose recomposition、I/O、锁

RenderThread 过慢
  -> 渲染命令、纹理、硬件加速、GPU 压力

帧提交和显示之间异常
  -> Surface / BufferQueue / 合成链路
```

第 15 章讲渲染原理，第 24 章用 Perfetto 把它变成证据。

帧问题可以先按三问拆：

```text
第一问：FrameTimeline 里是哪一帧超时？
第二问：超时发生在 App、RenderThread，还是 SurfaceFlinger / GPU 附近？
第三问：同一时间主线程、Binder、GC、CPU 频率有没有异常？
```

能回答这三问，掉帧分析才不容易停在表面。

## 第七部分：Perfetto 和其他工具如何配合

Perfetto 不替代所有工具。

它要和其他证据配合：

| 工具 | 和 Perfetto 的关系 |
| --- | --- |
| logcat | 定位时间点和业务事件 |
| dumpsys | 提供系统状态快照 |
| bugreport | 保存完整事故包 |
| gfxinfo | 给帧统计摘要 |
| simpleperf | 深挖 CPU 函数耗时 |
| meminfo | 确认内存压力 |

好的分析顺序是：

```text
logcat 找窗口
  -> Perfetto 看时间线
      -> dumpsys 验证状态
          -> simpleperf / meminfo 深挖局部
              -> bugreport 保存完整现场
```

## 本节自测

- Perfetto 和 logcat 最大区别是什么？
- 为什么 trace 配置要围绕问题选择？
- 主线程 Binder wait 时，为什么要看 system_server？
- 掉帧时为什么要同时看 main thread 和 RenderThread？

## 本节总结

Perfetto 是 Android 系统时间线工具。

它最适合回答：

```text
那几秒里谁在运行？
谁在等待？
谁阻塞了谁？
哪一帧错过了？
App 和 system_server 如何协作？
```

学会 Perfetto，你就从“看日志排查”进入了“看系统时间线排查”。
