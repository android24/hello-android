# 20.6 Watchdog、DropBox 与 bugreport：系统级事故证据

前面几节主要看 App 进程自己的事故：

```text
ANR
Java Crash
Native Crash
```

这一节把视角抬到系统级。

Android 不只监控 App，也要监控系统服务自身是否健康。

如果 `system_server` 中的关键线程或系统服务长时间无响应，问题就不再只是某个 App 卡住，而可能影响整个设备体验。

这时会出现：

```text
Watchdog
DropBox
bugreport
dumpsys
system_server trace
```

## 本节定位

本节负责解释：

- Watchdog 解决什么问题。
- DropBox 记录哪些系统事件。
- bugreport 为什么是系统级排查包。
- dumpsys 在稳定性诊断中如何使用。
- App 工程师为什么也要懂这些系统证据。

## 学习目标

学完本节后，你应该能够：

- 知道 Watchdog 主要监控 system_server 中关键 Handler 和服务。
- 知道 DropBox 可以记录系统异常、崩溃、ANR 等事件。
- 知道 bugreport 是多种系统证据的集合。
- 能用 dumpsys 辅助判断进程、Activity、内存和 ANR 状态。
- 能区分 App 级事故和系统级事故。

## 第一部分：Watchdog 监控的是谁

Watchdog 不是普通 App 里的“定时器”。

Android Framework 里的 Watchdog 主要用于监控 `system_server` 中关键线程和系统服务是否长时间卡住。

粗略理解：

```text
Watchdog
  -> 定期检查关键 Handler / Monitor
      -> 如果超时没有响应
          -> dump 线程栈
              -> 记录系统证据
                  -> 可能触发 system_server 重启
```

`system_server` 很重要，因为 AMS、WMS、PMS、Input、Power 等大量系统服务都在这里。

如果它卡死，影响可能是全局的。

### HandlerChecker 与 Monitor 模型

可以把 Watchdog 的核心思路理解成：

```text
给关键线程发一个检查任务
  -> 期待它在规定时间内执行
      -> 如果线程消息队列被卡住
          -> 检查任务迟迟无法执行
              -> Watchdog 认为该线程不可响应
```

同时，Watchdog 还可以检查一些系统服务 Monitor。

粗略模型是：

```text
Watchdog
  -> HandlerChecker
      -> 检查关键 Handler 线程是否能及时处理消息
  -> Monitor
      -> 检查关键系统服务锁或状态是否能及时进入
```

如果某个系统服务持有大锁做耗时任务，或者 system_server 关键线程卡在锁等待中，Watchdog 就可能抓到它。

这和 App ANR 的相似之处是：

```text
都在判断“关键线程是否能及时响应”
```

区别是：

```text
App ANR 面向应用进程和用户交互
Watchdog 面向 system_server 和系统服务健康
```

## 第二部分：App ANR 和 Watchdog 的区别

| 对比项 | App ANR | Watchdog |
| --- | --- | --- |
| 关注对象 | 某个 App 进程 | system_server / 系统服务 |
| 用户感知 | 某个 App 无响应 | 系统卡顿、全局异常、设备可能重启 |
| 典型证据 | ANR trace、logcat、dumpsys activity anr | system_server trace、watchdog log、DropBox |
| 常见根因 | 主线程耗时、锁、Binder、Receiver / Service 超时 | 系统服务死锁、Handler 不响应、锁等待 |

App 工程师为什么要懂 Watchdog？

因为有些问题表面发生在 App，但实际可能是系统服务被拖住：

```text
App 调用系统服务
  -> system_server 某服务锁等待
      -> 多个 App 调用堆积
          -> 全局卡顿或 ANR
```

这时只看 App 自己的代码，很难解释完整现场。

## 第三部分：DropBox 是系统事件箱

Android 的 DropBoxManager 可以记录系统级事件。

常见条目可能包括：

```text
system_app_crash
system_app_anr
data_app_crash
data_app_anr
system_server_watchdog
native_crash
lowmem
```

它像系统放事故摘要的箱子。

你可以通过命令观察：

```bash
adb shell dumpsys dropbox
```

不同设备、系统版本和权限下可见内容会有差异。

但概念上要记住：

```text
logcat 是流动日志
DropBox 是系统挑出来保存的关键事件
```

### DropBox 与 bugreport 的关系

DropBox 像系统保存的关键事件索引。

bugreport 像把很多系统状态打包成一个材料包。

它们不是互相替代：

```text
DropBox
  -> 告诉你系统保存过哪些关键事故条目

bugreport
  -> 给你事故前后更完整的系统状态
```

真实排查中可以这样配合：

```text
先从 DropBox 找到 data_app_anr / native_crash / system_server_watchdog
  -> 记录发生时间和进程
      -> 在 bugreport / logcat / dumpsys 中对齐同一时间线
          -> 再读具体 trace、stack 或 tombstone
```

## 第四部分：bugreport 是事故材料包

bugreport 不是一种日志，而是一包材料。

它通常包含：

- 系统属性。
- 进程列表。
- logcat。
- dumpsys 信息。
- ANR / crash 摘要。
- 电量、内存、CPU 状态。
- activity、window、package、input 等服务状态。

生成方式：

```bash
adb bugreport
```

或者：

```bash
adb shell bugreport
```

bugreport 适合分析：

- 难以复现的线上问题。
- 系统级卡顿。
- 厂商 ROM 差异。
- 低内存和后台回收。
- 输入、窗口、Activity 栈状态。

它的缺点是大、杂、需要筛选。

所以你要带着问题读：

```text
我要找 ANR？
我要找进程是否被杀？
我要找 Activity 栈？
我要找 system_server 是否卡住？
我要找 native crash？
```

### bugreport 阅读顺序

