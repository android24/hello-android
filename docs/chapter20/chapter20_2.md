# 20.2 ANR：系统如何判断 App 无响应

ANR 是 Android 稳定性里最容易被误解的问题之一。

很多人把 ANR 简单理解成：

```text
主线程卡住了
```

这句话不算错，但太粗。

更准确地说：

```text
系统向 App 发起某类需要及时完成的交互或回调
  -> App 没有在规定时间内响应
      -> 系统认为它影响用户体验或系统调度
          -> 记录 trace 和日志
              -> 弹出或上报 ANR
```

ANR 不是单纯的“慢”，而是“在系统期待你响应的时候，你没有响应”。

## 本节定位

本节负责解释：

- ANR 发生的基本条件是什么？
- Input、Broadcast、Service、ContentProvider ANR 有什么差异？
- 为什么主线程、Binder 和锁等待会共同参与 ANR？
- 为什么有些 ANR 看起来不是你当前点击的那段代码造成的？

## 学习目标

学完本节后，你应该能够：

- 说清楚 ANR 和普通卡顿的区别。
- 区分 Input ANR、Broadcast ANR、Service ANR、Provider ANR。
- 解释为什么 Service 不等于后台线程。
- 解释为什么 Binder 阻塞可能扩散成 ANR。
- 知道 ANR 排查不能只盯着当前页面代码。

## 第一部分：ANR 和卡顿不是一回事

卡顿是用户体验问题：

```text
一帧没有及时画出来
滑动不顺
动画掉帧
点击后响应慢
```

ANR 是系统判定问题：

```text
输入事件没有及时处理
广播没有及时返回
Service 回调没有及时完成
ContentProvider 发布或查询卡住
```

卡顿可能不会变成 ANR。

ANR 一定意味着系统已经认为这次无响应超过了某个关键边界。

例如：

```text
主线程忙 80ms
  -> 用户感觉掉帧
  -> 通常不是 ANR

主线程忙 8s
  -> 输入事件迟迟处理不了
  -> 可能触发 Input ANR
```

所以排查 ANR 时，不要只问“为什么慢”，还要问：

```text
系统当时在等 App 完成什么？
```

### 系统侧等待模型

可以把 ANR 理解成一套“系统等待模型”。

系统不是随便找一个 App，然后说它慢。

通常是某条系统链路已经把任务交给 App：

```text
system_server / InputDispatcher
  -> 发送输入、广播、服务、Provider 等调度请求
      -> App 进程通过 Binder 或主线程消息接收
          -> 系统记录开始等待的时间点
              -> App 在规定窗口内没有完成
                  -> 系统触发 ANR 取证
```

不同 ANR 的等待对象不一样：

| ANR 类型 | 系统等待的对象 | App 侧关键执行点 |
| --- | --- | --- |
| Input ANR | 输入事件完成分发和消费 | `ViewRootImpl`、Activity、View / Compose 输入处理 |
| Broadcast ANR | 广播处理完成 | `BroadcastReceiver.onReceive()` |
| Service ANR | Service 生命周期回调完成 | `onCreate()`、`onStartCommand()`、`onBind()` |
| Provider ANR | Provider 发布、查询或跨进程调用返回 | `ContentProvider.onCreate()`、`query()`、`insert()` 等 |

所以，ANR 不是一个孤立栈，而是：

```text
系统等待点
  + App 执行点
  + 超时时间
  + 当前线程现场
```

四者一起构成事故。

如果只看 App 栈，不看系统等待点，就会漏掉“为什么这次被判定为 ANR”。

## 第二部分：常见 ANR 类型

### Input ANR

Input ANR 常见于点击、滑动、按键事件无法及时分发。

大致链路是：

```text
InputReader 读取输入
  -> InputDispatcher 分发事件
      -> ViewRootImpl / App 主线程处理输入
          -> App 长时间没有完成处理
              -> Input dispatching timed out
```

常见根因：

- 主线程执行耗时任务。
- 主线程等待锁。
- 主线程同步等待 Binder 返回。
- 主线程被大量消息、布局、绘制或 I/O 拖住。

### Broadcast ANR

Broadcast ANR 发生在广播接收处理没有及时完成时。

很多初学者会在 `onReceive()` 中直接做网络、数据库或文件操作，这是危险的。

```text
system_server 分发广播
  -> App 主线程执行 BroadcastReceiver.onReceive()
      -> onReceive 长时间不返回
          -> Broadcast timeout
```

原则是：

```text
onReceive 只做快速判断和任务转交
耗时任务交给 WorkManager、Service、线程池或其他可靠调度方式
```

### Service ANR

Service 经常被误解。

很多人以为：

```text
Service 是后台组件
  -> 所以 Service 回调在后台线程执行
```

这是错的。

默认情况下，`Service.onCreate()`、`onStartCommand()`、`onBind()` 仍然运行在主线程。

```text
startService
  -> system_server 调度
      -> App 主线程执行 onStartCommand
          -> 回调长时间不返回
              -> Service timeout
```

