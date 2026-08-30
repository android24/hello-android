# 24.6 gfxinfo、meminfo、procstats 与 simpleperf：性能现场证据

Perfetto 能看系统时间线。

但很多性能问题还需要更聚焦的证据：

```text
帧到底卡了多少？
内存到底涨在哪？
进程后台运行多久？
CPU 时间到底花在哪些函数？
```

这一节看四类常用工具：

```text
gfxinfo
meminfo
procstats
simpleperf
```

## 本节先记住三句话

```text
gfxinfo 看帧，meminfo 看内存，procstats 看进程历史，simpleperf 看 CPU 热点。
性能结论必须有前后对比，不能只拿一张截图。
工具数字要回到用户体验：启动、滑动、点击、耗电、稳定性。
```

## 贯穿案例：课程列表滑动掉帧

用户说：

```text
课程列表滑动时不顺。
```

你不能只说：

```text
RecyclerView / LazyColumn 优化一下。
```

你要先取证：

```text
gfxinfo 看 janky frame 比例
Perfetto 看卡在哪一帧
simpleperf 看 CPU 热点
meminfo 看是否有内存抖动或 GC
```

如果没有量化，你不知道优化有没有用。

## 现场侦探问题

一次优化前后，你至少要保留哪三份证据？

建议：

```text
优化前指标
优化后指标
同一复现路径
```

没有同一复现路径，前后对比很容易自欺欺人。

## 学习目标

学完本节后，你应该能够：

- 用 `dumpsys gfxinfo` 快速观察 UI 帧表现。
- 用 `dumpsys meminfo` 和 `procstats` 观察内存与进程状态。
- 知道 simpleperf 适合定位 CPU 函数热点。
- 能把性能工具输出转成用户体验语言。
- 能设计优化前后对比报告。

## 第一部分：gfxinfo 看帧表现

基础命令：

```bash
adb shell dumpsys gfxinfo <package>
```

更详细的帧统计：

```bash
adb shell dumpsys gfxinfo <package> framestats
```

重点看：

```text
Total frames rendered
Janky frames
Frame time percentiles
Draw / Process / Execute
```

你要把数字翻译成体验：

```text
Janky frames 高
  -> 用户可能感到滑动或动画不顺

某些阶段耗时高
  -> 继续用 Perfetto 找具体帧
```

gfxinfo 像体温计。

它能告诉你发烧了，但不一定告诉你感染源在哪里。

一个简化输出可以这样读：

```text
Total frames rendered: 540
Janky frames: 86 (15.9%)
90th percentile: 24ms
95th percentile: 38ms
99th percentile: 76ms
```

这段证据说明：

```text
不是单帧偶发问题。
尾部帧耗时已经明显超过一帧预算。
下一步应该用 Perfetto 找这些慢帧附近的主线程、RenderThread 或 Binder 线索。
```

## 第二部分：meminfo 看内存快照

基础命令：

```bash
adb shell dumpsys meminfo <package>
```

详细模式：

```bash
adb shell dumpsys meminfo -a <package>
```

常见关注：

```text
Java Heap
Native Heap
Graphics
Code
Stack
PSS
RSS
SwapPss
Objects
Activities
ViewRootImpl
```

如果用户说“用久了变慢”，meminfo 可以帮助你判断：

```text
是不是内存持续增长？
是不是 Activity 没释放？
是不是图片或 native 内存过大？
是不是 zRAM / SwapPss 很高？
```

内存问题不能只看 Java Heap。

现代 Android App 里，图片、图形缓冲、native 分配和 mmap 都可能很重要。

一个内存快照可以这样转成报告语言：

```text
PSS Total 从 180MB 增长到 420MB。
Java Heap 增长有限，但 Graphics 和 Native Heap 明显增加。
Activity 数量从 1 增加到 5，ViewRootImpl 数量未回落。
```

这时可疑方向就不是“Java 对象太多”这么简单，而要继续检查图片、Surface、native 分配和页面释放。

## 第三部分：procstats 看进程历史

`meminfo` 更像当前快照。

`procstats` 更像一段时间里的历史统计：

```bash
adb shell dumpsys procstats --hours 3
```

它适合回答：

```text
App 在后台运行过多久？
内存状态如何变化？
进程是否经常处于 cached / service / top？
系统是否长期处于内存压力？
```

第 19 章讲进程和 OOM Adj。

这里你要用 procstats 把进程状态变成证据。

## 第四部分：simpleperf 看 CPU 热点

当 Perfetto 告诉你某段时间 CPU 很忙，下一步可能需要 simpleperf。

simpleperf 适合回答：

```text
CPU 时间花在哪些函数、线程、so 或 Java/native 调用上？
```

常见流程：

```text
准备可复现操作
采样 CPU
生成报告
定位热点函数
结合源码判断是否可优化
```

典型问题：

```text
JSON 解析过重
图片处理在主线程
native so 算法耗时
压缩 / 加密 / 解码过慢
Compose 或布局计算频繁
```

simpleperf 不是第一工具。

它更适合在你已经知道“CPU 是嫌疑人”之后，用来找具体函数。

一个 simpleperf 报告可以先看三列：

```text
Overhead  Command          Symbol
28.10%    main-thread      CourseParser.parseLargeJson
14.30%    RenderThread     libhwui.so
 8.40%    DefaultDispatcher ImageDecoder.decode
```

读法是：

```text
Overhead 高，不等于一定要优化。
还要看它是否发生在关键用户路径。
如果热点在主线程，优先级通常更高。
如果热点在后台线程，要继续看它是否抢占 CPU、阻塞锁或影响主线程。
```

## 第五部分：性能证据要有前后对比

一次性能优化报告至少包含：

```text
设备
Android 版本
App 版本
复现路径
优化前指标
优化后指标
采集命令
是否冷启动 / 热启动
是否清缓存
是否重复多次
```

不要只写：

```text
优化后明显变快。
```

更好的是：

```text
同一设备、同一数据量、同一滑动路径下，
Janky frames 从 18% 降到 6%，
Perfetto 中主线程长任务从 42ms 降到 13ms。
```

这才叫工程证据。

## 第六部分：工具选择地图

| 你想知道 | 工具 |
| --- | --- |
| 哪一帧卡了 | Perfetto / gfxinfo |
| 主线程在干什么 | Perfetto / trace section |
| CPU 热点函数 | simpleperf |
| 当前内存分布 | meminfo |
| 一段时间进程状态 | procstats |
| 系统整体事故现场 | bugreport |

选择工具时不要贪多。

先问问题，再拿工具。

## 本节自测

- gfxinfo 和 Perfetto 在掉帧分析里各自负责什么？
- 为什么内存问题不能只看 Java Heap？
- simpleperf 为什么通常不是第一工具？
- 性能优化为什么必须保留优化前后证据？

## 本节总结

第 24 章的性能工具可以这样记：

```text
gfxinfo 看帧统计
Perfetto 看时间线
meminfo 看内存快照
procstats 看进程历史
simpleperf 看 CPU 函数热点
```

当你能把这些工具组合起来，性能优化就不再是“试试看”，而是有证据、有对比、有回归的工程过程。
