# 10.4 从一次 App 启动看 Framework 调用链

App 启动是 Framework 入门最好的切口之一。

因为它连接了你熟悉的应用代码，也连接了系统服务、进程创建、主线程消息循环和生命周期调度。

## 本节定位

本节用一条简化启动链路，把“点击图标到 Activity.onCreate()”串起来。

我们不会在这一节展开所有源码细节，而是先建立整体路线。

## 学习目标

学完本节后，你应该能够：

- 说出冷启动的大致链路。
- 理解 Launcher、系统服务、Zygote 和应用进程的关系。
- 知道 `ActivityThread.main()` 为什么重要。
- 用 demo 观察应用侧能看到的启动节点。

## 第一部分：启动不是 Activity 自己开始的

当用户点击桌面图标时，最先响应的是 Launcher。

Launcher 本质上也是一个 App。它会向系统发起启动请求，而不是直接创建你的 `Activity`。

简化链路可以理解为：

```text
Launcher
  -> startActivity()
      -> ActivityTaskManagerService
          -> 判断目标 Activity、任务栈和进程状态
```

如果目标应用进程还不存在，系统需要先创建进程。

## 第二部分：Zygote 负责 fork 应用进程

Android 不会从零开始创建每个应用进程。

系统启动时会先准备一个 Zygote 进程。启动新 App 时，Zygote 会 fork 出新的应用进程。

可以先这样理解：

```text
Zygote 像一个提前准备好的进程模板。
新 App 启动时，从模板 fork 出一个应用进程。
```

这能减少启动成本，也让应用进程拥有 Android Runtime 等基础环境。

## 第三部分：ActivityThread.main() 登场

应用进程创建后，会进入 `ActivityThread.main()`。

这是 Framework 入门必须记住的入口之一。

它大致会做几件关键事：

```text
准备主线程 Looper
  -> 创建 ActivityThread
      -> attach 到系统服务
          -> 进入 Looper.loop()
```

进入 `Looper.loop()` 后，主线程就开始等待和处理消息。

应用后续的生命周期调度，也会通过这条主线程消息机制执行。

## 第四部分：Application 和 Activity 被创建

系统会通过 Framework 调度创建：

- `Application`
- `Activity`
- `Context`
- 生命周期回调

你写的：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
}
```

并不是自己突然执行的，而是 Framework 在合适的时机调用的。

这就是为什么理解启动链路能帮助你理解生命周期。

## 第五部分：demo 中能观察到什么

第 10 章 demo 只能运行在应用进程内，因此看不到系统服务内部所有步骤。

但它可以观察应用侧节点：

- `Application.onCreate()`
- `Activity.onCreate()`
- `Activity.onStart()`
- `Activity.onResume()`
- 当前进程 ID。
- 当前线程名。
- 主 Looper 状态。

这些信息是你进入系统源码前的地面标记。

## 第六部分：启动链路的学习边界

本节先不要急着展开：

- ActivityTaskManagerService 的完整调度。
- Task、Stack、LaunchMode 的复杂规则。
- Window 创建与首帧渲染细节。
- Zygote socket 通信细节。

它们都重要，但不是第 10 章第一轮要吃完的内容。

先把路线认清楚，后面才能逐个深入。

## 本节小挑战

请写出你理解中的启动链路：

```text
Launcher -> ? -> ? -> ActivityThread.main() -> ? -> Activity.onCreate()
```

然后对照 demo 页面里的启动链路卡片补全。

## 本节实践任务

### 基础任务

- 运行第 10 章 demo。
- 查看“启动链路”卡片。
- 打开 Logcat，按时间顺序记录 `Application` 和 `Activity` 生命周期日志。

### 进阶任务

- 强制停止 App 后重新启动，观察冷启动日志。
- 按 Home 键再回到 App，观察生命周期差异。
- 旋转屏幕，观察 Activity 是否重新创建。

## 本节小结

一次 App 启动不是一个 `onCreate()` 的故事，而是一条从 Launcher、系统服务、Zygote、ActivityThread 到生命周期回调的链路。理解这条链路，后面再看启动优化、ANR、页面跳转和 Framework 源码都会更有方向。
