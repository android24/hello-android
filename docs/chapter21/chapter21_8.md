# 21.8 综合实践：后台任务与系统调度观察实验

第 21 章最后一节，我们把 Service、Foreground Service、AlarmManager、JobScheduler、WorkManager、Doze、App Standby、Battery Saver 和后台任务体验问题放进一个综合实践。

目标是：让你不只是知道后台任务 API，而是能从一个业务需求出发，判断它应该被立即执行、延迟执行、用户感知执行，还是根本不应该在后台偷偷执行。

## 本节剧情钩子

现在你已经知道：

```text
App 进程可能被系统回收
后台任务会受到系统调度和省电策略影响
前台服务需要用户感知
WorkManager 适合可靠但可延迟的任务
AlarmManager 适合时间触发但不等于无限精确
```

综合实践要继续追问：

```text
一个后台任务没有按预期执行
  -> 是没有入队？
      -> 是约束不满足？
          -> 是系统延迟？
              -> 是权限或服务类型不合规？
                  -> 是业务设计本身不该这样做？
```

本节要把这些问题做成一套后台调度观察流程。

## 本节定位

本节是第 21 章综合实践。

配套示例工程：

```text
examples/21-background-scheduling-lab/
```

这个工程围绕前台服务、WorkManager、真实 WorkInfo 状态、Alarm、任务约束、Doze 命令卡、ADB 证据挑战、后台限制诊断、任务日志和事故剧本做成一个可运行实验室。

它不是为了证明“某个 API 一定会立刻执行”，而是为了训练一个更重要的能力：

```text
当后台任务没有按预期发生时
  -> 你能不能解释它为什么这样表现
```

## 学习目标

学完本节后，你应该能够：

- 区分立即任务、可靠任务、定时任务和用户可感知持续任务。
- 观察前台服务启动、通知和停止流程。
- 观察 WorkManager 在不同约束下的执行状态，并用真实 `WorkInfo` 校验页面判断。
- 观察 Alarm 注册、触发和可能延迟的现象。
- 使用 `dumpsys jobscheduler / alarm / deviceidle` 初步排查任务为什么没执行。
- 写出一份后台任务诊断报告。

## 第一部分：实践工程规划

第 21 章 Demo 可以拆成这些可观察区域：

- `任务决策卡`：根据业务语义推荐 Service、Foreground Service、WorkManager、AlarmManager。
- `前台服务实验区`：启动、停止、展示通知、记录服务生命周期。
- `WorkManager 实验区`：入队一次性任务、唯一任务、约束任务、取消任务，并刷新真实 `WorkInfo`。
- `Alarm 实验区`：注册提醒，观察期望触发、真实触发和时间窗口。
- `系统状态面板`：展示前后台、网络、电量、充电、进程、pid。
- `Doze / 省电命令卡`：列出 deviceidle、jobscheduler、alarm 等观察命令，并提供 ADB 证据挑战。
- `任务时间线`：记录入队、开始、成功、失败、取消、重试。
- `事故剧本模式`：模拟任务没执行、提醒不准时、下载被中断、耗电投诉。
- `后台任务诊断报告`：输出现象、证据、原因、修复和回归。
- `体验评分机制`：提醒学习者是否完成了“决策、触发、观察、诊断、恢复”五步。

## 第一部分补充：体验评分怎么设计

Demo 可以把学习过程拆成 11 个观察点：

| 观察点 | 得分条件 |
| --- | --- |
| 运行现场 | 记录 package、process、pid、前后台状态 |
| 任务决策 | 能为一个需求选择合适后台机制 |
| 前台服务 | 启动并停止一次用户可感知任务 |
| Work 入队 | 入队一次可靠任务并观察状态 |
| Work 约束 | 设置网络 / 充电等约束并解释延迟 |
| Alarm 注册 | 注册一次提醒并记录期望时间 |
| Alarm 触发 | 对比期望触发和实际触发 |
| 系统命令卡 | 阅读 jobscheduler、alarm、deviceidle 等入口 |
| ADB 证据挑战 | 至少执行一个 dumpsys 命令，并把输出和页面状态对上 |
| 事故剧本 | 完成至少一个后台事故判断 |
| 诊断报告 | 写出证据、原因、修复和回归 |

