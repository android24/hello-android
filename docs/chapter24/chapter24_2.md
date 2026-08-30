# 24.2 logcat、结构化日志与时间点定位

很多事故的第一份证据，来自 logcat。

但 logcat 不是“把日志全贴出来”。

它真正的价值是：

```text
帮你找到事件发生的时间点、线程、进程、tag 和第一条异常线索。
```

如果时间点错了，后面的 dumpsys、Perfetto、bugreport 都可能被你读偏。

## 本节先记住三句话

```text
日志首先用来定位时间点，不是直接证明根因。
好日志应该能串起用户动作、业务状态和系统证据。
没有脱敏和分级的日志，可能从证据变成事故。
```

## 贯穿案例：点击课程详情后卡住

用户说：

```text
我点课程详情，有时候页面卡住两三秒。
```

你不要先问：

```text
是不是网络慢？
```

先拿时间点：

```text
用户什么时候点的？
App 当时有没有记录点击事件？
主线程有没有 long task、ANR 前兆或异常日志？
同一时间有没有 GC、Binder、数据库、网络重试日志？
```

logcat 的第一任务，就是把“有时候”变成一个可追踪的时间窗口。

## 现场侦探问题

如果测试只给你一句：

```text
页面偶尔卡。
```

你会要求他补哪三样？

```text
复现步骤
大概时间点
完整 logcat 或 bugreport
```

其中时间点最关键。

没有时间点，证据海就是一片雾。

## 学习目标

学完本节后，你应该能够：

- 知道 logcat 适合定位事件、异常和时间窗口。
- 能选择合适的 buffer、tag、priority 和输出格式。
- 能把业务日志写成可复盘的结构。
- 知道日志脱敏、日志分级和 release 日志边界。
- 能从日志进入 dumpsys、Perfetto 或 bugreport。

## 第一部分：logcat 看什么

logcat 常见价值有四类：

```text
事件
  -> 用户点击、页面进入、任务开始、任务结束

异常
  -> Java Crash、ANR 前日志、权限异常、Provider 异常

状态
  -> 当前 userId、feature flag、网络状态、任务 id、数据版本

桥接
  -> 把业务动作和系统证据连接起来
```

你要避免只写：

```text
start
success
failed
```

更好的日志是：

```text
CourseDetail open courseId=42 source=home traceId=learn-2026-0001
CourseDetail loadLocal begin traceId=learn-2026-0001 thread=main
CourseDetail loadLocal end costMs=86 traceId=learn-2026-0001
CourseDetail syncRemote skipped reason=standby_bucket_rare traceId=learn-2026-0001
```

这类日志能让你从业务动作走向系统证据。

## 第二部分：常用命令

基础查看：

```bash
adb logcat
```

带时间和线程：

```bash
adb logcat -v threadtime
```

只看某个 tag：

```bash
adb logcat -v threadtime HelloAndroid:D *:S
```

导出当前已有日志：

```bash
adb logcat -d -v threadtime > logcat.txt
```

清空后重新复现：

```bash
adb logcat -c
adb logcat -v threadtime > reproduce-logcat.txt
```

看全部 buffer：

```bash
adb logcat -b all -v threadtime
```

不同设备和版本支持的参数可能不同，先看设备自己的帮助：

```bash
adb logcat --help
```

## 第三部分：日志 buffer 和优先级

logcat 不是只有一个桶。

常见 buffer：

| buffer | 用途 |
| --- | --- |
| main | App 和大部分 Java 层日志 |
| system | Framework 系统日志 |
| crash | 崩溃相关日志 |
| events | 结构化事件日志 |
| radio | 通信相关日志 |
| kernel | 部分内核日志，依设备权限而定 |

常见优先级：

```text
V -> Verbose
D -> Debug
I -> Info
W -> Warning
E -> Error
F -> Fatal
S -> Silent
```

排查事故时，不要只看 `E`。

很多关键线索可能是：

```text
W：系统警告
I：状态变化
D：业务埋点
```

真正重要的是同一个时间窗口里的前后关系。

## 第四部分：结构化日志怎么写

一条好日志至少包含：

```text
场景
动作
结果
耗时
traceId
关键业务 id
线程或协程信息
失败原因
```

例如后台同步：

```text
LearnSync run begin traceId=sync-42 worker=CourseSyncWorker network=CONNECTED
LearnSync query dirtyCount=8 traceId=sync-42
LearnSync upload fail code=503 retry=true traceId=sync-42 costMs=1204
```

这比：

```text
sync failed
```

有用得多。

## 第五部分：日志和隐私

第 23 章讲过安全和数据保护。

第 24 章要把它放进观测工具里：

```text
日志也是数据出口。
```

不要直接打印：

```text
token
手机号
身份证
完整地址
完整 content Uri
完整请求体
加密前明文
```

建议做三件事：

```text
release 只保留必要日志
敏感字段统一脱敏
事故报告导出前让用户确认
```

否则你可能把一个性能问题，变成一个隐私事故。

## 第六部分：日志不能证明什么

日志很重要，但它不是万能证据。

日志通常不能直接证明：

```text
CPU 调度顺序
线程到底在什么时间运行
Binder 等待多长
一帧为什么掉了
系统服务内部状态是否满足
进程为什么被回收
```

这些需要继续看：

```text
Perfetto
dumpsys
bugreport
gfxinfo
meminfo
simpleperf
```

日志像事故现场的文字记录。

Perfetto 更像时间线录像。

dumpsys 更像系统服务当时的体检表。

## 本节自测

- 为什么 logcat 第一价值是时间点，而不是根因？
- `-v threadtime` 为什么适合排查复杂问题？
- 为什么 release 日志需要分级和脱敏？
- 如果日志里只有 `failed`，你还缺什么信息？

## 本节总结

logcat 是证据链入口。

它不能解释所有问题，但它能帮你找到：

```text
谁
在什么时候
做了什么
得到什么结果
下一份证据应该去哪拿
```

从第 24 章开始，请把日志当成工程证据来设计，而不是临时打印。
