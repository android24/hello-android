# 20.3 ANR trace：如何读 main、Binder、锁和系统超时

上一节我们知道，ANR 不是简单的“慢”，而是系统等待 App 响应超时。

这一节进入真正的事故现场：ANR trace。

trace 看起来很吓人，因为它会把进程里很多线程都打印出来：

```text
main
RenderThread
Binder:xxxx_x
DefaultDispatcher-worker
FinalizerDaemon
Signal Catcher
```

初学者很容易被海量线程淹没。

资深工程师读 trace 的方式更像从一张城市交通图里找拥堵源头：

```text
先看主干道 main
  -> 再看是否有锁等待
      -> 再找持锁线程
          -> 再看 Binder 是否跨进程等待
              -> 最后和系统超时原因对齐
```

## 本节定位

本节负责回答：

- ANR trace 应该按什么顺序读？
- `RUNNABLE`、`BLOCKED`、`WAITING`、`TIMED_WAITING` 如何理解？
- 如何从 main 线程找到持锁线程？
- 如何判断是否存在 Binder 阻塞？
- 如何把 trace 和 logcat 里的系统超时原因对应起来？

## 学习目标

学完本节后，你应该能够：

- 从 ANR trace 中快速定位 main 线程。
- 根据线程状态判断主线程是在执行、等待锁、等待条件还是 sleep。
- 看懂 `waiting to lock` 和 `locked` 的基本含义。
- 知道 Binder 线程和 remote 进程在 ANR 排查里的价值。
- 能写出一份清晰的 ANR trace 阅读结论。

## 第一部分：先看系统给出的超时原因

在读 trace 前，先看 logcat 或 bugreport 中的 ANR 原因。

常见信息类似：

```text
Input dispatching timed out
Broadcast of Intent
executing service
ContentProvider not responding
```

这句话很重要，因为它告诉你：

```text
系统当时在等什么
```

如果是 Input ANR，重点看主线程为什么无法处理输入。

如果是 Broadcast ANR，重点看 Receiver 是否没有返回。

如果是 Service ANR，重点看 Service 生命周期回调是否卡住。

如果是 Provider ANR，重点看 Provider 初始化或查询链路。

没有这一步，很容易看着 trace 里的某个忙线程就误判根因。

### trace 是快照，不是录像

ANR trace 很重要，但它不是完整录像。

它更像系统在事故发生时拍下的一张现场照片：

```text
ANR 条件满足
  -> 系统开始收集进程和线程栈
      -> dump 需要时间
          -> 线程状态可能已经变化
              -> trace 记录的是 dump 时刻附近的状态
```

这意味着：

- trace 很有价值，但不是全部真相。
- 如果问题瞬间发生又恢复，trace 可能只留下恢复后的状态。
- 如果 dump 过程排队或被系统负载影响，现场可能有时间差。
- 某些等待链需要结合历史 logcat、业务日志和时间线才能还原。

所以不要机械地认为：

```text
trace 顶部看到什么
  -> 根因就一定是什么
```

更可靠的方式是把证据对齐：

```text
ANR reason
  -> 发生时间
      -> 用户操作
          -> main 线程状态
              -> 锁 / Binder / worker
                  -> 之前几秒的日志
```

trace 是关键证据，但要放回时间线里读。

## 第二部分：main 线程是第一入口

找到 main 线程。

它通常类似：

```text
"main" prio=5 tid=1 Native
  at ...
```

你要先判断 main 的状态：

| 状态 | 可能含义 | 排查方向 |
| --- | --- | --- |
| RUNNABLE | 正在执行 Java 或 native 代码 | 看是否 CPU 计算、循环、I/O 或 native 调用 |
| BLOCKED | 等待进入 synchronized 锁 | 找 `waiting to lock` 对象和持锁线程 |
| WAITING | 等待条件、park、join 等 | 看等待对象和唤醒方 |
| TIMED_WAITING | sleep、wait(timeout)、parkNanos | 看是谁主动等待以及等待多久 |
| Native | 停在 native 方法或系统调用 | 看 native 栈、I/O、Binder、futex 等 |

main 线程不是永远都必须 `RUNNABLE`。

正常空闲时，它也可能在 MessageQueue 里等待消息。

关键是：

```text
系统需要 main 响应时
main 为什么没有响应？
```

## 第三部分：BLOCKED 时找锁

如果 main 线程里出现：

```text
waiting to lock <0x1234> (a com.example.SessionStore)
```

说明 main 正在等一把锁。

下一步不是继续骂 main，而是找：

```text
谁 locked <0x1234>
```

可能在另一个线程看到：

```text
locked <0x1234> (a com.example.SessionStore)
```

这时你要继续读持锁线程：

- 它在做 I/O 吗？
- 它在等网络吗？
- 它在同步调用 Binder 吗？
- 它是不是也在等另一把锁？
- 它是否可能永远不释放？

很多 ANR 的真正根因不在 main，而在持锁线程。

main 只是被拖住的那个人。

## 第四部分：Binder 阻塞要看两边

如果 main 线程停在 Binder 调用附近，说明它可能正在等远端返回。

这时只看本进程是不够的。

你需要问：

```text
远端进程是谁？
远端 Binder 线程在做什么？
远端 main 线程是否卡住？
是否存在反向调用？
是否持锁跨进程调用？
```