评分不是为了游戏化而游戏化，而是为了防止学习者只点按钮、不复盘。

## 第二部分：后台任务决策路线

建议按四段完成：

```text
第一段：定性
  -> 这是立即任务、可靠任务、定时任务，还是用户可感知持续任务？

第二段：选择机制
  -> Service / Foreground Service / WorkManager / JobScheduler / AlarmManager

第三段：观察证据
  -> logcat、dumpsys activity services、dumpsys jobscheduler、dumpsys alarm、dumpsys deviceidle

第四段：设计恢复
  -> 幂等、重试、取消、状态持久化、通知和用户反馈
```

这四段对应真实工作里的后台任务设计闭环。

## 第二部分补充：后台任务选择表

实践时可以先填这张表：

| 业务需求 | 第一判断 | 推荐机制 | 关键证据 |
| --- | --- | --- | --- |
| 用户正在下载离线课程 | 用户可感知持续任务 | Foreground Service / 用户主动数据传输 | 通知、进度、服务生命周期 |
| 上传学习记录 | 可靠但可延迟 | WorkManager | Work state、约束、重试 |
| 每天学习提醒 | 时间触发 | AlarmManager + 通知 | alarm 注册、触发时间、通知权限 |
| 清理缓存 | 可延迟维护任务 | WorkManager / JobScheduler | 设备状态、任务日志 |
| 后台轮询消息 | 高风险设计 | Push / 服务端事件 / 重新设计 | 轮询频率、耗电、用户感知 |
| 直播开播提醒 | 强时间相关提醒 | Alarm / Push / 通知组合 | 时间承诺、权限、用户设置 |

## 第三部分：事故剧本怎么玩

后台问题最有意思的地方，是它常常披着业务问题的外衣。

Demo 可以提供几个事故剧本：

| 剧本 | 表面现象 | 第一线索 | 隐藏陷阱 |
| --- | --- | --- | --- |
| 离线课程没有下载完 | 用户切后台后下载中断 | 前台服务通知消失 | 下载任务不该只靠页面协程 |
| 学习提醒晚了 15 分钟 | Alarm 没准点触发 | 设备处于 Doze | 普通 Alarm 不是精确闹钟 |
| 学习记录没同步 | Work 已入队但未运行 | 约束要求 Wi-Fi + 充电 | 条件没满足不是任务丢失 |
| 一晚上耗电很高 | 后台频繁上报日志 | 多个任务短周期重试 | 重试没有退避和批量 |
| Android 新版本启动服务失败 | 后台启动 Service 异常 | logcat 有前台服务限制 | 服务启动语义不合规 |

玩剧本时先不要看答案，先写下自己的第一判断：

```text
这是任务没入队、没执行、被延迟、被取消，还是设计不合规？
第一证据应该看哪里？
下一步要查系统状态还是业务日志？
这个任务是否应该用户可感知？
```

## 第四部分：推荐观察命令

前台服务：

```bash
adb shell dumpsys activity services
adb shell dumpsys activity processes
adb shell dumpsys notification
adb logcat | grep -i service
```

WorkManager / JobScheduler：

```bash
adb shell dumpsys jobscheduler
adb logcat | grep -i WorkManager
```

Alarm：

```bash
adb shell dumpsys alarm
```

Doze / 省电：

```bash
adb shell dumpsys deviceidle
adb shell dumpsys deviceidle force-idle
adb shell dumpsys deviceidle step
adb shell dumpsys deviceidle unforce
adb shell dumpsys battery
```

进程：

```bash
adb shell dumpsys activity processes
adb shell ps -A | grep 包名
```

## 第四部分补充：命令观察顺序

排查后台任务时，命令不要乱敲。可以按下面顺序：

```text
先看任务自己
  -> logcat / App 内任务时间线

再看系统是否接收任务
  -> dumpsys jobscheduler / dumpsys alarm / dumpsys activity services

再看系统状态是否允许执行
  -> dumpsys deviceidle / dumpsys battery / dumpsys activity processes

最后看用户可感知入口
  -> dumpsys notification / 权限状态 / 前台服务通知
```

这和第 20 章稳定性诊断一样：先证据，后结论。

## 第五部分：后台任务诊断报告模板

建议报告格式：

