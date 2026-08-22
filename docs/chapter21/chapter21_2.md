# 21.2 Android 后台限制：系统为什么不让 App 随便运行

很多后台问题的表面现象都是：

```text
我的任务为什么没有执行？
```

但系统真正关心的是另一组问题：

```text
这个任务是否值得现在执行？
用户是否知道它在执行？
它是否会影响电量、性能和其他 App？
它是否可以延迟到更合适的时机？
```

理解后台限制，不能只背 API 版本差异。你要先理解 Android 的后台治理逻辑。

## 本节定位

本节负责回答：

- Android 为什么从“允许后台自由运行”走向“限制后台执行”？
- 后台限制主要限制了什么？
- 为什么后台任务延迟不一定是 bug？
- 后台限制和第 19 章进程优先级、第 20 章稳定性有什么关系？

## 学习目标

学完本节后，你应该能够：

- 解释后台限制的核心目标：省电、性能、公平性和用户可感知。
- 区分后台进程、后台 Service、后台任务和系统调度任务。
- 知道 Android 后台限制大致经历了哪些方向的演进。
- 能从系统角度解释“任务为什么被延迟、停止或拒绝启动”。

## 第一部分：后台自由运行的代价

假设系统完全不限制后台。

那么每个 App 都可以这样做：

```text
常驻 Service
频繁定时唤醒
后台轮询网络
收到广播就拉起进程
互相唤醒
持有 WakeLock
长期占用定位、蓝牙、网络或传感器
```

单个 App 看起来没问题。

但几十个 App 同时这么做，用户会看到：

- 电量下降很快。
- 手机发热。
- 后台流量增加。
- 前台 App 变卡。
- 系统频繁杀进程。
- 待机一夜掉电明显。

所以后台限制本质上是在做资源治理：

```text
谁可以运行？
什么时候运行？
运行多久？
是否必须让用户感知？
是否可以合并调度？
失败后如何重试？
```

## 第二部分：Android 限制的不是“后台”，而是“不受约束的后台”

Android 并不是禁止后台任务。

它禁止的是不受约束、不可感知、无法调度的后台行为。

例如：

| 行为 | 系统态度 |
| --- | --- |
| 用户正在听音乐 | 允许前台服务持续运行 |
| 用户正在下载离线课程 | 允许前台服务 + 通知 |
| 稍后上传学习记录 | 交给 WorkManager / JobScheduler |
| 每 5 秒后台轮询接口 | 通常不鼓励 |
| 后台偷偷启动 Service | 受限制 |
| 频繁精确唤醒设备 | 受权限和策略限制 |
| 为了“保活”互相拉起 | 不可靠且不合规 |

系统希望后台工作具备三个特征：

```text
可调度
可感知
可约束
```

## 第三部分：几个关键概念

### 后台进程

后台进程是指 App 当前没有被用户直接使用，或者优先级较低。

第 19 章讲过：

```text
前台进程
可见进程
服务进程
缓存进程
```

这些状态会影响 OOM Adj，也影响进程是否容易被系统回收。

### 后台 Service

Service 不是后台线程。

Service 是一个组件，它的回调默认仍然运行在主线程。更重要的是，从较新的 Android 版本开始，App 在后台启动普通 Service 会受到限制。

### 前台服务

Foreground Service 是告诉系统和用户：

```text
我正在做一件用户应该知道的持续任务。
```

它通常需要通知，并且越来越强调服务类型和权限。

### 系统调度任务

JobScheduler、WorkManager 这类机制不是让任务立刻执行，而是让系统在合适的时机执行。

合适时机可能取决于：

```text
网络
充电
空闲
电量
存储
应用待机桶
系统负载
任务配额
```

### Alarm

AlarmManager 负责时间触发。

但时间触发不等于一定准时执行。Doze、精确闹钟权限、电池策略都可能影响它。

## 第四部分：后台限制的典型方向

可以把 Android 的后台治理理解成几条线：

```text
限制后台直接启动
  -> 后台不再随便 startService

限制后台频繁唤醒
  -> Alarm、广播和网络被合并或延迟

强调用户可感知
  -> 前台服务需要通知和类型声明

鼓励系统调度
  -> JobScheduler / WorkManager 承接可靠任务

控制精确时间能力
  -> 精确闹钟需要更明确的理由和权限
```

这些限制合起来，就是系统对后台任务的基本态度：

```text
不是不能做
但要说明为什么做、什么时候做、用户是否知道、系统能否调度
```

## 第五部分：系统侧到底在判断什么

从 Framework 视角看，后台限制不是一个单独开关，而是一串连续判断。

可以先记住这条链路：

```text
App 请求后台执行
  -> Framework 识别调用方 uid / package / userId
      -> 查询进程状态和前后台状态
          -> 判断任务是否用户可感知
              -> 判断权限、服务类型、通知能力
                  -> 判断 Doze、App Standby、省电模式和配额
                      -> 决定允许、延迟、合并、拒绝、停止或抛异常
```

这里每一步都可能改变结果。

例如同样是“启动一个任务”：

| 系统看到的上下文 | 可能结果 |
| --- | --- |
| App 在前台，用户点击下载 | 允许启动用户可感知任务 |
| App 在后台，偷偷启动普通 Service | 可能直接拒绝 |
| App 后台入队 Work，要求 Wi-Fi + 充电 | 入队成功，但等待条件满足 |
| 设备进入 Doze，普通 Alarm 到点 | 可能延迟到维护窗口 |
| App 处于 Restricted 待机状态 | Job / Alarm 机会减少 |
| 前台服务类型和权限不匹配 | 可能抛异常或启动失败 |