一个典型危险模式：

```text
main
  -> synchronized(lock)
      -> remote.call()

remote Binder thread
  -> callback main process
      -> need lock
```

这就是“持锁跨进程同步调用”的典型事故味道。

修复方向通常是：

- 不在主线程做同步 Binder 调用。
- 不持锁跨进程调用。
- 增加超时和降级。
- 异步化远端请求。
- 缩小锁范围。

### Binder 线程名不等于根因

trace 里常见很多 Binder 线程：

```text
Binder:12345_1
Binder:12345_2
Binder:12345_3
```

它们代表进入当前进程的 Binder 调用处理线程。

但看到 Binder 线程忙，不等于 Binder 就是根因。

你要继续判断：

```text
Binder 线程是在处理正常请求？
还是持有 main 等待的锁？
还是同步调用远端？
还是 Binder 线程池被耗尽？
```

真正危险的是下面几类：

- Binder 线程持有业务锁，main 等这把锁。
- main 同步调用远端 Binder，远端迟迟不返回。
- Binder 线程池被长任务占满，新的系统或业务 IPC 无法处理。
- 双向 Binder 调用形成反向等待。

所以 Binder 线程不是“看到就背锅”，而是要看它是否在等待链上。

## 第五部分：不要忽略线程池

如果 main 等待一个后台任务结果，而后台线程池被占满，也可能间接导致 ANR。

例如：

```text
main
  -> runBlocking / CountDownLatch.await
      -> 等 worker 返回

worker-1..worker-n
  -> 全部被日志上传、数据库迁移或大文件 I/O 占满
```

这类 ANR 不是“主线程自己慢”，而是主线程等待的后台资源不可用。

工程上要避免：

- 主线程同步等待后台结果。
- 所有业务共享一个无边界或过小线程池。
- 长任务和短任务混用同一个队列。
- 在锁内等待异步任务返回。

## 第六部分：把 trace 变成结论

读完 trace 后，不要只写：

```text
main 线程卡住了
```

这句话太粗。

更好的结论应该像这样：

```text
问题类型：Input ANR
系统原因：Input dispatching timed out
main 状态：BLOCKED
等待对象：SessionStore lock
持锁线程：Binder:1234_2
持锁线程行为：同步请求 remote 登录服务
remote 状态：remote main 正在等待数据库 I/O
结论：主线程等待登录态锁，锁由 Binder 回调持有；Binder 回调又同步等待 remote 数据库，形成跨线程跨进程等待链。
修复方向：登录态读取异步化，不在 Binder 回调中持锁做远端请求，主线程只读取快照状态。
```

这才是可以指导修复的 ANR 结论。

## 第七部分：常见 trace 阅读顺序

可以记住这个顺序：

```text
1. 看 ANR reason
2. 找 main 线程
3. 判断 main 状态
4. 如果 BLOCKED，找锁持有方
5. 如果 Binder，找远端进程和远端线程
6. 如果等待 worker，找线程池状态
7. 对齐发生时间、前后台状态和用户操作
8. 写结论和修复方向
```

这套顺序比“从上到下读完整份 trace”更高效。

## 第八部分：一份 trace 结论的好坏

差的结论通常长这样：

```text
主线程卡住导致 ANR。
```

它没有告诉别人：

- 卡在哪里？
- 为什么卡？
- 谁造成的？
- 如何修？
- 如何验证？

好的结论应该能被复现和回归：

```text
ANR 类型：Input ANR
系统原因：Input dispatching timed out
main 状态：BLOCKED
等待对象：CourseCache
持锁线程：DefaultDispatcher-worker-1
持锁线程行为：在锁内同步等待 remote Service 返回
远端状态：remote main 正在执行数据库迁移
根因：主线程读取课程缓存时等待 CourseCache；后台线程持有 CourseCache 并同步等待远端，形成跨线程跨进程等待链
修复：去掉锁内远端调用，缓存读取改为快照，remote 请求异步化并增加超时
回归：构造 remote 延迟 10s，连续点击课程详情，不再出现 main BLOCKED
```

这类结论才算把 trace 读成了工程行动。

## 本节小挑战

### trace 阅读题

请解释下面伪 trace：

```text
ANR reason: Input dispatching timed out

main:
  BLOCKED
  waiting to lock <SessionStore>
  at HomeViewModel.loadUser()

Binder:2345_1:
  RUNNABLE
  locked <SessionStore>
  at PushReceiver.updateSession()
  at RemotePushService.onMessage()
```

你需要回答：

- 这是哪类 ANR？
- main 为什么不能处理输入？
- 第一嫌疑线程是谁？
- 修复方向是什么？

## 本节实践任务

### 基础任务

- 找一份 ANR trace，标出 main 线程。
- 标出 main 线程状态。
- 标出所有 `waiting to lock` 和 `locked`。

### 进阶任务

- 写一份 ANR trace 阅读报告。
- 尝试构造一个锁等待 ANR，并用 trace 证明等待链。
- 对比主线程 sleep、锁等待、Binder 等待三种 trace。

## 本节小结

读 ANR trace，不是读栈名，而是还原等待链。

你要用 trace 回答：

```text
系统在等什么？
main 在等什么？
谁让 main 等？
远端或后台线程是否参与？
修复要打断哪条等待链？
```

能回答这些问题，trace 才算真的读懂。
