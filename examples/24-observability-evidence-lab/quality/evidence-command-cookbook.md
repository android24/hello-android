# 第24章取证命令手册

下面的命令用于配合 `24-observability-evidence-lab` 练习。不同 Android 版本、设备厂商和权限状态下输出会有差异，关键是理解每个命令回答的问题。

## logcat：先锁定时间点

```bash
adb logcat -c
adb logcat -v threadtime | grep Chapter24Lab
adb logcat -v threadtime | grep traceId
adb logcat -v time | grep Choreographer
adb logcat -b crash | grep observability
adb logcat -b events | grep am_proc_start
```

logcat 适合回答：

```text
问题什么时候开始？
业务动作有没有发生？
有没有 crash / ANR / skipped frames 摘要？
后续证据应该围绕哪个时间窗口看？
```

Demo 的触发器会写出类似日志：

```text
traceId=main-thread-123456 main-thread-work start
traceId=main-thread-123456 main-thread-work end cost=121ms
traceId=memory-growth-123999 allocated memory samples: 4MB
```

这个 `traceId` 应该被写进证据链报告，用来对齐 logcat、Perfetto 和后续指标。

## dumpsys：询问系统服务状态

```bash
adb shell dumpsys package com.helloandroid.observability
adb shell dumpsys activity activities
adb shell dumpsys activity processes
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys deviceidle
adb shell dumpsys gfxinfo com.helloandroid.observability
adb shell dumpsys gfxinfo com.helloandroid.observability framestats
adb shell dumpsys meminfo com.helloandroid.observability
adb shell dumpsys procstats --hours 3
```

dumpsys 适合回答：

```text
系统是否识别这个包、组件、任务或权限？
Activity / Service / Job 当前是什么状态？
掉帧、内存和进程状态有没有数字证据？
```

## Perfetto：看时间线

```bash
adb shell perfetto \
  -o /data/misc/perfetto-traces/ch24.perfetto-trace \
  -t 10s \
  sched freq idle am wm gfx view binder_driver

adb pull /data/misc/perfetto-traces/ch24.perfetto-trace .
```

也可以使用本目录的配置文件：

```bash
adb shell perfetto \
  --txt \
  -c /data/local/tmp/perfetto_config.textproto \
  -o /data/misc/perfetto-traces/ch24_config.perfetto-trace
```

Perfetto 适合回答：

```text
主线程到底在运行还是等待？
RenderThread、Binder、system_server 是否参与了问题？
慢帧附近发生了哪些跨线程、跨进程事件？
```

采集 trace 后，可以在 Perfetto UI 里搜索：

```text
ch24_main_thread_work
ch24_memory_growth
```

这些 section 来自 Demo 里的 `Trace.beginSection`，用于把按钮触发的现场和系统时间线连接起来。

## bugreport：保存完整现场

```bash
mkdir -p bugreports
adb bugreport bugreports/
```

拿到 bugreport 后优先搜索：

```text
包名：com.helloandroid.observability
时间点：例如 10:24:31
ANR in
tombstone
lowmemorykiller / lmk
jobscheduler
dropbox
```

bugreport 适合回答：

```text
事故现场能否被保存下来？
多个系统服务状态能否放到同一份材料里复盘？
ANR trace、tombstone、DropBox 是否能与时间点对齐？
```

## simpleperf：定位 CPU 热点

```bash
adb shell pidof com.helloandroid.observability
simpleperf record -p <pid> --duration 10
simpleperf report
```

simpleperf 适合回答：

```text
CPU 时间花在哪些函数上？
优化后热点是否下降？
卡顿是否真的是 CPU 热点导致？
```