初学者打开 bugreport 最容易迷路。

不要从第一行读到最后一行。

建议按问题类型选择入口：

| 问题 | 优先入口 |
| --- | --- |
| ANR | ANR 摘要、traces、logcat、`dumpsys activity anr` |
| Crash | crash 摘要、logcat、DropBox 条目 |
| Native Crash | tombstone 摘要、native crash 条目、logcat |
| 后台回收 | `dumpsys activity processes`、lmkd / lowmem 日志、进程状态 |
| 窗口问题 | `dumpsys window`、Activity 栈、输入法窗口 |
| 输入问题 | `dumpsys input`、InputDispatcher 日志 |
| 系统卡死 | Watchdog、system_server trace、DropBox |

一个实用顺序是：

```text
先找发生时间
  -> 再找问题类型摘要
      -> 再进入对应 dumpsys 区域
          -> 再回到 logcat 对齐时间线
              -> 最后补充进程、内存、窗口和输入状态
```

bugreport 不是一本书，而是一箱证据。

你要像整理案卷一样，从问题入口开始抽材料。

## 第五部分：dumpsys 是现场查询工具

`dumpsys` 可以向系统服务询问当前状态。

常用命令：

```bash
adb shell dumpsys activity
adb shell dumpsys activity processes
adb shell dumpsys activity anr
adb shell dumpsys window
adb shell dumpsys input
adb shell dumpsys package 包名
adb shell dumpsys meminfo 包名
adb shell dumpsys dropbox
```

第 12 章看 Activity 启动时，我们用它观察任务栈。

第 13 章看窗口时，我们用它观察 Window。

第 19 章看进程时，我们用它观察进程状态。

第 20 章看稳定性时，它继续作为现场查询工具。

同一个工具，在不同章节服务不同问题。

## 第六部分：系统证据如何和 App 证据拼起来

一次稳定性事故的证据可能来自多个地方：

```text
App 日志
  -> 用户操作和业务状态

logcat
  -> 系统调度和异常摘要

ANR trace
  -> 线程现场

tombstone
  -> native 崩溃现场

DropBox
  -> 系统保存的关键事件

bugreport
  -> 全局状态包
```

排查时不要把它们割裂。

例如：

```text
用户说点击无响应
  -> App 日志确认点击时间
      -> logcat 找 ANR reason
          -> traces 看 main 等锁
              -> dumpsys activity processes 看前后台状态
                  -> bugreport 补系统内存和进程状态
```

证据越能对齐，结论越可靠。

### 一段 DropBox / bugreport 摘要样例

下面是一段教学用的简化摘要：

```text
DropBox entry: data_app_anr
Process: com.example.course
Time: 2026-08-16 21:18:42
Reason: Input dispatching timed out

dumpsys activity processes:
  Proc #12: cached com.example.course/u0a123
  pid=24680 adj=900 state=Cached

logcat:
  InputDispatcher: Application is not responding: com.example.course
```

逐行读：

| 线索 | 说明 |
| --- | --- |
| `data_app_anr` | 这是普通应用 ANR，不是 system_server Watchdog。 |
| `Process` | 确认目标进程。 |
| `Time` | 用来和业务日志、logcat、trace 对齐。 |
| `Reason` | 输入分发超时，先看 main 线程。 |
| `adj=900 state=Cached` | 进程状态偏后台，需要确认是否存在后台恢复、冷启动或状态丢失。 |
| `InputDispatcher` | 输入系统侧已经确认 App 未响应。 |

这类摘要不能直接给出根因，但它能告诉你下一步：

```text
去找同一时间点的 ANR trace
  -> 看 main 状态
      -> 看锁、Binder、worker
          -> 再结合进程前后台状态判断是否和恢复路径有关
```

## 第七部分：什么时候需要 bugreport

不是每个 Crash 都需要 bugreport。

适合收集 bugreport 的场景：

- 系统级卡顿。
- 多个 App 同时异常。
- 输入、窗口、任务栈状态异常。
- 后台回收、低内存、进程频繁死亡。
- 厂商 ROM 或系统版本相关问题。
- Watchdog、system_server、native 系统组件异常。

不适合只靠 bugreport 的场景：

- 明确 NPE。
- 明确业务参数错误。
- 本地可以 100% 复现的普通 Crash。

工具要和问题匹配。

## 本节小挑战

### 系统证据选择题

下面问题你会优先收集什么？

```text
1. 某 Activity 页面返回栈异常。
2. 输入法弹出后窗口遮挡。
3. App 后台回来 pid 变化，草稿丢失。
4. 系统整体卡住，多个 App 都无响应。
5. 某 native so 崩溃。
```

你需要回答：

- 哪些用 dumpsys activity？
- 哪些用 dumpsys window / input？
- 哪些用 bugreport？
- 哪些需要 tombstone？
- 哪些可能和 Watchdog 相关？

## 本节实践任务

### 基础任务

- 执行一次 `adb shell dumpsys activity processes`。
- 执行一次 `adb shell dumpsys meminfo 包名`。
- 执行一次 `adb shell dumpsys dropbox`。

### 进阶任务

- 生成一次 bugreport，并从中找到 activity、window、dropbox 相关内容。
- 整理一份团队常用稳定性命令清单。
- 模拟一次 ANR 后，对比 logcat、dumpsys 和 trace 的信息差异。

## 本节小结

Watchdog、DropBox、bugreport 和 dumpsys 是系统级稳定性诊断的工具箱。

你不需要一开始读懂 bugreport 里的每一行，但要知道：

```text
出了什么问题
  -> 哪个系统服务可能知道现场
      -> 哪个工具能把现场 dump 出来
```

这就是从 App 工程师走向 Framework 工程师的重要一步。
