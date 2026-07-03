# 10.5 ActivityThread、Application、Instrumentation 与 Context

如果第 10 章只能先记住几个 Framework 名字，`ActivityThread`、`Application`、`Instrumentation` 和 `Context` 一定在名单里。

它们经常出现在启动链路、生命周期、插件化、测试、热修复和源码阅读中。

## 本节剧情钩子

如果把应用进程看成一个临时剧场，那么这几个角色的分工很像一场开演前的后台协作：

- `ActivityThread` 像总调度，负责让各个环节按顺序发生。
- `Application` 像剧场开门前的总准备。
- `Instrumentation` 像舞台监督，负责把演员请到正确位置。
- `Context` 像通行证，让组件可以访问资源、服务和环境。

看懂它们，生命周期就不再像“系统突然调用了我的方法”，而更像一场有调度、有入口、有身份的演出。

## 本节定位

本节把应用启动后最核心的几个角色放到同一张关系图里，帮助你理解它们各自负责什么。

## 学习目标

学完本节后，你应该能够：

- 理解 `ActivityThread` 在应用进程中的位置。
- 知道 `Application` 为什么通常只创建一次。
- 理解 `Instrumentation` 和生命周期调度、测试之间的关系。
- 区分 `Application Context` 和 `Activity Context` 的使用场景。

## 第一部分：ActivityThread 不是 Thread

`ActivityThread` 的名字很容易误导人。

它不是一个继承自 `Thread` 的线程类，而是应用进程主线程上的核心调度对象。

可以先这样记：

```text
ActivityThread 管的是应用主线程上的 Framework 调度。
真正的线程是进程启动后的 main thread。
```

它负责和系统服务协作，接收生命周期调度，并在应用进程内完成组件创建。

## 第二部分：Application 是应用级入口

`Application` 通常在应用进程内创建一次。

它适合做：

- 全局初始化。
- 日志系统初始化。
- 依赖注入入口。
- 进程级状态记录。

但它不适合做大量耗时工作。

因为 `Application.onCreate()` 位于启动链路早期，如果这里做太多事，首屏展示会被拖慢。

第 9 章的启动优化经验，到这里就和 Framework 连接起来了。

## 第三部分：Instrumentation 像一位调度员

`Instrumentation` 经常被初学者忽略。

它参与了 Activity 创建和生命周期调用，也和自动化测试关系密切。

可以先这样理解：

```text
Instrumentation 是 Framework 调用应用组件时的一层调度入口。
```

当你以后学习测试框架、插件化或 Activity 启动源码时，会反复看到它。

## 第四部分：Context 是访问系统能力的门牌

`Context` 是 Android 中最常见、也最容易被轻视的对象之一。

你用它来：

- 获取资源。
- 启动 Activity。
- 获取系统服务。
- 访问文件目录。
- 创建数据库。

但不同 Context 的生命周期不同。

`Application Context` 跟随应用进程，生命周期长。

`Activity Context` 跟随页面，能访问窗口、主题和页面相关资源。

如果长期持有 `Activity Context`，就可能导致内存泄漏。

## 第五部分：它们之间的大致关系

可以这样理解：

```text
ActivityThread
  -> 管理应用进程内的组件调度
      -> 创建 Application
      -> 通过 Instrumentation 创建和调用 Activity
      -> 为组件绑定 Context
```

这不是完整源码图，但足够支撑第 10 章的理解。

## 第六部分：demo 中如何观察它们

第 10 章 demo 会展示：

- `Application.onCreate()` 日志。
- `Activity.onCreate()` / `onStart()` / `onResume()` 日志。
- 当前 Context 类型。
- 进程创建到 Activity 创建的大致耗时。

你可以把这些日志当成进入源码前的“脚印”。

## 本节小挑战

### 角色认领任务

请把下面三个问题当成一场后台分工检查。每答对一个，你就能少把一个系统角色记混。

请回答：

- 为什么不应该在 `Application.onCreate()` 里塞满所有初始化？
- `ActivityThread` 为什么不是普通线程类？
- 为什么弹窗、主题等场景通常需要 `Activity Context`？

## 本节实践任务

### 基础任务

- 打开 demo 中的 `FrameworkWalkthroughApplication.kt`。
- 找到 `Application.onCreate()` 日志。
- 打开 `MainActivity.kt`，观察生命周期日志。

### 进阶任务

- 在 `onPause()` 中增加一条日志。
- 在页面中展示 `applicationContext::class.java.simpleName`。
- 解释为什么这个值和 `Activity` 自身类型不同。

## 本节小结

`ActivityThread`、`Application`、`Instrumentation` 和 `Context` 是理解 Android 应用进程的四个关键角色。你不必第一天读完它们所有源码，但要先知道它们各自站在哪里、负责什么、和你写的代码如何相遇。
