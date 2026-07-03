# 示例工程：Framework 源码观察实验室

## 对应章节

第10章 Android Framework 入门、系统架构与源码阅读方法

## 工程目标

本工程用于配合第 10 章，把 Android Framework 入门阶段最容易抽象的概念变成可以观察的小实验。

它会围绕三个问题展开：

- 一次 App 启动，在应用侧能观察到哪些节点？
- `Handler`、`Looper` 和 `MessageQueue` 如何支撑主线程消息？
- 读 AOSP 源码时，应该从哪些类和问题开始定位？

## 当前效果

运行后你会看到一个“第 10 章 Framework 观察实验室”页面：

- `Framework 追踪任务卡` 会把本章练习拆成四个可执行任务。
- `进程、线程与 Context` 展示 PID、当前线程、主 Looper、SDK、Context 类型等信息。
- `App 启动链路` 用简化步骤串起 Launcher、系统服务、Zygote、ActivityThread、Application 和 Activity。
- `Handler / Looper 实验` 可以发送一次主线程 Handler 消息，并观察执行线程与等待耗时。
- `系统分层地图` 展示 App、Framework、Native、Kernel 四层职责。
- `AOSP 源码入口` 列出 ActivityThread、Instrumentation、ContextImpl、Handler、Looper、MessageQueue、Binder 等阅读入口。
- `Binder 初识` 用常见系统能力解释为什么 Android 需要跨进程通信。
- `生命周期与消息轨迹` 记录 `Application`、`Activity` 和 Handler 实验日志。

这个 demo 不模拟完整 Android 系统。它的价值是把 Framework 源码阅读前的“路标”放到一个可运行 App 里，让学习者从现象出发，再去找源码。

## 探索玩法

建议不要把这个 demo 当成普通页面看完就关掉。请把它当成一次 Framework 现场追踪：

```text
先观察启动日志
  -> 再发送 Handler 消息
      -> 然后选择一个源码入口
          -> 最后写一份 10 行以内的追踪报告
```

页面里的 `Framework 追踪任务卡` 就是本章的行动路线。每完成一张卡，都把结果写到 `quality/framework-trace-template.md` 对应的栏目里。

最小通关报告可以长这样：

```text
问题：Handler.post() 为什么能回到主线程？
现象：点击按钮后，日志显示 Runnable 在 main 线程执行。
入口：Handler, MessageQueue, Looper
链路：post -> enqueueMessage -> loop -> dispatchMessage -> run
仍不确定：MessageQueue 底层如何等待下一条消息？
```

## 运行方式

1. 使用 Android Studio 打开 `examples/10-framework-source-walkthrough`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `FrameworkWalkthrough`。
5. 点击 `发送 Handler 消息`，观察页面和日志变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
10-framework-source-walkthrough/
  app/
    src/main/java/com/helloandroid/framework/
      FrameworkWalkthroughApplication.kt
      MainActivity.kt
      FrameworkWalkthroughState.kt
      FrameworkWalkthroughViewModel.kt
      FrameworkWalkthroughScreen.kt
  quality/
    framework-trace-template.md
    source-reading-notes.md
```

## 关键源码入口

- `FrameworkWalkthroughApplication.kt`：记录应用进程启动后的 `Application.onCreate()`。
- `MainActivity.kt`：记录 Activity 生命周期，并采集进程、线程和 Context 信息。
- `FrameworkWalkthroughViewModel.kt`：管理 Handler 消息实验和观察轨迹。
- `FrameworkWalkthroughState.kt`：集中放置系统分层、启动链路、源码入口和 Binder 示例。
- `FrameworkWalkthroughScreen.kt`：展示 Framework 观察实验室页面。
- `quality/framework-trace-template.md`：源码追踪笔记模板。
- `quality/source-reading-notes.md`：第 10 章源码阅读建议。

## 推荐对照的 AOSP 入口

```text
frameworks/base/core/java/android/app/ActivityThread.java
frameworks/base/core/java/android/app/Instrumentation.java
frameworks/base/core/java/android/app/ContextImpl.java
frameworks/base/core/java/android/os/Handler.java
frameworks/base/core/java/android/os/Looper.java
frameworks/base/core/java/android/os/MessageQueue.java
frameworks/base/core/java/android/os/Binder.java
```

建议先不要追所有分支。先带着问题看主线：

```text
ActivityThread.main() 在哪里准备主线程 Looper？
Application.onCreate() 是谁触发的？
Handler.post() 如何进入 MessageQueue？
Looper.loop() 如何分发消息？
ContextImpl.startActivity() 为什么会走向系统服务？
```

## 计划覆盖知识点

- Android 系统分层
- App 启动链路
- ActivityThread
- Application
- Instrumentation
- Context / ContextImpl
- Handler / Looper / MessageQueue
- 主线程模型
- Binder 初识
- AOSP 源码阅读方法

## 练习任务

### 基础任务

- 按照页面里的 `Framework 追踪任务卡` 完成一轮观察。
- 运行工程，观察首页展示的进程、线程和 Context 信息。
- 打开 Logcat，搜索 `FrameworkWalkthrough`。
- 点击 `发送 Handler 消息`，记录页面和日志变化。
- 对照 `AOSP 源码入口` 卡片，选择一个类去源码中搜索。

### 进阶任务

- 把 Handler 实验改成 `postDelayed`，观察延迟消息。
- 增加一个 `Build.VERSION.SDK_INT` 之外的系统信息。
- 在 `onPause()` 或 `onStop()` 中补充更具体的日志。
- 使用 `quality/framework-trace-template.md` 写一份 `Handler.post()` 调用链笔记。
- 为 `startActivity()` 写一条从 App API 到 system_server 的简化 Binder 链路。

## 通关目标

完成本工程后，你应该能说清楚：

- Framework 学习为什么要从问题出发。
- App 启动为什么不是 Activity 自己开始的。
- `ActivityThread.main()`、`Application.onCreate()` 和 `Activity.onCreate()` 的大致关系。
- `Handler`、`Looper`、`MessageQueue` 如何协作。
- Binder 为什么会出现在系统服务调用背后。
- 如何把一次应用现象整理成一份源码阅读笔记。
- 如何用一张任务卡，把抽象源码问题拆成可验证的小步骤。
