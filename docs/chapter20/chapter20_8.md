# 20.8 综合实践：稳定性诊断实验室

第 20 章最后一节，我们把 ANR、Java Crash、Native Crash、Watchdog、DropBox、bugreport、dumpsys 和稳定性治理闭环放进一个综合实践。

目标是：让你不只是知道这些名词，而是能从一次事故出发，找到证据、分类问题、写出结论、设计修复和回归验证。

## 本节剧情钩子

现在你已经知道：

```text
App 进程如何出生和被回收
主线程、Binder 线程和 worker 如何协作
ANR、Crash、Native Crash 和 Watchdog 分别是什么
```

综合实践要继续追问：

```text
如果线上出了问题
  -> 我应该先看哪里？
      -> 如何判断类型？
          -> 如何让证据支撑结论？
              -> 如何证明修复有效？
```

本节要把这些问题做成一套稳定性诊断流程。

## 本节定位

本节是第 20 章综合实践。

后续可以配套工程：

```text
examples/20-stability-diagnosis-lab/
```

这个工程可以围绕 ANR 触发、Java Crash 触发、native crash 占位实验、线程等待链、锁竞争、Binder 阻塞、DropBox / dumpsys 命令面板、稳定性报告模板和体验评分机制做成一个可运行实验室。

## 学习目标

学完本节后，你应该能够：

- 用统一模板记录稳定性问题。
- 模拟并区分主线程 ANR、Broadcast ANR、Service ANR。
- 读懂 Java Crash 中的异常类型、线程和第一业务栈。
- 初步认识 native tombstone 的关键信息。
- 知道什么时候收集 bugreport 和 dumpsys。
- 为稳定性修复设计回归验证。

## 第一部分：实践工程规划

第 20 章 demo 可以拆成这些可观察区域：

- `稳定性诊断分数`：提示实验完成度。
- `事故分类卡`：在 ANR、Java Crash、Native Crash、Watchdog、Low Memory、业务失败之间做选择。
- `ANR 实验卡`：触发主线程、Broadcast、Service、锁等待和 Binder 等待。
- `trace 阅读卡`：展示 main、Binder、锁、worker 的阅读顺序。
- `Java Crash 实验卡`：触发 NPE、越界、非法状态、后台线程异常。
- `Crash 栈阅读卡`：提取异常类型、线程名、第一业务栈。
- `Native Crash 观察卡`：解释 signal、tombstone、so、ABI 和符号化。
- `系统证据命令卡`：列出 logcat、dumpsys、bugreport、dropbox 命令。
- `恢复与降级卡`：模拟 remote 进程死亡、接口失败、缓存兜底。
- `稳定性报告卡`：按模板生成诊断报告。
- `事件轨迹`：记录每次触发、观察和修复动作。

## 第二部分：稳定性诊断路线

建议按四段完成：

```text
第一段：分类
  -> 判断是 ANR、Java Crash、Native Crash、Watchdog、低内存，还是业务失败

第二段：取证
  -> 收集 logcat、trace、stack、tombstone、dumpsys、bugreport

第三段：定位
  -> 找到线程、进程、组件、锁、远端 Binder 或 native so

第四段：治理
  -> 修复根因、增加恢复、设计降级、补监控和回归用例
```

这四段对应真实工作里的稳定性闭环。

## 第三部分：事故分类表

实践时可以先填这张表：

| 现象 | 第一判断 | 第一证据 |
| --- | --- | --- |
| 点击后卡住，系统提示无响应 | ANR | logcat ANR reason + traces |
| 打开页面立刻退出 | Java Crash | FATAL EXCEPTION + Java stack |
| 少数 ABI 设备闪退 | Native Crash | tombstone + signal + so |
| 后台回来状态丢失 | Low Memory / 状态恢复 | pid 变化 + Application 重建 |
| 多个 App 同时卡住 | 系统级问题 / Watchdog | bugreport + DropBox + system_server trace |
| 页面空白但无崩溃 | 业务失败 / 恢复失败 | 业务日志 + 页面成功率 |

先分类，再进入具体证据。

## 第四部分：ANR 实验路线

建议模拟：