所以 Service 的正确姿势不是“在回调里做耗时任务”，而是“在回调里启动后台执行单元，并尽快返回”。

### ContentProvider ANR

Provider 相关卡住可能发生在安装、启动、查询或跨进程访问路径中。

尤其要注意：

```text
ContentProvider 可能早于 Application.onCreate 初始化
```

如果 Provider 初始化里做了大量 I/O、锁等待、网络等待，就可能拖慢甚至卡住进程启动或跨进程调用。

### 前台、后台和超时阈值

ANR 的超时阈值不是一句“统一 5 秒”就能概括。

不同组件、前后台状态、系统版本和调度场景会影响系统等待策略。

学习时可以先记住原则：

```text
越直接影响用户交互的链路
  -> 系统越不能长期等待

越靠后台调度的链路
  -> 超时判断可能更长，但也不能无限执行
```

例如 Input ANR 通常最容易被用户感知，因为它直接挡住点击和滑动；Broadcast / Service 则更多体现为系统调度被占住。

工程上不要把阈值当成“可用预算”：

```text
系统可能 10 秒后才判 ANR
  -> 不代表你可以在主线程做 9 秒任务
```

主线程和组件回调的目标应该是“尽快返回”，而不是“卡在阈值之前”。

## 第三部分：ANR 的核心不是“谁慢”，而是“谁在等谁”

ANR trace 里最重要的问题不是：

```text
哪一行代码看起来耗时？
```

而是：

```text
main 线程在做什么？
它是不是在等锁？
锁是谁持有？
它是不是在等 Binder？
远端进程在做什么？
系统当时等待的是输入、广播、服务还是 Provider？
```

例如：

```text
main thread
  -> waiting to lock SessionStore

Binder:1234_2
  -> locked SessionStore
      -> calling remote service

remote main
  -> waiting database result
```

这类问题不是一句“主线程卡住”能解释的。

它是一条等待链。

资深工程师读 ANR，本质上是在还原等待链。

## 第四部分：Binder 为什么会参与 ANR

Binder 是 Android 系统里最常见的跨进程通信方式。

如果主线程发起同步 Binder 调用，就可能出现：

```text
main 线程
  -> 调用远端 Binder
      -> 等远端返回
          -> 远端线程忙、死锁或反向等待
              -> main 线程无法处理输入
                  -> ANR
```

更复杂的是：

```text
主进程持有锁 A
  -> 同步调用 remote
      -> remote 反向调用主进程
          -> 需要锁 A
              -> 双方等待
                  -> ANR / 死锁
```

所以 Binder 排查要同时看两个进程：

- 发起调用的一方。
- 被调用的一方。
- 是否同步等待。
- 是否持锁跨进程调用。
- 是否有 Binder death、超时和降级策略。

## 第五部分：ANR 常见误区

### 误区一：只看 ANR 发生页面

ANR 弹出时用户在哪个页面，不一定代表根因在哪个页面。

主线程可能早已被前一个任务、后台初始化、数据库锁、远端 Binder 调用拖住。

### 误区二：只看 main 线程第一行

main 线程第一行只是入口。

如果它处于 `BLOCKED`，你必须找持锁线程。

如果它处于 Binder 调用，你必须看远端。

如果它处于 MessageQueue 等待，可能 ANR 根因不在当前 trace 中，需要结合历史日志。

### 误区三：以为所有 ANR 都能本地复现

线上 ANR 经常和设备性能、系统版本、低内存、锁时序、网络、Binder 远端状态有关。

不能复现不代表问题不存在。

你需要补充证据采集。

## 本节小挑战

### ANR 类型判断题

判断下面场景更可能是哪类 ANR：

```text
1. 用户点击按钮后页面无响应，logcat 出现 Input dispatching timed out。
2. 推送广播收到后同步上传日志，数秒后系统报超时。
3. startService 后 onStartCommand 里直接 Thread.sleep。
4. App 启动时某个 Provider 初始化数据库并等待文件锁。
```

你需要回答：

- 分别对应哪类 ANR？
- 第一条应该看哪个线程？
- 是否需要看 Binder 远端？
- 修复方向是什么？

## 本节实践任务

### 基础任务

- 在 debug 工程里模拟一次主线程 sleep。
- 模拟一次 `BroadcastReceiver.onReceive()` 耗时。
- 模拟一次 `Service.onStartCommand()` 耗时。

### 进阶任务

- 把三类 ANR 的 trace 放在一起，对比 main 线程栈差异。
- 写一份 ANR 判断表：类型、系统日志、主线程位置、修复方向。

## 本节小结

ANR 的本质是系统等待 App 响应，而 App 没能及时响应。

排查 ANR 时，最重要的是找到：

```text
系统在等什么
main 在做什么
谁持有 main 等待的资源
是否存在跨进程等待
```

读懂这四个问题，ANR 就不再只是“卡了”。