```text
问题标题：
任务类型：
用户现象：
触发来源：
App 前后台状态：
进程名：
pid：
Android 版本：
设备 / 厂商：
网络状态：
电池状态：
是否充电：
是否 Doze：
是否省电模式：
是否有通知权限：
是否使用前台服务：
前台服务类型：
是否使用 WorkManager：
Work state：
约束条件：
入队时间：
开始时间：
完成时间：
重试次数：
Alarm 注册时间：
Alarm 触发时间：
第一证据：
根因判断：
修复方案：
回归用例：
监控指标：
```

这份报告要回答的不是“代码哪里错了”，而是：

```text
这个后台任务为什么在这个系统状态下表现成这样？
```

## 第六部分：本章通关检查

完成第 21 章后，请确认自己能回答：

- Service 为什么不是后台线程？
- 什么任务适合 Foreground Service？
- 前台服务为什么需要通知和服务类型？
- AlarmManager 为什么不一定准时？
- 精确闹钟为什么不能滥用？
- WorkManager 的可靠性体现在哪里？
- 约束越多为什么越可能延迟？
- Doze 和 App Standby 为什么会影响任务执行？
- 厂商后台限制为什么会造成机型差异？
- 为什么保活不是健康的工程目标？
- 后台任务如何设计幂等和恢复？
- 一个后台任务没执行，第一证据应该看哪里？

## 第六部分补充：本章最终事故题

请分析下面这个综合事故：

```text
某课程 App 上线后，用户集中反馈：

1. 离线课程下载到一半，切后台后经常停住。
2. 每天 8 点学习提醒，有些设备会晚几分钟才出现。
3. 学习记录同步偶尔延迟半小时，但最终又会成功。
4. Android 新版本上，某些用户点击“继续下载”后前台服务启动失败。
5. 一部分低频用户反馈：打开 App 后才看到一堆历史任务开始执行。

日志和系统证据：

- 下载任务最初只放在页面协程里，进度没有完整持久化。
- 提醒使用普通 Alarm，没有申请精确闹钟能力。
- 学习记录同步使用 WorkManager，约束要求 Wi-Fi + 充电。
- `dumpsys deviceidle` 显示部分设备处于 idle。
- `dumpsys jobscheduler` 显示同步任务处于 pending，约束未满足。
- logcat 出现 ForegroundServiceStartNotAllowedException。
- 部分用户很久没有打开 App，可能处于较低 standby bucket。
```

你需要回答：

- 哪些问题属于设计错误，哪些属于系统调度的正常表现？
- 离线下载应该如何改造，是否需要前台服务、通知和状态持久化？
- 8 点提醒是否一定需要精确闹钟？如何判断？
- 学习记录同步为什么会延迟？约束是否设置过严？
- 前台服务启动失败可能发生在系统判断链路的哪一步？
- 低频用户打开 App 后任务集中执行，和 App Standby 有什么关系？
- 你会用哪些 `dumpsys` 命令继续取证？
- 修复后如何设计回归用例和监控指标？

这道题没有唯一答案，但有一条底线：

```text
不能只说“系统限制导致”。
必须把限制拆成具体证据、系统状态、业务语义和修复方案。
```

## 本节实践任务

### 基础任务

- 写一份后台任务诊断报告模板。
- 设计一个“学习记录同步”任务，说明为什么选择 WorkManager。
- 设计一个“离线课程下载”任务，说明为什么可能需要 Foreground Service。
- 设计一个“每天学习提醒”，说明 AlarmManager 和通知权限的关系。
- 用表格区分 Service、Foreground Service、WorkManager、JobScheduler、AlarmManager。
- 完成一次“任务没执行”的纸面推理：分别从约束、Doze、省电、权限、进程死亡、厂商限制中找可能原因。

### 进阶任务

- 为一个后台任务增加入队、开始、成功、失败、重试和取消日志。
- 设计一套任务幂等 key，避免重复上传或重复下载。
- 设计一个后台任务事故剧本，并写出排查路线。
- 用 dumpsys jobscheduler / alarm / deviceidle 收集一次系统状态证据。
- 为后台任务设计监控指标：成功率、等待时间、重试次数、失败原因和机型分布。
- 设计一个“后台任务实验室”的页面草图：任务决策卡、调度状态、系统状态、事件时间线、诊断报告五个区域。

