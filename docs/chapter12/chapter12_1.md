# 12.1 为什么要学习 AMS / ATMS 与 Activity 启动

第 11 章里，我们已经知道：App 想请求系统能力，通常要通过 Framework API、Manager、Binder 和 system_server 中的系统服务协作。

第 12 章开始，我们选择最熟悉的一条路继续追下去：

```kotlin
startActivity(intent)
```

它看起来只是一行代码，但背后牵涉 Activity 启动、任务栈、进程创建、生命周期调度、Intent 解析、启动模式和系统限制。

## 本章通关画面

完成第 12 章后，你应该能把一次页面启动讲成这样：

```text
App 调用 startActivity()
  -> Context / Instrumentation 包装请求
      -> Binder 请求 ActivityTaskManagerService
          -> 解析目标 Activity 与启动参数
              -> 调整 Task / Back Stack
                  -> 必要时请求 Zygote 创建进程
                      -> ActivityThread 接收调度
                          -> Activity 生命周期回调执行
```

本章不要求你一次读完整个 Activity 启动源码，但要能建立一条主线：

```text
页面跳转不是两个 Activity 私下商量，而是系统服务参与调度的结果。
```

## 本章剧情线

如果第 11 章是“系统办事大厅”，第 12 章就是你拿着一张“页面启动申请表”来到 Activity 窗口前。

你告诉系统：“我要打开这个页面。”

系统要检查：目标页面是谁、能不能打开、放到哪个任务里、是否复用已有页面、是否要创建进程、生命周期如何切换、返回键应该回到哪里。

你看到的是页面跳转，系统看到的是一次任务调度。

## 本章探索任务

建议按下面路线推进：

```text
认识 AMS / ATMS
  -> 从 startActivity 进入系统服务
      -> 理解 Task 与返回栈
          -> 认识 ActivityRecord 等调度对象
              -> 理解进程创建与 ActivityThread 协作
                  -> 分析 launchMode 与 Intent Flag
                      -> 处理启动异常和限制
                          -> 完成任务栈观察实验
```

## 本节定位

本节是第 12 章入口。

我们先回答：

- 为什么 Activity 启动值得单独学？
- AMS / ATMS 到底解决什么问题？
- 为什么页面跳转和任务栈、进程、生命周期都有关系？

## 学习目标

学完本节后，你应该能够：

- 理解 Activity 启动是 Framework 核心主题。
- 知道 AMS / ATMS 与页面启动、任务栈管理有关。
- 明白 `startActivity()` 背后不是简单对象创建。
- 知道第 12 章 demo 要观察什么。

## 第一部分：startActivity 不是 new Activity

初学时很容易以为：

```text
startActivity 就是创建一个新的 Activity 对象。
```

这不准确。

Activity 是 Android 组件，它的创建、调度和生命周期由 Framework 管理。你发起启动请求，系统决定如何处理。

系统至少要知道：

- 目标 Activity 是否存在。
- Intent 是否能解析到组件。
- 启动方是否有权限。
- 目标页面放在哪个 Task。
- 是否需要复用已有 Activity。
- 是否需要创建新进程。

## 第二部分：AMS / ATMS 负责调度

历史上，Activity 启动和任务栈管理常与 AMS 相关。

现代 Android 中，Activity 和任务相关调度大量由 ATMS 负责。

入门阶段可以先这样理解：

```text
AMS / ATMS 是 Activity 启动、任务栈和进程协作的重要系统服务入口。
```

不要在第一天纠结每个版本的内部拆分。先抓住主线：页面启动需要系统服务参与。

## 第三部分：任务栈决定返回路径

用户打开多个页面后，按返回键应该回到哪里？

这不是 Activity 自己凭感觉决定的，而是由任务栈和启动规则共同决定。

例如：

- 普通启动可能不断创建新实例。
- `singleTop` 可能复用栈顶实例。
- `CLEAR_TOP` 可能清理目标页面之上的页面。
- 新任务 Flag 可能改变任务归属。

这些规则决定了用户的返回体验。

## 第四部分：第 12 章 demo 怎么用

第 12 章示例工程是：

```text
examples/12-activity-task-launch-lab/
```

它会做几件事：

- 记录 MainActivity、DetailActivity、SingleTopActivity 的生命周期。
- 用按钮发起不同启动方式。
- 观察 `onCreate()`、`onNewIntent()`、`onResume()` 等回调。
- 展示任务栈概念卡片和启动任务卡。
- 用观察分数提示你是否完成一次启动实验。

这个 demo 不是系统源码模拟器，而是“Activity 调度观察台”。

## 本节小挑战

### 页面启动申请题

请判断下面哪些问题需要系统参与：

- 目标 Activity 是否存在。
- 页面按钮颜色如何显示。
- 返回键应该回到哪里。
- 是否复用栈顶 Activity。
- 是否需要创建应用进程。

## 本节实践任务

### 基础任务

- 打开第 12 章 demo。
- 普通启动一次详情页。
- 回到首页，观察生命周期日志。

### 进阶任务

- 连续启动两次 `singleTop` 页面。
- 观察是否触发 `onNewIntent()`。
- 写下你理解的“复用 Activity”是什么意思。

## 本节小结

第 12 章的核心不是背 AMS / ATMS 名字，而是理解：页面启动是系统调度行为。`startActivity()` 背后有 Intent 解析、任务栈调整、进程协作和生命周期分发。理解这条链路，Activity 跳转、返回栈异常、启动问题和 Framework 源码才会真正连起来。