```text
main thread sleep
  -> 观察 Input ANR

BroadcastReceiver.onReceive sleep
  -> 观察 Broadcast timeout

Service.onStartCommand sleep
  -> 观察 Service timeout

main waiting lock
  -> 观察 BLOCKED 和 held lock

main sync Binder call
  -> 观察跨进程等待
```

每次实验都要回答：

- ANR reason 是什么？
- main 线程状态是什么？
- 是否有锁等待？
- 是否有 Binder 等待？
- 根因线程是谁？
- 修复方向是什么？

## 第五部分：Crash 实验路线

建议模拟：

```text
NullPointerException
IndexOutOfBoundsException
IllegalStateException
background thread crash
```

每次实验都要提取：

```text
exception type
thread name
process name
pid
first app stack
trigger action
fix direction
regression case
```

Crash 实验不要只停在“触发闪退”，要训练“如何写修复报告”。

## 第六部分：Native 与系统证据路线

如果暂时不实现真实 native crash，也可以先做占位实验：

```text
展示 tombstone 样例
  -> 标出 signal
      -> 标出 crashing thread
          -> 标出 so
              -> 标出 ABI
                  -> 说明符号化前后差异
```

系统证据则可以提供命令卡：

```bash
adb logcat -d
adb shell dumpsys activity anr
adb shell dumpsys activity processes
adb shell dumpsys meminfo 包名
adb shell dumpsys dropbox
adb bugreport
```

让学习者知道不同事故应该拿不同工具。

## 第七部分：稳定性诊断报告模板

建议报告格式：

```text
问题标题：
问题类型：
用户现象：
发生时间：
设备 / 系统：
App 版本：
进程名：
pid：
线程名：
前后台状态：
是否多进程：
是否低内存：
ANR reason：
Java exception：
native signal：
tombstone：
关键 trace：
第一业务栈：
是否有锁等待：
是否有 Binder 等待：
是否有 remote 进程：
是否有降级：
根因：
修复方案：
回归用例：
灰度与监控：
```

报告不是为了形式，而是为了让结论经得起追问。

## 第八部分：本章通关检查

完成第 20 章后，请确认自己能回答：

- ANR 和卡顿有什么区别？
- Input ANR、Broadcast ANR、Service ANR 的触发点有什么不同？
- 读 ANR trace 时为什么先看 main？
- main 线程 BLOCKED 时下一步看什么？
- Binder 阻塞为什么可能要看两个进程？
- Java Crash 的第一业务栈怎么找？
- 全局异常捕获器为什么不能随便吞异常？
- Native Crash 为什么需要 tombstone 和符号表？
- Watchdog 主要监控什么？
- DropBox 和 bugreport 分别适合什么场景？
- 后台进程被杀为什么不等于 Crash？
- 稳定性修复为什么要有回归和监控？

## 本节小挑战

### 稳定性事故终局题

请分析下面问题：

```text
某版本上线后，低端机用户反馈：
进入课程详情页后偶尔卡住，随后系统提示应用无响应。
Crash 平台没有记录。
logcat 中出现 Input dispatching timed out。
ANR trace 显示 main 线程 waiting to lock CourseCache。
另一个 worker 线程持有 CourseCache，正在同步等待 remote Service 返回。
```

你需要回答：

- 这是 Crash 还是 ANR？
- 第一证据是什么？
- main 线程为什么卡住？
- 根因线程是谁？
- remote Service 是否参与？
- 修复方案是什么？
- 如何回归验证？
- 是否需要补监控？

## 本节实践任务

### 基础任务

- 写一份稳定性诊断报告模板。
- 收集一次 logcat 和 dumpsys activity anr。
- 读一份 Java Crash stack。
- 读一份 ANR trace。

### 进阶任务

- 为一个已有 App 增加稳定性上下文日志：processName、pid、threadName、前后台状态。
- 设计一套 ANR、Crash、Native Crash、Low Memory 的分类表。
- 为核心流程增加失败恢复和降级策略。

## 本节小结

第 20 章把 Framework 机制和工程稳定性接到了一起。

你不只是知道系统会产生 ANR、Crash、tombstone 和 Watchdog，而是要能把它们变成一条可操作路径：

```text
现象
  -> 分类
      -> 证据
          -> 根因
              -> 修复
                  -> 回归
                      -> 监控
                          -> 复盘
```

这条路径，就是稳定性工程能力真正落地的地方。