## 第七部分：Demo 已具备的玩法

`examples/21-background-scheduling-lab/` 已经按下面路径组织：

```text
第一步：任务决策与时间线
  -> 先让读者看懂“为什么选这个机制”，再观察事件发生顺序

第二步：WorkManager 实验
  -> 入队、约束、唯一任务、真实 WorkInfo、取消任务、状态观察

第三步：Foreground Service 实验
  -> 通知、启动、停止、生命周期日志

第四步：Alarm 实验
  -> 注册、期望时间、实际触发、时间窗口

第五步：Doze / dumpsys 命令卡
  -> 不自动执行命令，而是训练读者知道该去哪里取证

第六步：ADB 证据挑战
  -> 至少完成 Work、Alarm、Doze 中的一个系统证据观察

第七步：事故剧本和诊断报告
  -> 把实验结果变成工程判断
```

这个 Demo 不追求模拟所有系统限制。第 21 章 Demo 最重要的是让读者形成判断路径：

```text
任务语义
  -> 系统机制
      -> 运行证据
          -> 失败原因
              -> 恢复方案
```

## 第八部分：Demo 的原理观察点

第 21 章 Demo 不能只做成：

```text
启动服务按钮
启动 Work 按钮
注册 Alarm 按钮
```

这样会退回 API 示例。

更好的设计是让每个按钮都对应一个系统判断点。

| 实验 | 观察点 | 想证明什么 |
| --- | --- | --- |
| 普通 Service 启动 | 前后台状态、Service 生命周期、logcat | Service 是组件，不是后台线程 |
| 前台服务启动 | 通知、服务类型、启动时机 | 前台服务是用户可感知契约 |
| 延迟调用 startForeground | 异常或系统拒绝日志 | `startForegroundService` 有时间窗口 |
| Work 入队 | ENQUEUED / RUNNING / SUCCEEDED | 入队不等于立刻运行 |
| Work 约束 | 网络、充电、存储状态 | 约束不满足时延迟不是 bug |
| 唯一 Work | replace 行为、Work id、真实 WorkInfo | 后台任务要有业务唯一性 |
| Work 取消 | CANCELLED / IDLE / 事件时间线 | 取消也是后台任务状态机的一部分 |
| Alarm 注册 | 期望时间、实际触发时间 | Alarm 是时间入口，不是执行保证 |
| Doze 命令卡 | deviceidle 状态、任务延迟 | 系统会把任务挪到维护窗口 |
| ADB 挑战 | jobscheduler / alarm / deviceidle 输出 | 结论要能被系统证据支撑 |
| 事故剧本 | 现象、证据、根因、修复 | 后台任务需要证据链 |

每个实验页面都应该显示：

```text
业务期望
系统机制
当前状态
第一证据
下一步命令
复盘结论
```

这样读者才能从“我点了一个按钮”进入“我理解了系统为什么这么做”。

## 第九部分：AOSP 源码阅读入口

如果想把第 21 章继续往 Framework 深处读，可以从这些入口开始：

```text
Service / Foreground Service
  -> ActivityManagerService
  -> ActiveServices

JobScheduler / WorkManager 底层调度
  -> JobSchedulerService
  -> JobServiceContext
  -> JobStore

Alarm
  -> AlarmManagerService

Doze / Idle
  -> DeviceIdleController

App Standby
  -> AppStandbyController

通知与前台服务可感知
  -> NotificationManagerService
```

建议不要先读完整源码，而是带着具体问题进入：

```text
为什么后台启动前台服务失败？
为什么 Job 还在 pending？
为什么 Alarm 被延迟？
为什么 idle 下只有一小段执行窗口？
```

源码阅读的目标不是背类名，而是把系统证据和系统判断点对上。

## 本节小结

第 21 章把异步编程、进程模型、稳定性诊断和系统调度连接到了一起。

你不只是知道几个后台 API，而是要能把后台需求翻译成工程决策：

```text
业务语义
  -> 系统机制
      -> 执行约束
          -> 任务证据
              -> 失败恢复
                  -> 用户反馈
                      -> 监控复盘
```

真正成熟的后台任务设计，不是让 App 永远活着，而是让用户真正需要的事情，在系统允许的边界内可靠完成。
