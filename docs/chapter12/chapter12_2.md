# 12.2 从 startActivity 到系统服务

`startActivity()` 是应用开发里最常见的 API 之一。

这一节，我们从这行熟悉代码出发，看它如何一步步走向系统服务。

## 本节剧情钩子

你在 App 里写：

```kotlin
startActivity(intent)
```

感觉像对另一个页面说：“你出来一下。”

但真实情况更像提交一张申请表：先经过本地窗口检查，再交给系统服务审批，最后由系统安排目标页面登场。

## 本节定位

本节建立 `startActivity()` 的简化调用链。

## 学习目标

学完本节后，你应该能够：

- 知道 `startActivity()` 会通过 Framework 继续向下传递。
- 理解 Context、Instrumentation 与 ATMS 的关系。
- 知道 Binder 在 Activity 启动请求中的位置。
- 能写出一条简化启动调用链。

## 第一部分：从 Context 开始

很多启动请求从 Context 发出：

```kotlin
startActivity(intent)
```

或：

```kotlin
context.startActivity(intent)
```

Context 负责提供应用访问系统能力的入口。

但 Context 自己不会真正完成整个启动调度。

## 第二部分：Instrumentation 参与包装

Activity 启动链路里经常能看到 `Instrumentation`。

它参与 Activity 启动、创建和生命周期分发。

简化理解：

```text
Activity.startActivity()
  -> Instrumentation.execStartActivity()
      -> 发起系统服务请求
```

以后读源码时，`Instrumentation` 是一个重要中转站。

## 第三部分：请求进入 ATMS

页面启动最终需要系统服务判断和调度。

简化链路：

```text
App 进程
  -> Instrumentation
      -> Binder
          -> ActivityTaskManagerService
```

ATMS 会继续处理：

- Intent 解析。
- ActivityInfo 查询。
- 启动权限检查。
- Task 调整。
- 目标进程状态判断。

## 第四部分：为什么不能 App 自己启动

如果 App 可以自己决定所有 Activity 启动，会出现很多问题：

- 可以绕过权限。
- 可以伪造系统返回栈。
- 可以任意抢占前台。
- 可以绕开后台启动限制。
- 无法统一处理多窗口、多任务和生命周期。

所以 Activity 启动必须由系统服务统一调度。

## 第五部分：简化调用链

第 12 章入门阶段可以先记这条线：

```text
Activity.startActivity()
  -> Instrumentation.execStartActivity()
      -> ActivityTaskManager.getService()
          -> Binder
              -> ActivityTaskManagerService.startActivity()
```

不同 Android 版本细节可能变化，但这条“App 发起 -> Framework 包装 -> Binder -> 系统服务”的主线很重要。

## 本节小挑战

### 申请表流转题

请补全下面链路：

```text
Activity.startActivity()
  -> ?
      -> Binder
          -> ?
```

## 本节实践任务

### 基础任务

- 打开第 12 章 demo。
- 点击普通启动详情页。
- 在日志中找到 `MainActivity.onPause()` 和 `DetailActivity.onCreate()`。

### 进阶任务

- 在启动按钮旁新增一条日志。
- 记录点击按钮到目标页面 `onCreate()` 的顺序。
- 写一条你理解的 `startActivity()` 简化调用链。

## 本节小结

`startActivity()` 是应用层入口，不是完整启动过程本身。Framework 会经过 Context、Instrumentation、Binder，把请求交给 ATMS 等系统服务处理。记住这条线，后面理解任务栈、进程创建和启动异常都会更轻松。