这也是为什么后台问题不能只看一行业务代码。

业务代码只告诉你：

```text
我请求了什么。
```

系统证据才告诉你：

```text
系统在什么状态下如何处理这个请求。
```

## 第五部分补充：后台任务系统判断总图

把 Service、Alarm、WorkManager、Doze 和前台服务放在一起，可以得到一张总图：

```text
业务请求
  -> 是用户正在感知的持续任务吗？
      -> 是：考虑 Foreground Service / 用户主动数据传输
          -> 检查通知、服务类型、权限、启动时机
      -> 否：
          -> 是可靠但可延迟任务吗？
              -> 是：考虑 WorkManager / JobScheduler
                  -> 检查约束、重试、唯一任务、配额、待机桶
              -> 否：
                  -> 是时间触发任务吗？
                      -> 是：考虑 AlarmManager
                          -> 检查是否精确、是否 Doze、是否有通知权限
                      -> 否：
                          -> 重新审视业务设计，避免后台轮询和保活误区

系统状态
  -> 前后台 / uid state / 进程优先级
  -> Doze / App Standby / Battery Saver
  -> 网络 / 充电 / 空闲 / 存储
  -> 权限 / 通知 / 前台服务类型
  -> Job / Alarm / FGS 配额和限制

最终结果
  -> 允许
  -> 延迟
  -> 合并
  -> 等待条件
  -> 拒绝启动
  -> 抛异常
  -> 运行后被停止
```

这张图是第 21 章的主线。后面每一个 API，都可以放回这张图里看。

## 第六部分：Framework 源码阅读入口

第 21 章不要求你一次读完整个后台调度子系统，但建议先记住几个入口。

```text
frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
frameworks/base/services/core/java/com/android/server/am/ActiveServices.java
frameworks/base/services/core/java/com/android/server/job/JobSchedulerService.java
frameworks/base/services/core/java/com/android/server/alarm/AlarmManagerService.java
frameworks/base/services/core/java/com/android/server/DeviceIdleController.java
frameworks/base/services/core/java/com/android/server/usage/AppStandbyController.java
frameworks/base/services/core/java/com/android/server/notification/NotificationManagerService.java
```

这些类大致对应：

| 入口 | 负责观察什么 |
| --- | --- |
| `ActivityManagerService` | 进程状态、组件启动、uid 活跃状态 |
| `ActiveServices` | Service 启动、绑定、前台服务状态 |
| `JobSchedulerService` | Job 入队、约束、配额、执行窗口 |
| `AlarmManagerService` | Alarm 批处理、触发、idle 下行为 |
| `DeviceIdleController` | Doze 状态、维护窗口、临时白名单 |
| `AppStandbyController` | App 待机桶、后台执行机会 |
| `NotificationManagerService` | 通知渠道、前台服务通知和用户可感知入口 |

源码阅读时不要试图从入口一路读到底。

更好的方式是带着问题读：

```text
为什么这个 Service 被拒绝？
  -> 看 ActiveServices 的启动判断

为什么这个 Job 一直等待？
  -> 看 JobSchedulerService 的约束和配额

为什么这个 Alarm 没有准时？
  -> 看 AlarmManagerService 和 DeviceIdleController 的 idle 逻辑
```

## 第七部分：任务延迟不一定是 bug

很多开发者第一次遇到 WorkManager 或 JobScheduler 延迟时，会觉得：

```text
它怎么没有马上执行？
是不是框架坏了？
```

但系统调度的语义本来就不是“马上”。

例如你声明：

```text
需要网络
需要充电
需要设备空闲
失败后指数退避重试
```

那系统就会根据条件判断是否执行。

即使条件满足，也可能因为：

- 当前系统负载较高。
- App 处于受限待机桶。
- 任务配额不足。
- 设备进入省电模式。
- 厂商策略更激进。

所以后台任务排查不能只看业务日志，还要看系统状态。

## 第八部分：和稳定性治理的关系

后台任务失败常常会被误判。

```text
任务没执行
  -> 以为是 Crash
      -> 实际是进程被回收

任务执行慢
  -> 以为是网络慢
      -> 实际是 Doze 下被延迟

任务重复执行
  -> 以为是系统 bug
      -> 实际是重试策略和幂等没设计好

前台服务启动失败
  -> 以为是 Service 代码错
      -> 实际是后台启动限制或前台服务类型不合规
```

因此第 21 章会反复强调：

```text
后台任务要有证据意识。
```

你需要记录：

- 任务入队时间。
- 任务实际执行时间。
- 当前网络、电量、充电、前后台状态。
- 进程名和 pid。
- 约束条件。
- 重试次数。
- 失败原因。
- 用户是否可感知。

## 本节小结

Android 后台限制不是偶然的 API 变化，而是一套系统治理逻辑。

它要保护的是：

```text
用户电量
前台体验
系统公平
设备性能
后台任务的可解释性
```

第 21 章后面的内容，都会围绕这句话展开：

```text
后台任务不是越自由越好，而是越符合业务语义和系统调度越可靠。
```

## 自查问题

- 我能不能解释系统为什么不允许所有 App 长期后台运行？
- 我能不能区分后台进程、后台线程、Service、Foreground Service 和系统调度任务？
- 一个后台任务没执行时，我会先看业务日志，还是先判断它卡在系统判断链路的哪一步？
- 我能不能把“系统限制”拆成具体证据，而不是当成一句万能解释？
