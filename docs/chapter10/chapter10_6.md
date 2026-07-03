# 10.6 Handler、Looper、MessageQueue 与主线程模型

第 6 章里，我们从 Thread、线程池过渡到了协程。第 9 章里，我们反复强调不要阻塞主线程。

到了第 10 章，我们要继续往下问：

主线程为什么能一直等待事件？

`Handler.post {}` 为什么能把任务切回主线程？

生命周期、点击事件和 UI 更新为什么都围绕主线程展开？

答案离不开 `Handler`、`Looper` 和 `MessageQueue`。

## 本节定位

本节从 Framework 角度理解 Android 主线程消息模型。

它是后续学习 ANR、事件分发、Choreographer、View 绘制和 Binder 回调的重要基础。

## 学习目标

学完本节后，你应该能够：

- 解释 `Looper`、`MessageQueue`、`Handler` 的关系。
- 理解为什么主线程需要消息循环。
- 知道 `Handler.post {}` 的大致执行过程。
- 用 demo 发送一次主线程消息并观察结果。

## 第一部分：主线程不是执行完就退出

普通 Kotlin / Java 程序的 `main()` 方法执行完，程序通常就结束了。

Android App 不一样。

应用进程启动后，主线程会进入一个消息循环：

```text
准备 Looper
  -> 进入 Looper.loop()
      -> 不断从 MessageQueue 取消息
          -> 分发给对应 Handler
```

只要进程还活着，主线程就会不断等待和处理消息。

## 第二部分：三位角色

可以用一个简化比喻理解：

- `MessageQueue`：消息队列，负责排队。
- `Looper`：取消息的人，负责循环取出消息。
- `Handler`：投递和处理消息的人，负责把任务放入队列，也负责收到消息后执行。

简化流程：

```text
Handler.post(Runnable)
  -> Runnable 被包装成 Message
      -> Message 进入 MessageQueue
          -> Looper.loop() 取出 Message
              -> Handler 执行 Runnable
```

## 第三部分：为什么 UI 必须小心主线程

主线程负责太多事情：

- 生命周期调度。
- 点击事件处理。
- UI 状态更新。
- 绘制相关任务。
- 部分 Binder 回调。

如果你在主线程做耗时工作，队列里的后续消息就会被堵住。

这就是第 9 章卡顿和 ANR 的根源之一。

```text
主线程不是不能忙，而是不能被长时间霸占。
```

## 第四部分：协程和 Handler 的关系

Kotlin 协程让异步代码更好写，但它并没有让主线程消息机制消失。

当你使用：

```kotlin
Dispatchers.Main
```

本质上仍然需要把任务调度回 Android 主线程。

协程是更高级的并发抽象，`Handler` / `Looper` 是 Android 主线程调度的基础设施之一。

这就是从 Java 线程模型过渡到 Kotlin 协程后，仍然要理解主线程模型的原因。

## 第五部分：demo 中如何观察

第 10 章 demo 里有一个按钮：

```text
发送 Handler 消息
```

点击后，ViewModel 会通过主线程 `Handler` 投递一个任务，并记录：

- 投递时刻。
- 执行线程。
- 等待耗时。
- 当前 Looper 是否为主 Looper。

这不是复杂实验，但能把抽象消息模型变成可见结果。

## 第六部分：不要把 Handler 当作万能异步工具

`Handler` 可以切线程、延迟执行、投递消息，但它不是现代业务异步的全部答案。

实际开发中：

- UI 状态流转更适合用 ViewModel、StateFlow 和 Compose State。
- 后台耗时任务更适合用协程、线程池或 WorkManager。
- 主线程回调可以理解 Handler 模型，但不一定手写 Handler。

学习 Handler 是为了理解系统，不是为了所有业务都回到手写消息。

## 本节小挑战

请补全这条链路：

```text
Handler.post()
  -> ?
  -> MessageQueue
  -> ?
  -> Runnable.run()
```

## 本节实践任务

### 基础任务

- 运行第 10 章 demo。
- 点击 `发送 Handler 消息`。
- 查看页面上的线程报告。

### 进阶任务

- 把 `post` 改成 `postDelayed`，延迟 1000ms。
- 点击按钮后立刻观察页面是否马上变化。
- 用自己的话解释延迟消息为什么不会阻塞主线程。

## 本节小结

`Handler`、`Looper` 和 `MessageQueue` 是 Android 主线程模型的核心。理解它们，你就能更好地解释生命周期为什么在主线程回调、卡顿为什么会发生、协程为什么能切回主线程，以及后续 Framework 调度为什么总绕不开消息队列。
