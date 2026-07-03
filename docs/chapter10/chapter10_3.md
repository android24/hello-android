# 10.3 AOSP 源码阅读方法：从迷路到能定位

AOSP 很大。第一次打开源码时，很多人都会有同一个感受：

```text
每个目录都像入口，每个类都像重点，最后哪里都不敢点。
```

源码阅读最重要的不是“读得多”，而是“能定位”。

## 本节定位

本节不要求你搭建完整 AOSP 编译环境。我们的目标是学会用问题驱动的方式定位源码入口，并理解常见目录和类名。

## 学习目标

学完本节后，你应该能够：

- 理解 AOSP 源码阅读的基本顺序。
- 知道 Framework 常见源码目录。
- 用 API 名称、类名和调用链反向搜索入口。
- 避免一开始陷入细节。

## 第一部分：不要从目录开始读

不建议这样开始：

```text
我打开 frameworks/base，从第一个目录开始看。
```

这会很快耗尽耐心。

更好的方式是从一个问题开始：

```text
Activity.onCreate() 是谁调的？
Handler.post() 最后怎么执行到 Runnable？
startActivity() 为什么会跨进程？
```

问题会帮你过滤无关代码。

## 第二部分：常见源码目录

入门阶段先记住几个高频位置：

```text
frameworks/base/core/java/android/app/
frameworks/base/core/java/android/os/
frameworks/base/core/java/android/content/
frameworks/base/services/core/java/com/android/server/
frameworks/base/core/java/android/view/
```

它们大致对应：

- `android.app`：Activity、Application、Instrumentation、ActivityThread。
- `android.os`：Handler、Looper、MessageQueue、Binder 相关 API。
- `android.content`：Context、Intent、ContentResolver。
- `com.android.server`：系统服务实现。
- `android.view`：View、Window、输入事件和绘制相关入口。

## 第三部分：从应用 API 反向搜索

源码阅读可以从你熟悉的 API 开始。

例如你想理解页面启动：

```text
startActivity()
  -> ContextImpl.startActivity()
      -> Instrumentation.execStartActivity()
          -> ActivityTaskManagerService
```

例如你想理解主线程消息：

```text
Handler.post()
  -> Handler.sendMessageDelayed()
      -> MessageQueue.enqueueMessage()
          -> Looper.loop()
              -> Handler.dispatchMessage()
```

这样的链路不一定一开始就完全准确，但它能帮你找到方向。

## 第四部分：源码阅读的三层笔记

建议每次读源码都写三层笔记：

### 第一层：一句话结论

例如：

```text
ActivityThread 是应用进程主线程的核心调度入口。
```

### 第二层：调用链

例如：

```text
ActivityThread.main()
  -> Looper.prepareMainLooper()
  -> attach()
  -> Looper.loop()
```

### 第三层：关键问题

例如：

```text
Application 是在哪里创建的？
Activity 的生命周期回调由谁触发？
```

这样的笔记比复制大片源码更有价值。

## 第五部分：如何避免被细节淹没

源码里有大量兼容逻辑、异常处理、权限检查、缓存、统计和历史包袱。

初学阶段可以先跳过：

- 复杂分支。
- 低频兼容逻辑。
- 过长的异常处理。
- 和当前问题无关的字段。

先抓主干，再补枝叶。

读源码像看地铁图：第一次只需要知道从哪里上车、在哪里换乘、最终到哪里。至于每个站口通向哪条街，可以以后再慢慢补。

## 第六部分：第 10 章推荐源码入口

建议本章优先看这些类：

- `ActivityThread`
- `Application`
- `Instrumentation`
- `ContextImpl`
- `Handler`
- `Looper`
- `MessageQueue`
- `Binder`
- `IBinder`

第 10 章 demo 会把这些入口列在页面和 README 中，方便你一边运行 App，一边对照源码。

## 本节小挑战

请尝试为下面的问题写出“可能的源码入口”：

- `Application.onCreate()` 是谁触发的？
- `Handler.post {}` 为什么能回到主线程？
- `Context.startActivity()` 后面可能会进入哪个系统服务？

## 本节实践任务

### 基础任务

- 打开第 10 章 demo。
- 在源码入口卡片中选择一个类名。
- 去 AOSP 在线源码或本地源码中搜索这个类。

### 进阶任务

- 为 `Handler.post()` 写一条 5 行以内的调用链。
- 为 `Activity.onCreate()` 写一条“我目前能理解的调用链”。
- 不要求完全正确，但要标出你不确定的位置。

## 本节小结

读 AOSP 的关键不是硬啃，而是带着问题定位入口。先找到主干，再慢慢理解细节。第 10 章的每个主题都会围绕一个问题展开，让源码阅读从“巨大压力”变成“可追踪的线索”。
